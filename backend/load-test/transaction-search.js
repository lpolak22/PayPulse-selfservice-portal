import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.1.0/index.js';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const EMAIL = __ENV.PP_EMAIL || 'user1.zagreb-retail-group@paypulse.test';
const PASSWORD = __ENV.PP_PASSWORD || 'Password123!';
const RESULTS_DIR = __ENV.RESULTS_DIR || 'load-test/results';
const DAY_MS = 24 * 60 * 60 * 1000;

const STATUSES = ['APPROVED', 'DECLINED', 'REFUNDED', 'CANCELLED'];
const TYPES = ['SALE', 'SALE_WITH_INSTALLMENTS', 'REFUND', 'STORNO', 'TECHNICAL_REVERSAL'];
const CARD_BRANDS = ['VISA', 'MASTERCARD', 'MAESTRO', 'DINERS', 'AMEX'];
const SALES_CHANNELS = ['POS', 'ECOM', 'MOTO', 'CONTACTLESS'];

const QUERIES = [
    { name: 'default_7_days', weight: 25, build: () => [] },
    { name: 'last_30_days', weight: 20, build: () => daysBack(30) },
    { name: 'thirteen_months', weight: 10, build: () => thirteenMonthsBack() },
    { name: 'filters_30_days', weight: 15, build: () => daysBack(30).concat(randomFilters()) },
    { name: 'filters_13_months', weight: 10, build: () => thirteenMonthsBack().concat(statusAndBrand()) },
    { name: 'rrn_exact', weight: 7, build: (data) => thirteenMonthsBack().concat(exact('rrn', data.rrns)) },
    { name: 'auth_code_exact', weight: 7, build: (data) => thirteenMonthsBack().concat(exact('authCode', data.authCodes)) },
    { name: 'terminal_30_days', weight: 6, build: (data) => daysBack(30).concat(exact('terminalId', data.terminalIds)) },
];

export const options = {
    vus: 50,
    duration: __ENV.DURATION || '10m',
    thresholds: Object.assign({
        http_req_duration: ['p(95)<2000'],
        http_req_failed: ['rate<0.01'],
        checks: ['rate>0.99'],
    }, perQueryThresholds()),
};

export function setup() {
    const login = http.post(`${BASE_URL}/api/auth/login`, JSON.stringify({ email: EMAIL, password: PASSWORD }),
        { headers: { 'Content-Type': 'application/json' }, tags: { query: 'setup' } });
    if (login.status !== 200) {
        fail(`Login failed with status ${login.status}`);
    }
    const token = login.json('accessToken');
    const sample = http.get(`${BASE_URL}/api/transactions?${toQueryString(daysBack(30).concat([['pageSize', 100]]))}`,
        { headers: authHeaders(token), tags: { query: 'setup' } });
    if (sample.status !== 200) {
        fail(`Sample search failed with status ${sample.status}`);
    }
    return Object.assign({ token }, sampleValues(sample.json('items')));
}

export default function (data) {
    const query = pickQuery();
    const params = query.build(data).concat(sortingAndPaging());
    const response = http.get(`${BASE_URL}/api/transactions?${toQueryString(params)}`,
        { headers: authHeaders(data.token), tags: { query: query.name, name: query.name } });
    check(response, {
        'status is 200': (r) => r.status === 200,
        'has items array': (r) => r.status === 200 && Array.isArray(r.json('items')),
    });
    sleep(0.5 + Math.random() * 1.5);
}

export function handleSummary(data) {
    const stamp = new Date().toISOString().replace(/[:.]/g, '-');
    const text = textSummary(data, { indent: ' ', enableColors: false });
    const result = { stdout: textSummary(data, { indent: ' ', enableColors: true }) };
    result[`${RESULTS_DIR}/transaction-search-${stamp}.json`] = JSON.stringify(data, null, 2);
    result[`${RESULTS_DIR}/transaction-search-${stamp}.txt`] = text;
    return result;
}

function perQueryThresholds() {
    const thresholds = {};
    QUERIES.forEach((query) => {
        thresholds[`http_req_duration{query:${query.name}}`] = ['p(95)<2000'];
    });
    return thresholds;
}

function sampleValues(items) {
    const distinct = (field) => [...new Set(items.map((item) => item[field]).filter((value) => value))];
    return { rrns: distinct('rrn'), authCodes: distinct('authCode'), terminalIds: distinct('terminalId') };
}

function pickQuery() {
    const total = QUERIES.reduce((sum, query) => sum + query.weight, 0);
    let roll = Math.random() * total;
    for (const query of QUERIES) {
        roll -= query.weight;
        if (roll < 0) {
            return query;
        }
    }
    return QUERIES[0];
}

function daysBack(days) {
    const now = Date.now();
    return [['from', new Date(now - days * DAY_MS).toISOString()], ['to', new Date(now).toISOString()]];
}

function thirteenMonthsBack() {
    const now = new Date();
    const from = new Date(now.getTime());
    from.setUTCMonth(from.getUTCMonth() - 13);
    return [['from', new Date(from.getTime() + DAY_MS).toISOString()], ['to', now.toISOString()]];
}

function randomFilters() {
    const params = [];
    if (Math.random() < 0.6) params.push(...some('status', STATUSES));
    if (Math.random() < 0.4) params.push(...some('type', TYPES));
    if (Math.random() < 0.5) params.push(...some('cardBrand', CARD_BRANDS));
    if (Math.random() < 0.3) params.push(...some('salesChannel', SALES_CHANNELS));
    return params.length > 0 ? params : some('status', STATUSES);
}

function statusAndBrand() {
    return [['status', 'APPROVED']].concat(some('cardBrand', CARD_BRANDS));
}

function some(name, values) {
    const count = 1 + Math.floor(Math.random() * 2);
    const shuffled = values.slice().sort(() => Math.random() - 0.5);
    return shuffled.slice(0, count).map((value) => [name, value]);
}

function exact(name, values) {
    return values.length > 0 ? [[name, values[Math.floor(Math.random() * values.length)]]] : [];
}

function sortingAndPaging() {
    const sort = Math.random() < 0.7 ? 'date' : 'amount';
    const direction = Math.random() < 0.7 ? 'desc' : 'asc';
    const page = Math.random() < 0.6 ? 0 : 1 + Math.floor(Math.random() * 5);
    const pageSize = [20, 20, 20, 50, 100][Math.floor(Math.random() * 5)];
    return [['sort', sort], ['direction', direction], ['page', page], ['pageSize', pageSize]];
}

function toQueryString(params) {
    return params.map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`).join('&');
}

function authHeaders(token) {
    return { Authorization: `Bearer ${token}` };
}
