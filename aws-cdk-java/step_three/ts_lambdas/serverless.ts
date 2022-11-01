import type { AWS } from '@serverless/typescript';

import requestPresignedURL from '@functions/requestPresignedURL';
import requestDownloadURL from '@functions/requestDownloadURL';
import deleteS3File from '@functions/deleteS3File';
import createMultipartUpload from '@functions/createMultipartUpload';
import completeMultipartUpload from '@functions/completeMultipartUpload';
import requestMultipartUploadURLs from '@functions/requestMultipartUploadURLs';

const S3Bucket = 'tracto-file-upload-poc';

const serverlessConfiguration: AWS = {
  service: 'tracto-s3-file-upload-lambdas-poc',
  frameworkVersion: '3',
  plugins: ['serverless-esbuild'],
  provider: {
    name: 'aws',
    profile: 'tracto',
    runtime: 'nodejs14.x',
    region: 'us-east-1',
    logs: {
      restApi: true,
    },

    apiGateway: {
      minimumCompressionSize: 1024,
      shouldStartNameWithService: true,
    },
    environment: {
      AWS_NODEJS_CONNECTION_REUSE_ENABLED: '1',
      NODE_OPTIONS: '--enable-source-maps --stack-trace-limit=1000',
      S3_BUCKET_NAME: S3Bucket,
      S3_BUCKET_REGION: 'us-east-1',
      LAMBDA_S3_ACCESS_KEY_ID: '${ssm:/lambda-s3-signing/access-key-id}',
      LAMBDA_S3_SECRET_ACCESS_KEY: '${ssm:/lambda-s3-signing/secret-access-key}',
    },

    iamRoleStatements: [
      {
        Effect: 'Allow',
        Action: ['ssm:GetParameters', 'ssm:GetParameter'],
        Resource: 'arn:aws:ssm:*:*:parameter/lambda-s3-signing/*',
      },
      {
        Effect: 'Allow',
        Action: ['s3:PutObject', 's3:GetObject'],
        Resource: { 'Fn::Join': ['', ['arn:aws:s3:::', S3Bucket, '/*']] },
      },
    ],
  },
  // import the function via paths
  functions: { requestPresignedURL, requestDownloadURL, deleteS3File, createMultipartUpload, completeMultipartUpload, requestMultipartUploadURLs },
  package: { individually: true },
  custom: {
    esbuild: {
      bundle: true,
      minify: false,
      sourcemap: true,
      exclude: ['aws-sdk'],
      target: 'node14',
      define: { 'require.resolve': undefined },
      platform: 'node',
      concurrency: 10,
    },
  },
};

module.exports = serverlessConfiguration;
