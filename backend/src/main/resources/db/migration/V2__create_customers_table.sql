CREATE TABLE customers(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    name        TEXT NOT NULL,
    street      TEXT NOT NULL,
    zip         TEXT NOT NULL,
    city        TEXT NOT NULL,
    email       TEXT,
    phone       TEXT,
    notes       TEXT,
    created_at  TEXT NOT NULL,
    updated_at  TEXT NOT NULL
);