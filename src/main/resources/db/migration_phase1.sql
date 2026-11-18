-- Phase 1: Footer Extensions Migration
-- Adds social media links and support settings tables

-- 1. Extend footer_links table with new fields
ALTER TABLE footer_links ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT true;
ALTER TABLE footer_links ADD COLUMN IF NOT EXISTS open_in_new_tab BOOLEAN DEFAULT false;
ALTER TABLE footer_links ADD COLUMN IF NOT EXISTS column_heading VARCHAR(100);

-- 2. Create social_media_links table
CREATE TABLE IF NOT EXISTS social_media_links (
    id BIGSERIAL PRIMARY KEY,
    platform VARCHAR(50) NOT NULL,
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

-- 4. Insert default social media links
INSERT INTO social_media_links (platform, url, icon, sort_order) VALUES
('Instagram', 'https://instagram.com/gonaturefarms', 'instagram', 1),
('Facebook', 'https://facebook.com/gonaturefarms', 'facebook', 2),
('YouTube', 'https://youtube.com/gonaturefarms', 'youtube', 3)
ON CONFLICT DO NOTHING;

-- 5. Insert default support settings
INSERT INTO support_settings (support_phone, whatsapp_number, whatsapp_country_code, support_email, business_hours, working_days) VALUES
('+91 9182526xxx', '919182526xxx', '91', 'support@gonaturefarms.com', '9:00 AM - 6:00 PM', 'Monday - Saturday')
ON CONFLICT DO NOTHING;
