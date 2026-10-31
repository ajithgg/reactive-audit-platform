# Tenant Console

React frontend for the `user-tenant` service. Lists users, opens detail/edit views, and performs create/update/delete through `http://localhost:8081`.

## Run

Prerequisites: `user-tenant` (and downstream services) already running.

```bash
cd user-console
npm install
npm run dev
```

Open http://localhost:5173

Vite proxies `/api` → `http://localhost:8081`. Tenant/actor headers are editable in the top bar (`X-Tenant-Id`, `X-Actor-Id`).
