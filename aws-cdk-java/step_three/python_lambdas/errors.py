import json


def any_error(message: str, code: int) -> dict:
    return {
        'statusCode': code,
        'body': json.dumps({'message': message}),
        'headers': {
            "Access-Control-Allow-Origin": "*", "Access-Control-Allow-Credentials": True,
            "Access-Control-Allow-Headers": "Origin,Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,locale",
            "Access-Control-Allow-Methods": "POST, OPTIONS, GET",
        }
    }


def bad_request(exc: ValueError) -> dict:
    return {
        "statusCode": 400,
        "body": json.dumps({'message': f'Request is missing property: {exc.args[0]}'}),
        'headers': {
            "Access-Control-Allow-Origin": "*", "Access-Control-Allow-Credentials": True,
            "Access-Control-Allow-Headers": "Origin,Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,locale",
            "Access-Control-Allow-Methods": "POST, OPTIONS, GET",
        }
    }


def not_found(message: str):
    return {
        "statusCode": 404,
        # Dump camelCase for API
        "body": json.dumps(
            {'message': message}
        ),
        'headers': {
            "Access-Control-Allow-Origin": "*", "Access-Control-Allow-Credentials": True,
            "Access-Control-Allow-Headers": "Origin,Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,locale",
            "Access-Control-Allow-Methods": "POST, OPTIONS, GET",
        }
    }
