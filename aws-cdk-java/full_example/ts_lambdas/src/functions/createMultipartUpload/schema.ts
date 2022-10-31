export default {
  type: 'object',
  properties: {
    fileType: { type: 'string' },
    folder: {type: 'string' },
  },

  required: ['fileType'],
} as const;
