module "networking" {
  source = "../../../modules/networking"

  region         = "us-east-2"
  vpc_name       = "phoenix-prod-vpc"
  vpc_cidr       = "10.1.0.0/16"
  public_subnets = ["10.1.1.0/24", "10.1.2.0/24"]
  intra_subnets  = ["10.1.10.0/24", "10.1.11.0/24"]

  tags = {
    Project     = "Phoenix"
    Environment = "prod"
    ManagedBy   = "Terraform"
  }
}
