// Generated automatically by nearley, version 2.19.3
// http://github.com/Hardmath123/nearley
(function () {
function id(x) { return x[0]; }

//const moo = require('moo')
let lexer = moo.compile({
    space: {match: /\s+/, lineBreaks: true},
	'.': '.',
	and: /&{1,2}/,
	or: /\|{1,2}/,
	'=': '=',
	identifier: /[a-z][a-zA-Z0-9_]*/,
	myError: moo.error
})

var when_grammar_object = {
    Lexer: lexer,
    ParserRules: [
    {"name": "when$ebnf$1", "symbols": []},
    {"name": "when$ebnf$1$subexpression$1", "symbols": [(lexer.has("or") ? {type: "or"} : or), "conjonction"]},
    {"name": "when$ebnf$1", "symbols": ["when$ebnf$1", "when$ebnf$1$subexpression$1"], "postprocess": function arrpush(d) {return d[0].concat([d[1]]);}},
    {"name": "when", "symbols": ["conjonction", "_", "when$ebnf$1"], "postprocess": (data)=> { return [data[0]].concat(data[2].map(d=>d[1])) }},
    {"name": "conjonction$ebnf$1", "symbols": []},
    {"name": "conjonction$ebnf$1$subexpression$1", "symbols": [(lexer.has("and") ? {type: "and"} : and), "_", "equality", "_"]},
    {"name": "conjonction$ebnf$1", "symbols": ["conjonction$ebnf$1", "conjonction$ebnf$1$subexpression$1"], "postprocess": function arrpush(d) {return d[0].concat([d[1]]);}},
    {"name": "conjonction", "symbols": ["_", "equality", "_", "conjonction$ebnf$1"], "postprocess": 
        (data)=> { return [data[1]].concat(data[3].map((d)=>d[2])) }
        },
    {"name": "equality", "symbols": ["objet_attribute", "_", {"literal":"="}, "_", "objet_attribute"], "postprocess": (d)=> {return {"first":d[0],"second":d[4]} }},
    {"name": "identifier", "symbols": [(lexer.has("identifier") ? {type: "identifier"} : identifier)], "postprocess": function(d) { return d[0].value }},
    {"name": "objet_attribute$ebnf$1", "symbols": []},
    {"name": "objet_attribute$ebnf$1$subexpression$1", "symbols": [{"literal":"."}, "identifier"]},
    {"name": "objet_attribute$ebnf$1", "symbols": ["objet_attribute$ebnf$1", "objet_attribute$ebnf$1$subexpression$1"], "postprocess": function arrpush(d) {return d[0].concat([d[1]]);}},
    {"name": "objet_attribute", "symbols": ["identifier", "objet_attribute$ebnf$1"], "postprocess": 
        (data)=> { return {'base_identifier': data[0],'attributes_list':data[1].map((d)=>d[1])}}
        },
    {"name": "_", "symbols": []},
    {"name": "_", "symbols": [(lexer.has("space") ? {type: "space"} : space)], "postprocess": function(d) { return null; }}
]
  , ParserStart: "when"
}
if (typeof module !== 'undefined'&& typeof module.exports !== 'undefined') {
   module.exports = when_grammar_object;
} else {
   window.when_grammar_object = when_grammar_object;
}
})();
