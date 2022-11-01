export default {
  type: 'object',
  properties: {
    storageIdentifier: { type: 'string' },
    fileType: { type: 'string' },
    folder: { type: 'string' },
    uploadId: { type: 'string' },
    index: { type: 'number' },
  },
  required: ['storageIdentifier', 'fileType', 'uploadId', 'index'],
} as const;
