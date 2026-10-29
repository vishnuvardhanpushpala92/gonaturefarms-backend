-- Create homepage_layout table for storing visual configuration
CREATE TABLE IF NOT EXISTS homepage_layout (
    id BIGSERIAL PRIMARY KEY,
    hero_height_desktop INTEGER DEFAULT 450,
    hero_height_tablet INTEGER DEFAULT 320,
    hero_height_mobile INTEGER DEFAULT 250,
    hero_image_fit VARCHAR(20) DEFAULT 'contain',
    hero_image_position VARCHAR(20) DEFAULT 'center',
    hero_aspect_ratio VARCHAR(20) DEFAULT '16/9',
    hero_background_color VARCHAR(20) DEFAULT '#f5f5f0',
    product_card_style VARCHAR(20) DEFAULT 'standard',
    product_image_aspect_ratio VARCHAR(20) DEFAULT '1/1',
    product_image_fit VARCHAR(20) DEFAULT 'cover',
    products_per_row_desktop INTEGER DEFAULT 4,
    products_per_row_tablet INTEGER DEFAULT 3,
    products_per_row_mobile INTEGER DEFAULT 2,
    product_card_spacing INTEGER DEFAULT 16,
    section_spacing INTEGER DEFAULT 48,
    section_width VARCHAR(20) DEFAULT 'full',
    show_hero BOOLEAN DEFAULT true,
    show_products BOOLEAN DEFAULT true,
    show_testimonials BOOLEAN DEFAULT true,
    show_videos BOOLEAN DEFAULT true,
    show_features BOOLEAN DEFAULT true,
    product_section_order INTEGER DEFAULT 1,
    is_published BOOLEAN DEFAULT false,
    is_draft BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100),
    updated_by VARCHAR(100)
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_homepage_layout_published ON homepage_layout(is_published, updated_at DESC);
CREATE INDEX IF NOT EXISTS idx_homepage_layout_draft ON homepage_layout(is_draft, updated_at DESC);
