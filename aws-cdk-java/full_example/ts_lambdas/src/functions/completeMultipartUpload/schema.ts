export default {
  type: 'object',
  properties: {
    storageIdentifier: { type: 'string' },
    uploadId: { type: 'string' },
    parts: { type: 'array' },
  },
  required: ['storageIdentifier', 'uploadId', 'parts'],
} as const;
