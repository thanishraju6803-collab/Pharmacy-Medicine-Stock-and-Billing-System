-- Pharmacy Medicine Stock and Billing System
-- Database setup script

CREATE DATABASE IF NOT EXISTS pharmacy_db;

USE pharmacy_db;

-- 1. Medicines table
CREATE TABLE IF NOT EXISTS medicines (
    medicine_id INT PRIMARY KEY AUTO_INCREMENT,
    medicine_name VARCHAR(100) NOT NULL,
    batch_no VARCHAR(50) NOT NULL,
    category VARCHAR(50),
    unit_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    expiry_date DATE NOT NULL,
    reorder_level INT NOT NULL DEFAULT 10
);

-- 2. Customers table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20)
);

-- 3. Sales table
CREATE TABLE IF NOT EXISTS sales (
    sale_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NULL,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    grand_total DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_sales_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers(customer_id)
        ON DELETE SET NULL
);

-- 4. Sale items table
CREATE TABLE IF NOT EXISTS sale_items (
    sale_item_id INT PRIMARY KEY AUTO_INCREMENT,
    sale_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(10,2) NOT NULL,

    CONSTRAINT fk_saleitems_sale
        FOREIGN KEY (sale_id)
        REFERENCES sales(sale_id),

    CONSTRAINT fk_saleitems_medicine
        FOREIGN KEY (medicine_id)
        REFERENCES medicines(medicine_id)
);

-- Verify the tables
SHOW TABLES;