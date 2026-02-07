# bootstrap

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
| [aws_dynamodb_resource_policy.lock_table_policy](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/dynamodb_resource_policy) | resource |
| [aws_dynamodb_table.terraform_locks](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/dynamodb_table) | resource |
| [aws_s3_bucket.terraform_state](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket) | resource |
| [aws_s3_bucket_policy.state_cross_account](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_policy) | resource |
| [aws_s3_bucket_public_access_block.terraform_state](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_public_access_block) | resource |
| [aws_s3_bucket_server_side_encryption_configuration.state_encryption](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_server_side_encryption_configuration) | resource |
| [aws_s3_bucket_versioning.terraform_state](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket_versioning) | resource |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_aws_region"></a> [aws\_region](#input\_aws\_region) | n/a | `string` | `"us-east-2"` | no |
| <a name="input_dev_account_id"></a> [dev\_account\_id](#input\_dev\_account\_id) | n/a | `string` | n/a | yes |
| <a name="input_dynamodb_table_name"></a> [dynamodb\_table\_name](#input\_dynamodb\_table\_name) | n/a | `string` | `"terraform-state-lock"` | no |
| <a name="input_mgmt_account_id"></a> [mgmt\_account\_id](#input\_mgmt\_account\_id) | These are just to shut up the auto.tfvars warnings | `string` | n/a | yes |
| <a name="input_prod_account_id"></a> [prod\_account\_id](#input\_prod\_account\_id) | n/a | `string` | n/a | yes |
| <a name="input_region"></a> [region](#input\_region) | n/a | `string` | n/a | yes |
| <a name="input_state_bucket_name"></a> [state\_bucket\_name](#input\_state\_bucket\_name) | n/a | `string` | `"gabriel-tf-state-2026"` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_dynamodb_table_name"></a> [dynamodb\_table\_name](#output\_dynamodb\_table\_name) | The name of the DynamoDB table for locking |
| <a name="output_state_bucket_arn"></a> [state\_bucket\_arn](#output\_state\_bucket\_arn) | The ARN of the S3 bucket for state |
| <a name="output_state_bucket_name"></a> [state\_bucket\_name](#output\_state\_bucket\_name) | The name of the S3 bucket for state |
<!-- END_TF_DOCS -->
