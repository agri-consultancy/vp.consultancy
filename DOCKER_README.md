# VP Consultancy - Docker Production Deployment Setup

This directory contains all necessary files for deploying the VP Consultancy application to production using Docker on Hostinger VPS or any Linux server.

## 📋 Quick Start

### Prerequisites
- Linux VPS (Ubuntu 20.04 LTS or newer recommended)
- Docker and Docker Compose installed
- Domain name pointing to your VPS
- Root or sudo access to VPS

### Step 1: Prepare Configuration (On Your Local Machine)

```bash
# Copy and customize environment configuration
cp .env.example .env

# Edit .env with your production values
nano .env
# Set strong passwords and JWT secret
```

### Step 2: Upload to VPS

```bash
# Upload your project to VPS
scp -r . root@your_vps_ip:/opt/vp-consultancy/
```

### Step 3: Deploy on VPS

```bash
# SSH into your VPS
ssh root@your_vps_ip

# Navigate to project directory
cd /opt/vp-consultancy

# Make scripts executable
chmod +x *.sh

# Run deployment
./deploy.sh start
```

## 📁 File Structure

```
.
├── Dockerfile                    # Multi-stage Docker build for Spring Boot
├── docker-compose.yml            # Compose file for basic setup (MySQL, Redis, App)
├── docker-compose.prod.yml       # Compose file with Nginx reverse proxy (RECOMMENDED)
├── nginx.conf                    # Nginx configuration with SSL/TLS
├── logback-spring.xml            # Logging configuration with file rotation
├── application-prod.properties   # Production Spring Boot configuration
├── .env.example                  # Environment variables template
├── .dockerignore                 # Exclude files from Docker build
│
├── deploy.sh                     # Main deployment script
├── health-check.sh               # Health monitoring script
├── database-backup.sh            # Database backup/restore script
│
└── DEPLOYMENT_GUIDE.md           # Detailed deployment documentation
```

## 🚀 Deployment Files Explained

### Dockerfile
- Multi-stage build for optimized image size
- Builds application with Maven in first stage
- Creates lightweight runtime image with Java 21 JRE
- Non-root user for security
- Health check endpoint configuration

### docker-compose.prod.yml (RECOMMENDED)
**Services:**
- **MySQL**: Database with data persistence
- **Redis**: Cache with data persistence
- **App**: Spring Boot application
- **Nginx**: Reverse proxy with SSL/TLS
- **Certbot**: Automatic SSL certificate management

**Why use this instead of docker-compose.yml?**
- SSL/HTTPS support with automatic certificate renewal
- Nginx reverse proxy for better performance
- Security headers configured
- Gzip compression enabled
- Better production practices

### nginx.conf
- Reverse proxy configuration
- SSL/TLS with Let's Encrypt
- Security headers (HSTS, X-Frame-Options, etc.)
- Gzip compression
- Rate limiting ready
- Accessible API documentation

### logback-spring.xml
- Structured logging with file output
- Automatic log rotation (daily, customizable retention)
- Separate error log file
- Async appenders for performance
- Configurable log levels per profile

### application-prod.properties
- Production-specific Spring Boot settings
- Environment variable substitution
- Enhanced HikariCP connection pooling
- Production logging configuration
- Security settings

## 📝 Configuration

### 1. Environment Variables (.env file)

Copy `.env.example` and update with your values:

```bash
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_DATABASE=vp_consultancy
MYSQL_USER=consultancy_user
MYSQL_PASSWORD=your_secure_password
APP_JWT_SECRET=your_generated_secure_jwt_secret
```

**Important Security Notes:**
- Use strong passwords (minimum 16 characters)
- Don't commit `.env` to version control
- Regenerate secrets for each production environment
- Use `openssl rand -base64 64` to generate secure keys

### 2. SSL Certificate Setup

```bash
# Generate SSL certificate before starting
certbot certonly --standalone \
  -d api.agriconsultancy.tech \
  -d www.api.agriconsultancy.tech \
  --email your-email@example.com

# Certificate location: /etc/letsencrypt/live/api.agriconsultancy.tech/
```

Edit nginx.conf to use your domain:
```bash
sed -i 's/yourdomain.com/api.agriconsultancy.tech/g' nginx.conf
```

### 3. Log Configuration

Logs are saved to: `/var/agri-consultancy/logs/`

**Log Files:**
- `app.log` - All application logs
- `error.log` - Errors only (full stack trace)
- `app.YYYY-MM-DD.N.log` - Daily rotated logs
- `error.YYYY-MM-DD.N.log` - Daily rotated error logs

**Retention Policy:** 7 days (configurable in logback-spring.xml)

## 🔧 Usage

### Initial Deployment

```bash
# Full deployment with health checks
./deploy.sh start
```

### Common Operations

```bash
# Check service status
./deploy.sh status

# View application logs
./deploy.sh logs

# Restart services
./deploy.sh restart

# Stop services
./deploy.sh stop

# View latest logs
tail -f /var/agri-consultancy/logs/app.log
```

### Database Operations

```bash
# Create daily backup
./database-backup.sh backup daily

# Create weekly backup
./database-backup.sh backup weekly

# List all backups
./database-backup.sh list

# Restore from backup
./database-backup.sh restore /path/to/backup.sql.gz

# Verify backup integrity
./database-backup.sh verify /path/to/backup.sql.gz
```

### Health Monitoring

```bash
# Run health check
./health-check.sh

# Add to crontab for automatic checks (every 30 minutes)
crontab -e
# Add: */30 * * * * /opt/vp-consultancy/health-check.sh
```

## 📊 Monitoring & Logs

### View Application Logs

```bash
# Real-time logs
tail -f /var/agri-consultancy/logs/app.log

# Error logs
tail -f /var/agri-consultancy/logs/error.log

# Last 100 lines
tail -100 /var/agri-consultancy/logs/app.log

# Search logs
grep "ERROR" /var/agri-consultancy/logs/app.log
```

### View Container Logs

```bash
# Application logs
docker-compose -f docker-compose.prod.yml logs -f app

# All services
docker-compose -f docker-compose.prod.yml logs -f

# Specific service
docker-compose -f docker-compose.prod.yml logs -f <service_name>
```

### Monitor Resources

```bash
# CPU and memory usage
docker stats

# Disk space
df -h

# Log directory size
du -sh /var/agri-consultancy/logs/

# Process list
docker ps
```

## 🛡️ Security Best Practices

1. **Passwords & Secrets**
   - Use strong passwords (16+ characters)
   - Don't commit .env file
   - Rotate secrets regularly

2. **SSL/TLS**
   - Always use HTTPS in production
   - Let's Encrypt certificates auto-renew
   - Nginx handles SSL termination

3. **Network Security**
   - Firewall: Block non-essential ports
   - Only expose port 80 (HTTP redirect) and 443 (HTTPS)
   - Database and Redis run on private network

4. **Application Security**
   - Runs as non-root user
   - Read-only root filesystem (can be added to Dockerfile)
   - Health checks for automatic detection

5. **Data Security**
   - Database data persisted to `/var/lib/mysql-data/`
   - Regular backups to `/opt/vp-consultancy/backups/`
   - Keep 30 days of backups (configurable)

## 🔄 Maintenance

### Regular Tasks

**Daily:**
- Monitor application health: `./health-check.sh`
- Check disk space: `df -h`
- Review error logs: `tail -f /var/agri-consultancy/logs/error.log`

**Weekly:**
- Create backup: `./database-backup.sh backup weekly`
- Review logs for patterns
- Check SSL certificate status: `certbot certificates`

**Monthly:**
- Update Docker images: `docker-compose -f docker-compose.prod.yml pull`
- Clean up old backups: `./database-backup.sh cleanup`
- Archive old logs

### Updating Application

```bash
cd /opt/vp-consultancy
git pull origin main
docker-compose -f docker-compose.prod.yml build
docker-compose -f docker-compose.prod.yml up -d
./deploy.sh logs
```

## 🐛 Troubleshooting

### Container won't start
```bash
# Check logs
docker-compose -f docker-compose.prod.yml logs app

# Restart
docker-compose -f docker-compose.prod.yml restart app
```

### Database connection error
```bash
# Check MySQL status
docker-compose -f docker-compose.prod.yml ps mysql

# Restart MySQL
docker-compose -f docker-compose.prod.yml restart mysql

# Test connection
docker-compose -f docker-compose.prod.yml exec mysql mysql -u root -p$MYSQL_ROOT_PASSWORD -e "SELECT 1"
```

### 502 Bad Gateway (Nginx)
```bash
# Check app logs
docker-compose -f docker-compose.prod.yml logs app

# Check Nginx logs
tail -f /var/lib/docker/volumes/vp-consultancy_nginx-logs/_data/vp-consultancy-error.log

# Restart Nginx
docker-compose -f docker-compose.prod.yml restart nginx
```

### Out of disk space
```bash
# Check space
df -h

# Clean Docker
docker system prune -a

# Archive old logs
tar -czf /var/agri-consultancy/logs/archive_$(date +%Y%m%d).tar.gz /var/agri-consultancy/logs/*.log.*
```

## 📚 Additional Resources

- **Deployment Guide**: See `DEPLOYMENT_GUIDE.md`
- **Docker Docs**: https://docs.docker.com/
- **Spring Boot**: https://spring.io/projects/spring-boot
- **Nginx**: https://nginx.org/en/docs/
- **MySQL**: https://dev.mysql.com/doc/
- **Redis**: https://redis.io/documentation

## 📞 Support

For issues or questions:
1. Check logs in `/var/agri-consultancy/logs/`
2. Review `DEPLOYMENT_GUIDE.md` troubleshooting section
3. Run health check: `./health-check.sh`

---

**Last Updated:** August 23, 2026  
**Version:** 1.0  
**Status:** Production Ready ✅

