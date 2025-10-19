# Quick Start Guide

## One-Command Setup & Demo

```bash
./setup.sh && python3 demo.py
```

## Step-by-Step Instructions

### 1. Automated Setup (Recommended)

Run the setup script to automatically configure everything:

```bash
chmod +x setup.sh
./setup.sh
```

This will:
- ✓ Check prerequisites (Python, PostgreSQL)
- ✓ Install dependencies
- ✓ Create database
- ✓ Initialize schema
- ✓ Create stored procedures
- ✓ Create test users (admin, staff, viewer)

### 2. Run the Interactive Demo

```bash
python3 demo.py
```

**Demo Menu Options:**
1. **Admin Demo** - Full CRUD access, user management
2. **Staff Demo** - Create/Read/Update animals (no delete)
3. **Viewer Demo** - Read-only access
4. **Security Demo** - Password hashing, validation, injection prevention
5. **Run All** - Complete walkthrough
6. **Exit**

### 3. Or Run the Simple Demo

```bash
python3 Main.py
```

This runs all three role demos automatically.

## Test Users Created

| Username      | Password    | Role   | Permissions                  |
|---------------|-------------|--------|------------------------------|
| admin         | admin123    | Admin  | Full access                  |
| staff_user    | staff123    | Staff  | Create, Read, Update animals |
| viewer_user   | viewer123   | Viewer | Read-only                    |

**⚠️ Security Warning:** Change these passwords before production use!

## Manual Setup (If Automated Fails)

```bash
# 1. Install dependencies
pip3 install -r requirements.txt

# 2. Create database
createdb AAC

# 3. Initialize schema
psql -U postgres -d AAC -f schema.sql
psql -U postgres -d AAC -f stored_procedures.sql

# 4. Create admin user
python3 -c "
from animal_shelter import AnimalShelter
with AnimalShelter(host='localhost', port=5432, database='AAC',
                   user='postgres', password='your_password') as shelter:
    admin = shelter.auth_manager.register_user(
        username='admin', password='admin123',
        email='admin@shelter.com', role='admin'
    )
    print(f'Admin created: {admin.username}')
"

# 5. Run demo
python3 demo.py
```

## Troubleshooting

### PostgreSQL not running
```bash
# macOS
brew services start postgresql

# Linux
sudo systemctl start postgresql

# Check status
pg_isready
```

### Database connection error
```bash
# Verify database exists
psql -U postgres -l | grep AAC

# Test connection
psql -U postgres -d AAC -c "SELECT version();"
```

### Import errors
```bash
# Reinstall dependencies
pip3 install -r requirements.txt --force-reinstall
```

### Users already exist
```bash
# Drop and recreate database
dropdb AAC
./setup.sh
```

## What Gets Demonstrated

### Admin Demo Shows:
- ✓ Creating multiple animal records
- ✓ Reading/searching animals by type
- ✓ Updating animal information
- ✓ Deleting records (admin only)
- ✓ Viewing database statistics

### Staff Demo Shows:
- ✓ Creating new animal records
- ✓ Reading existing records
- ✓ Updating animal data
- ✗ Delete denied (no permission)

### Viewer Demo Shows:
- ✓ Reading animal records
- ✓ Viewing statistics
- ✗ Create denied (no permission)
- ✗ Update denied (no permission)
- ✗ Delete denied (no permission)

### Security Demo Shows:
- ✓ PBKDF2-SHA256 password hashing
- ✓ Failed login attempts
- ✓ Input validation
- ✓ SQL injection prevention
- ✓ Role-based access control

## Interactive Python Usage

```python
from animal_shelter import AnimalShelter

# Connect and login
shelter = AnimalShelter(
    host='localhost', port=5432,
    database='AAC', user='postgres',
    password='your_password'
)

# Authenticate
user = shelter.login('admin', 'admin123')

# Create animal
animal_id = shelter.create({
    'animal_type': 'Dog',
    'name': 'Rex',
    'breed': 'German Shepherd',
    'color': 'Black and Tan'
})

# Read animals
dogs = shelter.read({'animal_type': 'Dog'})

# Update animal
shelter.update(animal_id, {'name': 'Rex the Great'})

# Get statistics
stats = shelter.get_statistics()

# Logout and close
shelter.logout()
shelter.close()
```

## Next Steps

1. ✓ Run the demos to see features in action
2. ✓ Read [README.md](README.md) for detailed documentation
3. ✓ Explore the code in `animal_shelter.py`
4. ✓ Review security implementation in `security.py`
5. ✓ Check RBAC logic in `rbac.py`
6. ✓ Examine database schema in `schema.sql`

## Files Overview

```
Database/
├── setup.sh              # Automated setup script ⭐
├── demo.py               # Interactive demo ⭐
├── Main.py               # Simple demo
├── schema.sql            # Database schema (3NF)
├── stored_procedures.sql # PostgreSQL functions
├── security.py           # Password hashing & validation
├── authentication.py     # User authentication
├── rbac.py               # Role-based access control
├── animal_shelter.py     # Main CRUD operations
├── requirements.txt      # Python dependencies
├── README.md             # Full documentation
└── QUICKSTART.md         # This file
```

## Quick Commands Reference

```bash
# Setup everything
./setup.sh

# Run interactive demo
python3 demo.py

# Run simple demo
python3 Main.py

# View documentation
cat README.md | less

# Check database
psql -U postgres -d AAC -c "SELECT COUNT(*) FROM animals;"

# Reset database
dropdb AAC && ./setup.sh
```

---

**Happy Testing! 🎉**

For complete documentation, see [README.md](README.md)
