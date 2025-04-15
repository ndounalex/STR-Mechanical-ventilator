/***
 * Class Link
 * Represent transition model
 */
class Link {

	constructor(id = window.id.get(), source = null, destination = null, label = "?") {
		this.id = id;
		this.source = source;
		this.destination = destination;
		this.listView = new LinkView(this, source, destination); // The view !
		this.exported_array_parameters = [];
		this.exported_array_when = [];
		this.details = true;
		this.parameters = "";
		this.guard = "";
		this.when = "";
		this.code = "";
		this.com = "";
		this.type = "default";
		this.step = false;
		this.final = false;
		this.preferences = 3;
		this.comment_label = "";
		this.label = label;
		this.setArrowType();
		this.capture_identifiers = [];
		this.parameters_text_error = "";
		this.label_text_error = "";
		this.when_text_error = "";
		this.action_text_error = "";
		this.parameterize = new Map();
		this.test_parameters = [];
		this.comment_label_text_error = "";
	}

	get type() {
		return this.m_type;
	}

	set type(type) {
		this.m_type = type;
		this.listView.updateConnection();
	}

	get step() {
		return this.m_step;
	}

	set step(step) {
		this.m_step = step;
		this.listView.updateConnection();
	}

	get final() {
		return this.m_final;
	}

	set final(final) {
		this.m_final = final;
		this.listView.updateConnection();
	}

	get id() {
		return this.m_id;
	}

	set id(id) {
		this.m_id = id;
	}

	get source() {
		return this.m_source;
	}

	set source(source) {
		this.m_source = source;
	}

	get destination() {
		return this.m_destination;
	}

	set destination(destination) {
		this.m_destination = destination;
	}

	get arrowType() {
		return this.m_arrowType;
	}

	set arrowType(arrowType) {
		this.m_arrowType = arrowType;
	}

	get code() {
		return this.m_code;
	}

	set code(code) {
		this.m_code = code;
	}

	get label() {
		return this.m_label;
	}

	set label(label) {
		this.m_label = label;
		this.refreshLinkLabel();
	}

	select() {
		$(this.listView.e_item).addClass("selected-linklabel");
	}

	unselect() {
		$(this.listView.e_item).removeClass("selected-linklabel");
		this.listView.unfocusConnections();
	}

	refreshLinkLabel() {
		var compactLabel = this.label,
			compactParams = "",
			fullLabel = this.label,
			fullParams = "",
			paramLabel = "(" + this.parameters + ")",
			codeLabel = this.code[0] === "{" ? "/" + this.code : "/{" + this.code + "}",
			guardLabel = "[" + this.guard;
		guardLabel += this.guard && this.when ? "&#8743" + this.when + "]" : this.when + "]";
		if (this.step) {
			compactLabel = "Step";
			fullLabel = "Step";
		}
		if (this.test_parameters) {
			this.test_parameters.forEach((element) => {
				return compactLabel += "_$" + element;
			});
			this.test_parameters.forEach((element) => {
				return fullLabel += "_$" + element;
			});
		}
		//// Ici pour changer ce qui est afficher quand on survole la transition
		// if (this.parameters.length > this.preferences) {
		// 	compactLabel += "(...)";
		// } else if (this.parameters.length > 0) {
		// 	compactLabel += paramLabel;
		// }
		// if (this.guard.length + this.when.length > this.preferences) {
		// 	compactParams = "[...]";
		// } else if (this.guard.length + this.when.length > 0) {
		// 	compactParams = guardLabel;
		// }
		// if (this.code.length > this.preferences) {
		// 	compactParams += "/{...}";
		// } else if (this.code.length > 0) {
		// 	compactParams += codeLabel;
		// }

		if (this.parameters) {
			fullLabel += paramLabel;
		}
		if (this.guard || this.when) {
			fullParams += guardLabel;
		}
		if (this.code) {
			fullParams += codeLabel;
		}
		if (this.comment_label === "") {
			this.listView.refreshLabel(compactLabel, compactParams, fullLabel, fullParams);
		} else {
			this.listView.refreshLabel(this.comment_label, "", this.comment_label, "");
		}
	}

	get parameters() {
		return this.m_parameters;
	}

	set parameters(parameters) {
		this.m_parameters = parameters;
	}

	get preferences() {
		return this.m_preferences;
	}

	set preferences(preferences) {
		this.m_preferences = preferences;
	}

	get guard() {
		return this.m_guard;
	}

	set guard(guard) {
		this.m_guard = guard;
	}

	get when() {
		return this.m_when;
	}

	set when(when) {
		this.m_when = when;
	}

	get details() {
		return this.m_details;
	}

	set details(details) {
		this.m_details = details;
	}

	delete() {
		this.listView.delete();
	}

	log_input_error(message_to_log, field) {
		window.konsole.log_error(this, { label: message_to_log }, field + " error.");
	}

	getInheritedVariables() {
		return this.source.parent.getFile().getInheritedVariables(this.source.parent);
	}

	verify() {
		if (this.type === SEQUENCE) return;
		// eslint-disable-next-line no-undef
		if (this.type === TIMEOUT) return;
		if (this.arrowType === "") {
			window.konsole.log_error(this, this, "crosses more than one level of hierarchy");
		}
		// label is required, so we always check for errors
		if (this.label_text_error.length !== 0) {
			this.log_input_error(this.label, "label");
		}
		if (this.parameters && this.parameters_text_error.length !== 0) {
			this.log_input_error(this.parameters, "parameters");
		}
		if (this.when && this.when_text_error.length !== 0) {
			this.log_input_error(this.when, "when");
		}
		if (this.code && this.action_text_error.length !== 0) {
			this.log_input_error(this.code, "action");
		}
	}

	/**
	 *  Validates the value of the specified attribute and updates associated parameters
	 * @param attribute: the attribute to be validated
	 * @param textErrorName: the text error attribute name to store the error message, if any
	 * @param grammar: the grammar to use to validate the syntax of the attribute
	 * @param checkSemanticFunc: the function to call if there is any semantic to check. This function must always take 2 parameters
	 * @param exportedArrayName: the array attribute name where to store the parameters useful for exporting. By default, there is none
	 * @return a string containing the error message
	 */
	validateValue(attribute, textErrorName, grammar, checkSemanticFunc = null, exportedArrayName = "") {
		var verifyResult = Validator.syntax_Verify(attribute, grammar);

		if (attribute && verifyResult.message === "" && checkSemanticFunc) {
			this[textErrorName] = checkSemanticFunc.call(Validator, verifyResult.result, this);

			if (exportedArrayName) {
				this[exportedArrayName] = verifyResult.result;
			}

			return this[textErrorName];
		}

		this[textErrorName] = verifyResult.message;
		return this[textErrorName];
	}

	/**
	 * Valide le texte pour l'etiquette de transition
	 */
	validateCommentLabel() {
		return "";
	}

	/**
	 * Utility getter used to create components of a link
	 * @returns object containing functions to call to create a component
	 */
	get linkPanel() {
		var that = this;

		/**
		 * Utility function to execute blur action on enter key pressed. Must be bind to the input associated to the action
		 * @param event: fired event object
		 */
		function executeOnKeyUp(event) {
			if (event.keyCode === 13 /*enter key*/) {
				this.blur();
			}
		}

		return {
			createHeader: function() {
				var container = document.createElement("div"),
					title = document.createElement("h5"),
					labelInput = document.createElement("input"),
					labelError = CompBuilder.generateFieldError(),
					typeLabel = document.createElement("div"),
					checkFinal = CompBuilder.generateCheckboxBlock("Final", that.final),
					checkStep = CompBuilder.generateCheckboxBlock("Step", that.step),
					hr = document.createElement("hr");

				title.textContent = "Transition";
				labelInput.className = "form-control inline-input";
				labelInput.id = "linkNameInputId";
				labelInput.placeholder = "Label";
				labelInput.value = that.label;
				labelInput.onblur = function() {
					that.label = labelInput.value;
					CompBuilder.setInputValidity(labelInput, labelError, that.validateValue(that.label, "label_text_error", window.event_name_grammar_object));
				};
				labelInput.onkeyup = executeOnKeyUp.bind(labelInput);
				CompBuilder.setInputValidity(labelInput, labelError, Validator.syntax_Verify(that.label, window.event_name_grammar_object).message);

				typeLabel.className = "panel-default";
				typeLabel.textContent = that.arrowType;

				checkFinal.children[CHECKBOX].onclick = function() {
					that.final = checkFinal.children[CHECKBOX].checked;
				};

				checkStep.children[CHECKBOX].onclick = function() {
					that.step = checkStep.children[CHECKBOX].checked;

					if (that.step) {
						that.label = "Step";
						labelInput.value = that.label;
						labelInput.disabled = true;
						that.refreshLinkLabel();
					} else {
						that.label = "default";
						labelInput.value = that.label;
						labelInput.disabled = false;
						that.refreshLinkLabel();
					}
				};

				[title, labelInput, labelError, typeLabel, checkFinal, checkStep, hr].forEach((comp) => {
					container.appendChild(comp);
				});

				return container;
			},

			createInheritedVars: function() {
				return CompBuilder.generateInheritedVarsSection(that.getInheritedVariables());
			},

			createPreferences: function() {
				var container = document.createElement("div"),
					labelPref = document.createElement("label"),
					input = document.createElement("input"),
					checkFix = CompBuilder.generateCheckboxBlock("Stick label to transition", that.listView.fix),
					hr = document.createElement("hr");

				labelPref.className = "panel-default";
				labelPref.textContent = "Displayed number:";

				input.className = "form-control form-control-sm inline-input";
				input.id = "displayNumInput";
				input.type = "text";
				input.value = that.preferences;
				input.onblur = function() {
					that.preferences = input.value;
					that.refreshLinkLabel();
				};
				input.onkeyup = executeOnKeyUp.bind(input);

				checkFix.children[CHECKBOX].onclick = function() {
					that.listView.fix = checkFix.children[CHECKBOX].checked;
				};

				[labelPref, input, checkFix, hr].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			},

			createEventSettings: function() {
				var errors = {
						parameters: that.validateValue(that.parameters, "parameters_text_error", window.event_parameters_grammar_object, Validator.checkEventParametersSemantic, "exported_array_parameters"),
						when: that.validateValue(that.when, "when_text_error", window.when_grammar_object, Validator.checkWhenSemantic, "exported_array_when"),
						action: that.validateValue(that.code, "action_text_error", window.action_grammar_object, Validator.checkActionSemantic),
						guard: "",
						com: ""
					},
					parameterBlock = CompBuilder.generateTextBlock("textarea", "Parameters:", errors.parameters, that.parameters, "Ex: a, a.b, ?c:type, {any constant}"),
					whenBlock = CompBuilder.generateTextBlock("textarea", "When:", errors.when, that.when, "Ex: a.b=c && a=b.c || a=b "),
					guardBlock = CompBuilder.generateTextBlock("textarea", "Guard:", errors.guard, that.guard, "Ex: x=1 | guard_file.guard"),
					codeBlock = CompBuilder.generateTextBlock("textarea", "Action:", errors.action, that.code, "Ex: M.f(args) | M::f(args) | {c++ code}"),
					comBlock = CompBuilder.generateTextBlock("textarea", "Comment:", errors.com, that.com, "Ex: 'put what you want here'"),
					container = document.createElement("div"),
					hr = document.createElement("hr");

				// Attach function to execute on blur of each textArea
				parameterBlock[TEXT_INPUT].onblur = function() {
					that.parameters = parameterBlock[TEXT_INPUT].value;
					that.refreshLinkLabel();
					that.capture_identifiers = [];
					that.validateValue(that.parameters, "parameters_text_error", window.event_parameters_grammar_object, Validator.checkEventParametersSemantic, "exported_array_parameters");
					window.panel.show();
				};

				whenBlock[TEXT_INPUT].onblur = function() {
					that.when = whenBlock[TEXT_INPUT].value;
					const errorMessage = that.validateValue(that.when, "when_text_error", window.when_grammar_object, Validator.checkWhenSemantic, "exported_array_when");
					CompBuilder.setInputValidity(whenBlock[TEXT_INPUT], whenBlock[ERROR_LABEL], errorMessage);
					that.refreshLinkLabel();
				};

				guardBlock[TEXT_INPUT].onblur = function() {
					that.guard = guardBlock[TEXT_INPUT].value;
					that.refreshLinkLabel();
				};

				codeBlock[TEXT_INPUT].onblur = function() {
					that.code = codeBlock[TEXT_INPUT].value;
					that.refreshLinkLabel();
					const errorMessage = that.validateValue(that.code, "action_text_error", window.action_grammar_object, Validator.checkActionSemantic);
					CompBuilder.setInputValidity(codeBlock[TEXT_INPUT], codeBlock[ERROR_LABEL], errorMessage);
				};

				comBlock[TEXT_INPUT].onblur = function() {
					that.com = comBlock[TEXT_INPUT].value;
					that.refreshLinkLabel();
				};

				// take all components and add them to the container
				[...parameterBlock, ...whenBlock, ...guardBlock, ...codeBlock, ...comBlock, hr].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			},
			createCommentLabel: function() {
				var container = document.createElement("div"),
					comBlock_commentLabel = CompBuilder.generateTextBlock("input", "Comment label:", that.validateCommentLabel(), that.comment_label, "Comment in place of transition label");

				comBlock_commentLabel[TEXT_INPUT].onblur = function() {
					that.comment_label = comBlock_commentLabel[TEXT_INPUT].value;
					CompBuilder.setInputValidity(comBlock_commentLabel[TEXT_INPUT], comBlock_commentLabel[ERROR_LABEL], that.validateCommentLabel());
					that.refreshLinkLabel();
				};

				comBlock_commentLabel[TEXT_INPUT].onkeyup = function(key) {
					if (key.code === "Enter") {
						that.comment_label = comBlock_commentLabel[TEXT_INPUT].value;
						CompBuilder.setInputValidity(comBlock_commentLabel[TEXT_INPUT], comBlock_commentLabel[ERROR_LABEL], that.validateCommentLabel());
						that.refreshLinkLabel();
					}
				};

				comBlock_commentLabel.forEach((component) => {
					container.appendChild(component);
				});

				return container;
			},

			createTestParameters: function() {
				var container = document.createElement("div"),
					label = CompBuilder.generateCollapseLabel("Test Parameters", "testParamList"),
					testParameters = document.createElement("ul"),
					hr = document.createElement("hr");

				testParameters.className = "collapse";
				testParameters.id = "testParamList";
				that.refreshLinkLabel();
				if (that.source.test_parameters.length === 0) {
					testParameters.textContent = "There is no test parameters";
				}
				Array.from(that.parameterize.keys()).forEach((test_parameter) => {
					if (!that.source.test_parameters.includes(test_parameter)) {
						that.test_parameters = that.test_parameters.filter((item) => {
							return item !== test_parameter;
						});
						that.parameterize.delete(test_parameter);
					}
				});
				that.source.test_parameters.forEach((test_parameter, index) => {
					var checkParam = CompBuilder.generateCheckboxBlock(test_parameter, that.parameterize.get(test_parameter));
					checkParam.classList.add("ml-0", "pl-1");

					checkParam.children[CHECKBOX].onclick = function() {
						that.parameterize.set(test_parameter, event.target.checked);
						if (that.parameterize.get(test_parameter)) {
							that.test_parameters.push(test_parameter);
						} else {
							that.test_parameters = that.test_parameters.filter((item) => {
								return item !== test_parameter;
							});
						}
						that.refreshLinkLabel();
					};

					if (that.parameterize.get(test_parameter)) {
						if (!that.test_parameters.includes(test_parameter)) {
							that.test_parameters[index] = test_parameter;
						}
						that.refreshLinkLabel();
					}

					testParameters.appendChild(checkParam);
				});

				[label, testParameters, hr].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			}
		};
	}

	toPanel() {
		var that = this,
			defaultComponents = [this.linkPanel.createHeader, this.linkPanel.createInheritedVars, this.linkPanel.createPreferences, this.linkPanel.createEventSettings, this.linkPanel.createTestParameters, this.linkPanel.createCommentLabel];

		return (function() {
			var container = document.createElement("div");

			// eslint-disable-next-line no-undef
			if (that.type !== SEQUENCE && that.type !== TIMEOUT) {
				defaultComponents.forEach((componentFunc) => {
					container.appendChild(componentFunc());
				});
			}
			return container;
		})();
	}

	save() {
		return {
			id: this.id,
			source: this.source.id,
			destination: this.destination.id,
			details: this.details,
			comment: this.com,
			parameters: this.parameters,
			guard: this.guard,
			when: this.when,
			displayNumber: this.preferences,
			code: this.code,
			label: this.label,
			type: this.type,
			final: this.final,
			inflexion: this.listView.save(),
			test_parameters: this.test_parameters,
			commentLabel: this.comment_label,
			parameterize: JSON.stringify([...this.parameterize])
		};
	}

	/**
	 * Validates all attributes with check at once
	 */
	validateInputs() {
		this.validateValue(this.label, "label_text_error", window.event_name_grammar_object);
		this.validateValue(this.parameters, "parameters_text_error", window.event_parameters_grammar_object, Validator.checkEventParametersSemantic, "exported_array_parameters");
		this.validateValue(this.code, "action_text_error", window.action_grammar_object, Validator.checkActionSemantic);
		this.validateValue(this.when, "when_text_error", window.when_grammar_object, Validator.checkWhenSemantic, "exported_array_when");
	}

	load(link) {

		// only load listview if inflexion points were saved
		if (link.inflexion) {
			this.listView.load(link.inflexion);
		}

		this.details = true;
		// this.parameters = "";
		// this.guard = "";
		// this.when = "";
		// this.code = "";
		// this.label = "?";
		this.type = "default";
		this.final = link.final;
		this.details = link.details;
		this.parameters = link.parameters;
		this.guard = link.guard;
		this.when = link.when;
		this.preferences = link.displayNumber;
		this.code = link.code;
		this.test_parameters = link.test_parameters;
		this.parameterize = new Map(JSON.parse(link.parameterize));
		this.label = link.label;
		this.type = link.type;
		this.com = link.comment;

		// A enlever quand tout les fichiers auront la nouvelle version
		if (link.commentLabel === undefined) {
			link.commentLabel = "";
		}
		this.comment_label = link.commentLabel;

		this.validateInputs();
		this.refreshLinkLabel();
	}

	setArrowType() {
		var length_src_p = this.source.parent.tabs.length,
			length_dest_p = this.destination.parent.tabs.length;
		this.arrowType = "";
		if (this.source.parent.tabs[length_src_p - 1] === this.destination.parent.tabs[length_dest_p - 1]) {
			this.arrowType = "Local";
		}
		if (length_dest_p >= 2) {
			if (this.source.parent.tabs[length_src_p - 1] === this.destination.parent.tabs[length_dest_p - 2]) this.arrowType = "ToSub";
		} else {
			if (this.destination.parent.parent && this.destination.parent.parent instanceof ASTD) {
				var length_dest_pp = this.destination.parent.parent.tabs.length;
				if (this.source.parent.tabs[length_src_p - 1] === this.destination.parent.parent.tabs[length_dest_pp - 1]) this.arrowType = "ToSub";
			}
		}
		if (length_src_p >= 2) {
			if (this.source.parent.tabs[length_src_p - 2] === this.destination.parent.tabs[length_dest_p - 1]) this.arrowType = "FromSub";
		} else {
			if (this.source.parent.parent && this.source.parent.parent instanceof ASTD) {
				var length_src_pp = this.source.parent.parent.tabs.length;
				if (this.source.parent.parent.tabs[length_src_pp - 1] === this.destination.parent.tabs[length_dest_p - 1]) this.arrowType = "FromSub";
			}
		}

	}

	export(test_parameter, parameter_name, external_parameter_name) {
		let serialize = {
			arrow_type: this.arrowType,
			arrow: {
				from_state_name: this.source.label,
				to_state_name: this.destination.label
			},
			event_template: this.serializeEventTemplate(test_parameter, parameter_name, external_parameter_name),
			guard: this.guard,
			action: this.code,
			step: this.step,
			from_final_state_only: this.final
		};
		if (test_parameter && this.source instanceof ASTD) {
			test_parameter.forEach((element) => {
				serialize.arrow.from_state_name += "_" + element;
			});
		}
		if (test_parameter && this.destination instanceof ASTD) {
			test_parameter.forEach((element) => {
				serialize.arrow.to_state_name += "_" + element;
			});
		}
		if (this.arrowType === "ToSub") {
			serialize.arrow.through_state_name = this.destination.parent.label;
		} else if (this.arrowType === "FromSub") {
			serialize.arrow.through_state_name = this.source.parent.label;
		}
		if (test_parameter) {
			test_parameter.forEach((element) => {
				serialize.arrow.through_state_name += "_" + element;
			});
		}

		return serialize;
	}

	/**
	 * This function is paert of the export
	 * This is a function that return the serialize object for the event_template attribute of the export
	 * It could be directly put in the export function, it's been splitted from the original function
	 * for human-reading friendliness.
	 */
	serializeEventTemplate(test_parameter, parameter_name, external_parameter_name) {
		let serializeEventTemplate = {
			label: this.label,
			parameters: this.exported_array_parameters.map((param) => {
				switch (param.type) {
				case "identifier":
					return {
						parameter_kind: "Expression",
						parameter: param.value
					};
				case "member_accessor":
					return {
						parameter_kind: "Expression",
						parameter: param.value.base_identifier + "." + param.value.attributes_list.join(".")
					};
				case "capture":
					return {
						parameter_kind: "Capture",
						parameter: {
							variable_name: param.value.identifier,
							type: param.value.type
						}
					};
				case "json":
					return {
						parameter_kind: "Constant",
						parameter: param.value
					};
				case "joker":		// if it is a jocker, so we just push parameter kind but not the value '_'
					return { parameter_kind: "Joker" };
				default:
					// No Default Case
				}
			}),
			when: this.exported_array_when.map((conjunctions) => {
				return conjunctions.map((conjunction) => {
					const first_var_to_push = [conjunction.first.base_identifier].concat(conjunction.first.attributes_list).join("."),
						second_var_to_push = [conjunction.second.base_identifier].concat(conjunction.second.attributes_list).join(".");

					return first_var_to_push + "=" + second_var_to_push;
				});
			})
		};
		/**
		 * L’ajout du paramètre “ex” a une transition signifie que les valeurs des paramètres externes du Call
		 * seront ajoutées au nom de la transition
		 * External_parameter_name permet de savoir la longueur des paramètres de test externes, et par la suite
		 * récupérer leurs valeurs du test_parameter, le reste des valeurs dans test_parameter est associé aux
		 * test_parameter de la transition
		 */
		if (test_parameter && parameter_name) {
			if (this.test_parameters.includes("ex")) {
				for (let i = 0; i < external_parameter_name.length; i++) {
					serializeEventTemplate.label += "_" + test_parameter[i];
				}
			}
			parameter_name.forEach((element, index) => {
				if (this.test_parameters.includes(element)) {
					if (external_parameter_name) {
						serializeEventTemplate.label += "_" + test_parameter[index + external_parameter_name.length];
					} else {
						serializeEventTemplate.label += "_" + test_parameter[index];
					}
				}
			});
		}
		return serializeEventTemplate;
	}

	copy(link) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedLink(link.id, link.source, link.destination, link.label, link.detail, link.parameters, link.guard, link.when, link.code, link.com, link.default, link.step, link.final);
	}
}

class CopiedLink {
	constructor(id, source = null, destination = null, label = "?", details = true, parameters = "", guard = "", when = "", code = "", com = "", type = "default", step = false, final = false) {
		this.id = id;
		this.source = source;
		this.destination = destination;
		this.label = label;

		this.details = details;
		this.parameters = parameters;
		this.guard = guard;
		this.when = when;
		this.code = code;
		this.com = com;
		this.type = type;
		this.step = step;
		this.final = final;
	}

	paste(source, destination) {
		let link = new Link(window.id.get(), source, destination, this.label);
		link.details = this.details;
		link.parameters = this.parameters;
		link.guard = this.guard;
		link.when = this.when;
		link.code = this.code;
		link.com = this.com;
		link.type = this.type;
		link.step = this.step;
		link.final = this.final;
		return link;
	}
}

