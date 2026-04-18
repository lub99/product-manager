CREATE TABLE products (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(10)    NOT NULL UNIQUE,
    name         VARCHAR(255)   NOT NULL,
    price_eur    NUMERIC(19, 2) NOT NULL,
    price_usd    NUMERIC(19, 2) NOT NULL,
    is_available BOOLEAN        NOT NULL DEFAULT FALSE
);
