# http://www.json.org/
# http://www.asciitable.com/
@{%
//const moo = require('moo')
let lexer = moo.compile({
    space: {match: /\s+/, lineBreaks: true},
	identifier: /[a-zA-Z][a-zA-Z0-9_]*/,
	myError: moo.error
})

%}

@lexer lexer
 
 identifier -> _ %identifier _ {% function(d) { return d[1].value } %}

_ -> null | %space {% function(d) { return null; } %}
