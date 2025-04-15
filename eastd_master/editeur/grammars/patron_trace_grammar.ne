@{%
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

%}

@lexer lexer



block -> _ "{" _ (event_structure | block ):+ _ "}"  _ ("^" %number):? _ (parameterAdomain _ ):*
            {% 
            function(data) { 
                return { 'block' : {'events' : data[3].map((d) => d[0]) , 'repetition' : data[7] ? Number(data[7][1]) : 0 , 'definition_parameter': data[9] ? data[9].map((d)=>d[0]) : []  }} } %}

event_structure -> _ %identifier _ ("_"  parameterTest ):*  ("(" _ parameter ( _ "," _ parameter _ ):* ")"):? 
                {% (data) => {
                   return {'event':{'event_name' : data[1].value, 'test_parameters' :data[3] ? data[3].map((d) => d[1]) : [], 'parameters' :  data[4] ? [data[4][2]].concat(data[4][3].map((d) => d[3])): [] }}} %}
               


parameter -> "$" %identifier {% (data) =>  data[0].value+data[1].value %} 
            | constant {% id %}

parameterTest -> "$" %identifier {% (data) =>  data[0].value+data[1].value %}
                | %number  {% (data) => Number(data[0].value) %}

parameterAdomain -> _ parameter _ ":" _ %number _ ".." _ %number {% (data) => {
    return {'parameter_name': data[1] , 'domainLowerBound' : Number(data[5].value) , 'domainUpperBound' : Number(data[9].value)}} %}


constant -> %number  {% (data) => Number(data[0].value) %}
            | %string {% (data) => {return JSON.parse(data[0].value);} %}

_ -> [ \t\n]:*