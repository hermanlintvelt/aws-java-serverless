import type { ValidatedEventAPIGatewayProxyEvent } from '@libs/api-gateway';
import { formatJSONResponse } from '@libs/api-gateway';
import { middyfy } from '@libs/lambda';
import { S3 } from 'aws-sdk';
import schema from './schema';
import { SSM } from 'aws-sdk';

import { contentType } from 'mime-types';
import { GetObjectCommand, S3Client } from '@aws-sdk/client-s3';
import { getSignedUrl } from '@aws-sdk/s3-request-presigner';

const completeMultipartUpload: ValidatedEventAPIGatewayProxyEvent<typeof schema> = async (event) => {
  try {
    const body = typeof event.body === 'string' ? JSON.parse(event.body) : event.body;
    const mimeType = contentType(body.fileType);

    const ssm = new SSM();
    const accessKeyId = (await ssm.getParameter({ Name: '/lambda-s3-signing/access-key-id' }).promise()).Parameter.Value;
    const secretAccessKey = (await ssm.getParameter({ Name: '/lambda-s3-signing/secret-access-key', WithDecryption: true }).promise()).Parameter
      .Value;
    const region = (await ssm.getParameter({ Name: '/lambda-s3-signing/region' }).promise()).Parameter.Value;

    const s3Client = new S3({ credentials: { accessKeyId, secretAccessKey }, region });
    const client = new S3Client({ credentials: { accessKeyId, secretAccessKey }, region });
    const fileKey = body.storageIdentifier;

    // Request the signed URL from S3
    const completeCommand = {
      Bucket: process.env.S3_BUCKET_NAME,
      Key: fileKey,
      UploadId: body.uploadId,
      MultipartUpload: { Parts: body.parts },
    };
    console.log(completeCommand);
    let url;
    let downloadUrl;
    try {
      url = await s3Client.completeMultipartUpload(completeCommand).promise();
      const s3ParamsForDownload = {
        Bucket: process.env.S3_BUCKET_NAME,
        Key: body.storageIdentifier,
      };

      // Request the signed Download URL from S3
      const command = new GetObjectCommand(s3ParamsForDownload);
      downloadUrl = await getSignedUrl(client, command, { expiresIn: 7 * 24 * 60 * 60 });
    } catch (err) {
      console.error(`S3 error: ${err}`);
      throw Error(`S3 error: ${err}`);
    }

    return formatJSONResponse({
      downloadUrl,
      contentType: mimeType,
      fileKey,
      event,
    });
  } catch (err) {
    console.error(err);
    return {
      headers: {
        'Access-Control-Allow-Headers': 'Content-Type,X-Amz-Date,Authorization,X-Api-Key,Origin,Accept,Accept-Encoding,Host,User-Agent',
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'OPTIONS,POST,GET',
      },
      statusCode: err.statusCode,
      body: JSON.stringify({
        message: `An error occurred in the completeMultipartUpload Lambda Handler: ${err}`,
        event,
      }),
    };
  }
};

export const main = middyfy(completeMultipartUpload);
