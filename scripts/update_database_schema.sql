-- Update Tickets table to add category and flagged columns
ALTER TABLE Tickets ADD COLUMN category VARCHAR(50) DEFAULT 'BOOKING';
ALTER TABLE Tickets ADD COLUMN flagged BOOLEAN DEFAULT FALSE;

-- Update PaymentSlips table to support booking-based payments
ALTER TABLE PaymentSlips ADD COLUMN booking_id BIGINT;
ALTER TABLE PaymentSlips ADD COLUMN uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE PaymentSlips ADD COLUMN verified_at TIMESTAMP;
ALTER TABLE PaymentSlips ADD COLUMN verified_by VARCHAR(255);
ALTER TABLE PaymentSlips ADD COLUMN remarks TEXT;

-- Create index for better performance on booking-based queries
CREATE INDEX idx_payment_slips_booking_id ON PaymentSlips(booking_id);
CREATE INDEX idx_tickets_flagged ON Tickets(flagged);
CREATE INDEX idx_tickets_category ON Tickets(category);

-- Update existing tickets to have default category
UPDATE Tickets SET category = 'BOOKING' WHERE category IS NULL;

-- Add foreign key constraint for booking_id in PaymentSlips (optional)
-- ALTER TABLE PaymentSlips ADD CONSTRAINT fk_payment_slip_booking 
-- FOREIGN KEY (booking_id) REFERENCES Bookings(BookingID);
