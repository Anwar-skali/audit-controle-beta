-- Initialisation des bases de données indépendantes par microservice métier
CREATE DATABASE supplier_db;
CREATE DATABASE transaction_db;
CREATE DATABASE finding_db;

GRANT ALL PRIVILEGES ON DATABASE supplier_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE transaction_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE finding_db TO postgres;

