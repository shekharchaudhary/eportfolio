#!/bin/bash

# ============================================================================
# Animal Shelter Database - Automated Setup Script
# ============================================================================

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
DB_NAME="AAC"
DB_USER="postgres"
DB_HOST="localhost"
DB_PORT="5432"

echo -e "${BLUE}======================================================================${NC}"
echo -e "${BLUE}Animal Shelter Management System - Automated Setup${NC}"
echo -e "${BLUE}======================================================================${NC}\n"

# ============================================================================
# Step 1: Check Prerequisites
# ============================================================================
echo -e "${YELLOW}[1/7] Checking prerequisites...${NC}"

# Check Python
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}✗ Python 3 is not installed${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Python 3 found: $(python3 --version)${NC}"

# Check PostgreSQL
if ! command -v psql &> /dev/null; then
    echo -e "${RED}✗ PostgreSQL is not installed${NC}"
    echo -e "${YELLOW}Install with: brew install postgresql${NC}"
    exit 1
fi
echo -e "${GREEN}✓ PostgreSQL found: $(psql --version)${NC}"

# Check if PostgreSQL is running
if ! pg_isready -h $DB_HOST -p $DB_PORT &> /dev/null; then
    echo -e "${YELLOW}⚠ PostgreSQL is not running. Starting...${NC}"

    # Try to start PostgreSQL (macOS)
    if command -v brew &> /dev/null; then
        brew services start postgresql@14 2>/dev/null || brew services start postgresql 2>/dev/null
        sleep 2
    else
        echo -e "${RED}✗ Cannot start PostgreSQL automatically${NC}"
        echo -e "${YELLOW}Start it manually with: pg_ctl start${NC}"
        exit 1
    fi
fi

if pg_isready -h $DB_HOST -p $DB_PORT &> /dev/null; then
    echo -e "${GREEN}✓ PostgreSQL is running${NC}"
else
    echo -e "${RED}✗ PostgreSQL is not responding${NC}"
    exit 1
fi

# ============================================================================
# Step 2: Install Python Dependencies
# ============================================================================
echo -e "\n${YELLOW}[2/7] Installing Python dependencies...${NC}"

if [ -f "requirements.txt" ]; then
    pip3 install -r requirements.txt --quiet
    echo -e "${GREEN}✓ Python dependencies installed${NC}"
else
    echo -e "${RED}✗ requirements.txt not found${NC}"
    exit 1
fi

# ============================================================================
# Step 3: Get Database Password
# ============================================================================
echo -e "\n${YELLOW}[3/7] Database configuration${NC}"

# Try to connect without password first (common in local dev)
if psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d postgres -c '\q' 2>/dev/null; then
    echo -e "${GREEN}✓ Connected to PostgreSQL (no password required)${NC}"
    DB_PASSWORD=""
else
    echo -e "${YELLOW}Enter PostgreSQL password for user '$DB_USER':${NC}"
    read -s DB_PASSWORD

    # Test connection with password
    if ! PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d postgres -c '\q' 2>/dev/null; then
        echo -e "${RED}✗ Failed to connect to PostgreSQL${NC}"
        exit 1
    fi
    echo -e "${GREEN}✓ Database credentials verified${NC}"
fi

# ============================================================================
# Step 4: Create Database
# ============================================================================
echo -e "\n${YELLOW}[4/7] Creating database...${NC}"

# Check if database exists
if PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -lqt | cut -d \| -f 1 | grep -qw $DB_NAME; then
    echo -e "${YELLOW}⚠ Database '$DB_NAME' already exists${NC}"
    echo -e "${YELLOW}Do you want to drop and recreate it? (y/N):${NC}"
    read -r response
    if [[ "$response" =~ ^[Yy]$ ]]; then
        PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d postgres -c "DROP DATABASE $DB_NAME;" 2>/dev/null
        echo -e "${GREEN}✓ Dropped existing database${NC}"
    else
        echo -e "${YELLOW}⚠ Using existing database${NC}"
    fi
fi

# Create database if it doesn't exist
if ! PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -lqt | cut -d \| -f 1 | grep -qw $DB_NAME; then
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d postgres -c "CREATE DATABASE $DB_NAME;"
    echo -e "${GREEN}✓ Database '$DB_NAME' created${NC}"
fi

# ============================================================================
# Step 5: Initialize Schema
# ============================================================================
echo -e "\n${YELLOW}[5/7] Initializing database schema...${NC}"

if [ -f "schema.sql" ]; then
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f schema.sql -q
    echo -e "${GREEN}✓ Database schema created${NC}"
else
    echo -e "${RED}✗ schema.sql not found${NC}"
    exit 1
fi

# ============================================================================
# Step 6: Create Stored Procedures
# ============================================================================
echo -e "\n${YELLOW}[6/7] Creating stored procedures...${NC}"

if [ -f "stored_procedures.sql" ]; then
    PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -f stored_procedures.sql -q
    echo -e "${GREEN}✓ Stored procedures created${NC}"
else
    echo -e "${RED}✗ stored_procedures.sql not found${NC}"
    exit 1
fi

# ============================================================================
# Step 7: Create Initial Users
# ============================================================================
echo -e "\n${YELLOW}[7/7] Creating initial users...${NC}"

# Create a Python script to set up users
cat > /tmp/setup_users.py <<EOF
from animal_shelter import AnimalShelter
import sys

try:
    with AnimalShelter(
        host='$DB_HOST',
        port=$DB_PORT,
        database='$DB_NAME',
        user='$DB_USER',
        password='$DB_PASSWORD'
    ) as shelter:

        # Create admin user
        try:
            admin = shelter.auth_manager.register_user(
                username='admin',
                password='admin123',
                email='admin@animalshelter.com',
                full_name='System Administrator',
                role='admin'
            )
            print(f'✓ Admin user created: {admin.username}')
        except Exception as e:
            if 'already exists' in str(e):
                print('⚠ Admin user already exists')
            else:
                raise

        # Login as admin to create other users
        shelter.login('admin', 'admin123')

        # Create staff user
        try:
            staff = shelter.auth_manager.register_user(
                username='staff_user',
                password='staff123',
                email='staff@animalshelter.com',
                full_name='Staff Member',
                role='staff',
                assigned_by=shelter.user_id
            )
            print(f'✓ Staff user created: {staff.username}')
        except Exception as e:
            if 'already exists' in str(e):
                print('⚠ Staff user already exists')
            else:
                raise

        # Create viewer user
        try:
            viewer = shelter.auth_manager.register_user(
                username='viewer_user',
                password='viewer123',
                email='viewer@animalshelter.com',
                full_name='Viewer User',
                role='viewer',
                assigned_by=shelter.user_id
            )
            print(f'✓ Viewer user created: {viewer.username}')
        except Exception as e:
            if 'already exists' in str(e):
                print('⚠ Viewer user already exists')
            else:
                raise

except Exception as e:
    print(f'✗ Error creating users: {e}', file=sys.stderr)
    sys.exit(1)
EOF

python3 /tmp/setup_users.py
rm /tmp/setup_users.py

# ============================================================================
# Success Message
# ============================================================================
echo -e "\n${GREEN}======================================================================${NC}"
echo -e "${GREEN}Setup Complete!${NC}"
echo -e "${GREEN}======================================================================${NC}\n"

echo -e "${BLUE}Database Information:${NC}"
echo -e "  Host: $DB_HOST"
echo -e "  Port: $DB_PORT"
echo -e "  Database: $DB_NAME"
echo -e "  User: $DB_USER\n"

echo -e "${BLUE}Created Users:${NC}"
echo -e "  ${GREEN}Admin:${NC}   username='admin',       password='admin123'   (full access)"
echo -e "  ${GREEN}Staff:${NC}   username='staff_user',  password='staff123'   (create/read/update)"
echo -e "  ${GREEN}Viewer:${NC}  username='viewer_user', password='viewer123'  (read-only)\n"

echo -e "${YELLOW}⚠ SECURITY WARNING: Change default passwords before production use!${NC}\n"

echo -e "${BLUE}Next Steps:${NC}"
echo -e "  1. Run the demo:    ${GREEN}python3 Main.py${NC}"
echo -e "  2. View docs:       ${GREEN}cat README.md${NC}"
echo -e "  3. Interactive:     ${GREEN}python3${NC} then import animal_shelter\n"

echo -e "${BLUE}Quick Test:${NC}"
echo -e "  ${GREEN}python3 Main.py${NC}\n"
