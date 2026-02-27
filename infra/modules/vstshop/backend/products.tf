locals {
  vst_catalog = {
    "acid-saturator" = {
      name      = "Acid Saturator"
      price     = 1299
      s3_key    = "AcidSaturator.zip"
      imagePath = "product-media/acid-saturator-gui.png"
      youtubeId = "dQw4w9WgXcQ"
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

resource "local_file" "product_config" {
  filename = "${path.module}/../../../../services/vstshop-frontend/src/product_config.json"
  content = jsonencode([
    for k, v in local.vst_catalog : {
      id        = k
      name      = v.name
      price     = format("$%.2f", v.price / 100)
      imagePath = v.imagePath
      youtubeId = v.youtubeId # Pass this to the JSON
    }
  ])
}
