# modules/elastic-beanstalk-app/main.tf

# -----------------------------------------------------------------------------
# IAM Roles and Policies
# -----------------------------------------------------------------------------
# EC2 Instance Profile and Role
# EC2 Instance Profile
resource "aws_iam_instance_profile" "eb_instance_profile" {
  name = "eb-instance-profile"
  role = aws_iam_role.eb_instance_role.name
}

# EC2 Instance Role (The application's identity)
resource "aws_iam_role" "eb_instance_role" {
  name = "eb-instance-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })
}

# 1. NEW: Inline Policy for Custom Metrics (Required for your logMetric call)
resource "aws_iam_role_policy" "eb_custom_metrics" {
  name = "eb-custom-metrics-policy"
  role = aws_iam_role.eb_instance_role.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action   = "cloudwatch:PutMetricData"
        Effect   = "Allow"
        Resource = "*"
        Condition = {
          StringEquals = {
            "cloudwatch:namespace" = "Wilderchess"
          }
        }
      }
    ]
  })
}

# 2. Standard Elastic Beanstalk Permissions
resource "aws_iam_role_policy_attachment" "web_tier" {
  role       = aws_iam_role.eb_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/AWSElasticBeanstalkWebTier"
}

# 3. Required for CloudWatch Logs and Agent
resource "aws_iam_role_policy_attachment" "cw_agent" {
  role       = aws_iam_role.eb_instance_role.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
}

# Elastic Beanstalk Service Role
resource "aws_iam_role" "eb_service_role" {
  name = "eb-service-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "elasticbeanstalk.amazonaws.com"
        }
      }
    ]
  })
}

# Attach enhanced health policy to service role
resource "aws_iam_role_policy_attachment" "enhanced_health" {
  role       = aws_iam_role.eb_service_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSElasticBeanstalkEnhancedHealth"
}

# Attach service policy to service role
resource "aws_iam_role_policy_attachment" "service" {
  role       = aws_iam_role.eb_service_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSElasticBeanstalkService"
}

# Essential for Amazon Linux 2023 engine communication
resource "aws_iam_role_policy_attachment" "managed_updates" {
  role       = aws_iam_role.eb_service_role.name
  policy_arn = "arn:aws:iam::aws:policy/AWSElasticBeanstalkManagedUpdatesCustomerRolePolicy"
}



# -----------------------------------------------------------------------------
# Elastic Beanstalk Application and Version
# -----------------------------------------------------------------------------
resource "aws_elastic_beanstalk_application" "my_app" {
  name        = var.app_name
  description = "Elastic Beanstalk application for ${var.app_name}"
}

# Add a resource to upload the application version to S3
resource "aws_s3_object" "app_version" {
  bucket = var.s3_bucket_name
  # Use the dynamic version for the S3 Key
  key    = "versions/app-${var.app_version}.zip"
  source = "${path.root}/../../../../services/wilderchess/app-${var.app_version}.zip"
}

resource "aws_elastic_beanstalk_application_version" "my_app_version" {
  # This label must be unique for every deployment
  name        = "release-${var.app_version}"
  application = aws_elastic_beanstalk_application.my_app.name
  bucket      = aws_s3_object.app_version.bucket
  key         = aws_s3_object.app_version.key
}

# -----------------------------------------------------------------------------
# Security Groups
# -----------------------------------------------------------------------------
# Security group for the Elastic Beanstalk load balancer
resource "aws_security_group" "eb_lb_sg" {
  name        = "${var.app_name}-lb-sg"
  description = "Security group for Elastic Beanstalk load balancer"
  vpc_id      = var.vpc_id

  # Allow inbound traffic from anywhere on ports 80 and 443
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  # Allow outbound DNS resolution
  egress {
    from_port = 53
    to_port   = 53
    protocol  = "udp"
    #trivy:ignore:aws-0104
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow DNS resolution"
  }

  egress {
    from_port = 53
    to_port   = 53
    protocol  = "tcp"
    #trivy:ignore:aws-0104
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow DNS resolution"
  }

  # Allow outbound HTTP/HTTPS for updates and CloudWatch/S3 APIs
  egress {
    from_port = 80
    to_port   = 80
    protocol  = "tcp"
    #trivy:ignore:aws-0104
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow HTTP for OS updates"
  }

  egress {
    from_port = 443
    to_port   = 443
    protocol  = "tcp"
    #trivy:ignore:aws-0104
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow HTTPS for AWS APIs"
  }
}

# Security group for the EC2 instances
resource "aws_security_group" "eb_instance_sg" {
  name        = "${var.app_name}-instance-sg"
  description = "Security group for Elastic Beanstalk instances"
  vpc_id      = var.vpc_id

  # Allow inbound traffic on port 5000 from the load balancer's security group
  ingress {
    from_port       = 5000
    to_port         = 5000
    protocol        = "tcp"
    security_groups = [aws_security_group.eb_lb_sg.id]
  }



  # Allow outbound DNS resolution
  egress {
    from_port = 53
    to_port   = 53
    protocol  = "udp"
    #trivy:ignore:aws-0104
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow DNS resolution"
  }

  egress {
    from_port = 53
    to_port   = 53
    protocol  = "tcp"
    #trivy:ignore:aws-0104
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow DNS resolution"
  }

  # Allow outbound HTTP/HTTPS for updates and CloudWatch/S3 APIs
  egress {
    from_port = 80
    to_port   = 80
    protocol  = "tcp"
    #trivy:ignore:aws-0104
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow HTTP for OS updates"
  }

  egress {
    from_port = 443
    to_port   = 443
    protocol  = "tcp"
    #trivy:ignore:aws-0104
    cidr_blocks = ["0.0.0.0/0"]
    description = "Allow HTTPS for AWS APIs"
  }
}

# -----------------------------------------------------------------------------
# Elastic Beanstalk Environment
# -----------------------------------------------------------------------------
resource "aws_elastic_beanstalk_environment" "my_env" {
  name                = var.env_name
  application         = aws_elastic_beanstalk_application.my_app.name
  solution_stack_name = var.solution_stack_name
  version_label       = aws_elastic_beanstalk_application_version.my_app_version.name

  # Ensure the environment is not created before the application version and security groups exist
  depends_on = [
    aws_elastic_beanstalk_application_version.my_app_version,
    aws_security_group.eb_lb_sg,
    aws_security_group.eb_instance_sg,
    aws_iam_instance_profile.eb_instance_profile,
    aws_iam_role.eb_service_role
  ]

  # Add this to your aws_elastic_beanstalk_environment resource
  setting {
    namespace = "aws:elbv2:loadbalancer"
    name      = "IdleTimeout"
    value     = "3600" # 1 hour
  }

  setting {
    namespace = "aws:ec2:vpc"
    name      = "VPCId"
    value     = var.vpc_id
  }

  setting {
    namespace = "aws:ec2:vpc"
    name      = "Subnets"
    value     = join(",", var.public_subnet_ids)
  }

  setting {
    namespace = "aws:ec2:vpc"
    name      = "AssociatePublicIpAddress"
    value     = "true"
  }

  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name      = "InstanceType"
    value     = var.instance_type
  }

  setting {
    namespace = "aws:elasticbeanstalk:environment"
    name      = "ServiceRole"
    value     = aws_iam_role.eb_service_role.name
  }

  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name      = "IamInstanceProfile"
    value     = aws_iam_instance_profile.eb_instance_profile.name
  }

  # Associate security groups
  setting {
    namespace = "aws:autoscaling:launchconfiguration"
    name      = "SecurityGroups"
    value     = aws_security_group.eb_instance_sg.id
  }

  setting {
    namespace = "aws:elb:loadbalancer"
    name      = "SecurityGroups"
    value     = aws_security_group.eb_lb_sg.id
  }

  # Set application environment variables for the port
  setting {
    namespace = "aws:elasticbeanstalk:application:environment"
    name      = "SERVER_PORT"
    value     = "5000"
  }

  setting {
    namespace = "aws:elasticbeanstalk:application:environment"
    name      = "PORT"
    value     = "5000"
  }

  setting {
    namespace = "aws:elasticbeanstalk:environment"
    name      = "EnvironmentType"
    value     = "LoadBalanced"
  }

  setting {
    namespace = "aws:elasticbeanstalk:environment"
    name      = "LoadBalancerType"
    value     = "application"
  }

  # Specify the process port and protocol for health checks
  setting {
    namespace = "aws:elasticbeanstalk:environment:process:default"
    name      = "Port"
    value     = "5000"
  }

  setting {
    namespace = "aws:elasticbeanstalk:environment:process:default"
    name      = "Protocol"
    value     = "HTTP"
  }

  tags = {
    Name = var.app_name
  }
}

# -----------------------------------------------------------------------------
# Output
# -----------------------------------------------------------------------------
output "elastic_beanstalk_url" {
  value = aws_elastic_beanstalk_environment.my_env.cname
}
