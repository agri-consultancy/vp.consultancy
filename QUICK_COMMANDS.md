# Quick Reference Guide - Production Commands

Keep this file handy for quick access to frequently used commands.

## 🚀 Deployment Commands

```bash
# Navigate to project
cd /opt/vp-consultancy

# Full deployment (build + start + verify)
./deploy.sh start

# Check status
./deploy.sh status

# View logs
./deploy.sh logs

# Restart services
./deploy.sh restart

# Stop services
./deploy.sh stop

# Clean up (removes containers and volumes - CAREFUL!)
./deploy.sh clean
```

## 📊 Status & Monitoring

```bash
# Check all containers status
docker-compose -f docker-compose.prod.yml ps

# View real-time resource usage
docker stats

# Check specific service status
docker-compose -f docker-compose.prod.yml ps mysql
docker-compose -f docker-compose.prod.yml ps redis
docker-compose -f docker-compose.prod.yml ps app
docker-compose -f docker-compose.prod.yml ps nginx
```

## 📝 View Logs

```bash
# Application logs (real-time)
tail -f /var/agri-consultancy/logs/app.log

# Error logs (real-time)
tail -f /var/agri-consultancy/logs/error.log

# Last 100 lines
tail -100 /var/agri-consultancy/logs/app.log

# Last 50 lines (Docker logs)
docker-compose -f docker-compose.prod.yml logs --tail=50

# Real-time Docker logs
docker-compose -f docker-compose.prod.yml logs -f

# Specific service logs
docker-compose -f docker-compose.prod.yml logs -f app
docker-compose -f docker-compose.prod.yml logs -f mysql
docker-compose -f docker-compose.prod.yml logs -f redis
docker-compose -f docker-compose.prod.yml logs -f nginx
```

## 💾 Database Backups

```bash
# Create daily backup
./database-backup.sh backup daily

# Create weekly backup
./database-backup.sh backup weekly

# Create monthly backup
./database-backup.sh backup monthly

# List all backups
./database-backup.sh list

# Verify backup integrity
./database-backup.sh verify /path/to/backup.sql.gz

# Restore from backup (interactive prompt)
./database-backup.sh restore /path/to/backup.sql.gz

# Clean up old backups (>30 days)
./database-backup.sh cleanup
```

## 🏥 Health Checks

```bash
# Run comprehensive health check
./health-check.sh

# Check application health endpoint
curl -k https://api.agriconsultancy.tech/agri-consultancy-service/actuator/health

# Check if app is responding
curl -k -i https://api.agriconsultancy.tech/agri-consultancy-service/swagger-ui.html

# Check database connection
docker-compose -f docker-compose.prod.yml exec mysql mysql -u root -p$MYSQL_ROOT_PASSWORD -e "SELECT 1"

# Check Redis connection
docker-compose -f docker-compose.prod.yml exec redis redis-cli ping

# Should return: PONG
```

## 🔄 Service Management

```bash
# Restart specific service
docker-compose -f docker-compose.prod.yml restart app
docker-compose -f docker-compose.prod.yml restart mysql
docker-compose -f docker-compose.prod.yml restart redis
docker-compose -f docker-compose.prod.yml restart nginx

# Restart all services
docker-compose -f docker-compose.prod.yml restart

# Stop all services
docker-compose -f docker-compose.prod.yml down

# Stop and remove all data
docker-compose -f docker-compose.prod.yml down -v

# Rebuild images
docker-compose -f docker-compose.prod.yml build

# Update and restart
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

## 🗄️ Database Operations

```bash
# Connect to MySQL
docker-compose -f docker-compose.prod.yml exec mysql mysql -u root -p$MYSQL_ROOT_PASSWORD

# Inside MySQL shell:
SHOW DATABASES;
USE vp_consultancy;
SHOW TABLES;
DESCRIBE table_name;
SELECT COUNT(*) FROM table_name;
SELECT * FROM table_name LIMIT 10;

# Exit MySQL
exit
```

## 📦 Redis Operations

```bash
# Connect to Redis CLI
docker-compose -f docker-compose.prod.yml exec redis redis-cli

# Inside Redis:
PING                    # Test connection
INFO                    # Show stats
DBSIZE                  # Show number of keys
KEYS *                  # List all keys
DEL key_name            # Delete key
FLUSHDB                 # Clear database
SAVE                    # Save to disk
BGSAVE                  # Background save

# Exit Redis
exit
```

## 📁 File System Operations

```bash
# Check disk space
df -h

# Check specific directory size
du -sh /var/agri-consultancy/logs/
du -sh /var/lib/mysql-data/
du -sh /opt/vp-consultancy/backups/

# List backup files
ls -lh /opt/vp-consultancy/backups/daily/
ls -lh /opt/vp-consultancy/backups/weekly/

# Archive old logs
tar -czf /var/agri-consultancy/logs/archive_$(date +%Y%m%d).tar.gz /var/agri-consultancy/logs/*.log.*

# Remove old logs
find /var/agri-consultancy/logs -name "*.log.*" -mtime +30 -delete
```

## 🔐 SSL/Certificate Management

```bash
# Check certificate status
certbot certificates

# View certificate details
certbot show --name api.agriconsultancy.tech

# Test renewal (dry-run)
certbot renew --dry-run

# Force renewal
certbot renew --force-renewal

# Check certificate expiration
openssl x509 -in /etc/letsencrypt/live/api.agriconsultancy.tech/cert.pem -noout -dates
```

## 🐛 Troubleshooting

```bash
# Container logs with error details
docker-compose -f docker-compose.prod.yml logs app | grep -i error

# Show only errors from app logs
grep ERROR /var/agri-consultancy/logs/app.log

# Check for specific error
grep "OutOfMemory" /var/agri-consultancy/logs/error*.log

# Find containers with issues
docker ps --filter "status=exited"

# Inspect container details
docker inspect container_name

# View container events
docker events --container app --tail 50
```

## 🔧 Configuration Changes

```bash
# Edit environment variables
nano /opt/vp-consultancy/.env

# Apply changes (restart containers)
cd /opt/vp-consultancy
docker-compose -f docker-compose.prod.yml up -d

# Reload Nginx config (no downtime)
docker-compose -f docker-compose.prod.yml exec nginx nginx -s reload

# Verify Nginx config
docker-compose -f docker-compose.prod.yml exec nginx nginx -t
```

## 📊 Performance Monitoring

```bash
# Real-time stats
docker stats

# Memory usage
docker stats --no-stream --format "{{.Container}}\t{{.MemUsage}}"

# CPU usage
ps aux | grep java

# Disk I/O stats
iostat -x 1

# Network stats
netstat -antp | grep java
```

## 🔍 Application Debugging

```bash
# Enter application container shell
docker-compose -f docker-compose.prod.yml exec app /bin/bash

# Copy file from container
docker cp vp-consultancy-app:/var/agri-consultancy/logs/app.log ./

# View environment variables in container
docker-compose -f docker-compose.prod.yml exec app env

# Run Java process inside container
docker-compose -f docker-compose.prod.yml exec app ps aux
```

## 📈 Scaling Tips

```bash
# Increase memory limit for container
# Edit docker-compose.prod.yml:
deploy:
  resources:
    limits:
      memory: 2G    # Change this value

# Rebuild and restart
docker-compose -f docker-compose.prod.yml up -d

# Increase database connection pool
# Edit .env or application-prod.properties:
spring.datasource.hikari.maximum-pool-size=30

# Increase Redis memory limit
# Edit docker-compose.prod.yml command section
--maxmemory 512mb  # Change this value
```

## 🚨 Emergency Procedures

```bash
# Hard stop all services (if required)
docker-compose -f docker-compose.prod.yml kill

# Force remove stuck container
docker rm -f container_name

# Clear Docker system space
docker system prune -a --volumes

# Emergency restore from backup
./database-backup.sh restore /opt/vp-consultancy/backups/daily/vp_consultancy_daily_2026-08-23_02-00-00.sql.gz

# Rollback to previous version
git checkout HEAD~1
./deploy.sh start
```

## 🎯 Useful Aliases (Optional)

Add to `.bashrc` or `.bash_profile`:

```bash
alias vp-status='docker-compose -f /opt/vp-consultancy/docker-compose.prod.yml ps'
alias vp-logs='tail -f /var/agri-consultancy/logs/app.log'
alias vp-errors='tail -f /var/agri-consultancy/logs/error.log'
alias vp-restart='cd /opt/vp-consultancy && ./deploy.sh restart'
alias vp-health='cd /opt/vp-consultancy && ./health-check.sh'
alias vp-backup='cd /opt/vp-consultancy && ./database-backup.sh backup daily'
alias vp-stats='docker stats'
```

Then reload: `source ~/.bashrc`

Use: `vp-status`, `vp-logs`, etc.

---

## 📌 Important Paths

```
/opt/vp-consultancy/             # Application directory
/var/agri-consultancy/logs/       # Application logs
/var/lib/mysql-data/              # Database files
/opt/vp-consultancy/backups/      # Database backups
/etc/letsencrypt/                 # SSL certificates
```

## 🔑 Environment Variables

```
MYSQL_ROOT_PASSWORD               # Database root password
MYSQL_DATABASE                    # Database name
MYSQL_USER                        # Database user
MYSQL_PASSWORD                    # Database user password
APP_JWT_SECRET                    # JWT secret key
SPRING_PROFILES_ACTIVE            # Active profile (prod)
```

## 📌 Port Map

```
Port 80   →  HTTP (redirects to HTTPS)
Port 443  →  HTTPS (Nginx/Application)
Port 3306 →  MySQL (internal only)
Port 6379 →  Redis (internal only)
Port 8085 →  App (internal only)
```

## ✅ Daily Checklist

- [ ] Application is running: `./deploy.sh status`
- [ ] No errors in logs: `grep ERROR /var/agri-consultancy/logs/app.log`
- [ ] Health check passes: `./health-check.sh`
- [ ] Disk space adequate: `df -h`
- [ ] Backups created: `ls -l /opt/vp-consultancy/backups/daily/`
- [ ] SSL certificate valid: `certbot certificates`

---

**Last Updated:** August 23, 2026  
**Version:** 1.0

