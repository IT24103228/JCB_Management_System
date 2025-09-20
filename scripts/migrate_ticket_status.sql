-- Migration script to update existing ticket status values
-- Update existing OPEN tickets to IN_PROGRESS
UPDATE tickets 
SET status = 'IN_PROGRESS' 
WHERE status = 'OPEN';

-- Update existing CLOSED tickets to SOLVED
UPDATE tickets 
SET status = 'SOLVED' 
WHERE status = 'CLOSED';

-- Update any other status values to IN_PROGRESS as default
UPDATE tickets 
SET status = 'IN_PROGRESS' 
WHERE status NOT IN ('IN_PROGRESS', 'SOLVED');
