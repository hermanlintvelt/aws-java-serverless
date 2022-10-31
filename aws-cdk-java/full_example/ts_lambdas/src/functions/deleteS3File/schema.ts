export default {
  type: 'object',
  properties: {
    storageIdentifier: { type: 'string' },
  },
  required: ['storageIdentifier'],
} as const;
