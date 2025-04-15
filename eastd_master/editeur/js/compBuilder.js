// Indexes referencing to components positions used outside of the compBuilder
const TEXT_INPUT = 1,
	ERROR_LABEL = 2,
	VARS_ADD_BTN = 1,
	VARS_LIST = 2,
	CHECKBOX = 0;

/**
 * Utility class used to build generic components that share the same behaviour/style
 * It is mainly used to build components of the left panel that are similar for links, states and astds
 * IMPORTANT: Event functions must be attached after the generation of the component as it is only an empty shell for the ui with initial values
 */
class CompBuilder {
	/**
	 * generate the component used to display errors associated to an input field or textArea
 	 * @returns {HTMLDivElement}
	 */
	static generateFieldError() {
		var label = document.createElement("div");
		label.style.margin = "0 12px";
		label.style.fontSize = "14px";
		label.innerHTML = "";
		label.style.color = "red";
		return label;
	}

	/**
	 * generate components with a label, input field (textarea or input) and error label.
	 * @param compType: type of the field to enter value. Must be a valid html element for text inputs
	 * @param labelContent: text of the label component that will be generated
	 * @param errorContent: text of the error label to display
	 * @param inputContent: value of the input field
	 * @param placeholder: placeholder to display when the input content is empty
	 * @returns {(HTMLLabelElement|HTMLTextAreaElement|HTMLDivElement)[]}: Resulting components always have the same structure
	 */
	static generateTextBlock(compType, labelContent, errorContent, inputContent, placeholder = "") {
		var label = document.createElement("label"),
			errorLabel = CompBuilder.generateFieldError(),
			input = document.createElement(compType);
		label.className = "panel-default";
		label.textContent = labelContent;

		input.className = "form-control form-control-sm panel-default input-field";
		input.value = inputContent;
		input.placeholder = placeholder;
		this.setInputValidity(input, errorLabel, errorContent);

		return [label, input, errorLabel];
	}

	/**
	 * generate a label that can collapse and expand the content it is associated to.
	 * @param labelName: Value of the label to display
	 * @param targetId: id of the component that is collapsible when the user click on the label.
	 * @returns {HTMLLabelElement}
	 */
	static generateCollapseLabel(labelName, targetId) {
		var label = document.createElement("label"),
			iconValue = "fa-angle-right";

		label.className = "collapsed panel-default to-expend";
		label.innerHTML = `<i class="fas ${iconValue}"></i>${labelName}`;
		label.onclick = function() {
			this.classList.toggle("collapsed");
			iconValue = iconValue === "fa-angle-right" ? "fa-angle-down" : "fa-angle-right";
			this.innerHTML = `<i class="fas ${iconValue}"></i>${labelName}`;
			$(`#${targetId}`).collapse("toggle");
			// toggle add button if there is any in label container
			$(this.parentElement).find(".add-btn").toggleClass("d-none");
		};

		return label;
	}

	/**
	 * generate all the components to display inherited variables and return them into a container
	 * @param inheritedVars: Array of all the inherited variables to display
	 * @returns {HTMLDivElement}
	 */
	static generateInheritedVarsSection(inheritedVars) {
		var container = document.createElement("div"),
			labelInheritedVar = CompBuilder.generateCollapseLabel("Inherited Variables", "inheritedList"),
			variablesList = document.createElement("ul"),
			hr = document.createElement("hr");

		variablesList.className = "collapse";
		variablesList.id = "inheritedList";

		if (inheritedVars.length === 0) {
			variablesList.textContent = "There is no inherited variables";
		}

		inheritedVars.forEach((variable) => {
			var varComponent = document.createElement("li"),
				parents = variable.getParents(),
				index = 0,
				c = 0;

			varComponent.textContent = variable.label + ":" + variable.type + " - " + variable.constructor.name.toLowerCase() + " - ";
			for (var j = parents.length - 1; j >= 0; j--) {
				if (parents[j] === "/") {
					c++;
				}
				if (c === 2) {
					index = j;
					break;
				}
			}
			if (index) {
				varComponent.textContent += "...";
			}
			varComponent.textContent += parents.substring(index, parents.length);

			variablesList.appendChild(varComponent);
		});

		[labelInheritedVar, variablesList, hr].forEach((component) => {
			container.appendChild(component);
		});

		return container;
	}

	/**
	 * Create a checkbox with its associated label. The first child of container is always the input element
	 * @param name of the checkbox label ( what will be display )
	 * @param checkValue: Initial value of the checkbox
	 * @returns Html Container with the checkbox and label associated
	 */
	static generateCheckboxBlock(name, checkValue) {
		var container = document.createElement("div"),
			labelTitle = document.createElement("label"),
			input = document.createElement("input");

		container.className = "form-check panel-default";

		labelTitle.className = "form-check-label";
		labelTitle.textContent = name;

		input.className = "form-check-input";
		input.type = "checkbox";
		input.checked = checkValue;

		container.appendChild(input);
		container.appendChild(labelTitle);

		return container;
	}

	/**
	 * Generate a basic select component with the options given
	 * @param options: options to be added to the select component
	 * @param selected: Element initially selected
	 * @returns {HTMLSelectElement}
	 */
	static generateSelectBlock(options, selected) {
		var select = document.createElement("select");
		select.className = "form-control form-control-sm panel-default";

		options.forEach(function(option) {
			var element = document.createElement("option");
			element.value = option;
			element.textContent = option;

			if (option === selected) {
				element.setAttribute("selected", "selected");
			}

			select.appendChild(element);
		});

		return select;
	}

	/**
	 * Generate all the components for a variable list section (attributes and parameters.
	 * @param labelContent: Value of the label to display
	 * @param targetId: id of the component that is collapsible when the user click on the label.
	 * @param vars: array of variables to display (either of type Parameter or Attribute)
	 * @param emptyContent: Text to display when there is no variable in the list
	 * @returns {(HTMLLabelElement|HTMLButtonElement|HTMLUListElement|HTMLHRElement)[]}: Resulting components always have the same structure (label, add button, list, hr)
	 */
	static generateVarsListSection(labelContent, targetId, vars, emptyContent) {
		var labelVar = CompBuilder.generateCollapseLabel(labelContent, targetId),
			addBtn = document.createElement("button"),
			varDiv = document.createElement("ul"),
			hr = document.createElement("hr");

		addBtn.className = "btn btn-outline-primary btn-sm d-none add-btn no-print";
		addBtn.textContent = "+";

		varDiv.className = "collapse";
		varDiv.id = targetId;
		if (vars.length === 0) {
			varDiv.textContent = emptyContent;
		}
		vars.forEach((elem) => {
			varDiv.appendChild(elem.toPanel());
		});
		// Simulate a click to display as readonly when building the whole list at once
		$(varDiv).find(".fa-check").click();

		return [labelVar, addBtn, varDiv, hr];
	}

	/**
	 * Apply input validation message and set the error label component with given error message
	 * @param inputComp: The input field (or textarea) to set valid status
	 * @param errorComp: error component displaying the error message
	 * @param message: Error message to affect to the input and to display
	 */
	static setInputValidity(inputComp, errorComp, message) {
		errorComp.innerHTML = message;
		inputComp.setCustomValidity(message);
	}
}