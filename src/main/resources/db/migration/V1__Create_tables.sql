
CREATE TABLE payments (
                          id VARCHAR(255) PRIMARY KEY,
                          amount DECIMAL(10,2) NOT NULL,
                          creation_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          payment_method VARCHAR(100) NOT NULL,
                          status VARCHAR(20) NOT NULL DEFAULT 'VERIFYING'
);

CREATE TABLE donors (
                        id BIGSERIAL PRIMARY KEY,
                        email VARCHAR(255) NOT NULL,
                        full_name VARCHAR(255) NOT NULL
);

CREATE TABLE beneficiaries (
                               id BIGSERIAL PRIMARY KEY,
                               email VARCHAR(255) NOT NULL,
                               full_name VARCHAR(255) NOT NULL
);

CREATE TABLE donations (
                           id BIGSERIAL PRIMARY KEY,
                           donor_id BIGINT NOT NULL REFERENCES donors(id),
                           payment_id VARCHAR(255) NOT NULL REFERENCES payments(id),
                           creation_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE helps (
                       id BIGSERIAL PRIMARY KEY,
                       beneficiary_id BIGINT NOT NULL REFERENCES beneficiaries(id),
                       payment_id VARCHAR(255) NOT NULL REFERENCES payments(id),
                       accident_description TEXT NOT NULL,
                       creation_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index pour améliorer les performances
CREATE INDEX idx_donations_creation_datetime ON donations(creation_datetime DESC);
CREATE INDEX idx_helps_creation_datetime ON helps(creation_datetime DESC);
CREATE INDEX idx_payments_status ON payments(status);
