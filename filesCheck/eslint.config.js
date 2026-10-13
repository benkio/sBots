'use strict';

const defineConfig = require('eslint/config').defineConfig;
const gts = require('gts');

module.exports = defineConfig([
  { ignores: ['build/', 'coverage/', '**/node_modules/'] },
  ...gts,
  {
    files: ['**/*.ts'],
    rules: {
      'no-unused-vars': 'off',
      '@typescript-eslint/no-unused-vars': [
        'warn',
        {
          argsIgnorePattern: '^_',
          varsIgnorePattern: '^_',
          caughtErrorsIgnorePattern: '^_',
        },
      ],
    },
  },
]);
