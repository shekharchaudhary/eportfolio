"""
Authentication Module for Animal Shelter Management System
Handles user authentication and session management
"""

import psycopg2
from typing import Optional, Dict, Any
from security import PasswordHasher, InputValidator, SecurityLogger


class User:
    """Represents an authenticated user."""

    def __init__(self, user_id: int, username: str, email: str = None,
                 full_name: str = None, is_active: bool = True):
        self.user_id = user_id
        self.username = username
        self.email = email
        self.full_name = full_name
        self.is_active = is_active

    def __repr__(self):
        return f"User(id={self.user_id}, username='{self.username}')"


class AuthenticationError(Exception):
    """Custom exception for authentication failures."""
    pass


class AuthenticationManager:
    """
    Manages user authentication operations.
    Implements secure authentication with hashed passwords.
    """

    def __init__(self, db_connection):
        """
        Initialize Authentication Manager.

        Args:
            db_connection: psycopg2 database connection
        """
        self.conn = db_connection
        self.hasher = PasswordHasher()
        self.validator = InputValidator()

    def register_user(self, username: str, password: str, email: str = None,
                     full_name: str = None, role: str = 'viewer',
                     assigned_by: int = None) -> User:
        """
        Register a new user with secure password hashing.

        Args:
            username: Desired username
            password: Plaintext password (will be hashed)
            email: User's email address
            full_name: User's full name
            role: Initial role to assign (default: viewer)
            assigned_by: User ID of the person creating this account

        Returns:
            User: The newly created user object

        Raises:
            ValueError: If validation fails
            AuthenticationError: If user creation fails
        """
        # Validate inputs
        if not self.validator.validate_username(username):
            raise ValueError("Invalid username format. Must be 3-50 alphanumeric characters.")

        if email and not self.validator.validate_email(email):
            raise ValueError("Invalid email format.")

        if not self.validator.validate_role(role):
            raise ValueError(f"Invalid role: {role}")

        # Hash password
        try:
            password_hash, salt = self.hasher.hash_password(password)
        except ValueError as e:
            raise ValueError(f"Password validation failed: {str(e)}")

        cursor = self.conn.cursor()

        try:
            # Use stored procedure to create user with role
            cursor.execute("""
                SELECT create_user_with_role(%s, %s, %s, %s, %s, %s, %s)
            """, (username, password_hash, salt, email, full_name, role, assigned_by))

            user_id = cursor.fetchone()[0]
            self.conn.commit()

            SecurityLogger.log_security_event(
                "USER_REGISTERED",
                f"New user '{username}' registered with role '{role}'"
            )

            return User(user_id, username, email, full_name, True)

        except psycopg2.IntegrityError as e:
            self.conn.rollback()
            if 'username' in str(e):
                raise AuthenticationError(f"Username '{username}' already exists")
            elif 'email' in str(e):
                raise AuthenticationError(f"Email '{email}' already registered")
            else:
                raise AuthenticationError("User registration failed")

        except Exception as e:
            self.conn.rollback()
            raise AuthenticationError(f"User registration failed: {str(e)}")

        finally:
            cursor.close()

    def authenticate(self, username: str, password: str,
                    ip_address: str = None) -> Optional[User]:
        """
        Authenticate a user with username and password.

        Args:
            username: The username
            password: The plaintext password
            ip_address: Optional IP address for logging

        Returns:
            User: The authenticated user object if successful, None otherwise

        Raises:
            AuthenticationError: If authentication fails
        """
        if not username or not password:
            raise AuthenticationError("Username and password are required")

        cursor = self.conn.cursor()

        try:
            # Get user from database using stored procedure
            cursor.execute("SELECT * FROM get_user_by_username(%s)", (username,))
            user_row = cursor.fetchone()

            if not user_row:
                SecurityLogger.log_authentication_attempt(username, False, ip_address)
                raise AuthenticationError("Invalid username or password")

            # Unpack user data
            user_id, db_username, password_hash, salt, is_active, email, full_name = user_row

            # Check if account is active
            if not is_active:
                SecurityLogger.log_authentication_attempt(username, False, ip_address)
                raise AuthenticationError("Account is deactivated")

            # Verify password
            if not self.hasher.verify_password(password, password_hash, salt):
                SecurityLogger.log_authentication_attempt(username, False, ip_address)
                raise AuthenticationError("Invalid username or password")

            # Update last login
            cursor.execute("SELECT update_last_login(%s)", (user_id,))
            self.conn.commit()

            # Log successful authentication
            SecurityLogger.log_authentication_attempt(username, True, ip_address)

            return User(user_id, db_username, email, full_name, is_active)

        except AuthenticationError:
            raise

        except Exception as e:
            self.conn.rollback()
            SecurityLogger.log_authentication_attempt(username, False, ip_address)
            raise AuthenticationError(f"Authentication failed: {str(e)}")

        finally:
            cursor.close()

    def change_password(self, user_id: int, old_password: str,
                       new_password: str) -> bool:
        """
        Change a user's password.

        Args:
            user_id: The user ID
            old_password: Current password
            new_password: New password

        Returns:
            bool: True if password changed successfully

        Raises:
            AuthenticationError: If old password is incorrect
            ValueError: If new password is invalid
        """
        cursor = self.conn.cursor()

        try:
            # Get current password hash and salt
            cursor.execute("""
                SELECT password_hash, salt, username
                FROM users
                WHERE user_id = %s
            """, (user_id,))

            user_row = cursor.fetchone()

            if not user_row:
                raise AuthenticationError("User not found")

            current_hash, current_salt, username = user_row

            # Verify old password
            if not self.hasher.verify_password(old_password, current_hash, current_salt):
                SecurityLogger.log_security_event(
                    "PASSWORD_CHANGE_FAILED",
                    f"Incorrect old password for user_id {user_id}"
                )
                raise AuthenticationError("Current password is incorrect")

            # Hash new password
            new_hash, new_salt = self.hasher.hash_password(new_password)

            # Update password using stored procedure
            cursor.execute("""
                SELECT update_user_password(%s, %s, %s)
            """, (user_id, new_hash, new_salt))

            self.conn.commit()

            SecurityLogger.log_security_event(
                "PASSWORD_CHANGED",
                f"Password changed for user '{username}' (user_id {user_id})"
            )

            return True

        except (AuthenticationError, ValueError):
            raise

        except Exception as e:
            self.conn.rollback()
            raise AuthenticationError(f"Password change failed: {str(e)}")

        finally:
            cursor.close()

    def deactivate_account(self, user_id: int) -> bool:
        """
        Deactivate a user account.

        Args:
            user_id: The user ID to deactivate

        Returns:
            bool: True if successful
        """
        cursor = self.conn.cursor()

        try:
            cursor.execute("SELECT deactivate_user(%s)", (user_id,))
            self.conn.commit()

            SecurityLogger.log_security_event(
                "ACCOUNT_DEACTIVATED",
                f"User account {user_id} has been deactivated"
            )

            return True

        except Exception as e:
            self.conn.rollback()
            raise AuthenticationError(f"Account deactivation failed: {str(e)}")

        finally:
            cursor.close()

    def activate_account(self, user_id: int) -> bool:
        """
        Activate a user account.

        Args:
            user_id: The user ID to activate

        Returns:
            bool: True if successful
        """
        cursor = self.conn.cursor()

        try:
            cursor.execute("SELECT activate_user(%s)", (user_id,))
            self.conn.commit()

            SecurityLogger.log_security_event(
                "ACCOUNT_ACTIVATED",
                f"User account {user_id} has been activated"
            )

            return True

        except Exception as e:
            self.conn.rollback()
            raise AuthenticationError(f"Account activation failed: {str(e)}")

        finally:
            cursor.close()

    def get_user_info(self, user_id: int) -> Optional[User]:
        """
        Get user information by user ID.

        Args:
            user_id: The user ID

        Returns:
            User: User object if found, None otherwise
        """
        cursor = self.conn.cursor()

        try:
            cursor.execute("""
                SELECT user_id, username, email, full_name, is_active
                FROM users
                WHERE user_id = %s
            """, (user_id,))

            user_row = cursor.fetchone()

            if not user_row:
                return None

            return User(user_row[0], user_row[1], user_row[2], user_row[3], user_row[4])

        finally:
            cursor.close()
