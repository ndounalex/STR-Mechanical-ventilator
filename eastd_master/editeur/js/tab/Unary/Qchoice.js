const Q_CHOICE = "Quantified Choice",
	Q_CHOICE_SYMBOL = " | ";

class QChoice extends QTab {

	constructor(id = window.id.get(), parent = null, attributes = [], code = "", variable = null) {
		super(id, parent, attributes, code, variable);
	}

	get symbol() {
		return Q_CHOICE_SYMBOL;
	}

	getType() {
		return Q_CHOICE;
	}

	save() {
		var Qchoice = super.save();
		Qchoice.class = Item.TYPES.Q_CHOICE;
		return Qchoice;
	}

	get qChoicePanel() {
		var that = this;
		return {
			createPlayMode: function() {
				var container = document.createElement("div"),
					qVarLabel = document.createElement("div");

				if (window.project.lastJsonObj !== undefined && window.project.lastJsonObj !== null) {
					var parentLabel;
					if (that.parent.tabs.length > 1 && (that.label === null || that.label === undefined || that.label === "")) {
						parentLabel = that.parent.label;
					} else {
						parentLabel = that.label;
					}
					var qChoiceObj = window.project.getJsonObjByType(window.project.lastJsonObj.top_level_astd, "QChoice", parentLabel);
					if (qChoiceObj !== null && qChoiceObj.qchoice_var !== null) {
						qVarLabel.textContent = "Chosen value : " + qChoiceObj.qchoice_var + " = " + qChoiceObj.value;
					} else {
						qVarLabel.textContent = "Choice hasn't been made yet.";
					}
				} else {
					qVarLabel.textContent = "Choice hasn't been made yet.";
				}

				[qVarLabel].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			}
		};
	}

	toPanel() {
		return this.defaultPanel([], this.qChoicePanel.createPlayMode)();
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedQChoice(copiedParent, tab.attributes, tab.code, tab.variable);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedQChoice extends CopiedQTab {
	constructor(parent = null, attributes = [], code = "", variable = null) {
		super(parent, attributes, code, variable);
	}

	paste(parent) {
		return new QChoice(window.id.get(), parent, this.attributes, this.code, this.variable);
	}
}