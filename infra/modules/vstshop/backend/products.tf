locals {
  vst_catalog = {
    "cool-synth-v1" = {
      name   = "Cool Synth VST"
      price  = 2999
      s3_key = "vsts/cool-synth-v1.zip"
    },
    "retro-verb-v1" = {
      name   = "Retro Verb VST"
      price  = 1999
      s3_key = "vsts/retro-verb.zip"
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
resource "local_file" "vst_config" {
  filename = "${path.module}/../../../../services/vstshop-frontend/src/product_config.json"
  content = jsonencode([
    for k, v in local.vst_catalog : {
      id    = k
      name  = v.name
      price = format("$%.2f", v.price / 100)
    }
  ])
}
