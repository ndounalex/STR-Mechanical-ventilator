const SYNCHRONIZATION = "Synchronization",
	SYNCHRONIZATION_SYMBOL = "|[ ]|";

class Synchronization extends Tab {

	constructor(id = window.id.get(), parent = null, attributes = [], code = "", labels = "") {
		super(id, parent, attributes, code);
		this.labels = labels;
		this.syncEventsParametrize = [];
		this.synchronizationSet = [];
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
		if (labels !== undefined) {
			this.m_syncEvents = labels !== "" ? labels.replace(/ /g, "").split(",") : [];
			this.m_labels = labels;
		}
	}

	toHtml() {
		return this.labels ? "|[ { " + this.labels + " } ]|" : SYNCHRONIZATION_SYMBOL;
	}

	getType() {
		return SYNCHRONIZATION; // "Synchronisation if british -> @toCheck: #170 iASTD"
	}

	toPanel() {
		return this.defaultBinaryPanel([this.syncPanel.createLabels])();
	}

	save() {
		var synchronization = super.save();
		synchronization.class = Item.TYPES.SYNCHRONIZATION;
		synchronization.labels = this.m_labels;
		return synchronization;
	}

	load(sync) {
		super.load(sync);
		this.labels = sync.labels;
		// this.synchronizationSet
	}

	export(min_domain, max_domain, parameter_name, test_parameter, iD, index, external_parameter_name, test = false, nary = false, operationIndex = 0, operations_maxLength = 0, operationID = null) {

		let serialize = super.defaultExport(),
			linksItem0 = this.parent.getFile().getAllLinksFrom(this.parent.items[0]);

		var syncEventsParametrize = [];
		linksItem0.forEach((ev) => {
			if (this.syncEvents.includes(ev.label)) {
				var indexev = this.syncEvents.indexOf(ev.label);
				syncEventsParametrize[indexev] = ev.label;
				if (ev.test_parameters.includes("ex")) {
					for (let i = 0; i < external_parameter_name.length; i++) {
						syncEventsParametrize[indexev] += "_" + test_parameter[i];
					}
				}
				parameter_name.forEach((element, indexfor) => {
					if (ev.test_parameters.includes(element)) {
						if (external_parameter_name) {
							syncEventsParametrize[indexev] += "_" + test_parameter[indexfor + external_parameter_name.length];
						} else {
							syncEventsParametrize[indexev] += "_" + test_parameter[indexfor];
						}
					}
				});
			}
		});
		if (!test) {
			syncEventsParametrize = this.synchronizationSet;
		}
		serialize.synchronization_set = syncEventsParametrize;

		if (min_domain && max_domain) {
			if (min_domain + 1 === max_domain) {
				serialize.left_astd = this.parent.items[0].texport(0, test_parameter.concat(min_domain), null, null, parameter_name, null, external_parameter_name, test, nary, 0, operations_maxLength, null);
				serialize.right_astd = this.parent.items[0].texport(0, test_parameter.concat(max_domain), null, null, parameter_name, null, external_parameter_name, test, nary, 0, operations_maxLength, null);
			} else {
				serialize.left_astd = this.parent.items[0].texport(0, test_parameter.concat(min_domain), null, null, parameter_name, null, external_parameter_name, test, nary, 0, operations_maxLength, null);
				serialize.right_astd = this.parent.texport(index, test_parameter, min_domain + 1, max_domain, parameter_name, iD + 1, external_parameter_name, test, nary, 0, operations_maxLength, null);
			}
		} else {
			console.log("===================== max length =============== ", operationIndex+2, operations_maxLength);
			if (operationIndex + 2 === operations_maxLength) {
				serialize.left_astd = this.parent.items[operationIndex].texport(0, test_parameter, null, null, parameter_name, null, external_parameter_name, test, nary, 0, operations_maxLength, null);
				serialize.right_astd = this.parent.items[operationIndex + 1].texport(0, test_parameter, null, null, parameter_name, null, external_parameter_name, test, nary, 0, operations_maxLength, null);
			} else {
				console.log("============= opération index ============== ", {operation:operationIndex, items: [...this.parent.items]});
				console.log("my items ", { item: this.parent.items[operationIndex] });
				serialize.left_astd = this.parent.items[operationIndex].texport(0, test_parameter, null, null, parameter_name, null, external_parameter_name, test, nary, 0, operations_maxLength, null);
				serialize.right_astd = this.parent.texport(0, test_parameter, null, null, parameter_name, null, external_parameter_name, test, nary, operationIndex + 1, operations_maxLength, operationID + 1);
			}
		}
		return serialize;
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedSynchronization(copiedParent, tab.attributes, tab.code, tab.labels, tab.synchronizationSet);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedSynchronization extends CopiedTab {
	constructor(parent = null, attributes = [], code = "", labels = "", synchronizationSet = []) {
		super(parent, attributes, code);
		this.labels = labels;
		this.synchronizationSet = synchronizationSet;
	}

	paste(parent) {
		let synchro = new Synchronization(window.id.get(), parent, this.attributes, this.code, this.labels);
		synchro.synchronizationSet = this.this.synchronizationSet;
	}
}