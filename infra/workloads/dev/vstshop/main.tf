module "globals" {
  source = "../../../modules/globals"
}

module "vstshop_frontend" {
  source      = "../../../modules/vstshop-frontend"
  environment = "dev"
}

moved {
  from = module.vstshop
  to   = module.vstshop_frontend
}

module "vstshop_auth" {
  source       = "../../../modules/vstshop-auth"
  environment  = "dev"
  project_name = "vstshop"
  unique_id    = "vst-dev-123" # To ensure unique Cognito domain

  # Dynamically pass the CloudFront URL for Auth redirects
  callback_url = module.vstshop_frontend.website_url
}
