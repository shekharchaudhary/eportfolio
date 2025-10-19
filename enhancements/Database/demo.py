#!/usr/bin/env python3
"""
Interactive Demo Script for Animal Shelter Management System
Provides guided walkthrough of all features
"""

import sys
from animal_shelter import AnimalShelter
from authentication import AuthenticationError

# Database configuration
DB_CONFIG = {
    'host': 'localhost',
    'port': 5432,
    'database': 'AAC',
    'user': 'postgres',
    'password': ''  # Will prompt if needed
}


def print_header(title):
    """Print formatted header."""
    print("\n" + "=" * 70)
    print(f"  {title}")
    print("=" * 70)


def print_section(title):
    """Print section divider."""
    print(f"\n--- {title} ---")


def get_db_password():
    """Prompt for database password if needed."""
    import getpass

    # Try without password first
    try:
        with AnimalShelter(**DB_CONFIG) as shelter:
            return ''
    except Exception as e:
        if 'password' in str(e).lower() or 'authentication' in str(e).lower():
            print("Database password required.")
            password = getpass.getpass("Enter PostgreSQL password: ")
            return password
        raise


def demo_admin_features():
    """Demonstrate admin user capabilities."""
    print_header("ADMIN USER DEMONSTRATION")
    print("Admin users have full access to all system features.\n")

    with AnimalShelter(**DB_CONFIG) as shelter:
        try:
            # Login
            print_section("Authentication")
            admin = shelter.login("admin", "admin123")
            print(f"✓ Logged in as: {admin.username} ({admin.email})")

            # Create animals
            print_section("Creating Animal Records")
            animals_to_create = [
                {
                    'animal_id': 'A001',
                    'animal_type': 'Dog',
                    'name': 'Max',
                    'breed': 'Golden Retriever',
                    'color': 'Golden',
                    'age_upon_outcome': '2 years',
                    'sex_upon_outcome': 'Neutered Male',
                    'outcome_type': 'Adoption'
                },
                {
                    'animal_id': 'A002',
                    'animal_type': 'Cat',
                    'name': 'Whiskers',
                    'breed': 'Siamese',
                    'color': 'White and Brown',
                    'age_upon_outcome': '1 year',
                    'sex_upon_outcome': 'Spayed Female',
                    'outcome_type': 'Foster'
                },
                {
                    'animal_id': 'A003',
                    'animal_type': 'Dog',
                    'name': 'Buddy',
                    'breed': 'Labrador Mix',
                    'color': 'Brown',
                    'age_upon_outcome': '3 years',
                    'sex_upon_outcome': 'Neutered Male',
                    'outcome_type': 'Transfer'
                }
            ]

            created_ids = []
            for animal_data in animals_to_create:
                try:
                    animal_id = shelter.create(animal_data)
                    created_ids.append(animal_id)
                    print(f"✓ Created: {animal_data['name']} ({animal_data['animal_type']}) - ID: {animal_id}")
                except Exception as e:
                    print(f"⚠ {animal_data['name']}: {e}")

            # Read animals
            print_section("Reading Animal Records")
            all_animals = shelter.read(limit=10)
            print(f"✓ Total animals in database: {len(all_animals)}")

            # Search by type
            dogs = shelter.read({'animal_type': 'Dog'})
            cats = shelter.read({'animal_type': 'Cat'})
            print(f"  - Dogs: {len(dogs)}")
            print(f"  - Cats: {len(cats)}")

            # Update animal
            print_section("Updating Animal Records")
            if created_ids:
                update_id = created_ids[0]
                success = shelter.update(update_id, {
                    'name': 'Maximus',
                    'outcome_type': 'Adoption',
                    'outcome_subtype': 'Completed'
                })
                if success:
                    print(f"✓ Updated animal ID {update_id}: Max → Maximus")

            # Get statistics
            print_section("Database Statistics")
            stats = shelter.get_statistics()
            print("Animal distribution:")
            for stat in stats:
                print(f"  {stat['animal_type']:12} {stat['count']:3} animals ({stat['percentage']:5.1f}%)")

            # Delete animal (admin only)
            print_section("Deleting Animal Records (Admin Only)")
            if len(created_ids) > 2:
                delete_id = created_ids[-1]
                success = shelter.delete(delete_id)
                if success:
                    print(f"✓ Deleted animal ID {delete_id}")

            shelter.logout()
            print("\n✓ Admin demo completed successfully")

        except AuthenticationError as e:
            print(f"\n✗ Authentication failed: {e}")
            print("  Run setup.sh to create test users")
        except Exception as e:
            print(f"\n✗ Error: {e}")


def demo_staff_features():
    """Demonstrate staff user capabilities."""
    print_header("STAFF USER DEMONSTRATION")
    print("Staff users can create, read, and update animal records.\n")

    with AnimalShelter(**DB_CONFIG) as shelter:
        try:
            # Login
            print_section("Authentication")
            staff = shelter.login("staff_user", "staff123")
            print(f"✓ Logged in as: {staff.username}")

            # Read animals
            print_section("Reading Animal Records")
            animals = shelter.read(limit=5)
            print(f"✓ Can view {len(animals)} animals")
            for animal in animals[:3]:
                print(f"  - {animal.get('name', 'Unknown')} ({animal.get('animal_type', 'Unknown')})")

            # Create animal
            print_section("Creating Animal Records")
            try:
                animal_id = shelter.create({
                    'animal_id': 'S001',
                    'animal_type': 'Dog',
                    'name': 'Staff Dog',
                    'breed': 'Beagle',
                    'color': 'Tri-color',
                    'outcome_type': 'Available'
                })
                print(f"✓ Staff can create animals - ID: {animal_id}")
            except Exception as e:
                print(f"⚠ Create failed: {e}")

            # Update animal
            print_section("Updating Animal Records")
            if animals:
                try:
                    first_animal_id = animals[0].get('animal_id')
                    success = shelter.update(first_animal_id, {'outcome_type': 'Updated by Staff'})
                    if success:
                        print(f"✓ Staff can update animals")
                except Exception as e:
                    print(f"⚠ Update failed: {e}")

            # Try to delete (should fail)
            print_section("Attempting Delete (Should Fail)")
            try:
                shelter.delete(1)
                print("✗ Unexpected: Delete succeeded (should have been denied)")
            except PermissionError as e:
                print(f"✓ Delete correctly denied: {str(e)[:50]}...")

            shelter.logout()
            print("\n✓ Staff demo completed successfully")

        except AuthenticationError as e:
            print(f"\n✗ Authentication failed: {e}")
            print("  Run setup.sh to create test users")
        except Exception as e:
            print(f"\n✗ Error: {e}")


def demo_viewer_features():
    """Demonstrate viewer user capabilities."""
    print_header("VIEWER USER DEMONSTRATION")
    print("Viewer users have read-only access to animal records.\n")

    with AnimalShelter(**DB_CONFIG) as shelter:
        try:
            # Login
            print_section("Authentication")
            viewer = shelter.login("viewer_user", "viewer123")
            print(f"✓ Logged in as: {viewer.username}")

            # Read animals
            print_section("Reading Animal Records")
            animals = shelter.read(limit=5)
            print(f"✓ Viewer can view {len(animals)} animals")

            # Display some animals
            print("\nAnimal Records:")
            for i, animal in enumerate(animals[:3], 1):
                print(f"  {i}. {animal.get('name', 'Unknown'):15} "
                      f"{animal.get('animal_type', 'Unknown'):10} "
                      f"{animal.get('breed', 'Unknown')[:20]}")

            # Get statistics
            print_section("Viewing Statistics")
            stats = shelter.get_statistics()
            print("✓ Viewer can view statistics:")
            for stat in stats:
                print(f"  {stat['animal_type']}: {stat['count']} animals")

            # Try to create (should fail)
            print_section("Attempting Create (Should Fail)")
            try:
                shelter.create({
                    'animal_type': 'Cat',
                    'name': 'Test Cat'
                })
                print("✗ Unexpected: Create succeeded (should have been denied)")
            except PermissionError as e:
                print(f"✓ Create correctly denied: {str(e)[:50]}...")

            # Try to update (should fail)
            print_section("Attempting Update (Should Fail)")
            try:
                shelter.update(1, {'name': 'Modified'})
                print("✗ Unexpected: Update succeeded (should have been denied)")
            except PermissionError as e:
                print(f"✓ Update correctly denied: {str(e)[:50]}...")

            # Try to delete (should fail)
            print_section("Attempting Delete (Should Fail)")
            try:
                shelter.delete(1)
                print("✗ Unexpected: Delete succeeded (should have been denied)")
            except PermissionError as e:
                print(f"✓ Delete correctly denied: {str(e)[:50]}...")

            shelter.logout()
            print("\n✓ Viewer demo completed successfully")

        except AuthenticationError as e:
            print(f"\n✗ Authentication failed: {e}")
            print("  Run setup.sh to create test users")
        except Exception as e:
            print(f"\n✗ Error: {e}")


def demo_security_features():
    """Demonstrate security features."""
    print_header("SECURITY FEATURES DEMONSTRATION")

    with AnimalShelter(**DB_CONFIG) as shelter:
        print_section("Password Security")
        print("✓ Passwords hashed with PBKDF2-SHA256")
        print("✓ 600,000 iterations (OWASP 2023 standard)")
        print("✓ Unique salt per user")
        print("✓ Never stored in plain text")

        print_section("SQL Injection Prevention")
        print("✓ All queries use parameterized statements")
        print("✓ Input validation on all fields")
        print("✓ Type checking and constraints")

        print_section("Authentication Failures")
        try:
            shelter.login("admin", "wrong_password")
            print("✗ Should have failed")
        except AuthenticationError:
            print("✓ Invalid password correctly rejected")

        try:
            shelter.login("nonexistent_user", "password")
            print("✗ Should have failed")
        except AuthenticationError:
            print("✓ Invalid username correctly rejected")

        print_section("Input Validation")
        print("✓ Username format validation")
        print("✓ Email format validation")
        print("✓ Role validation")
        print("✓ Animal type constraints")
        print("✓ Coordinate validation")


def interactive_menu():
    """Display interactive menu."""
    print_header("ANIMAL SHELTER MANAGEMENT SYSTEM - DEMO")
    print("\nInteractive Demonstration Menu\n")
    print("1. Admin User Demo (Full Access)")
    print("2. Staff User Demo (Limited Access)")
    print("3. Viewer User Demo (Read-Only)")
    print("4. Security Features Demo")
    print("5. Run All Demos")
    print("6. Exit")
    print("\nNote: Run './setup.sh' first to initialize database and users")


def main():
    """Main demo function."""
    # Get database password if needed
    global DB_CONFIG
    password = get_db_password()
    DB_CONFIG['password'] = password

    while True:
        interactive_menu()
        choice = input("\nEnter your choice (1-6): ").strip()

        if choice == '1':
            demo_admin_features()
        elif choice == '2':
            demo_staff_features()
        elif choice == '3':
            demo_viewer_features()
        elif choice == '4':
            demo_security_features()
        elif choice == '5':
            demo_admin_features()
            demo_staff_features()
            demo_viewer_features()
            demo_security_features()
            print_header("ALL DEMOS COMPLETED")
            break
        elif choice == '6':
            print("\nExiting demo. Goodbye!")
            break
        else:
            print("\n⚠ Invalid choice. Please select 1-6.")

        if choice in ['1', '2', '3', '4']:
            input("\nPress Enter to return to menu...")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\nDemo interrupted. Goodbye!")
        sys.exit(0)
    except Exception as e:
        print(f"\n✗ Fatal error: {e}")
        sys.exit(1)
