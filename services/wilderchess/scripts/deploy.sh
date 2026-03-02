#!/bin/bash

# A script to build and deploy an application package with a version number.
# Login to aws with aws sso login -profile gabriel-prod-emergencyadmin
# Usage: ./deploy.sh [version]
# If no version is provided, it defaults to version 1.

# Get the version from the first command-line argument
version="$1"

# Step 1: Navigate to the directory containing index.js.
# Assuming the script is run from the root directory.

# Step 2: Create a backup of index.js before modification.
echo "Creating backup of assets/index.js..."
cp assets/index.js assets/index.js2

# Step 3: Use sed to replace the specific line in index.js.
# We use a non-standard delimiter '|' to avoid escaping the forward slashes in the URL.
echo "Replacing WebSocket URL in assets/index.js..."
search_pattern='s|this.ws = new WebSocket('\''ws://localhost:8080/'\'');|this.ws = new WebSocket('\''ws://Wilderchess.eba-nsb5rgs7.us-east-2.elasticbeanstalk.com/'\'');|g'
sed -i.bak "$search_pattern" assets/index.js

# Step 4: Determine the filename based on whether a version was provided.
if [[ -z "$version" ]]; then
    echo "No version provided. Defaulting to version 1."
    base_filename="app-1.zip"
    version="1"
else
    echo "Using provided version: $version"
    base_filename="app-$version.zip"
fi

# Step 5: Create a zip archive of the necessary files and directories.
# Changed from '7z a' to 'zip -r' for better compatibility with Elastic Beanstalk.
echo "Creating zip archive: $base_filename..."
zip -r "$base_filename" "src" ".ebextensions" "assets" "pom.xml" "Procfile" "Buildfile"

# Step 6: Restore the original index.js from the backup.
echo "Restoring assets/index.js..."
cp assets/index.js2 assets/index.js
# Clean up the backup file created by sed
rm assets/index.js.bak

# Step 7: Change into the terraform directory to apply the changes.
echo "Navigating to terraform directory..."
cd ../../infra/workloads/prod/wilderchess

# Step 8: Run the terraform apply command with the application version.
echo "Applying terraform changes with app_version=$version..."
terraform apply -var "app_version=$version"

# Step 9: Navigate back to the parent directory.
echo "Returning to parent directory..."
cd ..

echo "Script execution complete."
