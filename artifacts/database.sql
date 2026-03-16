CREATE TABLE [users] (
  [id] integer PRIMARY KEY IDENTITY(1, 1),
  [email] text UNIQUE,
  [phone] text UNIQUE,
  [password_hash] text NOT NULL,
  [role] text NOT NULL,
  [created_at] text NOT NULL
)
GO

CREATE TABLE [events] (
  [id] integer PRIMARY KEY IDENTITY(1, 1),
  [title] text NOT NULL,
  [category] text NOT NULL,
  [location] text NOT NULL,
  [event_datetime] text NOT NULL,
  [capacity] integer NOT NULL,
  [available_seats] integer NOT NULL,
  [status] text NOT NULL,
  [created_by_admin_id] integer NOT NULL,
  [created_at] text NOT NULL,
  [updated_at] text NOT NULL
)
GO

CREATE TABLE [reservations] (
  [id] integer PRIMARY KEY IDENTITY(1, 1),
  [user_id] integer NOT NULL,
  [event_id] integer NOT NULL,
  [ticket_quantity] integer NOT NULL,
  [status] text NOT NULL,
  [reserved_at] text NOT NULL,
  [cancelled_at] text
)
GO

CREATE TABLE [notification_logs] (
  [id] integer PRIMARY KEY IDENTITY(1, 1),
  [user_id] integer NOT NULL,
  [reservation_id] integer,
  [event_id] integer,
  [channel] text NOT NULL,
  [message_type] text NOT NULL,
  [delivery_status] text NOT NULL,
  [created_at] text NOT NULL
)
GO

CREATE INDEX [events_index_0] ON [events] ("event_datetime")
GO

CREATE INDEX [events_index_1] ON [events] ("location")
GO

CREATE INDEX [events_index_2] ON [events] ("category")
GO

CREATE INDEX [events_index_3] ON [events] ("status")
GO

CREATE INDEX [events_index_4] ON [events] ("event_datetime", "location", "category")
GO

CREATE INDEX [reservations_index_5] ON [reservations] ("user_id")
GO

CREATE INDEX [reservations_index_6] ON [reservations] ("event_id")
GO

CREATE INDEX [reservations_index_7] ON [reservations] ("status")
GO

CREATE INDEX [notification_logs_index_8] ON [notification_logs] ("user_id")
GO

CREATE INDEX [notification_logs_index_9] ON [notification_logs] ("reservation_id")
GO

CREATE INDEX [notification_logs_index_10] ON [notification_logs] ("event_id")
GO

CREATE INDEX [notification_logs_index_11] ON [notification_logs] ("created_at")
GO

EXEC sp_addextendedproperty
@name = N'Table_Description',
@value = 'Application enforces at least one of email or phone during registration.',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'users';
GO

EXEC sp_addextendedproperty
@name = N'Column_Description',
@value = 'CUSTOMER|ADMIN',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'users',
@level2type = N'Column', @level2name = 'role';
GO

EXEC sp_addextendedproperty
@name = N'Table_Description',
@value = 'Application/DB constraints: capacity >= 1 and available_seats between 0 and capacity.',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'events';
GO

EXEC sp_addextendedproperty
@name = N'Column_Description',
@value = 'ACTIVE|CANCELLED',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'events',
@level2type = N'Column', @level2name = 'status';
GO

EXEC sp_addextendedproperty
@name = N'Table_Description',
@value = 'Application/DB constraints: ticket_quantity >= 1.',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'reservations';
GO

EXEC sp_addextendedproperty
@name = N'Column_Description',
@value = 'CONFIRMED|CANCELLED',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'reservations',
@level2type = N'Column', @level2name = 'status';
GO

EXEC sp_addextendedproperty
@name = N'Column_Description',
@value = 'EMAIL|SMS',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'notification_logs',
@level2type = N'Column', @level2name = 'channel';
GO

EXEC sp_addextendedproperty
@name = N'Column_Description',
@value = 'RESERVATION_CONFIRMED|RESERVATION_CANCELLED|EVENT_CANCELLED',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'notification_logs',
@level2type = N'Column', @level2name = 'message_type';
GO

EXEC sp_addextendedproperty
@name = N'Column_Description',
@value = 'SENT|FAILED',
@level0type = N'Schema', @level0name = 'dbo',
@level1type = N'Table',  @level1name = 'notification_logs',
@level2type = N'Column', @level2name = 'delivery_status';
GO

ALTER TABLE [events] ADD FOREIGN KEY ([created_by_admin_id]) REFERENCES [users] ([id])
GO

ALTER TABLE [reservations] ADD FOREIGN KEY ([user_id]) REFERENCES [users] ([id])
GO

ALTER TABLE [reservations] ADD FOREIGN KEY ([event_id]) REFERENCES [events] ([id])
GO

ALTER TABLE [notification_logs] ADD FOREIGN KEY ([user_id]) REFERENCES [users] ([id])
GO

ALTER TABLE [notification_logs] ADD FOREIGN KEY ([reservation_id]) REFERENCES [reservations] ([id])
GO

ALTER TABLE [notification_logs] ADD FOREIGN KEY ([event_id]) REFERENCES [events] ([id])
GO
