CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    category_id UUID NOT NULL,
    CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

INSERT INTO categories (id, name) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'Electronics'),
('550e8400-e29b-41d4-a716-446655440002', 'Clothing'),
('550e8400-e29b-41d4-a716-446655440003', 'Home & Kitchen'),
('550e8400-e29b-41d4-a716-446655440004', 'Sports & Outdoors'),
('550e8400-e29b-41d4-a716-446655440005', 'Books');

INSERT INTO products (id, name, description, price, category_id) VALUES
('550e8400-e29b-41d4-a716-446655440010', 'Wireless Earbuds', 'Noise-cancelling Bluetooth 5.0 earbuds with 20hr playtime', 79.99, '550e8400-e29b-41d4-a716-446655440001'),
('550e8400-e29b-41d4-a716-446655440011', 'Smartwatch X200', '1.75" AMOLED display with heart rate monitoring', 149.95, '550e8400-e29b-41d4-a716-446655440001'),
('550e8400-e29b-41d4-a716-446655440020', 'Men''s Casual Shirt', '100% Cotton slim-fit shirt with breathable fabric', 29.99, '550e8400-e29b-41d4-a716-446655440002'),
('550e8400-e29b-41d4-a716-446655440021', 'Women''s Yoga Pants', 'High-waisted stretchable leggings with moisture-wicking', 39.95, '550e8400-e29b-41d4-a716-446655440002'),
('550e8400-e29b-41d4-a716-446655440030', 'Air Fryer Pro', '5.5L capacity with 8 preset cooking programs', 89.99, '550e8400-e29b-41d4-a716-446655440003'),
('550e8400-e29b-41d4-a716-446655440031', 'Robot Vacuum Cleaner', 'LiDAR navigation with 2000Pa suction power', 299.00, '550e8400-e29b-41d4-a716-446655440003'),
('550e8400-e29b-41d4-a716-446655440040', 'Camping Tent 4P', 'Waterproof 4-person tent with UV protection', 129.95, '550e8400-e29b-41d4-a716-446655440004'),
('550e8400-e29b-41d4-a716-446655440041', 'Yoga Mat Premium', 'Eco-friendly TPE material with alignment markers', 24.99, '550e8400-e29b-41d4-a716-446655440004'),
('550e8400-e29b-41d4-a716-446655440050', 'The Great Java Book', 'Bestselling Java book by award-winning author', 14.99, '550e8400-e29b-41d4-a716-446655440005'),
('550e8400-e29b-41d4-a716-446655440051', 'Learn Java Programming', 'Comprehensive guide to modern Java development', 49.99, '550e8400-e29b-41d4-a716-446655440005');

