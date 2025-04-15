// Generated automatically by nearley, version 2.19.3
// http://github.com/Hardmath123/nearley
(function () {
function id(x) { return x[0]; }

const moo = require('moo')
let lexer = moo.compile({
    space: {match: /\s+/, lineBreaks: true},
	identifier: /[a-zA-Z][a-zA-Z0-9]*/,
    number: /[0-9]+/,
    string: /"(?:\\["bfnrt\/\\]|\\u[a-fA-F0-9]{4}|[^"\\])*"/,
    '{' : '{',
    '}' : '}',
    '$' : '$',
    '(' : '(',
    ')' : ')',
    '_' : '_',
    ',' : ',',
    ':' : ':',
    '^' : '^',
    '..': '..',
	myError: moo.error
})

var patron_trace_grammar_object = {
    Lexer: lexer,
    ParserRules: [
    {"name": "block$ebnf$1$subexpression$1", "symbols": ["event_structure"]},
    {"name": "block$ebnf$1$subexpression$1", "symbols": ["block"]},
    {"name": "block$ebnf$1", "symbols": ["block$ebnf$1$subexpression$1"]},
    {"name": "block$ebnf$1$subexpression$2", "symbols": ["event_structure"]},
    {"name": "block$ebnf$1$subexpression$2", "symbols": ["block"]},
    {"name": "block$ebnf$1", "symbols": ["block$ebnf$1", "block$ebnf$1$subexpression$2"], "postprocess": function arrpush(d) {return d[0].concat([d[1]]);}},
    {"name": "block$ebnf$2$subexpression$1", "symbols": [{"literal":"^"}, (lexer.has("number") ? {type: "number"} : number)]},
    {"name": "block$ebnf$2", "symbols": ["block$ebnf$2$subexpression$1"], "postprocess": id},
    {"name": "block$ebnf$2", "symbols": [], "postprocess": function(d) {return null;}},
    {"name": "block$ebnf$3", "symbols": []},
    {"name": "block$ebnf$3$subexpression$1", "symbols": ["parameterAdomain", "_"]},
    {"name": "block$ebnf$3", "symbols": ["block$ebnf$3", "block$ebnf$3$subexpression$1"], "postprocess": function arrpush(d) {return d[0].concat([d[1]]);}},
    {"name": "block", "symbols": ["_", {"literal":"{"}, "_", "block$ebnf$1", "_", {"literal":"}"}, "_", "block$ebnf$2", "_", "block$ebnf$3"], "postprocess":  
        function(data) { 
            return { 'block' : {'events' : data[3].map((d) => d[0]) , 'repetition' : data[7] ? Number(data[7][1]) : 0 , 'definition_parameter': data[9] ? data[9].map((d)=>d[0]) : []  }} } },
    {"name": "event_structure$ebnf$1", "symbols": []},
    {"name": "event_structure$ebnf$1$subexpression$1", "symbols": [{"literal":"_"}, "parameterTest"]},
    {"name": "event_structure$ebnf$1", "symbols": ["event_structure$ebnf$1", "event_structure$ebnf$1$subexpression$1"], "postprocess": function arrpush(d) {return d[0].concat([d[1]]);}},
    {"name": "event_structure$ebnf$2$subexpression$1$ebnf$1", "symbols": []},
    {"name": "event_structure$ebnf$2$subexpression$1$ebnf$1$subexpression$1", "symbols": ["_", {"literal":","}, "_", "parameter", "_"]},
    {"name": "event_structure$ebnf$2$subexpression$1$ebnf$1", "symbols": ["event_structure$ebnf$2$subexpression$1$ebnf$1", "event_structure$ebnf$2$subexpression$1$ebnf$1$subexpression$1"], "postprocess": function arrpush(d) {return d[0].concat([d[1]]);}},
    {"name": "event_structure$ebnf$2$subexpression$1", "symbols": [{"literal":"("}, "_", "parameter", "event_structure$ebnf$2$subexpression$1$ebnf$1", {"literal":")"}]},
    {"name": "event_structure$ebnf$2", "symbols": ["event_structure$ebnf$2$subexpression$1"], "postprocess": id},
    {"name": "event_structure$ebnf$2", "symbols": [], "postprocess": function(d) {return null;}},
    {"name": "event_structure", "symbols": ["_", (lexer.has("identifier") ? {type: "identifier"} : identifier), "_", "event_structure$ebnf$1", "event_structure$ebnf$2"], "postprocess":  (data) => {
        return {'event':{'event_name' : data[1].value, 'test_parameters' :data[3] ? data[3].map((d) => d[1]) : [], 'parameters' :  data[4] ? [data[4][2]].concat(data[4][3].map((d) => d[3])): [] }}} },
    {"name": "parameter", "symbols": [{"literal":"$"}, (lexer.has("identifier") ? {type: "identifier"} : identifier)], "postprocess": (data) =>  data[0].value+data[1].value},
    {"name": "parameter", "symbols": ["constant"], "postprocess": id},
    {"name": "parameterTest", "symbols": [{"literal":"$"}, (lexer.has("identifier") ? {type: "identifier"} : identifier)], "postprocess": (data) =>  data[0].value+data[1].value},
    {"name": "parameterTest", "symbols": [(lexer.has("number") ? {type: "number"} : number)], "postprocess": (data) => Number(data[0].value)},
    {"name": "parameterAdomain", "symbols": ["_", "parameter", "_", {"literal":":"}, "_", (lexer.has("number") ? {type: "number"} : number), "_", {"literal":".."}, "_", (lexer.has("number") ? {type: "number"} : number)], "postprocess":  (data) => {
        return {'parameter_name': data[1] , 'domainLowerBound' : Number(data[5].value) , 'domainUpperBound' : Number(data[9].value)}} },
    {"name": "constant", "symbols": [(lexer.has("number") ? {type: "number"} : number)], "postprocess": (data) => Number(data[0].value)},
    {"name": "constant", "symbols": [(lexer.has("string") ? {type: "string"} : string)], "postprocess": (data) => {return JSON.parse(data[0].value);}},
    {"name": "_$ebnf$1", "symbols": []},
    {"name": "_$ebnf$1", "symbols": ["_$ebnf$1", /[ \t\n]/], "postprocess": function arrpush(d) {return d[0].concat([d[1]]);}},
    {"name": "_", "symbols": ["_$ebnf$1"]}
]
  , ParserStart: "block"
}
if (typeof module !== 'undefined'&& typeof module.exports !== 'undefined') {
   module.exports = patron_trace_grammar_object;
} else {
   window.patron_trace_grammar_object = patron_trace_grammar_object;
}
})();
