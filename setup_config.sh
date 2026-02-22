#!/bin/bash

# 1. Scrape values from config.yaml
BUCKET=$(yq '.backend.bucket' config.yaml)
REGION=$(yq '.default_region' config.yaml)
PROFILE=$(yq '.backend.profile' config.yaml)

# 2. Generate Local backend.hcl (Includes Profile)
cat <<EOF > infra/backend.hcl
bucket         = "$BUCKET"
region         = "$REGION"
use_lockfile = true
profile        = "$PROFILE"
EOF

# 3. Generate CI/CD backend.ci.hcl (No Profile)
cat <<EOF > infra/backend.ci.hcl
bucket         = "$BUCKET"
region         = "$REGION"
use_lockfile = true
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

# 5. Generate Frontend .env file
FRONTEND_DIR="services/vstshop-frontend"
VITE_REGION=$(yq '.default_region' config.yaml)
VITE_POOL_ID=$(yq '.vst_shop.frontend.user_pool_id' config.yaml)
VITE_CLIENT_ID=$(yq '.vst_shop.frontend.client_id' config.yaml)
VITE_API=$(yq '.vst_shop.frontend.api_url' config.yaml)

cat <<EOF > "$FRONTEND_DIR/.env"
VITE_AWS_REGION=$VITE_REGION
VITE_USER_POOL_ID=$VITE_POOL_ID
VITE_USER_POOL_CLIENT_ID=$VITE_CLIENT_ID
VITE_API_URL=$VITE_API
EOF

echo "✨ Frontend .env generated in $FRONTEND_DIR"
