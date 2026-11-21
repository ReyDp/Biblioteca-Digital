ALTER TABLE outbox_event ADD COLUMN publishing_since TIMESTAMPTZ;

CREATE INDEX idx_outbox_publishing_lease ON outbox_event (status, publishing_since);
