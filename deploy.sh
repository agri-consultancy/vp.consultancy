#!/bin/bash

# VP Consultancy Application - Startup and Deployment Script
# Handles application initialization, startup, and validation

set -e

# ========================================
# Configuration
# ========================================
COMPOSE_PROJECT_DIR="/opt/vp-consultancy"
COMPOSE_FILE="docker-compose.prod.yml"
APP_NAME="VP Consultancy"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# ========================================
# Functions
# ========================================

print_header() {
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}========================================${NC}"
}

log() {
    echo -e "$1"
}

check_prerequisites() {
    print_header "Checking Prerequisites"

    local all_ok=true

    # Check if Docker is installed
    if command -v docker &> /dev/null; then
        log "${GREEN}✓${NC} Docker is installed"
    else
        log "${RED}✗${NC} Docker is NOT installed"
        all_ok=false
    fi

    # Check if Docker Compose is installed
    if command -v docker-compose &> /dev/null; then
        log "${GREEN}✓${NC} Docker Compose is installed"
    else
        log "${RED}✗${NC} Docker Compose is NOT installed"
        all_ok=false
    fi

    # Check if project directory exists
    if [ -d "$COMPOSE_PROJECT_DIR" ]; then
        log "${GREEN}✓${NC} Project directory exists: $COMPOSE_PROJECT_DIR"
    else
        log "${RED}✗${NC} Project directory not found: $COMPOSE_PROJECT_DIR"
        all_ok=false
    fi

    # Check if docker-compose file exists
    if [ -f "$COMPOSE_PROJECT_DIR/$COMPOSE_FILE" ]; then
        log "${GREEN}✓${NC} Docker Compose file exists"
    else
        log "${RED}✗${NC} Docker Compose file NOT found: $COMPOSE_FILE"
        all_ok=false
    fi

    # Check if .env file exists
    if [ -f "$COMPOSE_PROJECT_DIR/.env" ]; then
        log "${GREEN}✓${NC} Environment file (.env) exists"
    else
        log "${RED}✗${NC} Environment file (.env) NOT found"
        log "${YELLOW}⚠${NC} Please create .env file from .env.example"
        all_ok=false
    fi

    # Check required directories
    if [ -d "/var/agri-consultancy/logs" ]; then
        log "${GREEN}✓${NC} Logs directory exists"
    else
        log "${YELLOW}Creating logs directory...${NC}"
        mkdir -p /var/agri-consultancy/logs
        chmod 777 /var/agri-consultancy/logs
        log "${GREEN}✓${NC} Logs directory created"
    fi

    if [ -d "/var/lib/mysql-data" ]; then
        log "${GREEN}✓${NC} MySQL data directory exists"
    else
        log "${YELLOW}Creating MySQL data directory...${NC}"
        mkdir -p /var/lib/mysql-data
        chmod 777 /var/lib/mysql-data
        log "${GREEN}✓${NC} MySQL data directory created"
    fi

    return $([ "$all_ok" = true ] && echo 0 || echo 1)
}

build_images() {
    print_header "Building Docker Images"

    cd "$COMPOSE_PROJECT_DIR"

    log "${YELLOW}This may take several minutes on the first run...${NC}"

    if docker-compose -f "$COMPOSE_FILE" build; then
        log "${GREEN}✓${NC} Docker images built successfully"
        return 0
    else
        log "${RED}✗${NC} Failed to build Docker images"
        return 1
    fi
}

pull_images() {
    print_header "Pulling Docker Images"

    cd "$COMPOSE_PROJECT_DIR"

    if docker-compose -f "$COMPOSE_FILE" pull; then
        log "${GREEN}✓${NC} Docker images pulled successfully"
        return 0
    else
        log "${RED}✗${NC} Failed to pull images"
        return 1
    fi
}

start_services() {
    print_header "Starting Services"

    cd "$COMPOSE_PROJECT_DIR"

    log "Starting containers..."
    docker-compose -f "$COMPOSE_FILE" up -d

    if [ $? -eq 0 ]; then
        log "${GREEN}✓${NC} Services started"
        return 0
    else
        log "${RED}✗${NC} Failed to start services"
        return 1
    fi
}

wait_for_health() {
    print_header "Waiting for Services to be Healthy"

    cd "$COMPOSE_PROJECT_DIR"

    local max_attempts=60
    local attempt=1

    log "Waiting for database..."
    while [ $attempt -le $max_attempts ]; do
        if docker-compose -f "$COMPOSE_FILE" exec -T mysql mysqladmin ping -u root -p"$MYSQL_ROOT_PASSWORD" &> /dev/null; then
            log "${GREEN}✓${NC} MySQL is healthy"
            break
        fi

        if [ $attempt -eq $max_attempts ]; then
            log "${RED}✗${NC} MySQL failed to start within timeout"
            return 1
        fi

        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    log ""
    log "Waiting for Redis..."
    attempt=1
    while [ $attempt -le $max_attempts ]; do
        if docker-compose -f "$COMPOSE_FILE" exec -T redis redis-cli ping &> /dev/null; then
            log "${GREEN}✓${NC} Redis is healthy"
            break
        fi

        if [ $attempt -eq $max_attempts ]; then
            log "${RED}✗${NC} Redis failed to start within timeout"
            return 1
        fi

        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    log ""
    log "Waiting for Application..."
    attempt=1
    while [ $attempt -le $max_attempts ]; do
        if docker-compose -f "$COMPOSE_FILE" exec -T app curl -f http://localhost:8085/agri-consultancy-service/actuator/health &> /dev/null; then
            log "${GREEN}✓${NC} Application is healthy"
            break
        fi

        if [ $attempt -eq $max_attempts ]; then
            log "${RED}✗${NC} Application failed to start within timeout"
            return 1
        fi

        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done

    echo ""
    return 0
}

show_services_status() {
    print_header "Services Status"

    cd "$COMPOSE_PROJECT_DIR"
    docker-compose -f "$COMPOSE_FILE" ps
}

show_logs() {
    print_header "Application Startup Logs (Last 50 lines)"

    cd "$COMPOSE_PROJECT_DIR"
    docker-compose -f "$COMPOSE_FILE" logs --tail=50 app || true
}

show_summary() {
    print_header "$APP_NAME - Deployment Complete"

    log "${GREEN}✓${NC} All services are running!"
    log ""
    log "Quick References:"
    log "  Application Logs:     tail -f /var/agri-consultancy/logs/app.log"
    log "  Error Logs:           tail -f /var/agri-consultancy/logs/error.log"
    log "  Service Status:       docker-compose -f $COMPOSE_FILE ps"
    log "  Service Logs:         docker-compose -f $COMPOSE_FILE logs -f [service]"
    log "  Health Check:         curl https://api.agriconsultancy.tech/agri-consultancy-service/actuator/health"
    log "  Swagger UI:           https://api.agriconsultancy.tech/agri-consultancy-service/swagger-ui.html"
    log ""
    log "Useful Commands:"
    log "  Restart services:     docker-compose -f $COMPOSE_FILE restart"
    log "  Stop services:        docker-compose -f $COMPOSE_FILE down"
    log "  View all logs:        docker-compose -f $COMPOSE_FILE logs"
    log "  Backup database:      ./database-backup.sh backup daily"
    log "  Health check:         ./health-check.sh"
    log ""
    log "Documentation: See DEPLOYMENT_GUIDE.md for detailed information"
    log ""
}

show_usage() {
    cat << EOF
Usage: $0 [COMMAND]

Commands:
    start           Start the application (full deployment)
    restart         Restart all services
    stop            Stop all services
    status          Show services status
    logs            Show application logs (last 50 lines)
    build           Build Docker images
    pull            Pull Docker images
    clean           Stop and remove all containers/volumes
    help            Show this help message

Examples:
    # Full deployment
    $0 start

    # Restart services
    $0 restart

    # Check status
    $0 status

    # View logs
    $0 logs

EOF
}

# ========================================
# Main Script Execution
# ========================================

main() {
    local command=${1:-start}

    case "$command" in
        start)
            # Full deployment workflow
            check_prerequisites || exit 1
            build_images || exit 1
            start_services || exit 1
            wait_for_health || exit 1
            show_services_status
            show_logs
            show_summary
            ;;
        restart)
            print_header "Restarting Services"
            cd "$COMPOSE_PROJECT_DIR"
            docker-compose -f "$COMPOSE_FILE" restart
            show_services_status
            ;;
        stop)
            print_header "Stopping Services"
            cd "$COMPOSE_PROJECT_DIR"
            docker-compose -f "$COMPOSE_FILE" down
            log "${GREEN}✓${NC} Services stopped"
            ;;
        status)
            show_services_status
            ;;
        logs)
            show_logs
            ;;
        build)
            check_prerequisites || exit 1
            build_images || exit 1
            ;;
        pull)
            pull_images || exit 1
            ;;
        clean)
            print_header "Cleaning Up"
            cd "$COMPOSE_PROJECT_DIR"
            log "${YELLOW}⚠${NC} This will remove all containers, volumes, and data!"
            read -p "Are you sure? (yes/no): " confirm
            if [ "$confirm" = "yes" ]; then
                docker-compose -f "$COMPOSE_FILE" down -v
                log "${GREEN}✓${NC} Cleanup complete"
            else
                log "Cleanup cancelled"
            fi
            ;;
        help|--help|-h)
            show_usage
            ;;
        *)
            log "${RED}✗${NC} Unknown command: $command"
            show_usage
            exit 1
            ;;
    esac
}

main "$@"

