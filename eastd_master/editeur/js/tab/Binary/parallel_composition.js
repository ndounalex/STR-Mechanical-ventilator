const PARRALLEL_COMPOSITION = "Parallel Composition",
	PARRALLEL_COMPOSITION_SYMBOL = " || ";

class ParallelComposition extends Tab {

	constructor(id = window.id.get(), parent = null, attributes = [], code = "") {
		super(id, parent, attributes, code);
	}

	toHtml() {
		return PARRALLEL_COMPOSITION_SYMBOL;
	}

	getType() {
		return PARRALLEL_COMPOSITION;
	}

	toPanel() {
		return this.defaultBinaryPanel()();
	}

	save() {
		var parallel_composition = super.save();
		parallel_composition.class = Item.TYPES.PARALLEL_COMPOSITION;
		return parallel_composition;
	}

	load(parallel_composition) {
		super.load(parallel_composition);
	}

	export(min_domain, max_domain, parameter_name, test_parameter, iD, index, external_parameter_name, test = false, nary = false, operationIndex = 0, operations_maxLength = 0, operationID = null) {
		operations_maxLength = this.parent.items.length;
		return super.naryExport(min_domain, max_domain, parameter_name, test_parameter, iD, index, external_parameter_name, test, nary, operationIndex, operations_maxLength, operationID);
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedParallelComposition(copiedParent, tab.attributes, tab.code);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedParallelComposition extends CopiedTab {
	constructor(parent = null, attributes = [], code = "") {
		super(parent, attributes, code);
	}

	paste(parent) {
		return new ParallelComposition(window.id.get(), parent, this.attributes, this.code);
	}
}