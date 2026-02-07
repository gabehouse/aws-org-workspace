provider "aws" {
  region  = var.region
  profile = "management" # Or whatever your Management account profile is named
}
