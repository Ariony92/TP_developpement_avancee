import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 20,
    duration: '30s',
    thresholds: {
        http_req_failed: ['rate<0.05'],
        http_req_duration: ['p(95)<800'],
    },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080/tp_avancee/api';

export default function () {
    const loginRes = http.post(`${BASE}/login`, JSON.stringify({ login: 'alice', password: 'secret' }), {
        headers: { 'Content-Type': 'application/json' },
    });
    check(loginRes, { 'login status 200': (r) => r.status === 200 });

    const token = loginRes.json('accessToken');
    const authHeaders = { Authorization: `Bearer ${token}` };

    const listRes = http.get(`${BASE}/annonces?page=1&size=5`, { headers: authHeaders });
    check(listRes, { 'list status 200': (r) => r.status === 200 });

    sleep(1);
}
