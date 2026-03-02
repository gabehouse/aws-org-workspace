module "globals" {
  source = "../../../modules/globals"
}


# 1. Access the Networking State
data "terraform_remote_state" "network" {
  backend = "s3"
  config = {
    bucket = "gabriel-tf-state-2026"
    key    = "workloads/dev/networking/terraform.tfstate"
    region = "us-east-2"
  }
}

locals {
  vpc_id        = data.terraform_remote_state.network.outputs.vpc_id
  public_rt_ids = data.terraform_remote_state.network.outputs.public_route_table_ids
  subnet_id     = data.terraform_remote_state.network.outputs.public_subnet_ids[0]
}

# Look up the S3 IP ranges for your region
data "aws_prefix_list" "s3" {
  name = "com.amazonaws.us-east-2.s3"
}

# Create the Private Tunnel to S3
resource "aws_vpc_endpoint" "s3" {
  vpc_id       = local.vpc_id
  service_name = "com.amazonaws.us-east-2.s3"
  # This attaches the endpoint to your existing route tables
  route_table_ids = local.public_rt_ids
}

# -----------------------------------------------------------------------------
# The Bot Factory Artifacts (ECR and S3)
# -----------------------------------------------------------------------------
resource "aws_ecr_repository" "dev_bot_repo" {
  name                 = "wilderchess-bot-runner-dev"
  image_tag_mutability = "MUTABLE"
  force_delete         = true
}

resource "aws_s3_bucket" "dev_simulation_logs" {
  bucket = "wilderchess-training-data-dev-${random_id.dev_id.hex}"
}

resource "random_id" "dev_id" {
  byte_length = 4
}

# -----------------------------------------------------------------------------
# IAM & Security Groups (With Access Fixes)
# -----------------------------------------------------------------------------
resource "aws_iam_role" "dev_bot_role" {
  name = "wilderchess_dev_bot_role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy_attachment" "dev_s3_push" {
  role       = aws_iam_role.dev_bot_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonS3FullAccess"
}

resource "aws_iam_role_policy_attachment" "dev_ecr_pull" {
  role       = aws_iam_role.dev_bot_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

# ESSENTIAL: Required for Browser-based "Session Manager" access
resource "aws_iam_role_policy_attachment" "dev_ssm_core" {
  role       = aws_iam_role.dev_bot_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_instance_profile" "dev_bot_profile" {
  name = "wilderchess_dev_bot_profile"
  role = aws_iam_role.dev_bot_role.name
}

resource "aws_security_group" "dev_bot_sg" {
  name   = "dev-bot-sg"
  vpc_id = local.vpc_id

  # ESSENTIAL: Allow SSH traffic
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "Allow HTTPS for Yum, Docker, and S3"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    # trivy:ignore:AVD-AWS-0104
    # tfsec:ignore:aws-vpc-no-public-egress-sgr
    cidr_blocks = ["0.0.0.0/0"]
  }
}

# -----------------------------------------------------------------------------
# Spot Fleet Auto Scaling Group
# -----------------------------------------------------------------------------
resource "aws_launch_template" "dev_bot_lt" {
  name_prefix   = "dev-bot-factory-"
  image_id      = "ami-0900fe555666598a2" # Amazon Linux 2023
  instance_type = "t3.medium"

  iam_instance_profile {
    name = aws_iam_instance_profile.dev_bot_profile.name
  }

  # FIX: 30GB storage prevents the "No space left" crash
  block_device_mappings {
    device_name = "/dev/xvda"
    ebs {
      volume_size           = 30
      volume_type           = "gp3"
      delete_on_termination = true
    }
  }

  network_interfaces {
    associate_public_ip_address = true
    security_groups             = [aws_security_group.dev_bot_sg.id]
  }

  user_data = base64encode(<<-EOF
              #!/bin/bash
              yum update -y
              yum install -y docker cronie

              # Start and enable Docker
              systemctl start docker
              systemctl enable docker

              # Start and enable Crond
              systemctl start crond
              systemctl enable crond

              # --- DISK SPACE PROTECTION: CAP SYSTEM LOGS ---
              # This stops the journal from eating the disk when errors occur
              echo "SystemMaxUse=100M" >> /etc/systemd/journald.conf
              echo "RuntimeMaxUse=50M" >> /etc/systemd/journald.conf
              systemctl restart systemd-journald
              journalctl --vacuum-size=100M

              REGISTRY_URL=$(echo "${aws_ecr_repository.dev_bot_repo.repository_url}" | cut -d'/' -f1)
              aws ecr get-login-password --region us-east-2 | docker login --username AWS --password-stdin $REGISTRY_URL

              docker pull ${aws_ecr_repository.dev_bot_repo.repository_url}:latest

              # Pull and Run the Bot
              docker pull ${aws_ecr_repository.dev_bot_repo.repository_url}:latest

              mkdir -p /home/ec2-user/gamelogs

              # Run container with log rotation and volume mount
              docker run -d \
                --name bot-runner \
                --log-opt max-size=5m \
                --log-opt max-file=2 \
                -v /home/ec2-user/gamelogs:/app/gamelogs \
                ${aws_ecr_repository.dev_bot_repo.repository_url}:latest

              # DRAIN LOGS: Ensure the directory exists, then write the cron job
              mkdir -p /etc/cron.d
              echo "* * * * * root /usr/bin/aws s3 mv /home/ec2-user/gamelogs s3://${aws_s3_bucket.dev_simulation_logs.id}/raw/ --recursive" > /etc/cron.d/s3_sync
              chmod 644 /etc/cron.d/s3_sync
              EOF
  )
}

resource "aws_autoscaling_group" "dev_bot_fleet" {
  name                = "dev-bot-asg"
  desired_capacity    = 0
  max_size            = 10
  min_size            = 0
  vpc_zone_identifier = [local.subnet_id]

  mixed_instances_policy {
    instances_distribution {
      on_demand_base_capacity                  = 0
      on_demand_percentage_above_base_capacity = 0
      spot_allocation_strategy                 = "lowest-price"
    }
    launch_template {
      launch_template_specification {
        launch_template_id = aws_launch_template.dev_bot_lt.id
        version            = "$Latest"
      }
    }
  }
}

output "dev_ecr_url" { value = aws_ecr_repository.dev_bot_repo.repository_url }
output "dev_s3_bucket" { value = aws_s3_bucket.dev_simulation_logs.id }
