INSERT INTO payments (id, amount, creation_datetime, payment_method, status)
VALUES ('payment-123', 50.00, '2025-08-01 10:00:00', 'Carte bancaire', 'SUCCEEDED');

-- Insertion d'un donateur d'exemple
INSERT INTO donors (email, full_name)
VALUES ('john.doe@hei.school', 'John Doe');

-- Insertion d'une donation d'exemple
INSERT INTO donations (donor_id, payment_id, creation_datetime)
VALUES (1, 'payment-123', '2025-08-01 10:00:00');

-- Données pour une aide
INSERT INTO payments (id, amount, creation_datetime, payment_method, status)
VALUES ('payment-456', 100.00, '2025-08-02 14:30:00', 'Virement', 'SUCCEEDED');

INSERT INTO beneficiaries (email, full_name)
VALUES ('marie.martin@hei.school', 'Marie Martin');

INSERT INTO helps (beneficiary_id, payment_id, accident_description, creation_datetime)
VALUES (1, 'payment-456', 'Aide d''urgence suite à un accident de voiture nécessitant des frais médicaux', '2025-08-02 14:30:00');
