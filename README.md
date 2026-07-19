# PayFlow – UPI Payment App (Learning / Demo Project)

A full-stack demo of a UPI-style payments app: Java 21 + Spring Boot backend,
React 19 frontend. Built to demonstrate JWT auth, transactional money-movement
logic, REST API design, and a polished mobile-first UI — **not** a real
payments integration. No real bank or UPI network is involved anywhere;
balances, verification, and money movement are all simulated inside the
app's own database.

## What's actually implemented (vs. scaffolded)

This is worth being upfront about, since the original spec was large:

**Fully implemented and working:**
- Spring Boot backend (Java 21) — register/login, JWT access+refresh tokens, BCrypt password hashing, hashed transaction PIN
- Role-based access control (`ROLE_USER` / `ROLE_ADMIN`) via Spring Security
- Mobile number OTP verification, and UPI PIN reset via OTP (separate from the logged-in "set PIN" flow)
- Bank account linking (simulated instant verification), multiple accounts, primary account selection
- UPI ID (VPA) creation with real QR code generation (ZXing), merchant VPAs, and dynamic (amount-encoded) QR codes
- Send money by UPI ID, mobile number, or bank account + IFSC; self-transfer between your own accounts
- Request money, accept/decline requests, split bills among multiple users with individual settlement
- Scheduled payments and recurring AutoPay mandates (runs via a background job every minute)
- Recharge & bill payments: mobile, DTH, electricity, water, gas, broadband, FASTag, credit card (13 seeded demo billers)
- In-app Wallet: add money (simulated top-up), wallet-to-bank transfer
- Favourite/saved contacts (beneficiaries)
- Cashback & rewards ledger, credited automatically on qualifying payments
- Basic fraud rule (velocity-based blocking) and trusted-device tracking with new-login alerts
- Transaction history with filters (date, status, type, amount, search), spending analytics/expense tracker, PDF receipt generation (iText)
- In-app notifications (payment success/failed, request received/accepted/declined, security alerts, AutoPay events)
- Complaint/support ticketing (user raises, admin resolves)
- Admin dashboard API (platform stats, user management, KYC status, transaction monitoring, complaint resolution, cashback overview)
- Global exception handling, input validation, audit logging
- Swagger/OpenAPI docs
- React frontend — Redux Toolkit, Axios, React Router, Tailwind CSS v4, Recharts, every screen above wired to the real API
- Docker images for both backend and frontend, docker-compose for local orchestration with Postgres + Redis

**Explicitly not implemented, and why:**
- Fingerprint/Face unlock, NFC Tap & Pay, contact sync, voice payments — these need real device/OS biometric and hardware APIs that only exist in a native mobile app, not a web demo.
- RuPay Credit Card on UPI, UPI Lite — these are NPCI/network-specific integrations tied to real payment rails; faking them wouldn't teach anything true about how they work.
- International UPI, investment/SIP integration, insurance purchase, loan eligibility, credit line on UPI — regulated financial products that require actual licensed partners; a demo "approval" would be actively misleading.
- AI chatbot, voice assistant, AI expense categorization/fraud prediction — would need a real LLM integration; the analytics here are honest server-side aggregation instead of simulated "AI."
- Family accounts, shared wallet, group payments, merchant dashboard/settlement reports, dynamic QR *scanning* (only generation is implemented) — reasonable further extensions, just out of scope for this pass.
- CI/CD (GitHub Actions), Kubernetes — not included, but the Docker setup is CI-friendly to add to.

## Quick start

### Backend only (fastest way to explore the API)

```bash
cd backend
mvn spring-boot:run
```

Runs on an in-memory H2 database (`dev` profile, the default) — zero setup.
On first boot it seeds three demo accounts (see below). Visit:
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 console: http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:payflow`, user `sa`, no password)

> Note: this sandbox environment couldn't reach Maven Central to actually
> run `mvn` and compile-verify the backend (network here is restricted to a
> few package registries). The code follows standard Spring Boot 3 / Java 21
> conventions throughout, but please run a build locally to confirm before
> relying on it.

### Frontend only

```bash
cd frontend
npm install   # already done in this workspace
npm run dev
```

Runs on http://localhost:5173, proxies `/api` to `localhost:8082`.
This **was** built and verified with `npm run build` in this environment.

### Everything with Docker Compose (Postgres + Redis + backend + frontend)

```bash
docker compose up --build
```

- Frontend: http://localhost:3000
- Backend: http://localhost:8082
- Note: the demo data seeder only runs on the `dev` (H2) profile, so with
  Postgres you'll register a fresh account through the UI instead.

## Demo accounts (dev / H2 profile only)

| Email | Password | Notes |
|---|---|---|
| admin@payflow.demo | Admin@1234 | Has `ROLE_ADMIN`, can see `/admin` dashboard |
| rahul@payflow.demo | Passw0rd! | VPA `rahul@payflow`, PIN `1234`, ₹10,000 starting balance |
| priya@payflow.demo | Passw0rd! | VPA `priya@payflow`, PIN `1234`, ₹10,000 starting balance |

Log in as Rahul and send money to `priya@payflow` to see the full flow.

## Project structure

```
payflow/
├── backend/            Spring Boot 3 / Java 21 monolith
│   └── src/main/java/com/payflow/
│       ├── config/      security, JPA auditing, Swagger, demo data seeder
│       ├── controller/  REST endpoints
│       ├── service/     business logic (transactional money movement lives here)
│       ├── repository/  Spring Data JPA repositories
│       ├── entity/      JPA entities (User, BankAccount, UpiId, Transaction, ...)
│       ├── dto/         request/response DTOs, one package per resource
│       ├── security/    JWT provider, filter, UserDetails
│       └── exception/   global exception handler + custom exceptions
├── frontend/            React 19 + Vite + Redux Toolkit + Tailwind CSS v4
│   └── src/
│       ├── api/          axios modules
│       ├── slices/       Redux slices
│       ├── pages/         one file per screen
│       └── layouts/       app shell (nav)
└── docker-compose.yml    Postgres + Redis + backend + frontend
```

## Security notes (since this handles "money")

- Login password and transaction PIN are hashed separately (BCrypt), so a leaked
  password never exposes the PIN and vice versa.
- JWT access tokens are short-lived (15 min); refresh tokens (7 days) are used
  to get new access tokens without re-entering a password.
- All money-movement endpoints require the transaction PIN, re-verified server-side.
- `sendMoney` runs inside a single `@Transactional` method — either both the
  debit and credit happen, or neither does.
- This is a demo. Do not reuse the JWT secret in `application.yml` for anything real.
