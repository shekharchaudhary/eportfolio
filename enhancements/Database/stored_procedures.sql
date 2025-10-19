-- ============================================================================
-- Stored Procedures for Animal Shelter Management System
-- Encapsulates complex operations and enforces business logic
-- ============================================================================

-- ============================================================================
-- Procedure: create_user_with_role
-- Purpose: Securely create a new user with hashed password and assign role
-- Parameters:
--   p_username: Username for the new account
--   p_password_hash: Pre-hashed password (hashed by application)
--   p_salt: Salt used for password hashing
--   p_email: User's email address
--   p_full_name: User's full name
--   p_role_name: Role to assign (admin, staff, viewer)
--   p_assigned_by: User ID of the person creating this account
-- Returns: The new user_id
-- ============================================================================
CREATE OR REPLACE FUNCTION create_user_with_role(
    p_username VARCHAR(50),
    p_password_hash VARCHAR(255),
    p_salt VARCHAR(255),
    p_email VARCHAR(100),
    p_full_name VARCHAR(100),
    p_role_name VARCHAR(50),
    p_assigned_by INTEGER DEFAULT NULL
)
RETURNS INTEGER AS $$
DECLARE
    v_user_id INTEGER;
    v_role_id INTEGER;
BEGIN
    -- Validate role name
    IF p_role_name NOT IN ('admin', 'staff', 'viewer') THEN
        RAISE EXCEPTION 'Invalid role name: %. Must be admin, staff, or viewer', p_role_name;
    END IF;

    -- Validate username length
    IF LENGTH(p_username) < 3 THEN
        RAISE EXCEPTION 'Username must be at least 3 characters long';
    END IF;

    -- Get role_id
    SELECT role_id INTO v_role_id
    FROM roles
    WHERE role_name = p_role_name;

    IF v_role_id IS NULL THEN
        RAISE EXCEPTION 'Role % does not exist', p_role_name;
    END IF;

    -- Insert user
    INSERT INTO users (username, password_hash, salt, email, full_name)
    VALUES (p_username, p_password_hash, p_salt, p_email, p_full_name)
    RETURNING user_id INTO v_user_id;

    -- Assign role
    INSERT INTO user_roles (user_id, role_id, assigned_by)
    VALUES (v_user_id, v_role_id, p_assigned_by);

    RETURN v_user_id;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- Procedure: update_user_password
-- Purpose: Update a user's password with new hash and salt
-- Parameters:
--   p_user_id: The user ID
--   p_new_password_hash: New password hash
--   p_new_salt: New salt
-- Returns: Boolean indicating success
-- ============================================================================
CREATE OR REPLACE FUNCTION update_user_password(
    p_user_id INTEGER,
    p_new_password_hash VARCHAR(255),
    p_new_salt VARCHAR(255)
)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE users
    SET password_hash = p_new_password_hash,
        salt = p_new_salt,
        updated_at = CURRENT_TIMESTAMP
    WHERE user_id = p_user_id;

    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- Procedure: create_animal_record
-- Purpose: Create a new animal record with validation
-- Returns: The new animal_id
-- ============================================================================
CREATE OR REPLACE FUNCTION create_animal_record(
    p_external_id VARCHAR(50),
    p_animal_type VARCHAR(50),
    p_name VARCHAR(100),
    p_breed VARCHAR(100),
    p_color VARCHAR(100),
    p_sex_upon_outcome VARCHAR(50),
    p_age_upon_outcome VARCHAR(50),
    p_age_upon_outcome_in_weeks DECIMAL(10, 2),
    p_date_of_birth DATE,
    p_outcome_type VARCHAR(50),
    p_outcome_subtype VARCHAR(50),
    p_outcome_datetime TIMESTAMP,
    p_location_lat DECIMAL(10, 8),
    p_location_long DECIMAL(11, 8),
    p_created_by INTEGER
)
RETURNS INTEGER AS $$
DECLARE
    v_animal_id INTEGER;
BEGIN
    -- Validate animal type
    IF p_animal_type NOT IN ('Dog', 'Cat', 'Bird', 'Other') THEN
        RAISE EXCEPTION 'Invalid animal type: %. Must be Dog, Cat, Bird, or Other', p_animal_type;
    END IF;

    -- Validate coordinates if provided
    IF (p_location_lat IS NOT NULL AND p_location_long IS NULL) OR
       (p_location_lat IS NULL AND p_location_long IS NOT NULL) THEN
        RAISE EXCEPTION 'Both latitude and longitude must be provided together';
    END IF;

    -- Insert animal record
    INSERT INTO animals (
        external_id, animal_type, name, breed, color, sex_upon_outcome,
        age_upon_outcome, age_upon_outcome_in_weeks, date_of_birth,
        outcome_type, outcome_subtype, outcome_datetime,
        location_lat, location_long, created_by, updated_by
    )
    VALUES (
        p_external_id, p_animal_type, p_name, p_breed, p_color, p_sex_upon_outcome,
        p_age_upon_outcome, p_age_upon_outcome_in_weeks, p_date_of_birth,
        p_outcome_type, p_outcome_subtype, p_outcome_datetime,
        p_location_lat, p_location_long, p_created_by, p_created_by
    )
    RETURNING animal_id INTO v_animal_id;

    RETURN v_animal_id;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- Procedure: update_animal_record
-- Purpose: Update an existing animal record
-- Returns: Boolean indicating success
-- ============================================================================
CREATE OR REPLACE FUNCTION update_animal_record(
    p_animal_id INTEGER,
    p_name VARCHAR(100),
    p_breed VARCHAR(100),
    p_color VARCHAR(100),
    p_outcome_type VARCHAR(50),
    p_outcome_subtype VARCHAR(50),
    p_outcome_datetime TIMESTAMP,
    p_location_lat DECIMAL(10, 8),
    p_location_long DECIMAL(11, 8),
    p_updated_by INTEGER
)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE animals
    SET name = COALESCE(p_name, name),
        breed = COALESCE(p_breed, breed),
        color = COALESCE(p_color, color),
        outcome_type = COALESCE(p_outcome_type, outcome_type),
        outcome_subtype = COALESCE(p_outcome_subtype, outcome_subtype),
        outcome_datetime = COALESCE(p_outcome_datetime, outcome_datetime),
        location_lat = COALESCE(p_location_lat, location_lat),
        location_long = COALESCE(p_location_long, location_long),
        updated_by = p_updated_by,
        updated_at = CURRENT_TIMESTAMP
    WHERE animal_id = p_animal_id;

    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- Procedure: deactivate_user
-- Purpose: Deactivate a user account (soft delete)
-- Parameters:
--   p_user_id: The user ID to deactivate
-- Returns: Boolean indicating success
-- ============================================================================
CREATE OR REPLACE FUNCTION deactivate_user(p_user_id INTEGER)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE users
    SET is_active = FALSE,
        updated_at = CURRENT_TIMESTAMP
    WHERE user_id = p_user_id;

    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- Procedure: activate_user
-- Purpose: Reactivate a deactivated user account
-- Parameters:
--   p_user_id: The user ID to activate
-- Returns: Boolean indicating success
-- ============================================================================
CREATE OR REPLACE FUNCTION activate_user(p_user_id INTEGER)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE users
    SET is_active = TRUE,
        updated_at = CURRENT_TIMESTAMP
    WHERE user_id = p_user_id;

    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- Procedure: update_last_login
-- Purpose: Update user's last login timestamp
-- Parameters:
--   p_user_id: The user ID
-- Returns: Boolean indicating success
-- ============================================================================
CREATE OR REPLACE FUNCTION update_last_login(p_user_id INTEGER)
RETURNS BOOLEAN AS $$
BEGIN
    UPDATE users
    SET last_login = CURRENT_TIMESTAMP
    WHERE user_id = p_user_id;

    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- Procedure: search_animals
-- Purpose: Search animals by various criteria with pagination
-- Parameters:
--   p_animal_type: Animal type filter (optional)
--   p_name_pattern: Name pattern to search (optional, supports wildcards)
--   p_outcome_type: Outcome type filter (optional)
--   p_limit: Maximum number of results to return
--   p_offset: Number of records to skip (for pagination)
-- Returns: Table of matching animal records
-- ============================================================================
CREATE OR REPLACE FUNCTION search_animals(
    p_animal_type VARCHAR(50) DEFAULT NULL,
    p_name_pattern VARCHAR(100) DEFAULT NULL,
    p_outcome_type VARCHAR(50) DEFAULT NULL,
    p_limit INTEGER DEFAULT 100,
    p_offset INTEGER DEFAULT 0
)
RETURNS TABLE (
    animal_id INTEGER,
    external_id VARCHAR(50),
    animal_type VARCHAR(50),
    name VARCHAR(100),
    breed VARCHAR(100),
    color VARCHAR(100),
    age_upon_outcome VARCHAR(50),
    outcome_type VARCHAR(50),
    created_at TIMESTAMP
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        a.animal_id,
        a.external_id,
        a.animal_type,
        a.name,
        a.breed,
        a.color,
        a.age_upon_outcome,
        a.outcome_type,
        a.created_at
    FROM animals a
    WHERE (p_animal_type IS NULL OR a.animal_type = p_animal_type)
      AND (p_name_pattern IS NULL OR a.name ILIKE p_name_pattern)
      AND (p_outcome_type IS NULL OR a.outcome_type = p_outcome_type)
    ORDER BY a.created_at DESC
    LIMIT p_limit
    OFFSET p_offset;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- Procedure: get_user_by_username
-- Purpose: Retrieve user information by username for authentication
-- Parameters:
--   p_username: The username to search for
-- Returns: Table with user authentication details
-- ============================================================================
CREATE OR REPLACE FUNCTION get_user_by_username(p_username VARCHAR(50))
RETURNS TABLE (
    user_id INTEGER,
    username VARCHAR(50),
    password_hash VARCHAR(255),
    salt VARCHAR(255),
    is_active BOOLEAN,
    email VARCHAR(100),
    full_name VARCHAR(100)
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        u.user_id,
        u.username,
        u.password_hash,
        u.salt,
        u.is_active,
        u.email,
        u.full_name
    FROM users u
    WHERE u.username = p_username;
END;
$$ LANGUAGE plpgsql;

-- ============================================================================
-- Procedure: get_animal_statistics
-- Purpose: Get statistics about animals in the database
-- Returns: Table with animal type counts and percentages
-- ============================================================================
CREATE OR REPLACE FUNCTION get_animal_statistics()
RETURNS TABLE (
    animal_type VARCHAR(50),
    count BIGINT,
    percentage NUMERIC(5, 2)
) AS $$
DECLARE
    total_count BIGINT;
BEGIN
    -- Get total count
    SELECT COUNT(*) INTO total_count FROM animals;

    -- Return statistics
    RETURN QUERY
    SELECT
        a.animal_type,
        COUNT(*)::BIGINT as count,
        ROUND((COUNT(*)::NUMERIC / NULLIF(total_count, 0) * 100), 2) as percentage
    FROM animals a
    GROUP BY a.animal_type
    ORDER BY count DESC;
END;
$$ LANGUAGE plpgsql;
