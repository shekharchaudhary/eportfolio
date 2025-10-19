"""
Enhanced Animal Shelter Management System
Implements secure CRUD operations with RBAC and PostgreSQL
"""

import psycopg2
from typing import List, Dict, Any, Optional
from decimal import Decimal
from datetime import datetime, date
from authentication import AuthenticationManager, User, AuthenticationError
from rbac import RBACManager, PermissionDecorator
from security import InputValidator, SecurityLogger


class AnimalShelter:
    """
    Enhanced CRUD operations for Animal Shelter with security and RBAC.

    This class implements:
    - Secure database connections
    - Role-Based Access Control (RBAC)
    - Input validation and SQL injection prevention
    - Audit logging
    - Transaction management
    """

    def __init__(self, host: str, port: int, database: str, user: str, password: str):
        """
        Initialize the Animal Shelter management system.

        Args:
            host: Database host
            port: Database port
            database: Database name
            user: Database user
            password: Database password
        """
        # Establish database connection
        try:
            self.conn = psycopg2.connect(
                host=host,
                port=port,
                database=database,
                user=user,
                password=password
            )
            self.conn.autocommit = False  # Enable transaction management
        except psycopg2.Error as e:
            raise ConnectionError(f"Failed to connect to database: {str(e)}")

        # Initialize managers
        self.auth_manager = AuthenticationManager(self.conn)
        self.rbac_manager = RBACManager(self.conn)
        self.validator = InputValidator()

        # Current user context (set after authentication)
        self.user: Optional[User] = None
        self.user_id: Optional[int] = None
        self.username: Optional[str] = None

    def login(self, username: str, password: str, ip_address: str = None) -> User:
        """
        Authenticate and log in a user.

        Args:
            username: The username
            password: The password
            ip_address: Optional IP address for logging

        Returns:
            User: The authenticated user

        Raises:
            AuthenticationError: If authentication fails
        """
        self.user = self.auth_manager.authenticate(username, password, ip_address)
        self.user_id = self.user.user_id
        self.username = self.user.username
        return self.user

    def logout(self):
        """Log out the current user."""
        if self.user:
            SecurityLogger.log_security_event(
                "USER_LOGOUT",
                f"User '{self.username}' logged out"
            )
        self.user = None
        self.user_id = None
        self.username = None

    def _require_authentication(self):
        """Ensure user is authenticated."""
        if not self.user or not self.user_id:
            raise AuthenticationError("User must be logged in to perform this operation")

    def create(self, data: Dict[str, Any]) -> int:
        """
        Create a new animal record (requires 'animals.create' permission).

        Args:
            data: Dictionary containing animal information

        Returns:
            int: The new animal_id

        Raises:
            AuthenticationError: If user is not authenticated
            PermissionError: If user lacks required permission
            ValueError: If data is invalid
        """
        self._require_authentication()
        self.rbac_manager.require_permission(
            self.user_id, self.username, 'animals', 'create'
        )

        if not data or not isinstance(data, dict):
            raise ValueError("Data must be a non-empty dictionary")

        # Validate required fields
        required_fields = ['animal_type']
        for field in required_fields:
            if field not in data:
                raise ValueError(f"Missing required field: {field}")

        # Validate animal type
        if data['animal_type'] not in ('Dog', 'Cat', 'Bird', 'Other'):
            raise ValueError(f"Invalid animal_type: {data['animal_type']}")

        cursor = self.conn.cursor()

        try:
            # Use stored procedure for validation and insertion
            cursor.execute("""
                SELECT create_animal_record(
                    %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s
                )
            """, (
                data.get('animal_id'),  # external_id
                data['animal_type'],
                data.get('name'),
                data.get('breed'),
                data.get('color'),
                data.get('sex_upon_outcome'),
                data.get('age_upon_outcome'),
                data.get('age_upon_outcome_in_weeks'),
                data.get('date_of_birth'),
                data.get('outcome_type'),
                data.get('outcome_subtype'),
                data.get('datetime'),
                data.get('location_lat'),
                data.get('location_long'),
                self.user_id
            ))

            animal_id = cursor.fetchone()[0]
            self.conn.commit()

            SecurityLogger.log_security_event(
                "ANIMAL_CREATED",
                f"User '{self.username}' created animal record {animal_id}"
            )

            return animal_id

        except psycopg2.Error as e:
            self.conn.rollback()
            raise ValueError(f"Failed to create animal record: {str(e)}")

        finally:
            cursor.close()

    def read(self, criteria: Dict[str, Any] = None, limit: int = 100,
             offset: int = 0) -> List[Dict[str, Any]]:
        """
        Read animal records (requires 'animals.read' permission).

        Args:
            criteria: Dictionary of search criteria (optional)
            limit: Maximum number of records to return
            offset: Number of records to skip (for pagination)

        Returns:
            List[Dict]: List of animal records

        Raises:
            AuthenticationError: If user is not authenticated
            PermissionError: If user lacks required permission
        """
        self._require_authentication()
        self.rbac_manager.require_permission(
            self.user_id, self.username, 'animals', 'read'
        )

        cursor = self.conn.cursor()

        try:
            if criteria and isinstance(criteria, dict):
                # Use search function for filtered queries
                cursor.execute("""
                    SELECT * FROM search_animals(%s, %s, %s, %s, %s)
                """, (
                    criteria.get('animal_type'),
                    criteria.get('name'),
                    criteria.get('outcome_type'),
                    limit,
                    offset
                ))
            else:
                # Get all animals with pagination
                cursor.execute("""
                    SELECT animal_id, external_id, animal_type, name, breed,
                           color, age_upon_outcome, outcome_type, created_at
                    FROM animals
                    ORDER BY created_at DESC
                    LIMIT %s OFFSET %s
                """, (limit, offset))

            # Convert rows to dictionaries
            columns = [desc[0] for desc in cursor.description]
            results = []

            for row in cursor.fetchall():
                animal_dict = {}
                for i, value in enumerate(row):
                    # Convert special types to JSON-serializable formats
                    if isinstance(value, (datetime, date)):
                        animal_dict[columns[i]] = value.isoformat()
                    elif isinstance(value, Decimal):
                        animal_dict[columns[i]] = float(value)
                    else:
                        animal_dict[columns[i]] = value
                results.append(animal_dict)

            return results

        except psycopg2.Error as e:
            raise ValueError(f"Failed to read animal records: {str(e)}")

        finally:
            cursor.close()

    def update(self, animal_id: int, update_data: Dict[str, Any]) -> bool:
        """
        Update an animal record (requires 'animals.update' permission).

        Args:
            animal_id: The animal ID to update
            update_data: Dictionary of fields to update

        Returns:
            bool: True if successful

        Raises:
            AuthenticationError: If user is not authenticated
            PermissionError: If user lacks required permission
            ValueError: If data is invalid
        """
        self._require_authentication()
        self.rbac_manager.require_permission(
            self.user_id, self.username, 'animals', 'update'
        )

        if not update_data or not isinstance(update_data, dict):
            raise ValueError("Update data must be a non-empty dictionary")

        cursor = self.conn.cursor()

        try:
            # Use stored procedure for update
            cursor.execute("""
                SELECT update_animal_record(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)
            """, (
                animal_id,
                update_data.get('name'),
                update_data.get('breed'),
                update_data.get('color'),
                update_data.get('outcome_type'),
                update_data.get('outcome_subtype'),
                update_data.get('outcome_datetime'),
                update_data.get('location_lat'),
                update_data.get('location_long'),
                self.user_id
            ))

            success = cursor.fetchone()[0]
            self.conn.commit()

            if success:
                SecurityLogger.log_security_event(
                    "ANIMAL_UPDATED",
                    f"User '{self.username}' updated animal record {animal_id}"
                )

            return success

        except psycopg2.Error as e:
            self.conn.rollback()
            raise ValueError(f"Failed to update animal record: {str(e)}")

        finally:
            cursor.close()

    def delete(self, animal_id: int) -> bool:
        """
        Delete an animal record (requires 'animals.delete' permission).

        Args:
            animal_id: The animal ID to delete

        Returns:
            bool: True if successful

        Raises:
            AuthenticationError: If user is not authenticated
            PermissionError: If user lacks required permission
        """
        self._require_authentication()
        self.rbac_manager.require_permission(
            self.user_id, self.username, 'animals', 'delete'
        )

        cursor = self.conn.cursor()

        try:
            cursor.execute("""
                DELETE FROM animals WHERE animal_id = %s
                RETURNING animal_id
            """, (animal_id,))

            deleted = cursor.fetchone()
            self.conn.commit()

            if deleted:
                SecurityLogger.log_security_event(
                    "ANIMAL_DELETED",
                    f"User '{self.username}' deleted animal record {animal_id}"
                )
                return True

            return False

        except psycopg2.Error as e:
            self.conn.rollback()
            raise ValueError(f"Failed to delete animal record: {str(e)}")

        finally:
            cursor.close()

    def get_statistics(self) -> List[Dict[str, Any]]:
        """
        Get animal statistics (requires 'animals.read' permission).

        Returns:
            List[Dict]: Statistics about animals in the database

        Raises:
            AuthenticationError: If user is not authenticated
            PermissionError: If user lacks required permission
        """
        self._require_authentication()
        self.rbac_manager.require_permission(
            self.user_id, self.username, 'animals', 'read'
        )

        cursor = self.conn.cursor()

        try:
            cursor.execute("SELECT * FROM get_animal_statistics()")

            columns = [desc[0] for desc in cursor.description]
            results = []

            for row in cursor.fetchall():
                stat_dict = {}
                for i, value in enumerate(row):
                    if isinstance(value, Decimal):
                        stat_dict[columns[i]] = float(value)
                    else:
                        stat_dict[columns[i]] = value
                results.append(stat_dict)

            return results

        finally:
            cursor.close()

    def close(self):
        """Close the database connection."""
        if self.conn:
            self.conn.close()
            SecurityLogger.log_security_event(
                "CONNECTION_CLOSED",
                "Database connection closed"
            )

    def __enter__(self):
        """Context manager entry."""
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        """Context manager exit."""
        self.close()
