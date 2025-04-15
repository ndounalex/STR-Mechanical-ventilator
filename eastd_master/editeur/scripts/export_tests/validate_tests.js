const Ajv = require('ajv');
const fs = require('fs');

var ajv = new Ajv();
var schema = JSON.parse(fs.readFileSync('spec.schema.json', 'utf8'));
var validate = ajv.compile(schema);

var test_files = fs.readdirSync("./eastd_saves");

console.log("Starting test suite...\n");

test_files.forEach(file_path => {
    var data = JSON.parse(fs.readFileSync(`./eastd_saves/${file_path}`, 'utf8')).iastd;
    var valid = validate(data);
    if (valid) {
        console.log(`Test ./eastd_saves/${file_path} successful.`);
    } else {
        console.log(`Test ./eastd_saves/${file_path} unsuccessful.`);
        console.log(validate.errors);
    }
    return valid;
});

console.log();

console.log("Test suite completed.");
