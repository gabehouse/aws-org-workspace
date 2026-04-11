#!/bin/bash
VERSION=$(date +%Y%m%d%H%M%S)

echo "Building JAR file..."
# If using Maven:
mvn clean package -DskipTests

# Identify your JAR (usually in the /target folder)
JAR_PATH="target/wilderchess-app.jar"

# Step 2: Create a clean deployment directory
mkdir -p deploy
cp $JAR_PATH deploy/application.jar
cp Procfile deploy/
cp -r assets deploy/  # Only if your Java app doesn't bundle these inside the JAR

# Step 3: Zip ONLY the artifacts, not the source
cd deploy
zip -r "../app-$VERSION.zip" . *
cd ..

# Step 4: Run Terraform from the infrastructure directory
echo "Navigating to terraform directory..."
# Use an absolute path or a verified relative path to where your main.tf lives
cd ../../infra/workloads/prod/wilderchess

# Now Terraform will find your .tf files
terraform apply -var "app_version=$VERSION" -auto-approve

# Return to where you started
cd -
