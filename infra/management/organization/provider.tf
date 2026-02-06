provider "aws" {
  region  = var.aws_region
  profile = "management" # Or whatever your Management account profile is named
}
