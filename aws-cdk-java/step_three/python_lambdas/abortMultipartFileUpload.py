import logging
from botocore.exceptions import ClientError
from errors import bad_request, any_error, not_found
import json
import uuid
import boto3
import os

logger = logging.getLogger()
logger.setLevel(logging.INFO)


def handler(event, context):
    logger.info(event)
    body = json.loads(event['body'])

    ssm_client = boto3.client('ssm')
    bucket_name = os.environ['S3_BUCKET_NAME']
    try:
        file_key = body['storageIdentifier']
        upload_id = body["uploadId"]
    except KeyError as e:
        return bad_request(e)

    access_key_id = ssm_client.get_parameter(Name='/lambda-s3-signing/access-key-id')['Parameter']['Value']
    secret_access_key = \
        ssm_client.get_parameter(Name='/lambda-s3-signing/secret-access-key', WithDecryption=True)['Parameter'][
            'Value']
    region = ssm_client.get_parameter(Name='/lambda-s3-signing/region')['Parameter']['Value']

    try:
        session = boto3.Session(aws_access_key_id=access_key_id, aws_secret_access_key=secret_access_key,
                                region_name=region)
        s3 = session.client('s3')
        response = s3.abort_multipart_upload(Bucket=bucket_name, Key=file_key, UploadId=upload_id)
    except ClientError as e:
        logger.warning(e.response['Error']['Code'])
        return any_error(e.response['Error']['Code'], e.response['ResponseMetadata']['HTTPStatusCode'])

    return {
        'statusCode': 200,
        'body': json.dumps({'response': response}),
        'headers': {
            "Access-Control-Allow-Origin": "*", "Access-Control-Allow-Credentials": True,
            "Access-Control-Allow-Headers": "Origin,Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,locale",
            "Access-Control-Allow-Methods": "POST, OPTIONS, GET",
        },
    }
