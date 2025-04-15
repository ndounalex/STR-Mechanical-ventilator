// Generated automatically by nearley, version 2.19.3
// http://github.com/Hardmath123/nearley
(function () {
function id(x) { return x[0]; }

//const moo = require('moo')
let lexer = moo.compile({
    space: {match: /\s+/, lineBreaks: true},
    '[': '[',
    ']': ']',
    ',': ',',
    ':': ':',
	'.': '.',
	'?': '?',
	'(': '(',
	')': ')',
	'!': '!',
	'&': '&',
    true: 'true',
    false: 'false',
    null: 'null',
	identifier: /[a-z][a-zA-Z0-9_]*/,
	module: /[A-Z][a-zA-Z0-9_]*/,
	inline: /{.*}/,
	myError: moo.error
})

var action_grammar_object = {
    Lexer: lexer,
    ParserRules: [
    {"name": "action$ebnf$1$subexpression$1$ebnf$1$subexpression$1", "symbols": [{"literal":"!"}]},
    {"name": "action$ebnf$1$subexpression$1$ebnf$1", "symbols": ["action$ebnf$1$subexpression$1$ebnf$1$subexpression$1"], "postprocess": id},
    {"name": "action$ebnf$1$subexpression$1$ebnf$1", "symbols": [], "postprocess": function(d) {return null;}},
    {"name": "action$ebnf$1$subexpression$1$ebnf$2", "symbols": []},
    {"name": "action$ebnf$1$subexpression$1$ebnf$2$subexpression$1$ebnf$1$subexpression$1", "symbols": [{"literal":"!"}]},
    {"name": "action$ebnf$1$subexpression$1$ebnf$2$subexpression$1$ebnf$1", "symbols": ["action$ebnf$1$subexpression$1$ebnf$2$subexpression$1$ebnf$1$subexpression$1"], "postprocess": id},
    {"name": "action$ebnf$1$subexpression$1$ebnf$2$subexpression$1$ebnf$1", "symbols": [], "postprocess": function(d) {return null;}},
    {"name": "action$ebnf$1$subexpression$1$ebnf$2$subexpression$1", "symbols": [{"literal":","}, "_", "action$ebnf$1$subexpression$1$ebnf$2$subexpression$1$ebnf$1", "parameter", "_"]},
    {"name": "action$ebnf$1$subexpression$1$ebnf$2", "symbols": ["action$ebnf$1$subexpression$1$ebnf$2", "action$ebnf$1$subexpression$1$ebnf$2$subexpression$1"], "postprocess": function arrpush(d) {return d[0].concat([d[1]]);}},
    {"name": "action$ebnf$1$subexpression$1", "symbols": ["_", "action$ebnf$1$subexpression$1$ebnf$1", "parameter", "_", "action$ebnf$1$subexpression$1$ebnf$2"]},
    {"name": "action$ebnf$1", "symbols": ["action$ebnf$1$subexpression$1"], "postprocess": id},
    {"name": "action$ebnf$1", "symbols": [], "postprocess": function(d) {return null;}},
    {"name": "action", "symbols": ["_", "module", {"literal":"."}, "identifier", "_", {"literal":"("}, "_", "action$ebnf$1", {"literal":")"}, "_"], "postprocess": 
        (data)=> { return {'target':'OCaml', 'module': data[1], 'function': data[3],'parameters_list': data[7] ?[data[7][2]].concat(data[7][4].map((d)=>d[3])):[]}}
        },
    {"name": "action$ebnf$2$subexpression$1$ebnf$1$subexpression$1", "symbols": [{"literal":"&"}]},
    {"name": "action$ebnf$2$subexpression$1$ebnf$1", "symbols": ["action$ebnf$2$subexpression$1$ebnf$1$subexpression$1"], "postprocess": id},
    {"name": "action$ebnf$2$subexpression$1$ebnf$1", "symbols": [], "postprocess": function(d) {return null;}},
    {"name": "action$ebnf$2$subexpression$1$ebnf$2", "symbols": []},
    {"name": "action$ebnf$2$subexpression$1$ebnf$2$subexpression$1$ebnf$1$subexpression$1", "symbols": [{"literal":"&"}]},
    {"name": "action$ebnf$2$subexpression$1$ebnf$2$subexpression$1$ebnf$1", "symbols": ["action$ebnf$2$subexpression$1$ebnf$2$subexpression$1$ebnf$1$subexpression$1"], "postprocess": id},
    {"name": "action$ebnf$2$subexpression$1$ebnf$2$subexpression$1$ebnf$1", "symbols": [], "postprocess": function(d) {return null;}},
    {"name": "action$ebnf$2$subexpression$1$ebnf$2$subexpression$1", "symbols": [{"literal":","}, "_", "action$ebnf$2$subexpression$1$ebnf$2$subexpression$1$ebnf$1", "parameter", "_"]},
    {"name": "action$ebnf$2$subexpression$1$ebnf$2", "symbols": ["action$ebnf$2$subexpression$1$ebnf$2", "action$ebnf$2$subexpression$1$ebnf$2$subexpression$1"], "postprocess": function arrpush(d) {return d[0].concat([d[1]]);}},
    {"name": "action$ebnf$2$subexpression$1", "symbols": ["_", "action$ebnf$2$subexpression$1$ebnf$1", "parameter", "_", "action$ebnf$2$subexpression$1$ebnf$2"]},
    {"name": "action$ebnf$2", "symbols": ["action$ebnf$2$subexpression$1"], "postprocess": id},
    {"name": "action$ebnf$2", "symbols": [], "postprocess": function(d) {return null;}},
    {"name": "action", "symbols": ["_", "module", {"literal":":"}, {"literal":":"}, "identifier", "_", {"literal":"("}, "_", "action$ebnf$2", {"literal":")"}, "_"], "postprocess": 
        (data)=> { return {'target':'C++', 'module': data[1], 'function': data[4],'parameters_list': data[8] ?[data[8][2]].concat(data[8][4].map((d)=>d[3])):[]}}
          },
    {"name": "action", "symbols": ["_", (lexer.has("inline") ? {type: "inline"} : inline), "_"], "postprocess": (d) => { return {'inline_code':true}}},
    {"name": "identifier", "symbols": [(lexer.has("identifier") ? {type: "identifier"} : identifier)], "postprocess": function(d) { return d[0].value }},
    {"name": "objet_attribute$ebnf$1$subexpression$1", "symbols": [{"literal":"."}, "identifier"]},
    {"name": "objet_attribute$ebnf$1", "symbols": ["objet_attribute$ebnf$1$subexpression$1"]},
    {"name": "objet_attribute$ebnf$1$subexpression$2", "symbols": [{"literal":"."}, "identifier"]},
    {"name": "objet_attribute$ebnf$1", "symbols": ["objet_attribute$ebnf$1", "objet_attribute$ebnf$1$subexpression$2"], "postprocess": function arrpush(d) {return d[0].concat([d[1]]);}},
    {"name": "objet_attribute", "symbols": ["identifier", "objet_attribute$ebnf$1"], "postprocess": 
        (data)=> { return {'base_identifier': data[0],'attributes_list':data[1].map((d)=>d[1])}}
        },
    {"name": "module", "symbols": [(lexer.has("module") ? {type: "module"} : module)], "postprocess": function(d) { return d[0].value }},
    {"name": "parameter", "symbols": ["identifier"], "postprocess": (d) => {return {"type":"identifier","value":d[0]}  }},
    {"name": "parameter", "symbols": ["objet_attribute"], "postprocess": (d) => {return {"type":"member_accessor","value":d[0]}  }},
    {"name": "_", "symbols": []},
    {"name": "_", "symbols": [(lexer.has("space") ? {type: "space"} : space)], "postprocess": function(d) { return null; }}
]
  , ParserStart: "action"
}
if (typeof module !== 'undefined'&& typeof module.exports !== 'undefined') {
   module.exports = action_grammar_object;
} else {
   window.action_grammar_object = action_grammar_object;
}
})();
