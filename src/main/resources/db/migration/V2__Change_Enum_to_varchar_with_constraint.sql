ALTER TABLE accounts
ALTER COLUMN status SET DATA TYPE VARCHAR(20), -- s pozorom
ADD CONSTRAINT validate_status CHECK (status IN ('ACTIVE', 'CLOSED'));