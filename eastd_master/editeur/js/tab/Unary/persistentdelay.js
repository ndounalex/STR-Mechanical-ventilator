const PERSISTENTDELAY = "PersistentDelay",
	PERSISTENTDELAY_SYMBOL = "Persistent Delay";

class PersistentDelay extends Tab {

	constructor(id = window.id.get(), parent = null, attributes = [], code = "", timer = "") {
		super(id, parent, attributes, code);
		this.timer = timer;
		this.timeUnit = "Second";
	}

	get timer() {
		return this.m_timer;
	}

	set timer(timer) {
		this.m_timer = timer;
	}

	get timeUnit() {
		return this.m_timeUnit;
	}

	set timeUnit(timeUnit) {
		this.m_timeUnit = timeUnit;
	}

	toHtml() {
		return PERSISTENTDELAY_SYMBOL + "(" + this.timer + ")";
	}

	getType() {
		return PERSISTENTDELAY;
	}

	/**
	 * Utility getter used to create specific components of the persdelay
	 * @return: object containing functions to call to create a component
	 */
	get persdelayPanel() {
		var that = this;

		return {
			createTimer: function() {
				var container = document.createElement("div"),
					timerBlock = CompBuilder.generateTextBlock("textarea", "PersistentDelay:", "", that.timer, "Ex: 1, 1.5"),
					unitBlock = CompBuilder.generateSelectBlock(["uSecond", "mSecond", "Second", "Hour", "Day"], that.timeUnit),
					hr = document.createElement("hr");
				// attach action on blur event of the input field
				timerBlock[TEXT_INPUT].onblur = function() {
					that.timer = event.target.value;
				};

				unitBlock.onchange = function() {
					that.timeUnit = event.target.value;
				};

				[...timerBlock, unitBlock, hr].forEach((component) => {
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
					var persdelayObj = window.project.getJsonObjByType(window.project.lastJsonObj.top_level_astd, PERSISTENTDELAY, parentLabel);
					if (persdelayObj !== null && persdelayObj.started !== null) {
						startedLabel.textContent = "Stared: " + persdelayObj.started;
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
		return this.defaultPanel([this.persdelayPanel.createTimer], this.persdelayPanel.createPlayMode)();
	}

	save() {
		var persdelay = super.save();
		persdelay.class = Item.TYPES.PERSISTENT_DELAY;
		persdelay.timer = this.timer;
		persdelay.timeUnit = this.timeUnit;
		return persdelay;
	}

	load(persdelay) {
		super.load(persdelay);
		this.timer = persdelay.timer;
		this.timeUnit = persdelay.timeUnit;
	}

	export() {
		let serialize = super.defaultExport();
		serialize.delay = this.timer;
		serialize.delay_unit = this.timeUnit;
		return serialize;
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedPersistentDelay(copiedParent, tab.attributes, tab.code, tab.timer);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedPersistentDelay extends CopiedTab {
	constructor(parent = null, attributes = [], code = "", timer = "") {
		super(parent, attributes, code);
		this.timer = timer;
	}

	paste(parent) {
		return new PersistentDelay(window.id.get(), parent, this.attributes, this.code, this.timer);
	}
}