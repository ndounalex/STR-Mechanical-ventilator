const DELAY = "Delay",
	DELAY_SYMBOL = "Delay";

class Delay extends Tab {

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
		return DELAY_SYMBOL + "(" + this.timer + ")";
	}

	getType() {
		return DELAY;
	}

	/**
	* Utility getter used to create specific components of the delay
	* @return: object containing functions to call to create a component
	*/
	get delayPanel() {
		var that = this;

		return {
			createTimer: function() {
				var container = document.createElement("div"),
					timerBlock = CompBuilder.generateTextBlock("textarea", "Delay time:", "", that.timer, "Ex: 1, 1.5"),
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
					var delayObj = window.project.getJsonObjByType(window.project.lastJsonObj.top_level_astd, DELAY, parentLabel);
					if (delayObj !== null && delayObj.started !== null) {
						startedLabel.textContent = "Stared: " + delayObj.started;
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
		return this.defaultPanel([this.delayPanel.createTimer], this.delayPanel.createPlayMode)();
	}

	save() {
		var delay = super.save();
		delay.class = Item.TYPES.DELAY;
		delay.timer = this.timer;
		delay.timeUnit = this.timeUnit;
		return delay;
	}

	load(delay) {
		super.load(delay);
		this.timer = delay.timer;
		this.timeUnit = delay.timeUnit;
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
		return new CopiedDelay(copiedParent, tab.attributes, tab.code, tab.timer);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedDelay extends CopiedTab {
	constructor(parent = null, attributes = [], code = "", timer = "") {
		super(parent, attributes, code);
		this.timer = timer;
	}
	paste(parent) {
		return new Delay(window.id.get(), parent, this.attributes, this.code, this.timer);
	}
}