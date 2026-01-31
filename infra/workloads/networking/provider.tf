provider "aws" {
  region  = "us-east-2"
  # This tells Terraform: "If I'm in the dev workspace, use the dev profile"
  profile = terraform.workspace == "default" ? "management" : terraform.workspace
}