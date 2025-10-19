# Enhanced Animal Shelter Management System

## Overview

This is an enhanced version of the Animal Shelter database system that implements enterprise-grade security, role-based access control (RBAC), and normalized database design following best practices.

## Key Enhancements

### 1. Database Normalization (3NF)
- Migrated from MongoDB to PostgreSQL
- Achieved Third Normal Form (3NF) with separate tables for:
  - `users` - User account information
  - `roles` - System roles (admin, staff, viewer)
  - `permissions` - Granular permission definitions
  - `animals` - Normalized animal records
  - Junction tables for many-to-many relationships

### 2. Security Enhancements

#### Password Security
- **PBKDF2-SHA256** hashing algorithm (OWASP recommended)
- 600,000 iterations (2023 OWASP standard)
- Unique salt per user
- Never stores passwords in plain text

#### Input Validation
- SQL injection prevention through parameterized queries
- Username and email format validation
- Data sanitization for all user inputs
- Type checking and constraint validation

### 3. Role-Based Access Control (RBAC)

#### Roles
- **Admin**: Full system access including user management
- **Staff**: Can create, read, and update animal records
- **Viewer**: Read-only access to animal records

#### Permission Model
- Granular permissions (e.g., `animals.create`, `animals.read`)
- Least privilege principle enforcement
- Dynamic permission checking at runtime

### 4. Stored Procedures

Encapsulated business logic in PostgreSQL functions:
- `create_user_with_role()` - Secure user creation with role assignment
- `create_animal_record()` - Validated animal record insertion
- `update_animal_record()` - Atomic record updates
- `search_animals()` - Optimized search with pagination
- `get_animal_statistics()` - Database analytics

### 5. Modular Architecture

```
├── schema.sql              # Database schema and initial data
├── stored_procedures.sql   # PostgreSQL stored procedures
├── security.py            # Password hashing and validation
├── authentication.py      # User authentication logic
├── rbac.py               # Role-based access control
├── animal_shelter.py     # Enhanced CRUD operations
└── Main.py              # Demo application
```

## Installation

### Prerequisites
- Python 3.8+
- PostgreSQL 12+
- Required Python packages:
  ```bash
  pip install psycopg2-binary
  ```

### Database Setup

1. Create PostgreSQL database:
   ```sql
   CREATE DATABASE AAC;
   ```

2. Run schema creation:
   ```bash
   psql -U postgres -d AAC -f schema.sql
   ```

3. Create stored procedures:
   ```bash
   psql -U postgres -d AAC -f stored_procedures.sql
   ```

### Application Configuration

Update `Main.py` with your database credentials:
```python
DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'AAC',
    'user': 'postgres',
    'password': 'your_password_here'
}
```

## Usage

### Creating the Initial Admin User

Uncomment the setup function in `Main.py`:
```python
# In main() function
register_initial_admin()
```

Run once to create admin account:
```bash
python Main.py
```

**⚠️ Security Warning**: Change the default admin password immediately!

### Running the Demo

```bash
python Main.py
```

This demonstrates:
- Admin operations (full CRUD access)
- Staff operations (limited permissions)
- Viewer operations (read-only access)
- Permission enforcement

### Programmatic Usage

```python
from animal_shelter import AnimalShelter

# Connect to database
with AnimalShelter(host='localhost', port=5432,
                   database='AAC', user='postgres',
                   password='password') as shelter:

    # Authenticate
    user = shelter.login('admin', 'admin_password')

    # Create animal record
    animal_id = shelter.create({
        'animal_type': 'Dog',
        'name': 'Buddy',
        'breed': 'Labrador',
        'color': 'Brown'
    })

    # Read records
    animals = shelter.read({'animal_type': 'Dog'})

    # Update record
    shelter.update(animal_id, {'name': 'Max'})

    # Get statistics
    stats = shelter.get_statistics()

    # Logout
    shelter.logout()
```

## Security Features

### Authentication Flow
1. User provides username and password
2. System retrieves stored hash and salt
3. PBKDF2-SHA256 hashes provided password with stored salt
4. Constant-time comparison prevents timing attacks
5. Failed attempts are logged for security auditing

### Authorization Flow
1. User must be authenticated
2. System retrieves user's roles
3. System aggregates permissions from all roles
4. Permission check before each operation
5. Denials are logged for security monitoring

### SQL Injection Prevention
- All queries use parameterized statements
- Input validation before database interaction
- Type checking and constraint enforcement
- Stored procedures encapsulate complex logic

## Database Schema

### Entity-Relationship Diagram

```
users (1) ──< (M) user_roles (M) >── (1) roles
                                         │
                                         │ (1)
                                         │
                                         v
                                     (M) role_permissions
                                         │
                                         │ (M)
                                         v
                                     (1) permissions

users (1) ──< (M) animals (creator/updater)
```

### Key Tables

#### users
- `user_id` (PK)
- `username` (UNIQUE)
- `password_hash` (PBKDF2-SHA256)
- `salt` (Unique per user)
- `email`, `full_name`
- `is_active` (Account status)
- Timestamps and audit fields

#### roles
- `role_id` (PK)
- `role_name` (admin, staff, viewer)
- `description`

#### permissions
- `permission_id` (PK)
- `permission_name` (e.g., animals.create)
- `resource`, `action`
- `description`

#### animals
- `animal_id` (PK)
- `external_id` (Original ID)
- Animal attributes (type, name, breed, etc.)
- Outcome information
- Location coordinates
- Audit fields (created_by, updated_by)

## Testing

### Security Testing
- Password hashing verification
- SQL injection attempts (parameterized queries prevent)
- Permission boundary testing
- Authentication failure handling

### Functional Testing
- CRUD operations for each role
- Permission enforcement validation
- Data validation and constraint checking
- Transaction rollback on errors

## Performance Considerations

### Indexes
- Username and email lookups (authentication)
- Animal type and name searches
- Outcome type filtering
- Created/updated timestamps

### Optimization
- Parameterized query caching
- Connection pooling support
- Pagination for large result sets
- Stored procedures reduce round trips

## Security Audit Logging

All security-relevant events are logged:
- Authentication attempts (success/failure)
- Authorization failures
- Password changes
- User creation/deactivation
- Role assignments
- Data modifications

## Future Enhancements

- [ ] Implement connection pooling
- [ ] Add multi-factor authentication (MFA)
- [ ] Enhance audit logging with persistent storage
- [ ] Add API rate limiting
- [ ] Implement session management with JWT tokens
- [ ] Add automated security testing suite
- [ ] Create web-based admin interface
- [ ] Add data export functionality
- [ ] Implement backup and recovery procedures

## Comparison: Before vs After

### Before (Original)
- MongoDB (NoSQL, unstructured)
- Plain text passwords (hardcoded)
- No authentication system
- No access control
- Direct database access
- No input validation
- No audit logging

### After (Enhanced)
- PostgreSQL (relational, normalized)
- PBKDF2-SHA256 hashed passwords
- Secure authentication module
- Role-Based Access Control (RBAC)
- Abstracted data access layer
- Comprehensive input validation
- Security event logging
- Stored procedures
- Transaction management
- Modular architecture

## License

This project was developed as part of CS-499 Computer Science Capstone at Southern New Hampshire University.

## Author

**Shekhar Chaudhary**
CS-499 Computer Science Capstone
Southern New Hampshire University

## References

- OWASP Password Storage Cheat Sheet
- PostgreSQL Documentation
- NIST Special Publication 800-63B (Digital Identity Guidelines)
- Python psycopg2 Documentation
