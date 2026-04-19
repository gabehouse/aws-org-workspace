# 🤖 Wilderchess Bot Factory (Dev Workload)

This module provisions a high-throughput, cost-optimized simulation environment for the Wilderchess ML engine. It is designed to spin up transient compute clusters that generate game-state logs and CSV training data, which are then offloaded to a central data lake for model training.

## 🏗️ Architectural Overview

### 💰 Cost-Optimized Compute (Spot Fleet)

- **Spot Allocation:** Utilizes a **Mixed Instances Policy** with a `capacity-optimized` strategy. This allows the cluster to scale up to 30 nodes using `t3.small`, `t3.medium`, and `t2.small` instances, maximizing availability while reducing compute costs by **~80%**.
- **Self-Healing:** Managed via an **Auto Scaling Group (ASG)** with a `desired_capacity` of 0 by default, allowing for manual or event-driven "simulation bursts."

### 🔐 Security & Networking

- **Zero-Trust Identity:** Bot instances utilize an **IAM Instance Profile** with strictly scoped access to ECR (Pull-only) and S3 (Push-only).
- **Keyless Management:** Includes `AmazonSSMManagedInstanceCore`, enabling secure browser-based terminal access via **AWS Systems Manager (SSM)**, eliminating the need for managed SSH keys or open port 22.
- **Network Isolation:** Traffic to S3 is routed through a **VPC Gateway Endpoint**, ensuring data transit remains within the AWS backbone. This increases security while eliminating NAT Gateway data processing charges.

### 🛠️ Data Pipeline & Reliability

- **Disk Space Protection:** The `user_data` script configures `journald` and Docker `log-opts` to strictly limit on-disk log size, preventing "No space left on device" crashes during high-volume simulations.
- **Automated Offboarding:** A cron-driven **S3 Sync Pipeline** moves raw JSON logs and staging CSVs from the local instance to the data lake every minute. This ensures data persistence even in the event of a Spot Instance interruption.

---

## 🚀 Deployment

### Prerequisites

- **Networking State:** Must be initialized and deployed in `infra/workloads/dev/networking`.
- **IAM Permissions:** Valid credentials with authority to manage ASG, Launch Templates, and IAM Roles.

### Execution

Bash

```bash
terraform init
terraform plan
terraform apply
```

**Scaling the Factory**
To trigger a training data generation run of 10 nodes:

Bash

```bash
aws autoscaling update-auto-scaling-group \
  --auto-scaling-group-name dev-bot-asg \
  --desired-capacity 10
```

## 📊 Infrastructure Components

* **Container Registry:** `wilderchess-bot-runner-dev` (**ECR**)
* **Data Lake:** `wilderchess-training-data-dev-*` (**S3**)
* **Compute:** `dev-bot-asg` (**Spot-backed Auto Scaling Group**)

<!-- BEGIN_TF_DOCS -->

## Requirements

No requirements.

## Providers

| Name                                             | Version |
| ------------------------------------------------ | ------- |
| <a name="provider_aws"></a> [aws](#provider_aws) | 6.34.0  |

## Modules

No modules.

## Resources

| Name                                                                                                                                                                          | Type     |
| ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | -------- |
| [aws_elastic_beanstalk_application.my_app](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/elastic_beanstalk_application)                         | resource |
| [aws_elastic_beanstalk_application_version.my_app_version](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/elastic_beanstalk_application_version) | resource |
| [aws_elastic_beanstalk_environment.my_env](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/elastic_beanstalk_environment)                         | resource |
| [aws_iam_instance_profile.eb_instance_profile](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_instance_profile)                              | resource |
| [aws_iam_role.eb_instance_role](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role)                                                         | resource |
| [aws_iam_role.eb_service_role](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role)                                                          | resource |
| [aws_iam_role_policy.eb_custom_metrics](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy)                                          | resource |
| [aws_iam_role_policy_attachment.cw_agent](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment)                             | resource |
| [aws_iam_role_policy_attachment.enhanced_health](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment)                      | resource |
| [aws_iam_role_policy_attachment.managed_updates](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment)                      | resource |
| [aws_iam_role_policy_attachment.service](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment)                              | resource |
| [aws_iam_role_policy_attachment.web_tier](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment)                             | resource |
| [aws_s3_object.app_version](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_object)                                                            | resource |
| [aws_security_group.eb_instance_sg](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/security_group)                                               | resource |
| [aws_security_group.eb_lb_sg](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/security_group)                                                     | resource |

## Inputs

| Name                                                                                       | Description                                                       | Type           | Default | Required |
| ------------------------------------------------------------------------------------------ | ----------------------------------------------------------------- | -------------- | ------- | :------: |
| <a name="input_app_name"></a> [app_name](#input_app_name)                                  | The name of the Elastic Beanstalk application.                    | `string`       | n/a     |   yes    |
| <a name="input_app_version"></a> [app_version](#input_app_version)                         | Automatically generated version string from deploy script         | `string`       | n/a     |   yes    |
| <a name="input_env_name"></a> [env_name](#input_env_name)                                  | The name of the Elastic Beanstalk environment.                    | `string`       | n/a     |   yes    |
| <a name="input_env_vars"></a> [env_vars](#input_env_vars)                                  | A map of environment variables to be set on the instances.        | `map(string)`  | `{}`    |    no    |
| <a name="input_instance_type"></a> [instance_type](#input_instance_type)                   | The EC2 instance type for the environment.                        | `string`       | n/a     |   yes    |
| <a name="input_private_subnet_ids"></a> [private_subnet_ids](#input_private_subnet_ids)    | A list of private subnet IDs for the environment.                 | `list(string)` | n/a     |   yes    |
| <a name="input_public_subnet_ids"></a> [public_subnet_ids](#input_public_subnet_ids)       | A list of public subnet IDs for the environment.                  | `list(string)` | n/a     |   yes    |
| <a name="input_s3_bucket_name"></a> [s3_bucket_name](#input_s3_bucket_name)                | The name of the S3 bucket where the application source is stored. | `string`       | n/a     |   yes    |
| <a name="input_solution_stack_name"></a> [solution_stack_name](#input_solution_stack_name) | The solution stack for the environment, e.g., Corretto 11.        | `string`       | n/a     |   yes    |
| <a name="input_source_path"></a> [source_path](#input_source_path)                         | The path to the application source file (.jar or .war).           | `string`       | n/a     |   yes    |
| <a name="input_version_label"></a> [version_label](#input_version_label)                   | The label for the application version to deploy.                  | `string`       | n/a     |   yes    |
| <a name="input_vpc_id"></a> [vpc_id](#input_vpc_id)                                        | The ID of the VPC where the resources will be created.            | `string`       | n/a     |   yes    |

## Outputs

| Name                                                                                               | Description                                                                                                                                                        |
| -------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| <a name="output_elastic_beanstalk_url"></a> [elastic_beanstalk_url](#output_elastic_beanstalk_url) | ----------------------------------------------------------------------------- Output ----------------------------------------------------------------------------- |

<!-- END_TF_DOCS -->
