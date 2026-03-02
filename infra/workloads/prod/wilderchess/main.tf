module "globals" {
  source = "../../../modules/globals"
}

data "aws_elastic_beanstalk_solution_stack" "latest_corretto" {
  most_recent = true
  name_regex  = "^64bit Amazon Linux 2023 v.* running Corretto 21$"
}

# 1. Access the Networking State
data "terraform_remote_state" "network" {
  backend = "s3"
  config = {
    bucket = "gabriel-tf-state-2026"
    key    = "workloads/prod/networking/terraform.tfstate"
    region = "us-east-2"
  }
}

# 2. Application Resources (S3)
resource "aws_s3_bucket" "prod_app_bucket" {
  bucket = "wilderchess-app-bucket-123"
  tags   = { Name = "prod-app-bucket" }
}

# 3. Call your core module using the REMOTE data
module "wilderchess" {
  source = "../../../modules/wilderchess"

  app_name            = "Wilderchess"
  env_name            = "Wilderchess"
  solution_stack_name = data.aws_elastic_beanstalk_solution_stack.latest_corretto.name
  version_label       = "my-app-${var.app_version}"
  instance_type       = var.prod_instance_type
  s3_bucket_name      = aws_s3_bucket.prod_app_bucket.id
  source_path         = "../../../../services/wilderchess/app-${var.app_version}.zip"

  vpc_id             = data.terraform_remote_state.network.outputs.vpc_id
  public_subnet_ids  = data.terraform_remote_state.network.outputs.public_subnets
  private_subnet_ids = data.terraform_remote_state.network.outputs.intra_subnets
}
