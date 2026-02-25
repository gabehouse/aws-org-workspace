locals {
  vst_catalog = {
    "cool-synth-v1" = {
      name   = "Acid Saturator"
      price  = 500
      s3_key = "AcidSaturator.zip"
    }
  }
}

resource "stripe_product" "vst" {
  for_each = local.vst_catalog
  name     = each.value.name
}

resource "stripe_price" "vst_price" {
  for_each    = stripe_product.vst
  product     = each.value.id
  unit_amount = local.vst_catalog[each.key].price
  currency    = "usd"
}

# This creates a file your React app can import!
resource "local_file" "product_config" {
  filename = "${path.module}/../../../../services/vstshop-frontend/src/product_config.json"
  content = jsonencode([
    for k, v in local.vst_catalog : {
      id    = k
      name  = v.name
      price = format("$%.2f", v.price / 100)
    }
  ])
}
