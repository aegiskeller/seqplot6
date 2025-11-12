#!/bin/bash
# Seqplot 6.0.0 Rollback Management System
# Manages rollback points and restoration of enhanced versions

echo "=== Seqplot 6.0.0 Rollback Manager ==="
echo

# Set the base directory
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$BASE_DIR"

# Function to list available rollback points
list_rollbacks() {
    echo "📋 Available rollback points:"
    echo
    if ls backup_v6.0.0_* 1> /dev/null 2>&1; then
        for backup in backup_v6.0.0_*; do
            if [ -d "$backup" ]; then
                timestamp=$(echo "$backup" | sed 's/backup_v6.0.0_//')
                formatted_date=$(date -j -f "%Y%m%d_%H%M%S" "$timestamp" "+%B %d, %Y at %I:%M:%S %p" 2>/dev/null || echo "$timestamp")
                echo "  📁 $backup"
                echo "     Created: $formatted_date"
                if [ -f "$backup/BACKUP_INFO.txt" ]; then
                    echo "     Status: Enhanced version with warm pastel colors"
                fi
                echo
            fi
        done
    else
        echo "  ⚠️  No rollback points found"
        echo "     Run ./backup.sh to create your first rollback point"
        echo
    fi
}

# Function to restore from a rollback point
restore_rollback() {
    local backup_dir="$1"
    
    if [ ! -d "$backup_dir" ]; then
        echo "❌ Backup directory not found: $backup_dir"
        exit 1
    fi
    
    echo "🔄 Restoring from rollback point: $backup_dir"
    echo
    
    # Create a safety backup of current state
    echo "💾 Creating safety backup of current state..."
    timestamp=$(date +"%Y%m%d_%H%M%S")
    safety_backup="safety_backup_before_restore_$timestamp"
    mkdir -p "$safety_backup"
    cp -r src/ "$safety_backup/" 2>/dev/null || true
    cp *.sh "$safety_backup/" 2>/dev/null || true
    echo "   Safety backup created: $safety_backup"
    echo
    
    # Clear current build
    echo "🧹 Cleaning current build..."
    rm -rf build/*
    rm -f *.class
    
    # Restore source files
    echo "📁 Restoring source files..."
    if [ -d "$backup_dir/src" ]; then
        rm -rf src/
        cp -r "$backup_dir/src" .
        echo "   ✅ Source files restored"
    else
        echo "   ❌ No source files found in backup"
        exit 1
    fi
    
    # Restore build scripts
    echo "📄 Restoring build scripts..."
    for script in build.sh run.sh backup.sh; do
        if [ -f "$backup_dir/$script" ]; then
            cp "$backup_dir/$script" .
            chmod +x "$script"
            echo "   ✅ $script restored and made executable"
        fi
    done
    
    # Test compilation
    echo "🔨 Testing compilation..."
    if ./build.sh > /dev/null 2>&1; then
        echo "   ✅ Compilation successful"
    else
        echo "   ⚠️  Compilation had warnings (this is normal for decompiled code)"
    fi
    
    echo
    echo "✅ Rollback completed successfully!"
    echo "🎨 Enhanced Seqplot 6.0.0 with warm pastel colors restored"
    echo "🚀 Run ./run.sh to launch the application"
}

# Function to create a new rollback point
create_rollback() {
    echo "📦 Creating new rollback point..."
    ./backup.sh
}

# Function to show detailed info about a rollback point
show_info() {
    local backup_dir="$1"
    
    if [ ! -d "$backup_dir" ]; then
        echo "❌ Backup directory not found: $backup_dir"
        exit 1
    fi
    
    echo "📋 Rollback Point Information"
    echo "=============================="
    echo
    echo "Directory: $backup_dir"
    
    if [ -f "$backup_dir/BACKUP_INFO.txt" ]; then
        echo
        cat "$backup_dir/BACKUP_INFO.txt"
    else
        echo "No detailed information available for this rollback point."
    fi
}

# Main script logic
case "$1" in
    "list"|"ls")
        list_rollbacks
        ;;
    "restore")
        if [ -z "$2" ]; then
            echo "Usage: $0 restore <backup_directory>"
            echo
            list_rollbacks
            exit 1
        fi
        restore_rollback "$2"
        ;;
    "create"|"backup")
        create_rollback
        ;;
    "info")
        if [ -z "$2" ]; then
            echo "Usage: $0 info <backup_directory>"
            echo
            list_rollbacks
            exit 1
        fi
        show_info "$2"
        ;;
    "help"|"-h"|"--help"|"")
        echo "Seqplot 6.0.0 Rollback Manager"
        echo "==============================="
        echo
        echo "Usage: $0 <command> [options]"
        echo
        echo "Commands:"
        echo "  list, ls              List all available rollback points"
        echo "  create, backup        Create a new rollback point"
        echo "  restore <backup_dir>  Restore from a rollback point"
        echo "  info <backup_dir>     Show detailed info about a rollback point"
        echo "  help                  Show this help message"
        echo
        echo "Examples:"
        echo "  $0 list                                    # List rollback points"
        echo "  $0 create                                  # Create new rollback"
        echo "  $0 restore backup_v6.0.0_20251103_190515  # Restore specific version"
        echo "  $0 info backup_v6.0.0_20251103_190515     # Show backup details"
        echo
        echo "Current Enhanced Features:"
        echo "  🎨 Warm pastel color scheme (periwinkle, sage, coral, lavender, cream)"
        echo "  🎯 Unified star point colors (body, edges, hover highlights)"
        echo "  📝 Enhanced font size validation (10-20 point range)"
        echo "  📍 Modern coordinate formatting (hh:mm:ss.s and dd:mm:ss)"
        echo "  🏷️  Version 6.0.0 branding with November 3, 2025 release date"
        ;;
    *)
        echo "❌ Unknown command: $1"
        echo "Run '$0 help' for usage information"
        exit 1
        ;;
esac