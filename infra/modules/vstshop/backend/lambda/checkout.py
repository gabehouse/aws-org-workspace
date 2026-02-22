import os
import json
import logging

# Setup logging
logger = logging.getLogger()
logger.setLevel(logging.INFO)

try:
    import stripe
    STRIPE_IMPORTED = True
except ImportError:
    STRIPE_IMPORTED = False

CORS_HEADERS = {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Headers": "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token",
    "Access-Control-Allow-Methods": "OPTIONS,POST,GET"
}


def handler(event, context):
    # 1. Check if the Layer actually worked
    if not STRIPE_IMPORTED:
        return {
            'statusCode': 500,
            'headers': CORS_HEADERS,
            'body': json.dumps({'error': 'Stripe library not found in Lambda Layer.'})
        }

    stripe.api_key = os.environ.get('STRIPE_SECRET_KEY')
    PRODUCT_MAP = json.loads(os.environ.get('STRIPE_PRODUCT_MAP', '{}'))

    params = event.get('queryStringParameters') or {}
    requested_id = params.get('id')
    price_id = PRODUCT_MAP.get(requested_id)

    if not price_id:
        return {
            'statusCode': 400,
            'headers': CORS_HEADERS,
            'body': json.dumps({'error': f'Product {requested_id} not found in map: {list(PRODUCT_MAP.keys())}'})
        }

    # 2. Get Origin
    headers = event.get('headers', {})
    browser_origin = headers.get('origin') or headers.get('Origin')
    frontend_url = browser_origin or os.environ.get(
        'FRONTEND_URL', "http://localhost:5173")

    try:
        # 3. Identity check
        authorizer = event.get('requestContext', {}).get('authorizer', {})
        user_id = authorizer.get('claims', {}).get('sub') or "guest"

        logger.info(f"Creating session for {user_id} product {requested_id}")

        session = stripe.checkout.Session.create(
            payment_method_types=['card'],
            line_items=[{'price': price_id, 'quantity': 1}],
            mode='payment',
            metadata={'productId': requested_id},
            client_reference_id=user_id,
            success_url=f"{frontend_url}/success?session_id={{CHECKOUT_SESSION_ID}}",
            cancel_url=f"{frontend_url}/cancel",
        )

        return {
            'statusCode': 200,
            'headers': CORS_HEADERS,
            'body': json.dumps({'checkoutUrl': session.url})
        }

    except Exception as e:
        logger.error(f"Stripe Error: {str(e)}")
        return {
            'statusCode': 400,
            'headers': CORS_HEADERS,
            'body': json.dumps({'error': str(e)})
        }
