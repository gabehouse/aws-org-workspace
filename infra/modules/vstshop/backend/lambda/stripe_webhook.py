import json
import os
import boto3
import stripe

# Initialize clients
dynamodb = boto3.resource('dynamodb')
# TABLE_NAME should be set in your Lambda environment variables
table = dynamodb.Table(os.environ['TABLE_NAME'])

# Stripe API key must be in Lambda environment variables (sk_test_...)
stripe.api_key = os.environ['STRIPE_SECRET_KEY']


def lambda_handler(event, context):
    payload = event['body']
    sig_header = event['headers'].get('stripe-signature')
    endpoint_secret = os.environ['STRIPE_WEBHOOK_SECRET']

    event = None

    try:
        # 1. Verify the webhook event came from Stripe
        event = stripe.Webhook.construct_event(
            payload, sig_header, endpoint_secret
        )
    except ValueError as e:
        # Invalid payload
        return {'statusCode': 400, 'body': json.dumps({'message': 'Invalid payload'})}
    except stripe.error.SignatureVerificationError as e:
        # Invalid signature
        return {'statusCode': 400, 'body': json.dumps({'message': 'Invalid signature'})}

    # 2. Handle the checkout.session.completed event
    if event['type'] == 'checkout.session.completed':
        session = event['data']['object']

        # Extract the customer ID and product ID
        # These were passed in when creating the checkout session
        user_id = session.get('client_reference_id')
        product_id = session.get('metadata', {}).get('productId')

        if user_id and product_id:
            print(f"Purchased: User {user_id} bought Product {product_id}")

            # 3. Add to DynamoDB
            table.put_item(
                Item={
                    'userId': user_id,
                    'productId': product_id,
                    'purchaseDate': session['created']
                }
            )

    return {'statusCode': 200, 'body': json.dumps({'message': 'Success'})}
