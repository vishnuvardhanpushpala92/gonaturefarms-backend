package com.gonaturefarms.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * One-time-safe migration: the production orders table has CHECK constraints
 * (orders_payment_status_check / orders_status_check) created before the
 * VerificationPending / PaymentVerificationPending statuses existed.
 * ddl-auto=update does not manage CHECK constraints, so we repair them here
 * on every startup (idempotent — safe to re-run).
 */
@Component
public class DbConstraintMigration implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DbConstraintMigration.class);

    private final JdbcTemplate jdbcTemplate;

    public DbConstraintMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Prod runs ddl-auto=validate, so entity columns are never auto-added.
        // Add return_eligible to products (idempotent). Default TRUE preserves
        // the pre-existing behavior where returns were allowed for all products.
        try {
            jdbcTemplate.execute("ALTER TABLE products ADD COLUMN IF NOT EXISTS return_eligible BOOLEAN NOT NULL DEFAULT TRUE");
            log.info("products.return_eligible column ensured");
        } catch (Exception e) {
            log.error("Failed to add products.return_eligible: {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_payment_status_check");
            jdbcTemplate.execute("ALTER TABLE orders ADD CONSTRAINT orders_payment_status_check " +
                    "CHECK (payment_status IN ('Pending','VerificationPending','Paid','Failed','Refunded','Cancelled'))");
            log.info("orders_payment_status_check constraint updated");
        } catch (Exception e) {
            log.error("Failed to update orders_payment_status_check: {}", e.getMessage());
        }

        try {
            jdbcTemplate.execute("ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check");
            jdbcTemplate.execute("ALTER TABLE orders ADD CONSTRAINT orders_status_check " +
                    "CHECK (status IN ('Pending','Placed','Confirmed','Processing','Packed','Shipped','OutForDelivery','Delivered','Cancelled','PaymentVerificationPending'))");
            log.info("orders_status_check constraint updated");
        } catch (Exception e) {
            log.error("Failed to update orders_status_check: {}", e.getMessage());
        }
    }
}
