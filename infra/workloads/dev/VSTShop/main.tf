# 1. THE FRONTEND BUCKET
resource "aws_s3_bucket" "frontend" {
  bucket        = "${var.project_name}-frontend-${var.environment}"
  force_destroy = true 
}

# 2. THE BUCKET POLICY (Merged)
resource "aws_s3_bucket_policy" "frontend_policy" {
  bucket = aws_s3_bucket.frontend.id
  policy = data.aws_iam_policy_document.combined_policy.json
}

data "aws_iam_policy_document" "combined_policy" {
  # Statement 1: Allow CloudFront to read files (OAC)
  statement {
    sid       = "AllowCloudFrontServicePrincipal"
    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.frontend.arn}/*"]

    principals {
      type        = "Service"
      identifiers = ["cloudfront.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "AWS:SourceArn"
      values   = [aws_cloudfront_distribution.s3_distribution.arn]
    }
  }

  # Statement 2: Allow GitHub Actions Role to Sync
  statement {
    sid       = "AllowGithubActionsSync"
    actions   = [
      "s3:ListBucket",
      "s3:GetObject",
      "s3:PutObject",
      "s3:DeleteObject"
    ]
    # Bucket and Objects
    resources = [
      aws_s3_bucket.frontend.arn,
      "${aws_s3_bucket.frontend.arn}/*"
    ]

    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::086739225244:role/github-actions-oidc-role"]
    }
  }
}

# 2. ORIGIN ACCESS CONTROL (The "Identity" for CloudFront)
resource "aws_cloudfront_origin_access_control" "default" {
  name                              = "${var.project_name}-oac-${var.environment}"
  description                       = "OAC for ${var.project_name} S3 access"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

# 3. CLOUDFRONT DISTRIBUTION
resource "aws_cloudfront_distribution" "s3_distribution" {
  origin {
    domain_name              = aws_s3_bucket.frontend.bucket_regional_domain_name
    origin_id                = "S3-${aws_s3_bucket.frontend.id}"
    origin_access_control_id = aws_cloudfront_origin_access_control.default.id
  }

  enabled             = true
  is_ipv6_enabled     = true
  default_root_object = "index.html"

  default_cache_behavior {
    allowed_methods  = ["GET", "HEAD"]
    cached_methods   = ["GET", "HEAD"]
    target_origin_id = "S3-${aws_s3_bucket.frontend.id}"

    forwarded_values {
      query_string = false
      cookies {
        forward = "none"
      }
    }

    viewer_protocol_policy = "redirect-to-https"
    min_ttl                = 0
    default_ttl            = 3600
    max_ttl                = 86400
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    cloudfront_default_certificate = true
  }
}

# 4. BUCKET POLICY (Allows only CloudFront to talk to S3)
resource "aws_s3_bucket_policy" "allow_access_from_cloudfront" {
  bucket = aws_s3_bucket.frontend.id
  policy = data.aws_iam_policy_document.allow_access_from_cloudfront.json
}

data "aws_iam_policy_document" "allow_access_from_cloudfront" {
  statement {
    actions   = ["s3:GetObject"]
    resources = ["${aws_s3_bucket.frontend.arn}/*"]

    principals {
      type        = "Service"
      identifiers = ["cloudfront.amazonaws.com"]
    }

    condition {
      test     = "StringEquals"
      variable = "AWS:SourceArn"
      values   = [aws_cloudfront_distribution.s3_distribution.arn]
    }
  }
}