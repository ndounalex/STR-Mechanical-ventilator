// File that contains all library imports needed by the project and references so we don't have to add them separately in each file
// All imports done here are accessible in any file that is part of the folder "editeur"
window.$ = window.jQuery = require("jquery");
require("jquery-ui-bundle");
require("jquery-contextmenu");
require("jsplumb");
require("bootstrap");
const moo = require("moo"),
	nearley = require("nearley"),
	toastr = require("toastr"),
	Ajv = require("ajv"),
	html2canvas = require("html2canvas"),
	ipc = require("electron").ipcRenderer,
	fs = require("fs"),
	path = require("path"),
	{
		fork, spawn
	} = require("child_process");
window.action_grammar_object = require("./js/grammar/action_grammar.js");
window.call_parameters_grammar_object = require("./js/grammar/call_argument_grammar.js");
window.event_name_grammar_object = require("./js/grammar/event_name_grammar.js");
window.event_parameters_grammar_object = require("./js/grammar/event_parameters_grammar.js");
window.integer_domain_grammar_object = require("./js/grammar/integer_domain_grammar.js");
window.when_grammar_object = require("./js/grammar/when_grammar.js");
window.patron_trace_grammar_object = require("./js/grammar/patron_trace_grammar.js");