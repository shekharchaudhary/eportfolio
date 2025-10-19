# Test Fix Instructions

## Problem
The unit tests are failing because they use Robolectric's `RuntimeEnvironment.getApplication()` which is deprecated.

## Solution

### Option 1: Update Robolectric calls (Recommended)

Replace in **ALL** test files:

**OLD:**
```java
context = RuntimeEnvironment.getApplication();
```

**NEW:**
```java
context = RuntimeEnvironment.application;
```

### Option 2: Use AndroidX Test (Better for Android tests)

Move tests from `test/` to `androidTest/` and use:

```java
@RunWith(AndroidJUnit4.class)
public class InventoryRepositoryTest {

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        repository = new InventoryRepository(context);
        repository.deleteAllItems();
    }
}
```

### Option 3: Remove Robolectric (Simplest for now)

**Update these 3 test files:**

1. `InventoryRepositoryTest.java`
2. `LoginRepositoryTest.java`
3. Any other tests using `RuntimeEnvironment`

**Changes needed:**

**In InventoryRepositoryTest.java:**
```java
// Remove this line
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)

// Change setUp to:
@Before
public void setUp() {
    context = RuntimeEnvironment.application;  // Change from getApplication()
    repository = new InventoryRepository(context);
    repository.deleteAllItems();
}
```

**In LoginRepositoryTest.java:**
```java
// Remove this line
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)

// Change setUp to:
@Before
public void setUp() {
    context = RuntimeEnvironment.application;  // Change from getApplication()
    dataSource = new LoginDataSource(context);
    repository = LoginRepository.getInstance(dataSource);
    repository.logout();
}
```

## Quick Fix Script

Run this in your project directory:

```bash
# Navigate to test directory
cd "app/src/test/java/com/example/inventorycontrolapplication"

# Fix InventoryRepositoryTest.java
sed -i '' 's/RuntimeEnvironment.getApplication()/RuntimeEnvironment.application/g' InventoryRepositoryTest.java

# Fix LoginRepositoryTest.java
sed -i '' 's/RuntimeEnvironment.getApplication()/RuntimeEnvironment.application/g' LoginRepositoryTest.java
```

## Manual Fix

### File 1: InventoryRepositoryTest.java

Find line ~26:
```java
context = RuntimeEnvironment.getApplication();
```

Change to:
```java
context = RuntimeEnvironment.application;
```

### File 2: LoginRepositoryTest.java

Find line ~28:
```java
context = RuntimeEnvironment.getApplication();
```

Change to:
```java
context = RuntimeEnvironment.application;
```

## After Fixing

Run build again:
```bash
./gradlew clean build
```

Or just run tests:
```bash
./gradlew test
```

## Alternative: Skip Tests Temporarily

If you need to build quickly without tests:

```bash
./gradlew build -x test
```

Or in `build.gradle`, temporarily disable:
```gradle
test {
    enabled = false
}
```

## Note

The `PasswordHasherTest.java` should work fine as it doesn't use Android context.
