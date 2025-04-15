const Q_PARALLEL_COMPOSITION = "Quantified Parallel Composition",
	Q_PARALLEL_COMPOSITION_SYMBOL = " || ";

class QParallelComposition extends QTab {

	constructor(id = window.id.get(), parent = null, attributes = [], code = "", variable = null) {
		super(id, parent, attributes, code, variable);
	}

	get symbol() {
		return Q_PARALLEL_COMPOSITION_SYMBOL;
	}

	getType() {
		return Q_PARALLEL_COMPOSITION;
	}

	save() {
		var Qparallel_composition = super.save();
		Qparallel_composition.class = Item.TYPES.Q_PARALLEL_COMPOSITION;
		return Qparallel_composition;
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedQParallelComposition(copiedParent, tab.attributes, tab.code, tab.variable);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedQParallelComposition extends CopiedQTab {
	constructor(parent = null, attributes = [], code = "", variable = null) {
		super(parent, attributes, code, variable);
	}

	paste(parent) {
		return new QParallelComposition(window.id.get(), parent, this.attributes, this.code, this.variable);
	}
}

