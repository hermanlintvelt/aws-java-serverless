import type { ValidatedEventAPIGatewayProxyEvent } from '@libs/api-gateway';
import { formatJSONResponse } from '@libs/api-gateway';
import { middyfy } from '@libs/lambda';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';
import { S3Client, GetObjectCommand, S3 } from '@aws-sdk/client-s3';
import { SSM } from 'aws-sdk';
import schema from './schema';

const requestDownloadURL: ValidatedEventAPIGatewayProxyEvent<typeof schema> = async (event) => {
  try {
    const body = typeof event.body === 'string' ? JSON.parse(event.body) : event.body;
    const ssm = new SSM();

    const accessKeyId = (await ssm.getParameter({ Name: '/lambda-s3-signing/access-key-id' }).promise()).Parameter.Value;
    const secretAccessKey = (await ssm.getParameter({ Name: '/lambda-s3-signing/secret-access-key', WithDecryption: true }).promise()).Parameter
      .Value;
    const region = (await ssm.getParameter({ Name: '/lambda-s3-signing/region' }).promise()).Parameter.Value;

    const s3 = new S3Client({ region, credentials: { accessKeyId, secretAccessKey } });

    const s3ParamsForDownload = {
      Bucket: process.env.S3_BUCKET_NAME,
      Key: body.folder ? `${body.folder}/${body.storageIdentifier}` : body.storageIdentifier,
    };

    // Request the signed URL from S3
    const command = new GetObjectCommand(s3ParamsForDownload);
    const downloadUrl = await getSignedUrl(s3, command, { expiresIn: 7 * 24 * 60 * 60 });

    return formatJSONResponse({
      downloadUrl,
      fileKey: body.folder ? `${body.folder}/${body.storageIdentifier}` : body.storageIdentifier,
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
        message: `An error occurred in the requestDownloadURL Lambda Handler: ${err}`,
        event,
      }),
    };
  }
};

export const main = middyfy(requestDownloadURL);
