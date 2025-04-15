// Generated automatically by nearley, version 2.19.3
// http://github.com/Hardmath123/nearley
(function () {
function id(x) { return x[0]; }

//const moo = require('moo')
let lexer = moo.compile({
    space: {match: /\s+/, lineBreaks: true},
    integer: /[0-9]+/,
	',': ',',
	'[': '[',
	']': ']',
	'{': '{',
	'}': '}',
	'(': '(',
	')': ')',
	'+': '+',
	'-': '-',
	']': ']',
	']': ']',
	']': ']',
	']': ']',
	myError: moo.error
})

var integer_domain_grammar_object = {
    Lexer: lexer,
    ParserRules: [
    {"name": "integer_domain", "symbols": ["_", (lexer.has("integer") ? {type: "integer"} : integer), "_"], "postprocess": function(d) { return d[1].value; }},
    {"name": "integer_domain", "symbols": ["integer_domain", {"literal":"+"}, "integer_domain"], "postprocess": function(d) { return [d[0], d[2]]; }},
    {"name": "integer_domain", "symbols": ["integer_domain", {"literal":"-"}, "integer_domain"], "postprocess": function(d) { return [d[0], d[2]]; }},
    {"name": "integer_domain", "symbols": ["_", {"literal":"("}, "integer_domain", {"literal":")"}, "_"], "postprocess": function(d) { return d[2]; }},
    {"name": "integer_domain", "symbols": ["_", "integer_domain_list", "_"], "postprocess": function(d) { return d[1]; }},
    {"name": "integer_domain", "symbols": ["_", "integer_domain_range", "_"], "postprocess": function(d) { return d[1]; }},
    {"name": "integer_domain_range", "symbols": [{"literal":"["}, "_", (lexer.has("integer") ? {type: "integer"} : integer), "_", {"literal":","}, "_", (lexer.has("integer") ? {type: "integer"} : integer), "_", {"literal":"]"}], "postprocess": function(d) { return [d[2].value, d[6].value]; }},
    {"name": "integer_domain_list", "symbols": [{"literal":"{"}, "_", "integer_domain_list_content", "_", {"literal":"}"}], "postprocess": function(d) { return d[2]; }},
    {"name": "integer_domain_list_content", "symbols": [(lexer.has("integer") ? {type: "integer"} : integer), "_", {"literal":","}, "_", "integer_domain_list_content", "_"], "postprocess": function(d) { return [d[0].value].concat(d[4]); }},
    {"name": "integer_domain_list_content", "symbols": [(lexer.has("integer") ? {type: "integer"} : integer), "_", {"literal":","}, "_"], "postprocess": function(d) { return d[0].value; }},
    {"name": "integer_domain_list_content", "symbols": [(lexer.has("integer") ? {type: "integer"} : integer), "_"], "postprocess": function(d) { return d[0].value; }},
    {"name": "_", "symbols": []},
    {"name": "_", "symbols": [(lexer.has("space") ? {type: "space"} : space)], "postprocess": function(d) { return null; }}
]
  , ParserStart: "integer_domain"
}
if (typeof module !== 'undefined'&& typeof module.exports !== 'undefined') {
   module.exports = integer_domain_grammar_object;
} else {
   window.integer_domain_grammar_object = integer_domain_grammar_object;
}
})();
