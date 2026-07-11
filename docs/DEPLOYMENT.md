# Deployment

The app is packaged as a Docker image (`Dockerfile` in the project root) and
is fully **stateless** - uploaded images (food photos, category images,
logo, banner, generated QR codes) are stored in the `uploaded_file` database
table rather than on local disk, so there's no persistent volume to manage
and any free compute tier works, including ones that don't offer a
persistent disk at all.

This doc covers **Render** (app) + **TiDB Cloud Starter** (database) - both
have a genuine free tier with no credit card required and no time-of-day
restrictions, which is why they're the recommended combo here over
Railway (free tier blocks deploys 8am-8pm) or Oracle Cloud (Always Free
tier requires a credit card for identity verification).

## Environment variables the container reads

| Env var | Overrides | Example value |
|---|---|---|
| `DB_URL` | `db.url` | `jdbc:mysql://<host>:<port>/<db>?useSSL=true&enabledTLSProtocols=TLSv1.2&characterEncoding=UTF-8` |
| `DB_USERNAME` | `db.username` | `<prefix>.root` (TiDB Cloud's username format) |
| `DB_PASSWORD` | `db.password` | (your DB password) |
| `APP_BASE_URL` | `app.base.url` | `https://<your-deployed-domain>` (no trailing path - the app deploys at the domain root) |
| `PORT` | Tomcat's HTTP port | set automatically by Render |

## Step 1: TiDB Cloud Starter (database)

1. Sign up at [tidbcloud.com](https://tidbcloud.com).
2. **Create Cluster** → **Starter** (confirm $0/month) → name it, pick a region, create.
3. Once **Active**, click **Connect** and copy the **Host**, **Port** (`4000`), **User** (format `<prefix>.root`), and generate/save a **Password**.
4. Build your `DB_URL`:
   ```
   jdbc:mysql://<host>:4000/restaurant_db?useSSL=true&enabledTLSProtocols=TLSv1.2&characterEncoding=UTF-8
   ```
5. Load the schema: run `sql/schema.sql` then `sql/seed-data.sql` against the cluster (TiDB Cloud's **SQL Editor** in the console, or any MySQL client pointed at the host/port/user/password above with TLS enabled - TiDB Cloud rejects plain connections).
6. Set real admin/staff passwords (seeded rows have placeholder hashes by design - see `docs/README.md` §5.3): run `java -cp target/classes com.restro.utility.PasswordHashGeneratorTool` locally, then run the printed `UPDATE` against `admin` and each `staff` row on the TiDB cluster.

## Step 2: Render (app)

1. Sign up at [render.com](https://render.com) - no credit card required for the free tier.
2. **New → Web Service → Build and deploy from a Git repository**, pick this repo. Render detects the `Dockerfile` automatically.
3. Instance type: **Free** is fine - no disk is needed since the app is stateless.
4. **Environment** tab, add:
   ```
   DB_URL=jdbc:mysql://<your-tidb-host>:4000/restaurant_db?useSSL=true&enabledTLSProtocols=TLSv1.2&characterEncoding=UTF-8
   DB_USERNAME=<your-tidb-user>
   DB_PASSWORD=<your-tidb-password>
   APP_BASE_URL=https://<will be shown after first deploy - Render assigns a *.onrender.com domain>
   ```
   Deploy once to get your Render domain, then come back and set `APP_BASE_URL` to it, then redeploy (Manual Deploy → Deploy latest commit) so it takes effect.
5. Generate QR codes from Admin → Tables & QR *after* `APP_BASE_URL` is set correctly, so the encoded URL points at your real domain.

Note: Render's free tier spins the service down after periods of inactivity
and takes a few seconds to wake back up on the next request - fine for a
demo/low-traffic deployment, but worth knowing about.

## Testing the container locally first (optional, needs Docker installed)

```powershell
docker compose up --build
```

Brings up MySQL (auto-loads `sql/schema.sql` + `sql/seed-data.sql` on first
run) and the app together. Open
`http://localhost:8081/menu?table=1&token=a1e6f9c2b3d84e0f9a1c2b3d4e5f6071`.
Passwords still need to be set per §5.3 of `docs/README.md` before staff/admin
login works.
