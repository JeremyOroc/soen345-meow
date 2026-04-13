-- SQLite schema for the Ticket Reservation System
-- Enable these pragmas at connection time (Hibernate does this via application.properties):
--   PRAGMA foreign_keys = ON;
--   PRAGMA journal_mode = WAL;
--   PRAGMA busy_timeout = 5000;

CREATE TABLE users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  email TEXT UNIQUE,
  phone TEXT UNIQUE,
  password_hash TEXT NOT NULL,
  -- role is CUSTOMER or ADMIN
  role TEXT NOT NULL,
  created_at TEXT NOT NULL
  -- at least one of email or phone must be provided (enforced by application)
);

CREATE TABLE events (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  title TEXT NOT NULL,
  category TEXT NOT NULL,
  location TEXT NOT NULL,
  event_datetime TEXT NOT NULL,
  -- capacity must be >= 1 (enforced by application)
  capacity INTEGER NOT NULL,
  -- available_seats must be between 0 and capacity
  available_seats INTEGER NOT NULL,
  -- status is ACTIVE or CANCELLED
  status TEXT NOT NULL,
  created_by_admin_id INTEGER NOT NULL,
  created_at TEXT NOT NULL,
  updated_at TEXT NOT NULL,
  FOREIGN KEY (created_by_admin_id) REFERENCES users(id)
);

CREATE TABLE reservations (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  user_id INTEGER NOT NULL,
  event_id INTEGER NOT NULL,
  -- ticket_quantity must be >= 1 (enforced by application)
  ticket_quantity INTEGER NOT NULL,
  -- status is CONFIRMED or CANCELLED
  status TEXT NOT NULL,
  reserved_at TEXT NOT NULL,
  cancelled_at TEXT,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE TABLE notification_logs (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  -- recipient is the email address of the person being notified
  recipient TEXT NOT NULL,
  event_title TEXT,
  -- channel is EMAIL or SMS
  channel TEXT NOT NULL,
  -- message_type is BOOKING_CONFIRMATION, RESERVATION_CANCELLED, or EVENT_CANCELLED
  message_type TEXT NOT NULL,
  -- delivery_status is SENT or FAILED
  delivery_status TEXT NOT NULL,
  created_at TEXT NOT NULL
);

-- indexes on events
CREATE INDEX events_event_datetime ON events (event_datetime);
CREATE INDEX events_location ON events (location);
CREATE INDEX events_category ON events (category);
CREATE INDEX events_status ON events (status);
CREATE INDEX events_composite ON events (event_datetime, location, category);

-- indexes on reservations
CREATE INDEX reservations_user_id ON reservations (user_id);
CREATE INDEX reservations_event_id ON reservations (event_id);
CREATE INDEX reservations_status ON reservations (status);

-- indexes on notification_logs
CREATE INDEX notification_logs_recipient ON notification_logs (recipient);
CREATE INDEX notification_logs_created_at ON notification_logs (created_at);

