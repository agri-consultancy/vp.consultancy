#!/bin/bash

# Production Health Check and Monitoring Script
# This script monitors the application and services on the VPS
# Run it periodically via cron for automated health checks

set -e

# ========================================
# Configuration
# ========================================
APPLICATION_URL="https://api.agriconsultancy.tech/agri-consultancy-service"
HEALTH_ENDPOINT="/actuator/health"
COMPOSE_PROJECT_DIR="/opt/vp-consultancy"
COMPOSE_FILE="docker-compose.prod.yml"
LOG_FILE="/var/agri-consultancy/logs/health-check.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# ========================================
# Functions
# ========================================

log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

check_docker_services() {
    log "=== Checking Docker Services ==="

    cd "$COMPOSE_PROJECT_DIR"

    local services=("mysql" "redis" "app" "nginx")
    local all_healthy=true

    for service in "${services[@]}"; do
        local status=$(docker-compose -f "$COMPOSE_FILE" ps "$service" --format "{{.State}}" 2>/dev/null || echo "Not Running")

        if [[ "$status" == "running" ]]; then
            log "${GREEN}✓${NC} $service is running"
        else
            log "${RED}✗${NC} $service is NOT running (Status: $status)"
            all_healthy=false
        fi
    done

    return $([ "$all_healthy" = true ] && echo 0 || echo 1)
}

check_database_connection() {
    log "=== Checking Database Connection ==="

    cd "$COMPOSE_PROJECT_DIR"

    if docker-compose -f "$COMPOSE_FILE" exec -T mysql mysqladmin ping -u root -p"$MYSQL_ROOT_PASSWORD" &> /dev/null; then
        log "${GREEN}✓${NC} MySQL database is responding"
        return 0
    else
        log "${RED}✗${NC} MySQL database is NOT responding"
        return 1
    fi
}

check_redis_connection() {
    log "=== Checking Redis Connection ==="

    cd "$COMPOSE_PROJECT_DIR"

    if docker-compose -f "$COMPOSE_FILE" exec -T redis redis-cli ping &> /dev/null; then
        log "${GREEN}✓${NC} Redis is responding"
        return 0
    else
        log "${RED}✗${NC} Redis is NOT responding"
        return 1
    fi
}

check_application_health() {
    log "=== Checking Application Health ==="

    local response=$(curl -sk -m 5 "$APPLICATION_URL$HEALTH_ENDPOINT" 2>/dev/null || echo "FAILED")

    if [[ "$response" == *"UP"* ]]; then
        log "${GREEN}✓${NC} Application is UP"
        log "Health Check Response: $response"
        return 0
    else
        log "${RED}✗${NC} Application health check FAILED"
        log "Response: $response"
        return 1
    fi
}

check_api_endpoints() {
    log "=== Checking API Endpoints ==="

    # Add your API endpoints here
    local endpoints=(
        "$APPLICATION_URL/api/endpoint1"
        "$APPLICATION_URL/api/endpoint2"
    )

    local all_responding=true

    for endpoint in "${endpoints[@]}"; do
        local http_code=$(curl -sk -m 5 -o /dev/null -w "%{http_code}" "$endpoint" 2>/dev/null || echo "000")

        if [[ "$http_code" =~ ^[2345] ]]; then
            log "${GREEN}✓${NC} $endpoint - HTTP $http_code"
        else
            log "${RED}✗${NC} $endpoint - HTTP $http_code"
            all_responding=false
        fi
    done

    return $([ "$all_responding" = true ] && echo 0 || echo 1)
}

check_disk_space() {
    log "=== Checking Disk Space ==="

    local disk_usage=$(df / | awk 'NR==2 {print $5}' | sed 's/%//')
    local threshold=80

    if [ "$disk_usage" -lt "$threshold" ]; then
        log "${GREEN}✓${NC} Disk usage: ${disk_usage}%"
    else
        log "${RED}✗${NC} Disk usage is HIGH: ${disk_usage}%"
        return 1
    fi
}

check_memory_usage() {
    log "=== Checking Memory Usage ==="

    local memory_usage=$(free | grep Mem | awk '{printf("%.0f", $3/$2 * 100.0)}')
    local threshold=85

    if [ "$memory_usage" -lt "$threshold" ]; then
        log "${GREEN}✓${NC} Memory usage: ${memory_usage}%"
    else
        log "${RED}✗${NC} Memory usage is HIGH: ${memory_usage}%"
        return 1
    fi
}

check_log_files() {
    log "=== Checking Log Files ==="

    local log_dir="/var/agri-consultancy/logs"

    if [ -d "$log_dir" ]; then
        local log_size=$(du -sh "$log_dir" | awk '{print $1}')
        log "${GREEN}✓${NC} Log directory size: $log_size"

        # Check for recent errors
        local error_count=$(grep -c "\[ERROR\]" "$log_dir/error.log" 2>/dev/null || echo "0")
        if [ "$error_count" -gt 0 ]; then
            log "${YELLOW}⚠${NC} Found $error_count errors in error.log"
        fi
    else
        log "${RED}✗${NC} Log directory not found: $log_dir"
        return 1
    fi
}

restart_failed_service() {
    local service=$1
    log "Attempting to restart $service..."

    cd "$COMPOSE_PROJECT_DIR"
    docker-compose -f "$COMPOSE_FILE" restart "$service"

    sleep 5
}

send_alert() {
    local message=$1
    # Implement your alerting mechanism here
    # Examples:
    # - Send email
    # - Send Slack notification
    # - Send Telegram message
    # - Log to monitoring system

    log "${RED}ALERT:${NC} $message"
}

# ========================================
# Main Health Check Execution
# ========================================

main() {
    log "=========================================="
    log "Starting VP Consultancy Health Check"
    log "=========================================="

    local checks_failed=0

    # Load environment
    if [ -f "$COMPOSE_PROJECT_DIR/.env" ]; then
        set -a
        source "$COMPOSE_PROJECT_DIR/.env"
        set +a
    else
        log "${RED}✗${NC} .env file not found!"
        return 1
    fi

    # Run all checks
    check_docker_services || ((checks_failed++))
    check_database_connection || ((checks_failed++))
    check_redis_connection || ((checks_failed++))
    check_disk_space || ((checks_failed++))
    check_memory_usage || ((checks_failed++))
    check_log_files || ((checks_failed++))

    # Only check application health if Docker services are running
    if [ $checks_failed -eq 0 ]; then
        check_application_health || ((checks_failed++))
        # check_api_endpoints || ((checks_failed++))
    else
        log "${YELLOW}⚠${NC} Skipping application health check due to service failures"
    fi

    # Summary
    log "=========================================="
    if [ $checks_failed -eq 0 ]; then
        log "${GREEN}✓ ALL CHECKS PASSED${NC}"
    else
        log "${RED}✗ HEALTH CHECK FAILED - $checks_failed check(s) failed${NC}"
        send_alert "VP Consultancy health check failed - $checks_failed issue(s) detected"
        return 1
    fi
    log "=========================================="
}

# Run main function
main "$@"

