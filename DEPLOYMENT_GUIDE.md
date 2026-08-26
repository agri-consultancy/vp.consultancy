# Production Deployment Guide - VP Consultancy Application

This guide provides step-by-step instructions to deploy the VP Consultancy application on Hostinger VPS using Docker.

---

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [VPS Setup](#vps-setup)
3. [Docker Installation](#docker-installation)
4. [Application Preparation](#application-preparation)
5. [Environment Configuration](#environment-configuration)
6. [SSL Certificate Setup](#ssl-certificate-setup)
7. [Deployment](#deployment)
8. [Verification](#verification)
9. [Monitoring & Logging](#monitoring--logging)
10. [Maintenance](#maintenance)
11. [Troubleshooting](#troubleshooting)

---

## Prerequisites

- Hostinger VPS with root/sudo access
- OS: Ubuntu 20.04 LTS or newer (recommended)
- Minimum 2GB RAM, 2 CPU cores
- Domain name pointing to your VPS IP
- Git installed locally to push code changes

---

## VPS Setup

### Step 1: Connect to Your VPS

```bash
ssh root@your_vps_ip_address
```

### Step 2: Update System

```bash
apt update && apt upgrade -y
apt autoremove -y
```

### Step 3: Create Application Directory

```bash
mkdir -p /opt/vp-consultancy
cd /opt/vp-consultancy
```

### Step 4: Create Logs Directory with Proper Permissions

```bash
mkdir -p /var/agri-consultancy/logs
chmod 777 /var/agri-consultancy/logs
```

### Step 5: Create Database Data Directory

```bash
mkdir -p /var/lib/mysql-data
chmod 777 /var/lib/mysql-data
```

---

## Docker Installation

### Step 1: Install Docker

```bash
# Install Docker repository
apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release

# Add Docker GPG key
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# Add Docker repository
echo "deb [arch=amd64 signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker
apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-compose-plugin
```

### Step 2: Install Docker Compose Standalone

```bash
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
docker-compose --version
```

### Step 3: Add User to Docker Group (Optional)

```bash
usermod -aG docker $USER
newgrp docker
```

### Step 4: Enable Docker Service

```bash
systemctl enable docker
systemctl start docker
```

---

## Application Preparation

### Step 1: Clone/Upload Your Application

**Option A: Clone from Git**
```bash
cd /opt/vp-consultancy
git clone https://your-repo-url.git .
```

**Option B: Upload via SFTP**
Use any SFTP client to upload your project files to `/opt/vp-consultancy/`

### Step 2: Verify Required Files

Ensure these files exist in your project root:
- ✅ `Dockerfile`
- ✅ `docker-compose.prod.yml`
- ✅ `nginx.conf`
- ✅ `.env.example`
- ✅ `logback-spring.xml` (in src/main/resources/)
- ✅ `application-prod.properties` (in src/main/resources/)
- ✅ `pom.xml`

---

## Environment Configuration

### Step 1: Create .env File

```bash
cd /opt/vp-consultancy
cp .env.example .env
nano .env
```

### Step 2: Edit .env with Your Values

```ini
# ===============================
# MySQL Configuration
# ===============================
MYSQL_ROOT_PASSWORD=your_secure_root_password_here_min_16_chars
MYSQL_DATABASE=vp_consultancy
MYSQL_USER=consultancy_user
MYSQL_PASSWORD=your_secure_db_password_here_min_16_chars
MYSQL_HOST=mysql

# ===============================
# JWT Configuration
# ===============================
# Generate a new secure JWT secret using:
# openssl rand -base64 64
APP_JWT_SECRET=your_generated_secure_jwt_secret_key_here

# ===============================
# Application Environment
# ===============================
SPRING_PROFILES_ACTIVE=prod

# ===============================
# Compose Project Name
# ===============================
COMPOSE_PROJECT_NAME=vp-consultancy
```

### Step 3: Generate Secure Passwords

```bash
# Generate random 32-character password
openssl rand -base64 32
```

### Step 4: Set Proper Permissions for .env

```bash
chmod 600 .env
```

---

## SSL Certificate Setup

### Step 1: Install Certbot

```bash
apt-get install -y certbot python3-certbot-nginx
```

### Step 2: Create Certbot Directory

```bash
mkdir -p /var/www/certbot
mkdir -p /etc/letsencrypt
chmod 755 /var/www/certbot
```

### Step 3: Initial Certificate Generation (Before Starting Containers)

```bash
certbot certonly --standalone \
  -d api.agriconsultancy.tech \
  -d www.api.agriconsultancy.tech \
  --email your-email@example.com \
  --agree-tos \
  --no-eff-email
```

### Step 4: Verify Certificate Installation

```bash
ls -la /etc/letsencrypt/live/api.agriconsultancy.tech/
```

You should see:
- `fullchain.pem`
- `privkey.pem`

### Update Nginx Configuration

Edit `nginx.conf` and replace `yourdomain.com` with your actual domain:

```bash
cd /opt/vp-consultancy
sed -i 's/yourdomain.com/api.agriconsultancy.tech/g' nginx.conf
```

---

## Deployment

### Step 1: Build Docker Images

```bash
cd /opt/vp-consultancy
docker-compose -f docker-compose.prod.yml build
```

**Note:** This may take 5-10 minutes on the first run.

### Step 2: Start Services

```bash
docker-compose -f docker-compose.prod.yml up -d
```

### Step 3: Verify Services Status

```bash
docker-compose -f docker-compose.prod.yml ps
```

Expected output:
```
NAME                      STATUS
vp-consultancy-mysql      Up (healthy)
vp-consultancy-redis      Up (healthy)
vp-consultancy-app        Up (healthy)
vp-consultancy-nginx      Up
vp-consultancy-certbot    Up
```

---

## Verification

### Step 1: Check Application Logs

```bash
# Real-time logs
docker-compose -f docker-compose.prod.yml logs -f app

# Or view saved logs
tail -f /var/agri-consultancy/logs/app.log
```

### Step 2: Check Health Status

```bash
# Via curl
curl -k https://your-domain.com/agri-consultancy-service/actuator/health

# Should return JSON like:
# {"status":"UP","components":{"db":{"status":"UP"},"redis":{"status":"UP"}}}
```

### Step 3: Test API Endpoint

```bash
curl -k https://your-domain.com/agri-consultancy-service/api/your-endpoint
```

### Step 4: View Swagger Documentation

```
https://your-domain.com/agri-consultancy-service/swagger-ui.html
```

### Step 5: Check SSL Certificate

```bash
# Verify certificate status
certbot certificates

# Test SSL with online tools
# https://www.ssllabs.com/ssltest/
```

---

## Monitoring & Logging

### Step 1: View Application Logs

**Live Logs from Docker:**
```bash
docker-compose -f docker-compose.prod.yml logs -f app
```

**Persistent Logs on VPS:**
```bash
# All logs
tail -f /var/agri-consultancy/logs/app.log

# Error logs only
tail -f /var/agri-consultancy/logs/error.log

# Last 100 lines
tail -100 /var/agri-consultancy/logs/app.log
```

### Step 2: Nginx Logs

```bash
# Access logs
tail -f /var/lib/docker/volumes/vp-consultancy_nginx-logs/_data/vp-consultancy-access.log

# Error logs
tail -f /var/lib/docker/volumes/vp-consultancy_nginx-logs/_data/vp-consultancy-error.log
```

### Step 3: Docker Resource Monitoring

```bash
# Check container resource usage
docker stats

# Check disk space
df -h

# Check memory usage
free -h
```

### Step 4: Database Monitoring

```bash
# Connect to MySQL container
docker-compose -f docker-compose.prod.yml exec mysql mysql -u root -p$MYSQL_ROOT_PASSWORD

# Common MySQL queries
SHOW DATABASES;
USE vp_consultancy;
SHOW TABLES;
SELECT COUNT(*) FROM table_name;
```

### Step 5: Redis Monitoring

```bash
# Connect to Redis
docker-compose -f docker-compose.prod.yml exec redis redis-cli

# Check Redis info
INFO
DBSIZE
FLUSHDB  # Only if needed - clears cache
```

### Step 6: Set Up Log Rotation (Optional but Recommended)

Create `/etc/logrotate.d/vp-consultancy`:

```bash
cat > /etc/logrotate.d/vp-consultancy << 'EOF'
/var/agri-consultancy/logs/*.log {
    daily
    missingok
    rotate 7
    compress
    delaycompress
    notifempty
    create 0644 nobody nobody
    sharedscripts
}
EOF
```

Test log rotation:
```bash
logrotate -f /etc/logrotate.d/vp-consultancy
```

---

## Maintenance

### Step 1: Regular Database Backup

**Create Backup Script** (`/opt/vp-consultancy/backup.sh`):

```bash
#!/bin/bash

BACKUP_DIR="/opt/vp-consultancy/backups"
DATE=$(date +%Y-%m-%d_%H-%M-%S)
BACKUP_FILE="$BACKUP_DIR/vp_consultancy_$DATE.sql"

mkdir -p $BACKUP_DIR

docker-compose -f docker-compose.prod.yml exec -T mysql mysqldump \
  -u root -p$MYSQL_ROOT_PASSWORD \
  vp_consultancy > $BACKUP_FILE

echo "Backup created: $BACKUP_FILE"

# Keep only last 7 days of backups
find $BACKUP_DIR -name "*.sql" -mtime +7 -delete
```

Make it executable:
```bash
chmod +x /opt/vp-consultancy/backup.sh
```

**Add to Crontab** (Daily backup at 2 AM):
```bash
crontab -e

# Add this line:
0 2 * * * /opt/vp-consultancy/backup.sh
```

### Step 2: SSL Certificate Auto-Renewal

Certbot automatically renews certificates. To verify:

```bash
# Test renewal
certbot renew --dry-run

# View renewal schedule
systemctl list-timers | grep certbot
```

### Step 3: Application Updates

To deploy new application version:

```bash
cd /opt/vp-consultancy

# Pull latest code
git pull origin main

# Rebuild and restart
docker-compose -f docker-compose.prod.yml build
docker-compose -f docker-compose.prod.yml up -d

# Verify
docker-compose -f docker-compose.prod.yml logs -f app
```

### Step 4: Update Dependencies/Security Patches

```bash
# Update Docker images
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d

# Clean up unused images
docker image prune -a
```

---

## Troubleshooting

### Issue: Container fails to start

```bash
# Check logs
docker-compose -f docker-compose.prod.yml logs app

# Restart container
docker-compose -f docker-compose.prod.yml restart app

# Full restart
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d
```

### Issue: Database connection error

```bash
# Check MySQL container status
docker-compose -f docker-compose.prod.yml ps mysql

# Check MySQL logs
docker-compose -f docker-compose.prod.yml logs mysql

# Restart MySQL
docker-compose -f docker-compose.prod.yml restart mysql

# Verify connection
docker-compose -f docker-compose.prod.yml exec mysql mysql -u root -p$MYSQL_ROOT_PASSWORD -e "SELECT 1"
```

### Issue: Redis connection error

```bash
# Check Redis status
docker-compose -f docker-compose.prod.yml exec redis redis-cli ping

# Should return: PONG
```

### Issue: SSL certificate error

```bash
# Check certificate status
certbot certificates

# Manually renew
certbot renew --force-renewal

# Restart Nginx
docker-compose -f docker-compose.prod.yml restart nginx
```

### Issue: Disk space running out

```bash
# Check disk usage
df -h

# Clean up Docker
docker system prune -a --volumes

# Check log size
du -sh /var/agri-consultancy/logs/

# Archive old logs
tar -czf /var/agri-consultancy/logs/archive_$(date +%Y%m%d).tar.gz /var/agri-consultancy/logs/*.log.*
```

### Issue: High memory usage

```bash
# Check which container uses most memory
docker stats

# Restart container if needed
docker-compose -f docker-compose.prod.yml restart app
```

### Issue: 502 Bad Gateway Error

```bash
# Check if app container is running
docker-compose -f docker-compose.prod.yml ps app

# Check app logs
docker-compose -f docker-compose.prod.yml logs app

# Check Nginx logs
tail -f /var/lib/docker/volumes/vp-consultancy_nginx-logs/_data/vp-consultancy-error.log

# Restart Nginx
docker-compose -f docker-compose.prod.yml restart nginx
```

---

## Additional Commands

### Useful Docker Commands

```bash
# View all containers
docker ps -a

# View container logs
docker logs container_name

# Enter container shell
docker exec -it container_name /bin/bash

# Copy files from container
docker cp container_name:/path/to/file /local/path

# Restart all services
docker-compose -f docker-compose.prod.yml restart

# Stop all services
docker-compose -f docker-compose.prod.yml down

# Remove all containers and volumes
docker-compose -f docker-compose.prod.yml down -v

# View network details
docker network ls
docker network inspect vp-network
```

### Useful MySQL Commands

```bash
# Enter MySQL container
docker-compose -f docker-compose.prod.yml exec mysql mysql -u root -p$MYSQL_ROOT_PASSWORD

# Inside MySQL:
SHOW DATABASES;
USE vp_consultancy;
SHOW TABLES;
DESCRIBE table_name;
SELECT COUNT(*) FROM table_name;
SHOW PROCESSLIST;  # Active queries
```

---

## Support & Documentation

- Spring Boot: https://spring.io/projects/spring-boot
- Docker: https://docs.docker.com/
- Nginx: https://nginx.org/en/docs/
- Let's Encrypt: https://letsencrypt.org/docs/
- MySQL: https://dev.mysql.com/doc/
- Redis: https://redis.io/documentation

---

**Last Updated:** August 23, 2026
**Version:** 1.0

