#!/bin/bash
sudo apt-get update
sudo apt-get install -y tree curl
pip install pre-commit --break-system-packages
curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sudo sh -s -- -b /usr/local/bin
curl -Lo ./terraform-docs.tar.gz https://github.com/terraform-docs/terraform-docs/releases/download/v0.17.0/terraform-docs-v0.17.0-linux-amd64.tar.gz
tar -xzf terraform-docs.tar.gz
chmod +x terraform-docs
sudo mv terraform-docs /usr/local/bin/
rm terraform-docs.tar.gz
/home/vscode/.local/bin/pre-commit install
