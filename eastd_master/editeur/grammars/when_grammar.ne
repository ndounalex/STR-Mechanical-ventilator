# http://www.json.org/
# http://www.asciitable.com/
@{%
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

%}

@lexer lexer

when -> conjonction _ (%or conjonction):*  {% (data)=> { return [data[0]].concat(data[2].map(d=>d[1])) } %}

conjonction -> _ equality _ (%and _ equality _ ):* {%
    (data)=> { return [data[1]].concat(data[3].map((d)=>d[2])) }
%}
equality -> objet_attribute _ "=" _ objet_attribute {% (d)=> {return {"first":d[0],"second":d[4]} }  %} 
identifier -> %identifier {% function(d) { return d[0].value } %}
objet_attribute -> identifier ("." identifier):*   {%
    (data)=> { return {'base_identifier': data[0],'attributes_list':data[1].map((d)=>d[1])}}
%}

_ -> null | %space {% function(d) { return null; } %}