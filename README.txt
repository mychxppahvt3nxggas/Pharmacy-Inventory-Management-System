================================================================================
               PHARMACY INVENTORY MANAGEMENT SYSTEM (PIMS)
                              USER MANUAL & README
================================================================================

1. TECH STACK & PREREQUISITES
--------------------------------------------------------------------------------
* Programming Language: Java (JDK 8 or higher)
* GUI Framework: Java Swing (JFrame, JTabbedPane, JTable)
* Database: PostgreSQL
* Database Driver: PostgreSQL JDBC Driver (postgresql-42.x.x.jar)


2. DATABASE SCHEMA SETUP
--------------------------------------------------------------------------------
Execute the following DDL and initial sample data script in pgAdmin or psql:

-- Create Database
CREATE DATABASE pims_db;

-- 1. Users Table
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL -- 'Admin' or 'Cashier'
);

-- 2. Medicines / Inventory Table
CREATE TABLE medicines (
    medicine_id SERIAL PRIMARY KEY,
    medicine_name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    price NUMERIC(10, 2) NOT NULL,
    quantity_in_stock INT NOT NULL,
    expiry_date DATE NOT NULL
);

-- 3. Sales Header Table
CREATE TABLE sales (
    sale_id SERIAL PRIMARY KEY,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount NUMERIC(10, 2) NOT NULL,
    user_id INT REFERENCES users(user_id)
);

-- Insert Default Administrative & Cashier Users
INSERT INTO users (username, password, role) VALUES 
('admin', 'admin123', 'Admin'),
('cashier', 'cashier123', 'Cashier');

-- Insert Initial Inventory Data
INSERT INTO medicines (medicine_name, category, price, quantity_in_stock, expiry_date) VALUES 
('Paracetamol 500mg', 'Analgesic', 5.50, 3, '2026-10-15'),
('Amoxicillin 250mg', 'Antibiotic', 12.00, 5, '2026-11-20'),
('Ibuprofen 400mg', 'NSAID', 8.25, 25, '2027-01-10');

-- Insert Sample Sales Data
INSERT INTO sales (sale_date, total_amount, user_id) VALUES 
(CURRENT_TIMESTAMP, 150.50, 1),
(CURRENT_TIMESTAMP - INTERVAL '1 day', 85.00, 1),
(CURRENT_TIMESTAMP - INTERVAL '2 days', 210.25, 2);


3. STEP-BY-STEP SETUP INSTRUCTIONS
--------------------------------------------------------------------------------
1. Clone or Extract Project:
   Download and extract the project source code to your local machine.

2. Setup PostgreSQL Database:
   - Open pgAdmin or your preferred PostgreSQL client.
   - Execute the SQL commands in Section 2 above to create tables and insert data.

3. Configure Database Connection:
   - Open DatabaseConnection.java in your IDE.
   - Update URL, USER, and PASSWORD variables to match your local PostgreSQL setup.

4. Add JDBC Driver:
   - Ensure the postgresql-42.x.x.jar driver is included in your project build path.

5. Run the Application:
   - Locate and execute LoginFrame.java.
   - Use default credentials:
     * Admin:   username: admin   | password: admin123
     * Cashier: username: cashier | password: cashier123


4. REQUIRED SCREENSHOTS CHECKLIST
--------------------------------------------------------------------------------
Save all captured screenshots in the /screenshots directory of the project:

[01] Filename:    01_login_screen.png
     Target View: LoginFrame
     Description: Authentication window showing login text fields for Admin/Cashier.

[02] Filename:    02_cashier_pos.png
     Target View: Cashier Dashboard
     Description: Point of Sale view with medicine lookup, stock check, and cart.

[03] Filename:    03_receipt_popup.png
     Target View: Cashier Checkout
     Description: Transaction receipt confirmation dialog after successful sale.

[04] Filename:    04_inventory_mgmt.png
     Target View: Admin Panel
     Description: Table displaying medicine inventory with CRUD operations.

[05] Filename:    05_admin_dashboard.png
     Target View: AdminDashboard
     Description: Main administrative panel view.

[06] Filename:    06_report_sales.png
     Target View: Admin Reports - Tab 1
     Description: Sales report listing sale IDs, timestamps, amounts, and user IDs.

[07] Filename:    07_report_item_wise.png
     Target View: Admin Reports - Tab 2
     Description: Revenue summary report aggregating sales per user account.

[08] Filename:    08_report_low_stock.png
     Target View: Admin Reports - Tab 3
     Description: Low stock alert report listing items with quantity_in_stock <= 10.

[09] Filename:    09_report_expiry.png
     Target View: Admin Reports - Tab 4
     Description: Expiry report listing items sorted by upcoming expiry_date.

================================================================================