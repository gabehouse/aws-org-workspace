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
