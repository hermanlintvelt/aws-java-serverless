import type { ValidatedEventAPIGatewayProxyEvent } from '@libs/api-gateway';
import { formatJSONResponse } from '@libs/api-gateway';
import { middyfy } from '@libs/lambda';
import { SSM } from 'aws-sdk';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';
import { S3Client, GetObjectCommand, PutObjectCommand } from '@aws-sdk/client-s3';
import { v4 as uuidv4 } from 'uuid';
import { contentType } from 'mime-types';
import schema from './schema';

const requestPresignedURL: ValidatedEventAPIGatewayProxyEvent<typeof schema> = async (event) => {
  try {
    const body = typeof event.body === 'string' ? JSON.parse(event.body) : event.body;
    const mimeType = contentType(body.fileType);

    const ssm = new SSM();
    const accessKeyId = (await ssm.getParameter({ Name: '/lambda-s3-signing/access-key-id' }).promise()).Parameter.Value;
    const secretAccessKey = (await ssm.getParameter({ Name: '/lambda-s3-signing/secret-access-key', WithDecryption: true }).promise()).Parameter
      .Value;
    const region = (await ssm.getParameter({ Name: '/lambda-s3-signing/region' }).promise()).Parameter.Value;

    const s3 = new S3Client({ region, credentials: { accessKeyId, secretAccessKey } });

    const logTime = Date.now();

    const fileKey = uuidv4() + body.fileType;

    // Will probable use this to log note in DB
    const newEvent = {
      uploadState: 'pending', // for db entries
      logTime, // for db entries
      bucket: {
        name: process.env.S3_BUCKET_NAME,
        key: body.folder ? `${body.folder}/${fileKey}` : fileKey,
        mimeType: mimeType,
        region: process.env.AWS_REGION,
      },
    };

    // Params for the S3 bucket
    const s3Params = {
      Bucket: newEvent.bucket.name,
      Key: newEvent.bucket.key,
      ContentType: mimeType,
    };

    const s3ParamsForDownload = {
      Bucket: newEvent.bucket.name,
      Key: newEvent.bucket.key,
    };

    const putCommand = new PutObjectCommand(s3Params);

    // Request the signed URL from S3
    const url = await getSignedUrl(s3, putCommand, { expiresIn: 7 * 24 * 60 * 60 });

    // Request the signed URL from S3
    const command = new GetObjectCommand(s3ParamsForDownload);
    const downloadUrl = await getSignedUrl(s3, command, { expiresIn: 7 * 24 * 60 * 60 });

    return formatJSONResponse({
      url,
      downloadUrl,
      contentType: mimeType,
      fileKey: newEvent.bucket.key,
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
        message: `An error occurred in the requestPresignedURL Lambda Handler: ${err}`,
        event,
      }),
    };
  }
};

export const main = middyfy(requestPresignedURL);
