"""
Role-Based Access Control (RBAC) Module
Implements authorization and permission checking for the Animal Shelter System
"""

import psycopg2
from typing import List, Optional, Dict, Any
from security import InputValidator, SecurityLogger


class Permission:
    """Represents a single permission."""

    def __init__(self, permission_id: int, permission_name: str,
                 resource: str, action: str, description: str = None):
        self.permission_id = permission_id
        self.permission_name = permission_name
        self.resource = resource
        self.action = action
        self.description = description

    def __repr__(self):
        return f"Permission({self.permission_name}: {self.action} on {self.resource})"


class Role:
    """Represents a user role with associated permissions."""

    def __init__(self, role_id: int, role_name: str, description: str = None):
        self.role_id = role_id
        self.role_name = role_name
        self.description = description
        self.permissions: List[Permission] = []

    def add_permission(self, permission: Permission):
        """Add a permission to this role."""
        self.permissions.append(permission)

    def has_permission(self, resource: str, action: str) -> bool:
        """
        Check if this role has a specific permission.

        Args:
            resource: The resource to check (e.g., 'animals', 'users')
            action: The action to check (e.g., 'create', 'read', 'update', 'delete')

        Returns:
            bool: True if role has the permission
        """
        return any(
            p.resource == resource and p.action == action
            for p in self.permissions
        )

    def __repr__(self):
        return f"Role({self.role_name}, {len(self.permissions)} permissions)"


class RBACManager:
    """
    Manages Role-Based Access Control operations.
    Implements least privilege principle.
    """

    def __init__(self, db_connection):
        """
        Initialize RBAC Manager with database connection.

        Args:
            db_connection: psycopg2 database connection
        """
        self.conn = db_connection
        self.validator = InputValidator()

    def get_user_roles(self, user_id: int) -> List[Role]:
        """
        Get all roles assigned to a user.

        Args:
            user_id: The user ID

        Returns:
            List[Role]: List of roles with their permissions
        """
        cursor = self.conn.cursor()
        roles = []

        try:
            # Get roles for the user
            cursor.execute("""
                SELECT r.role_id, r.role_name, r.description
                FROM roles r
                INNER JOIN user_roles ur ON r.role_id = ur.role_id
                WHERE ur.user_id = %s
            """, (user_id,))

            for role_row in cursor.fetchall():
                role = Role(role_row[0], role_row[1], role_row[2])

                # Get permissions for this role
                cursor.execute("""
                    SELECT p.permission_id, p.permission_name, p.resource,
                           p.action, p.description
                    FROM permissions p
                    INNER JOIN role_permissions rp ON p.permission_id = rp.permission_id
                    WHERE rp.role_id = %s
                """, (role.role_id,))

                for perm_row in cursor.fetchall():
                    permission = Permission(
                        perm_row[0], perm_row[1], perm_row[2],
                        perm_row[3], perm_row[4]
                    )
                    role.add_permission(permission)

                roles.append(role)

            return roles

        finally:
            cursor.close()

    def has_permission(self, user_id: int, resource: str, action: str) -> bool:
        """
        Check if a user has permission to perform an action on a resource.

        Args:
            user_id: The user ID
            resource: The resource (e.g., 'animals', 'users')
            action: The action (e.g., 'create', 'read', 'update', 'delete')

        Returns:
            bool: True if user has permission
        """
        roles = self.get_user_roles(user_id)

        # Check if any of the user's roles grant the permission
        return any(role.has_permission(resource, action) for role in roles)

    def require_permission(self, user_id: int, username: str,
                          resource: str, action: str) -> bool:
        """
        Require permission or raise exception.

        Args:
            user_id: The user ID
            username: The username (for logging)
            resource: The resource
            action: The action

        Returns:
            bool: True if permission granted

        Raises:
            PermissionError: If user lacks required permission
        """
        if not self.has_permission(user_id, resource, action):
            SecurityLogger.log_authorization_failure(username, resource, action)
            raise PermissionError(
                f"User '{username}' does not have permission to {action} {resource}"
            )
        return True

    def assign_role(self, user_id: int, role_name: str, assigned_by: int) -> bool:
        """
        Assign a role to a user.

        Args:
            user_id: The user to assign the role to
            role_name: The role name to assign
            assigned_by: The user ID performing the assignment

        Returns:
            bool: True if successful

        Raises:
            ValueError: If role name is invalid
            psycopg2.Error: If database operation fails
        """
        # Validate role name
        if not self.validator.validate_role(role_name):
            raise ValueError(f"Invalid role name: {role_name}")

        cursor = self.conn.cursor()

        try:
            # Get role_id
            cursor.execute("SELECT role_id FROM roles WHERE role_name = %s", (role_name,))
            role_row = cursor.fetchone()

            if not role_row:
                raise ValueError(f"Role '{role_name}' does not exist")

            role_id = role_row[0]

            # Assign role to user
            cursor.execute("""
                INSERT INTO user_roles (user_id, role_id, assigned_by)
                VALUES (%s, %s, %s)
                ON CONFLICT (user_id, role_id) DO NOTHING
            """, (user_id, role_id, assigned_by))

            self.conn.commit()

            SecurityLogger.log_security_event(
                "ROLE_ASSIGNED",
                f"Role '{role_name}' assigned to user_id {user_id} by user_id {assigned_by}"
            )

            return True

        except Exception as e:
            self.conn.rollback()
            raise

        finally:
            cursor.close()

    def revoke_role(self, user_id: int, role_name: str) -> bool:
        """
        Revoke a role from a user.

        Args:
            user_id: The user to revoke the role from
            role_name: The role name to revoke

        Returns:
            bool: True if successful
        """
        cursor = self.conn.cursor()

        try:
            cursor.execute("""
                DELETE FROM user_roles
                WHERE user_id = %s
                AND role_id = (SELECT role_id FROM roles WHERE role_name = %s)
            """, (user_id, role_name))

            self.conn.commit()

            SecurityLogger.log_security_event(
                "ROLE_REVOKED",
                f"Role '{role_name}' revoked from user_id {user_id}"
            )

            return True

        except Exception as e:
            self.conn.rollback()
            raise

        finally:
            cursor.close()

    def get_user_permissions(self, user_id: int) -> List[Permission]:
        """
        Get all permissions for a user (aggregated from all roles).

        Args:
            user_id: The user ID

        Returns:
            List[Permission]: List of all permissions the user has
        """
        roles = self.get_user_roles(user_id)
        permissions = []
        seen_permissions = set()

        for role in roles:
            for permission in role.permissions:
                # Avoid duplicates
                if permission.permission_name not in seen_permissions:
                    permissions.append(permission)
                    seen_permissions.add(permission.permission_name)

        return permissions

    def list_all_roles(self) -> List[Dict[str, Any]]:
        """
        List all available roles in the system.

        Returns:
            List[Dict]: List of role information
        """
        cursor = self.conn.cursor()

        try:
            cursor.execute("""
                SELECT role_id, role_name, description
                FROM roles
                ORDER BY role_name
            """)

            roles = []
            for row in cursor.fetchall():
                roles.append({
                    'role_id': row[0],
                    'role_name': row[1],
                    'description': row[2]
                })

            return roles

        finally:
            cursor.close()


class PermissionDecorator:
    """
    Decorator for enforcing permissions on methods.
    """

    def __init__(self, resource: str, action: str):
        """
        Initialize permission decorator.

        Args:
            resource: The resource being protected
            action: The required action
        """
        self.resource = resource
        self.action = action

    def __call__(self, func):
        """
        Decorator implementation.

        Args:
            func: The function to decorate

        Returns:
            Wrapped function with permission checking
        """
        def wrapper(self, *args, **kwargs):
            # Assumes the object has rbac_manager, user_id, and username attributes
            if hasattr(self, 'rbac_manager') and hasattr(self, 'user_id'):
                username = getattr(self, 'username', 'unknown')
                self.rbac_manager.require_permission(
                    self.user_id, username, self.resource, self.action
                )
            return func(self, *args, **kwargs)

        return wrapper
