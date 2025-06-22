CREATE TABLE sys_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    role VARCHAR(100) NOT NULL,
    is_enable BOOLEAN NOT NULL
);
