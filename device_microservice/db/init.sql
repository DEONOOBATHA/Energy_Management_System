-- Seed data for device-service
-- These devices will be automatically created when the database starts

INSERT INTO device (id, name, max_cons_value, user_id) 
VALUES 
    ('22222222-2222-2222-2222-222222222222', 'Demo Device 1', 500.0, '11111111-1111-1111-1111-111111111111'),
    ('33333333-3333-3333-3333-333333333333', 'Demo Device 2', 1000.0, '11111111-1111-1111-1111-111111111111')
ON CONFLICT (id) DO NOTHING;
