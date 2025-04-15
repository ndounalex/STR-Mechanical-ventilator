Thanks to 


https://github.com/fvigneault


for this package, which was part of this project for a long time.

A small test project that runs the exported iASTD json object from save files of eASTD against the json schema for the export.
Detects small structural errors.

More precisely, it takes all the files in the `eastd_saves/` directory and run `ajv` on their `iastd` property to validate against the schema in `spec.new.schema.json`.

Run with :
* `node validate_tests.js`
