const KLEENE = "Kleene",
	KLEENE_SYMBOL = "★";

class Closure extends Tab {

	constructor(id = window.id.get(), parent = null, attributes = [], code = "") {
		super(id, parent, attributes, code);
	}

	toHtml() {
		return KLEENE_SYMBOL;
	}

	getType() {
		return KLEENE + " Closure";
	}

	classNameForExport() {
		return KLEENE;
	}

	save() {
		var closure = super.save();
		closure.class = Item.TYPES.CLOSURE;
		return closure;
	}

	get kleenePanel() {
		var that = this;
		return {
			createPlayMode: function() {
				var container = document.createElement("div"),
					startedLabel = document.createElement("div");

				if (window.project.lastJsonObj !== undefined && window.project.lastJsonObj !== null) {
					var parentLabel;
					if (that.parent.tabs.length > 1 && (that.label === null || that.label === undefined || that.label === "")) {
						parentLabel = that.parent.label;
					} else {
						parentLabel = that.label;
					}
					var kleeneObj = window.project.getJsonObjByType(window.project.lastJsonObj.top_level_astd, KLEENE, parentLabel);
					if (kleeneObj !== null && kleeneObj.started !== null) {
						startedLabel.textContent = "Stared: " + kleeneObj.started;
					} else {
						startedLabel.textContent = "Started: false";
					}
				} else {
					startedLabel.textContent = "Started: false";
				}

				[startedLabel].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			}
		};
	}

	toPanel() {
		return this.defaultPanel([], this.kleenePanel.createPlayMode)();
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedClosure(copiedParent, tab.attributes, tab.code);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedClosure extends CopiedTab {
	constructor(parent = null, attributes = [], code = "") {
		super(parent, attributes, code);
	}

	paste(parent) {
		return new Closure(window.id.get(), parent, this.attributes, this.code);
	}
}