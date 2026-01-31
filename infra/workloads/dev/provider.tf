# The Default Provider (Ohio)
provider "aws" {
  region = "us-east-2"

  # This block allows Terraform to "step into" the execution role
  assume_role {
    role_arn     = "arn:aws:iam::195481994910:role/terraform-execution-role-dev"
    session_name = "TerraformDeployment"
  }
}

# The CloudFront/Certificate Provider (N. Virginia)
# Required because CloudFront certificates MUST live in us-east-1
provider "aws" {
  alias  = "us_east_1" 
  region = "us-east-1"

  assume_role {
    role_arn     = "arn:aws:iam::195481994910:role/terraform-execution-role-dev"
    session_name = "TerraformDeploymentGlobal"
  }
}