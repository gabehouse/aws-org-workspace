import os
import json
import stripe

# Initialize Stripe
stripe.api_key = os.environ.get('STRIPE_SECRET_KEY')

# Global map
PRODUCT_MAP = json.loads(os.environ.get('STRIPE_PRODUCT_MAP', '{}'))

# Standard CORS headers for every response
CORS_HEADERS = {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Headers": "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token",
    "Access-Control-Allow-Methods": "OPTIONS,POST,GET"
}


def handler(event, context):
    # 1. Get the requested product ID
    params = event.get('queryStringParameters') or {}
    requested_id = params.get('id')

    # 2. Look up the Price ID
    price_id = PRODUCT_MAP.get(requested_id)

    if not price_id:
        return {
            'statusCode': 400,
            'headers': CORS_HEADERS,
            'body': json.dumps({'error': f'Product {requested_id} not found.'})
        }

    # 3. Dynamic Origin Handling
    headers = event.get('headers', {})
    browser_origin = headers.get('origin') or headers.get('Origin')
    fallback_url = os.environ.get('FRONTEND_URL')
    frontend_url = browser_origin or fallback_url or "http://localhost:5173"

    try:
        # 4. Identity
        # Ensure we catch cases where authorizer might be missing
        authorizer = event.get('requestContext', {}).get('authorizer', {})
        user_id = authorizer.get('claims', {}).get('sub') or "guest"

        # 5. Create Session
        session = stripe.checkout.Session.create(
            payment_method_types=['card'],
            line_items=[{'price': price_id, 'quantity': 1}],
            mode='payment',
            metadata={'productId': requested_id},
            client_reference_id=user_id,
            # Use f-string but keep the double braces for Stripe
            success_url=f"{frontend_url}/success?session_id={{CHECKOUT_SESSION_ID}}",
            cancel_url=f"{frontend_url}/cancel",
        )

        return {
            'statusCode': 200,
            'headers': CORS_HEADERS,
            'body': json.dumps({'checkoutUrl': session.url})
        }

    except Exception as e:
        print(f"Checkout Error: {str(e)}")
        return {
            'statusCode': 400,  # Using 400 instead of 500 often avoids API Gateway "panic" 502s
            'headers': CORS_HEADERS,
            # Return real error so you can see it in console
            'body': json.dumps({'error': str(e)})
        }
