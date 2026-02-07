#!/bin/bash

# Define the source files (absolute paths are safer for symlinks)
ROOT_DIR=$(pwd)
GLOBAL_VARS="$ROOT_DIR/global.tfvars"
BACKEND_CONFIG="$ROOT_DIR/backend.hcl"

echo "🔗 Starting symlink automation..."

# Find all directories containing a main.tf file (excluding hidden folders)
find . -type d -not -path '*/.*' | while read -r dir; do
    # Skip the root directory itself
    if [ "$dir" == "." ]; then continue; fi

    # Only link into directories that have terraform code
    if [ -f "$dir/main.tf" ]; then
        echo "  -> Linking in: $dir"

        # Create global.auto.tfvars (Terraform auto-loads .auto.tfvars)
        ln -sf "$GLOBAL_VARS" "$dir/global.auto.tfvars"

        # Create backend.hcl link for easy access during init
        ln -sf "$BACKEND_CONFIG" "$dir/backend.hcl"
    fi
done

echo "✅ Done! All folders are now synced to global config."
