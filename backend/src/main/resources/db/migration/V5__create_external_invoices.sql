CREATE TABLE external_invoices (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    file_path TEXT NOT NULL,
    year INTEGER NOT NULL,
    date TEXT NOT NULL,
    description TEXT NOT NULL,
    category TEXT,
    gross_amount REAL NOT NULL,
    net_amount REAL NOT NULL,
    tax_amount REAL NOT NULL,
    tax_rate REAL NOT NULL DEFAULT 19.0,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE INDEX idx_external_invoices_year_date
    ON external_invoices (year, date);