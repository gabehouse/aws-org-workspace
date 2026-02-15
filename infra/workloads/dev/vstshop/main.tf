module "globals" {
  source = "../../../modules/globals"
}

module "vstshop_frontend" {
  source       = "../../../modules/vstshop-frontend"
  project_name = "vstshop"
  environment  = "dev"
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

module "vstshop_backend" {
  source        = "../../../modules/vstshop-backend"
  environment   = "dev"
  project_name  = "vstshop"
  user_pool_arn = module.vstshop_auth.user_pool_arn # Passing the ARN here
}
