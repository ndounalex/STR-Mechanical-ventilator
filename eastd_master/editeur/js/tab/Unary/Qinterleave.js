const Q_INTERLEAVE = "Quantified Interleave",
	Q_INTERLEAVE_SYMBOL = " ||| ";

class QInterleave extends QTab {

	constructor(id = window.id.get(), parent = null, attributes = [], code = "", variable = null) {
		super(id, parent, attributes, code, variable);
	}

	get symbol() {
		return Q_INTERLEAVE_SYMBOL;
	}

	getType() {
		return Q_INTERLEAVE;
	}

	get qInterleavePanel() {
		var that = this;
		return {
			createPlayMode: function() {
				var container = document.createElement("div"),
					qvarListDiv = document.createElement("div"),
					qvarSelectLabel = document.createElement("div");

				qvarListDiv.textContent = "Quantified Variable";

				if (window.project.lastJsonObj !== undefined && window.project.lastJsonObj !== null) {
					var parentLabel;
					if (that.parent.tabs.length > 1 && (that.label === null || that.label === undefined || that.label === "")) {
						parentLabel = that.parent.label;
					} else {
						parentLabel = that.label;
					}
					var qSyncObj = window.project.getJsonObjByType(window.project.lastJsonObj.top_level_astd, "QSynchronization", parentLabel);

					if (qSyncObj !== null) {
						var resultToDisplay = [],
							jsonObjVar,
							i;
						for (i = 0; i < qSyncObj.sub_states.length; i++) {
							jsonObjVar = qSyncObj.sub_states[i].qsynch_var;
							resultToDisplay.push(qSyncObj.sub_states[i].value);
						}
						qvarSelectLabel.textContent = jsonObjVar + " : ";
						if (resultToDisplay.length > 0) {
							var qvarSelect = CompBuilder.generateSelectBlock([].concat(resultToDisplay), window.project.qvarValuePerName.get(jsonObjVar));
						} else {
							var qvarSelect = CompBuilder.generateSelectBlock([""], "");
						}
						qvarSelect.classList.add("play-mode-select");

						qvarSelect.onchange = function() {
							if (qvarSelect.value !== "") {
								window.project.qvarValuePerName.set(jsonObjVar, qvarSelect.value);
								window.project.refreshConsoleCurrentState();
							}
						};
					}
				} else {
					qvarSelectLabel.textContent = "";
					var qvarSelect = CompBuilder.generateSelectBlock([""], "");
				}
				qvarSelectLabel.appendChild(qvarSelect);

				[qvarListDiv, qvarSelectLabel].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			}
		};
	}

	toPanel() {
		return this.defaultPanel([], this.qInterleavePanel.createPlayMode)();
	}

	save() {
		var Qinterleave = super.save();
		Qinterleave.class = Item.TYPES.Q_INTERLEAVE;
		return Qinterleave;
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedQInterleave(copiedParent, tab.attributes, tab.code, tab.variable);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedQInterleave extends CopiedQTab {
	constructor(parent = null, attributes = [], code = "", variable = null) {
		super(parent, attributes, code, variable);
	}

	paste(parent) {
		return new QInterleave(window.id.get(), parent, this.attributes, this.code, this.variable);
	}
}