class Validator {
	/**
	 * Used to validate inherited variable
	 * @param component: to get inherited variable from
	 * @returns {{name: *, type: *}[]}
	 */
	static inherited_real_variables(component) {
		return component.getInheritedVariables().map((inheritedVar) => {
			return {
				name: inheritedVar.label,
				type: inheritedVar.type
			};
		});
	}

	/**
	 * Validate Syntax on a string with a given grammar
	 * @param {*} toCheck 			string to validate
	 * @param {*} grammar_object 	given grammar
	 * @param {*} isMandatory 		if field is mandatory
	 * @Return
	 * 	+errorMessage: self-explanatory [None if correct]
	 *  +result: result of parser validation
	 *  +isValid: boolean that indicate if string match gramar or not
	 */
	static syntax_Verify(toCheck, grammar_object, isMandatory = false) {
		//global object to hand extracted field text
		toCheck = toCheck.replace(/\n/g, "");
		toCheck = toCheck.replace(/" "/g, "");
		var mytext = isMandatory ? "This field is mandatory" : "";
		if (toCheck.length === 0 && grammar_object !== window.event_name_grammar_object) {
			return {
				message: mytext,
				result: null
			};
		}

		// Create a Parser object from object given in parameter (grammar_object).
		const parser = new nearley.Parser(nearley.Grammar.fromCompiled(grammar_object));
		try {
			parser.feed(toCheck);
			if (parser.results.length) {
				return {
					message: "",
					result: parser.results[0],
					isValid: true
				};
			} else {
				return {
					message: "incomplete syntax",
					result: null,
					isValid: false
				};
			}

		} catch (err) {
			var character = parser.lexer.buffer[parser.current],
				errorAt = toCheck.trim().indexOf(character) + 1;
			mytext = "Unexpected character " + "'" + character + "'" + "(" + errorAt + ")";

			return {
				message: mytext,
				result: null,
				isValid: false
			};
		}
	}

	//checking fonction parameters if theys exist in inherited variables
	static check_semantic_with_inherited_parameters(tableOfParameters, all_inheritedVariables) {
		var returnedText = "",
			inheritedParameters = all_inheritedVariables.map((inheritedVar) => {
				return inheritedVar.name;
			}),
			nonexistentVariables = [],
			badMembers = [];

		tableOfParameters.forEach((parameter) => {
			var variableName = parameter.identifier.trim();
			if (variableName.indexOf("!") === 0) {
				variableName = variableName.substring(1, variableName.length);
			}
			if (!inheritedParameters.includes(variableName)) {
				//semantic is incorrect, variable doesn't exist
				nonexistentVariables.push(variableName);
			} else if (parameter.member_accessors && parameter.member_accessors.length > 0) { // check for member_accessors
				var refVariable = all_inheritedVariables.find((inheritedVar) => {
					return inheritedVar.name === variableName;
				});

				if (!project.isNativeType(refVariable.type) && !project.complexStructure.propertyExists(refVariable.type, parameter.member_accessors)) {
					// Semantic is incorrect, member accessor doesn't exist for variable type
					badMembers.push(parameter.member_accessors.join("."));
				}
			}
		});

		if (nonexistentVariables.length > 0) {
			returnedText = "Inherited variables don't contain the variable(s) \"" + nonexistentVariables.join("\", \"") + "\".";
		}

		if (badMembers.length > 0) {
			returnedText += "Member(s) \"" + badMembers.join("\", \"") + "\" don't exist.";
		}
		return returnedText;
	}

	static checkEventParametersSemantic(parameters, that) {
		that.capture_identifiers = [];
		var identifiers_and_members_accessor = [],
			errorMessage = "",
			invalidCapturedVar = [];
		parameters.forEach((parameter) => {
			switch (parameter.type) {
			case "identifier":
				identifiers_and_members_accessor.push({ identifier: parameter.value });
				break;
			case "member_accessor":
				identifiers_and_members_accessor.push({
					identifier: parameter.value.base_identifier,
					member_accessors: parameter.value.attributes_list
				});
				break;
			case "capture":
				that.capture_identifiers.push({
					name: parameter.value.identifier,
					type: parameter.value.type
				});
				if (!project.isValidType(parameter.value.type)) {
					invalidCapturedVar.push(parameter.value.identifier);
				}
				break;
			case "json":
				//@todo
				break;
			}
		});

		if (invalidCapturedVar.length > 0) {
			errorMessage = "Invalid type for variable(s) \"" + invalidCapturedVar.join("\", \"") + "\". ";
		}
		return errorMessage + this.check_semantic_with_inherited_parameters(identifiers_and_members_accessor, Validator.inherited_real_variables(that));
	}

	// Function that checks extract parameters semantic got from call parameters
	static checkCallParametersSemantic(parameter, that, type) {
		var identifiers_and_members_accessor,
			errorMessage = "";
		switch (parameter.type) {
		case "identifier":
			identifiers_and_members_accessor = { identifier: parameter.value };
			break;
		case "member_accessor":
			identifiers_and_members_accessor = {
				identifier: parameter.value.base_identifier,
				member_accessors: parameter.value.attributes_list
			};
			break;
		case "json":
			if (project.isComplexType(type) && !project.complexStructure.validate(type, parameter.value))
				errorMessage = project.complexStructure.error.message;
			break;
		default:
		}
		// identifiers_an_members_accessor is undefined when json type. Return errorMessage or empty string in that case
		return identifiers_and_members_accessor ? this.check_semantic_with_inherited_parameters([identifiers_and_members_accessor], this.inherited_real_variables(that)) : errorMessage;
	}

	// Function that checks extract parameters semantic got from when
	static checkWhenSemantic(conjunctions, that) {
		var when_identifiers = [];
		conjunctions.forEach((conjunction) => {
			conjunction.forEach((parameter) => {
				var currentEquality = [{ //first base identifier of equality (e.g: a=b)
					identifier: parameter.first.base_identifier,
					member_accessors: parameter.first.attributes_list
				},
				{ //second base identifier of equality (e.g: a=b)
					identifier: parameter.second.base_identifier,
					member_accessors: parameter.second.attributes_list
				}];
				when_identifiers = when_identifiers.concat(currentEquality);
			});
		});

		return this.check_semantic_with_inherited_parameters(when_identifiers, Validator.inherited_real_variables(that).concat(that.capture_identifiers));
	}

	// Function that checks extract parameters semantic got from actions
	static checkActionSemantic(parameters, that) {
		// If it is inline code, we only verify the target language
		if (parameters.inline_code) {
			return project.targetLanguage === "C++" ? "" : "Inline code is only possible for C++ target language.";
		}

		if (parameters.target !== project.targetLanguage) {
			return `The code should be written in ${project.targetLanguage}.`;
		}

		var identifiers_and_members_accessor = parameters.parameters_list.map((parameter) => {
				switch (parameter.type) {
				case "identifier":
					return { identifier: parameter.value };
				case "member_accessor":
					return {
						identifier: parameter.value.base_identifier,
						member_accessors: parameter.value.attributes_list
					};
				}
			}),
			Inherited_and_capture_variables = that.capture_identifiers ? Validator.inherited_real_variables(that).concat(that.capture_identifiers) : Validator.inherited_real_variables(that);
		return this.check_semantic_with_inherited_parameters(identifiers_and_members_accessor, Inherited_and_capture_variables);
	}

	static checkTestDomainSyntax(domain_value) {
		var domainValues = domain_value.split("..");
		if (domain_value.indexOf("..") === -1) {
			return "incorrect syntax";
		} else if (isNaN(domainValues[0]) || parseFloat(domainValues[0]) !== parseInt(domainValues[0])) {
			return domainValues[0] + " is not an Integer";
		} else if (isNaN(domainValues[1]) || parseFloat(domainValues[1]) !== parseInt(domainValues[1])) {
			return domainValues[1] + " is not an Integer";
		} else if (parseInt(domainValues[0]) > parseInt(domainValues[1])) {
			return domainValues[0] + " > " + domainValues[1];
		} else {
			return "";
		}
	}

	/**
	 * Returns an instance of ajv used only by the validator to check for objects in the app that don't have link with complex types.
	 * @returns {ajv | ajv.Ajv} Creates a new instance of ajv if it doesn't already exists and returns the instance
	 */
	static get ajv() {
		if (!this.m_ajv) {
			this.m_ajv = new Ajv({
				schemas:
					[{
						$schema: "http://json-schema.org/schema#",
						$id: "__event_signature__",
						type: "object",
						additionalProperties: {
							type: "array",
							items: { type: "string" }
						}
					}]
			});
		}
		return this.m_ajv;
	}
}
