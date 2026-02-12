# VSTShop Development Infrastructure 🛠️

This directory manages the development (sandbox) environment for the VSTShop frontend. It is designed for rapid iteration, feature testing, and automated previews.

## 🏗️ Environment Purpose
The Dev environment serves as the primary integration point for new features. It mirrors the Production architecture but is optimized for speed and visibility rather than strict gatekeeping.

* **Rapid Feedback:** Infrastructure changes are planned on every feature branch push.
* **Automated Sync:** The frontend is automatically deployed to S3 upon merging to the `dev` branch.
* **Cache Optimization:** CloudFront TTLs (Time-to-Live) are typically shorter here to allow developers to see changes faster.



---

## 🚀 Deployment Workflow
1.  **Feature Push:** Pushing to any `feature/**` branch triggers an automated `terraform plan` to validate infrastructure code.
2.  **Pull Request:** Opening a PR to the `dev` branch posts a visual plan comment, allowing for peer review of infra changes.
3.  **Continuous Deployment:** Merging into `dev` triggers an immediate `terraform apply` and S3 sync. No manual approval is required for this environment, favoring developer velocity.

---

## 🔒 Security & Isolation
* **Account Separation:** Hosted in a dedicated Development AWS account to prevent accidental interference with Production data.
* **OIDC Auth:** Uses GitHub Actions OIDC to assume the `terraform-execution-role-dev`, ensuring no long-lived AWS keys are stored in GitHub.

---

## 📋 Terraform Documentation
(Run `terraform-docs` to populate this section)
---

## 🛠️ Local Development
To test infrastructure changes locally:
1.  Initialize using the CI-specific backend config:
    `terraform init -backend-config=../../../backend.ci.hcl`
2.  Target the dev workspace/
<!-- BEGIN_TF_DOCS -->
## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.5 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | ~> 6.0 |

## Providers

No providers.

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_globals"></a> [globals](#module\_globals) | ../../../modules/globals | n/a |
| <a name="module_vstshop"></a> [vstshop](#module\_vstshop) | ../../../modules/vstshop-frontend | n/a |

## Resources

No resources.

## Inputs

No inputs.

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_distribution_id"></a> [distribution\_id](#output\_distribution\_id) | n/a |
| <a name="output_s3_bucket_name"></a> [s3\_bucket\_name](#output\_s3\_bucket\_name) | n/a |
| <a name="output_website_url"></a> [website\_url](#output\_website\_url) | The CloudFront URL to access your shop |
<!-- END_TF_DOCS -->
