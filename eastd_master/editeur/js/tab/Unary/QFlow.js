const Q_FLOW = "Quantified Flow",
	Q_FLOW_SYMBOL = " Ψ ";

class QFlow extends QTab {
	constructor(id = window.id.get(), parent = null, attributes = [], code = "", variable = null) {
		super(id, parent, attributes, code, variable);
	}

	get symbol() {
		return Q_FLOW_SYMBOL;
	}

	getType() {
		return Q_FLOW;
	}

	save() {
		var Qflow = super.save();
		Qflow.class = Item.TYPES.Q_FLOW;
		return Qflow;
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedQFlow(copiedParent, tab.attributes, tab.code, tab.variable);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedQFlow extends CopiedQTab {
	constructor(parent = null, attributes = [], code = "", variable = null) {
		super(parent, attributes, code, variable);
	}

	paste(parent) {
		return new QChoice(window.id.get(), parent, this.attributes, this.code, this.variable);
	}
}