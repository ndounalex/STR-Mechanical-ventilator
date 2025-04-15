const GUARD = "Guard",
	GUARD_SYMBOL = "⇒";

class Guard extends Tab {

	constructor(id = window.id.get(), parent = null, attributes = [], code = "", predicate = "") {
		super(id, parent, attributes, code);
		this.predicate = predicate;
	}

	get predicate() {
		return this.m_predicate;
	}

	set predicate(predicate) {
		this.m_predicate = predicate;
	}

	toHtml() {
		return GUARD_SYMBOL;
	}

	getType() {
		return GUARD;
	}

	/**
	 * Utility getter used to create specific components of the guard
	 * @return: object containing functions to call to create a component
	 */
	get guardPanel() {
		var that = this;

		return {
			createPredicate: function() {
				var container = document.createElement("div"),
					predicateBlock = CompBuilder.generateTextBlock("textarea", "Predicate:", "", that.predicate, "Ex: x=1 | guard_file.txt"),
					hr = document.createElement("hr");
				// attach action on blur event of the input field
				predicateBlock[TEXT_INPUT].onblur = function() {
					that.predicate = event.target.value;
				};

				[...predicateBlock, hr].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			},
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
					var guardObj = window.project.getJsonObjByType(window.project.lastJsonObj.top_level_astd, GUARD, parentLabel);
					if (guardObj !== null && guardObj.started !== null) {
						startedLabel.textContent = "Stared: " + guardObj.started;
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
		return this.defaultPanel([this.guardPanel.createPredicate], this.guardPanel.createPlayMode)();
	}

	save() {
		var guard = super.save();
		guard.class = Item.TYPES.GUARD;
		guard.predicate = this.predicate;
		return guard;
	}

	load(guard) {
		super.load(guard);
		this.predicate = guard.predicate;
	}

	export() {
		let serialize = super.defaultExport();
		serialize.guard = this.predicate;
		return serialize;
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedGuard(copiedParent, tab.attributes, tab.code, tab.predicate);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedGuard extends CopiedTab {
	constructor(parent = null, attributes = [], code = "", predicate = "") {
		super(parent, attributes, code);
		this.predicate = predicate;
	}

	paste(parent) {
		return new Guard(window.id.get(), parent, this.attributes, this.code, this.predicate);
	}
}
