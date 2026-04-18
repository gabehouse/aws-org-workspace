#!/bin/bash
set -e

echo "🚀 Starting DevContainer Provisioning..."

# 1. System Basics (Fast check)
if ! command -v tree &> /dev/null; then
    sudo apt-get update && sudo apt-get install -y tree curl wget
fi

# 1.5 Git Safety Configuration
echo "🛡️ Configuring Git safe directory..."
# This handles the "dubious ownership" error in DevContainers
if [[ $(git config --global --get-all safe.directory) != *"/workspaces/aws-org-workspace"* ]]; then
    git config --global --add safe.directory /workspaces/aws-org-workspace
fi

# --- Git Identity Setup ---
# This ensures you never see the "Author identity unknown" error again.
if [[ -z "$(git config --global user.email)" ]]; then
    echo "👤 Configuring Git Identity..."
    git config --global user.name "Gabe House"
    git config --global user.email "gabriel.jsh@gmail.com"
fi

# 2. Install uv (The 'pnpm' of Python)
if ! command -v uv &> /dev/null; then
    echo "⚡ Preparing home directories..."
    sudo mkdir -p /home/vscode/.local/bin
    sudo chown -R vscode:vscode /home/vscode/.local
    
    echo "⚡ Installing uv..."
    curl -LsSf https://astral.sh/uv/install.sh | INSTALLER_NO_MODIFY_PATH=1 sh
    export PATH="/home/vscode/.local/bin:$PATH"
fi

# 3. The "Install Once" Magic
# We use the full path to uv to ensure it works during the first run
UV_BIN="/home/vscode/.local/bin/uv"

if [ -f "pyproject.toml" ]; then
    echo "🐍 Syncing Python Workspace..."
    # Fix cache permissions for the volume mount
    sudo mkdir -p /home/vscode/.cache/uv
    sudo chown -R vscode:vscode /home/vscode/.cache/uv

    $UV_BIN sync
else
    echo "📦 No pyproject.toml, creating standalone venv..."
    $UV_BIN venv
    $UV_BIN pip install boto3 pandas torch onnxruntime pre-commit
fi # <--- YOU WERE MISSING THIS

# 4. Terraform-docs (Only download if missing)
if ! command -v terraform-docs &> /dev/null; then
    echo "📄 Installing terraform-docs..."
    curl -Lo ./terraform-docs.tar.gz https://github.com/terraform-docs/terraform-docs/releases/download/v0.17.0/terraform-docs-v0.17.0-linux-amd64.tar.gz
    tar -xzf terraform-docs.tar.gz
    chmod +x terraform-docs
    sudo mv terraform-docs /usr/local/bin/
    rm terraform-docs.tar.gz
fi

# 5. Safe Zsh Configuration
ZSHRC="/home/vscode/.zshrc"

if ! grep -q "AWS Profile Shortcuts" "$ZSHRC"; then
    echo "🛠️ Injecting Clean AWS & Navigation shortcuts..."
    cat << 'EOF' >> "$ZSHRC"

# --- AWS Profile Shortcuts ---
# Logic: If a profile is already set by tasks.json, we don't overwrite it.
# We only run the login check if we are actually using the command.
setprod() {
  export AWS_PROFILE=prod
  aws sts get-caller-identity --profile prod >/dev/null 2>&1 || aws sso login --profile prod
  echo "✅ Switched to PROD"
}

setdev() {
  export AWS_PROFILE=dev
  aws sts get-caller-identity --profile dev >/dev/null 2>&1 || aws sso login --profile dev
  echo "✅ Switched to DEV"
}

# --- Aliases ---
alias tf="terraform"
# Folders only by default
alias tt="tree -d -L 3 -I 'node_modules|.git|.terraform|.vite'"
# Files + Folders option
alias ta="tree -a -L 3 -I 'node_modules|.git|.terraform|.vite'"

# --- Navigation (The Context Switcher) ---

alias cdpw="cd /workspaces/aws-org-workspace/infra/workloads/prod && setprod"
alias cddw="cd /workspaces/aws-org-workspace/infra/workloads/dev && setdev"

# Management & Identity (High Privilege)
alias cdmg="cd /workspaces/aws-org-workspace/infra/management && setmgmt"

# Shared Infrastructure Modules
alias cdm="cd /workspaces/aws-org-workspace/infra/modules"

# Projects & Frontend
alias cda="cd /workspaces/aws-org-workspace/services/AcidSaturator"
alias cdp="cd /workspaces/aws-org-workspace/services/cloud-portfolio"
alias cdw="cd /workspaces/aws-org-workspace/services/wilderchess"

# Movement Shortcuts
alias ..="cd .."
alias ...="cd ../.."
alias ....="cd ../../.."

# --- Fix for Tasks.json ---
# This ensures that when tasks.json runs 'zsh', it loads this file correctly
# without re-printing the NVM errors.
export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && \. "$NVM_DIR/nvm.sh" --no-use
EOF
fi

echo "✅ Provisioning complete!"