# 🌐 Phoenix — AWS Multi-Account Platform Template

Phoenix is a **secure, automated, and scalable infrastructure-as-code (IaC) platform** for deploying workloads across multiple AWS accounts using **Terraform** and **GitHub Actions**.

It is designed around AWS best practices: **strong isolation**, **zero long-lived credentials**, and a **GitOps-driven workflow**.

---

## 🚀 Features

- **Security First**
  - Zero hardcoded IAM credentials
  - Uses **OIDC (OpenID Connect)** for GitHub Actions to assume short-lived AWS roles

- **Multi-Environment Isolation**
  - Separate AWS accounts for **Management**, **Development**, and **Production**
  - Centralized control plane with strict trust boundaries

- **Automated Infrastructure**
  - `terraform plan` runs automatically on pull requests
  - `terraform apply` runs on merge to environment branches

- **GitOps Workflow**
  - Centralized Terraform state stored in S3
  - Native S3 state locking via `use_lockfile = true`
  - No DynamoDB lock table required

---

## 🏗️ Architecture Overview

Phoenix uses a **hub-and-spoke** architecture to maximize isolation and security:

1. **Management Account (Control Plane)**
   - Hosts the GitHub OIDC provider
   - Owns the global Terraform state bucket
   - Contains the `GatewayRole`

2. **Workload Accounts (Dev / Prod)**
   - Contain all application and infrastructure resources
   - Examples: Networking, S3, CloudFront, services

3. **Secure Role Hopping**
   - GitHub Actions assumes the `GatewayRole` in the Management account
   - The `GatewayRole` then assumes environment-specific `ExecutionRoles`
   - No direct GitHub access to workload accounts

## 📂 Project Structure


```text
.
├── config.yaml               # ⚙️ Global configuration (AWS IDs, Regions, Role names)
├── setup_config.sh           # 🛠️ Script to generate backend and local .hcl files
├── infra/
│   ├── management/           # 🔐 Control Plane (Run these first)
│   │   ├── bootstrap/        # Creates the S3 bucket for Terraform state
│   │   ├── identity/         # Sets up GitHub OIDC and the GatewayRole
│   │   └── organization/     # (Optional) Manages AWS Organizations/Accounts
│   ├── modules/              # 🧱 Reusable Infrastructure Blocks (DRY)
│   │   ├── globals/          # Shared tags and project-wide variables
│   │   ├── networking/       # VPC, Subnet, and Routing logic
│   │   └── vstshop-frontend/ # S3, CloudFront, and OAC logic
│   └── workloads/            # 🚀 Environment-Specific Deployments
│       ├── dev/              # Sandbox environment (Automatic Deploys)
│       └── prod/             # Production environment (Manual Approval Required)
├── services/                 # 💻 Application Source Code
│   └── vstshop/              # Static assets (HTML/CSS/JS) for the frontend
└── .github/workflows/        # 🤖 CI/CD Pipelines
    ├── deploy-dev.yml        # Triggered on push to dev branch
    └── deploy-prod.yml       # Triggered on merge to master branch

## 🛠️ Quick Start

### Prerequisites

- **AWS Organization**
  - Must already exist
  - Created via the AWS Console in the Management account

- **Terraform**
  - Version `v1.10+`

- **yq**
  - Used to parse the configuration YAML

- **AWS CLI**
  - Configured with a profile for the Management account

---

### Step 1: Configure the Platform

Edit the `config.yaml` file at the repository root with your project name, AWS region, and account IDs (from AWS Organizations):

```yaml
project_name: "phoenix"
default_region: "us-east-2"

accounts:
  mgmt: "086739225244"
  dev:  "195481994910"
  prod: "212984412001"

iam:
  gateway_role_name: "github-actions-oidc-role"
  dev_execution_role_name: "terraform-execution-role-dev"
  prod_execution_role_name: "terraform-execution-role-prod"

## Deployment Guide

### Step 2: Generate Environment Configuration

Run the setup script to generate derived configuration files:
```bash
chmod +x setup_config.sh
./setup_config.sh
```

### Step 3: Bootstrap the Infrastructure (Required Order)

The initial trust chain must be established in the following order. All bootstrap steps should be run locally using the Management account AWS profile.

1. **Management Bootstrap** (`infra/management/bootstrap`)
   - Creates the global S3 Terraform state bucket

2. **Management Identity** (`infra/management/identity`)
   - Configures GitHub OIDC trust
   - Creates the GatewayRole

3. **Workload Account Identity** (`infra/workloads/[env]/iam`)
   - Creates environment-specific execution roles (Dev & Prod)
   - Allows role assumption from the Management account

4. **Workloads** (`infra/workloads/[env]/*`)
   - Deploy networking, services, and application infrastructure
   - Example: `infra/workloads/prod/vstshop`

### CI/CD Workflow

| Event        | Branch      | Action            | Target Account | AWS Impact            |
|--------------|-------------|-------------------|----------------|-----------------------|
| Pull Request | `feature/**`| `terraform plan`  | Dev / Prod     | Validation only       |
| Push / Merge | `dev`       | `terraform apply` | Development    | Dev changes applied   |
| Push / Merge | `master`    | `terraform apply` | Production     | Applied **after** Manual Approval |

## License

This project is licensed under the Apache License 2.0.
