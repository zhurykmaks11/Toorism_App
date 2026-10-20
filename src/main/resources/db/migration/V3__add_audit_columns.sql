-- Лаба №4: аудит — хто і коли створив / востаннє змінив запис

ALTER TABLE trips
    ADD COLUMN created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    ADD COLUMN updated_by VARCHAR(50),
    ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE destinations
    ADD COLUMN created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    ADD COLUMN created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_by VARCHAR(50),
    ADD COLUMN updated_at TIMESTAMP;

ALTER TABLE places
    ADD COLUMN created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    ADD COLUMN created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_by VARCHAR(50),
    ADD COLUMN updated_at TIMESTAMP;
