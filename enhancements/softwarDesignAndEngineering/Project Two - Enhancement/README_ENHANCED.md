# Inventory Control Application - Enhanced Edition

## CS-499 Enhancement 1: Software Design & Engineering

### Overview
This is an enhanced version of the Android Inventory Control Application, demonstrating professional software engineering practices including SOLID principles, comprehensive error handling, structured logging, improved modularity, and extensive test coverage.

---

## Table of Contents
1. [What's New](#whats-new)
2. [Architecture](#architecture)
3. [Setup Instructions](#setup-instructions)
4. [Running Tests](#running-tests)
5. [Key Features](#key-features)
6. [Code Organization](#code-organization)
7. [Usage Guide](#usage-guide)

---

## What's New

### ✨ Major Enhancements

#### 1. SOLID Principles Implementation
- **Single Responsibility:** Each class has one clearly defined purpose
- **Open/Closed:** Extended through interfaces, closed for modification
- **Liskov Substitution:** Repository implementations are fully substitutable
- **Interface Segregation:** Focused interfaces (IInventoryRepository, IAuthRepository)
- **Dependency Inversion:** High-level modules depend on abstractions

#### 2. Structured Logging System
- **AppLogger utility** for centralized logging
- Multiple log levels: DEBUG, INFO, WARNING, ERROR
- Specialized logging methods for database and auth operations
- Method entry/exit tracing
- Production-ready configuration

#### 3. Comprehensive Error Handling
- Input validation with meaningful error messages
- Try-catch blocks around all database operations
- Graceful error recovery
- Proper resource cleanup (cursors)
- User-friendly error feedback

#### 4. Improved Architecture
- **Repository Pattern** for data access abstraction
- **Dependency Injection** via constructors and factories
- **ViewModelFactory** for proper DI in ViewModels
- Clear separation between layers (Presentation, Domain, Data)

#### 5. Extensive Test Coverage
- **66 unit tests** across 3 test suites
- **PasswordHasherTest** - 21 tests
- **InventoryRepositoryTest** - 24 tests
- **LoginRepositoryTest** - 21 tests
- Uses Robolectric for Android context testing

---

## Architecture

### Layer Structure

```
┌─────────────────────────────────────┐
│     Presentation Layer              │
│  (Activities, Fragments, ViewModels)│
└────────────┬────────────────────────┘
             │
             ↓
┌─────────────────────────────────────┐
│       Domain Layer                  │
│   (Repository Interfaces,           │
│    Business Logic)                  │
└────────────┬────────────────────────┘
             │
             ↓
┌─────────────────────────────────────┐
│        Data Layer                   │
│  (Repository Implementations,       │
│   Data Sources, Database)           │
└─────────────────────────────────────┘
```

### Key Components

#### Repositories
- **IInventoryRepository** - Interface for inventory operations
- **InventoryRepository** - Concrete implementation with SQLite
- **IAuthRepository** - Interface for authentication
- **LoginRepository** - Singleton managing auth state

#### ViewModels
- **HomeViewModel** - Manages inventory list UI state
- **HomeViewModelFactory** - Creates HomeViewModel with dependencies

#### Utilities
- **AppLogger** - Centralized structured logging
- **PasswordHasher** - PBKDF2 password hashing with salt
- **SqlDbHelper** - Database management

---

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 28 or higher
- Gradle 7.0+
- Java 8

### Build the Project

1. **Clone/Open the project in Android Studio**

2. **Sync Gradle dependencies**
   ```bash
   ./gradlew build
   ```

3. **Run the application**
   - Click "Run" in Android Studio
   - Or use command line:
   ```bash
   ./gradlew installDebug
   ```

### Dependencies

The enhanced version includes:

```gradle
// Core Android
implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.0'
implementation 'androidx.navigation:navigation-fragment:2.3.5'

// Testing
testImplementation 'junit:junit:4.13.2'
testImplementation 'org.mockito:mockito-core:3.9.0'
testImplementation 'org.robolectric:robolectric:4.5.1'

// AndroidX Testing
androidTestImplementation 'androidx.test.ext:junit:1.1.2'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'
```

---

## Running Tests

### Command Line

**Run all unit tests:**
```bash
./gradlew test
```

**Run specific test class:**
```bash
./gradlew test --tests PasswordHasherTest
./gradlew test --tests InventoryRepositoryTest
./gradlew test --tests LoginRepositoryTest
```

**Run with coverage:**
```bash
./gradlew testDebugUnitTestCoverage
```

### Android Studio

1. Right-click on test file or test class
2. Select "Run [TestName]"
3. View results in Run panel

### Expected Output
```
PasswordHasherTest          PASSED (21/21)
InventoryRepositoryTest     PASSED (24/24)
LoginRepositoryTest         PASSED (21/21)
─────────────────────────────────────
Total:                      66 tests passed
```

---

## Key Features

### 🔒 Security
- **Password Hashing:** PBKDF2WithHmacSHA256, 120k iterations
- **SQL Injection Prevention:** Parameterized queries
- **Constant-time Comparison:** Prevents timing attacks
- **Automatic Migration:** Legacy plaintext passwords upgraded

### 📊 Logging
```java
// Method tracing
AppLogger.logMethodEntry(TAG, "methodName");

// Database operations
AppLogger.logDatabaseOperation("INSERT", "inventory", true);

// Authentication events
AppLogger.logAuthEvent("LOGIN", username, true);

// Error logging
AppLogger.e(TAG, "Error message", exception);
```

### ✅ Error Handling

All repository methods return `Result<T>`:
```java
Result<List<HashMap>> result = repository.getAllItems();

if (result instanceof Result.Success) {
    List<HashMap> items = ((Result.Success<List<HashMap>>) result).getData();
    // Handle success
} else if (result instanceof Result.Error) {
    Exception error = ((Result.Error) result).getError();
    // Handle error
}
```

### 🧪 Testability

Interfaces enable easy mocking:
```java
// In tests, mock the repository
IInventoryRepository mockRepo = mock(IInventoryRepository.class);
HomeViewModel viewModel = new HomeViewModel(mockRepo);
```

---

## Code Organization

### Directory Structure

```
app/src/main/java/com/example/inventorycontrolapplication/
│
├── data/
│   ├── repository/
│   │   ├── IAuthRepository.java           # Auth repository interface
│   │   ├── IInventoryRepository.java      # Inventory repository interface
│   │   └── InventoryRepository.java       # Inventory implementation
│   ├── helpers/
│   │   ├── PasswordHasher.java            # Password security
│   │   ├── SqlDbHelper.java               # Database helper
│   │   └── SqlCommands.java               # SQL statements
│   ├── model/
│   │   ├── LoggedInUser.java              # User model
│   │   └── SqlDbContract.java             # Database schema
│   ├── LoginDataSource.java               # Auth data operations
│   ├── LoginRepository.java               # Auth repository
│   ├── InventoryDataSource.java           # Legacy data source
│   └── Result.java                        # Result wrapper
│
├── ui/
│   ├── home/
│   │   ├── HomeFragment.java              # Inventory list UI
│   │   ├── HomeViewModel.java             # Inventory list logic
│   │   └── HomeViewModelFactory.java      # ViewModel factory
│   ├── login/
│   │   ├── LoginActivity.java             # Login screen
│   │   └── LoginViewModel.java            # Login logic
│   └── ...
│
├── utils/
│   └── AppLogger.java                     # Logging utility
│
└── MainActivity.java                      # Main navigation

app/src/test/java/com/example/inventorycontrolapplication/
├── PasswordHasherTest.java                # Security tests
├── InventoryRepositoryTest.java           # Repository tests
└── LoginRepositoryTest.java               # Auth tests
```

---

## Usage Guide

### For Developers

#### Adding a New Repository

1. **Create interface:**
```java
public interface IMyRepository {
    Result<Data> getData();
}
```

2. **Implement interface:**
```java
public class MyRepository implements IMyRepository {
    @Override
    public Result<Data> getData() {
        AppLogger.logMethodEntry(TAG, "getData");
        try {
            // Implementation
            AppLogger.logDatabaseOperation("SELECT", "table", true);
            return new Result.Success<>(data);
        } catch (Exception e) {
            AppLogger.e(TAG, "Error getting data", e);
            return new Result.Error(e);
        }
    }
}
```

3. **Inject into ViewModel:**
```java
public class MyViewModel extends ViewModel {
    private final IMyRepository repository;

    public MyViewModel(@NonNull IMyRepository repository) {
        this.repository = repository;
    }
}
```

#### Adding Logging

```java
// At method entry
AppLogger.logMethodEntry(TAG, "myMethod");

// For database operations
AppLogger.logDatabaseOperation("INSERT", "users", success);

// For errors
AppLogger.e(TAG, "Operation failed", exception);

// At method exit
AppLogger.logMethodExit(TAG, "myMethod");
```

#### Writing Tests

```java
@Test
public void testMyFeature() {
    // Arrange
    IMyRepository mockRepo = mock(IMyRepository.class);
    when(mockRepo.getData()).thenReturn(new Result.Success<>(data));

    // Act
    MyViewModel viewModel = new MyViewModel(mockRepo);
    Result<Data> result = viewModel.loadData();

    // Assert
    assertTrue(result instanceof Result.Success);
}
```

---

## Performance Considerations

### Database Operations
- Parameterized queries for efficiency
- Proper cursor management (closed in finally blocks)
- Minimal object allocation in hot paths

### Memory Management
- Cursors properly closed
- No memory leaks in ViewModels
- Efficient data structures (HashMap for flexibility)

### Logging
- Configurable logging levels
- Can be disabled in production
- Minimal performance impact

---

## Security Best Practices

### Implemented
✅ Password hashing with PBKDF2
✅ Random salt per password
✅ SQL injection prevention
✅ Constant-time password comparison
✅ Secure password migration

### Recommended for Production
- Encrypt local database (SQLCipher)
- Use Android Keystore for sensitive data
- Implement certificate pinning
- Enable ProGuard/R8 obfuscation
- Add network security configuration

---

## Troubleshooting

### Build Issues

**Problem:** Gradle sync fails
```bash
Solution: ./gradlew clean build --refresh-dependencies
```

**Problem:** Test failures
```bash
Solution: Check Android SDK version (should be 28+)
         Update Gradle wrapper if needed
```

### Runtime Issues

**Problem:** Database errors
```
Check AppLogger output for detailed error messages
Enable verbose logging: AppLogger.setLoggingEnabled(true)
```

**Problem:** Login fails
```
Check that passwords are being hashed correctly
Review LoginDataSource logs
```

---

## Contributing Guidelines

### Code Style
- Follow existing patterns
- Add JavaDoc comments
- Include unit tests for new features
- Use AppLogger for all logging
- Implement error handling

### Pull Request Checklist
- [ ] All tests pass
- [ ] New tests added for new features
- [ ] Code follows SOLID principles
- [ ] Error handling implemented
- [ ] Logging statements added
- [ ] Documentation updated

---

## Additional Resources

### Documentation
- [ENHANCEMENT_SUMMARY.md](ENHANCEMENT_SUMMARY.md) - Detailed enhancement documentation
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)

### Related Files
- `app/build.gradle` - Dependencies and build configuration
- `app/src/main/AndroidManifest.xml` - App permissions and activities

---

## Contact & Support

For questions or issues:
1. Check the logs using AppLogger
2. Review ENHANCEMENT_SUMMARY.md
3. Check test cases for usage examples
4. Review inline code documentation

---

## License

Educational project for CS-499 Computer Science Capstone
Southern New Hampshire University

---

**Last Updated:** October 2025
**Version:** 2.0 (Enhanced)
**Android SDK:** 28+
**Test Coverage:** 66 unit tests
