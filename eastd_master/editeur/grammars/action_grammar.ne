# http://www.json.org/
# http://www.asciitable.com/
@{%
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
	myError: moo.error,
})

%}

@lexer lexer
 
action  -> _ module "." identifier _ "(" _ (_ ("!"):? parameter _  ("," _ ("!"):? parameter _ ):*):? ")" _ {%
	(data)=> { return {'target':'OCaml', 'module': data[1], 'function': data[3],'parameters_list': data[7] ?[data[7][2]].concat(data[7][4].map((d)=>d[3])):[]}}
%}
| _ module ":" ":" identifier _ "(" _ (_ ("&"):? parameter _  ("," _ ("&"):? parameter _ ):*):? ")" _ {%
  	(data)=> { return {'target':'C++', 'module': data[1], 'function': data[4],'parameters_list': data[8] ?[data[8][2]].concat(data[8][4].map((d)=>d[3])):[]}}
  %}
| _ %inline _ {%    (d) => {    return {'inline_code':true}    }    %}

identifier -> %identifier {% function(d) { return d[0].value } %}
objet_attribute -> identifier ("." identifier):+  {%
    (data)=> { return {'base_identifier': data[0],'attributes_list':data[1].map((d)=>d[1])}}
%}

module -> %module {%    function(d) {    return d[0].value    }    %}
				   
parameter -> identifier  {%    (d) => {    return {"type":"identifier","value":d[0]}    }    %}
| objet_attribute {% (d) => {return {"type":"member_accessor","value":d[0]}  } %}

_ -> null | %space {%    function(d) {    return null;    }    %}