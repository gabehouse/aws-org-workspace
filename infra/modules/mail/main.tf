data "aws_region" "current" {}

resource "aws_iam_role" "mail_role" {
  count = var.is_enabled ? 1 : 0
  name  = "${var.project_name}-mail-role-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action    = "sts:AssumeRole"
      Effect    = "Allow"
      Principal = { Service = "lambda.amazonaws.com" }
    }]
  })
}

resource "aws_iam_role_policy" "mail_policy" {
  count = var.is_enabled ? 1 : 0
  name  = "mail-permissions"
  role  = aws_iam_role.mail_role[0].id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect   = "Allow"
        Action   = ["logs:CreateLogGroup", "logs:CreateLogStream", "logs:PutLogEvents"]
        Resource = "arn:aws:logs:*:*:*"
      },
      {
        Effect   = "Allow"
        Action   = ["s3:GetObject"]
        Resource = "${aws_s3_bucket.email_storage[0].arn}/*"
      },
      {
        Effect   = "Allow"
        Action   = ["ses:SendRawEmail", "ses:SendEmail"]
        Resource = "*"
      }
    ]
  })
}


# 1. --- SES Domain Identity ---
resource "aws_ses_domain_identity" "main" {
  count  = var.is_enabled ? 1 : 0
  domain = var.domain_name
}

# 2. --- DNS Records (Route 53) ---
resource "aws_route53_record" "ses_verification" {
  count   = var.is_enabled ? 1 : 0
  zone_id = var.route53_zone_id
  name    = "_amazonses.${var.domain_name}"
  type    = "TXT"
  ttl     = "600"
  records = [aws_ses_domain_identity.main[0].verification_token]
}

resource "aws_route53_record" "mx" {
  count   = var.is_enabled ? 1 : 0
  zone_id = var.route53_zone_id
  name    = var.domain_name
  type    = "MX"
  ttl     = "300"
  records = ["10 inbound-smtp.${data.aws_region.current.name}.amazonaws.com"]
}

# 3. --- S3 Storage for Inbound Mail ---
resource "aws_s3_bucket" "email_storage" {
  count  = var.is_enabled ? 1 : 0
  bucket = "${var.project_name}-inbound-emails-${var.environment}"
}

resource "aws_s3_bucket_policy" "allow_ses" {
  count  = var.is_enabled ? 1 : 0
  bucket = aws_s3_bucket.email_storage[0].id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Sid       = "AllowSESPuts"
      Effect    = "Allow"
      Principal = { Service = "ses.amazonaws.com" }
      Action    = "s3:PutObject"
      Resource  = "${aws_s3_bucket.email_storage[0].arn}/*"
    }]
  })
}

# 4. --- The Forwarder Lambda ---
data "archive_file" "email_forwarder_zip" {
  count       = var.is_enabled ? 1 : 0
  type        = "zip"
  source_file = "${path.module}/lambda/email_forwarder.py"
  output_path = "${path.module}/lambda/email_forwarder.zip"
}

resource "aws_lambda_function" "email_forwarder" {
  count            = var.is_enabled ? 1 : 0
  function_name    = "${var.project_name}-email-forwarder-${var.environment}"
  role             = aws_iam_role.mail_role[0].arn
  handler          = "email_forwarder.handler"
  runtime          = "python3.12"
  filename         = data.archive_file.email_forwarder_zip[0].output_path
  source_code_hash = data.archive_file.email_forwarder_zip[0].output_base64sha256
  timeout          = 15

  environment {
    variables = {
      BUCKET_NAME    = aws_s3_bucket.email_storage[0].bucket
      DOMAIN         = var.domain_name
      PERSONAL_EMAIL = var.personal_email
    }
  }
}

resource "aws_lambda_permission" "allow_ses" {
  count         = var.is_enabled ? 1 : 0
  statement_id  = "AllowExecutionFromSES"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.email_forwarder[0].function_name
  principal     = "ses.amazonaws.com"
  source_arn    = aws_ses_domain_identity.main[0].arn
}

# 5. --- SES Receipt Rules ---
resource "aws_ses_receipt_rule_set" "main" {
  count         = var.is_enabled ? 1 : 0
  rule_set_name = "${var.project_name}-inbound-rules"
}

resource "aws_ses_active_receipt_rule_set" "main" {
  count         = var.is_enabled ? 1 : 0
  rule_set_name = aws_ses_receipt_rule_set.main[0].rule_set_name
}

resource "aws_ses_receipt_rule" "forward_support" {
  count         = var.is_enabled ? 1 : 0
  name          = "forward-support"
  rule_set_name = aws_ses_receipt_rule_set.main[0].rule_set_name
  recipients    = ["support@${var.domain_name}"]
  enabled       = true

  s3_action {
    bucket_name = aws_s3_bucket.email_storage[0].bucket
    position    = 1
  }

  lambda_action {
    function_arn = aws_lambda_function.email_forwarder[0].arn
    position     = 2
  }
}

# 6. --- DKIM Authentication ---
resource "aws_ses_domain_dkim" "main" {
  count  = var.is_enabled ? 1 : 0
  domain = aws_ses_domain_identity.main[0].domain
}

resource "aws_route53_record" "ses_dkim_records" {
  # FIX: Use a static count of 3 since SES always provides 3 tokens
  # This makes the 'keys' known at plan time.
  for_each = var.is_enabled ? toset(["0", "1", "2"]) : []

  zone_id = var.route53_zone_id
  # Access the token using the static index
  name    = "${aws_ses_domain_dkim.main[0].dkim_tokens[each.key]}._domainkey.${var.domain_name}"
  type    = "CNAME"
  ttl     = "600"
  records = ["${aws_ses_domain_dkim.main[0].dkim_tokens[each.key]}.dkim.amazonses.com"]
}

# 7. --- DMARC (The "Pro" Safety Net) ---
resource "aws_route53_record" "dmarc" {
  count   = var.is_enabled ? 1 : 0
  zone_id = var.route53_zone_id
  name    = "_dmarc.${var.domain_name}"
  type    = "TXT"
  ttl     = "600"
  records = ["v=DMARC1; p=none;"] # 'p=none' monitors; 'p=quarantine' protects
}
