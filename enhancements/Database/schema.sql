-- ============================================================================
-- Database Schema for Animal Shelter Management System
-- Enhanced with normalization (3NF), RBAC, and security best practices
-- ============================================================================

-- Drop existing tables if they exist (for clean reinstallation)
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS role_permissions CASCADE;
DROP TABLE IF EXISTS animals CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;
DROP TABLE IF EXISTS permissions CASCADE;

-- ============================================================================
-- Table: roles
-- Purpose: Define system roles (admin, staff, viewer)
-- ============================================================================
CREATE TABLE roles (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================================
-- Table: permissions
-- Purpose: Define granular permissions for RBAC
-- ============================================================================
CREATE TABLE permissions (
    permission_id SERIAL PRIMARY KEY,
    permission_name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    resource VARCHAR(50) NOT NULL, -- e.g., 'animals', 'users'
    action VARCHAR(20) NOT NULL,   -- e.g., 'create', 'read', 'update', 'delete'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_resource_action UNIQUE (resource, action)
);

-- ============================================================================
-- Table: role_permissions
-- Purpose: Many-to-many relationship between roles and permissions
-- ============================================================================
CREATE TABLE role_permissions (
    role_id INTEGER REFERENCES roles(role_id) ON DELETE CASCADE,
    permission_id INTEGER REFERENCES permissions(permission_id) ON DELETE CASCADE,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id, permission_id)
);

-- ============================================================================
-- Table: users
-- Purpose: Store user account information with hashed passwords
-- Security: Passwords hashed using PBKDF2-SHA256
-- ============================================================================
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- PBKDF2-SHA256 hash
    salt VARCHAR(255) NOT NULL,          -- Unique salt per user
    email VARCHAR(100) UNIQUE,
    full_name VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    CONSTRAINT username_length CHECK (LENGTH(username) >= 3),
    CONSTRAINT email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- ============================================================================
-- Table: user_roles
-- Purpose: Many-to-many relationship between users and roles
-- ============================================================================
CREATE TABLE user_roles (
    user_id INTEGER REFERENCES users(user_id) ON DELETE CASCADE,
    role_id INTEGER REFERENCES roles(role_id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by INTEGER REFERENCES users(user_id),
    PRIMARY KEY (user_id, role_id)
);

-- ============================================================================
-- Table: animals
-- Purpose: Store animal shelter data (normalized from MongoDB structure)
-- ============================================================================
CREATE TABLE animals (
    animal_id SERIAL PRIMARY KEY,
    external_id VARCHAR(50) UNIQUE,
    animal_type VARCHAR(50) NOT NULL,
    name VARCHAR(100),
    breed VARCHAR(100),
    color VARCHAR(100),
    sex_upon_outcome VARCHAR(50),
    age_upon_outcome VARCHAR(50),
    age_upon_outcome_in_weeks DECIMAL(10, 2),
    date_of_birth DATE,
    outcome_type VARCHAR(50),
    outcome_subtype VARCHAR(50),
    outcome_datetime TIMESTAMP,
    location_lat DECIMAL(10, 8),
    location_long DECIMAL(11, 8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER REFERENCES users(user_id),
    updated_by INTEGER REFERENCES users(user_id),
    CONSTRAINT valid_animal_type CHECK (animal_type IN ('Dog', 'Cat', 'Bird', 'Other')),
    CONSTRAINT valid_coordinates CHECK (
        (location_lat IS NULL AND location_long IS NULL) OR
        (location_lat BETWEEN -90 AND 90 AND location_long BETWEEN -180 AND 180)
    )
);

-- ============================================================================
-- Indexes for Performance Optimization
-- ============================================================================
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active);
CREATE INDEX idx_animals_type ON animals(animal_type);
CREATE INDEX idx_animals_name ON animals(name);
CREATE INDEX idx_animals_external_id ON animals(external_id);
CREATE INDEX idx_animals_outcome_type ON animals(outcome_type);
CREATE INDEX idx_animals_created_at ON animals(created_at);

-- ============================================================================
-- Initial Data: Roles
-- ============================================================================
INSERT INTO roles (role_name, description) VALUES
    ('admin', 'Full system access with user management capabilities'),
    ('staff', 'Can create, read, and update animal records'),
    ('viewer', 'Read-only access to animal records');

-- ============================================================================
-- Initial Data: Permissions
-- ============================================================================
INSERT INTO permissions (permission_name, resource, action, description) VALUES
    -- Animal permissions
    ('animals.create', 'animals', 'create', 'Create new animal records'),
    ('animals.read', 'animals', 'read', 'View animal records'),
    ('animals.update', 'animals', 'update', 'Update existing animal records'),
    ('animals.delete', 'animals', 'delete', 'Delete animal records'),

    -- User permissions
    ('users.create', 'users', 'create', 'Create new user accounts'),
    ('users.read', 'users', 'read', 'View user information'),
    ('users.update', 'users', 'update', 'Update user accounts'),
    ('users.delete', 'users', 'delete', 'Delete user accounts'),

    -- Role permissions
    ('roles.assign', 'roles', 'assign', 'Assign roles to users'),
    ('roles.revoke', 'roles', 'revoke', 'Revoke roles from users');

-- ============================================================================
-- Initial Data: Role-Permission Mappings
-- ============================================================================
-- Admin: Full access
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
CROSS JOIN permissions p
WHERE r.role_name = 'admin';

-- Staff: Can manage animals but not users
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
CROSS JOIN permissions p
WHERE r.role_name = 'staff'
AND p.resource = 'animals'
AND p.action IN ('create', 'read', 'update');

-- Viewer: Read-only access to animals
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.role_id, p.permission_id
FROM roles r
CROSS JOIN permissions p
WHERE r.role_name = 'viewer'
AND p.resource = 'animals'
AND p.action = 'read';

-- ============================================================================
-- Trigger: Update timestamp on modification
-- ============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_animals_updated_at
    BEFORE UPDATE ON animals
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_roles_updated_at
    BEFORE UPDATE ON roles
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();
