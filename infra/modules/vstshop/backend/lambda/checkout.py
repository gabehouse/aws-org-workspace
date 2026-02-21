import os
import json
import stripe

# Initialize Stripe
stripe.api_key = os.environ.get('STRIPE_SECRET_KEY')

# Load the Product Map once at the start (Global scope for performance)
# Terraform passes this as: {"cool-synth-v1": "price_123...", "retro-verb-v1": "price_456..."}
PRODUCT_MAP = json.loads(os.environ.get('STRIPE_PRODUCT_MAP', '{}'))


def handler(event, context):
    # 1. Get the requested product ID from Query Parameters
    params = event.get('queryStringParameters') or {}
    requested_id = params.get('id')

    # 2. Look up the Price ID from our Map
    price_id = PRODUCT_MAP.get(requested_id)

    if not price_id:
        return {
            'statusCode': 400,
            'headers': {"Access-Control-Allow-Origin": "*"},
            'body': json.dumps({'error': f'Product {requested_id} not found in catalog.'})
        }

    # 3. Handle Frontend URLs
    browser_origin = event.get('headers', {}).get(
        'origin') or event.get('headers', {}).get('Origin')
    fallback_url = os.environ.get('FRONTEND_URL')
    frontend_url = browser_origin or fallback_url or "http://localhost:5173"

    try:
        # 4. Get User Identity from Cognito Authorizer
        user_id = event['requestContext']['authorizer']['claims']['sub']

        # 5. Create Stripe Session using the Dynamic Price ID
        session = stripe.checkout.Session.create(
            payment_method_types=['card'],
            line_items=[{
                'price': price_id,  # No price_data needed; Stripe knows the price from the ID
                'quantity': 1,
            }],
            mode='payment',
            metadata={
                'productId': requested_id  # Essential for the Webhook to grant the right download
            },
            client_reference_id=user_id,
            success_url=f"{frontend_url}/success?session_id={{CHECKOUT_SESSION_ID}}",
            cancel_url=f"{frontend_url}/cancel",
        )

        return {
            'statusCode': 200,
            'headers': {
                "Access-Control-Allow-Origin": "*",
                "Content-Type": "application/json"
            },
            'body': json.dumps({'checkoutUrl': session.url})
        }
    except Exception as e:
        print(f"Checkout Error: {str(e)}")
        return {
            'statusCode': 500,
            'headers': {"Access-Control-Allow-Origin": "*"},
            'body': json.dumps({'error': 'Internal server error during checkout.'})
        }
