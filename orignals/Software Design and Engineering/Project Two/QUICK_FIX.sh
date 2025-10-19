#!/bin/bash

# Quick fix script for test errors
# Run this from your project root directory

echo "Fixing Robolectric test files..."

# Find and fix InventoryRepositoryTest.java
find . -name "InventoryRepositoryTest.java" -type f -exec sed -i '' 's/RuntimeEnvironment\.getApplication()/RuntimeEnvironment.application/g' {} \;

# Find and fix LoginRepositoryTest.java
find . -name "LoginRepositoryTest.java" -type f -exec sed -i '' 's/RuntimeEnvironment\.getApplication()/RuntimeEnvironment.application/g' {} \;

echo "Done! Now run: ./gradlew clean build"
