# http://www.json.org/
# http://www.asciitable.com/
@{%
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

%}

@lexer lexer

event_parameters  -> _ parameter _  ("," _ parameter _ ):*  {%
    (data)=> { return [data[1]].concat(data[3].map((d)=>d[2])) }
%}
parameter -> identifier {% (d) => {return {"type":"identifier","value":d[0]}  }%} 
| json {% (d) => {return {"type":"json","value":d[0]}  } %} 
| declaration_variable_with_type {% (d) => {return {"type":"capture","value":d[0]}  } %} 
| objet_attribute {% (d) => {return {"type":"member_accessor","value":d[0]}  } %} 
| "_" {%  () => {return  {"type":"joker","value":null}  } %} 

identifier -> %identifier {% function(d) { return d[0].value } %}
objet_attribute -> identifier ("." identifier):+  {%
    (data)=> { return {'base_identifier': data[0],'attributes_list':data[1].map((d)=>d[1])}}
%}

declaration_variable_with_type -> "?" identifier ":" type {%
    (data)=> { return {'identifier': data[1],'type':data[3]}}
%}
type -> identifier {% id %}
        | %type_identifier {% function(d) { return d[0].value } %}

json -> _ value _ {% function(d) { return d[1]; } %} 

object -> "{" _ "}" {% function(d) { return {}; } %}
    | "{" _ pair (_ "," _ pair):* _ "}" {% extractObject %}

array -> "[" _ "]" {% function(d) { return []; } %}
    | "[" _ value (_ "," _ value):* _ "]" {% extractArray %}

		value ->
			  object {% id %}
    | array {% id %}
    | number {% id %}
    | string {% id %}
    | "true" {% function(d) { return true; } %}
    | "false" {% function(d) { return false; } %}
    | "null" {% function(d) { return null; } %}


number -> %number {% function(d) { return parseFloat(d[0].value) } %}

string -> %string {% function(d) { return JSON.parse(d[0].value) } %}

pair -> key _ ":" _ value {% function(d) { return [d[0], d[4]]; } %}

key -> string {% id %}

_ -> null | %space {% function(d) { return null; } %}

@{%

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

%}
