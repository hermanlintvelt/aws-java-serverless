import type { ValidatedEventAPIGatewayProxyEvent } from '@libs/api-gateway';
import { formatJSONResponse } from '@libs/api-gateway';
import { middyfy } from '@libs/lambda';
import { S3 } from 'aws-sdk';
import { S3Client, UploadPartCommandInput } from '@aws-sdk/client-s3';

import schema from './schema';
import { SSM } from 'aws-sdk';

import { contentType } from 'mime-types';

const requestMultipartUploadURL: ValidatedEventAPIGatewayProxyEvent<typeof schema> = async (event) => {
  try {
    const body = typeof event.body === 'string' ? JSON.parse(event.body) : event.body;
    const mimeType = contentType(body.fileType);

    const ssm = new SSM();
    const accessKeyId = (await ssm.getParameter({ Name: '/lambda-s3-signing/access-key-id' }).promise()).Parameter.Value;
    const secretAccessKey = (await ssm.getParameter({ Name: '/lambda-s3-signing/secret-access-key', WithDecryption: true }).promise()).Parameter
      .Value;
    const region = (await ssm.getParameter({ Name: '/lambda-s3-signing/region' }).promise()).Parameter.Value;

    const s3Client = new S3({ region, credentials: { secretAccessKey, accessKeyId } });

    const fileKey = body.storageIdentifier;

    // Request the signed URL from S3
    const putCommand = {
      Bucket: process.env.S3_BUCKET_NAME,
      Key: body.folder ? `${body.folder}/${fileKey}` : fileKey,
      UploadId: body.uploadId,
      PartNumber: body.index,
    };

    const url = await s3Client.getSignedUrlPromise('uploadPart', putCommand);
    console.log(url);

    return formatJSONResponse({
      url,
      contentType: mimeType,
      fileKey,
      event,
    });
  } catch (err) {
    return {
      headers: {
        'Access-Control-Allow-Headers': 'Content-Type,X-Amz-Date,Authorization,X-Api-Key,Origin,Accept,Accept-Encoding,Host,User-Agent',
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'OPTIONS,POST,GET',
      },
      statusCode: err.statusCode,
      body: JSON.stringify({
        message: `An error occurred in the requestMultipartUploadURL Lambda Handler: ${err}`,
        event,
      }),
    };
  }
};

export const main = middyfy(requestMultipartUploadURL);
