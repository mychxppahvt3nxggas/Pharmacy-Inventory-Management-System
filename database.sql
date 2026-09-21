-- Create Database
-- (Note: If you already created 'pims_db' via the pgAdmin menu, skip the line below and run the rest)
-- CREATE DATABASE pims_db;

-- Table 1: users (Stores login credentials and roles)
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) CHECK (role IN ('Admin', 'Cashier')) NOT NULL,
    full_name VARCHAR(100) NOT NULL
);

-- Table 2: suppliers (Stores medicine supplier details)
CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT
);

-- Table 3: medicines (Core inventory table)
CREATE TABLE IF NOT EXISTS medicines (
    medicine_id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    company VARCHAR(100),
    medicine_type VARCHAR(50),
    price NUMERIC(10,2) NOT NULL,
    quantity_in_stock INT NOT NULL DEFAULT 0,
    reorder_level INT NOT NULL DEFAULT 10,
    expiry_date DATE NOT NULL,
    supplier_id INT REFERENCES suppliers(supplier_id) ON DELETE SET NULL
);

-- Table 4: sales (Header information for each transaction)
CREATE TABLE IF NOT EXISTS sales (
    sale_id SERIAL PRIMARY KEY,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount NUMERIC(10,2) NOT NULL,
    user_id INT REFERENCES users(user_id) ON DELETE SET NULL
);

-- Table 5: sale_items (Line items for each sale)
CREATE TABLE IF NOT EXISTS sale_items (
    sale_item_id SERIAL PRIMARY KEY,
    sale_id INT NOT NULL REFERENCES sales(sale_id) ON DELETE CASCADE,
    medicine_id INT NOT NULL REFERENCES medicines(medicine_id) ON DELETE CASCADE,
    quantity_sold INT NOT NULL,
    price_at_sale NUMERIC(10,2) NOT NULL
);

-- Insert Default Sample Data
INSERT INTO users (username, password, role, full_name) VALUES
('admin', 'admin123', 'Admin', 'System Administrator'),
('cashier', 'cash123', 'Cashier', 'John Doe');

INSERT INTO suppliers (name, contact_person, phone, email, address) VALUES
('PharmaCorp Ltd', 'Sarah Connor', '011-555-0199', 'sarah@pharmacorp.com', '123 Health Ave, Johannesburg'),
('MediHealth Supplies', 'Mike Ross', '021-555-0144', 'mike@medihealth.co.za', '45 Park Road, Cape Town');

INSERT INTO medicines (name, company, medicine_type, price, quantity_in_stock, reorder_level, expiry_date, supplier_id) VALUES
('Paracetamol 500mg', 'Adcock Ingram', 'Tablet', 25.50, 150, 20, '2026-12-31', 1),
('Amoxicillin 250mg', 'Aspen Pharmacare', 'Capsule', 85.00, 8, 15, '2026-10-15', 1),
('Benylin Cough Syrup', 'Johnson & Johnson', 'Syrup', 65.00, 40, 10, '2026-04-05', 2),
('Hydrocortisone Cream', 'GSK', 'Cream', 45.00, 25, 5, '2027-01-20', 2);