import type { ValidatedEventAPIGatewayProxyEvent } from '@libs/api-gateway';
import { formatJSONResponse } from '@libs/api-gateway';
import { middyfy } from '@libs/lambda';
import { S3 } from 'aws-sdk';

import schema from './schema';

const deleteS3File: ValidatedEventAPIGatewayProxyEvent<typeof schema> = async (event) => {
  try {
    const body = typeof event.body === 'string' ? JSON.parse(event.body) : event.body;

    const s3 = new S3();

    const s3ParamsForDelete = {
      Bucket: process.env.S3_BUCKET_NAME,
      Key: body.storageIdentifier,
    };

    // Request the delete
    const res = await s3.deleteObject(s3ParamsForDelete);

    return formatJSONResponse({
      res,
      fileKey: body.storageIdentifier,
      event,
    });
  } catch (err) {
    return {
      headers: {
                "Access-Control-Allow-Headers" : "Content-Type,X-Amz-Date,Authorization,X-Api-Key,Origin,Accept,Accept-Encoding,Host,User-Agent",
                "Access-Control-Allow-Origin": "*",
                "Access-Control-Allow-Methods": "OPTIONS,POST,GET"
            },
      statusCode: err.statusCode,
      body: JSON.stringify({
        message: `An error occurred in the requestDownloadURL Lambda Handler: ${err}`,
        event,
      }),
    };
  }
};

export const main = middyfy(deleteS3File);
