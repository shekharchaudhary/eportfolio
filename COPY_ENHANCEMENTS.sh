#!/bin/bash

# Script to copy enhanced files to the correct project location

SOURCE_DIR="/Users/cshekhar/Desktop/SHNU/cs499/eportfolio/orignals/Software Design and Engineering/Project Two"
TARGET_DIR="/Users/cshekhar/Desktop/SHNU/cs499/eportfolio/enhancements/softwarDesignAndEngineering/Project Two - Enhancement"

echo "Copying enhanced files from orignals to enhancements project..."

# Copy layout files
echo "Copying layouts..."
cp -v "$SOURCE_DIR/app/src/main/res/layout/fragment_home.xml" "$TARGET_DIR/app/src/main/res/layout/" 2>/dev/null
cp -v "$SOURCE_DIR/app/src/main/res/layout/item_warehouse.xml" "$TARGET_DIR/app/src/main/res/layout/" 2>/dev/null
cp -v "$SOURCE_DIR/app/src/main/res/layout/fragment_add_data.xml" "$TARGET_DIR/app/src/main/res/layout/" 2>/dev/null
cp -v "$SOURCE_DIR/app/src/main/res/layout/fragment_edit_data.xml" "$TARGET_DIR/app/src/main/res/layout/" 2>/dev/null
cp -v "$SOURCE_DIR/app/src/main/res/layout/activity_register.xml" "$TARGET_DIR/app/src/main/res/layout/" 2>/dev/null

# Copy drawable resources
echo "Copying drawables..."
mkdir -p "$TARGET_DIR/app/src/main/res/drawable"
cp -v "$SOURCE_DIR/app/src/main/res/drawable/badge_background.xml" "$TARGET_DIR/app/src/main/res/drawable/" 2>/dev/null
cp -v "$SOURCE_DIR/app/src/main/res/drawable/count_background.xml" "$TARGET_DIR/app/src/main/res/drawable/" 2>/dev/null

# Copy strings
echo "Copying strings.xml..."
cp -v "$SOURCE_DIR/app/src/main/res/values/strings.xml" "$TARGET_DIR/app/src/main/res/values/" 2>/dev/null

# Copy Java files
echo "Copying Java files..."

# Home fragment
cp -v "$SOURCE_DIR/app/src/main/java/com/example/inventorycontrolapplication/ui/home/HomeFragment.java" \
   "$TARGET_DIR/app/src/main/java/com/example/inventorycontrolapplication/ui/home/" 2>/dev/null

# Add Data fragment
cp -v "$SOURCE_DIR/app/src/main/java/com/example/inventorycontrolapplication/ui/home/AddDataFragment.java" \
   "$TARGET_DIR/app/src/main/java/com/example/inventorycontrolapplication/ui/home/" 2>/dev/null

# Register Activity
cp -v "$SOURCE_DIR/app/src/main/java/com/example/inventorycontrolapplication/ui/login/RegisterActivity.java" \
   "$TARGET_DIR/app/src/main/java/com/example/inventorycontrolapplication/ui/login/" 2>/dev/null

# Login ViewModel
cp -v "$SOURCE_DIR/app/src/main/java/com/example/inventorycontrolapplication/ui/login/LoginViewModel.java" \
   "$TARGET_DIR/app/src/main/java/com/example/inventorycontrolapplication/ui/login/" 2>/dev/null

# Home ViewModel and Factory
cp -v "$SOURCE_DIR/app/src/main/java/com/example/inventorycontrolapplication/ui/home/HomeViewModel.java" \
   "$TARGET_DIR/app/src/main/java/com/example/inventorycontrolapplication/ui/home/" 2>/dev/null

cp -v "$SOURCE_DIR/app/src/main/java/com/example/inventorycontrolapplication/ui/home/HomeViewModelFactory.java" \
   "$TARGET_DIR/app/src/main/java/com/example/inventorycontrolapplication/ui/home/" 2>/dev/null

# Repository files
echo "Copying repository files..."
mkdir -p "$TARGET_DIR/app/src/main/java/com/example/inventorycontrolapplication/data/repository"
cp -v "$SOURCE_DIR/app/src/main/java/com/example/inventorycontrolapplication/data/repository/"*.java \
   "$TARGET_DIR/app/src/main/java/com/example/inventorycontrolapplication/data/repository/" 2>/dev/null

# Utils
echo "Copying utils..."
mkdir -p "$TARGET_DIR/app/src/main/java/com/example/inventorycontrolapplication/utils"
cp -v "$SOURCE_DIR/app/src/main/java/com/example/inventorycontrolapplication/utils/AppLogger.java" \
   "$TARGET_DIR/app/src/main/java/com/example/inventorycontrolapplication/utils/" 2>/dev/null

# Updated LoginRepository and LoginDataSource
cp -v "$SOURCE_DIR/app/src/main/java/com/example/inventorycontrolapplication/data/LoginRepository.java" \
   "$TARGET_DIR/app/src/main/java/com/example/inventorycontrolapplication/data/" 2>/dev/null

cp -v "$SOURCE_DIR/app/src/main/java/com/example/inventorycontrolapplication/data/LoginDataSource.java" \
   "$TARGET_DIR/app/src/main/java/com/example/inventorycontrolapplication/data/" 2>/dev/null

echo ""
echo "✅ Files copied successfully!"
echo ""
echo "Now open this project in Android Studio:"
echo "$TARGET_DIR"
echo ""
echo "Then run: File > Sync Project with Gradle Files"
echo "Then: Build > Rebuild Project"
