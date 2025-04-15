// Generated automatically by nearley, version 2.19.0
// http://github.com/Hardmath123/nearley
(function () {
function id(x) { return x[0]; }

//const moo = require('moo')
let lexer = moo.compile({
    space: {match: /\s+/, lineBreaks: true},
	identifier: /[a-zA-Z][a-zA-Z0-9_]*/,
	myError: moo.error
})

var event_name_grammar_object = {
    Lexer: lexer,
    ParserRules: [
    {"name": "identifier", "symbols": ["_", (lexer.has("identifier") ? {type: "identifier"} : identifier), "_"], "postprocess": function(d) { return d[1].value }},
    {"name": "_", "symbols": []},
    {"name": "_", "symbols": [(lexer.has("space") ? {type: "space"} : space)], "postprocess": function(d) { return null; }}
]
  , ParserStart: "identifier"
}
if (typeof module !== 'undefined'&& typeof module.exports !== 'undefined') {
   module.exports = event_name_grammar_object;
} else {
   window.event_name_grammar_object = event_name_grammar_object;
}
})();
