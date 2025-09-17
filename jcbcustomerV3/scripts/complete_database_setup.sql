-- Complete JCB Management System Database Setup
-- This script creates all tables and adds the required enhancements

-- Create Users table (UC-01)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Users' AND xtype='U')
CREATE TABLE Users (
    UserID BIGINT PRIMARY KEY IDENTITY(1,1),
    Username VARCHAR(50) NOT NULL UNIQUE,
    Password VARCHAR(255) NOT NULL,  -- Hashed password
    Role VARCHAR(50) NOT NULL,       -- e.g., 'Customer', 'Admin', 'FinanceOfficer', 'SupportStaff', etc.
    Email VARCHAR(100),
    CreatedAt DATETIME DEFAULT GETDATE()
);

-- Create Machines table (UC-04)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Machines' AND xtype='U')
CREATE TABLE Machines (
    MachineID BIGINT PRIMARY KEY IDENTITY(1,1),
    Model VARCHAR(100) NOT NULL,
    Description TEXT,
    Status VARCHAR(50) NOT NULL,     -- e.g., 'Available', 'InUse', 'Maintenance'
    Availability BIT DEFAULT 1,      -- 1 = Available, 0 = Not
    Archived BIT DEFAULT 0           -- For retired machines
);

-- Create Bookings table (UC-02)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Bookings' AND xtype='U')
CREATE TABLE Bookings (
    BookingID BIGINT PRIMARY KEY IDENTITY(1,1),
    CustomerID BIGINT NOT NULL FOREIGN KEY REFERENCES Users(UserID),
    MachineID BIGINT NOT NULL FOREIGN KEY REFERENCES Machines(MachineID),
    StartDate DATETIME NOT NULL,
    EndDate DATETIME NOT NULL,
    Status VARCHAR(50) NOT NULL,     -- e.g., 'Pending', 'Confirmed', 'Cancelled'
    CreatedAt DATETIME DEFAULT GETDATE()
);

-- Create Invoices table (UC-03)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Invoices' AND xtype='U')
CREATE TABLE Invoices (
    InvoiceID BIGINT PRIMARY KEY IDENTITY(1,1),
    BookingID BIGINT NOT NULL FOREIGN KEY REFERENCES Bookings(BookingID),
    Amount DECIMAL(10,2) NOT NULL,
    Status VARCHAR(50) NOT NULL,     -- e.g., 'Generated', 'Paid', 'Declined'
    CreatedAt DATETIME DEFAULT GETDATE()
);

-- Create PaymentSlips table (UC-03, for bank slip uploads)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='PaymentSlips' AND xtype='U')
CREATE TABLE PaymentSlips (
    SlipID BIGINT PRIMARY KEY IDENTITY(1,1),
    InvoiceID BIGINT NOT NULL FOREIGN KEY REFERENCES Invoices(InvoiceID),
    FilePath VARCHAR(255) NOT NULL,  -- Path to uploaded file (e.g., '/uploads/slip123.pdf')
    Status VARCHAR(50) NOT NULL,     -- e.g., 'Uploaded', 'Verified', 'Invalid'
    UploadedAt DATETIME DEFAULT GETDATE()
);

-- Create MaintenanceRecords table (UC-05)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='MaintenanceRecords' AND xtype='U')
CREATE TABLE MaintenanceRecords (
    RecordID BIGINT PRIMARY KEY IDENTITY(1,1),
    MachineID BIGINT NOT NULL FOREIGN KEY REFERENCES Machines(MachineID),
    MaintenanceDate DATETIME NOT NULL,
    Description TEXT,
    NextDueDate DATETIME,
    CreatedAt DATETIME DEFAULT GETDATE()
);

-- Create Tickets table (UC-06)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Tickets' AND xtype='U')
CREATE TABLE Tickets (
    TicketID BIGINT PRIMARY KEY IDENTITY(1,1),
    CustomerID BIGINT NOT NULL FOREIGN KEY REFERENCES Users(UserID),
    AssignedStaffID BIGINT FOREIGN KEY REFERENCES Users(UserID),
    Subject VARCHAR(255) NOT NULL,
    Description TEXT NOT NULL,
    Status VARCHAR(50) NOT NULL,     -- e.g., 'Open', 'InProgress', 'Resolved'
    CreatedAt DATETIME DEFAULT GETDATE(),
    UpdatedAt DATETIME DEFAULT GETDATE()
);

-- Create TicketResponses table (UC-06, for threaded replies)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='TicketResponses' AND xtype='U')
CREATE TABLE TicketResponses (
    ResponseID BIGINT PRIMARY KEY IDENTITY(1,1),
    TicketID BIGINT NOT NULL FOREIGN KEY REFERENCES Tickets(TicketID),
    UserID BIGINT NOT NULL FOREIGN KEY REFERENCES Users(UserID),
    Message TEXT NOT NULL,
    CreatedAt DATETIME DEFAULT GETDATE()
);

-- Now add the enhancements for the new functionality

-- Add category and flagged columns to Tickets table if they don't exist
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'Tickets' AND COLUMN_NAME = 'category')
    ALTER TABLE Tickets ADD category VARCHAR(50) DEFAULT 'BOOKING';

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'Tickets' AND COLUMN_NAME = 'flagged')
    ALTER TABLE Tickets ADD flagged BIT DEFAULT 0;

-- Add booking-based payment support to PaymentSlips table
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'PaymentSlips' AND COLUMN_NAME = 'booking_id')
    ALTER TABLE PaymentSlips ADD booking_id BIGINT;

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'PaymentSlips' AND COLUMN_NAME = 'uploaded_at')
    ALTER TABLE PaymentSlips ADD uploaded_at DATETIME DEFAULT GETDATE();

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'PaymentSlips' AND COLUMN_NAME = 'verified_at')
    ALTER TABLE PaymentSlips ADD verified_at DATETIME;

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'PaymentSlips' AND COLUMN_NAME = 'verified_by')
    ALTER TABLE PaymentSlips ADD verified_by VARCHAR(255);

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'PaymentSlips' AND COLUMN_NAME = 'remarks')
    ALTER TABLE PaymentSlips ADD remarks TEXT;

-- Create indexes for better performance
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_payment_slips_booking_id')
    CREATE INDEX idx_payment_slips_booking_id ON PaymentSlips(booking_id);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_tickets_flagged')
    CREATE INDEX idx_tickets_flagged ON Tickets(flagged);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_tickets_category')
    CREATE INDEX idx_tickets_category ON Tickets(category);

-- Update existing tickets to have default category
UPDATE Tickets SET category = 'BOOKING' WHERE category IS NULL;

-- Insert sample data for testing (optional)
-- Sample users
IF NOT EXISTS (SELECT * FROM Users WHERE Username = 'admin')
INSERT INTO Users (Username, Password, Role, Email) VALUES 
('admin', 'hashed_password_here', 'Admin', 'admin@jcb.com'),
('customer1', 'hashed_password_here', 'Customer', 'customer1@email.com'),
('booking_manager', 'hashed_password_here', 'BookingManager', 'manager@jcb.com');

-- Sample machines
IF NOT EXISTS (SELECT * FROM Machines WHERE Model = 'JCB 3CX')
INSERT INTO Machines (Model, Description, Status, Availability) VALUES 
('JCB 3CX', 'Backhoe Loader - Versatile construction machine', 'Available', 1),
('JCB 8085', 'Midi Excavator - Perfect for medium construction work', 'Available', 1),
('JCB 540-200', 'Telehandler - Material handling and lifting', 'Available', 1);

PRINT 'Database setup completed successfully!';
