class Parameter extends Elem {
	constructor(parent = null, label = "", type = window.project.types[0].name, domain = "") {
		super(parent, label, type);
		this.domain = domain;
		this.domainType = "UnboundedDomain";
	}

	// redefinition of elem default implementation
	getParents() {
		return this.parent.label;
	}

	// redefinition of label blur event implementation
	runOnLabelBlur(errorLabel) {
		super.runOnLabelBlur(errorLabel);
		this.parent.refreshASTDLabel();
	}

	// redefinition of type blur event implementation
	runOnTypeBlur() {
		super.runOnTypeBlur();
		CompBuilder.setInputValidity(this.getTypeBlurInput(event.target.parentElement), this.getLabelError(event.target.parentElement), this.validateDomain());
	}

	// @toCheck: tester a la loupe
	findCall(astd, index) {
		var that = this;
		if (astd.tabs) {
			if (astd.tabs[astd.tabs.length - 1] instanceof Call && this.parent === astd.tabs[astd.tabs.length - 1].astd) {
				astd.tabs[astd.tabs.length - 1].argument.splice(index, 1);
			}
		}

		if (astd.items) {
			astd.items.forEach((item) => {
				that.findCall(item, index);
			});
		}
	}

	// concrete implementation of delete event
	runOnDelete(btnContainer) {
		var that = this;
		for (var i = 0; i < this.parent.parameters.length; i++) {
			if (this.parent.parameters[i] === this) {
				this.parent.parameters.splice(i, 1);
				window.project.files.forEach((file) => {
					that.findCall(file.astd, i);
				});
				break;
			}
		}
		this.parent.refreshASTDLabel();
		if (this.parent.parameters.length === 0) {
			$("#paramList").text("There is no parameters");
		}
		super.runOnDelete(btnContainer);
	}

	toPanel() {
		var components = [
			this.elemPanel.createEditDelete,
			this.elemPanel.createReadElem,
			this.elemPanel.createNameAndType,
			this.elemPanel.createDomain
		];

		return this.defaultPanel(components, "li")();
	}

	save() {
		var parameter = super.save();
		parameter.domain = this.domain;
		parameter.domainType = this.domainType;
		return parameter;
	}

	load(parameter) {
		super.load(parameter);
		this.domain = parameter.domain;
		this.domainType = parameter.domainType;
		this.validateDomain();
	}

}