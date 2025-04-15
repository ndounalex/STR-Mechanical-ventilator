/**
 * Standalone module called to generate event traces and save them in files. It takes the project rootPath as an argument and expects to receive
 * messages containing the trace objects resulting from the parsing of the template provided by the user in eASTD (manage -> traces)
 */
const fs = require("fs");
var events = [],
	currentFilePath = "";

/**
 * Makes the expansion of an events block
 * @param obj
 * @param i
 * @param valuation
 */
function block_expansion(obj, i, valuation = new Map()) {
	if ( i < obj.block.definition_parameter.length ) {
		for (var val = obj.block.definition_parameter[i].domainLowerBound ; val <= obj.block.definition_parameter[i].domainUpperBound ; val++ ) {
			block_expansion(obj, i + 1, valuation.set(obj.block.definition_parameter[i].parameter_name, val));
		}
	} else {
		const repetition = obj.block.repetition !== 0 ? obj.block.repetition : 1;
		for (var h = 0 ; h < repetition;h++) {
			obj.block.events.forEach((event) => {
				expansion(event, valuation); // eslint-disable-line no-use-before-define
			});
		}
	}
}

/**
 * makes the expansion of an individual event, add it to events and write in file if the array has a certain size
 * This behaviour aims to minimize disk accesses to reduce processing time and to avoid memory overflow and string range overflow
 * @param obj
 * @param valuation
 */
function event_expansion(obj, valuation) {
	let event = {
		label: obj.event.event_name,
		arguments: []
	};
	obj.event.test_parameters.forEach((test_param) => {
		if (!Number.isInteger(test_param)) {
			event.label += "_" + valuation.get(test_param);
		} else {
			event.label += "_" + test_param;
		}
	});
	obj.event.parameters.forEach((param) => {
		if (!Number.isInteger(param) && param[0] === "$" ) {
			event.arguments.push(valuation.get(param));
		} else {
			event.arguments.push(param);
		}
	});

	events.push(JSON.stringify(event));
	if (events.length >= 250000) {
		fs.appendFileSync(currentFilePath, events.join("\n") + "\n");
		events.splice(0, events.length);
	}
}

function expansion(obj, valuation) {
	if (obj.block) {
		block_expansion(obj, 0, valuation);
	} else if (obj.event) {
		event_expansion(obj, valuation);
	}
}

/**
 * generate a trace based on the template provided by the content property if there is no error in it
 * @returns {boolean} Returns true if the generation was a success, false otherwise
 */
function generateTrace(data) {
	fs.writeFileSync(currentFilePath, "");
	block_expansion(data, 0);
	fs.appendFileSync(currentFilePath, events.join("\n"));
	events.splice(0, events.length);
}

/**
 * Action to execute when the process receive data from eASTD renderer process. It expects an array containing all traces names and object
 * to use to generate the resulting trace in the right file. Sends to the parent process if the operation was successful or not.
 */
process.on("message", (received) => {
	try {
		const receivedObjs = JSON.parse(received);
		receivedObjs.forEach((obj) => {
			currentFilePath = process.argv[2] + "/" + obj.name + ".txt";
			generateTrace(obj.toGenerate);
		});
		process.send(true);
	} catch (error) {
		process.send(false);
	}
});