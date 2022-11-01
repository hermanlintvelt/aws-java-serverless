export default {
  type: 'object',
  properties: {
    storageIdentifier: { type: 'string' },
    folder: { type: 'string' },
  },
  required: ['storageIdentifier'],
} as const;
