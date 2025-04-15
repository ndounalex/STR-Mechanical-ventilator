const FLOW = "Flow",
	FLOW_SYMBOL = " ⋓ ";

class Flow extends Tab {
	constructor(id = window.id.get(), parent = null, attributes = [], code = "") {
		super(id, parent, attributes, code);
	}

	toHtml() {
		return FLOW_SYMBOL;
	}

	getType() {
		return FLOW;
	}

	toPanel() {
		return this.defaultBinaryPanel()();
	}

	save() {
		var choice = super.save();
		choice.class = Item.TYPES.FLOW;
		return choice;
	}

	export(min_domain, max_domain, parameter_name, test_parameter, iD, index, external_parameter_name, test = false, nary = false, operationIndex = 0, operations_maxLength = 0, operationID = null) {
		operations_maxLength = this.parent.items.length;
		return super.naryExport(min_domain, max_domain, parameter_name, test_parameter, iD, index, external_parameter_name, test, nary, operationIndex, operations_maxLength, operationID);
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedFlow(copiedParent, tab.attributes, tab.code);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}

}

// eslint-disable-next-line no-undef
class CopiedFlow extends CopiedTab {
	constructor(parent = null, attributes = [], code = "") {
		super(parent, attributes, code);
	}

	paste(parent) {
		return new Flow(window.id.get(), parent, this.attributes, this.code, this.interruptCode, this.label);
	}
}