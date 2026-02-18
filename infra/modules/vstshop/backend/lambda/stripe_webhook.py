import json
import os
import boto3
import stripe

# Initialize clients
dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table(os.environ['TABLE_NAME'])
stripe.api_key = os.environ['STRIPE_SECRET_KEY']


def lambda_handler(event, context):
    # 1. Get the RAW body and signature
    payload = event['body']
    # Use .get() to handle potential case sensitivity in headers
    sig_header = event['headers'].get(
        'stripe-signature') or event['headers'].get('Stripe-Signature')
    endpoint_secret = os.environ['STRIPE_WEBHOOK_SECRET']

    stripe_event = None

    try:
        # 2. Verify the webhook event (MUST use raw payload string)
        stripe_event = stripe.Webhook.construct_event(
            payload, sig_header, endpoint_secret
        )
    except ValueError as e:
        print("Invalid payload")
        return {'statusCode': 400, 'body': json.dumps({'message': 'Invalid payload'})}
    except stripe.error.SignatureVerificationError as e:
        print(f"Signature verification failed: {e}")
        return {'statusCode': 400, 'body': json.dumps({'message': 'Invalid signature'})}

    # 3. Handle the checkout.session.completed event
    if stripe_event['type'] == 'checkout.session.completed':
        session = stripe_event['data']['object']

        user_id = session.get('client_reference_id')
        product_id = session.get('metadata', {}).get('productId')

        if user_id and product_id:
            print(f"✅ Success: User {user_id} bought {product_id}")

            table.put_item(
                Item={
                    'userId': user_id,
                    'productId': product_id,
                    'purchaseDate': session['created']
                }
            )
        else:
            print("❌ Error: Missing user_id or product_id in metadata")

    # Always return 200 to Stripe for other event types so it stops retrying
    return {'statusCode': 200, 'body': json.dumps({'message': 'Success'})}
