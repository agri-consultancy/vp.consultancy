# Production Deployment Checklist

Use this checklist to ensure everything is properly configured before deploying to production.

## Pre-Deployment Planning

### Infrastructure
- [ ] VPS provisioned and accessible
- [ ] Domain name registered and pointing to VPS IP
- [ ] Root/sudo access confirmed on VPS
- [ ] Operating System: Ubuntu 20.04 LTS or newer
- [ ] Minimum 2GB RAM verified
- [ ] Minimum 2 CPU cores verified
- [ ] SSD storage: 20GB+ available

### Communication & Documentation
- [ ] Team aware of deployment schedule
- [ ] Rollback plan documented
- [ ] Maintenance window communicated to users (if applicable)
- [ ] Support contacts listed
- [ ] Deployment guide reviewed by team

---

## Pre-Deployment Configuration

### Application Code
- [ ] All code committed and pushed to repository
- [ ] Latest version pulled/downloaded locally
- [ ] Database migrations up to date
- [ ] No sensitive data in code (check .gitignore)
- [ ] Application builds successfully locally
- [ ] All tests passing: `mvn clean test`

### Docker Files
- [ ] Dockerfile present and reviewed
- [ ] docker-compose.prod.yml configured with Nginx
- [ ] .dockerignore properly configured
- [ ] Build tested locally

### Environment Configuration
- [ ] .env.example matches required variables
- [ ] .env file created from .env.example (only on VPS)
- [ ] Strong passwords generated (16+ characters)
- [ ] JWT secret generated: `openssl rand -base64 64`
- [ ] MYSQL_ROOT_PASSWORD set to unique strong value
- [ ] MYSQL_PASSWORD set to unique strong value
- [ ] Database name configured
- [ ] Database user name configured
- [ ] .env file permissions set to 600
- [ ] .env file NOT committed to version control

### SSL/HTTPS
- [ ] Domain name confirmed and accessible
- [ ] SSL certificate requirements understood
- [ ] email for Let's Encrypt prepared
- [ ] Firewall rules allow port 80 and 443
- [ ] nginx.conf domain name updated
- [ ] certbot installation planned

### Logging Configuration
- [ ] Log directory path: /var/agri-consultancy/logs/ verified
- [ ] Log rotation retention set (7 days confirmed)
- [ ] logback-spring.xml reviewed
- [ ] application-prod.properties reviewed
- [ ] Error logs separate from application logs (configured)

### Database
- [ ] MySQL version compatible (8.0 recommended)
- [ ] Initial SQL scripts up to date (db_setup.sql)
- [ ] Database backup strategy planned
- [ ] Backup retention policy confirmed (30 days)
- [ ] Backup schedule configured

### Caching
- [ ] Redis required for application (confirmed)
- [ ] Redis configuration correct
- [ ] Redis persistence enabled
- [ ] Redis memory limits set (256MB)

---

## Pre-Deployment VPS Setup

### SSH Access
- [ ] SSH key-based authentication (recommended)
- [ ] SSH password authentication disabled (recommended)
- [ ] SSH port changed from default if desired
- [ ] Firewall rules for SSH configured

### System Updates
- [ ] System packages updated: `apt update && apt upgrade`
- [ ] Kernel up to date
- [ ] Security patches applied

### Directory Structure
- [ ] /opt/vp-consultancy/ directory created
- [ ] /var/agri-consultancy/logs/ directory created with permissions
- [ ] /var/lib/mysql-data/ directory created with permissions
- [ ] Directory permissions verified (chmod 777)

### Docker Installation
- [ ] Docker installed
- [ ] Docker Compose installed (version 2+)
- [ ] Docker daemon started and enabled
- [ ] Docker service status verified: `systemctl status docker`
- [ ] Current user added to docker group (optional but recommended)

### SSL Certificate (Pre-generated)
- [ ] Certbot installed
- [ ] Initial certificate generated before deployment
- [ ] Certificate location verified: /etc/letsencrypt/live/api.agriconsultancy.tech/
- [ ] Certificate renewal permissions set

### Firewall Configuration
- [ ] Port 80 (HTTP) allowed
- [ ] Port 443 (HTTPS) allowed
- [ ] Port 3306 (MySQL) blocked from external access
- [ ] Port 6379 (Redis) blocked from external access
- [ ] Port 8085 (App) blocked from external access (only via Nginx)
- [ ] SSH port configured and allowed

---

## Deployment Execution

### Pre-Deployment
- [ ] Latest changes merged to main branch
- [ ] No uncommitted changes in working directory
- [ ] All team members notified of deployment
- [ ] Backup created before deployment
- [ ] Downtime window opened (if applicable)

### Uploading Application
- [ ] Project files uploaded to /opt/vp-consultancy/
- [ ] Dockerfile present and verified
- [ ] docker-compose.prod.yml present and verified
- [ ] nginx.conf present and updated with domain
- [ ] logback-spring.xml present
- [ ] application-prod.properties present
- [ ] Deploy scripts uploaded and made executable

### Pre-Deployment Scripts
- [ ] Certificates directory structure correct: /etc/letsencrypt/
- [ ] Certbot auto-renewal configured
- [ ] Health check script in place and executable
- [ ] Backup script in place and executable
- [ ] Deploy script in place and executable

### Database Preparation
- [ ] Database backup created: `./database-backup.sh backup daily`
- [ ] Backup verified: `./database-backup.sh verify <backup_file>`
- [ ] Database user created in .env
- [ ] Database name configured in .env

### Deployment Commands
- [ ] Navigate to project directory: `cd /opt/vp-consultancy`
- [ ] Build Docker images: `./deploy.sh start` or `docker-compose -f docker-compose.prod.yml build`
- [ ] Start services: `./deploy.sh start` or `docker-compose -f docker-compose.prod.yml up -d`
- [ ] Wait for containers to become healthy (3-5 minutes)
- [ ] Monitor logs: `./deploy.sh logs` or `docker-compose -f docker-compose.prod.yml logs -f app`

---

## Post-Deployment Verification

### Service Status
- [ ] All containers running: `docker-compose -f docker-compose.prod.yml ps`
  - [ ] mysql is healthy
  - [ ] redis is healthy
  - [ ] app is healthy
  - [ ] nginx is running
  - [ ] certbot is running

### Application Health
- [ ] Health endpoint responds: `curl https://api.agriconsultancy.tech/agri-consultancy-service/actuator/health`
- [ ] Returns status: UP
- [ ] Database component: UP
- [ ] Redis component: UP

### API Endpoints
- [ ] Main endpoints responding correctly
- [ ] Error responses are proper (4xx/5xx)
- [ ] API authentication working
- [ ] Rate limiting working (if configured)

### Swagger Documentation
- [ ] Swagger UI accessible: https://api.agriconsultancy.tech/agri-consultancy-service/swagger-ui.html
- [ ] All endpoints documented
- [ ] Can execute test requests

### SSL/HTTPS
- [ ] HTTPS works: https://api.agriconsultancy.tech/
- [ ] HTTP redirects to HTTPS
- [ ] Security headers present
- [ ] SSL Labs score is A or higher: https://www.ssllabs.com/ssltest/

### Logging
- [ ] Application logs being written: `tail /var/agri-consultancy/logs/app.log`
- [ ] Error logs being written: `tail /var/agri-consultancy/logs/error.log`
- [ ] Log rotation configured correctly
- [ ] Log files have proper permissions

### Database
- [ ] Database connected and working
- [ ] Data persisted correctly
- [ ] All tables created
- [ ] Backup schedule working

### Redis
- [ ] Redis connected and working
- [ ] Cache operations working
- [ ] Redis data persists correctly

### Performance
- [ ] CPU usage normal: `docker stats`
- [ ] Memory usage normal: `docker stats`
- [ ] Disk space adequate: `df -h`
- [ ] Response times acceptable

### Security
- [ ] Non-root user running container
- [ ] Sensitive data not in logs
- [ ] Database password not exposed
- [ ] JWT secrets not exposed
- [ ] Security headers configured

---

## Post-Deployment Configuration

### Monitoring & Alerts
- [ ] Health check script scheduled: `crontab -e`
  - [ ] Every 30 minutes: `*/30 * * * * /opt/vp-consultancy/health-check.sh`
- [ ] Log monitoring configured
- [ ] Alert system configured (email/Slack/Telegram)
- [ ] Dashboard or monitoring tool configured

### Automated Backups
- [ ] Database backup scheduled daily: `crontab -e`
  - [ ] Daily at 2 AM: `0 2 * * * /opt/vp-consultancy/database-backup.sh backup daily`
- [ ] Weekly backup scheduled
  - [ ] Weekly on Sunday at 3 AM: `0 3 * * 0 /opt/vp-consultancy/database-backup.sh backup weekly`
- [ ] Old backups auto-cleaned
  - [ ] Weekly: `0 4 * * 0 /opt/vp-consultancy/database-backup.sh cleanup`

### Log Rotation
- [ ] Log rotation configured: `/etc/logrotate.d/vp-consultancy`
- [ ] Rotation tested manually
- [ ] Archive location configured

### SSL Certificate Renewal
- [ ] Certbot renewal schedule verified
- [ ] Auto-renewal working
- [ ] Renewal tested: `certbot renew --dry-run`

### Documentation
- [ ] Deployment procedures documented
- [ ] Emergency procedures documented
- [ ] Rollback procedures documented
- [ ] Team informed about procedures

---

## Post-Deployment Testing

### Functional Testing
- [ ] All critical user journeys tested
- [ ] Data input/output working correctly
- [ ] File uploads/downloads working
- [ ] Search functionality working
- [ ] Authentication & authorization working

### Load Testing (if applicable)
- [ ] Basic load test performed
- [ ] Application stable under expected load
- [ ] No memory leaks observed (monitor over time)
- [ ] Response times acceptable

### Integration Testing
- [ ] Third-party API integrations working
- [ ] Webhook endpoints responding
- [ ] Email notifications working (if applicable)
- [ ] File processing working (if applicable)

### Error Handling
- [ ] Error pages rendering correctly
- [ ] Proper HTTP status codes returned
- [ ] Errors logged correctly
- [ ] Stack traces not exposed to users

### Security Testing (Basic)
- [ ] SQL injection attempts blocked
- [ ] XSS attempts blocked
- [ ] CORS properly configured
- [ ] JWT tokens validating correctly

---

## Final Sign-Off

### Documentation Review
- [ ] All documentation updated
- [ ] README.md reflects production setup
- [ ] DEPLOYMENT_GUIDE.md reviewed
- [ ] DOCKER_README.md reviewed
- [ ] Runbook created for team

### Team Notifications
- [ ] Deployment completion announced
- [ ] Access information shared
- [ ] Maintenance window closed
- [ ] Users notified if applicable

### Monitoring
- [ ] Monitoring dashboard accessed
- [ ] Alerts tested and working
- [ ] Team access to monitoring confirmed
- [ ] 24/7 support ready

### Final Checklist
- [ ] All checks above completed
- [ ] No critical issues remaining
- [ ] Application fully operational
- [ ] Deployment successful ✅

---

## Deployment Metadata

**Deployment Date:** _______________

**Deployed By:** _______________

**Reviewed By:** _______________

**Version:** _______________

**Domain:** _______________

**VPS Provider:** Hostinger

**VPS IP:** _______________

**Notes:**

```
_________________________________________________________________

_________________________________________________________________

_________________________________________________________________
```

---

**Document Status:** Ready for Production ✅

**Last Updated:** August 23, 2026

