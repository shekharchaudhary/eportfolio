"""
Security Module for Animal Shelter Management System
Implements PBKDF2-SHA256 password hashing and security utilities
"""

import hashlib
import secrets
import os
from typing import Tuple


class PasswordHasher:
    """
    Secure password hashing using PBKDF2-SHA256 algorithm.
    Follows OWASP security best practices.
    """

    # OWASP recommended iteration count (2023)
    PBKDF2_ITERATIONS = 600000
    SALT_LENGTH = 32  # 32 bytes = 256 bits
    HASH_LENGTH = 64  # 64 bytes = 512 bits

    @staticmethod
    def generate_salt() -> str:
        """
        Generate a cryptographically secure random salt.

        Returns:
            str: Hexadecimal string representation of the salt
        """
        return secrets.token_hex(PasswordHasher.SALT_LENGTH)

    @staticmethod
    def hash_password(password: str, salt: str = None) -> Tuple[str, str]:
        """
        Hash a password using PBKDF2-SHA256 with a random salt.

        Args:
            password: The plaintext password to hash
            salt: Optional salt (if None, a new salt will be generated)

        Returns:
            Tuple[str, str]: (password_hash, salt) as hexadecimal strings

        Raises:
            ValueError: If password is empty or invalid
        """
        if not password or not isinstance(password, str):
            raise ValueError("Password must be a non-empty string")

        if len(password) < 8:
            raise ValueError("Password must be at least 8 characters long")

        # Generate new salt if not provided
        if salt is None:
            salt = PasswordHasher.generate_salt()

        # Convert salt from hex to bytes
        salt_bytes = bytes.fromhex(salt)

        # Hash the password using PBKDF2-SHA256
        password_hash = hashlib.pbkdf2_hmac(
            'sha256',
            password.encode('utf-8'),
            salt_bytes,
            PasswordHasher.PBKDF2_ITERATIONS,
            dklen=PasswordHasher.HASH_LENGTH
        )

        # Return hash and salt as hexadecimal strings
        return password_hash.hex(), salt

    @staticmethod
    def verify_password(password: str, stored_hash: str, salt: str) -> bool:
        """
        Verify a password against a stored hash.

        Args:
            password: The plaintext password to verify
            stored_hash: The stored password hash (hexadecimal string)
            salt: The salt used to create the hash (hexadecimal string)

        Returns:
            bool: True if password matches, False otherwise
        """
        if not password or not stored_hash or not salt:
            return False

        try:
            # Hash the provided password with the same salt
            computed_hash, _ = PasswordHasher.hash_password(password, salt)

            # Use constant-time comparison to prevent timing attacks
            return secrets.compare_digest(computed_hash, stored_hash)
        except Exception:
            return False


class InputValidator:
    """
    Input validation utilities to prevent injection attacks.
    """

    @staticmethod
    def validate_username(username: str) -> bool:
        """
        Validate username format.

        Args:
            username: The username to validate

        Returns:
            bool: True if valid, False otherwise
        """
        if not username or not isinstance(username, str):
            return False

        # Username must be 3-50 characters, alphanumeric with underscores
        if len(username) < 3 or len(username) > 50:
            return False

        return username.replace('_', '').isalnum()

    @staticmethod
    def validate_email(email: str) -> bool:
        """
        Validate email format (basic validation).

        Args:
            email: The email to validate

        Returns:
            bool: True if valid, False otherwise
        """
        if not email or not isinstance(email, str):
            return False

        # Basic email validation
        if '@' not in email or '.' not in email.split('@')[-1]:
            return False

        if len(email) > 100:
            return False

        return True

    @staticmethod
    def sanitize_string(input_str: str, max_length: int = 255) -> str:
        """
        Sanitize string input to prevent injection attacks.

        Args:
            input_str: The string to sanitize
            max_length: Maximum allowed length

        Returns:
            str: Sanitized string

        Raises:
            ValueError: If input is invalid
        """
        if not isinstance(input_str, str):
            raise ValueError("Input must be a string")

        # Trim whitespace
        sanitized = input_str.strip()

        # Enforce maximum length
        if len(sanitized) > max_length:
            raise ValueError(f"Input exceeds maximum length of {max_length}")

        return sanitized

    @staticmethod
    def validate_role(role_name: str) -> bool:
        """
        Validate that a role is one of the allowed roles.

        Args:
            role_name: The role name to validate

        Returns:
            bool: True if valid, False otherwise
        """
        allowed_roles = {'admin', 'staff', 'viewer'}
        return role_name in allowed_roles


class SecurityLogger:
    """
    Security event logging utility.
    """

    @staticmethod
    def log_authentication_attempt(username: str, success: bool, ip_address: str = None):
        """
        Log authentication attempts for security auditing.

        Args:
            username: The username attempting authentication
            success: Whether the attempt was successful
            ip_address: Optional IP address of the client
        """
        status = "SUCCESS" if success else "FAILED"
        ip_info = f" from {ip_address}" if ip_address else ""
        print(f"[SECURITY] Authentication {status} for user '{username}'{ip_info}")

    @staticmethod
    def log_authorization_failure(username: str, resource: str, action: str):
        """
        Log authorization failures for security auditing.

        Args:
            username: The username attempting the action
            resource: The resource being accessed
            action: The action being attempted
        """
        print(f"[SECURITY] Authorization DENIED: User '{username}' attempted '{action}' on '{resource}'")

    @staticmethod
    def log_security_event(event_type: str, details: str):
        """
        Log general security events.

        Args:
            event_type: Type of security event
            details: Event details
        """
        print(f"[SECURITY] {event_type}: {details}")
