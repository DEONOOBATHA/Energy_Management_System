-- Seed data for user-service
-- This user will be automatically created when the database starts

INSERT INTO person (id, name, address, age) 
VALUES ('11111111-1111-1111-1111-111111111111', 'Demo User', 'Demo Street 123', 25)
ON CONFLICT (id) DO NOTHING;
