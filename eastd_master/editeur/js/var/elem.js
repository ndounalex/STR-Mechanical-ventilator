class Elem {
	constructor(parent = null, label = "", type = window.project.types[0].name) {
		this.parent = parent;
		this.label = label;
		this.type = type;
		this.label_text_error = "Name field is mandatory.";
		this.field_text_error = "";
	}

	get parent() {
		return this.m_parent;
	}

	set parent(parent) {
		this.m_parent = parent;
	}

	get label() {
		return this.m_label;
	}

	set label(label) {
		this.m_label = label;
	}

	get type() {
		return this.m_type;
	}

	set type(type) {
		this.m_type = type.trim();
	}

	get domain() {
		return this.m_domain;
	}

	set domain(domain) {
		this.m_domain = domain;
	}

	get domainType() {
		return this.m_domainType;
	}

	set domainType(domainType) {
		this.m_domainType = domainType;
	}

	get value() {
		return this.m_value;
	}

	set value(value) {
		this.m_value = value;
	}

	getParents() {
		var a,
			parents = "",
			par = this.parent.parent,
			i;

		for (i = par.tabs.indexOf(this.parent); i >= 1; i--) {
			parents = "/" + par.tabs[i].label + ", " + par.tabs[i].constructor.name + parents;
		}
		parents = "/" + par.label + ", " + par.tabs[0].constructor.name + parents;
		par = par.parent;

		a = par === this.parent.parent.getFile().astd;

		while (par !== this.parent.parent.getFile().astd && !(par instanceof File)) {
			a = true;
			for (i = par.tabs.length - 1; i >= 1; i--) {
				parents = "/" + par.tabs[i].label + ", " + par.tabs[i].constructor.name + parents;
			}
			parents = par.tabs[0] ? "/" + par.label + ", " + par.tabs[0].constructor.name + parents : "/" + par.label + parents;

			par = par.parent;
		}
		if (a) {
			for (i = par.tabs.length - 1; i >= 1; i--) {
				parents = "/" + par.tabs[i].label + ", " + par.tabs[i].constructor.name + parents;
			}
			parents = par.tabs[0] ? par.label + ", " + par.tabs[0].constructor.name + parents : par.label + parents;
		}
		if (parents[0] === "/") {
			parents = parents.substring(1, parents.length);
		}
		return parents;
	}

	// default implementation of label blur event
	runOnLabelBlur(errorLabel) {
		this.label = event.target.value;
		this.refreshReadLabel();
		CompBuilder.setInputValidity(event.target, errorLabel, this.validateLabel());
	}

	// default implementation of type blur event
	runOnTypeBlur() {
		this.type = event.target.value;
		if (this.type in ["int", "string", "bool", "float"]) {
			var dom = event.target.parentNode.parentNode.getElementsByClassName("domain-select")[0];
			if (this.type === "int") {
				if (dom.length !== 3) {
					dom.add(new Option("IntegerDomainExpression", "IntegerDomainExpression"));
				}
			} else {
				dom.remove(2);
			}
		}
		this.refreshReadLabel();
	}

	// default implementation of domain blur event
	runOnDomainBlur() {
		this.domain = event.target.value;
		CompBuilder.setInputValidity(event.target, this.getLabelError(event.target), this.validateDomain());
		this.refreshReadLabel();
	}

	// default implementation of read mode refresh content. Must be overridden if elem type doesn't have a domain and domain type
	refreshReadLabel() {
		this.e_read.textContent = `${this.label} : ${this.type} - ${this.domainType}`;
		this.e_read.textContent += this.domainType === "UnboundedDomain" ? "" : ` : ${this.domain}`;
	}

	// check if the name property is not empty
	validateLabel() {
		return this.label_text_error = this.label ? "" : "Name field is mandatory.";
	}

	// check the domain syntax according to the domain type selected
	validateDomain() {
		if (this.type) {
			switch (this.domainType) {
			case "EnumeratedDomain":
				this.validateEnumeratedDomain();
				break;
			case "IntegerDomainExpression":
				this.validateIntegerDomainExp();
				break;
			default:
				this.field_text_error = "";
			}
		} else {
			this.field_text_error = "";
		}

		return this.field_text_error;
	}

	// Validate the domain value for EnumeratedDomain
	validateEnumeratedDomain() {
		if (!project.isComplexType(this.type)) {
			// match a regex exp
			if (this.type === "int")
				this.field_text_error = this.domain.match(/(([0-9]+,)*[0-9]+)*/)[0] !== this.domain ? "Invalid syntax. Accepted: 1,2,3" : "";
			if (this.type === "string")
				this.field_text_error = this.domain.match(/(("\w+",)*"\w+")*/)[0] !== this.domain ? "Invalid syntax. Accepted: \"a\",\"b\",\"c\"" : "";
			if (this.type === "bool")
				this.field_text_error = this.domain.match(/(((true|false)+,)*(true|false))*/)[0] !== this.domain ? "Invalid syntax. Accepted: \"true\",\"false\"" : "";
			if (this.type === "float")
				this.field_text_error = this.domain.match(/(([-+]?[0-9]*\.?[0-9]+,)*[-+]?[0-9]*\.?[0-9])*/)[0] !== this.domain ? "Invalid syntax. Accepted: 1.2,2.5,3.0" : "";
		} else {
			// should be able to parse as a json object
			try {
				var domainEnumeration = JSON.parse("[" + this.domain + "]");
				domainEnumeration.forEach((value) => {
					if (!project.complexStructure.validate(this.type, value)) {
						throw project.complexStructure.error;
					}
				});
				this.field_text_error = "";
			} catch (error) {
				this.field_text_error = error.dataPath ? error.dataPath + " " + error.message : error.message;
			}
		}
	}

	// Validate the domain value for IntegerDomainExpression
	validateIntegerDomainExp() {
		if (this.type === "int") {
			this.field_text_error = Validator.syntax_Verify(this.domain, window.integer_domain_grammar_object).message;
		} else {
			this.field_text_error = "The type must be an int to use this domain.";
		}
	}

	// validate the initial value of an attribute
	validateInitialValue() {
		if (this.value === null || this.value === undefined || this.value === "") {
			this.field_text_error = "Initial value cannot be empty.";
		} else if (!project.isComplexType(this.type)) {
			if (this.type === "int")
				this.field_text_error = this.value.match(/[0-9]*/)[0] !== this.value ? "Value is not an int." : "";
			if (this.type === "string")
				this.field_text_error = this.value.trim().match(/("\w+")?/)[0] !== this.value ? "Value is not a string." : "";
			if (this.type === "bool")
				this.field_text_error = this.value.match(/(true|false)/)[0] !== this.value ? "Value is not a boolean." : "";
			if (this.type === "float")
				this.field_text_error = this.value.match(/[-+]?[0-9]*\.?[0-9]*/)[0] !== this.value ? "Value is not a float." : "";
			if (!(this.type in ["int", "string", "bool", "float"]))
				this.field_text_error = "";
		} else {
			try {
				if (!project.complexStructure.validate(this.type, JSON.parse(this.value))) {
					throw project.complexStructure.error;
				}
				this.field_text_error = "";
			} catch (error) {
				this.field_text_error = error.dataPath ? error.dataPath + " " + error.message : error.message;
			}
		}

		return this.field_text_error;
	}

	// retrieve the error label of the current element
	// elem must be a child of the current element
	getLabelError(elem) {
		return elem.parentNode.getElementsByClassName("errorVar")[0];
	}

	// retrieve the input affected by a type change
	getTypeBlurInput(elem) {
		return elem.parentNode.getElementsByClassName("type-blur-input")[0];
	}

	// default implementation on domain selection change
	runOnSelectChange(domainComp) {
		this.domainType = event.target.value;
		if (this.domainType === "UnboundedDomain") {
			domainComp.classList.add("d-none");
			domainComp.classList.remove("read-write-list");

		} else {
			domainComp.classList.remove("d-none");
			domainComp.classList.add("read-write-list");
		}
		domainComp.placeholder = this.getDomainPlaceholder();
		this.refreshReadLabel();
		CompBuilder.setInputValidity(domainComp, this.getLabelError(domainComp), this.validateDomain());
	}

	// default implementation of value blur event (only called for attribute)
	runOnValueBlur() {
		this.value = event.target.value;
		this.refreshReadLabel();
		this.field_text_error = "";
		CompBuilder.setInputValidity(event.target, this.getLabelError(event.target), this.validateInitialValue());
	}

	// default implementation of keyup event
	// IMPORTANT: it needs to be bind to the input, not the element
	runOnKeyUp() {
		if (event.keyCode === 13) {
			this.blur();
		}
	}

	// default implementation of the delete of an element event
	runOnDelete(btnContainer) {
		btnContainer.parentElement.remove();
	}

	getDomainPlaceholder() {
		switch (this.domainType) {
		case "EnumeratedDomain":
			return "Ex: 1,2,3";
		case "IntegerDomainExpression":
			return "Ex: ([1,10]-5)+{15,17}";
		default:
			return "";
		}
	}

	/**
	* Utility getter used to create components of an element (variable, attribute or parameter
	* @return Object: object containing functions to call to create a component
	*/
	get elemPanel() {
		var that = this;
		return {
			createNameAndType: function() {
				var options = project.types.filter((typeElem) => {
						if (that instanceof Variable) // Quantified variables can't be a native type, so remove the option
							return typeElem.type !== "native";
						return typeElem.name !== "";
					}).map((typeElem) => {
						return typeElem.name;
					}),
					container = document.createElement("div"),
					nameInput = document.createElement("input"),
					typeSelect = CompBuilder.generateSelectBlock(options, that.type),
					errorLabel = CompBuilder.generateFieldError();

				nameInput.className = "form-control form-control-sm elem-name read-write-list no-print";
				nameInput.placeholder = "name";
				nameInput.value = that.label;
				nameInput.onblur = that.runOnLabelBlur.bind(that, errorLabel);
				nameInput.onkeyup = that.runOnKeyUp.bind(nameInput);
				CompBuilder.setInputValidity(nameInput, errorLabel, that.label_text_error);

				typeSelect.classList.replace("panel-default", "elem-name");
				typeSelect.classList.add("read-write-list", "no-print", "type-blur-input");
				typeSelect.onchange = that.runOnTypeBlur.bind(that);

				[nameInput, typeSelect, errorLabel].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			},

			createEditDelete: function() {
				var container = document.createElement("div"),
					editBtn = document.createElement("button"),
					deleteBtn = document.createElement("button"),
					iconValue = "fa-check";
				container.className = "delete no-print";

				editBtn.className = "btn btn-sm px-1";
				editBtn.innerHTML = `<i class="fas ${iconValue}"></i>`;
				editBtn.onclick = function() {
					iconValue = iconValue === "fa-pencil-alt" ? "fa-check" : "fa-pencil-alt";
					this.innerHTML = `<i class="fas ${iconValue}"></i>`;
					$(container.parentElement).find(".read-write-list").toggleClass("d-none");
				};

				deleteBtn.className = "btn btn-sm px-1";
				deleteBtn.innerHTML = "<i class=\"far fa-trash-alt\"></i>";
				deleteBtn.onclick = that.runOnDelete.bind(that, container);

				container.appendChild(editBtn);
				container.appendChild(deleteBtn);
				return container;
			},

			createDomain: function() {
				var container = document.createElement("div"),
					select = CompBuilder.generateSelectBlock(["UnboundedDomain", "EnumeratedDomain", "IntegerDomainExpression"], that.domainType),
					domainBlock = CompBuilder.generateTextBlock("textarea", "", that.field_text_error, that.domain, that.getDomainPlaceholder());

				select.classList.replace("panel-default", "panel-list");
				select.classList.add("read-write-list", "no-print", "domain-select");
				select.onchange = that.runOnSelectChange.bind(that, domainBlock[TEXT_INPUT]);
				// domain textArea
				domainBlock[TEXT_INPUT].classList.replace("panel-default", "panel-list");
				domainBlock[TEXT_INPUT].onblur = that.runOnDomainBlur.bind(that);
				if (that.domainType === "UnboundedDomain")
					domainBlock[TEXT_INPUT].classList.add("d-none");
				else
					domainBlock[TEXT_INPUT].classList.add("read-write-list");
				// error label
				domainBlock[ERROR_LABEL].className += " errorVar";

				[select, domainBlock[TEXT_INPUT], domainBlock[ERROR_LABEL]].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			},

			createValue: function() {
				var container = document.createElement("div"),
					textBlock = CompBuilder.generateTextBlock("textarea", "", that.field_text_error, that.value, "Initial value");
				// value textArea
				textBlock[TEXT_INPUT].classList.replace("panel-default", "panel-list");
				textBlock[TEXT_INPUT].classList.add("read-write-list", "no-print", "type-blur-input");
				textBlock[TEXT_INPUT].onblur = that.runOnValueBlur.bind(that);

				textBlock[ERROR_LABEL].className += " errorVar"; // error Label

				container.appendChild(textBlock[TEXT_INPUT]);
				container.appendChild(textBlock[ERROR_LABEL]);
				return container;
			},

			createReadElem: function() {
				var div = document.createElement("div");
				div.className = "read-write-list label-list d-none";
				that.e_read = div; // We keep the reference of this component to update it later on
				that.refreshReadLabel();
				return div;
			}
		};
	}

	/**
	 *  Generate the panel for the elements
	 * @param components(Array:function): functions used to generate the desired components in the panel
	 * @param containerType: type of the element in which components must be added. It is either part of a list (li) or standalone (div)
	 * @return function: the executing function to be called in the toPanel() function of children
	 */
	defaultPanel(components, containerType) {
		return function() {
			var container = document.createElement(containerType);
			components.forEach((componentFunc) => {
				container.appendChild(componentFunc());
			});
			return container;
		};
	}

	save() {
		return {
			parent: this.parent.id,
			label: this.label,
			type: this.type
		};
	}

	load(elem) {
		this.label = elem.label;
		this.validateLabel();
		this.type = elem.type;
	}

	export() {
		let serialize = {
			name: this.label,
			type: this.type,
			domain: { type: this.domainType }
		};

		// Unbounded domain -> shouldnt contains value attribute
		if (this.domainType === "UnboundedDomain")
			return serialize;

		// Other Cases
		var domain = [];
		if (this.domainType === "EnumeratedDomain") {
			// @todo: .split should not be working properly with json objects
			domain = this.domain.split(",").map((dom) => {
				return JSON.parse(dom);
			});
		} else {
			domain = this.domain;
		}

		// Domain value
		serialize.domain.value = domain;
		return serialize;
	}
}