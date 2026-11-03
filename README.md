# Pharmacy Medicine Stock and Billing System

A desktop-based Pharmacy Medicine Stock and Billing Management System developed using **Java Swing, JDBC, and MySQL**.

The application helps manage medicine inventory, monitor stock levels, search medicines, and generate customer bills through a graphical user interface.

## Project Overview

The Pharmacy Medicine Stock and Billing System is designed to simplify everyday pharmacy operations. It provides an interface for managing medicine records and processing sales while keeping inventory information stored in a MySQL database.

## Features

### Medicine Inventory Management
- Add new medicines.
- View all available medicines.
- Update medicine information.
- Delete medicine records.
- Search medicines by name.
- Identify low-stock medicines.

### Billing Management
- Select medicines for billing.
- Add and remove items from the cart.
- Enter medicine quantities.
- Calculate item totals and the bill amount.
- Complete a bill and save sales information.
- Automatically update medicine stock after a successful sale.

## Technologies Used

| Technology | Purpose |
|---|---|
| Java | Application programming |
| Java Swing | Desktop graphical user interface |
| JDBC | Java–database connectivity |
| MySQL 8.0 | Database management |
| MySQL Connector/J | JDBC driver |
| VS Code | Development environment |

## Database

Database name: `pharmacy_db`

The project uses four main tables:

- `medicines` – stores medicine details and stock.
- `customers` – stores customer information.
- `sales` – stores bill and sales information.
- `sale_items` – stores individual medicines included in each bill.

## Project Structure

```text
PharmacyManagementSystem/
│
├── lib/
│   └── mysql-connector-j-26.7.0.jar
│
├── src/
│   ├── Main.java
│   ├── DBConnection.java
│   ├── TestConnection.java
│   ├── Medicine.java
│   ├── MedicineDAO.java
│   ├── BillingDAO.java
│   ├── BillingPanel.java
│   └── BillingTest.java
│
├── .gitignore
└── README.md
```

## Requirements

- Java JDK 25 or compatible version
- MySQL Server 8.0 or compatible version
- MySQL Connector/J
- Visual Studio Code or another Java IDE

## Database Configuration

1. Install and start MySQL Server.
2. Open MySQL Workbench.
3. Create the database:

```sql
CREATE DATABASE pharmacy_db;
```

4. Create the required tables. See the database setup script in the project repository when available.
5. Configure the database password as an environment variable.

For Windows PowerShell:

```powershell
$env:PHARMACY_DB_PASSWORD = Read-Host "Enter your MySQL root password"
```

The application reads the password through the `PHARMACY_DB_PASSWORD` environment variable. Do not put your database password directly in the source code.

## How to Run

Open the project folder in VS Code and use the terminal from the project root.

### 1. Compile the Inventory Application

```powershell
javac -cp "lib\mysql-connector-j-26.7.0.jar;src" -d out src\DBConnection.java src\Medicine.java src\MedicineDAO.java src\BillingDAO.java src\Main.java
```

### 2. Run the Inventory Application

```powershell
java -cp "out;lib\mysql-connector-j-26.7.0.jar" Main
```

### 3. Compile the Billing Application

```powershell
javac -cp "lib\mysql-connector-j-26.7.0.jar;src" -d out src\DBConnection.java src\Medicine.java src\MedicineDAO.java src\BillingDAO.java src\BillingPanel.java src\BillingTest.java
```

### 4. Run the Billing Application

```powershell
java -cp "out;lib\mysql-connector-j-26.7.0.jar" BillingTest
```

**Note:** Set the `PHARMACY_DB_PASSWORD` environment variable in the same PowerShell terminal before running either application.

## Concepts Demonstrated

- Object-Oriented Programming
- Classes and Objects
- Encapsulation
- Inheritance
- Method Overriding
- Exception Handling
- Java Collections
- Lambda Expressions
- Swing GUI and Event Handling
- JDBC Database Connectivity
- SQL CRUD Operations
- Database Transactions

## Project Purpose

This project was developed as a Java Programming Micro Project to demonstrate the practical application of Java programming, GUI development, database connectivity, and inventory and billing management.

## Author

**Thanish Raju**

