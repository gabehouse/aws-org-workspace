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

- **Container Registry:** `wilderchess-bot-runner-dev` (**ECR**)
- **Data Lake:** `wilderchess-training-data-dev-*` (**S3**)
- **Compute:** `dev-bot-asg` (**Spot-backed Auto Scaling Group**)
<!-- BEGIN_TF_DOCS -->

## Requirements

No requirements.

## Providers

| Name                                                               | Version |
| ------------------------------------------------------------------ | ------- |
| <a name="provider_aws"></a> [aws](#provider_aws)                   | 6.34.0  |
| <a name="provider_random"></a> [random](#provider_random)          | 3.8.1   |
| <a name="provider_terraform"></a> [terraform](#provider_terraform) | n/a     |

## Modules

| Name                                                     | Source                   | Version |
| -------------------------------------------------------- | ------------------------ | ------- |
| <a name="module_globals"></a> [globals](#module_globals) | ../../../modules/globals | n/a     |

## Resources

| Name                                                                                                                                                  | Type        |
| ----------------------------------------------------------------------------------------------------------------------------------------------------- | ----------- |
| [aws_autoscaling_group.dev_bot_fleet](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/autoscaling_group)                  | resource    |
| [aws_ecr_repository.dev_bot_repo](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/ecr_repository)                         | resource    |
| [aws_iam_instance_profile.dev_bot_profile](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_instance_profile)          | resource    |
| [aws_iam_role.dev_bot_role](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role)                                     | resource    |
| [aws_iam_role_policy_attachment.dev_ecr_pull](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment) | resource    |
| [aws_iam_role_policy_attachment.dev_s3_push](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment)  | resource    |
| [aws_iam_role_policy_attachment.dev_ssm_core](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment) | resource    |
| [aws_launch_template.dev_bot_lt](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/launch_template)                         | resource    |
| [aws_s3_bucket.dev_simulation_logs](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket)                            | resource    |
| [aws_security_group.dev_bot_sg](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/security_group)                           | resource    |
| [aws_vpc_endpoint.s3](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/vpc_endpoint)                                       | resource    |
| [random_id.dev_id](https://registry.terraform.io/providers/hashicorp/random/latest/docs/resources/id)                                                 | resource    |
| [aws_prefix_list.s3](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/data-sources/prefix_list)                                      | data source |
| [terraform_remote_state.network](https://registry.terraform.io/providers/hashicorp/terraform/latest/docs/data-sources/remote_state)                   | data source |

## Inputs

No inputs.

## Outputs

| Name                                                                       | Description |
| -------------------------------------------------------------------------- | ----------- |
| <a name="output_dev_ecr_url"></a> [dev_ecr_url](#output_dev_ecr_url)       | n/a         |
| <a name="output_dev_s3_bucket"></a> [dev_s3_bucket](#output_dev_s3_bucket) | n/a         |

<!-- END_TF_DOCS -->
