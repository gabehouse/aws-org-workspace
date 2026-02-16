module "globals" {
  source = "../../globals"
}

resource "aws_s3_bucket" "vst_storage" {
  bucket = "vst-storage-${var.environment}-${module.globals.accounts.dev}"
}

# 1. Strictly block all public access
resource "aws_s3_bucket_public_access_block" "vst_storage_block" {
  bucket = aws_s3_bucket.vst_storage.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# 2. CORS configuration so your Local Dev can download files
resource "aws_s3_bucket_cors_configuration" "vst_cors" {
  bucket = aws_s3_bucket.vst_storage.id

  cors_rule {
    allowed_headers = ["*"]
    allowed_methods = ["GET"]
    allowed_origins = [
      "http://localhost:5173",
      "https://${var.domain_name}"
    ]
    expose_headers  = ["ETag"]
    max_age_seconds = 3000
  }
}

output "bucket_name" {
  value = aws_s3_bucket.vst_storage.id
}
