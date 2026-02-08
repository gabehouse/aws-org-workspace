#!/bin/bash

# 1. Scrape values from config.yaml
BUCKET=$(yq '.backend.bucket' config.yaml)
REGION=$(yq '.default_region' config.yaml)
TABLE=$(yq '.backend.dynamodb_table' config.yaml)
PROFILE=$(yq '.backend.profile' config.yaml)

# Scrape IAM and Account values for the CI backend
MGMT_ID=$(yq '.accounts.mgmt' config.yaml)
GATEWAY_ROLE=$(yq '.iam.gateway_role_name' config.yaml)

# 2. Generate Local backend.hcl (Standard Profile usage)
cat <<EOF > infra/backend.hcl
bucket         = "$BUCKET"
region         = "$REGION"
dynamodb_table = "$TABLE"
profile        = "$PROFILE"
EOF

# 3. Generate CI/CD backend.ci.hcl (Assume Role for State)
# We use the modern 'assume_role' block syntax
cat <<EOF > infra/backend.ci.hcl
bucket         = "$BUCKET"
region         = "$REGION"
dynamodb_table = "$TABLE"
assume_role = {
  role_arn = "arn:aws:iam::${MGMT_ID}:role/${GATEWAY_ROLE}"
}
EOF

echo "✅ Generated Local and CI/CD (cross-account) backend configs."

# 4. Symlink BOTH to the workload directories
TARGET_DIRS=$(find infra/workloads infra/management -path "*/.terraform" -prune -o -type d -print)

for dir in $TARGET_DIRS; do
  if ls "$dir"/*.tf >/dev/null 2>&1; then
    echo "🔗 Linking $dir..."

    # Link Local Config
    REL_LOCAL=$(realpath --relative-to="$dir" "infra/backend.hcl")
    ln -sf "$REL_LOCAL" "$dir/backend.hcl"

    # Link CI Config
    REL_CI=$(realpath --relative-to="$dir" "infra/backend.ci.hcl")
    ln -sf "$REL_CI" "$dir/backend.ci.hcl"
  fi
done

echo "🚀 All symlinks synchronized!"
