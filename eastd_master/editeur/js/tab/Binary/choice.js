const CHOICE = "Choice",
	CHOICE_SYMBOL = " | ";

class Choice extends Tab {
	constructor(id = window.id.get(), parent = null, attributes = [], code = "") {
		super(id, parent, attributes, code);
	}

	toHtml() {
		return CHOICE_SYMBOL;
	}

	getType() {
		return CHOICE;
	}

	get choicePanel() {
		var that = this;
		return {
			createPlayMode: function() {
				var container = document.createElement("div"),
					sideLabel = document.createElement("div");

				if (window.project.lastJsonObj !== undefined && window.project.lastJsonObj !== null) {
					var parentLabel = that.parent.tabs.length > 1 ? that.label : that.parent.label,
						choiceObj = window.project.getJsonObjByType(window.project.lastJsonObj.top_level_astd, CHOICE, parentLabel);
					if (choiceObj !== null && choiceObj.side !== null) {
						sideLabel.textContent = "Side: " + choiceObj.side + ".";
					} else {
						sideLabel.textContent = "Side: No side yet.";
					}
				} else {
					sideLabel.textContent = "Side: No side yet.";
				}

				[sideLabel].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			}
		};
	}

	toPanel() {
		return this.defaultBinaryPanel([], this.choicePanel.createPlayMode)();
	}

	save() {
		var choice = super.save();
		choice.class = Item.TYPES.CHOICE;
		return choice;
	}

	export(min_domain, max_domain, parameter_name, test_parameter, iD, index, external_parameter_name, test = false, nary = false, operationIndex = 0, operations_maxLength = 0, operationID = null) {
		operations_maxLength = this.parent.items.length;
		return super.naryExport(min_domain, max_domain, parameter_name, test_parameter, iD, index, external_parameter_name, test, nary, operationIndex, operations_maxLength, operationID);
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedChoice(copiedParent, tab.attributes, tab.code);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}

}

// eslint-disable-next-line no-undef
class CopiedChoice extends CopiedTab {
	constructor(parent = null, attributes = [], code = "") {
		super(parent, attributes, code);
	}

	paste(parent) {
		return new Choice(window.id.get(), parent, this.attributes, this.code, this.interruptCode, this.label);
	}
}