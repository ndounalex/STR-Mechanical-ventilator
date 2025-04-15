const INTERRUPT = "Interrupt",
	INTERRUPT_SYMBOL = "Interrupt";

class Interrupt extends Tab {

	constructor(id = window.id.get(), parent = null, attributes = [], code = "") {
		super(id, parent, attributes, code);
	}

	toHtml() {
		return INTERRUPT_SYMBOL;
	}

	getType() {
		return INTERRUPT;
	}

	get interruptPanel() {
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
			createInterrupt: function() {
				var container = document.createElement("div"),
					labelInterrupt = CompBuilder.generateCollapseLabel("Elements", "elemsListID"),
					elemsList = document.createElement("ul"),
					hr = document.createElement("hr");

				elemsList.className = "collapse";
				elemsList.id = "elemsListID";
				// change the arrow type of links already existing before defining the type to interrupt
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

				[labelInterrupt, elemsList, hr].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			},
			createPlayMode: function() {
				var container = document.createElement("div"),
					stepLabel = document.createElement("div");

				if (window.project.lastJsonObj !== undefined && window.project.lastJsonObj !== null) {
					var parentLabel = that.parent.tabs.length > 1 ? that.label : that.parent.label,
						interruptObj = window.project.getJsonObjByType(window.project.lastJsonObj.top_level_astd, INTERRUPT, parentLabel);

					if (interruptObj !== null && interruptObj.step !== null) {
						stepLabel.textContent = "Step: " + interruptObj.step + ".";
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
		return this.defaultBinaryPanel([this.tabPanel.createInterruptAction, this.interruptPanel.createInterrupt], this.interruptPanel.createPlayMode)();
	}

	save() {
		var interrupt = super.save();
		interrupt.class = Item.TYPES.INTERRUPT;
		return interrupt;
	}

	export(min_domain, max_domain, parameter_name, test_parameter, iD, index, external_parameter_name) {
		let serialize = super.defaultExport();
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
		return new CopiedInterrupt(copiedParent, tab.attributes, tab.code);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedInterrupt extends CopiedTab {
	constructor(parent = null, attributes = [], code = "") {
		super(parent, attributes, code);
	}

	paste(parent) {
		return new Interrupt(window.id.get(), parent, this.attributes, this.code);
	}
}