import json
import os
import boto3
import stripe
import base64

# Initialize clients
dynamodb = boto3.resource('dynamodb')
table = dynamodb.Table(os.environ['TABLE_NAME'])
stripe.api_key = os.environ['STRIPE_SECRET_KEY']


def lambda_handler(event, context):
    # 1. Get the signature from headers (check both casings)
    sig_header = event['headers'].get(
        'stripe-signature') or event['headers'].get('Stripe-Signature')
    endpoint_secret = os.environ['STRIPE_WEBHOOK_SECRET']

    # 2. Get the RAW body
    payload = event['body']

    # 3. Handle Base64 encoding (The most likely culprit)
    if event.get('isBase64Encoded'):
        payload = base64.b64decode(payload).decode('utf-8')

    try:
        # 4. Verify the webhook event
        stripe_event = stripe.Webhook.construct_event(
            payload, sig_header, endpoint_secret
        )
        print(f"✅ Signature Verified! Event Type: {stripe_event['type']}")

    except stripe.error.SignatureVerificationError as e:
        print(f"❌ Verification Failed: {e}")
        # Final debug: Is the payload empty or weird?
        print(f"Payload Preview: {str(payload)[:50]}")
        return {'statusCode': 400, 'body': 'Invalid Signature'}

    # 5. Handle the event
    if stripe_event['type'] == 'checkout.session.completed':
        session = stripe_event['data']['object']
        user_id = session.get('client_reference_id')
        product_id = session.get('metadata', {}).get('productId')

        if user_id and product_id:
            table.put_item(
                Item={
                    'userId': user_id,
                    'productId': product_id,
                    'purchaseDate': session['created']
                }
            )
            print(f"📂 DynamoDB Updated for {user_id}")

    return {'statusCode': 200, 'body': 'Success'}
