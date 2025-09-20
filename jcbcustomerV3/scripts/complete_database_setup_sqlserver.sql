-- Complete JCB Management System Database Setup for SQL Server
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
    CustomerID BIGINT NOT NULL,
    MachineID BIGINT NOT NULL,
    StartDate DATETIME NOT NULL,
    EndDate DATETIME NOT NULL,
    Status VARCHAR(50) NOT NULL,     -- e.g., 'Pending', 'Confirmed', 'Cancelled'
    CreatedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (CustomerID) REFERENCES Users(UserID),
    FOREIGN KEY (MachineID) REFERENCES Machines(MachineID)
);

-- Create Invoices table (UC-03)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Invoices' AND xtype='U')
CREATE TABLE Invoices (
    InvoiceID BIGINT PRIMARY KEY IDENTITY(1,1),
    BookingID BIGINT NOT NULL,
    Amount DECIMAL(10,2) NOT NULL,
    Status VARCHAR(50) NOT NULL,     -- e.g., 'Generated', 'Paid', 'Declined'
    CreatedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (BookingID) REFERENCES Bookings(BookingID)
);

-- Create PaymentSlips table (UC-03, for bank slip uploads)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='PaymentSlips' AND xtype='U')
CREATE TABLE PaymentSlips (
    SlipID BIGINT PRIMARY KEY IDENTITY(1,1),
    InvoiceID BIGINT,
    BookingID BIGINT,
    FilePath VARCHAR(255) NOT NULL,  -- Path to uploaded file
    Status VARCHAR(50) NOT NULL,     -- e.g., 'Uploaded', 'Verified', 'Invalid'
    UploadedAt DATETIME DEFAULT GETDATE(),
    VerifiedAt DATETIME,
    VerifiedBy VARCHAR(255),
    Remarks TEXT,
    FOREIGN KEY (InvoiceID) REFERENCES Invoices(InvoiceID),
    FOREIGN KEY (BookingID) REFERENCES Bookings(BookingID)
);

-- Create MaintenanceRecords table (UC-05)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='MaintenanceRecords' AND xtype='U')
CREATE TABLE MaintenanceRecords (
    RecordID BIGINT PRIMARY KEY IDENTITY(1,1),
    MachineID BIGINT NOT NULL,
    MaintenanceDate DATETIME NOT NULL,
    Description TEXT,
    NextDueDate DATETIME,
    CreatedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (MachineID) REFERENCES Machines(MachineID)
);

-- Create Tickets table (UC-06) with enhanced functionality
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='Tickets' AND xtype='U')
CREATE TABLE Tickets (
    TicketID BIGINT PRIMARY KEY IDENTITY(1,1),
    CustomerID BIGINT NOT NULL,
    AssignedStaffID BIGINT,
    Subject VARCHAR(255) NOT NULL,
    Description TEXT NOT NULL,
    Status VARCHAR(50) NOT NULL DEFAULT 'in progress',     -- 'in progress' or 'solved'
    Category VARCHAR(50) DEFAULT 'BOOKING',                -- BOOKING, TECHNICAL, GENERAL
    Flagged BIT DEFAULT 0,                                 -- 0 = normal, 1 = flagged for admin
    CreatedAt DATETIME DEFAULT GETDATE(),
    UpdatedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (CustomerID) REFERENCES Users(UserID),
    FOREIGN KEY (AssignedStaffID) REFERENCES Users(UserID),
    CONSTRAINT CK_Tickets_Status CHECK (Status IN ('in progress', 'solved'))
);

-- Create TicketResponses table (UC-06, for threaded replies)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='TicketResponses' AND xtype='U')
CREATE TABLE TicketResponses (
    ResponseID BIGINT PRIMARY KEY IDENTITY(1,1),
    TicketID BIGINT NOT NULL,
    UserID BIGINT NOT NULL,
    Message TEXT NOT NULL,
    CreatedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (TicketID) REFERENCES Tickets(TicketID),
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

-- Create indexes for better performance
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_tickets_customer_id' AND object_id = OBJECT_ID('Tickets'))
    CREATE INDEX idx_tickets_customer_id ON Tickets(CustomerID);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_tickets_status' AND object_id = OBJECT_ID('Tickets'))
    CREATE INDEX idx_tickets_status ON Tickets(Status);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_tickets_flagged' AND object_id = OBJECT_ID('Tickets'))
    CREATE INDEX idx_tickets_flagged ON Tickets(Flagged);

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'idx_tickets_category' AND object_id = OBJECT_ID('Tickets'))
    CREATE INDEX idx_tickets_category ON Tickets(Category);

-- Insert sample data for testing
IF NOT EXISTS (SELECT * FROM Users WHERE Username = 'admin')
BEGIN
    INSERT INTO Users (Username, Password, Role, Email) VALUES 
    ('admin', 'hashed_password_here', 'Admin', 'admin@jcb.com'),
    ('customer1', 'hashed_password_here', 'Customer', 'customer1@email.com'),
    ('booking_manager', 'hashed_password_here', 'BookingManager', 'manager@jcb.com');
END

-- Sample machines
IF NOT EXISTS (SELECT * FROM Machines WHERE Model = 'JCB 3CX')
BEGIN
    INSERT INTO Machines (Model, Description, Status, Availability) VALUES 
    ('JCB 3CX', 'Backhoe Loader - Versatile construction machine', 'Available', 1),
    ('JCB 8085', 'Midi Excavator - Perfect for medium construction work', 'Available', 1),
    ('JCB 540-200', 'Telehandler - Material handling and lifting', 'Available', 1);
END

PRINT 'Database setup completed successfully!';
