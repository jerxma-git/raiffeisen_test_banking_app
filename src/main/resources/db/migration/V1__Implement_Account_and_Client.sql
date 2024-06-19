CREATE TABLE clients (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(255),
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);


CREATE TYPE account_status AS ENUM ('ACTIVE', 'CLOSED');

CREATE TABLE accounts (
    id SERIAL PRIMARY KEY,
    client_id BIGINT REFERENCES clients(id),
    account_number VARCHAR(255) NOT NULL UNIQUE,
    balance DOUBLE PRECISION NOT NULL,
    status account_status NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);