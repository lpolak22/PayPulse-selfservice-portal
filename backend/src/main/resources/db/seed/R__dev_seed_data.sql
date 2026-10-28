INSERT INTO organisation (name, status)
SELECT v.name, v.status
FROM (VALUES ('PayPulse Demo d.o.o.', 'ACTIVE'),
             ('Adriatic Retail j.d.o.o.', 'ACTIVE')) AS v (name, status)
WHERE NOT EXISTS (SELECT 1 FROM organisation o WHERE o.name = v.name);

WITH dev_password AS (SELECT '$2a$10$QBaArHbfvBXGkYwuYlGwYuPWh/KLVnovtTVpYZB94lUTS3tbMLcA6' AS hash)
INSERT INTO app_user (id_organisation, first_name, last_name, email, phone, role, status,
                      password_hash, is_active, is_first_login, deleted_at)
SELECT o.id_organisation,
       v.first_name,
       v.last_name,
       v.email,
       v.phone,
       v.role,
       v.status,
       CASE WHEN v.has_password THEN p.hash END,
       v.status = 'ACTIVE' AND v.deleted_at IS NULL,
       NOT v.has_password,
       v.deleted_at
FROM (VALUES
          ('PayPulse Demo d.o.o.', 'Ana', 'Horvat', 'ana.horvat@paypulse-demo.example', '+385911234567', 'ADMIN', 'ACTIVE', true, NULL::timestamp),
          ('PayPulse Demo d.o.o.', 'Marko', 'Kovačević', 'marko.kovacevic@paypulse-demo.example', '+385921112233', 'ADMIN', 'ACTIVE', true, NULL),
          ('PayPulse Demo d.o.o.', 'Sara', 'Grgić', 'sara.grgic@paypulse-demo.example', '+385917778899', 'ADMIN', 'PENDING_ACTIVATION', false, NULL),
          ('PayPulse Demo d.o.o.', 'Ivana', 'Babić', 'ivana.babic@paypulse-demo.example', '+385981234000', 'USER', 'ACTIVE', true, NULL),
          ('PayPulse Demo d.o.o.', 'Josip', 'Vuković', 'josip.vukovic@paypulse-demo.example', '+38514567890', 'USER', 'ACTIVE', true, NULL),
          ('PayPulse Demo d.o.o.', 'Katarina', 'Novak', 'katarina.novak@paypulse-demo.example', '+385976543210', 'USER', 'ACTIVE', true, NULL),
          ('PayPulse Demo d.o.o.', 'Luka', 'Marić', 'luka.maric@paypulse-demo.example', '+385915550101', 'USER', 'PENDING_ACTIVATION', false, NULL),
          ('PayPulse Demo d.o.o.', 'Maja', 'Knežević', 'maja.knezevic@paypulse-demo.example', '+385958887766', 'USER', 'PENDING_ACTIVATION', false, NULL),
          ('PayPulse Demo d.o.o.', 'Petra', 'Jurić', 'petra.juric@paypulse-demo.example', NULL, 'USER', 'INACTIVE', true, NULL),
          ('PayPulse Demo d.o.o.', 'Filip', 'Pavić', 'filip.pavic@paypulse-demo.example', '+385993334455', 'USER', 'INACTIVE', true, NULL),
          ('PayPulse Demo d.o.o.', 'Tomislav', 'Božić', 'tomislav.bozic@paypulse-demo.example', '+385912223344', 'USER', 'ACTIVE', true, (CURRENT_TIMESTAMP - INTERVAL '10 days')::timestamp),
          ('Adriatic Retail j.d.o.o.', 'Nikola', 'Perić', 'nikola.peric@adriatic-retail.example', '+385914445566', 'ADMIN', 'ACTIVE', true, NULL),
          ('Adriatic Retail j.d.o.o.', 'Ema', 'Pavlović', 'ema.pavlovic@adriatic-retail.example', '+385955556677', 'USER', 'ACTIVE', true, NULL)
     ) AS v (organisation_name, first_name, last_name, email, phone, role, status, has_password, deleted_at)
         JOIN organisation o ON o.name = v.organisation_name
         CROSS JOIN dev_password p
WHERE NOT EXISTS (SELECT 1 FROM app_user u WHERE lower(u.email) = lower(v.email));
