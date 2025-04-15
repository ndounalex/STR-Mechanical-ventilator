const INTERLEAVE = "Interleave",
	INTERLEAVE_SYMBOL = " ||| ";

class Interleave extends Tab {

	constructor(id = window.id.get(), parent = null, attributes = [], code = "") {
		super(id, parent, attributes, code);
	}

	toHtml() {
		return INTERLEAVE_SYMBOL;
	}

	getType() {
		return INTERLEAVE;
	}

	toPanel() {
		return this.defaultBinaryPanel()();
	}

	save() {
		var interleave = super.save();
		interleave.class = Item.TYPES.INTERLEAVE;
		return interleave;
	}

	load(interleave) {
		super.load(interleave);
	}

	export(min_domain, max_domain, parameter_name, test_parameter, iD, index, external_parameter_name, test = false, nary = false, operationIndex = 0, operations_maxLength = 0, operationID = null) {
		operations_maxLength = this.parent.items.length;
		return super.naryExport(min_domain, max_domain, parameter_name, test_parameter, iD, index, external_parameter_name, test, nary, operationIndex, operations_maxLength, operationID);
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedInterleave(copiedParent, tab.attributes, tab.code);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedInterleave extends CopiedTab {
	constructor(parent = null, attributes = [], code = "") {
		super(parent, attributes, code);
	}

	paste(parent) {
		return new Interleave(window.id.get(), parent, this.attributes, this.code);
	}
}
