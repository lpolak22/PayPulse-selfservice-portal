#!/usr/bin/env bash
# Smoke test for PULSE-34 (user CRUD API) against a locally running app started with the "dev" profile.
# Safe to re-run: every run creates users with unique emails.
# Usage: bash scripts/smoke-test-users.sh     (BASE_URL defaults to http://localhost:8080)

BASE_URL=${BASE_URL:-http://localhost:8080}
BASE=$BASE_URL/api/users
PW='PayPulse-Dev-2026!'   # dev password of the seeded users, see db/seed/R__dev_seed_data.sql
ADMIN="ana.horvat@paypulse-demo.example:$PW"
OTHER_ORG_ADMIN="nikola.peric@adriatic-retail.example:$PW"
PLAIN_USER="ivana.babic@paypulse-demo.example:$PW"
RUN=$(date +%s)
PASSED=0; FAILED=0

call() { # label, expected status, curl args...
  local label=$1 expected=$2; shift 2
  local out status
  out=$(curl -s -w $'\n%{http_code}' "$@")
  status=${out##*$'\n'}; LAST_BODY=${out%$'\n'*}
  if [ "$status" = "$expected" ]; then PASSED=$((PASSED + 1)); mark=PASS; else FAILED=$((FAILED + 1)); mark=FAIL; fi
  printf '%s  %-56s %s\n' "$mark" "$label" "$status"
  [ -n "$LAST_BODY" ] && printf '      %s\n' "$(echo "$LAST_BODY" | cut -c1-200)"
}
body_has() { # label, text the last response body must contain
  if [[ "$LAST_BODY" == *"$2"* ]]; then PASSED=$((PASSED + 1)); mark=PASS; else FAILED=$((FAILED + 1)); mark=FAIL; fi
  printf '%s    %-54s\n' "$mark" "$1"
}
body_lacks() { # label, text the last response body must not contain
  if [[ "$LAST_BODY" != *"$2"* ]]; then PASSED=$((PASSED + 1)); mark=PASS; else FAILED=$((FAILED + 1)); mark=FAIL; fi
  printf '%s    %-54s\n' "$mark" "$1"
}
post()  { call "$1" "$2" -u "$ADMIN" -H 'Content-Type: application/json' -d "$3" "$BASE"; }
patch() { call "$1" "$2" -u "$ADMIN" -X PATCH -H 'Content-Type: application/json' -d "$3" "$BASE/$NEW_ID"; }
id_of() { echo "$1" | grep -oE '"id":[0-9]+' | head -1 | cut -d: -f2; }

echo "== Security"
call "No credentials -> 401" 401 "$BASE"
body_has "error code UNAUTHORIZED" '"code":"UNAUTHORIZED"'
call "Role USER -> 403" 403 -u "$PLAIN_USER" "$BASE"
body_has "error code FORBIDDEN" '"code":"FORBIDDEN"'

echo "== GET /api/users (list, search, filters, paging)"
call "Default page: undeleted users of own organisation" 200 -u "$ADMIN" "$BASE"
body_lacks "no users of the other organisation" 'Adriatic Retail'
call "search=KOV (name, any case)" 200 -u "$ADMIN" "$BASE?search=KOV"
body_has "finds Kovačević" '"lastName":"Kova'
call "search=ana horvat (full name)" 200 -u "$ADMIN" "$BASE?search=ana%20horvat"
body_has "finds Ana Horvat" '"email":"ana.horvat@paypulse-demo.example"'
call "search=filip.pavic@ (email)" 200 -u "$ADMIN" "$BASE?search=filip.pavic@"
body_has "finds Filip Pavić" '"email":"filip.pavic@paypulse-demo.example"'
call "role=ADMIN&status=ACTIVE" 200 -u "$ADMIN" "$BASE?role=ADMIN&status=ACTIVE"
body_has "returns only active admins" '"role":"ADMIN","status":"ACTIVE"'
call "page=1&pageSize=3" 200 -u "$ADMIN" "$BASE?page=1&pageSize=3"
body_has "second page of size 3" '"page":1,"pageSize":3'
call "pageSize=500" 200 -u "$ADMIN" "$BASE?pageSize=500"
body_has "page size is capped at 100" '"pageSize":100'
call "role=BOSS -> 400" 400 -u "$ADMIN" "$BASE?role=BOSS"
body_has "error code BAD_REQUEST" '"code":"BAD_REQUEST"'

echo "== POST /api/users"
EMAIL="nina.smoke.$RUN@paypulse-demo.example"
post "Valid user -> 201" 201 \
  "{\"firstName\":\"Nina\",\"lastName\":\"Smoke\",\"email\":\"$EMAIL\",\"phone\":\"+385911110000\",\"role\":\"USER\"}"
NEW_ID=$(id_of "$LAST_BODY")
body_has "status is PENDING_ACTIVATION and inactive" '"status":"PENDING_ACTIVATION","active":false'
post "Same email in upper case -> 409" 409 \
  "{\"firstName\":\"Nina\",\"lastName\":\"Dup\",\"email\":\"${EMAIL^^}\",\"role\":\"USER\"}"
body_has "error code USER_EMAIL_EXISTS" '"code":"USER_EMAIL_EXISTS"'
post "Email used in another organisation -> 409" 409 \
  '{"firstName":"X","lastName":"Y","email":"nikola.peric@adriatic-retail.example","role":"USER"}'
post "Invalid email + phone -> 400" 400 \
  '{"firstName":"A","lastName":"B","email":"not-an-email","phone":"091 123 4567","role":"USER"}'
body_has "error code VALIDATION_ERROR" '"code":"VALIDATION_ERROR"'
body_has "field error for email" '"field":"email"'
body_has "field error for phone" '"field":"phone"'
post "Missing required fields -> 400" 400 '{}'
body_has "field error for firstName" '"field":"firstName"'
post "Unknown role -> 400" 400 \
  '{"firstName":"A","lastName":"B","email":"a.b@paypulse-demo.example","role":"SUPERUSER"}'
body_has "error code BAD_REQUEST" '"code":"BAD_REQUEST"'
post "Malformed JSON -> 400" 400 '{"firstName":'
body_has "error code BAD_REQUEST" '"code":"BAD_REQUEST"'

echo "== GET /api/users/{id}"
call "Created user" 200 -u "$ADMIN" "$BASE/$NEW_ID"
OTHER_ID=$(id_of "$(curl -s -u "$OTHER_ORG_ADMIN" "$BASE?search=nikola")")
call "User of another organisation -> 404" 404 -u "$ADMIN" "$BASE/$OTHER_ID"
body_has "error code NOT_FOUND" '"code":"NOT_FOUND"'
call "Unknown id -> 404" 404 -u "$ADMIN" "$BASE/999999"

echo "== PATCH /api/users/{id}"
patch "Activate + rename" 200 '{"status":"ACTIVE","lastName":"Smoke-Tested"}'
body_has "renamed" '"lastName":"Smoke-Tested"'
body_has "is_active follows status ACTIVE" '"status":"ACTIVE","active":true'
patch "Deactivate" 200 '{"status":"INACTIVE"}'
body_has "is_active follows status INACTIVE" '"status":"INACTIVE","active":false'
patch "Own email in other case -> 200" 200 "{\"email\":\"${EMAIL^^}\"}"
patch "Email of another user -> 409" 409 '{"email":"ANA.HORVAT@paypulse-demo.example"}'
body_has "error code USER_EMAIL_EXISTS" '"code":"USER_EMAIL_EXISTS"'
patch "Invalid phone + blank name -> 400" 400 '{"phone":"12345","firstName":"  "}'
body_has "field errors for phone and firstName" '"field":"phone"'
patch "Back to PENDING_ACTIVATION -> 400" 400 '{"status":"PENDING_ACTIVATION"}'
body_has "error code VALIDATION_ERROR" '"code":"VALIDATION_ERROR"'

echo "== DELETE /api/users/{id} (soft delete)"
call "Delete -> 204" 204 -u "$ADMIN" -X DELETE "$BASE/$NEW_ID"
call "Get deleted user -> 404" 404 -u "$ADMIN" "$BASE/$NEW_ID"
call "Delete again -> 404" 404 -u "$ADMIN" -X DELETE "$BASE/$NEW_ID"
call "Deleted user not listed" 200 -u "$ADMIN" "$BASE?search=$EMAIL"
body_has "list is empty" '"total":0'
post "Email of deleted user can be reused -> 201" 201 \
  "{\"firstName\":\"Nina\",\"lastName\":\"Again\",\"email\":\"$EMAIL\",\"role\":\"USER\"}"

echo "== OpenAPI"
call "API docs (/api/docs/openapi.json)" 200 "$BASE_URL/api/docs/openapi.json"
body_has "documents the basic auth scheme" '"basicAuth"'
body_has "documents the ErrorResponse schema" '"ErrorResponse"'
echo "      operations: $(echo "$LAST_BODY" | grep -oE '"operationId":"[a-zA-Z]+"' | cut -d'"' -f4 | tr '\n' ' ')"
call "Swagger UI (/api/docs)" 200 -o /dev/null -L "$BASE_URL/api/docs"

echo
echo "Passed: $PASSED  Failed: $FAILED  (user id under test: $NEW_ID, email: $EMAIL)"
[ "$FAILED" -eq 0 ]
