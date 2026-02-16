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

| Name | Version |
|------|---------|
| <a name="provider_local"></a> [local](#provider\_local) | 2.6.2 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_globals"></a> [globals](#module\_globals) | ../../../modules/globals | n/a |
| <a name="module_vstshop_auth"></a> [vstshop\_auth](#module\_vstshop\_auth) | ../../../modules/vstshop-auth | n/a |
| <a name="module_vstshop_backend"></a> [vstshop\_backend](#module\_vstshop\_backend) | ../../../modules/vstshop-backend | n/a |
| <a name="module_vstshop_frontend"></a> [vstshop\_frontend](#module\_vstshop\_frontend) | ../../../modules/vstshop-frontend | n/a |

## Resources

| Name | Type |
|------|------|
| [local_file.env_file](https://registry.terraform.io/providers/hashicorp/local/latest/docs/resources/file) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_env"></a> [env](#input\_env) | n/a | `string` | `"dev"` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_api_url"></a> [api\_url](#output\_api\_url) | n/a |
| <a name="output_auth_config"></a> [auth\_config](#output\_auth\_config) | n/a |
| <a name="output_distribution_id"></a> [distribution\_id](#output\_distribution\_id) | n/a |
| <a name="output_s3_bucket_name"></a> [s3\_bucket\_name](#output\_s3\_bucket\_name) | n/a |
| <a name="output_website_url"></a> [website\_url](#output\_website\_url) | The CloudFront URL to access your shop |
<!-- END_TF_DOCS -->
