# 🏗️ AWS Cloud-Native Monorepo

[![Terraform](https://img.shields.io/badge/Terraform-1.x-623CE4?logo=terraform&style=flat-square)](https://www.terraform.io/)
[![AWS](https://img.shields.io/badge/AWS-Organization-FF9900?logo=amazon-aws&style=flat-square)](https://aws.amazon.com/)
[![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-2088FF?logo=github-actions&style=flat-square)](https://github.com/features/actions)

A production-grade **Infrastructure-as-Platform** workspace designed for high-availability service delivery. This repository manages a multi-account AWS Organization, enterprise identity federation, and a diverse suite of full-stack services ranging from serverless audio platforms to ML-driven strategy engines.

## 🏛️ Platform Architecture

### 🛰️ Infrastructure Layer (`/infra`)

The infrastructure is architected as a **3-Account Landing Zone** (Management, Dev, Prod) using AWS Organizations to enforce strict administrative, security, and billing boundaries.

- **Identity & Governance:** Implemented **IAM Identity Center (SSO)** with granular permission sets to eliminate the need for long-lived IAM user credentials and root-level access.
- **Modular Networking:** Engineered a shared networking layer featuring isolated VPCs and Security Group "blueprints" to ensure consistent, secure connectivity across account boundaries.
- **Keyless Security:** Integrated **GitHub Actions via OIDC** for short-lived, credential-less authentication, adhering to **Zero-Trust** security principles for all CI/CD pipelines.

### 📦 Service Catalog (`/services`)

This workspace utilizes a monorepo strategy to manage polyglot workloads with unified governance and deployment standards:

- **House Audio:** A serverless storefront featuring event-driven Stripe fulfillment and OIDC identity federation.
- **Wilderchess:** A high-concurrency Java 21 engine utilizing **EC2 Spot Fleets** and **ONNX** for real-time ML inference.
- **Grand River Tennis:** A full-stack booking platform built on **Amplify Gen 2**, utilizing event-driven DynamoDB streams for automated notifications.
- **Cloud Portfolio:** A hybrid-cloud professional platform leveraging **Cloudflare Global Edge** and AWS serverless primitives.
- **AcidSaturator:** A professional C++/JUCE audio plugin, integrating low-level DSP into the cloud distribution pipeline.

---

## 🛠️ Cloud Engineering Highlights

- **Infrastructure-as-Platform:** 100% of the organization’s state is managed via **Terraform**, utilizing S3 remote backends and modular structures to reduce code duplication and ensure auditability.
- **Containerized Parity:** Provisioned a **Dockerized Devcontainer** to provide a consistent, pre-configured development environment for local Terraform, AWS CLI, and Python operations.
- **Cost & Performance Optimization:** Architected **Spot Instance Fleets** for compute-heavy ML data generation, reducing infrastructure overhead by ~80% while maintaining high throughput.
- **Security Automation:** Leveraged **AWS Certificate Manager (ACM)** and automated DNS-record validation to ensure seamless SSL/TLS lifecycle management across all public-facing services.

---

## 🚀 Repository Structure

```text
.
├── infra
│   ├── management     # Root organization, SSO, and Bootstrap logic
│   ├── modules        # Reusable VPC, Security, and App modules (vstshop, wilderchess, etc.)
│   └── workloads      # Environment-specific (Dev/Prod) state deployments
└── services
    ├── AcidSaturator  # C++/JUCE Audio Plugin
    ├── cloud-portfolio# React / Amplify Gen 2 Frontend
    ├── tennis-site    # Full-stack Booking Platform (Amplify Gen 2)
    ├── vstshop-frontend# Full-stack Serverless Storefront
    └── wilderchess    # Java ML Engine & Ops
```

### 🛠️ Development Workflow

This repository is designed to be opened in a **VS Code Devcontainer**. This ensures that the Terraform version, AWS CLI, and Python environment are identical for every engineer on the project, eliminating environment drift.

- **Bootstrap:** Initializing the organization structure and root resources in `infra/management/organization`.
- **Identity:** Provisioning SSO users and granular permission sets in `infra/management/identity`.
- **Deploy:** Utilizing `validate-all.sh` and **GitHub Actions** for verified, automated deployments to target AWS accounts.
