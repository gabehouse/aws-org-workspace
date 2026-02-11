module "globals" {
  source = "../../../modules/globals"
}

module "vstshop" {
  source      = "../../../modules/vstshop-frontend"
  environment = "dev"
}
