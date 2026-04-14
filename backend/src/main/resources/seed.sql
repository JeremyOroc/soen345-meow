-- Seed default users for local development/demo.
-- Password for all seeded users is: password
INSERT OR IGNORE INTO users (id, email, phone, password_hash, role, created_at)
VALUES
  (1, 'admin@meow.com', NULL, '$2a$10$EWxVjASXOshLl5wPJ9W/puUvZ.qBs.McuJM.D9lqjydtffoNuYyvG', 'ADMIN', 1767276000000),
  (2, 'alice@meow.com', '5145551001', '$2a$10$EWxVjASXOshLl5wPJ9W/puUvZ.qBs.McuJM.D9lqjydtffoNuYyvG', 'CUSTOMER', 1767367800000),
  (3, 'bob@meow.com', '5145551002', '$2a$10$EWxVjASXOshLl5wPJ9W/puUvZ.qBs.McuJM.D9lqjydtffoNuYyvG', 'CUSTOMER', 1767456900000),
  (4, 'charlie@meow.com', '5145551003', '$2a$10$EWxVjASXOshLl5wPJ9W/puUvZ.qBs.McuJM.D9lqjydtffoNuYyvG', 'CUSTOMER', 1767555900000);

-- Seed default events.
INSERT OR IGNORE INTO events (id, title, category, location, event_datetime, capacity, available_seats, status)
VALUES
  (1, 'Montreal Tech Meetup', 'Tech', 'Montreal', '2026-05-10T18:30:00', 120, 120, 'ACTIVE'),
  (2, 'Spring Boot Workshop', 'Wellness', 'Concordia University', '2026-05-15T14:00:00', 80, 80, 'ACTIVE'),
  (3, 'Indie Music Night', 'Concerts', 'Plateau Mont-Royal', '2026-05-22T20:00:00', 200, 200, 'ACTIVE'),
  (4, 'Startup Pitch Evening', 'Other', 'Downtown Montreal', '2026-06-01T19:00:00', 150, 150, 'ACTIVE'),
  (5, 'Community Cat Cafe Event', 'Food & Drink', 'Mile End', '2026-06-08T11:00:00', 60, 60, 'ACTIVE'),
  (6, 'Winter Art Expo', 'Theatre', 'Old Port', '2026-01-18T13:00:00', 90, 24, 'COMPLETED'),
  (7, 'Open Source Hack Night', 'Festivals', 'Montreal', '2025-11-12T18:00:00', 110, 0, 'COMPLETED'),
  (8, 'Jazz in the Park', 'Movies', 'Parc La Fontaine', '2025-08-03T19:30:00', 300, 0, 'COMPLETED'),
  (9, 'AI Product Meetup', 'Travel', 'Downtown Montreal', '2026-07-19T18:45:00', 140, 140, 'ACTIVE'),
  (10, 'Sunday Farmers Market', 'Sports', 'Jean-Talon Market', '2026-09-07T10:00:00', 500, 500, 'ACTIVE');

-- Seed default reservations linked to seeded users/events.
INSERT OR IGNORE INTO reservations (id, user_id, event_id, ticket_quantity, status, reserved_at, cancelled_at)
VALUES
  (1, 2, 1, 2, 'CONFIRMED', 1775048400000, NULL),
  (2, 3, 2, 1, 'CONFIRMED', 1775244000000, NULL),
  (3, 4, 6, 3, 'COMPLETED', 1768064400000, NULL),
  (4, 2, 8, 2, 'CANCELLED', 1753014600000, 1753452000000),
  (5, 3, 4, 4, 'CONFIRMED', 1775940000000, NULL);