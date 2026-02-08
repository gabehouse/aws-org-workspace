#!/bin/bash
# validate-all.sh - Full Workspace Verification

echo "🧪 Starting Full Workspace Validation..."

# Find component directories (skipping .terraform)
TARGET_DIRS=$(find infra/workloads infra/management -path "*/.terraform" -prune -o -type d -print)

for dir in $TARGET_DIRS; do
  if ls "$dir"/*.tf >/dev/null 2>&1; then
    echo "──────────────────────────────────────────"
    echo "📂 Verifying: $dir"

    # We enter the directory to ensure relative paths in backend.hcl work correctly
    pushd "$dir" > /dev/null

    # Run init using the symlinked backend.hcl
    # -input=false ensures the script doesn't hang waiting for user input
    terraform init -backend-config="backend.hcl" -input=false -reconfigure

    if [ $? -eq 0 ]; then
      echo "✅ $dir: Init successful"
      terraform validate
      echo "✅ $dir: Validation successful"
    else
      echo "❌ $dir: Init FAILED"
      popd > /dev/null
      exit 1
    fi

    popd > /dev/null
  fi
done

echo "🎉 All components initialized and validated successfully!"
