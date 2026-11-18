-- Footer Upgrade Migration - All Phases
-- This migration adds support for extended footer settings, FAQ system, support tickets, policies, and social media

-- 1. Extend footer_links table with new fields
ALTER TABLE footer_links ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT true;
ALTER TABLE footer_links ADD COLUMN IF NOT EXISTS open_in_new_tab BOOLEAN DEFAULT false;
ALTER TABLE footer_links ADD COLUMN IF NOT EXISTS column_heading VARCHAR(100);

-- 2. Create social_media_links table
CREATE TABLE IF NOT EXISTS social_media_links (
    id BIGSERIAL PRIMARY KEY,
    platform VARCHAR(50) NOT NULL, -- instagram, facebook, youtube, etc.
    url VARCHAR(500) NOT NULL,
    icon VARCHAR(100),
    enabled BOOLEAN DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 3. Create support_settings table
CREATE TABLE IF NOT EXISTS support_settings (
    id BIGSERIAL PRIMARY KEY,
    support_phone VARCHAR(20),
    whatsapp_number VARCHAR(20),
    whatsapp_country_code VARCHAR(10) DEFAULT '91',
    support_email VARCHAR(100),
    business_hours VARCHAR(200),
    working_days VARCHAR(100),
    store_address TEXT,
    whatsapp_welcome_message TEXT,
    response_info TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 4. Create faq_categories table
CREATE TABLE IF NOT EXISTS faq_categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    sort_order INTEGER DEFAULT 0,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 5. Create faqs table
CREATE TABLE IF NOT EXISTS faqs (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT REFERENCES faq_categories(id) ON DELETE CASCADE,
    question VARCHAR(500) NOT NULL,
    answer TEXT NOT NULL,
    sort_order INTEGER DEFAULT 0,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- 6. Create support_tickets table
CREATE TABLE IF NOT EXISTS support_tickets (
    id BIGSERIAL PRIMARY KEY,
    ticket_number VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    order_id VARCHAR(50),
    category VARCHAR(100) NOT NULL,
    subject VARCHAR(200),
    description TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'Open', -- Open, In Progress, Resolved, Rejected
    priority VARCHAR(20) DEFAULT 'Normal', -- Low, Normal, High, Urgent
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    resolved_at TIMESTAMP
);

-- 7. Create ticket_replies table
CREATE TABLE IF NOT EXISTS ticket_replies (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES support_tickets(id) ON DELETE CASCADE,
    user_id BIGINT,
    is_admin BOOLEAN DEFAULT false,
    message TEXT NOT NULL,
    is_internal BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. Create policies table
CREATE TABLE IF NOT EXISTS policies (
    id BIGSERIAL PRIMARY KEY,
    policy_type VARCHAR(50) UNIQUE NOT NULL, -- shipping, cancellation, return, refund, privacy, terms
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT
);

-- 9. Insert default FAQ categories
INSERT INTO faq_categories (name, description, sort_order) VALUES
('Orders & Payments', 'Questions about placing orders, payments, and order management', 1),
('Shipping & Delivery', 'Questions about shipping times, delivery areas, and tracking', 2),
('Products & Quality', 'Questions about product quality, freshness, and storage', 3),
('Returns & Refunds', 'Questions about return policies and refund processes', 4),
('Account & Registration', 'Questions about account creation, login, and profile management', 5)
ON CONFLICT DO NOTHING;

-- 10. Insert default customer care links
INSERT INTO footer_links (name, url, category, sort_order, enabled) VALUES
('WhatsApp Support', 'https://wa.me/919182526xxx', 'CUSTOMER_CARE', 1, true),
('Contact Support', 'support', 'CUSTOMER_CARE', 2, true),
('Track My Order', '/tracking', 'CUSTOMER_CARE', 3, true),
('Raise a Complaint', '/support', 'CUSTOMER_CARE', 4, true),
('Track Support Ticket', '/support/tickets', 'CUSTOMER_CARE', 5, true),
('FAQs / Help Center', '/help', 'CUSTOMER_CARE', 6, true),
('Shipping Policy', '/policies/shipping', 'CUSTOMER_CARE', 7, true),
('Return & Refund Policy', '/policies/return', 'CUSTOMER_CARE', 8, true),
('Contact Us', '/contact', 'CUSTOMER_CARE', 9, true)
ON CONFLICT DO NOTHING;

-- 11. Insert default social media links
INSERT INTO social_media_links (platform, url, icon, sort_order) VALUES
('Instagram', 'https://instagram.com/gonaturefarms', 'instagram', 1),
('Facebook', 'https://facebook.com/gonaturefarms', 'facebook', 2),
('YouTube', 'https://youtube.com/gonaturefarms', 'youtube', 3)
ON CONFLICT DO NOTHING;

-- 12. Insert default support settings
INSERT INTO support_settings (support_phone, whatsapp_number, whatsapp_country_code, support_email, business_hours, working_days) VALUES
('+91 9182526xxx', '919182526xxx', '91', 'support@gonaturefarms.com', '9:00 AM - 6:00 PM', 'Monday - Saturday')
ON CONFLICT DO NOTHING;

-- 13. Insert default policies
INSERT INTO policies (policy_type, title, content) VALUES
('shipping', 'Shipping Policy', 'We deliver fresh organic products to your doorstep within 2-3 business days for Hyderabad and 5-7 business days for other cities.'),
('cancellation', 'Cancellation Policy', 'Orders can be cancelled within 2 hours of placement. After that, cancellation requests will be reviewed on a case-by-case basis.'),
('return', 'Return Policy', 'If you receive damaged or incorrect products, please contact us within 24 hours for a replacement or refund.'),
('refund', 'Refund Policy', 'Refunds are processed within 5-7 business days after approval. Refunds will be credited to the original payment method.'),
('privacy', 'Privacy Policy', 'We respect your privacy and are committed to protecting your personal information. Read our full privacy policy for details.'),
('terms', 'Terms & Conditions', 'By using our website and services, you agree to these terms and conditions. Please read them carefully.')
ON CONFLICT DO NOTHING;
