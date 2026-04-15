CREATE TABLE line_items (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    document_id  INTEGER NOT NULL,
    position     INTEGER NOT NULL,
    description  TEXT NOT NULL,
    gross_amount REAL NOT NULL,
    net_amount   REAL NOT NULL,
    tax_amount   REAL NOT NULL,
    tax_rate     REAL NOT NULL,
    created_at   TEXT NOT NULL,
    FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE
);