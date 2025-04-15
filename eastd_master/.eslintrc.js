/* eslint-disable */
module.exports = {
	"root": true,
	"env": {
		"browser": true,
		"es6": true,
		"jquery": true
	},
	"extends": "eslint:recommended",
	"globals": {
		"require": "readonly",
		"process": "readonly",
		"__dirname": "readonly"
	},
	"ignorePatterns": ["iASTD/"],
	"rules": {
		"indent": [
			"warn",
			"tab"
		],
		"linebreak-style": [
			"error",
			"unix"
		],
		"quotes": [
			"warn",
			"double"
		],
		"semi": [
			"error",
			"always"
		],
		"no-extra-parens": [
			"warn",
			"all"
		],
		"no-shadow": "error",
		"eqeqeq": [
			"error",
			"always"
		],
		"no-constructor-return": "error",
		"no-empty-function": [
			"error",
			{ "allow": [] }
		],
		"no-multi-spaces": "warn",
		"no-redeclare": [
			"error",
			{ "builtinGlobals": false }
		],
		"no-self-compare": "error",
		"no-mixed-spaces-and-tabs": "warn",
		"no-use-before-define": "error",

		"block-spacing": [
			"warn",
			"always"
		],
		"brace-style": "warn",
		"comma-dangle": [
			"error",
			"never"
		],
		"comma-spacing": [
			"warn",
			{
				"before": false,
				"after": true
			}
		],
		"func-call-spacing": ["warn", "never"],
		"key-spacing": "warn",
		"keyword-spacing": "warn",
		"lines-between-class-members": "warn",
		"new-parens": "error",
		"no-multiple-empty-lines": ["error", {
			"max": 1,
			"maxEOF": 1
		}],
		"no-trailing-spaces": "warn",
		"no-whitespace-before-property": "warn",
		"object-curly-newline": [
			"warn",
			{
				"multiline": true,
				"minProperties": 2
			}
		],
		"object-property-newline": "warn",
		"object-curly-spacing": [
			"warn",
			"always"
		],
		"one-var": [
			"warn",
			"consecutive"
		],
		"one-var-declaration-per-line": [
			"warn",
			"always"
		],
		"space-before-blocks": "warn",
		"space-before-function-paren": [
			"warn",
			{
				"anonymous": "never",
				"named": "never",
				"asyncArrow": "always"
			}
		],
		"space-infix-ops": "warn",
		"switch-colon-spacing": "error",
		"arrow-body-style": ["warn", "always"],
		"arrow-parens": "warn",
		"arrow-spacing": "warn",
		"no-unused-vars": [
			"error",
			{ "vars": "local" }
		]
	}
};