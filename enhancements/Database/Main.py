"""
Enhanced Animal Shelter Management System - Main Demo
Demonstrates secure CRUD operations with RBAC and PostgreSQL
"""

from animal_shelter import AnimalShelter
from authentication import AuthenticationManager

# Database configuration
DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'AAC',
    'user': 'postgres',
    'password': 'your_password_here'  # Update with actual password
}


def demo_admin_operations():
    """Demonstrate admin user operations."""
    print("\n" + "=" * 70)
    print("DEMO: Admin Operations")
    print("=" * 70)

    with AnimalShelter(**DB_CONFIG) as shelter:
        # Login as admin
        try:
            admin = shelter.login("admin", "admin_password")
            print(f"\n✓ Logged in as: {admin.username}")
        except Exception as e:
            print(f"\n✗ Login failed: {e}")
            print("Note: Create admin user first using register_initial_admin()")
            return

        # Create animal record
        print("\n--- Creating Animal Record ---")
        try:
            animal_id = shelter.create({
                'animal_id': "A123456",
                'animal_type': "Dog",
                'name': "Buddy",
                'breed': "Labrador Retriever Mix",
                'color': "Brown and White",
                'sex_upon_outcome': "Neutered Male",
                'age_upon_outcome': "2 months",
                'age_upon_outcome_in_weeks': 8.71,
                'date_of_birth': "2023-01-15",
                'outcome_type': 'Transfer',
                'outcome_subtype': "Foster",
                'datetime': "2023-03-15 14:30:00",
                'location_lat': 34.052235,
                'location_long': -118.243683
            })
            print(f"✓ Created animal record with ID: {animal_id}")
        except Exception as e:
            print(f"✗ Failed to create record: {e}")

        # Read animal records
        print("\n--- Reading Animal Records ---")
        try:
            animals = shelter.read({'name': 'Buddy'})
            print(f"✓ Found {len(animals)} animal(s)")
            for animal in animals:
                print(f"  - {animal.get('name')} ({animal.get('animal_type')})")
        except Exception as e:
            print(f"✗ Failed to read records: {e}")

        # Get statistics
        print("\n--- Animal Statistics ---")
        try:
            stats = shelter.get_statistics()
            print("✓ Database Statistics:")
            for stat in stats:
                print(f"  - {stat['animal_type']}: {stat['count']} ({stat['percentage']}%)")
        except Exception as e:
            print(f"✗ Failed to get statistics: {e}")

        shelter.logout()
        print("\n✓ Logged out")


def demo_staff_operations():
    """Demonstrate staff user operations."""
    print("\n" + "=" * 70)
    print("DEMO: Staff Operations")
    print("=" * 70)

    with AnimalShelter(**DB_CONFIG) as shelter:
        # Login as staff
        try:
            staff = shelter.login("staff_user", "staff_password")
            print(f"\n✓ Logged in as: {staff.username}")
        except Exception as e:
            print(f"\n✗ Login failed: {e}")
            return

        # Staff can read and update
        print("\n--- Reading Animal Records ---")
        try:
            animals = shelter.read(limit=5)
            print(f"✓ Found {len(animals)} animal(s)")
        except Exception as e:
            print(f"✗ Failed to read: {e}")

        # Try to delete (should fail - staff doesn't have delete permission)
        print("\n--- Attempting Delete (Should Fail) ---")
        try:
            shelter.delete(1)
            print("✗ Unexpected: Delete succeeded")
        except PermissionError as e:
            print(f"✓ Expected permission denial: {e}")
        except Exception as e:
            print(f"✗ Unexpected error: {e}")

        shelter.logout()
        print("\n✓ Logged out")


def demo_viewer_operations():
    """Demonstrate viewer user operations."""
    print("\n" + "=" * 70)
    print("DEMO: Viewer Operations (Read-Only)")
    print("=" * 70)

    with AnimalShelter(**DB_CONFIG) as shelter:
        # Login as viewer
        try:
            viewer = shelter.login("viewer_user", "viewer_password")
            print(f"\n✓ Logged in as: {viewer.username}")
        except Exception as e:
            print(f"\n✗ Login failed: {e}")
            return

        # Viewer can only read
        print("\n--- Reading Animal Records ---")
        try:
            animals = shelter.read(limit=5)
            print(f"✓ Found {len(animals)} animal(s)")
        except Exception as e:
            print(f"✗ Failed to read: {e}")

        # Try to create (should fail)
        print("\n--- Attempting Create (Should Fail) ---")
        try:
            shelter.create({'animal_type': 'Cat', 'name': 'Whiskers'})
            print("✗ Unexpected: Create succeeded")
        except PermissionError as e:
            print(f"✓ Expected permission denial: {e}")
        except Exception as e:
            print(f"✗ Unexpected error: {e}")

        shelter.logout()
        print("\n✓ Logged out")


def register_initial_admin():
    """Register the initial admin user."""
    print("\n" + "=" * 70)
    print("SETUP: Creating Initial Admin User")
    print("=" * 70)

    with AnimalShelter(**DB_CONFIG) as shelter:
        # Register admin without authentication (for initial setup)
        # In production, this should be restricted or done via migration
        try:
            admin = shelter.auth_manager.register_user(
                username="admin",
                password="admin_password",  # Change in production!
                email="admin@animalshelter.com",
                full_name="System Administrator",
                role="admin"
            )
            print(f"\n✓ Admin user created: {admin.username}")
            print("  WARNING: Change the default password in production!")
        except Exception as e:
            print(f"\n✗ Failed to create admin: {e}")


def main():
    """Main demonstration function."""
    print("\n" + "=" * 70)
    print("Animal Shelter Management System - Enhanced Edition")
    print("Features: RBAC, Security, PostgreSQL, Input Validation")
    print("=" * 70)

    # Uncomment to create initial admin user
    # register_initial_admin()

    # Run demonstrations
    demo_admin_operations()
    demo_staff_operations()
    demo_viewer_operations()

    print("\n" + "=" * 70)
    print("Demo Complete!")
    print("=" * 70)


if __name__ == "__main__":
    main()
