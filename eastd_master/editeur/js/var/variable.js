class Variable extends Elem {
	constructor(parent = null, label = "", type = window.project.types[0].name, domain = "") {
		super(parent, label, type);
		this.domain = domain;
		this.domainType = "UnboundedDomain";
	}

	// redefinition of label blur event implementation
	runOnLabelBlur(errorLabel) {
		super.runOnLabelBlur(errorLabel);
		this.parent.parent.refreshTabs();
	}

	// redefinition of type blur event implementation
	runOnTypeBlur() {
		super.runOnTypeBlur();
		CompBuilder.setInputValidity(this.getTypeBlurInput(event.target.parentElement), this.getLabelError(event.target.parentElement), this.validateDomain());
		this.parent.parent.refreshTabs();
	}

	// redefinition of domain blur event implementation
	runOnDomainBlur() {
		super.runOnDomainBlur();
		this.parent.parent.refreshTabs();
	}

	runOnSelectChange(domainComp) {
		super.runOnSelectChange(domainComp);
		this.parent.parent.refreshTabs();
	}

	toPanel() {
		var components = [
			this.elemPanel.createReadElem, // only used to display on print
			this.elemPanel.createNameAndType,
			this.elemPanel.createDomain
		];
		return this.defaultPanel(components, "div")();
	}

	save() {
		var variable = super.save();
		variable.domain = this.domain;
		variable.domainType = this.domainType;
		return variable;
	}

	load(variable) {
		super.load(variable);
		this.domain = variable.domain;
		this.domainType = variable.domainType;
		this.validateDomain();
	}

}