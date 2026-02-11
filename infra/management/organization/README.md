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

| Name | Source | Version |
|------|--------|---------|
| <a name="module_globals"></a> [globals](#module\_globals) | ../../modules/globals | n/a |

## Resources

| Name | Type |
|------|------|
| [aws_organizations_account.dev](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/organizations_account) | resource |
| [aws_organizations_account.prod](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/organizations_account) | resource |
| [aws_organizations_organization.org](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/organizations_organization) | resource |
| [aws_organizations_organizational_unit.workloads](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/organizations_organizational_unit) | resource |

## Inputs

No inputs.

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_dev_account_id"></a> [dev\_account\_id](#output\_dev\_account\_id) | The Account ID for the Development environment |
| <a name="output_org_id"></a> [org\_id](#output\_org\_id) | The ID of the AWS Organization |
| <a name="output_prod_account_id"></a> [prod\_account\_id](#output\_prod\_account\_id) | The Account ID for the Production environment |
| <a name="output_workloads_ou_id"></a> [workloads\_ou\_id](#output\_workloads\_ou\_id) | The ID of the Workloads Organizational Unit |
<!-- END_TF_DOCS -->
