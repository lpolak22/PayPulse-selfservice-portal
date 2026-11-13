SELECT id_organisation, name
FROM organisation
WHERE name = 'Zagreb Retail Group d.o.o.';

SELECT t.raw_data ->> 'rrn' AS rrn, t.raw_data ->> 'auth_code' AS auth_code
FROM transaction t
         JOIN terminal te ON te.id_terminal = t.id_terminal
         JOIN outlet o ON o.id_outlet = te.id_outlet
         JOIN merchant m ON m.id_merchant = o.id_merchant
WHERE m.id_organisation = :org_id
  AND t.raw_data ->> 'rrn' IS NOT NULL
  AND t.raw_data ->> 'auth_code' IS NOT NULL
LIMIT 5;

EXPLAIN (ANALYZE, BUFFERS)
SELECT t.id_transaction AS id,
       t.transaction_at AS transaction_at,
       t.amount AS amount,
       t.currency AS currency,
       t.status AS status,
       t.raw_data ->> 'type' AS type,
       t.raw_data ->> 'card_brand' AS card_brand,
       t.raw_data ->> 'masked_pan' AS masked_pan,
       t.raw_data ->> 'sales_channel' AS sales_channel,
       o.name AS outlet_name,
       te.terminal_id AS terminal_id,
       t.raw_data ->> 'auth_code' AS auth_code,
       t.raw_data ->> 'rrn' AS rrn
FROM transaction t
         JOIN terminal te ON te.id_terminal = t.id_terminal
         JOIN outlet o ON o.id_outlet = te.id_outlet
         JOIN merchant m ON m.id_merchant = o.id_merchant
WHERE m.id_organisation = :org_id
  AND t.transaction_at >= now() - INTERVAL '7 days'
  AND t.transaction_at < now()
ORDER BY t.transaction_at DESC, t.id_transaction DESC
LIMIT 20 OFFSET 0;

EXPLAIN (ANALYZE, BUFFERS)
SELECT t.id_transaction AS id,
       t.transaction_at AS transaction_at,
       t.amount AS amount,
       t.currency AS currency,
       t.status AS status,
       t.raw_data ->> 'type' AS type,
       t.raw_data ->> 'card_brand' AS card_brand,
       t.raw_data ->> 'masked_pan' AS masked_pan,
       t.raw_data ->> 'sales_channel' AS sales_channel,
       o.name AS outlet_name,
       te.terminal_id AS terminal_id,
       t.raw_data ->> 'auth_code' AS auth_code,
       t.raw_data ->> 'rrn' AS rrn
FROM transaction t
         JOIN terminal te ON te.id_terminal = t.id_terminal
         JOIN outlet o ON o.id_outlet = te.id_outlet
         JOIN merchant m ON m.id_merchant = o.id_merchant
WHERE m.id_organisation = :org_id
  AND t.transaction_at >= now() - INTERVAL '13 months' + INTERVAL '1 day'
  AND t.transaction_at < now()
  AND t.status IN ('APPROVED')
  AND (t.raw_data @> CAST('{"card_brand":"VISA"}' AS jsonb))
ORDER BY t.transaction_at DESC, t.id_transaction DESC
LIMIT 20 OFFSET 0;

EXPLAIN (ANALYZE, BUFFERS)
SELECT COUNT(*)
FROM transaction t
         JOIN terminal te ON te.id_terminal = t.id_terminal
         JOIN outlet o ON o.id_outlet = te.id_outlet
         JOIN merchant m ON m.id_merchant = o.id_merchant
WHERE m.id_organisation = :org_id
  AND t.transaction_at >= now() - INTERVAL '13 months' + INTERVAL '1 day'
  AND t.transaction_at < now()
  AND t.status IN ('APPROVED')
  AND (t.raw_data @> CAST('{"card_brand":"VISA"}' AS jsonb));

EXPLAIN (ANALYZE, BUFFERS)
SELECT t.id_transaction AS id,
       t.transaction_at AS transaction_at,
       t.amount AS amount,
       t.currency AS currency,
       t.status AS status,
       t.raw_data ->> 'type' AS type,
       t.raw_data ->> 'card_brand' AS card_brand,
       t.raw_data ->> 'masked_pan' AS masked_pan,
       t.raw_data ->> 'sales_channel' AS sales_channel,
       o.name AS outlet_name,
       te.terminal_id AS terminal_id,
       t.raw_data ->> 'auth_code' AS auth_code,
       t.raw_data ->> 'rrn' AS rrn
FROM transaction t
         JOIN terminal te ON te.id_terminal = t.id_terminal
         JOIN outlet o ON o.id_outlet = te.id_outlet
         JOIN merchant m ON m.id_merchant = o.id_merchant
WHERE m.id_organisation = :org_id
  AND t.transaction_at >= now() - INTERVAL '13 months' + INTERVAL '1 day'
  AND t.transaction_at < now()
  AND t.raw_data ->> 'rrn' = :rrn
ORDER BY t.transaction_at DESC, t.id_transaction DESC
LIMIT 20 OFFSET 0;

EXPLAIN (ANALYZE, BUFFERS)
SELECT t.id_transaction AS id,
       t.transaction_at AS transaction_at,
       t.amount AS amount,
       t.currency AS currency,
       t.status AS status,
       t.raw_data ->> 'type' AS type,
       t.raw_data ->> 'card_brand' AS card_brand,
       t.raw_data ->> 'masked_pan' AS masked_pan,
       t.raw_data ->> 'sales_channel' AS sales_channel,
       o.name AS outlet_name,
       te.terminal_id AS terminal_id,
       t.raw_data ->> 'auth_code' AS auth_code,
       t.raw_data ->> 'rrn' AS rrn
FROM transaction t
         JOIN terminal te ON te.id_terminal = t.id_terminal
         JOIN outlet o ON o.id_outlet = te.id_outlet
         JOIN merchant m ON m.id_merchant = o.id_merchant
WHERE m.id_organisation = :org_id
  AND t.transaction_at >= now() - INTERVAL '13 months' + INTERVAL '1 day'
  AND t.transaction_at < now()
  AND t.raw_data ->> 'auth_code' = :auth_code
ORDER BY t.transaction_at DESC, t.id_transaction DESC
LIMIT 20 OFFSET 0;
