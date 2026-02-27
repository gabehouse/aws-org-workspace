# VSTShop Production Infrastructure 🚀

This directory manages the production-grade deployment of the VSTShop frontend. It utilizes a centralized module architecture to ensure consistency while enforcing strict production guardrails.

## 🏗️ Architecture Overview
This workload deploys a globally distributed static website using:
* **Amazon S3:** Origin storage for the frontend assets.
* **Amazon CloudFront:** Content Delivery Network (CDN) for low-latency delivery and HTTPS.
* **OAC (Origin Access Control):** Restricts S3 access to CloudFront only.



---

## 🔒 Production Guardrails
Unlike the development environment, this workload is protected by the following:
* **Manual Approval Gate:** Deployments are paused until a manual review is performed in GitHub Actions.
* **Environment Isolation:** Deployed into a dedicated Production AWS account.
* **Least Privilege:** Uses OIDC role-chaining to assume a `terraform-execution-role-prod` specifically scoped to these resources.

---

## 🚀 Deployment Workflow
1.  **Pull Request:** A PR is opened from `dev` to `master` (or `main`).
2.  **Continuous Integration:** GitHub Actions runs a `terraform plan` and posts the output to the PR comment for review.
3.  **Merge & Approval:** Upon merging, the `Production Apply` job is triggered but held for **Manual Approval**.
4.  **Continuous Deployment:** Once approved, Terraform applies changes and invalidates the CloudFront cache.

---

## 📋 Terraform Documentation
(Run `terraform-docs` to generate this section automatically)
---

## 🛠️ Maintenance
To run Terraform locally for debugging (not recommended for deployment):
1.  Ensure you have the correct AWS credentials for the **Prod** account.
2.  Run `terraform init -backend-config=../../../backend.ci.hcl`.
3.  Run `terraform plan`.



<!-- BEGIN_TF_DOCS -->
## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.5 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | ~> 6.0 |
| <a name="requirement_stripe"></a> [stripe](#requirement\_stripe) | 0.1.3 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 6.33.0 |
| <a name="provider_aws.us_east_1"></a> [aws.us\_east\_1](#provider\_aws.us\_east\_1) | 6.33.0 |
| <a name="provider_local"></a> [local](#provider\_local) | 2.7.0 |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_auth"></a> [auth](#module\_auth) | ../../../modules/vstshop/auth | n/a |
| <a name="module_backend"></a> [backend](#module\_backend) | ../../../modules/vstshop/backend | n/a |
| <a name="module_database"></a> [database](#module\_database) | ../../../modules/vstshop/database | n/a |
| <a name="module_frontend"></a> [frontend](#module\_frontend) | ../../../modules/vstshop/frontend | n/a |
| <a name="module_globals"></a> [globals](#module\_globals) | ../../../modules/globals | n/a |
| <a name="module_storage"></a> [storage](#module\_storage) | ../../../modules/vstshop/storage | n/a |

## Resources

| Name | Type |
|------|------|
| [aws_acm_certificate.cert](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/acm_certificate) | resource |
| [aws_acm_certificate_validation.cert_verify](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/acm_certificate_validation) | resource |
| [aws_route53_record.cert_validation](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record) | resource |
| [aws_route53_record.www](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/route53_record) | resource |
| [local_file.env_file](https://registry.terraform.io/providers/hashicorp/local/latest/docs/resources/file) | resource |
| [aws_route53_zone.main](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/route53_zone) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_environment"></a> [environment](#input\_environment) | n/a | `string` | `"prod"` | no |
| <a name="input_project_name"></a> [project\_name](#input\_project\_name) | n/a | `string` | `"vstshop"` | no |
| <a name="input_stripe_secret_key"></a> [stripe\_secret\_key](#input\_stripe\_secret\_key) | n/a | `string` | n/a | yes |
| <a name="input_stripe_webhook_secret"></a> [stripe\_webhook\_secret](#input\_stripe\_webhook\_secret) | n/a | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_api_url"></a> [api\_url](#output\_api\_url) | n/a |
| <a name="output_auth_config"></a> [auth\_config](#output\_auth\_config) | n/a |
| <a name="output_cloudfront_domain_name"></a> [cloudfront\_domain\_name](#output\_cloudfront\_domain\_name) | n/a |
| <a name="output_cloudfront_hosted_zone_id"></a> [cloudfront\_hosted\_zone\_id](#output\_cloudfront\_hosted\_zone\_id) | n/a |
| <a name="output_distribution_id"></a> [distribution\_id](#output\_distribution\_id) | n/a |
| <a name="output_s3_bucket_name"></a> [s3\_bucket\_name](#output\_s3\_bucket\_name) | n/a |
| <a name="output_website_url"></a> [website\_url](#output\_website\_url) | The CloudFront URL to access your shop |
<!-- END_TF_DOCS -->
