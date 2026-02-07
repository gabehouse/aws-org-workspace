# organization

<!-- BEGIN_TF_DOCS -->
## Requirements

| Name | Version |
|------|---------|
| <a name="requirement_terraform"></a> [terraform](#requirement\_terraform) | >= 1.5 |
| <a name="requirement_aws"></a> [aws](#requirement\_aws) | ~> 6.0 |

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 6.28.0 |

## Modules

No modules.

## Resources

| Name | Type |
|------|------|
| [aws_organizations_account.dev](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/organizations_account) | resource |
| [aws_organizations_account.prod](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/organizations_account) | resource |
| [aws_organizations_organization.org](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/organizations_organization) | resource |
| [aws_organizations_organizational_unit.workloads](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/organizations_organizational_unit) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_dev_account_id"></a> [dev\_account\_id](#input\_dev\_account\_id) | n/a | `string` | n/a | yes |
| <a name="input_dev_email"></a> [dev\_email](#input\_dev\_email) | n/a | `string` | n/a | yes |
| <a name="input_mgmt_account_id"></a> [mgmt\_account\_id](#input\_mgmt\_account\_id) | n/a | `string` | n/a | yes |
| <a name="input_prod_account_id"></a> [prod\_account\_id](#input\_prod\_account\_id) | n/a | `string` | n/a | yes |
| <a name="input_prod_email"></a> [prod\_email](#input\_prod\_email) | n/a | `string` | n/a | yes |
| <a name="input_region"></a> [region](#input\_region) | n/a | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_dev_account_id"></a> [dev\_account\_id](#output\_dev\_account\_id) | The Account ID for the Development environment |
| <a name="output_org_id"></a> [org\_id](#output\_org\_id) | The ID of the AWS Organization |
| <a name="output_prod_account_id"></a> [prod\_account\_id](#output\_prod\_account\_id) | The Account ID for the Production environment |
| <a name="output_workloads_ou_id"></a> [workloads\_ou\_id](#output\_workloads\_ou\_id) | The ID of the Workloads Organizational Unit |
<!-- END_TF_DOCS -->
