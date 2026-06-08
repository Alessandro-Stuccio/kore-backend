-- ============================================================
-- Kore — dati di seed per lo sviluppo locale
-- Eseguito da Spring Boot dopo la creazione dello schema
-- Hibernate (spring.sql.init.mode: always, dev profile).
--
-- Password comune per tutti gli utenti: "password"
-- BCrypt hash: $2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu
-- ============================================================

-- Piani
INSERT INTO plans (name, duration, monthly_creditspt, monthly_credits_nutri, full_price, monthly_installment_price)
VALUES
    ('Basic Pack Semestrale',   'SEMESTRALE', 1, 1,  960.0,  160.0),
    ('Basic Pack Annuale',      'ANNUALE',    1, 1, 1800.0,  150.0),
    ('Premium Pack Semestrale', 'SEMESTRALE', 2, 2, 1620.0,  270.0),
    ('Premium Pack Annuale',    'ANNUALE',    2, 2, 3000.0,  250.0)
ON CONFLICT (name) DO NOTHING;

-- ============================================================
-- Professionisti e staff
-- ============================================================
INSERT INTO users (version, email, password, first_name, last_name, role, assigned_pt_id, assigned_nutritionist_id)
VALUES
    (0, 'pt1@test.com',        '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Marco',   'Rossi',         'PERSONAL_TRAINER', NULL, NULL),
    (0, 'pt2@test.com',        '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Giulia',  'Bianchi',       'PERSONAL_TRAINER', NULL, NULL),
    (0, 'nutri1@test.com',     '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Laura',   'Verdi',         'NUTRITIONIST',     NULL, NULL),
    (0, 'nutri2@test.com',     '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Andrea',  'Esposito',      'NUTRITIONIST',     NULL, NULL),
    (0, 'admin@test.com',      '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Admin',   'Sistema',       'ADMIN',            NULL, NULL),
    (0, 'insurance@test.com',  '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Paolo',   'Assicurazioni', 'INSURANCE_MANAGER',NULL, NULL),
    (0, 'moderator1@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Marta',   'Moderatrice',   'MODERATOR',        NULL, NULL),
    (0, 'moderator2@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Lorenzo', 'Support',       'MODERATOR',        NULL, NULL),
    (0, 'moderator3@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Elisa',   'Care',          'MODERATOR',        NULL, NULL)
ON CONFLICT (email) DO NOTHING;

-- ============================================================
-- Clienti (referenziano PT e nutrizionista già inseriti)
-- ============================================================
INSERT INTO users (version, email, password, first_name, last_name, role, assigned_pt_id, assigned_nutritionist_id)
VALUES (0, 'luca@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Luca', 'Ferri', 'CLIENT',
        (SELECT id FROM users WHERE email = 'pt1@test.com'),
        (SELECT id FROM users WHERE email = 'nutri1@test.com'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (version, email, password, first_name, last_name, role, assigned_pt_id, assigned_nutritionist_id)
VALUES (0, 'sofia@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Sofia', 'Conti', 'CLIENT',
        (SELECT id FROM users WHERE email = 'pt1@test.com'),
        (SELECT id FROM users WHERE email = 'nutri2@test.com'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (version, email, password, first_name, last_name, role, assigned_pt_id, assigned_nutritionist_id)
VALUES (0, 'matteo@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Matteo', 'Galli', 'CLIENT',
        (SELECT id FROM users WHERE email = 'pt2@test.com'),
        (SELECT id FROM users WHERE email = 'nutri1@test.com'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (version, email, password, first_name, last_name, role, assigned_pt_id, assigned_nutritionist_id)
VALUES (0, 'chiara@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Chiara', 'Fontana', 'CLIENT',
        (SELECT id FROM users WHERE email = 'pt2@test.com'),
        (SELECT id FROM users WHERE email = 'nutri2@test.com'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (version, email, password, first_name, last_name, role, assigned_pt_id, assigned_nutritionist_id)
VALUES (0, 'testreview@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Test', 'Recensore', 'CLIENT',
        (SELECT id FROM users WHERE email = 'pt1@test.com'),
        (SELECT id FROM users WHERE email = 'nutri1@test.com'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (version, email, password, first_name, last_name, role, assigned_pt_id, assigned_nutritionist_id)
VALUES (0, 'elena@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Elena', 'Marino', 'CLIENT',
        (SELECT id FROM users WHERE email = 'pt1@test.com'),
        (SELECT id FROM users WHERE email = 'nutri1@test.com'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (version, email, password, first_name, last_name, role, assigned_pt_id, assigned_nutritionist_id)
VALUES (0, 'davide@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Davide', 'Ricci', 'CLIENT',
        (SELECT id FROM users WHERE email = 'pt2@test.com'),
        (SELECT id FROM users WHERE email = 'nutri1@test.com'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (version, email, password, first_name, last_name, role, assigned_pt_id, assigned_nutritionist_id)
VALUES (0, 'valentina@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Valentina', 'Greco', 'CLIENT',
        (SELECT id FROM users WHERE email = 'pt1@test.com'),
        (SELECT id FROM users WHERE email = 'nutri2@test.com'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (version, email, password, first_name, last_name, role, assigned_pt_id, assigned_nutritionist_id)
VALUES (0, 'francesca@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Francesca', 'Bruno', 'CLIENT',
        (SELECT id FROM users WHERE email = 'pt2@test.com'),
        (SELECT id FROM users WHERE email = 'nutri2@test.com'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (version, email, password, first_name, last_name, role, assigned_pt_id, assigned_nutritionist_id)
VALUES (0, 'alessio@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Alessio', 'Fiore', 'CLIENT',
        (SELECT id FROM users WHERE email = 'pt2@test.com'),
        (SELECT id FROM users WHERE email = 'nutri1@test.com'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (version, email, password, first_name, last_name, role, assigned_pt_id, assigned_nutritionist_id)
VALUES (0, 'irene@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Irene', 'Lombardi', 'CLIENT',
        (SELECT id FROM users WHERE email = 'pt1@test.com'),
        (SELECT id FROM users WHERE email = 'nutri2@test.com'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (version, email, password, first_name, last_name, role, assigned_pt_id, assigned_nutritionist_id)
VALUES (0, 'antonio@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Antonio', 'Moretti', 'CLIENT',
        (SELECT id FROM users WHERE email = 'pt2@test.com'),
        (SELECT id FROM users WHERE email = 'nutri2@test.com'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (version, email, password, first_name, last_name, role, assigned_pt_id, assigned_nutritionist_id)
VALUES (0, 'giulia.c@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Giulia', 'Costa', 'CLIENT',
        (SELECT id FROM users WHERE email = 'pt1@test.com'),
        (SELECT id FROM users WHERE email = 'nutri1@test.com'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (version, email, password, first_name, last_name, role, assigned_pt_id, assigned_nutritionist_id)
VALUES (0, 'roberto@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Roberto', 'Ferrari', 'CLIENT',
        (SELECT id FROM users WHERE email = 'pt2@test.com'),
        (SELECT id FROM users WHERE email = 'nutri1@test.com'))
ON CONFLICT (email) DO NOTHING;

INSERT INTO users (version, email, password, first_name, last_name, role, assigned_pt_id, assigned_nutritionist_id)
VALUES (0, 'marina@test.com', '$2a$10$0VtW52huEimaZO64NAgNpO8NXKTrMutT24RHz..em0HI8QkxW0.eu', 'Marina', 'Pellegrini', 'CLIENT',
        (SELECT id FROM users WHERE email = 'pt1@test.com'),
        (SELECT id FROM users WHERE email = 'nutri2@test.com'))
ON CONFLICT (email) DO NOTHING;

-- Utente con account creato 40 giorni fa (abilita logica recensione)
UPDATE users SET created_at = NOW() - INTERVAL '40 days' WHERE email = 'testreview@test.com';

-- ============================================================
-- Abbonamenti
-- ============================================================
INSERT INTO subscriptions (version, user_id, plan_id, payment_frequency, start_date, end_date, active, current_creditspt, current_credits_nutri, last_renewal_date, installments_paid, total_installments, next_payment_date)
VALUES (0, (SELECT id FROM users WHERE email = 'luca@test.com'), (SELECT id FROM plans WHERE name = 'Basic Pack Semestrale'),
    'UNICA_SOLUZIONE', CURRENT_DATE, CURRENT_DATE + INTERVAL '6 months', true, 1, 1, CURRENT_DATE, 1, 1, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO subscriptions (version, user_id, plan_id, payment_frequency, start_date, end_date, active, current_creditspt, current_credits_nutri, last_renewal_date, installments_paid, total_installments, next_payment_date)
VALUES (0, (SELECT id FROM users WHERE email = 'sofia@test.com'), (SELECT id FROM plans WHERE name = 'Basic Pack Annuale'),
    'RATE_MENSILI', CURRENT_DATE, CURRENT_DATE + INTERVAL '12 months', true, 1, 1, CURRENT_DATE, 1, 12, CURRENT_DATE + INTERVAL '1 month')
ON CONFLICT DO NOTHING;

INSERT INTO subscriptions (version, user_id, plan_id, payment_frequency, start_date, end_date, active, current_creditspt, current_credits_nutri, last_renewal_date, installments_paid, total_installments, next_payment_date)
VALUES (0, (SELECT id FROM users WHERE email = 'matteo@test.com'), (SELECT id FROM plans WHERE name = 'Premium Pack Semestrale'),
    'UNICA_SOLUZIONE', CURRENT_DATE, CURRENT_DATE + INTERVAL '6 months', true, 2, 2, CURRENT_DATE, 1, 1, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO subscriptions (version, user_id, plan_id, payment_frequency, start_date, end_date, active, current_creditspt, current_credits_nutri, last_renewal_date, installments_paid, total_installments, next_payment_date)
VALUES (0, (SELECT id FROM users WHERE email = 'chiara@test.com'), (SELECT id FROM plans WHERE name = 'Premium Pack Annuale'),
    'RATE_MENSILI', CURRENT_DATE, CURRENT_DATE + INTERVAL '12 months', true, 2, 2, CURRENT_DATE, 1, 12, CURRENT_DATE + INTERVAL '1 month')
ON CONFLICT DO NOTHING;

INSERT INTO subscriptions (version, user_id, plan_id, payment_frequency, start_date, end_date, active, current_creditspt, current_credits_nutri, last_renewal_date, installments_paid, total_installments, next_payment_date)
VALUES (0, (SELECT id FROM users WHERE email = 'testreview@test.com'), (SELECT id FROM plans WHERE name = 'Basic Pack Semestrale'),
    'UNICA_SOLUZIONE', CURRENT_DATE, CURRENT_DATE + INTERVAL '6 months', true, 1, 1, CURRENT_DATE, 1, 1, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO subscriptions (version, user_id, plan_id, payment_frequency, start_date, end_date, active, current_creditspt, current_credits_nutri, last_renewal_date, installments_paid, total_installments, next_payment_date)
VALUES (0, (SELECT id FROM users WHERE email = 'elena@test.com'), (SELECT id FROM plans WHERE name = 'Premium Pack Semestrale'),
    'UNICA_SOLUZIONE', CURRENT_DATE, CURRENT_DATE + INTERVAL '6 months', true, 2, 2, CURRENT_DATE, 1, 1, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO subscriptions (version, user_id, plan_id, payment_frequency, start_date, end_date, active, current_creditspt, current_credits_nutri, last_renewal_date, installments_paid, total_installments, next_payment_date)
VALUES (0, (SELECT id FROM users WHERE email = 'davide@test.com'), (SELECT id FROM plans WHERE name = 'Basic Pack Annuale'),
    'RATE_MENSILI', CURRENT_DATE, CURRENT_DATE + INTERVAL '12 months', true, 1, 1, CURRENT_DATE, 1, 12, CURRENT_DATE + INTERVAL '1 month')
ON CONFLICT DO NOTHING;

INSERT INTO subscriptions (version, user_id, plan_id, payment_frequency, start_date, end_date, active, current_creditspt, current_credits_nutri, last_renewal_date, installments_paid, total_installments, next_payment_date)
VALUES (0, (SELECT id FROM users WHERE email = 'valentina@test.com'), (SELECT id FROM plans WHERE name = 'Premium Pack Annuale'),
    'UNICA_SOLUZIONE', CURRENT_DATE, CURRENT_DATE + INTERVAL '12 months', true, 2, 2, CURRENT_DATE, 1, 1, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO subscriptions (version, user_id, plan_id, payment_frequency, start_date, end_date, active, current_creditspt, current_credits_nutri, last_renewal_date, installments_paid, total_installments, next_payment_date)
VALUES (0, (SELECT id FROM users WHERE email = 'francesca@test.com'), (SELECT id FROM plans WHERE name = 'Basic Pack Semestrale'),
    'RATE_MENSILI', CURRENT_DATE, CURRENT_DATE + INTERVAL '6 months', true, 1, 1, CURRENT_DATE, 1, 6, CURRENT_DATE + INTERVAL '1 month')
ON CONFLICT DO NOTHING;

INSERT INTO subscriptions (version, user_id, plan_id, payment_frequency, start_date, end_date, active, current_creditspt, current_credits_nutri, last_renewal_date, installments_paid, total_installments, next_payment_date)
VALUES (0, (SELECT id FROM users WHERE email = 'alessio@test.com'), (SELECT id FROM plans WHERE name = 'Premium Pack Semestrale'),
    'UNICA_SOLUZIONE', CURRENT_DATE, CURRENT_DATE + INTERVAL '6 months', true, 2, 2, CURRENT_DATE, 1, 1, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO subscriptions (version, user_id, plan_id, payment_frequency, start_date, end_date, active, current_creditspt, current_credits_nutri, last_renewal_date, installments_paid, total_installments, next_payment_date)
VALUES (0, (SELECT id FROM users WHERE email = 'irene@test.com'), (SELECT id FROM plans WHERE name = 'Basic Pack Annuale'),
    'RATE_MENSILI', CURRENT_DATE, CURRENT_DATE + INTERVAL '12 months', true, 1, 1, CURRENT_DATE, 1, 12, CURRENT_DATE + INTERVAL '1 month')
ON CONFLICT DO NOTHING;

INSERT INTO subscriptions (version, user_id, plan_id, payment_frequency, start_date, end_date, active, current_creditspt, current_credits_nutri, last_renewal_date, installments_paid, total_installments, next_payment_date)
VALUES (0, (SELECT id FROM users WHERE email = 'antonio@test.com'), (SELECT id FROM plans WHERE name = 'Premium Pack Annuale'),
    'RATE_MENSILI', CURRENT_DATE, CURRENT_DATE + INTERVAL '12 months', true, 2, 2, CURRENT_DATE, 1, 12, CURRENT_DATE + INTERVAL '1 month')
ON CONFLICT DO NOTHING;

INSERT INTO subscriptions (version, user_id, plan_id, payment_frequency, start_date, end_date, active, current_creditspt, current_credits_nutri, last_renewal_date, installments_paid, total_installments, next_payment_date)
VALUES (0, (SELECT id FROM users WHERE email = 'giulia.c@test.com'), (SELECT id FROM plans WHERE name = 'Basic Pack Semestrale'),
    'UNICA_SOLUZIONE', CURRENT_DATE, CURRENT_DATE + INTERVAL '6 months', true, 1, 1, CURRENT_DATE, 1, 1, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO subscriptions (version, user_id, plan_id, payment_frequency, start_date, end_date, active, current_creditspt, current_credits_nutri, last_renewal_date, installments_paid, total_installments, next_payment_date)
VALUES (0, (SELECT id FROM users WHERE email = 'roberto@test.com'), (SELECT id FROM plans WHERE name = 'Premium Pack Semestrale'),
    'UNICA_SOLUZIONE', CURRENT_DATE, CURRENT_DATE + INTERVAL '6 months', true, 2, 2, CURRENT_DATE, 1, 1, NULL)
ON CONFLICT DO NOTHING;

INSERT INTO subscriptions (version, user_id, plan_id, payment_frequency, start_date, end_date, active, current_creditspt, current_credits_nutri, last_renewal_date, installments_paid, total_installments, next_payment_date)
VALUES (0, (SELECT id FROM users WHERE email = 'marina@test.com'), (SELECT id FROM plans WHERE name = 'Basic Pack Annuale'),
    'RATE_MENSILI', CURRENT_DATE, CURRENT_DATE + INTERVAL '12 months', true, 1, 1, CURRENT_DATE, 1, 12, CURRENT_DATE + INTERVAL '1 month')
ON CONFLICT DO NOTHING;

-- ============================================================
-- Orari settimanali
-- ============================================================
INSERT INTO weekly_schedules (professional_id, day_of_week, start_time, end_time)
VALUES
    ((SELECT id FROM users WHERE email = 'pt1@test.com'),    'MONDAY',    '09:00:00', '13:00:00'),
    ((SELECT id FROM users WHERE email = 'pt1@test.com'),    'WEDNESDAY', '15:00:00', '19:00:00'),
    ((SELECT id FROM users WHERE email = 'pt1@test.com'),    'FRIDAY',    '09:00:00', '13:00:00'),
    ((SELECT id FROM users WHERE email = 'pt2@test.com'),    'TUESDAY',   '10:00:00', '14:00:00'),
    ((SELECT id FROM users WHERE email = 'pt2@test.com'),    'THURSDAY',  '16:00:00', '20:00:00'),
    ((SELECT id FROM users WHERE email = 'pt2@test.com'),    'SATURDAY',  '09:00:00', '12:00:00'),
    ((SELECT id FROM users WHERE email = 'nutri1@test.com'), 'MONDAY',    '14:00:00', '18:00:00'),
    ((SELECT id FROM users WHERE email = 'nutri1@test.com'), 'WEDNESDAY', '09:00:00', '13:00:00'),
    ((SELECT id FROM users WHERE email = 'nutri1@test.com'), 'FRIDAY',    '10:00:00', '14:00:00'),
    ((SELECT id FROM users WHERE email = 'nutri2@test.com'), 'TUESDAY',   '09:00:00', '13:00:00'),
    ((SELECT id FROM users WHERE email = 'nutri2@test.com'), 'THURSDAY',  '15:00:00', '19:00:00'),
    ((SELECT id FROM users WHERE email = 'nutri2@test.com'), 'SATURDAY',  '10:00:00', '13:00:00');

-- ============================================================
-- Slot futuri (generati automaticamente dallo schedule settimanale)
-- ============================================================
INSERT INTO slots (professional_id, start_time, end_time, booked_by_id, reminder_sent, version)
SELECT
    ws.professional_id,
    (CURRENT_DATE + offs + ws.start_time + (n * INTERVAL '30 minutes'))::timestamp,
    (CURRENT_DATE + offs + ws.start_time + (n * INTERVAL '30 minutes') + INTERVAL '30 minutes')::timestamp,
    NULL,
    false,
    0
FROM weekly_schedules ws
CROSS JOIN generate_series(1, 14) AS offs
CROSS JOIN generate_series(0,
    (EXTRACT(EPOCH FROM (ws.end_time - ws.start_time)) / 1800)::int - 1
) AS n
WHERE EXTRACT(ISODOW FROM (CURRENT_DATE + offs))::int =
    CASE ws.day_of_week
        WHEN 'MONDAY'    THEN 1  WHEN 'TUESDAY'   THEN 2  WHEN 'WEDNESDAY' THEN 3
        WHEN 'THURSDAY'  THEN 4  WHEN 'FRIDAY'    THEN 5  WHEN 'SATURDAY'  THEN 6
        WHEN 'SUNDAY'    THEN 7
    END
ON CONFLICT DO NOTHING;

-- ============================================================
-- Slot passati (per booking completati e recensioni)
-- ============================================================

-- PT1: slot passati per luca, sofia, testreview, elena, valentina, irene, giulia.c, marina
INSERT INTO slots (professional_id, start_time, end_time, booked_by_id, status, meeting_link, reminder_sent, version)
VALUES
    ((SELECT id FROM users WHERE email = 'pt1@test.com'), (CURRENT_DATE - 10 + TIME '09:00:00')::timestamp, (CURRENT_DATE - 10 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'luca@test.com'),        'COMPLETED', 'https://meet.jit.si/Kore_luca_pt1_past',       true, 1),
    ((SELECT id FROM users WHERE email = 'pt1@test.com'), (CURRENT_DATE - 8 + TIME '09:00:00')::timestamp,  (CURRENT_DATE - 8 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'sofia@test.com'),       'COMPLETED', 'https://meet.jit.si/Kore_sofia_pt1_past',      true, 1),
    ((SELECT id FROM users WHERE email = 'pt1@test.com'), (CURRENT_DATE - 6 + TIME '09:00:00')::timestamp,  (CURRENT_DATE - 6 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'testreview@test.com'),  'COMPLETED', 'https://meet.jit.si/Kore_testreview_pt1_past', true, 1),
    ((SELECT id FROM users WHERE email = 'pt1@test.com'), (CURRENT_DATE - 5 + TIME '09:00:00')::timestamp,  (CURRENT_DATE - 5 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'elena@test.com'),       'COMPLETED', 'https://meet.jit.si/Kore_elena_pt1_past',      true, 1),
    ((SELECT id FROM users WHERE email = 'pt1@test.com'), (CURRENT_DATE - 4 + TIME '09:00:00')::timestamp,  (CURRENT_DATE - 4 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'valentina@test.com'),   'COMPLETED', 'https://meet.jit.si/Kore_valentina_pt1_past',  true, 1),
    ((SELECT id FROM users WHERE email = 'pt1@test.com'), (CURRENT_DATE - 3 + TIME '09:00:00')::timestamp,  (CURRENT_DATE - 3 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'irene@test.com'),       'COMPLETED', 'https://meet.jit.si/Kore_irene_pt1_past',      true, 1),
    ((SELECT id FROM users WHERE email = 'pt1@test.com'), (CURRENT_DATE - 2 + TIME '09:00:00')::timestamp,  (CURRENT_DATE - 2 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'giulia.c@test.com'),    'COMPLETED', 'https://meet.jit.si/Kore_giuliac_pt1_past',    true, 1),
    ((SELECT id FROM users WHERE email = 'pt1@test.com'), (CURRENT_DATE - 1 + TIME '09:00:00')::timestamp,  (CURRENT_DATE - 1 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'marina@test.com'),      'COMPLETED', 'https://meet.jit.si/Kore_marina_pt1_past',     true, 1);

-- PT2: slot passati per matteo, chiara, davide, francesca, alessio, antonio, roberto
INSERT INTO slots (professional_id, start_time, end_time, booked_by_id, status, meeting_link, reminder_sent, version)
VALUES
    ((SELECT id FROM users WHERE email = 'pt2@test.com'), (CURRENT_DATE - 10 + TIME '10:00:00')::timestamp, (CURRENT_DATE - 10 + TIME '10:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'matteo@test.com'),      'COMPLETED', 'https://meet.jit.si/Kore_matteo_pt2_past',     true, 1),
    ((SELECT id FROM users WHERE email = 'pt2@test.com'), (CURRENT_DATE - 8 + TIME '10:00:00')::timestamp,  (CURRENT_DATE - 8 + TIME '10:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'chiara@test.com'),      'COMPLETED', 'https://meet.jit.si/Kore_chiara_pt2_past',     true, 1),
    ((SELECT id FROM users WHERE email = 'pt2@test.com'), (CURRENT_DATE - 6 + TIME '10:00:00')::timestamp,  (CURRENT_DATE - 6 + TIME '10:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'davide@test.com'),      'COMPLETED', 'https://meet.jit.si/Kore_davide_pt2_past',     true, 1),
    ((SELECT id FROM users WHERE email = 'pt2@test.com'), (CURRENT_DATE - 4 + TIME '10:00:00')::timestamp,  (CURRENT_DATE - 4 + TIME '10:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'francesca@test.com'),   'COMPLETED', 'https://meet.jit.si/Kore_francesca_pt2_past',  true, 1),
    ((SELECT id FROM users WHERE email = 'pt2@test.com'), (CURRENT_DATE - 3 + TIME '10:00:00')::timestamp,  (CURRENT_DATE - 3 + TIME '10:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'alessio@test.com'),     'COMPLETED', 'https://meet.jit.si/Kore_alessio_pt2_past',    true, 1),
    ((SELECT id FROM users WHERE email = 'pt2@test.com'), (CURRENT_DATE - 2 + TIME '10:00:00')::timestamp,  (CURRENT_DATE - 2 + TIME '10:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'antonio@test.com'),     'COMPLETED', 'https://meet.jit.si/Kore_antonio_pt2_past',    true, 1),
    ((SELECT id FROM users WHERE email = 'pt2@test.com'), (CURRENT_DATE - 1 + TIME '10:00:00')::timestamp,  (CURRENT_DATE - 1 + TIME '10:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'roberto@test.com'),     'COMPLETED', 'https://meet.jit.si/Kore_roberto_pt2_past',    true, 1);

-- NUTRI1: slot passati per luca, matteo, testreview, davide, alessio, roberto
INSERT INTO slots (professional_id, start_time, end_time, booked_by_id, status, meeting_link, reminder_sent, version)
VALUES
    ((SELECT id FROM users WHERE email = 'nutri1@test.com'), (CURRENT_DATE - 9 + TIME '14:00:00')::timestamp, (CURRENT_DATE - 9 + TIME '14:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'luca@test.com'),        'COMPLETED', 'https://meet.jit.si/Kore_luca_nutri1_past',    true, 1),
    ((SELECT id FROM users WHERE email = 'nutri1@test.com'), (CURRENT_DATE - 7 + TIME '14:00:00')::timestamp, (CURRENT_DATE - 7 + TIME '14:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'matteo@test.com'),      'COMPLETED', 'https://meet.jit.si/Kore_matteo_nutri1_past',  true, 1),
    ((SELECT id FROM users WHERE email = 'nutri1@test.com'), (CURRENT_DATE - 5 + TIME '14:00:00')::timestamp, (CURRENT_DATE - 5 + TIME '14:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'testreview@test.com'),  'COMPLETED', 'https://meet.jit.si/Kore_testreview_n1_past',  true, 1),
    ((SELECT id FROM users WHERE email = 'nutri1@test.com'), (CURRENT_DATE - 3 + TIME '14:00:00')::timestamp, (CURRENT_DATE - 3 + TIME '14:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'davide@test.com'),      'COMPLETED', 'https://meet.jit.si/Kore_davide_nutri1_past',  true, 1),
    ((SELECT id FROM users WHERE email = 'nutri1@test.com'), (CURRENT_DATE - 2 + TIME '14:00:00')::timestamp, (CURRENT_DATE - 2 + TIME '14:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'alessio@test.com'),     'COMPLETED', 'https://meet.jit.si/Kore_alessio_n1_past',     true, 1),
    ((SELECT id FROM users WHERE email = 'nutri1@test.com'), (CURRENT_DATE - 1 + TIME '14:00:00')::timestamp, (CURRENT_DATE - 1 + TIME '14:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'roberto@test.com'),     'COMPLETED', 'https://meet.jit.si/Kore_roberto_n1_past',     true, 1);

-- NUTRI2: slot passati per sofia, chiara, valentina, francesca, irene, antonio
INSERT INTO slots (professional_id, start_time, end_time, booked_by_id, status, meeting_link, reminder_sent, version)
VALUES
    ((SELECT id FROM users WHERE email = 'nutri2@test.com'), (CURRENT_DATE - 9 + TIME '09:00:00')::timestamp, (CURRENT_DATE - 9 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'sofia@test.com'),       'COMPLETED', 'https://meet.jit.si/Kore_sofia_nutri2_past',   true, 1),
    ((SELECT id FROM users WHERE email = 'nutri2@test.com'), (CURRENT_DATE - 7 + TIME '09:00:00')::timestamp, (CURRENT_DATE - 7 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'chiara@test.com'),      'COMPLETED', 'https://meet.jit.si/Kore_chiara_n2_past',      true, 1),
    ((SELECT id FROM users WHERE email = 'nutri2@test.com'), (CURRENT_DATE - 5 + TIME '09:00:00')::timestamp, (CURRENT_DATE - 5 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'valentina@test.com'),   'COMPLETED', 'https://meet.jit.si/Kore_valentina_n2_past',   true, 1),
    ((SELECT id FROM users WHERE email = 'nutri2@test.com'), (CURRENT_DATE - 3 + TIME '09:00:00')::timestamp, (CURRENT_DATE - 3 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'francesca@test.com'),   'COMPLETED', 'https://meet.jit.si/Kore_francesca_n2_past',   true, 1),
    ((SELECT id FROM users WHERE email = 'nutri2@test.com'), (CURRENT_DATE - 2 + TIME '09:00:00')::timestamp, (CURRENT_DATE - 2 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'irene@test.com'),       'COMPLETED', 'https://meet.jit.si/Kore_irene_n2_past',       true, 1),
    ((SELECT id FROM users WHERE email = 'nutri2@test.com'), (CURRENT_DATE - 1 + TIME '09:00:00')::timestamp, (CURRENT_DATE - 1 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'antonio@test.com'),     'COMPLETED', 'https://meet.jit.si/Kore_antonio_n2_past',     true, 1);

-- ============================================================
-- Slot futuri confermati
-- ============================================================

-- PT1
INSERT INTO slots (professional_id, start_time, end_time, booked_by_id, status, meeting_link, reminder_sent, version)
VALUES
    ((SELECT id FROM users WHERE email = 'pt1@test.com'), (CURRENT_DATE + 3 + TIME '09:00:00')::timestamp,  (CURRENT_DATE + 3 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'luca@test.com'),      'CONFIRMED', 'https://meet.jit.si/Kore_luca_pt1_01',      false, 1),
    ((SELECT id FROM users WHERE email = 'pt1@test.com'), (CURRENT_DATE + 5 + TIME '09:00:00')::timestamp,  (CURRENT_DATE + 5 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'sofia@test.com'),     'CONFIRMED', 'https://meet.jit.si/Kore_sofia_pt1_01',     false, 1),
    ((SELECT id FROM users WHERE email = 'pt1@test.com'), (CURRENT_DATE + 7 + TIME '09:00:00')::timestamp,  (CURRENT_DATE + 7 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'elena@test.com'),     'CONFIRMED', 'https://meet.jit.si/Kore_elena_pt1_01',     false, 1),
    ((SELECT id FROM users WHERE email = 'pt1@test.com'), (CURRENT_DATE + 9 + TIME '09:00:00')::timestamp,  (CURRENT_DATE + 9 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'valentina@test.com'), 'CONFIRMED', 'https://meet.jit.si/Kore_valentina_pt1_01', false, 1),
    ((SELECT id FROM users WHERE email = 'pt1@test.com'), (CURRENT_DATE + 11 + TIME '09:00:00')::timestamp, (CURRENT_DATE + 11 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'irene@test.com'),     'CONFIRMED', 'https://meet.jit.si/Kore_irene_pt1_01',     false, 1),
    ((SELECT id FROM users WHERE email = 'pt1@test.com'), (CURRENT_DATE + 13 + TIME '09:00:00')::timestamp, (CURRENT_DATE + 13 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'giulia.c@test.com'),  'CONFIRMED', 'https://meet.jit.si/Kore_giuliac_pt1_01',   false, 1),
    ((SELECT id FROM users WHERE email = 'pt1@test.com'), (CURRENT_DATE + 15 + TIME '09:00:00')::timestamp, (CURRENT_DATE + 15 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'marina@test.com'),    'CONFIRMED', 'https://meet.jit.si/Kore_marina_pt1_01',    false, 1)
ON CONFLICT (professional_id, start_time) DO UPDATE SET
    booked_by_id  = EXCLUDED.booked_by_id,
    status        = EXCLUDED.status,
    meeting_link  = EXCLUDED.meeting_link,
    reminder_sent = EXCLUDED.reminder_sent,
    version       = EXCLUDED.version;

-- PT2
INSERT INTO slots (professional_id, start_time, end_time, booked_by_id, status, meeting_link, reminder_sent, version)
VALUES
    ((SELECT id FROM users WHERE email = 'pt2@test.com'), (CURRENT_DATE + 3 + TIME '10:00:00')::timestamp,  (CURRENT_DATE + 3 + TIME '10:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'matteo@test.com'),    'CONFIRMED', 'https://meet.jit.si/Kore_matteo_pt2_01',    false, 1),
    ((SELECT id FROM users WHERE email = 'pt2@test.com'), (CURRENT_DATE + 5 + TIME '10:00:00')::timestamp,  (CURRENT_DATE + 5 + TIME '10:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'chiara@test.com'),    'CONFIRMED', 'https://meet.jit.si/Kore_chiara_pt2_01',    false, 1),
    ((SELECT id FROM users WHERE email = 'pt2@test.com'), (CURRENT_DATE + 7 + TIME '10:00:00')::timestamp,  (CURRENT_DATE + 7 + TIME '10:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'davide@test.com'),    'CONFIRMED', 'https://meet.jit.si/Kore_davide_pt2_01',    false, 1),
    ((SELECT id FROM users WHERE email = 'pt2@test.com'), (CURRENT_DATE + 9 + TIME '10:00:00')::timestamp,  (CURRENT_DATE + 9 + TIME '10:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'francesca@test.com'), 'CONFIRMED', 'https://meet.jit.si/Kore_francesca_pt2_01', false, 1),
    ((SELECT id FROM users WHERE email = 'pt2@test.com'), (CURRENT_DATE + 11 + TIME '10:00:00')::timestamp, (CURRENT_DATE + 11 + TIME '10:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'alessio@test.com'),   'CONFIRMED', 'https://meet.jit.si/Kore_alessio_pt2_01',   false, 1),
    ((SELECT id FROM users WHERE email = 'pt2@test.com'), (CURRENT_DATE + 13 + TIME '10:00:00')::timestamp, (CURRENT_DATE + 13 + TIME '10:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'antonio@test.com'),   'CONFIRMED', 'https://meet.jit.si/Kore_antonio_pt2_01',   false, 1),
    ((SELECT id FROM users WHERE email = 'pt2@test.com'), (CURRENT_DATE + 15 + TIME '10:00:00')::timestamp, (CURRENT_DATE + 15 + TIME '10:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'roberto@test.com'),   'CONFIRMED', 'https://meet.jit.si/Kore_roberto_pt2_01',   false, 1)
ON CONFLICT (professional_id, start_time) DO UPDATE SET
    booked_by_id  = EXCLUDED.booked_by_id,
    status        = EXCLUDED.status,
    meeting_link  = EXCLUDED.meeting_link,
    reminder_sent = EXCLUDED.reminder_sent,
    version       = EXCLUDED.version;

-- NUTRI1
INSERT INTO slots (professional_id, start_time, end_time, booked_by_id, status, meeting_link, reminder_sent, version)
VALUES
    ((SELECT id FROM users WHERE email = 'nutri1@test.com'), (CURRENT_DATE + 4 + TIME '14:00:00')::timestamp,  (CURRENT_DATE + 4 + TIME '14:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'luca@test.com'),      'CONFIRMED', 'https://meet.jit.si/Kore_luca_n1_01',     false, 1),
    ((SELECT id FROM users WHERE email = 'nutri1@test.com'), (CURRENT_DATE + 6 + TIME '14:00:00')::timestamp,  (CURRENT_DATE + 6 + TIME '14:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'matteo@test.com'),    'CONFIRMED', 'https://meet.jit.si/Kore_matteo_n1_01',   false, 1),
    ((SELECT id FROM users WHERE email = 'nutri1@test.com'), (CURRENT_DATE + 8 + TIME '14:00:00')::timestamp,  (CURRENT_DATE + 8 + TIME '14:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'davide@test.com'),    'CONFIRMED', 'https://meet.jit.si/Kore_davide_n1_01',   false, 1),
    ((SELECT id FROM users WHERE email = 'nutri1@test.com'), (CURRENT_DATE + 10 + TIME '14:00:00')::timestamp, (CURRENT_DATE + 10 + TIME '14:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'alessio@test.com'),   'CONFIRMED', 'https://meet.jit.si/Kore_alessio_n1_01',  false, 1),
    ((SELECT id FROM users WHERE email = 'nutri1@test.com'), (CURRENT_DATE + 12 + TIME '14:00:00')::timestamp, (CURRENT_DATE + 12 + TIME '14:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'roberto@test.com'),   'CONFIRMED', 'https://meet.jit.si/Kore_roberto_n1_01',  false, 1),
    ((SELECT id FROM users WHERE email = 'nutri1@test.com'), (CURRENT_DATE + 14 + TIME '14:00:00')::timestamp, (CURRENT_DATE + 14 + TIME '14:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'giulia.c@test.com'),  'CONFIRMED', 'https://meet.jit.si/Kore_giuliac_n1_01',  false, 1)
ON CONFLICT (professional_id, start_time) DO UPDATE SET
    booked_by_id  = EXCLUDED.booked_by_id,
    status        = EXCLUDED.status,
    meeting_link  = EXCLUDED.meeting_link,
    reminder_sent = EXCLUDED.reminder_sent,
    version       = EXCLUDED.version;

-- NUTRI2
INSERT INTO slots (professional_id, start_time, end_time, booked_by_id, status, meeting_link, reminder_sent, version)
VALUES
    ((SELECT id FROM users WHERE email = 'nutri2@test.com'), (CURRENT_DATE + 4 + TIME '09:00:00')::timestamp,  (CURRENT_DATE + 4 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'sofia@test.com'),     'CONFIRMED', 'https://meet.jit.si/Kore_sofia_n2_01',    false, 1),
    ((SELECT id FROM users WHERE email = 'nutri2@test.com'), (CURRENT_DATE + 6 + TIME '09:00:00')::timestamp,  (CURRENT_DATE + 6 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'chiara@test.com'),    'CONFIRMED', 'https://meet.jit.si/Kore_chiara_n2_01',   false, 1),
    ((SELECT id FROM users WHERE email = 'nutri2@test.com'), (CURRENT_DATE + 8 + TIME '09:00:00')::timestamp,  (CURRENT_DATE + 8 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'valentina@test.com'), 'CONFIRMED', 'https://meet.jit.si/Kore_valentina_n2_01',false, 1),
    ((SELECT id FROM users WHERE email = 'nutri2@test.com'), (CURRENT_DATE + 10 + TIME '09:00:00')::timestamp, (CURRENT_DATE + 10 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'francesca@test.com'), 'CONFIRMED', 'https://meet.jit.si/Kore_francesca_n2_01',false, 1),
    ((SELECT id FROM users WHERE email = 'nutri2@test.com'), (CURRENT_DATE + 12 + TIME '09:00:00')::timestamp, (CURRENT_DATE + 12 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'irene@test.com'),     'CONFIRMED', 'https://meet.jit.si/Kore_irene_n2_01',    false, 1),
    ((SELECT id FROM users WHERE email = 'nutri2@test.com'), (CURRENT_DATE + 14 + TIME '09:00:00')::timestamp, (CURRENT_DATE + 14 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'antonio@test.com'),   'CONFIRMED', 'https://meet.jit.si/Kore_antonio_n2_01',  false, 1),
    ((SELECT id FROM users WHERE email = 'nutri2@test.com'), (CURRENT_DATE + 16 + TIME '09:00:00')::timestamp, (CURRENT_DATE + 16 + TIME '09:30:00')::timestamp,
     (SELECT id FROM users WHERE email = 'marina@test.com'),    'CONFIRMED', 'https://meet.jit.si/Kore_marina_n2_01',   false, 1)
ON CONFLICT (professional_id, start_time) DO UPDATE SET
    booked_by_id  = EXCLUDED.booked_by_id,
    status        = EXCLUDED.status,
    meeting_link  = EXCLUDED.meeting_link,
    reminder_sent = EXCLUDED.reminder_sent,
    version       = EXCLUDED.version;

-- ============================================================
-- booked_at
-- ============================================================
UPDATE slots SET booked_at = start_time - INTERVAL '3 days' WHERE status = 'COMPLETED';
UPDATE slots SET booked_at = NOW()                           WHERE status = 'CONFIRMED';

-- ============================================================
-- Recensioni (solo per slot COMPLETED)
-- ============================================================
INSERT INTO reviews (client_id, professional_id, rating, comment, created_at)
VALUES
    ((SELECT id FROM users WHERE email = 'luca@test.com'),        (SELECT id FROM users WHERE email = 'pt1@test.com'),    5, 'Marco è eccezionale, consigliatissimo!',            NOW() - INTERVAL '9 days'),
    ((SELECT id FROM users WHERE email = 'sofia@test.com'),       (SELECT id FROM users WHERE email = 'pt1@test.com'),    4, 'Ottima preparazione atletica, molto professionale.',  NOW() - INTERVAL '7 days'),
    ((SELECT id FROM users WHERE email = 'testreview@test.com'),  (SELECT id FROM users WHERE email = 'pt1@test.com'),    5, 'Sessioni molto efficaci, super consigliato.',         NOW() - INTERVAL '5 days'),
    ((SELECT id FROM users WHERE email = 'elena@test.com'),       (SELECT id FROM users WHERE email = 'pt1@test.com'),    4, 'Marco sa motivare e far lavorare bene.',              NOW() - INTERVAL '4 days'),
    ((SELECT id FROM users WHERE email = 'matteo@test.com'),      (SELECT id FROM users WHERE email = 'pt2@test.com'),    5, 'Giulia è bravissima nel powerlifting!',               NOW() - INTERVAL '9 days'),
    ((SELECT id FROM users WHERE email = 'chiara@test.com'),      (SELECT id FROM users WHERE email = 'pt2@test.com'),    4, 'Molto seria e preparata.',                            NOW() - INTERVAL '7 days'),
    ((SELECT id FROM users WHERE email = 'davide@test.com'),      (SELECT id FROM users WHERE email = 'pt2@test.com'),    5, 'Ottimo PT, percorso personalizzato al top.',          NOW() - INTERVAL '5 days'),
    ((SELECT id FROM users WHERE email = 'luca@test.com'),        (SELECT id FROM users WHERE email = 'nutri1@test.com'), 5, 'Laura ha rivoluzionato la mia alimentazione.',        NOW() - INTERVAL '8 days'),
    ((SELECT id FROM users WHERE email = 'matteo@test.com'),      (SELECT id FROM users WHERE email = 'nutri1@test.com'), 4, 'Piano alimentare ottimo, mi trovo bene.',             NOW() - INTERVAL '6 days'),
    ((SELECT id FROM users WHERE email = 'testreview@test.com'),  (SELECT id FROM users WHERE email = 'nutri1@test.com'), 5, 'Consigli pratici e piani adattati.',                  NOW() - INTERVAL '4 days'),
    ((SELECT id FROM users WHERE email = 'sofia@test.com'),       (SELECT id FROM users WHERE email = 'nutri2@test.com'), 4, 'Andrea conosce bene le intolleranze.',                NOW() - INTERVAL '8 days'),
    ((SELECT id FROM users WHERE email = 'chiara@test.com'),      (SELECT id FROM users WHERE email = 'nutri2@test.com'), 5, 'Dieta personalizzata e ottimi risultati.',            NOW() - INTERVAL '6 days')
ON CONFLICT DO NOTHING;

-- ============================================================
-- Documenti
-- ============================================================
INSERT INTO documents (file_name, file_path, content_type, type, upload_date, owner_id, uploaded_by_id, notes)
VALUES
    ('polizza_luca.pdf',        'uploads/seed/polizza_luca.pdf',        'application/pdf', 'INSURANCE_POLICE', NOW() - INTERVAL '30 days',
     (SELECT id FROM users WHERE email = 'luca@test.com'),    (SELECT id FROM users WHERE email = 'insurance@test.com'), 'Polizza assicurativa attiva per Luca Ferri'),
    ('scheda_luca_pt1.pdf',     'uploads/seed/scheda_luca_pt1.pdf',     'application/pdf', 'WORKOUT_PLAN',     NOW() - INTERVAL '20 days',
     (SELECT id FROM users WHERE email = 'luca@test.com'),    (SELECT id FROM users WHERE email = 'pt1@test.com'),       'Scheda funzionale settimana 1-4'),
    ('dieta_luca_n1.pdf',       'uploads/seed/dieta_luca_n1.pdf',       'application/pdf', 'DIET_PLAN',        NOW() - INTERVAL '15 days',
     (SELECT id FROM users WHERE email = 'luca@test.com'),    (SELECT id FROM users WHERE email = 'nutri1@test.com'),    'Piano alimentare mediterraneo fase 1'),
    ('polizza_sofia.pdf',       'uploads/seed/polizza_sofia.pdf',       'application/pdf', 'INSURANCE_POLICE', NOW() - INTERVAL '28 days',
     (SELECT id FROM users WHERE email = 'sofia@test.com'),   (SELECT id FROM users WHERE email = 'insurance@test.com'), 'Polizza assicurativa attiva per Sofia Conti'),
    ('scheda_sofia_pt1.pdf',    'uploads/seed/scheda_sofia_pt1.pdf',    'application/pdf', 'WORKOUT_PLAN',     NOW() - INTERVAL '18 days',
     (SELECT id FROM users WHERE email = 'sofia@test.com'),   (SELECT id FROM users WHERE email = 'pt1@test.com'),       'Scheda powerlifting introduttivo'),
    ('dieta_sofia_n2.pdf',      'uploads/seed/dieta_sofia_n2.pdf',      'application/pdf', 'DIET_PLAN',        NOW() - INTERVAL '12 days',
     (SELECT id FROM users WHERE email = 'sofia@test.com'),   (SELECT id FROM users WHERE email = 'nutri2@test.com'),    'Dieta ipolipidica personalizzata'),
    ('polizza_matteo.pdf',      'uploads/seed/polizza_matteo.pdf',      'application/pdf', 'INSURANCE_POLICE', NOW() - INTERVAL '25 days',
     (SELECT id FROM users WHERE email = 'matteo@test.com'),  (SELECT id FROM users WHERE email = 'insurance@test.com'), 'Polizza assicurativa attiva per Matteo Galli'),
    ('scheda_matteo_pt2.pdf',   'uploads/seed/scheda_matteo_pt2.pdf',   'application/pdf', 'WORKOUT_PLAN',     NOW() - INTERVAL '16 days',
     (SELECT id FROM users WHERE email = 'matteo@test.com'),  (SELECT id FROM users WHERE email = 'pt2@test.com'),       'Programma forza massimale 8 settimane'),
    ('dieta_matteo_n1.pdf',     'uploads/seed/dieta_matteo_n1.pdf',     'application/pdf', 'DIET_PLAN',        NOW() - INTERVAL '10 days',
     (SELECT id FROM users WHERE email = 'matteo@test.com'),  (SELECT id FROM users WHERE email = 'nutri1@test.com'),    'Piano sportivo ad alto contenuto proteico'),
    ('polizza_chiara.pdf',      'uploads/seed/polizza_chiara.pdf',      'application/pdf', 'INSURANCE_POLICE', NOW() - INTERVAL '22 days',
     (SELECT id FROM users WHERE email = 'chiara@test.com'),  (SELECT id FROM users WHERE email = 'insurance@test.com'), 'Polizza assicurativa attiva per Chiara Fontana'),
    ('scheda_chiara_pt2.pdf',   'uploads/seed/scheda_chiara_pt2.pdf',   'application/pdf', 'WORKOUT_PLAN',     NOW() - INTERVAL '14 days',
     (SELECT id FROM users WHERE email = 'chiara@test.com'),  (SELECT id FROM users WHERE email = 'pt2@test.com'),       'Allenamento funzionale misto'),
    ('dieta_chiara_n2.pdf',     'uploads/seed/dieta_chiara_n2.pdf',     'application/pdf', 'DIET_PLAN',        NOW() - INTERVAL '8 days',
     (SELECT id FROM users WHERE email = 'chiara@test.com'),  (SELECT id FROM users WHERE email = 'nutri2@test.com'),    'Dieta per intolleranza al lattosio'),
    ('polizza_elena.pdf',       'uploads/seed/polizza_elena.pdf',       'application/pdf', 'INSURANCE_POLICE', NOW() - INTERVAL '20 days',
     (SELECT id FROM users WHERE email = 'elena@test.com'),   (SELECT id FROM users WHERE email = 'insurance@test.com'), 'Polizza assicurativa attiva per Elena Marino'),
    ('scheda_davide_pt2.pdf',   'uploads/seed/scheda_davide_pt2.pdf',   'application/pdf', 'WORKOUT_PLAN',     NOW() - INTERVAL '11 days',
     (SELECT id FROM users WHERE email = 'davide@test.com'),  (SELECT id FROM users WHERE email = 'pt2@test.com'),       'Scheda ipertrofia muscolare 12 settimane')
ON CONFLICT DO NOTHING;

-- ============================================================
-- Aggiorna crediti dopo le prenotazioni future
-- ============================================================
UPDATE subscriptions SET current_creditspt = current_creditspt - 1
WHERE user_id IN (
    SELECT id FROM users WHERE email IN
    ('luca@test.com','sofia@test.com','matteo@test.com','chiara@test.com',
     'elena@test.com','davide@test.com','valentina@test.com','francesca@test.com',
     'alessio@test.com','irene@test.com','antonio@test.com','giulia.c@test.com',
     'roberto@test.com','marina@test.com')
) AND current_creditspt > 0;

UPDATE subscriptions SET current_credits_nutri = current_credits_nutri - 1
WHERE user_id IN (
    SELECT id FROM users WHERE email IN
    ('luca@test.com','sofia@test.com','matteo@test.com','chiara@test.com',
     'davide@test.com','valentina@test.com','francesca@test.com',
     'alessio@test.com','irene@test.com','antonio@test.com','giulia.c@test.com',
     'roberto@test.com','marina@test.com')
) AND current_credits_nutri > 0;

-- ============================================================
-- Edge-case abbonamenti
-- ============================================================
UPDATE subscriptions SET active = false, end_date = CURRENT_DATE - INTERVAL '30 days'
WHERE user_id IN (SELECT id FROM users WHERE email IN ('giulia.c@test.com', 'roberto@test.com'));

UPDATE subscriptions SET current_creditspt = 0, current_credits_nutri = 0
WHERE user_id IN (SELECT id FROM users WHERE email IN ('francesca@test.com', 'alessio@test.com'));

UPDATE subscriptions SET next_payment_date = CURRENT_DATE - INTERVAL '10 days'
WHERE user_id IN (SELECT id FROM users WHERE email IN ('sofia@test.com', 'irene@test.com'))
  AND next_payment_date IS NOT NULL;

-- ============================================================
-- Chat e messaggi
-- ============================================================
INSERT INTO chats (user1_id, user2_id, created_at, status)
VALUES ((SELECT id FROM users WHERE email = 'luca@test.com'), (SELECT id FROM users WHERE email = 'moderator1@test.com'), NOW() - INTERVAL '5 days', 'OPEN')
ON CONFLICT DO NOTHING;

INSERT INTO chats (user1_id, user2_id, created_at, status)
VALUES ((SELECT id FROM users WHERE email = 'matteo@test.com'), (SELECT id FROM users WHERE email = 'pt2@test.com'), NOW() - INTERVAL '3 days', 'OPEN')
ON CONFLICT DO NOTHING;

INSERT INTO chats (user1_id, user2_id, created_at, status)
VALUES ((SELECT id FROM users WHERE email = 'chiara@test.com'), (SELECT id FROM users WHERE email = 'admin@test.com'), NOW() - INTERVAL '2 days', 'OPEN')
ON CONFLICT DO NOTHING;

INSERT INTO messages (content, time_stamp, status, sent_by_user1, chat_id)
VALUES
    ('Ciao, avrei bisogno di assistenza con il mio abbonamento.',
     NOW() - INTERVAL '5 days', 'READ', true,
     (SELECT id FROM chats WHERE user1_id = (SELECT id FROM users WHERE email = 'luca@test.com')
                              AND user2_id = (SELECT id FROM users WHERE email = 'moderator1@test.com'))),
    ('Certo, come posso aiutarti?',
     NOW() - INTERVAL '5 days' + INTERVAL '10 minutes', 'READ', false,
     (SELECT id FROM chats WHERE user1_id = (SELECT id FROM users WHERE email = 'luca@test.com')
                              AND user2_id = (SELECT id FROM users WHERE email = 'moderator1@test.com'))),
    ('Ho un problema con la prenotazione dello slot di giovedì.',
     NOW() - INTERVAL '4 days', 'SENT', true,
     (SELECT id FROM chats WHERE user1_id = (SELECT id FROM users WHERE email = 'luca@test.com')
                              AND user2_id = (SELECT id FROM users WHERE email = 'moderator1@test.com')));

INSERT INTO messages (content, time_stamp, status, sent_by_user1, chat_id)
VALUES
    ('Ciao Giulia, posso spostare la sessione di martedì?',
     NOW() - INTERVAL '3 days', 'READ', true,
     (SELECT id FROM chats WHERE user1_id = (SELECT id FROM users WHERE email = 'matteo@test.com')
                              AND user2_id = (SELECT id FROM users WHERE email = 'pt2@test.com'))),
    ('Certo Matteo, quando preferiresti?',
     NOW() - INTERVAL '3 days' + INTERVAL '30 minutes', 'SENT', false,
     (SELECT id FROM chats WHERE user1_id = (SELECT id FROM users WHERE email = 'matteo@test.com')
                              AND user2_id = (SELECT id FROM users WHERE email = 'pt2@test.com')));

INSERT INTO messages (content, time_stamp, status, sent_by_user1, chat_id)
VALUES
    ('Salve, vorrei richiedere informazioni sui piani disponibili.',
     NOW() - INTERVAL '2 days', 'READ', true,
     (SELECT id FROM chats WHERE user1_id = (SELECT id FROM users WHERE email = 'chiara@test.com')
                              AND user2_id = (SELECT id FROM users WHERE email = 'admin@test.com'))),
    ('Buongiorno Chiara! Le invio a breve il catalogo aggiornato.',
     NOW() - INTERVAL '2 days' + INTERVAL '15 minutes', 'SENT', false,
     (SELECT id FROM chats WHERE user1_id = (SELECT id FROM users WHERE email = 'chiara@test.com')
                              AND user2_id = (SELECT id FROM users WHERE email = 'admin@test.com')));
