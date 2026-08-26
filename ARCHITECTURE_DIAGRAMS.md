# Deployment Architecture & Flow Diagrams

## 1. System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          INTERNET / USERS                               │
│                     (HTTPS via api.agriconsultancy.tech)                          │
└────────────────────────────────┬────────────────────────────────────────┘
                                 │
                     ┌───────────┴────────────┐
                     │   Hostinger VPS        │
                     │   Ubuntu 20.04 LTS     │
                     │   2GB+ RAM, 2+ CPU     │
                     │                        │
        ┌────────────┴────────────────────────┼──────────────────┐
        │                                     │                  │
        │                                     │                  │
    ┌───▼────────────────────────────────────▼────────────────┐  │
    │           DOCKER NETWORK (vp-network)                   │  │
    │                                                          │  │
    │  ┌──────────────────────┐                               │  │
    │  │   NGINX (Port 80/443)│                               │  │
    │  │  ┌────────────────┐  │                               │  │
    │  │  │ SSL/TLS Config │  │                               │  │
    │  │  │ Reverse Proxy  │  │                               │  │
    │  │  │ gzip compress  │  │                               │  │
    │  │  └────────────────┘  │                               │  │
    │  └──────────────────────┘                               │  │
    │              │                                           │  │
    │              │ (Port 8085)                              │  │
    │              ▼                                           │  │
    │  ┌──────────────────────────────────────────┐            │  │
    │  │    SPRING BOOT APPLICATION               │            │  │
    │  │  ┌────────────────────────────────────┐  │            │  │
    │  │  │ • Business Logic                   │  │            │  │
    │  │  │ • API Endpoints                    │  │            │  │
    │  │  │ • Authentication (JWT)             │  │            │  │
    │  │  │ • Logging (async)                  │  │            │  │
    │  │  │ • Health Checks                    │  │            │  │
    │  │  └────────────────────────────────────┘  │            │  │
    │  └──────────────────────────────────────────┘            │  │
    │         │                            │                  │  │
    │         ▼                            ▼                  │  │
    │  ┌─────────────────╖        ┌────────────────┐         │  │
    │  │ MySQL Database  ║        │  Redis Cache   │         │  │
    │  │ ┌─────────────┐ ║        ├────────────────┤         │  │
    │  │ │ vp_          │ ║        │ • Session info │         │  │
    │  │ │ consultancy  │ ║        │ • Data Cache   │         │  │
    │  │ │ tables       │ ║        │ • Rate limits  │         │  │
    │  │ └─────────────┘ ║        └────────────────┘         │  │
    │  │ Port: 3306      ║        Port: 6379                  │  │
    │  │ Persistence:    ║        Persistence:                │  │
    │  │ /var/lib/      ║        /var/lib/docker/            │  │
    │  │ mysql-data/     ║        volumes/                    │  │
    │  └─────────────────╜        volumes/redis-data/         │  │
    │                                                          │  │
    └──────────────────────────────────────────────────────────┘  │
    │                                                               │
    │  ┌─────────────────────────────────────────────────────┐   │
    │  │  PERSISTENT VOLUMES & BACKUPS                       │   │
    │  │  • /var/agri-consultancy/logs/  (App logs)          │   │
    │  │  • /opt/vp-consultancy/backups/ (DB backups)        │   │
    │  │  • /etc/letsencrypt/          (SSL certs)           │   │
    │  └─────────────────────────────────────────────────────┘   │
    │                                                               │
    │  ┌─────────────────────────────────────────────────────┐   │
    │  │  AUTOMATION & MONITORING                            │   │
    │  │  • Health checks (every 30 min)                     │   │
    │  │  • Database backups (daily at 2 AM)                 │   │
    │  │  • Log rotation (daily)                             │   │
    │  │  • SSL renewal (automatic)                          │   │
    │  └─────────────────────────────────────────────────────┘   │
    │                                                               │
    └───────────────────────────────────────────────────────────────┘
```

---

## 2. Application Logging Flow

```
SPRING BOOT APPLICATION
        │
        ↓
    Logback-spring.xml (logging config)
        │
        ├─────────────────────┬─────────────────────┐
        │                     │                     │
        ▼                     ▼                     ▼
   CONSOLE              APP.LOG              ERROR.LOG
   (Docker)          (Async Appender)       (Async Appender)
        │                  │                       │
        │                  ↓                       ↓
        │         /var/agri-consultancy/    /var/agri-consultancy/
        │         logs/app.log.2026-       logs/error.log.2026-
        │         08-23.1.log              08-23.1.log
        │                  │                       │
        │         Daily Rotation           Daily Rotation
        │         (max 100MB)              (max 100MB)
        │                  │                       │
        ▼                  ▼                       ▼
    docker logs    Persistent Storage    Persistent Storage
    docker stats        (7 days)              (7 days)
    stdout/stderr        Searchable           Stack traces
                        Queryable            Full errors

┌──────────────────────────────────────────────────────────┐
│ Log Rotation Schedule                                    │
│ ┌────────────────────────────────────────────────────┐  │
│ │ Daily at midnight: Rotate to .YYYY-MM-DD.N.log    │  │
│ │ Keep: 7 days of history                           │  │
│ │ Max size: 100MB per file                          │  │
│ │ Compression: .gz (via logback)                    │  │
│ │ Location: /var/agri-consultancy/logs/             │  │
│ └────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

---

## 3. Deployment Process Flow

```
START DEPLOYMENT
       │
       ▼
┌────────────────────────────────┐
│ Prerequisites Check            │
│ • Docker installed?            │
│ • Docker Compose installed?    │
│ • .env file present?           │
│ • Directories exist?           │
└────────────────────────────────┘
       │
       ├─ FAILED ──→ Exit with error
       │
       ▼ OK
┌────────────────────────────────┐
│ Build Docker Images            │
│ • Maven compile                │
│ • Package JAR                  │
│ • Create runtime image         │
│ (Takes 5-10 minutes first time)│
└────────────────────────────────┘
       │
       ▼
┌────────────────────────────────┐
│ Start Docker Services          │
│ • MySQL container              │
│ • Redis container              │
│ • App container                │
│ • Nginx container              │
│ (docker-compose up -d)         │
└────────────────────────────────┘
       │
       ▼
┌────────────────────────────────┐
│ Wait for Health Checks         │
│ • MySQL health check (30s)     │
│ • Redis health check (30s)     │
│ • App health check (60s)       │
│ (Max timeout: 3 minutes)       │
└────────────────────────────────┘
       │
       ├─ TIMEOUT ──→ Logs shown, troubleshoot
       │
       ▼ HEALTHY
┌────────────────────────────────┐
│ Display Status                 │
│ • Service status               │
│ • Container list               │
│ • Startup logs                 │
└────────────────────────────────┘
       │
       ▼
  DEPLOYMENT COMPLETE ✅
```

---

## 4. Database Backup & Recovery Flow

```
BACKUP PROCESS
       │
       ▼
./database-backup.sh backup daily
       │
       ├─ Load .env environment variables
       │
       └─ Execute mysqldump
              │
              ├─ Connect to MySQL container
              │
              ├─ Export all data (single transaction)
              │
              └─ Compress with gzip
                     │
                     ▼
         /opt/vp-consultancy/backups/daily/
         vp_consultancy_daily_YYYY-MM-DD_HH-MM-SS.sql.gz
                     │
                     ├─ Verify integrity (gzip test)
                     │
                     └─ Success message


RESTORE PROCESS (CAREFUL!)
       │
       ▼
./database-backup.sh restore /path/to/backup.sql.gz
       │
       ├─ Verify backup file exists
       │
       ├─ Confirm with user (interactive)
       │
       ├─ Decompress .sql.gz
       │
       └─ Import into MySQL
              │
              ├─ Connect to MySQL container
              │
              ├─ Execute SQL commands
              │
              └─ Success message
                     │
                     ↓
              DATABASE RESTORED 🔄
              (Old data replaced)


RETENTION POLICY
       │
       ▼
./database-backup.sh cleanup
       │
       └─ Remove backups older than 30 days
              │
              ├─ Daily backups: Keep 30 days
              │
              ├─ Weekly backups: Keep 30 days
              │
              └─ Monthly backups: Keep 30 days
```

---

## 5. Health Monitoring Flow

```
HEALTH CHECK SCRIPT (Runs every 30 minutes)
       │
       ▼
┌──────────────────────────────────────┐
│ Check Docker Services Status         │ 
│ • mysql container running?           │
│ • redis container running?           │
│ • app container running?             │
│ • nginx container running?           │
└──────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│ Check Database Connectivity          │
│ • mysqladmin ping                    │
│ • Response within timeout?           │
└──────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│ Check Cache Connectivity             │
│ • redis-cli PING                     │
│ • Response == PONG?                  │
└──────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│ Check Application Health             │
│ • GET /actuator/health               │
│ • Response status == UP?             │
└──────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│ Check System Resources               │
│ • Disk usage < 80%?                  │
│ • Memory usage < 85%?                │
│ • Log directory size acceptable?     │
└──────────────────────────────────────┘
       │
       ├─ ALL CHECKS PASS ──→ Log "HEALTHY" ✅
       │
       └─ ANY CHECK FAIL ──→ Log "UNHEALTHY" ⚠️
              │
              └─ Send alert (if configured)
                     │
                     ├─ Email notification
                     ├─ Slack message
                     ├─ Telegram message
                     └─ Log file entry
```

---

## 6. SSL Certificate Renewal Flow

```
SSL CERTIFICATE LIFECYCLE
       │
       ▼
Initial Cert Generated (90 days valid)
       │
    Day 30
       ▼
Certbot Renewal Check (daily via systemd timer)
       │
       ├─ Certificate expires in 30 days?
       │
       │  YES
       │   │
       │   ▼
       │  Request new certificate from Let's Encrypt
       │   │
       │   ├─ Validate domain ownership
       │   │
       │   ├─ Generate new certificate
       │   │
       │   └─ Place in /etc/letsencrypt/live/
       │        │
       │        ▼
       │     Reload Nginx config
       │        │
       │        └─ NO DOWNTIME ✅
       │
       │  NO
       │   │
       │   └─ Continue monitoring
       │
    Day 90
       │
       ▼
Certificate expires (if renewal failed)
       │
       └─ ALERT! Manual intervention needed
              │
              └─ certbot renew --force-renewal
```

---

## 7. Container Lifecycle

```
CONTAINER STARTUP SEQUENCE
       │
       ▼
┌─────────────────┐
│ Docker Compose  │
│ reads YAML      │
└─────────────────┘
       │
       ├──────────────┬──────────────┬──────────────┬──────────────┐
       │              │              │              │              │
       ▼              ▼              ▼              ▼              ▼
    MySQL         Redis          App           Nginx         Certbot
    Start         Start          Start          Start         Start
       │              │              │              │              │
       ├─ Init       ├─ Init       ├─ Build      ├─ Load       ├─ Wait
  Databases    Memory pool    JVM heap     Config            for Nginx
       │              │              │              │              │
       ├─ Health  ├─ Health   ├─ Spring   ├─ Listen    ├─ Monitor
  check        check    startup    80/443       Certs
       │              │              │              │              │
       └─ Ready   └─ Ready    ├─ Health   ├─ Start      └─ Renew
                             check       Proxy         when needed
                             │           │
                             └─ Ready    └─ Ready
                                  │
                                  ▼
                           ALL SERVICES READY ✅
```

---

## 8. Request Flow Through System

```
USER REQUEST
       │
       ▼
HTTPS to api.agriconsultancy.tech
       │
       ▼
┌─────────────────────────────┐
│ Nginx Reverse Proxy         │
│ • Decrypt SSL/TLS           │
│ • Validate security headers │
│ • Apply gzip compression    │
│ • Log access                │
└─────────────────────────────┘
       │
       ▼
/agri-consultancy-service/* route
       │
       ▼
┌─────────────────────────────┐
│ Spring Boot Application     │
│ • Route to controller       │
│ • Authentication check      │
│ • Business logic            │
│ • Database query            │
│ • Cache layer (Redis)       │
└─────────────────────────────┘
       │
       ├──────────────┬──────────────┐
       │              │              │
       ▼              ▼              ▼
   Database       Cache (Redis)   External API
   (MySQL)        (In-Memory)     (if any)
       │              │              │
       └──────────────┴──────────────┘
              │
              ▼
    Response Generated
              │
              ├─ Log to app.log
              ├─ Log to error.log (if error)
              │
              ▼
┌──────────────────────────────┐
│ Nginx Response               │
│ • Compress with gzip         │
│ • Add security headers       │
│ • Encrypt with SSL/TLS       │
│ • Log access                 │
└──────────────────────────────┘
              │
              ▼
   HTTPS Response to User
```

---

## 9. Key Directories & Data Flow

```
┌─────────────────────────────────────────────────────────────────┐
│ HOSTINGER VPS (/root or /home/user)                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│ /opt/vp-consultancy/                  PROJECT ROOT             │
│ ├── Dockerfile                        Container definition     │
│ ├── docker-compose.prod.yml           Services orchestration  │
│ ├── nginx.conf                        Web server config        │
│ ├── logback-spring.xml                Logging config           │
│ ├── application-prod.properties       Spring config            │
│ ├── .env                              Environment vars (SECRET) │
│ ├── src/                              Source code              │
│ │   └── main/resources/               Config files            │
│ ├── target/                           Built JAR               │
│ ├── deploy.sh                         Deploy script            │
│ ├── health-check.sh                   Monitor script           │
│ ├── database-backup.sh                Backup script            │
│ ├── backups/                          Database backups        │
│ │   ├── daily/                        Daily backups           │
│ │   ├── weekly/                       Weekly backups          │
│ │   └── monthly/                      Monthly backups         │
│ └── Docker volumes (docker namespaced)                         │
│                                                                  │
│ /var/agri-consultancy/                APPLICATION DATA         │
│ └── logs/                             Application logs        │
│     ├── app.log                       Current app log         │
│     ├── error.log                     Current error log       │
│     ├── app.2026-08-23.1.log          Daily rotated log       │
│     └── error.2026-08-23.1.log        Daily rotated error    │
│                                                                  │
│ /var/lib/mysql-data/                  DATABASE PERSISTENCE    │
│ ├── vp_consultancy/                   Database folder         │
│ │   ├── users.ibd                     Table files            │
│ │   ├── posts.ibd                     Table files            │
│ │   └── ...                           More table files       │
│ └── mysql/                            System database        │
│                                                                  │
│ /etc/letsencrypt/                     SSL CERTIFICATES        │
│ └── live/api.agriconsultancy.tech/              Certificate folder     │
│     ├── fullchain.pem                 Complete chain         │
│     └── privkey.pem                  Private key (SECRET)    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 10. Monitoring Dashboard Connections

```
┌────────────────────────────────────────────────────┐
│ Applications/Tools That Can Connect                │
├────────────────────────────────────────────────────┤
│                                                    │
│ HEALTH & STATUS (Public)                          │
│ • https://api.agriconsultancy.tech/actuator/health         │
│ • https://api.agriconsultancy.tech/swagger-ui.html         │
│                                                    │
│ LOGS (SSH Access)                                 │
│ • /var/agri-consultancy/logs/app.log            │
│ • tail -f command                                │
│ • Log aggregation tools (ELK, Splunk, etc.)      │
│                                                    │
│ DOCKER (SSH Access)                               │
│ • docker-compose ps                              │
│ • docker stats                                    │
│ • Portainer (optional UI)                        │
│                                                    │
│ DATABASE (Internal Only)                          │
│ • MySQL from app container only                  │
│ • No external access                             │
│ • Backup files stored locally                    │
│                                                    │
│ MONITORING TOOLS (Can SSH tunnel)                │
│ • Prometheus (metrics collection)                │
│ • Grafana (dashboards)                           │
│ • ELK Stack (log aggregation)                    │
│ • New Relic (APM)                                │
│ • Datadog (monitoring)                           │
│                                                    │
└────────────────────────────────────────────────────┘
```

---

This visual guide helps understand:
1. How components interact
2. How data flows through the system
3. How logging works
4. How deployments happen
5. How monitoring functions
6. Where files are stored
7. How SSL works
8. Container communication

Print or bookmark this page for reference!

