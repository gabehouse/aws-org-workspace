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
ZSHRC="$HOME/.zshrc"
touch "$ZSHRC"

if ! grep -q "uv venv" "$ZSHRC"; then
    echo "🛠️ Adding uv hooks to .zshrc..."
    cat << 'EOF' >> "$ZSHRC"
# Aliases
alias tt="tree -d -L 3 -I 'node_modules|.git|.terraform|.vite'"

# Auto-activate uv venv
chpwd() {
  if [[ -d .venv ]]; then source .venv/bin/activate
  elif [[ -d venv ]]; then source venv/bin/activate
  fi
}
chpwd
# uv completions
if command -v uv &> /dev/null; then
  eval "$(uv generate-shell-completion zsh)"
fi
EOF
fi

echo "✅ Provisioning complete!"