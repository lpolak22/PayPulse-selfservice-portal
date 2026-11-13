# Transaction search: load test and query plans

Task: PULSE-71. Target from the AC: 50 concurrent users for 10 minutes, 95% of searches under 2 seconds.

## 1. Prepare the backend

From `backend/` in PowerShell, with `.env` in place:

```powershell
.\gradlew.bat refreshDb
.\gradlew.bat seed
.\gradlew.bat bootRun
```

The seed creates `SEED_TRANSACTION_COUNT` transactions (default 100000). Write down the count you used next to the result,
because the expected production volume (PULSE-25) is still an open question on the AC page.

## 2. Run the k6 load test

Open a second PowerShell window in `backend/` while `bootRun` is running.

Trial run (1 minute):

```powershell
k6 run -e DURATION=1m load-test/transaction-search.js
```

Full run (10 minutes, the AC target):

```powershell
k6 run load-test/transaction-search.js
```

Other user or server:

```powershell
k6 run -e BASE_URL=http://localhost:8080 -e PP_EMAIL=admin1.zagreb-retail-group@paypulse.test -e PP_PASSWORD='Password123!' load-test/transaction-search.js
```

| Variable | Default |
| --- | --- |
| `BASE_URL` | `http://localhost:8080` |
| `PP_EMAIL` | `user1.zagreb-retail-group@paypulse.test` |
| `PP_PASSWORD` | `Password123!` (seed password) |
| `DURATION` | `10m` |
| `RESULTS_DIR` | `load-test/results` |

The script logs in once in `setup()`. The access token lives 15 minutes (`JWT_ACCESS_TOKEN_TTL`), so keep `DURATION`
under about 14 minutes.

The query mix: default 7 days, last 30 days, about 13 months (13 months minus one day, so a daylight saving change never
pushes it over the limit), random status/type/card brand/sales channel filters, rrn and auth code exact match, terminal
filter, random sort, page 0 to 5 and page size 20/50/100. The rrn, auth code and terminal values are taken from a real
search in `setup()`.

### Reading the result

k6 prints the summary and writes two files to `load-test/results/` (ignored by Git):

- `transaction-search-<time>.txt`: the same summary as on screen; attach this to PULSE-71.
- `transaction-search-<time>.json`: all numbers.

The run passes when every threshold has a green check:

- `http_req_duration p(95)<2000`: 95% of requests under 2 s.
- `http_req_failed rate<0.01`: under 1% failed requests.
- `http_req_duration{query:...}`: p(95) per query type, so you can see which query is the slowest.

If a threshold fails, k6 exits with a non-zero code. In that case the AC says to report the results and the slowest
queries, and the task stays open.

## 3. Check the query plans (EXPLAIN)

`explain-transaction-search.sql` holds the same SQL that `TransactionSearchSql` builds for four typical searches:
default 7 days, 13 months with status + card brand (page and count), rrn exact match and auth code exact match.
`EXPLAIN (ANALYZE, BUFFERS)` really runs the query and shows the plan PostgreSQL chose, with times.

In IntelliJ:

1. Open the Database tool window, connect to `paypulse_db` (the seeded database, not `paypulse_test`).
2. Open `backend/load-test/explain-transaction-search.sql`, choose that data source for the file.
3. Run the first `SELECT` to get the organisation id of Zagreb Retail Group:
   ```sql
   SELECT id_organisation, name FROM organisation WHERE name = 'Zagreb Retail Group d.o.o.';
   ```
4. Run the second `SELECT` to get a real `rrn` and `auth_code` of that organisation.
5. Put the cursor on each `EXPLAIN` statement and run it (Ctrl+Enter). IntelliJ asks for `:org_id`, `:rrn` and
   `:auth_code`; enter the values from steps 3 and 4 (text values in single quotes, for example `'123456789012'`).
   If it does not ask, replace them in the file by hand and do not commit that change.

What to look for in each plan:

| Query | Good sign | Bad sign |
| --- | --- | --- |
| Default 7 days | `Index Scan` or `Bitmap Index Scan` on `ix_transaction_transaction_at` or `ix_transaction_terminal_transaction_at` | `Seq Scan on transaction` |
| 13 months + status + card brand | `Bitmap Index Scan on ix_transaction_raw_data` (the GIN index) and/or a `transaction_at` index | `Seq Scan on transaction` with a large `Rows Removed by Filter` |
| rrn exact | `Index Scan` or `Bitmap Index Scan on ix_transaction_rrn` | `Seq Scan on transaction` |
| auth code exact | `Index Scan` or `Bitmap Index Scan on ix_transaction_auth_code` | `Seq Scan on transaction` |

Also note `Execution Time` at the bottom of each plan and `Buffers: shared read` (pages read from disk).

A `Seq Scan` on the small tables (`merchant`, `outlet`, `terminal`) is fine. On `transaction` it is only acceptable when
the filter matches a large part of the table (for example 13 months with a common card brand on a small seed), because
then reading the whole table is cheaper than using an index. Check `Execution Time`: if it stays well under 2 s it is not
a problem, but write it down in the PULSE-71 result. Run `ANALYZE transaction;` once after seeding if the plan looks
odd, so PostgreSQL has fresh statistics.
