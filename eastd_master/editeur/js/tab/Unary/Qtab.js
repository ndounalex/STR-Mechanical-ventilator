class QTab extends Tab {
	constructor(id = window.id.get(), parent = null, attributes = [], code = "", variable = null) {
		super(id, parent, attributes, code);
		this.variable = variable ? variable : new Variable(this);
	}

	get variable() {
		return this.m_variable;
	}

	set variable(variable) {
		this.m_variable = variable;
	}

	get symbol() {
		return "";
	}

	/**
	 * Utility getter used to create generic components of a quantified tab
	 * @return: object containing functions to call to create a component
	 */
	get tabPanel() {
		var that = this,
			panel = super.tabPanel;

		panel.createVariable = function() {
			var container = document.createElement("div"),
				labelQVar = CompBuilder.generateCollapseLabel("Quantified variable", "qVarDiv"),
				varPanel = that.variable.toPanel(),
				hr = document.createElement("hr");

			varPanel.className = "collapse ml-3";
			varPanel.id = "qVarDiv";

			[labelQVar, varPanel, hr].forEach((component) => {
				container.appendChild(component);
			});
			return container;
		};
		return panel;
	}

	/**
	 * Template function generating the html text to display for quantified tab
	 * All classes inheriting QTab must override symbol, otherwise it is empty
	 * @return (string): html text to display on the tab
	 */
	toHtml() {
		var html = this.symbol;
		if (this.variable.label || this.variable.type || this.variable.domain) {
			html += " " + this.variable.label + " : " + this.variable.type;
			if (this.variable.domainType !== "UnboundedDomain" && this.variable.domain !== undefined) {
				html += this.variable.domain;
			}
		}
		return html;
	}

	/**
	 * Generates a default panel for the quantified types adding the variable section
	 * @param extraComponents(Array:function): all extra components func to add to the panel. Always added at the end of the panel
	 * @return function: the executing function to be called in the toPanel()
	 */
	defaultPanel(extraComponents = [], extraComponentsRunMode = null) {
		return super.defaultPanel([this.tabPanel.createVariable].concat(extraComponents), extraComponentsRunMode);
	}

	save() {
		var qtab = super.save();
		qtab.variable = this.variable.save();
		return qtab;
	}

	load(qtab) {
		super.load(qtab);
		this.variable = new Variable(this);
		this.variable.load(qtab.variable);
	}

	export() {
		let serialize = super.defaultExport();
		serialize.quantified_variable = this.variable.export();
		return serialize;
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedQTab(copiedParent, tab.attributes, tab.code, tab.variable);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedQTab extends CopiedTab {
	constructor(parent = null, attributes = [], code = "", variable = null) {
		super(parent, attributes, code);
		this.variable = variable;
	}

	paste(parent) {
		return new QTab(window.id.get(), parent, this.attributes, this.code, this.variable);
	}
}