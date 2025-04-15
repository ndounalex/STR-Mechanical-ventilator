// Generated automatically by nearley, version 2.19.3
// http://github.com/Hardmath123/nearley
(function () {
function id(x) { return x[0]; }

//const moo = require('moo')
let lexer = moo.compile({
    space: {match: /\s+/, lineBreaks: true},
    number: /-?(?:[0-9]|[1-9][0-9]+)(?:\.[0-9]+)?(?:[eE][-+]?[0-9]+)?\b/,
    string: /"(?:\\["bfnrt\/\\]|\\u[a-fA-F0-9]{4}|[^"\\])*"/,
    '{': '{',
    '}': '}',
    '[': '[',
    ']': ']',
    ',': ',',
    ':': ':',
	'.': '.',
	'?': '?',
	'_': '_',
    true: 'true',
    false: 'false',
    null: 'null',
	identifier: /[a-z][a-zA-Z0-9_]*/,
	type_identifier: /[A-Z][a-zA-Z0-9_]*/,
	myError: moo.error
})




function extractPair(kv, output) {
    if(kv[0]) { output[kv[0]] = kv[1]; }
}

function extractObject(d) {
    let output = {};

    extractPair(d[2], output);

    for (let i in d[3]) {
        extractPair(d[3][i][3], output);
    }

    return output;
}

function extractArray(d) {
    let output = [d[2]];

    for (let i in d[3]) {
        output.push(d[3][i][3]);
    }

    return output;
}

var event_parameters_grammar_object = {
    Lexer: lexer,
    ParserRules: [
    {"name": "event_parameters$ebnf$1", "symbols": []},
    {"name": "event_parameters$ebnf$1$subexpression$1", "symbols": [{"literal":","}, "_", "parameter", "_"]},
    {"name": "event_parameters$ebnf$1", "symbols": ["event_parameters$ebnf$1", "event_parameters$ebnf$1$subexpression$1"], "postprocess": function arrpush(d) {return d[0].concat([d[1]]);}},
    {"name": "event_parameters", "symbols": ["_", "parameter", "_", "event_parameters$ebnf$1"], "postprocess": 
        (data)=> { return [data[1]].concat(data[3].map((d)=>d[2])) }
        },
    {"name": "parameter", "symbols": ["identifier"], "postprocess": (d) => {return {"type":"identifier","value":d[0]}  }},
    {"name": "parameter", "symbols": ["json"], "postprocess": (d) => {return {"type":"json","value":d[0]}  }},
    {"name": "parameter", "symbols": ["declaration_variable_with_type"], "postprocess": (d) => {return {"type":"capture","value":d[0]}  }},
    {"name": "parameter", "symbols": ["objet_attribute"], "postprocess": (d) => {return {"type":"member_accessor","value":d[0]}  }},
    {"name": "parameter", "symbols": [{"literal":"_"}], "postprocess": () => {return  {"type":"joker","value":null}  }},
    {"name": "identifier", "symbols": [(lexer.has("identifier") ? {type: "identifier"} : identifier)], "postprocess": function(d) { return d[0].value }},
    {"name": "objet_attribute$ebnf$1$subexpression$1", "symbols": [{"literal":"."}, "identifier"]},
    {"name": "objet_attribute$ebnf$1", "symbols": ["objet_attribute$ebnf$1$subexpression$1"]},
    {"name": "objet_attribute$ebnf$1$subexpression$2", "symbols": [{"literal":"."}, "identifier"]},
    {"name": "objet_attribute$ebnf$1", "symbols": ["objet_attribute$ebnf$1", "objet_attribute$ebnf$1$subexpression$2"], "postprocess": function arrpush(d) {return d[0].concat([d[1]]);}},
    {"name": "objet_attribute", "symbols": ["identifier", "objet_attribute$ebnf$1"], "postprocess": 
        (data)=> { return {'base_identifier': data[0],'attributes_list':data[1].map((d)=>d[1])}}
        },
    {"name": "declaration_variable_with_type", "symbols": [{"literal":"?"}, "identifier", {"literal":":"}, "type"], "postprocess": 
        (data)=> { return {'identifier': data[1],'type':data[3]}}
        },
    {"name": "type", "symbols": ["identifier"], "postprocess": id},
    {"name": "type", "symbols": [(lexer.has("type_identifier") ? {type: "type_identifier"} : type_identifier)], "postprocess": function(d) { return d[0].value }},
    {"name": "json", "symbols": ["_", "value", "_"], "postprocess": function(d) { return d[1]; }},
    {"name": "object", "symbols": [{"literal":"{"}, "_", {"literal":"}"}], "postprocess": function(d) { return {}; }},
    {"name": "object$ebnf$1", "symbols": []},
    {"name": "object$ebnf$1$subexpression$1", "symbols": ["_", {"literal":","}, "_", "pair"]},
    {"name": "object$ebnf$1", "symbols": ["object$ebnf$1", "object$ebnf$1$subexpression$1"], "postprocess": function arrpush(d) {return d[0].concat([d[1]]);}},
    {"name": "object", "symbols": [{"literal":"{"}, "_", "pair", "object$ebnf$1", "_", {"literal":"}"}], "postprocess": extractObject},
    {"name": "array", "symbols": [{"literal":"["}, "_", {"literal":"]"}], "postprocess": function(d) { return []; }},
    {"name": "array$ebnf$1", "symbols": []},
    {"name": "array$ebnf$1$subexpression$1", "symbols": ["_", {"literal":","}, "_", "value"]},
    {"name": "array$ebnf$1", "symbols": ["array$ebnf$1", "array$ebnf$1$subexpression$1"], "postprocess": function arrpush(d) {return d[0].concat([d[1]]);}},
    {"name": "array", "symbols": [{"literal":"["}, "_", "value", "array$ebnf$1", "_", {"literal":"]"}], "postprocess": extractArray},
    {"name": "value", "symbols": ["object"], "postprocess": id},
    {"name": "value", "symbols": ["array"], "postprocess": id},
    {"name": "value", "symbols": ["number"], "postprocess": id},
    {"name": "value", "symbols": ["string"], "postprocess": id},
    {"name": "value", "symbols": [{"literal":"true"}], "postprocess": function(d) { return true; }},
    {"name": "value", "symbols": [{"literal":"false"}], "postprocess": function(d) { return false; }},
    {"name": "value", "symbols": [{"literal":"null"}], "postprocess": function(d) { return null; }},
    {"name": "number", "symbols": [(lexer.has("number") ? {type: "number"} : number)], "postprocess": function(d) { return parseFloat(d[0].value) }},
    {"name": "string", "symbols": [(lexer.has("string") ? {type: "string"} : string)], "postprocess": function(d) { return JSON.parse(d[0].value) }},
    {"name": "pair", "symbols": ["key", "_", {"literal":":"}, "_", "value"], "postprocess": function(d) { return [d[0], d[4]]; }},
    {"name": "key", "symbols": ["string"], "postprocess": id},
    {"name": "_", "symbols": []},
    {"name": "_", "symbols": [(lexer.has("space") ? {type: "space"} : space)], "postprocess": function(d) { return null; }}
]
  , ParserStart: "event_parameters"
}
if (typeof module !== 'undefined'&& typeof module.exports !== 'undefined') {
   module.exports = event_parameters_grammar_object;
} else {
   window.event_parameters_grammar_object = event_parameters_grammar_object;
}
})();
