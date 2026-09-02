-- Customer_db (customer-service) - 10 customers + home addresses
BEGIN;

INSERT INTO customers (id, active, anniversary, created_at, customer_code, customer_status, date_of_birth, email, first_name, gender, last_name, mobile_number, updated_at) VALUES
(1,  true, NULL, now() - interval '200 days', 'CUS-2026-000001', 'ACTIVE', '1990-04-12', 'rahul.sharma@example.com', 'Rahul',   'MALE',   'Sharma', '9876510001', now() - interval '1 day'),
(2,  true, NULL, now() - interval '200 days', 'CUS-2026-000002', 'ACTIVE', '1992-08-25', 'priya.patel@example.com', 'Priya',   'FEMALE', 'Patel',  '9876510002', now() - interval '1 day'),
(3,  true, NULL, now() - interval '200 days', 'CUS-2026-000003', 'ACTIVE', '1988-01-05', 'amit.verma@example.com', 'Amit',    'MALE',   'Verma',  '9876510003', now() - interval '1 day'),
(4,  true, NULL, now() - interval '200 days', 'CUS-2026-000004', 'ACTIVE', '1995-11-19', 'sneha.reddy@example.com', 'Sneha',   'FEMALE', 'Reddy',  '9876510004', now() - interval '1 day'),
(5,  true, NULL, now() - interval '200 days', 'CUS-2026-000005', 'ACTIVE', '1985-06-30', 'vikram.singh@example.com', 'Vikram',  'MALE',   'Singh',  '9876510005', now() - interval '1 day'),
(6,  true, NULL, now() - interval '200 days', 'CUS-2026-000006', 'ACTIVE', '1993-03-15', 'kavita.iyer@example.com', 'Kavita',  'FEMALE', 'Iyer',   '9876510006', now() - interval '1 day'),
(7,  true, NULL, now() - interval '200 days', 'CUS-2026-000007', 'ACTIVE', '1991-09-08', 'rohan.mehta@example.com', 'Rohan',   'MALE',   'Mehta',  '9876510007', now() - interval '1 day'),
(8,  true, NULL, now() - interval '200 days', 'CUS-2026-000008', 'ACTIVE', '1996-12-02', 'ananya.das@example.com', 'Ananya',  'FEMALE', 'Das',    '9876510008', now() - interval '1 day'),
(9,  true, NULL, now() - interval '200 days', 'CUS-2026-000009', 'ACTIVE', '1989-07-21', 'arjun.nair@example.com', 'Arjun',   'MALE',   'Nair',   '9876510009', now() - interval '1 day'),
(10, true, NULL, now() - interval '200 days', 'CUS-2026-000010', 'ACTIVE', '1994-05-17', 'meera.joshi@example.com', 'Meera',   'FEMALE', 'Joshi',  '9876510010', now() - interval '1 day');

INSERT INTO customer_addresses (id, active, address_line1, address_line2, address_type, city, country, created_at, default_address, postal_code, state, updated_at, customer_id) VALUES
(1,  true, '12, Rose Villa, Andheri West', 'Near Link Road',  'HOME', 'Mumbai',    'India', now() - interval '200 days', true, '400053', 'Maharashtra',    now() - interval '1 day', 1),
(2,  true, '45, Green Park Extension', NULL,                'HOME', 'New Delhi', 'India', now() - interval '200 days', true, '110016', 'Delhi',          now() - interval '1 day', 2),
(3,  true, '78, Whitefield Main Road', 'Kundalahalli Gate', 'HOME', 'Bengaluru', 'India', now() - interval '200 days', true, '560066', 'Karnataka',      now() - interval '1 day', 3),
(4,  true, '23, Jubilee Hills', NULL,                       'HOME', 'Hyderabad', 'India', now() - interval '200 days', true, '500033', 'Telangana',      now() - interval '1 day', 4),
(5,  true, '101, Boat Club Road', NULL,                     'HOME', 'Pune',      'India', now() - interval '200 days', true, '411001', 'Maharashtra',    now() - interval '1 day', 5),
(6,  true, '67, T Nagar', NULL,                             'HOME', 'Chennai',   'India', now() - interval '200 days', true, '600017', 'Tamil Nadu',     now() - interval '1 day', 6),
(7,  true, '9, Salt Lake Sector V', NULL,                   'HOME', 'Kolkata',   'India', now() - interval '200 days', true, '700091', 'West Bengal',    now() - interval '1 day', 7),
(8,  true, '34, Boring Road', NULL,                         'HOME', 'Patna',     'India', now() - interval '200 days', true, '800001', 'Bihar',          now() - interval '1 day', 8),
(9,  true, '18, MG Road', NULL,                             'HOME', 'Kochi',     'India', now() - interval '200 days', true, '682016', 'Kerala',         now() - interval '1 day', 9),
(10, true, '56, C-Scheme', NULL,                            'HOME', 'Jaipur',    'India', now() - interval '200 days', true, '302001', 'Rajasthan',      now() - interval '1 day', 10);

COMMIT;
