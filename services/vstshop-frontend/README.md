# 🎹 VST Shop Engine (Full-Stack)

![React](https://img.shields.io/badge/React-20232A?logo=react&style=flat-square) ![Vite](https://img.shields.io/badge/Vite-646CFF?logo=vite&style=flat-square&logoColor=white) ![AWS](https://img.shields.io/badge/AWS-Serverless-232F3E?logo=amazon-aws&style=flat-square) ![Terraform](https://img.shields.io/badge/Terraform-1.x-623CE4?logo=terraform&style=flat-square) ![Stripe](https://img.shields.io/badge/Stripe-v3-635BFF?logo=stripe&style=flat-square)

A modern, high-performance template for selling digital assets (VSTs, samples, or software). Powered by a **React + Vite** frontend and a **fully serverless AWS architecture** managed through an enterprise-grade Multi-Account Terraform structure.

## 🚀 Architecture Overview

This project leverages **Infrastructure as Code (IaC)** to enforce a **Zero-Trust** environment across isolated AWS accounts.

- **Frontend:** React (Vite) distributed via **AWS CloudFront** and **S3** for global low-latency delivery.
- **API Strategy:** Regional **API Gateway** with **Cognito User Pool** authorization for secure endpoint access.
- **Compute:** **Python 3.12 Lambdas** architected as independent microservices.
- **Database:** **DynamoDB** providing atomic purchase tracking and user entitlement mapping.
- **Fulfillment:** **Stripe API** integration with automated webhook verification and product/price synchronization.
- **Secure Storage:** Private S3 buckets utilizing **Lambda-generated Presigned URLs** to prevent direct asset exposure.

<p align="center">
  <a href="../cloud-portfolio/public/assets/diagram-vstshop.svg" target="_blank">
    <img src="../cloud-portfolio/public/assets/diagram-vstshop.svg" width="800" alt="VST Shop Architecture - Click to Expand">
  </a>
  <br>
  <em>(Click diagram to view full resolution)</em>
</p>

---

## 🛠️ Tech Stack & Dependencies

### **Enterprise Infrastructure (Terraform)**

- **Multi-Account Orchestration:** Utilizes **AWS Organizations** to separate `Management`, `Dev`, and `Prod` workloads.
- **Automated Lifecycle:** **GitHub Actions** CI/CD utilizing **OIDC** for keyless, secure deployments.
- **Lambda Layers:** Automated Python dependency packaging for high-performance execution.
- **Least-Privilege IAM:** Granular, resource-specific roles to minimize the security attack surface.

### **Frontend & DX**

- **Vite:** For near-instant Hot Module Replacement (HMR) and optimized builds.
- **AWS Amplify SDK:** Seamless **Cognito Auth** and session management integration.
- **Hard-Link Syncing:** `setup_config.sh` ensures a **Single Source of Truth** by syncing `config.yaml` across frontend and backend runtimes.

---

## 📂 Project Structure

```text
.
├── .github/workflows/      # CI/CD Pipeline (GitHub Actions)
├── config.yaml             # Single source of truth for VST catalog & constants
├── infra/                  # Enterprise-grade Multi-Account Terraform
│   ├── management/         # Identity (OIDC/GitHub Actions) & Organizations
│   ├── modules/            # Reusable business logic (Auth, API, Storage, DB)
│   └── workloads/          # Environment-level instances (Dev vs. Production)
├── services/
│   └── vstshop-frontend/   # React + Vite application source code
└── validate-all.sh         # Global security, linting, and integrity checks
```

---

## 🔒 Security & Performance

- **Protected Downloads:** Assets are strictly private. The `/download` API performs a **DynamoDB Entitlement Check** before issuing a **15-minute S3 Presigned URL**.
- **Signature Verification:** All Stripe fulfillment events are validated via **HmacSHA256 signatures** to prevent spoofing.
- **State Persistence:** Infrastructure state is managed via **Remote S3 Backends** with **DynamoDB State Locking** to prevent concurrent deployment conflicts.
- **Keyless Auth:** Zero permanent AWS keys stored in GitHub; all deployment runners utilize short-lived session tokens via **OIDC**.

---

## ⚡ Deployment Workflow

### **1. Automated CI/CD (Recommended)**

Pushing to the `master` branch triggers a **GitHub Actions** workflow that:

1. **Validates** Terraform formatting and security policies.
2. **Synchronizes** the local VST catalog with the **Stripe Dashboard**.
3. **Deploys** the serverless stack and invalidates the **CloudFront** edge cache.

### **2. Manual Handshake (Initial Setup)**

For the first deployment, a "handshake" is required to link **Stripe Webhooks**:

1. Run `terraform apply` in the desired workload folder (e.g., `infra/workloads/dev`).
2. Retrieve the `api_url` from the Terraform outputs.
3. Configure a **Stripe Webhook** in your dashboard pointing to `${api_url}/webhook`.
4. Update the `stripe_webhook_secret` in your `secrets.auto.tfvars` and re-apply.
