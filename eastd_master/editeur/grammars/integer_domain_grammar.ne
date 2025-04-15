# http://www.json.org/
# http://www.asciitable.com/
@{%
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

%}

@lexer lexer

integer_domain -> _ %integer _ {% function(d) { return d[1].value; } %}
                | integer_domain "+" integer_domain {% function(d) { return [d[0], d[2]]; } %}
                | integer_domain "-" integer_domain {% function(d) { return [d[0], d[2]]; } %}
                | _ "(" integer_domain ")" _ {% function(d) { return d[2]; } %}
                | _ integer_domain_list _ {% function(d) { return d[1]; } %}
                | _ integer_domain_range _ {% function(d) { return d[1]; } %}

integer_domain_range -> "[" _ %integer _ "," _ %integer _ "]" {% function(d) { return [d[2].value, d[6].value]; } %}

integer_domain_list -> "{" _ integer_domain_list_content _ "}" {% function(d) { return d[2]; } %}

integer_domain_list_content -> %integer _ "," _ integer_domain_list_content _ {% function(d) { return [d[0].value].concat(d[4]); } %}
                             | %integer _ "," _ {% function(d) { return d[0].value; } %}
                             | %integer _ {% function(d) { return d[0].value; } %}

_ -> null | %space {% function(d) { return null; } %}