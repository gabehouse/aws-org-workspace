terraform {
  backend "s3" {
    bucket         = "gabriel-tf-state-2026"
    key            = "management/identity/terraform.tfstate"
    region         = "us-east-2"
    dynamodb_table = "terraform-state-lock"
    encrypt        = true
    # We use the profile we just successfully tested
    profile        = "management" 
  }
}