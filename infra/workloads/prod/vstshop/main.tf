module "globals" {
  source = "../../../modules/globals"
}

module "frontend" {
  source              = "../../../modules/vstshop/frontend"
  project_name        = "vstshop"
  environment         = var.environment
  aliases             = ["houseaudio.net"]
  acm_certificate_arn = aws_acm_certificate_validation.cert_verify.certificate_arn
  depends_on          = [aws_acm_certificate_validation.cert_verify]
}

module "auth" {
  source       = "../../../modules/vstshop/auth"
  environment  = var.environment
  project_name = var.project_name
  unique_id    = "vst-${var.environment}-123" # To ensure unique Cognito domain

  # Dynamically pass the CloudFront URL for Auth redirects
  callback_url = module.frontend.website_url
}


# 1. Create the Database
module "database" {
  source       = "../../../modules/vstshop/database"
  project_name = var.project_name
  environment  = var.environment
}

# 2. Pass database outputs into the Backend
module "backend" {
  source          = "../../../modules/vstshop/backend"
  project_name    = var.project_name
  environment     = var.environment
  vst_bucket_name = module.storage.vst_bucket_name
  user_pool_arn   = module.auth.user_pool_arn

  # These link the modules together:
  purchases_table_name = module.database.table_name
  purchases_table_arn  = module.database.table_arn

  stripe_secret_key     = var.stripe_secret_key
  stripe_webhook_secret = var.stripe_webhook_secret

  cloudfront_url = module.frontend.website_url
}


module "storage" {
  source      = "../../../modules/vstshop/storage"
  domain_name = module.frontend.website_url
  environment = var.environment
}

# This tells Terraform to write a file on your local machine
resource "local_file" "env_file" {
  filename = "${path.module}/../../../../services/vstshop-frontend/.env"
  content  = <<-EOT
    VITE_AWS_REGION=${module.globals.region}
    VITE_USER_POOL_ID=${module.auth.user_pool_id}
    VITE_USER_POOL_CLIENT_ID=${module.auth.client_id}
    VITE_API_URL=${module.backend.api_url}
    VITE_CLOUDFRONT_URL=https://houseaudio.net
  EOT
}

# 2. GET YOUR HOSTED ZONE
data "aws_route53_zone" "main" {
  name         = "houseaudio.net"
  private_zone = false
}

# 3. CREATE SSL CERTIFICATE (Using your existing alias)
resource "aws_acm_certificate" "cert" {
  provider          = aws.us_east_1 # Updated to match your provider alias
  domain_name       = "houseaudio.net"
  validation_method = "DNS"

  lifecycle {
    create_before_destroy = true
  }
}

# 4. AUTOMATIC DNS VALIDATION
resource "aws_route53_record" "cert_validation" {
  for_each = {
    for dvo in aws_acm_certificate.cert.domain_validation_options : dvo.domain_name => {
      name   = dvo.resource_record_name
      record = dvo.resource_record_value
      type   = dvo.resource_record_type
    }
  }

  allow_overwrite = true
  name            = each.value.name
  records         = [each.value.record]
  ttl             = 60
  type            = each.value.type
  zone_id         = data.aws_route53_zone.main.zone_id
}

# 5. WAIT FOR VALIDATION TO FINISH
resource "aws_acm_certificate_validation" "cert_verify" {
  provider                = aws.us_east_1 # Updated to match your provider alias
  certificate_arn         = aws_acm_certificate.cert.arn
  validation_record_fqdns = [for record in aws_route53_record.cert_validation : record.fqdn]
}

# 6. POINT DOMAIN TO CLOUDFRONT
resource "aws_route53_record" "www" {
  zone_id = data.aws_route53_zone.main.zone_id
  name    = "houseaudio.net"
  type    = "A"

  alias {
    name                   = module.frontend.cloudfront_domain_name
    zone_id                = module.frontend.cloudfront_hosted_zone_id
    evaluate_target_health = false
  }
}
