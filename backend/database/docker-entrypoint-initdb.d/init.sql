
CREATE TABLE users (
   id SERIAL PRIMARY KEY,
   username VARCHAR(30),
   password TEXT NOT NULL CHECK (password <> ''),
   name VARCHAR(30),
   gender CHAR(1) CHECK (gender IN ('М', 'Ж')),
   usual_dialog_style VARCHAR(20) DEFAULT 'BASE',
   task VARCHAR(20)
);


CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    name VARCHAR(20) UNIQUE
);

INSERT INTO tasks (name)
VALUES 
    ('GET_METRICS'),
    ('TALK'),
    ('ACQUAINTANCE');

CREATE TABLE users_tasks (
    user_id INTEGER,
    task_id INTEGER,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);



CREATE TABLE metrics (
    id SERIAL PRIMARY KEY,
    user_id INTEGER,
    metric_type VARCHAR(30) NOT NULL, 
    created_at TIMESTAMP DEFAULT NOW(),
    metric_value INTEGER CHECK (metric_value >= 0 AND metric_value <= 100),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


CREATE TABLE saved_messages (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    message TEXT NOT NULL,
    role VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);



















CREATE OR REPLACE FUNCTION user_history_length_check()
    RETURNS TRIGGER AS $$
DECLARE
    max_history_length INTEGER;
    message_count INTEGER;
    excess INTEGER;
BEGIN
    BEGIN
        max_history_length := current_setting('ai.history.length')::INTEGER;
    EXCEPTION
        WHEN others THEN
            max_history_length := 10;
    END;

    SELECT COUNT(*) INTO message_count
    FROM saved_messages
    WHERE user_id = NEW.user_id;

    IF message_count >= max_history_length THEN
        excess := message_count - max_history_length + 1;

        DELETE FROM saved_messages
        WHERE id IN (
            SELECT id FROM saved_messages
            WHERE user_id = NEW.user_id
            ORDER BY id ASC
            LIMIT excess
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE TRIGGER trg_check_user_history_length
    BEFORE INSERT ON saved_messages
    FOR EACH ROW
EXECUTE FUNCTION user_history_length_check();
