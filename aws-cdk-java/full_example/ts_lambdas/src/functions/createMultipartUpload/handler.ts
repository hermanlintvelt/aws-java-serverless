import type { ValidatedEventAPIGatewayProxyEvent } from '@libs/api-gateway';
import { formatJSONResponse } from '@libs/api-gateway';
import { middyfy } from '@libs/lambda';
import { S3 } from 'aws-sdk';
import schema from './schema';
import { SSM } from 'aws-sdk';
import { v4 as uuidv4 } from 'uuid';
import { contentType } from 'mime-types';

const createMultipartUpload: ValidatedEventAPIGatewayProxyEvent<typeof schema> = async (event) => {
  try {
    const body = typeof event.body === 'string' ? JSON.parse(event.body) : event.body;

    const ssm = new SSM();

    const accessKeyId = (await ssm.getParameter({ Name: '/lambda-s3-signing/access-key-id' }).promise()).Parameter.Value;
    const secretAccessKey = (await ssm.getParameter({ Name: '/lambda-s3-signing/secret-access-key', WithDecryption: true }).promise()).Parameter
      .Value;
    const region = (await ssm.getParameter({ Name: '/lambda-s3-signing/region' }).promise()).Parameter.Value;

    const s3 = new S3({ region, credentials: { accessKeyId, secretAccessKey } });
    const mimeType = contentType(body.fileType);
    const fileKey = uuidv4() + body.fileType;

    const command = {
      ACL: 'private',
      ContentType: mimeType,
      Bucket: process.env.S3_BUCKET_NAME,
      Key: body.folder ? `${body.folder}/${fileKey}` : fileKey,
    };

    // Request the delete
    const res = await s3.createMultipartUpload(command).promise();

    return formatJSONResponse({
      res,
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
        message: `An error occurred in the createMultipartUpload Lambda Handler: ${err}`,
        event,
      }),
    };
  }
};

export const main = middyfy(createMultipartUpload);
