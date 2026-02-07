# iam

<!-- BEGIN_TF_DOCS -->
## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.5.0 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | ~> 6.0 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 6.30.0 |

## Modules

No modules.

## Resources

| Name | Type |
|------|------|
| [aws_iam_role.terraform_execution](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role) | resource |
| [aws_iam_role_policy_attachment.admin](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_dev_account_id"></a> [dev\_account\_id](#input\_dev\_account\_id) | n/a | `string` | n/a | yes |
| <a name="input_dev_account_root_arn"></a> [dev\_account\_root\_arn](#input\_dev\_account\_root\_arn) | n/a | `string` | n/a | yes |
| <a name="input_github_actions_oidc_execution_role_arn"></a> [github\_actions\_oidc\_execution\_role\_arn](#input\_github\_actions\_oidc\_execution\_role\_arn) | The ARN of the execution role in the Dev account | `string` | n/a | yes |
| <a name="input_mgmt_account_id"></a> [mgmt\_account\_id](#input\_mgmt\_account\_id) | n/a | `string` | n/a | yes |
| <a name="input_prod_account_id"></a> [prod\_account\_id](#input\_prod\_account\_id) | n/a | `string` | n/a | yes |
| <a name="input_region"></a> [region](#input\_region) | AWS Region | `string` | `"us-east-2"` | no |

## Outputs

No outputs.
<!-- END_TF_DOCS -->
