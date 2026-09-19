# PayFlow – Frontend

React 19 + Vite + Redux Toolkit + Tailwind CSS v4 + React Router + Axios.

## Run locally

```bash
npm install
npm run dev
```

App runs at http://localhost:5173. API calls to `/api/*` are proxied to
`http://localhost:8082` (the Spring Boot backend) via `vite.config.js` in dev,
and via nginx in the Docker image.

## Structure

```
src/
  api/          axios modules, one per backend resource
  slices/       Redux Toolkit slices (auth, wallet, toast)
  store/        Redux store configuration
  layouts/      AppLayout (top nav + mobile bottom nav)
  components/   shared UI (Logo, AmountText, ToastHost, ProtectedRoute)
  pages/        one file per screen (Login, Dashboard, SendMoney, ...)
  pages/admin/  admin dashboard
```

## Demo accounts (seeded by the backend's `dev` profile)

| Email | Password | Notes |
|---|---|---|
| admin@payflow.demo | Admin@1234 | Admin dashboard |
| rahul@payflow.demo | Passw0rd! | VPA: rahul@payflow, PIN: 1234 |
| priya@payflow.demo | Passw0rd! | VPA: priya@payflow, PIN: 1234 |

Try sending money from Rahul's account to `priya@payflow`.
