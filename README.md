# Go Nature Farms — Migrated Stack

This is the migrated version of the Go Nature Farms e-commerce app, moved from
**Node.js/Express/MySQL** + vanilla HTML/JS to:

- **Backend:** Java 21, Spring Boot 4.1.0, Spring Data JPA (Hibernate), Spring Security + JWT, Maven
- **Database:** PostgreSQL
- **Frontend:** React 18 (Vite), React Router, Context API + hooks, Axios

Two independent projects are delivered:

```
gonaturefarms-backend/     Spring Boot API server
gonaturefarms-frontend/    React (Vite) single-page app
```

They communicate purely over the REST API (`/api/**`), so each can be built,
deployed, and scaled independently.

---

## 1. Backend — Spring Boot + PostgreSQL

### 1.1 What was migrated
Every Express route file has a 1:1 Spring Boot equivalent:

| Original (Node/Express)     | Spring Boot                                                                 |
|------------------------------|-------------------------------------------------------------------------------|
| `routes/auth.js`             | `AuthController` → `AuthService`                                             |
| `routes/products.js`         | `ProductController` → `ProductService`                                       |
| `routes/orders.js`           | `OrderController` → `OrderService`                                           |
| `routes/wishlist.js`         | `WishlistController` → `WishlistService`                                     |
| `routes/reviews.js`          | `ReviewController` → `ReviewService`                                         |
| `routes/coupons.js`          | `CouponController` → `CouponService`                                         |
| `routes/support.js`          | `SupportController` → `SupportService`                                       |
| `routes/admin.js`            | 11 focused `Admin*Controller`s (Settings, Slides, FAQs, Zones, ScrollBlocks, Categories, Analytics, Orders, Users, Credentials, Upload, Export) → matching services |
| `middleware/auth.js`         | `JwtAuthenticationFilter` + `SecurityConfig` + `@PreAuthorize`                |
| `database/schema.sql` (MySQL)| `src/main/resources/db/schema.sql` (PostgreSQL DDL, fully commented)         |

**All API paths are byte-for-byte identical** to the original Express routes
(`/api/auth/...`, `/api/products/...`, `/api/admin/...`, etc.) and the JSON
response shape (`{success, message, ...}`) is preserved, including "soft-fail"
responses that return HTTP 200 with `success:false` for expected business-rule
failures — exactly like the original `res.json(...)` calls.

**One intentional omission:** `config/email.js` (OTP email helper) and the
`otp_store` table exist in the original codebase but are **never actually
called from any route** — they were dead code. To keep behavior identical,
they were not migrated. If you want OTP-based verification, it can be added
as a new feature but wasn't part of the original app's real behavior.

### 1.2 Project structure
```
src/main/java/com/gonaturefarms/
  ├── controller/    REST endpoints (thin — delegate to services)
  ├── service/       Business logic (mirrors each Express route file)
  ├── repository/    Spring Data JPA repositories
  ├── entity/        JPA entities (one per table)
  ├── dto/           Request/response DTOs, grouped by feature
  ├── security/      JWT generation/validation, Spring Security wiring
  ├── config/        Security, CORS, rate limiting, static file serving
  ├── exception/     Centralized exception handling
  └── util/          OrderIdGenerator, etc.
src/main/resources/
  ├── application.properties
  └── db/schema.sql  Hand-written PostgreSQL DDL (optional — see below)
```

### 1.3 Key design decisions
- **Jackson snake_case naming strategy** (`spring.jackson.property-naming-strategy=SNAKE_CASE`)
  is configured globally, so Java's `imgUrl` field automatically serializes as
  JSON's `img_url` — matching the original MySQL column names the frontend
  was already calling. This means most entities can be returned directly
  without manual DTO mapping.
- **BCrypt strength 12** — matches the original `bcryptjs` cost factor, so the
  seeded admin password hash in `schema.sql` keeps working without
  re-hashing.
- **JWT** uses the same claim names (`id, name, phone, email, role`) as the
  original `jsonwebtoken` tokens.
- **Soft-fail vs hard-fail errors:** `ApiException` → HTTP 200 with
  `{success:false, message}` (matches Express's implicit-200 `res.json`
  calls). `ResourceNotFoundException` → HTTP 404. Validation errors → HTTP 400.
  Unhandled errors → HTTP 500.
- **Rate limiting** — a lightweight in-memory filter mirrors the original
  `express-rate-limit` config (300 req/15min general, 20 req/15min on
  `/api/auth/**`). For multi-instance deployments, swap this for a
  Redis-backed limiter (e.g. Bucket4j + Redis).

### 1.4 Running locally

**Prerequisites:** JDK 21+, Maven 3.9+, PostgreSQL 14+ running locally (or via Docker).

```bash
# 1. Create the database
createdb gonaturefarms

# 2. (Optional) run the hand-written schema — or just let Hibernate create it (see below)
psql -d gonaturefarms -f src/main/resources/db/schema.sql

# 3. Configure environment (or edit application.properties directly)
export DB_HOST=localhost DB_PORT=5432 DB_NAME=gonaturefarms DB_USER=postgres DB_PASSWORD=postgres
export JWT_SECRET="change-me-to-something-long-and-random"

# 4. Run
mvn spring-boot:run
```

The API starts on `http://localhost:5000` by default (`PORT` env var to change).

**Two ways to get the schema into place:**
1. **Automatic (fastest for local dev):** leave `spring.jpa.hibernate.ddl-auto=update`
   in `application.properties` (the default). Hibernate creates/updates tables
   from the JPA entities on startup — you don't need to run `schema.sql` at all.
2. **Explicit (recommended for production):** run `db/schema.sql` yourself via
   `psql`, then set `spring.jpa.hibernate.ddl-auto=validate` so Hibernate only
   checks the schema matches instead of altering it. `schema.sql` is heavily
   commented with the MySQL→PostgreSQL translation notes and includes all the
   original seed data (default admin, sample products, coupons, delivery
   zones, FAQs, site settings).

**Default admin login:** username `Vishnu`, password `918252` — **change this
immediately** after first login via the admin panel's Credentials tab, or by
setting `ADMIN_DEFAULT_USER` / `ADMIN_DEFAULT_PASS` env vars for the built-in
fallback login.

### 1.5 Building for production
```bash
mvn clean package -DskipTests
java -jar target/gonaturefarms-backend.jar
```

> **Note on this sandbox:** Maven Central wasn't reachable from this
> environment, so the project could not be `mvn`-compiled here. All code was
> written and manually reviewed for correctness, but please run `mvn clean
> compile` yourself as a first step after downloading — if anything doesn't
> compile, the error message will point straight at the line.

---

## 2. Frontend — React (Vite)

### 2.1 What was migrated
The original single HTML file (`public/index.html`, 1280 lines) + vanilla JS
(`public/script.js`, 1885 lines) was rebuilt as a componentized React app,
keeping the **same CSS/visual design** (the original `<style>` block was
extracted directly into `src/App.css`) and calling the **same REST API**.

```
src/
  ├── api/client.js        Axios instance: JWT header, camelCase<->snake_case conversion
  ├── context/              AuthContext, CartContext, SiteContext, ToastContext
  ├── components/           Header, HeroSlider, ProductGrid/Card, CartDrawer,
  │                         CheckoutModal, AuthModal, OrdersModal, SupportModal,
  │                         ReviewModal, FaqSection, Testimonials, Footer, Modal
  ├── components/admin/     9 admin dashboard tabs (Analytics, Products, Orders,
  │                         Coupons, Reviews, Support, Customers, Content, Settings)
  └── pages/                HomePage, AdminPage
```

### 2.2 A note on naming conventions
The Spring Boot backend returns JSON in **snake_case** (`img_url`,
`order_id`, `customer_name`...) to match the database columns the original
frontend was built against. Rather than writing every React component in
snake_case, `src/api/client.js` has axios request/response interceptors that
**transparently convert between camelCase (idiomatic React/JS) and
snake_case (backend wire format)** — so components read/write natural
`product.imgUrl`, `order.customerName`, etc., while the actual HTTP traffic
stays snake_case. The one exception is the free-form site-settings
key/value map (`/admin/settings/*`), which is passed through unconverted
(`{ skipTransform: true }`) since its keys are dynamic, not a fixed shape.

### 2.3 Running locally
```bash
cd gonaturefarms-frontend
npm install
npm run dev
```
Opens on `http://localhost:5173`. The Vite dev server proxies `/api` and
`/uploads` to `http://localhost:5000` (the Spring Boot backend) — see
`vite.config.js`. Override the proxy target with `VITE_API_PROXY_TARGET` if
your backend runs elsewhere.

### 2.4 Building for production
```bash
npm run build     # outputs to dist/
npm run preview   # sanity-check the production build locally
```
Deploy `dist/` behind any static host (Nginx, Vercel, Netlify, S3+CloudFront,
or Spring Boot's own static resource serving) and point it at your deployed
API via `VITE_API_BASE` (see `.env.example`) if it isn't served from a
proxied same-origin path.

---

## 3. End-to-end local dev checklist
1. `createdb gonaturefarms && psql -d gonaturefarms -f gonaturefarms-backend/src/main/resources/db/schema.sql`
2. `cd gonaturefarms-backend && mvn spring-boot:run` (starts on :5000)
3. `cd gonaturefarms-frontend && npm install && npm run dev` (starts on :5173, proxies to :5000)
4. Visit `http://localhost:5173`, browse products, place a test order.
5. Visit `http://localhost:5173/admin`, log in with `Vishnu` / `918252`,
   change the admin password immediately.

---

## 4. What's intentionally different from the original
- **Frontend framework:** vanilla HTML/JS → React (per your request). Visual
  design, copy, and all user-facing behavior were kept the same; the DOM
  structure and interaction code were rewritten idiomatically for React.
- **OTP/email verification:** omitted (was dead code in the original — see §1.1).
- Everything else — API contracts, business rules, validation messages,
  pricing/coupon/GST math, order ID format, admin capabilities — was ported
  as closely as possible to the original behavior.
