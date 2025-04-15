const TIMEOUT = "Timeout",
	TIMEOUT_SYMBOL = "Timeout";

class Timeout extends Tab {

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
		return TIMEOUT_SYMBOL + "(" + this.timer + ")";
	}

	getType() {
		return TIMEOUT;
	}

	get TimeoutPanel() {
		var that = this;

		function executeIfSelected(callback) {
			window.project.files.forEach(function(arrayItem) {
				if (arrayItem.e_file.classList.contains("active")) {
					callback(arrayItem);
				}
			});
		}

		function sourceLink(source) {
			var l = null;

			executeIfSelected((arrayItem) => {
				arrayItem.links.forEach((link) => {
					if (link.source === source) {
						l = link;
					}
				});
			});
			return l;
		}

		function first() {
			var destinations = [],
				item = null;

			executeIfSelected((arrayItem) => {
				arrayItem.links.forEach(function(link) {
					destinations.push(link.destination);
				});
			});

			executeIfSelected((arrayItem) => {
				arrayItem.astd.items.forEach(function(i) {
					if (!destinations.includes(i)) {
						item = i;
					}
				});
			});
			return item;
		}

		return {
			createTimeout: function() {
				var container = document.createElement("div"),
					timerBlock = CompBuilder.generateTextBlock("textarea", "Timeout:", "", that.timer, "Ex: 1, 1.5"),
					unitBlock = CompBuilder.generateSelectBlock(["uSecond", "mSecond", "Second", "Hour", "Day"], that.timeUnit),
					labelTimeout = CompBuilder.generateCollapseLabel("Elements", "elemsListID"),
					elemsList = document.createElement("ul"),
					hr1 = document.createElement("hr"),
					hr2 = document.createElement("hr");

				timerBlock[TEXT_INPUT].onblur = function() {
					that.timer = event.target.value;
				};

				unitBlock.onchange = function() {
					that.timeUnit = event.target.value;
				};

				elemsList.className = "collapse";
				elemsList.id = "elemsListID";
				// change the arrow type of links already existing before defining the type to TIMEOUT
				that.parent.items.forEach((items) => {
					that.parent.getFile().getLinks(items).forEach((link) => {
						link.type = TIMEOUT;
					});
				});

				var f = first(),
					link = sourceLink(f);
				if (f && link) {
					var elt = document.createElement("li"),
						elt2 = document.createElement("li");

					elt.textContent = f.label;
					elt2.textContent = link.destination.label;
					elemsList.appendChild(elt);
					elemsList.appendChild(elt2);
				} else {
					elemsList.textContent = "There is no elements";
				}

				[labelTimeout, elemsList, hr1, ...timerBlock, unitBlock, hr2].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			},
			createPlayMode: function() {
				var container = document.createElement("div"),
					stepLabel = document.createElement("div");

				if (window.project.lastJsonObj !== undefined && window.project.lastJsonObj !== null) {
					var parentLabel = that.parent.tabs.length > 1 ? that.label : that.parent.label,
						TimeoutObj = window.project.getJsonObjByType(window.project.lastJsonObj.top_level_astd, TIMEOUT, parentLabel);

					if (TimeoutObj !== null && TimeoutObj.step !== null) {
						stepLabel.textContent = "Step: " + TimeoutObj.step + ".";
					} else {
						stepLabel.textContent = "Step: No step yet.";
					}
				} else {
					stepLabel.textContent = "Step: No step yet.";
				}

				[stepLabel].forEach((component) => {
					container.appendChild(component);
				});
				return container;

			}
		};
	}

	toPanel() {
		return this.defaultBinaryPanel([this.tabPanel.createInterruptAction, this.TimeoutPanel.createTimeout], this.TimeoutPanel.createPlayMode)();
	}

	save() {
		var timeout = super.save();
		timeout.class = Item.TYPES.TIMEOUT;
		timeout.timer = this.timer;
		timeout.timeUnit = this.timeUnit;
		return timeout;
	}

	load(timeout) {
		super.load(timeout);
		this.timer = timeout.timer;
		this.timeUnit = timeout.timeUnit;
	}

	export(min_domain, max_domain, parameter_name, test_parameter, iD, index, external_parameter_name) {
		let serialize = super.defaultExport();
		serialize.duration = this.timer;
		serialize.duration_unit = this.timeUnit;
		if (min_domain && max_domain) {
			if (min_domain + 1 === max_domain) {
				serialize.left_astd = this.parent.items[0].texport(0, test_parameter.concat(min_domain), null, null, parameter_name, null, external_parameter_name);
				serialize.right_astd = this.parent.items[0].texport(0, test_parameter.concat(max_domain), null, null, parameter_name, null, external_parameter_name);
			} else {
				serialize.left_astd = this.parent.items[0].texport(0, test_parameter.concat(min_domain), null, null, parameter_name, null, external_parameter_name);
				serialize.right_astd = this.parent.texport(index, test_parameter, min_domain + 1, max_domain, parameter_name, iD + 1, external_parameter_name);

			}
		} else {

			this.parent.getFile().links.forEach((link) => {
				if (link.source.parent === this.parent && link.destination.parent === this.parent) {
					serialize.left_astd = link.source.texport(0, test_parameter, null, null, parameter_name, null, external_parameter_name);
					serialize.right_astd = link.destination.texport(0, test_parameter, null, null, parameter_name, null, external_parameter_name);
				}
			});
		}

		return serialize;
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedTimeout(copiedParent, tab.attributes, tab.code, tab.timer);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedTimeout extends CopiedTab {
	constructor(parent = null, attributes = [], code = "", timer = "") {
		super(parent, attributes, code);
		this.timer = timer;
	}

	paste(parent) {
		return new Timeout(window.id.get(), parent, this.attributes, this.code, this.timer);
	}
}