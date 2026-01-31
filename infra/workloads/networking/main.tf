data "aws_availability_zones" "available" {
  state = "available"
}

# 1. The Core Network
module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "~> 5.0"

  name = "phoenix-${terraform.workspace}-vpc"
  cidr = var.vpc_cidr

  azs = slice(data.aws_availability_zones.available.names, 0, 2)

  public_subnets = var.public_subnets
  intra_subnets  = var.intra_subnets

  enable_nat_gateway     = false
  single_nat_gateway     = false
  
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Project     = "Phoenix"
    Environment = terraform.workspace
  }
}

# 2. The S3 Gateway
resource "aws_vpc_endpoint" "s3" {
  vpc_id       = module.vpc.vpc_id
  service_name = "com.amazonaws.us-east-2.s3"
  
  # Corrected: vpc_endpoint_type is the right name, 
  # but "Gateway" is the default so we can safely omit it.
  vpc_endpoint_type = "Gateway"

  route_table_ids = flatten([
    module.vpc.intra_route_table_ids,
    module.vpc.public_route_table_ids
  ])

  tags = {
    Name        = "phoenix-${terraform.workspace}-s3-endpoint"
    Environment = terraform.workspace
  }
}