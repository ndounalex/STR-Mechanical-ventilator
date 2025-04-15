class Attribute extends Elem {
	constructor(parent = null, label = "", type = window.project.types[0].name, value = "") {
		super(parent, label, type);
		this.value = value;
		this.field_text_error = "Initial value cannot be empty.";
	}

	// concrete implementation of delete event
	runOnDelete(btnContainer) {
		for (var i = 0; i < this.parent.attributes.length; i++) {
			if (this.parent.attributes[i] === this) {
				this.parent.attributes.splice(i, 1);
				break;
			}
		}
		if (this.parent.attributes.length === 0) {
			$("#attributesList").text("There is no attributes");
		}
		super.runOnDelete(btnContainer);
	}

	// redefinition of type blur event implementation
	runOnTypeBlur() {
		super.runOnTypeBlur();
		CompBuilder.setInputValidity(this.getTypeBlurInput(event.target.parentElement), this.getLabelError(event.target.parentElement), this.validateInitialValue());
	}

	// Overrides default behaviour because it doesn't have a domain, but an initial value
	refreshReadLabel() {
		this.e_read.textContent = `${this.label} : ${this.type} - Initial value : ${this.value}`;
	}

	toPanel() {
		var components = [
			this.elemPanel.createEditDelete,
			this.elemPanel.createReadElem,
			this.elemPanel.createNameAndType,
			this.elemPanel.createValue
		];
		return this.defaultPanel(components, "li")();
	}

	save() {
		var attribute = super.save();
		attribute.value = this.value;
		return attribute;
	}

	load(attribute) {
		super.load(attribute);
		this.value = attribute.value;
		this.validateInitialValue();
	}

	export() {
		return {
			name: this.label,
			type: this.type,
			initial_value: this.type === "string" ? JSON.parse(this.value) : this.value
		};
	}
}