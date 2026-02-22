🎹 VST Shop Engine (Full-Stack)
A modern, high-performance template for selling digital assets (VSTs, samples, or software). Powered by React + Vite on the frontend and a serverless AWS architecture managed entirely via Terraform.

🚀 Architecture Overview
This project uses a "Infrastructure as Code" approach to ensure 1:1 parity between development and production.

Frontend: React (Vite) hosted on AWS CloudFront/S3.

API: AWS API Gateway with Cognito User Pool Authorization.

Compute: Python 3.12 Lambdas (handled as individual microservices).

Database: DynamoDB for tracking user purchases.

Payments: Stripe API integration with automated Webhook fulfillment.

Storage: Secure S3 bucket with Lambda-generated Presigned URLs for downloads.

🛠️ Tech Stack & Dependencies
Backend (Terraform Managed)
Stripe: Automated product/price syncing.

Lambda Layers: Automated Python dependency packaging (Stripe CLI).

IAM: Least-privilege execution roles for all services.

Frontend
Vite: For ultra-fast Hot Module Replacement (HMR).

AWS Amplify SDK: For Cognito Auth and Session management.

Tailwind CSS: (Optional/Suggested) for rapid UI styling.

📂 Project Structure
.
├── config.yaml             # Single source of truth for VST catalog & constants
├── infra/                  # Infrastructure as Code (Terraform)
│   ├── backend.hcl         # Global S3/DynamoDB remote state configuration
│   ├── management/         # Root-level account & identity management
│   │   ├── bootstrap/      # Initial setup of Terraform state S3/DynamoDB
│   │   ├── identity/       # OIDC (GitHub Actions) & Global IAM roles
│   │   └── organization/   # AWS Organizations & Account-level settings
│   ├── modules/            # Reusable business logic (The "Blueprints")
│   │   ├── globals/        # Standardized tags and naming conventions
│   │   ├── networking/     # VPC, Subnets, and Security Groups
│   │   └── vstshop/        # Component modules: /auth, /backend, /database, /storage
│   └── workloads/          # Environment deployments (The "Instances")
│       ├── dev/            # Isolated Development environment
│       └── prod/           # Isolated Production environment
├── services/
│   └── vstshop-frontend/   # React + Vite application source code
├── setup_config.sh         # Syncs config.yaml to frontend/backend runtimes
└── validate-all.sh         # Security, linting, and integrity check script

⚡ Quick Start
1. Prerequisites
AWS CLI configured with appropriate credentials.

Terraform installed.

Python 3.12 installed (for Lambda dependency packaging).

A Stripe account (API keys).

2. Configuration
Create a secrets.auto.tfvars in your environment folder:

Terraform
stripe_secret_key      = "sk_test_..."
stripe_webhook_secret  = "whsec_..."
3. Deploy
Bash
# Initialize and Apply
terraform init
terraform apply -var-file="environments/dev/secrets.auto.tfvars"
The deployment will automatically:

Provision all AWS resources.

Sync products to Stripe.

Generate a .env and product_config.json directly into the React source folder.

🔒 Security
Protected Downloads: Users cannot access S3 files directly. The /download API verifies the user's purchase in DynamoDB before generating a short-lived (15 min) signed URL.

Auth: Secure JWT validation using Cognito ID Tokens.

Webhooks: Stripe signature verification on all fulfillment events.
