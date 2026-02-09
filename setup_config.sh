#!/bin/bash

# 1. Scrape values from config.yaml
BUCKET=$(yq '.backend.bucket' config.yaml)
REGION=$(yq '.default_region' config.yaml)
TABLE=$(yq '.backend.dynamodb_table' config.yaml)
PROFILE=$(yq '.backend.profile' config.yaml)

# 2. Generate Local backend.hcl (Includes Profile)
cat <<EOF > infra/backend.hcl
bucket         = "$BUCKET"
region         = "$REGION"
dynamodb_table = "$TABLE"
profile        = "$PROFILE"
EOF

# 3. Generate CI/CD backend.ci.hcl (No Profile)
cat <<EOF > infra/backend.ci.hcl
bucket         = "$BUCKET"
region         = "$REGION"
dynamodb_table = "$TABLE"
EOF

echo "✅ Generated Local and CI/CD backend configs."

# 4. Symlink BOTH to the workload directories
TARGET_DIRS=$(find infra/workloads infra/management -path "*/.terraform" -prune -o -type d -print)

for dir in $TARGET_DIRS; do
  if ls "$dir"/*.tf >/dev/null 2>&1; then
    echo "🔗 Linking $dir..."

    # Link Local Config
    REL_LOCAL=$(realpath --relative-to="$dir" "infra/backend.hcl")
    ln -sf "$REL_LOCAL" "$dir/backend.hcl"
  fi
done

echo "🚀 All symlinks synchronized!"
