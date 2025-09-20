-- Update Tickets table to add category and flagged columns (SQL Server syntax)
ALTER TABLE Tickets ADD category VARCHAR(50) DEFAULT 'BOOKING';
ALTER TABLE Tickets ADD flagged BIT DEFAULT 0;

-- Update PaymentSlips table to support booking-based payments
ALTER TABLE PaymentSlips ADD booking_id BIGINT;
ALTER TABLE PaymentSlips ADD uploaded_at DATETIME DEFAULT GETDATE();
ALTER TABLE PaymentSlips ADD verified_at DATETIME;
ALTER TABLE PaymentSlips ADD verified_by VARCHAR(255);
ALTER TABLE PaymentSlips ADD remarks TEXT;

-- Create index for better performance on booking-based queries
CREATE INDEX idx_payment_slips_booking_id ON PaymentSlips(booking_id);
CREATE INDEX idx_tickets_flagged ON Tickets(flagged);
CREATE INDEX idx_tickets_category ON Tickets(category);

-- Update existing tickets to have default category
UPDATE Tickets SET category = 'BOOKING' WHERE category IS NULL;

-- Add foreign key constraint for booking_id in PaymentSlips (optional)
-- ALTER TABLE PaymentSlips ADD CONSTRAINT fk_payment_slip_booking 
-- FOREIGN KEY (booking_id) REFERENCES Bookings(BookingID);
