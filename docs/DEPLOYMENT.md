# Deployment

The app is packaged as a Docker image (`Dockerfile` in the project root) that
runs on any Docker-capable host. This doc covers **Railway** (recommended -
one-click real MySQL, no code changes needed) and **Render** (works, but
needs an external MySQL since Render has no managed MySQL of its own).

Account creation and clicking through each platform's dashboard is something
only you can do - this doc gives you the exact values to enter at each step.

## Environment variables the container reads

Set via `db.properties`/`app.properties` for local dev.

| Env var | Overrides | Example value |
|---|---|---|
| `DB_URL` | `db.url` | `jdbc:mysql://<host>:<port>/<db>?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8` |
| `DB_USERNAME` | `db.username` | `root` |
| `DB_PASSWORD` | `db.password` | (your DB password) |
| `UPLOAD_DIR` | `upload.dir` | `/data/uploads` |
| `APP_BASE_URL` | `app.base.url` | `https://<your-deployed-domain>` (no trailing path - the app deploys at the domain root) |
| `PORT` | Tomcat's HTTP port | set automatically by the host; defaults to 8080 if unset |

## Option A: Railway (recommended)

1. **Push to GitHub** (already done if you've been following along - Railway deploys from a GitHub repo).
2. On [railway.app](https://railway.app), **New Project → Deploy from GitHub repo**, pick this repo. Railway detects the `Dockerfile` automatically.
3. In the same project, **+ New → Database → Add MySQL**. Railway provisions a real MySQL instance and exposes `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, `MYSQLDATABASE` as variables on that MySQL service.
4. On the **app service → Variables**, add:
   ```
   DB_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
   DB_USERNAME=${{MySQL.MYSQLUSER}}
   DB_PASSWORD=${{MySQL.MYSQLPASSWORD}}
   UPLOAD_DIR=/data/uploads
   APP_BASE_URL=https://${{RAILWAY_PUBLIC_DOMAIN}}
   ```
   (The `${{ServiceName.VAR}}` syntax is Railway's cross-service variable reference - it resolves automatically, you don't fill in real values yourself.)
5. **App service → Settings → Volumes → New Volume**, mount path `/data/uploads`. Without this, every redeploy wipes uploaded food/category photos, logo, banner, and QR codes.
6. **Load the schema**: MySQL service → **Data** tab (or connect with any MySQL client using the connection details Railway shows) → run `sql/schema.sql` then `sql/seed-data.sql` from this repo, in that order.
7. **Set real admin/staff passwords** (the seeded rows have placeholder hashes that can't log in, by design - see `docs/README.md` §5.3): run `java -cp target/classes com.restro.utility.PasswordHashGeneratorTool` locally, then run the printed `UPDATE` statement against the Railway MySQL database for `admin` and each `staff` row.
8. **App service → Settings → Networking → Generate Domain**. Railway builds the Dockerfile and deploys automatically on every push to `main`.
9. Generate QR codes from Admin → Tables & QR *after* `APP_BASE_URL` is set correctly, so the encoded URL points at your real domain.

## Option B: Render + external MySQL

Render has no managed MySQL, so provision MySQL elsewhere first - Railway's
MySQL plugin (step 3 above, without deploying the app there) or a free-tier
Aiven/PlanetScale MySQL both work, since the driver just needs a standard
`jdbc:mysql://` endpoint.

1. **New → Web Service → Build and deploy from a Git repository**, pick this repo. Render detects the `Dockerfile`.
2. Instance type: must be a **paid** plan to attach a persistent Disk (Free tier has no persistent disk - every deploy wipes `/data/uploads`).
3. **Settings → Disks → Add Disk**, mount path `/data/uploads`.
4. **Environment** tab, add the same `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` / `UPLOAD_DIR=/data/uploads` / `APP_BASE_URL` variables as above, pointed at your external MySQL's real host/port/credentials (Render can't cross-reference a database it doesn't manage, so these are literal values here, not `${{...}}` references).
5. Same schema-load and password-setup steps as Railway, above.

## Testing the container locally first (optional, needs Docker installed)

```powershell
docker compose up --build
```

Brings up MySQL (auto-loads `sql/schema.sql` + `sql/seed-data.sql` on first
run) and the app together, wired with the same env vars a cloud host would
use. Open `http://localhost:8081/menu?table=1&token=a1e6f9c2b3d84e0f9a1c2b3d4e5f6071`.
Passwords still need to be set per §5.3 of `docs/README.md` before staff/admin
login works.
