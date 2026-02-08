#!/bin/bash
set -e # Exit immediately if a command fails

echo "🚀 Starting DevContainer Provisioning..."

# 1. System Updates & Basics
sudo apt-get update
sudo apt-get install -y tree curl wget

# 2. Install yq (Declarative Check)
if ! command -v yq &> /dev/null; then
    echo "📦 Installing yq..."
    sudo wget https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64 -O /usr/local/bin/yq
    sudo chmod +x /usr/local/bin/yq
fi

# 3. Security & Docs Tools
echo "🔍 Installing Security and Documentation tools..."
pip install pre-commit --break-system-packages

# Trivy (Security Scanner)
curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sudo sh -s -- -b /usr/local/bin

# Terraform-docs
curl -Lo ./terraform-docs.tar.gz https://github.com/terraform-docs/terraform-docs/releases/download/v0.17.0/terraform-docs-v0.17.0-linux-amd64.tar.gz
tar -xzf terraform-docs.tar.gz
chmod +x terraform-docs
sudo mv terraform-docs /usr/local/bin/
rm terraform-docs.tar.gz

# 4. Initialize pre-commit
# Using 'python3 -m' is the safest way to call pip-installed tools in a script
python3 -m pre_commit install
