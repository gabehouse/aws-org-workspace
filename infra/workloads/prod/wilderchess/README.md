# wilderchess

<!-- BEGIN_TF_DOCS -->
## Requirements

No requirements.

## Providers

| Name | Version |
|------|---------|
| <a name="provider_aws"></a> [aws](#provider\_aws) | 6.39.0 |
| <a name="provider_terraform"></a> [terraform](#provider\_terraform) | n/a |

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_globals"></a> [globals](#module\_globals) | ../../../modules/globals | n/a |
| <a name="module_wilderchess"></a> [wilderchess](#module\_wilderchess) | ../../../modules/wilderchess | n/a |

## Resources

| Name | Type |
|------|------|
| [aws_s3_bucket.prod_app_bucket](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket) | resource |
| [aws_elastic_beanstalk_solution_stack.latest_corretto](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/elastic_beanstalk_solution_stack) | data source |
| [terraform_remote_state.network](https://registry.terraform.io/providers/hashicorp/terraform/latest/docs/data-sources/remote_state) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_app_version"></a> [app\_version](#input\_app\_version) | The version label for the application to be deployed. | `string` | n/a | yes |
| <a name="input_prod_instance_type"></a> [prod\_instance\_type](#input\_prod\_instance\_type) | The EC2 instance type to use for the production Elastic Beanstalk environment. | `string` | `"t2.micro"` | no |

## Outputs

No outputs.
<!-- END_TF_DOCS -->
