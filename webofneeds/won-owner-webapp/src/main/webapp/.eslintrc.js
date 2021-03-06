module.exports = {
  root: true,
  extends: ["plugin:prettier/recommended", "eslint:recommended"],
  parser: "babel-eslint",
  env: {
    browser: true,
    es6: true,
  },
  parserOptions: {
    sourceType: "module",
    ecmaVersion: 2017,
  },
  rules: {
    //Deactivating console errors
    "no-console": 0,
    "no-var": "error",
    "no-restricted-syntax": [
      "warn",
      {
        selector: "ForStatement",
        message: "Don't use `for(let i = ...` statements",
      },
    ],
  },
  globals: {
    Exception: true,
  },
};
