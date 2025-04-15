const Q_SYNCHRONIZATION = "Quantified Synchronization",
	Q_SYNCHRONIZATION_SYMBOL = "|[ ]|";

class QSynchronization extends QTab {

	constructor(id = window.id.get(), parent = null, attributes = [], code = "", labels = "", variable = null) {
		super(id, parent, attributes, code, variable);
		this.labels = labels;
	}

	get syncEvents() {
		return this.m_syncEvents;
	}

	set syncEvents(syncEvents) {
		this.m_syncEvents = syncEvents;
	}

	get labels() {
		return this.m_labels;
	}

	set labels(labels) {
		this.m_syncEvents = labels !== "" ? labels.replace(/ /g, "").split(",") : [];
		this.m_labels = labels;
	}

	get symbol() {
		return this.labels ? "|[ { " + this.labels + " } ]|" : Q_SYNCHRONIZATION_SYMBOL;
	}

	getType() {
		return Q_SYNCHRONIZATION;
	}

	get qSyncPanel() {
		var that = this;
		return {
			createPlayMode: function() {
				var container = document.createElement("div"),
					qvarListDiv = document.createElement("div"),
					qvarSelectLabel = document.createElement("div");

				qvarListDiv.textContent = "Quantified Variable";
				var qvarSelect;
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
							qvarSelect = CompBuilder.generateSelectBlock([].concat(resultToDisplay), window.project.qvarValuePerName.get(jsonObjVar));
						} else {
							qvarSelect = CompBuilder.generateSelectBlock([""], "");
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
					qvarSelect = CompBuilder.generateSelectBlock([""], "");
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
		return this.defaultPanel([this.syncPanel.createLabels], this.qSyncPanel.createPlayMode)();
	}

	save() {
		var Qsynchronization = super.save();
		Qsynchronization.class = Item.TYPES.Q_SYNCHRONIZATION;
		Qsynchronization.labels = this.labels;
		return Qsynchronization;
	}

	load(Qsynchronization) {
		super.load(Qsynchronization);
		this.labels = Qsynchronization.labels;
	}

	export(test_parameter, parameter_name, external_parameter_name) {
		let serialize = super.defaultExport();
		var syncEventsParametrize = [];
		serialize.synchronization_set = [];
		serialize.quantified_variable = this.variable.export();
		let linksItem0 = this.parent.getFile().getAllLinksFrom(this.parent);
		linksItem0.forEach((ev) => {
			if (this.syncEvents.includes(ev.label)) {
				var indexev = this.syncEvents.indexOf(ev.label);
				syncEventsParametrize[indexev] = ev.label;
				if (ev.test_parameters.includes("ex")) {
					for (let i = 0; i < external_parameter_name.length; i++) {
						syncEventsParametrize[indexev] += "_" + test_parameter[i];
					}
				}
				parameter_name.forEach((element, index) => {
					if (ev.test_parameters.includes(element)) {
						if (external_parameter_name) {
							syncEventsParametrize[indexev] += "_" + test_parameter[index + external_parameter_name.length];
						} else {
							syncEventsParametrize[indexev] += "_" + test_parameter[index];
						}
					}
				});
			}
		});
		serialize.synchronization_set = syncEventsParametrize;
		return serialize;
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedQSynchronization(copiedParent, tab.attributes, tab.code, tab.labels, tab.variable);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedQSynchronization extends CopiedQTab {
	constructor(parent = null, attributes = [], code = "", labels = "", variable = null) {
		super(parent, attributes, code, variable);
		this.labels = labels;
	}

	paste(parent) {
		return new QSynchronization(window.id.get(), parent, this.attributes, this.code, this.labels, this.variable);
	}
}