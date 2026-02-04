module "networking" {
  source = "../../../modules/networking"

  region         = "us-east-2"
  vpc_name       = "phoenix-dev-vpc"
  vpc_cidr       = "10.0.0.0/16"
  public_subnets = ["10.0.1.0/24", "10.0.2.0/24"]
  intra_subnets  = ["10.0.10.0/24", "10.0.11.0/24"]

  tags = {
    Project     = "Phoenix"
    Environment = "dev"
    ManagedBy   = "Terraform"
  }
}