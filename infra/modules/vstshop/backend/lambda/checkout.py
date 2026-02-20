import os
import json
import stripe

stripe.api_key = os.environ.get('STRIPE_SECRET_KEY')


def handler(event, context):
    # 1. Try to get Origin from browser (dynamic)
    # 2. Fallback to FRONTEND_URL from Terraform (static)
    # 3. Last resort: localhost (hardcoded safety)
    browser_origin = event.get('headers', {}).get(
        'origin') or event.get('headers', {}).get('Origin')
    fallback_url = os.environ.get('FRONTEND_URL')

    frontend_url = browser_origin or fallback_url or "http://localhost:5173"

    try:
        user_id = event['requestContext']['authorizer']['claims']['sub']

        session = stripe.checkout.Session.create(
            payment_method_types=['card'],
            line_items=[{
                'price_data': {
                    'currency': 'usd',
                    'product_data': {'name': 'Cool Synth VST'},
                    'unit_amount': 2999,
                },
                'quantity': 1,
            }],
            mode='payment',
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
        return {'statusCode': 400, 'body': json.dumps({'error': str(e)})}
