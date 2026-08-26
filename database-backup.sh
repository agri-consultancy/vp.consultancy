0.0.0#00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000cript
# Automates MySQL database backup with compression and retention policy

set -e

# ========================================
# Configuration
# ========================================
COMPOSE_PROJECT_DIR="/opt/vp-consultancy"
COMPOSE_FILE="docker-compose.prod.yml"
BACKUP_BASE_DIR="/opt/vp-consultancy/backups"
BACKUP_RETENTION_DAYS=30  # Keep backups for 30 days
LOG_FILE="/var/log/vp-consultancy-backup.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# ========================================
# Functions
# ========================================

log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

create_backup_directory() {
    mkdir -p "$BACKUP_BASE_DIR/daily"
    mkdir -p "$BACKUP_BASE_DIR/weekly"
    mkdir -p "$BACKUP_BASE_DIR/monthly"
    chmod 700 "$BACKUP_BASE_DIR"
}

backup_database() {
    local backup_type=${1:-daily}

    log "Starting $backup_type database backup..."

    cd "$COMPOSE_PROJECT_DIR"

    # Load environment variables
    if [ -f ".env" ]; then
        set -a
        source .env
        set +a
    else
        log "${RED}✗${NC} .env file not found!"
        return 1
    fi

    local timestamp=$(date +%Y-%m-%d_%H-%M-%S)
    local backup_file="$BACKUP_BASE_DIR/$backup_type/vp_consultancy_${backup_type}_${timestamp}.sql.gz"

    # Perform backup
    if docker-compose -f "$COMPOSE_FILE" exec -T mysql mysqldump \
        -u root -p"$MYSQL_ROOT_PASSWORD" \
        --single-transaction \
        --quick \
        --lock-tables=false \
        "$MYSQL_DATABASE" | gzip > "$backup_file"; then

        local file_size=$(du -h "$backup_file" | cut -f1)
        log "${GREEN}✓${NC} Database backup successful"
        log "Backup file: $backup_file ($file_size)"

        # Verify backup integrity
        if gzip -t "$backup_file" 2>/dev/null; then
            log "${GREEN}✓${NC} Backup integrity verified"
        else
            log "${RED}✗${NC} Backup file is corrupted!"
            rm -f "$backup_file"
            return 1
        fi

        return 0
    else
        log "${RED}✗${NC} Database backup FAILED!"
        return 1
    fi
}

restore_database() {
    local backup_file=$1

    if [ -z "$backup_file" ]; then
        log "${RED}✗${NC} Backup file path required"
        return 1
    fi

    if [ ! -f "$backup_file" ]; then
        log "${RED}✗${NC} Backup file not found: $backup_file"
        return 1
    fi

    log "${YELLOW}⚠${NC} WARNING: This will RESTORE the database from: $backup_file"
    log "${YELLOW}⚠${NC} All current data will be OVERWRITTEN!"
    read -p "Are you sure you want to proceed? (yes/no): " confirm

    if [ "$confirm" != "yes" ]; then
        log "Restore operation cancelled"
        return 0
    fi

    log "Starting database restore..."

    cd "$COMPOSE_PROJECT_DIR"

    # Load environment variables
    if [ -f ".env" ]; then
        set -a
        source .env
        set +a
    else
        log "${RED}✗${NC} .env file not found!"
        return 1
    fi

    # Restore backup
    if gunzip -c "$backup_file" | docker-compose -f "$COMPOSE_FILE" exec -T mysql mysql \
        -u root -p"$MYSQL_ROOT_PASSWORD" \
        "$MYSQL_DATABASE"; then

        log "${GREEN}✓${NC} Database restore successful"
        log "Database restored from: $backup_file"
        return 0
    else
        log "${RED}✗${NC} Database restore FAILED!"
        return 1
    fi
}

cleanup_old_backups() {
    log "Cleaning up backups older than $BACKUP_RETENTION_DAYS days..."

    local deleted_count=0

    for backup_type in daily weekly monthly; do
        find "$BACKUP_BASE_DIR/$backup_type" -name "*.sql.gz" -mtime "+$BACKUP_RETENTION_DAYS" -delete
        deleted_count=$((deleted_count + $?))
    done

    log "${GREEN}✓${NC} Cleanup complete"
}

list_backups() {
    log "Available backups:"
    find "$BACKUP_BASE_DIR" -name "*.sql.gz" -type f | sort -r | while read backup; do
        local size=$(du -h "$backup" | cut -f1)
        local date=$(date -r "$backup" +'%Y-%m-%d %H:%M:%S')
        echo "  $date | $size | $backup"
    done
}

verify_backup() {
    local backup_file=$1

    if [ -z "$backup_file" ]; then
        log "${RED}✗${NC} Backup file path required"
        return 1
    fi

    if [ ! -f "$backup_file" ]; then
        log "${RED}✗${NC} Backup file not found: $backup_file"
        return 1
    fi

    log "Verifying backup: $backup_file"

    if gzip -t "$backup_file" 2>/dev/null; then
        log "${GREEN}✓${NC} Backup integrity verified"

        # Show backup statistics
        local size=$(du -h "$backup_file" | cut -f1)
        local uncompressed_size=$(gunzip -c "$backup_file" | wc -c)
        local ratio=$(echo "scale=2; $(stat -c%s "$backup_file") / $uncompressed_size * 100" | bc)

        log "Backup size: $size"
        log "Compression ratio: ${ratio}%"
        return 0
    else
        log "${RED}✗${NC} Backup file is CORRUPTED!"
        return 1
    fi
}

show_usage() {
    cat << EOF
Usage: $0 [COMMAND] [OPTIONS]

Commands:
    backup [TYPE]       Create a database backup
                       TYPE: daily, weekly, monthly (default: daily)

    restore [FILE]      Restore database from backup file
                       FILE: Full path to backup file

    list                List all available backups

    verify [FILE]       Verify backup file integrity
                       FILE: Full path to backup file

    cleanup             Remove backups older than $BACKUP_RETENTION_DAYS days

    help                Show this help message

Examples:
    # Create daily backup
    $0 backup daily

    # Create weekly backup
    $0 backup weekly

    # List all backups
    $0 list

    # Verify specific backup
    $0 verify /opt/vp-consultancy/backups/daily/vp_consultancy_daily_2026-08-23_10-30-00.sql.gz

    # Restore from specific backup
    $0 restore /opt/vp-consultancy/backups/daily/vp_consultancy_daily_2026-08-23_10-30-00.sql.gz

    # Cleanup old backups
    $0 cleanup

EOF
}

# ========================================
# Main Script Execution
# ========================================

main() {
    create_backup_directory

    local command=${1:-help}

    case "$command" in
        backup)
            backup_database "${2:-daily}"
            ;;
        restore)
            restore_database "$2"
            ;;
        list)
            list_backups
            ;;
        verify)
            verify_backup "$2"
            ;;
        cleanup)
            cleanup_old_backups
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

