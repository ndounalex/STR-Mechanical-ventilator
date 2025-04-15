const AUTOMATON = "Automaton",
	AUTOMATON_SYMBOL = "aut";

class Automaton extends Tab {

	constructor(id = window.id.get(), parent = null, attributes = [], code = "") {
		super(id, parent, attributes, code);
	}

	delete(force = false) {
		if (!force) {
			for (var i = 0; i < this.parent.items.length; i++) {
				if (this.parent.items[i] instanceof State) {
					alert("Please remove all states before removing this tab");
					return false;
				}
			}
		}
		return super.delete();
	}

	getType() {
		return AUTOMATON;
	}

	toHtml() {
		return AUTOMATON_SYMBOL;
	}

	save() {
		var automaton = super.save();
		automaton.class = Item.TYPES.AUTOMATON;
		return automaton;
	}

	export(test_parameter, parameter_name, external_parameter_name) {
		let serialize = super.defaultExport();
		serialize.states = [];
		serialize.transitions = [];
		serialize.shallow_final_state_names = [];
		serialize.deep_final_state_names = [];
		for (let i = 0; i < this.parent.items.length; i++) {
			var item = this.parent.items[i];
			// State
			if (item instanceof State) {
				serialize.states.push(item.export());

				// State is Final
				if (item.final) {
					serialize.shallow_final_state_names.push(item.label);
				}
				// Initial State
				if (item.initial) {
					serialize.initial_state_name = item.label;
				}

				// ASTD
			} else if (item instanceof ASTD) {
				let astd = item.texport(0, test_parameter, null, null, parameter_name, null, external_parameter_name);
				serialize.states.push({
					name: astd.name,
					astd,
					entry_code: item.entryCode,
					stay_code: item.stayCode,
					exit_code: item.exitCode
				});

				// Final
				if (item.state === "shallow-final") {
					serialize.shallow_final_state_names.push(astd.name);
				} else if (item.state === "deep-final") {
					serialize.deep_final_state_names.push(astd.name);
				}
				// Initial State
				if (item.initial) {
					serialize.initial_state_name = astd.name;
				}
			}
		}

		// Export Transition
		this.parent.getFile().links.filter((link) => {
			return link.source.parent === this.parent && link.destination.parent === this.parent 			// local
				|| link.source.parent.parent === this.parent && link.destination.parent === this.parent 	// from sub
				|| link.source.parent === this.parent && link.destination.parent.parent === this.parent; 	// to sub
		}).forEach((link) => {
			serialize.transitions.push(link.export(test_parameter, parameter_name, external_parameter_name)); // Push Transition
		});

		return serialize;
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedAutomaton(copiedParent, tab.attributes, tab.code);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedAutomaton extends CopiedTab {
	constructor(parent = null, attributes = [], code = "") {
		super(parent, attributes, code);
	}

	paste(parent) {
		return new Automaton(window.id.get(), parent, this.attributes, this.code, this.interruptCode, this.label);
	}
}