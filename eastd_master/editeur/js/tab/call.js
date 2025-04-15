const CALL = "Call";

class Call extends Tab {

	constructor(id = window.id.get(), parent = null, astd = null, argument = []) {
		super(id, parent);
		this.e_label = this.createContent();
		this.astd = astd;
		this.target = "null";
		this.argument = argument;
		this.tabLabelError = [];
		this.callError = "";
		this.calledAstdNull = "";

	}

	toHtml() {
		return CALL;
	}

	getType() {
		return this.astd && this.astd.tabs[0] ? CALL + ": " + this.astd.tabs[0].getType() : CALL;
	}

	/**
	 * This function makes sure to go through every parameter and
	 * fill the tabLabelError which is required to be laod on load
	 * and after change on call argument.
	 */
	forceLoadTabError() {
		if (this.astd) {
			this.astd.parameters.forEach((param, index) => {
				var argumValue = this.argument[index] ? this.argument[index] : "";
				this.validateCalledParam(argumValue, index, param.type);
			});
		}
	}

	verify_calledAstdNull() {
		if (!this.astd) {
			this.calledAstdNull = "called a null ASTD";
		}
	}

	verify_testParameters() {
		if (this.astd && this.parent.test_parameters.length === 0 && this.astd.externalTest) {
			this.callError = "Call ASTD without test parameters calling a Top-level ASTD with external test parameter";
		}
	}

	get argument() {
		return this.m_argument;
	}

	set argument(argument) {
		this.m_argument = argument;
	}

	get astd() {
		if (!this.m_astd)
			this.getCallAstd();
		return this.m_astd;
	}

	set astd(astd) {
		this.m_astd = astd;
		this.refreshCalledASTD();
	}

	getCallAstd() {
		var that = this;
		window.project.files.forEach((file) => {
			if (file.astd && file.astd.label === that.target) {
				that.astd = file.astd;
			}
		});
		return that.target;
	}

	createContent() {
		var callLabel = document.createElement("div"),
			compactLabel = document.createElement("div"),
			fullLabel = document.createElement("div");
		compactLabel.id = "compLabel";
		fullLabel.id = "fullLabel";
		compactLabel.innerHTML = "No ASTD selected";
		fullLabel.innerHTML = "No ASTD selected";
		callLabel.style.marginTop = "35%";
		[compactLabel, fullLabel].forEach((component) => {
			callLabel.appendChild(component);
		});
		$("#" + this.parent.id + " .content")[0].appendChild(callLabel);
		callLabel.onmouseenter = this.toggleContainer.bind(callLabel);
		callLabel.onmouseleave = this.toggleContainer.bind(callLabel);
		return callLabel;
	}

	toggleContainer() {
		this.classList.toggle("expand");
	}

	updateContentDisplay() {
		var testParameters = "",
			calledArgumentsComp = "()",
			calledArguments = "()";
		// On load, target astd might not be loaded already
		if (this.astd && this.astd.external_test_parameters) {
			this.astd.test_parameters.forEach((element) => {
				return testParameters += "_$" + element;
			} );
		}
		// Append argument to display content
		if (this.argument.length > 0) {
			calledArguments = "(";
			this.argument.forEach((arg) => {
				calledArguments += arg += ",";
			});
			calledArguments = calledArguments.slice(0, -1);
			calledArguments += ")";
		}
		if (this.argument.length > 2) {
			calledArgumentsComp = "(...)";
		} else if (this.argument.length > 0) {
			calledArgumentsComp = calledArguments;
		}
		this.e_label.children[0].innerHTML = this.target + testParameters + calledArgumentsComp;
		this.e_label.children[1].innerHTML = this.target + testParameters + calledArguments;
	}

	refreshCalledASTD() {
		if (this.astd) {
			for (let i = 0; i < this.astd.called.length; i++) {
				if (this.astd.called.includes(this.parent)) {
					this.astd.called.push(this.parent);
					break;
				}
			}
			this.updateContentDisplay();
		} else {
			this.e_label.children[0].innerHTML = "No ASTD selected";
		}
	}

	validateCalledParam(calledParam, index, type) {
		var verify_result = Validator.syntax_Verify(calledParam, window.call_parameters_grammar_object, true);
		this.tabLabelError[index] = verify_result.message;

		if (this.tabLabelError[index].length === 0) {
			this.tabLabelError[index] = Validator.checkCallParametersSemantic(verify_result.result, this, type);
		}
		return this.tabLabelError[index];
	}

	delete() {
		this.e_label.remove();
		return super.delete();
	}

	/**
	* Utility getter used to create specific components of the call
	* @return: object containing functions to call to create a component
	*/
	get callPanel() {
		var that = this;

		return {
			createCall: function() {
				var options = window.project.files.filter((file) => {
						return file.astd.label !== that.parent.label || that.parent.tabs.length > 1;
					}).map((file) => {
						return file.astd.label;
					}),
					container = document.createElement("div"),
					labelTarget = document.createElement("label"),
					selectTarget = CompBuilder.generateSelectBlock(["null"].concat(options), that.getCallAstd()),
					hr = document.createElement("hr");

				labelTarget.className = "panel-default";
				labelTarget.textContent = "Target:";

				selectTarget.classList.replace("panel-default", "select-target");
				selectTarget.onchange = function() {
					that.target = selectTarget.value;
					that.tabLabelError = [];
					if (that.getCallAstd() === "null") {
						that.astd = null;
						that.argument = [];
					}

					if (that.parent.tabs[0] === that)
						window.panel.show(that.parent);
					else
						window.panel.show(that);
				};

				container.appendChild(labelTarget);
				container.appendChild(selectTarget);

				if (that.astd) {

					if (that.parent.test_parameters && that.astd.externalTest) {
						//voir le get test_parameters; ceci permet de charger that.test_parameters par les exteranal_test_parameters au lieu de "ex"
						that.astd.externalTest = false;
						that.astd.external_test_parameters = [];
						that.parent.test_parameters.forEach((element) => {
							return that.astd.external_test_parameters.push(element);
						});
						that.updateContentDisplay();
						//permettre l'appel de that.astd par un autre Call d'autre external_test_parameters
						that.astd.externalTest = true;
					}

					// Gestion des paramètres du call
					var labelParam = CompBuilder.generateCollapseLabel("Called ASTD parameters", "calledParamList"),
						calledParamsList = document.createElement("ul");

					calledParamsList.className = "collapse";
					calledParamsList.id = "calledParamList";
					if (that.astd.parameters.length === 0) { // pas de call
						calledParamsList.textContent = "There is no parameters";
					}

					that.astd.parameters.forEach((parameter, index) => {
						var argumValue = that.argument[index] ? that.argument[index] : "",
							errorContent = that.validateCalledParam(argumValue, index, parameter.type),
							elt = document.createElement("li"),
							paramBlock = CompBuilder.generateTextBlock("textarea", parameter.label + ":" + parameter.type, errorContent, argumValue, "Value");

						paramBlock[TEXT_INPUT].onblur = function() {
							that.argument[index] = event.target.value;
							paramBlock[ERROR_LABEL].textContent = that.validateCalledParam(that.argument[index], index, parameter.type);
							that.refreshCalledASTD();
						};

						paramBlock.forEach((comp) => {
							comp.classList.replace("panel-default", "panel-list");
							elt.appendChild(comp);
						});
						calledParamsList.appendChild(elt);
					});
					container.appendChild(labelParam);
					container.appendChild(calledParamsList);
				}
				container.appendChild(hr);
				return container;
			},
			createPlayMode: function() {
				var container = document.createElement("div"),
					calledAstdLabel = document.createElement("div"),
					startedLabel = document.createElement("div");

				if (window.project.lastJsonObj !== undefined && window.project.lastJsonObj !== null) {
					var parentLabel = that.parent.tabs.length > 1 ? that.label : that.parent.label,
					 callObj = window.project.getJsonObjByType(window.project.lastJsonObj.top_level_astd, CALL, parentLabel);

					if (callObj !== null && callObj.called_astd.name) {
						calledAstdLabel.textContent = "Called ASTD: " + callObj.called_astd.name;
					} else {
						calledAstdLabel.textContent = "Called ASTD: No called ASTD yet.";
					}

					if (callObj !== null && callObj.started !== null) {
						startedLabel.textContent = "Started: " + callObj.started;
					} else {
						startedLabel.textContent = "Started: false";
					}
				} else {
					calledAstdLabel.textContent = "Called ASTD: No called ASTD yet.";
					startedLabel.textContent = "Started: false";
				}

				[calledAstdLabel, startedLabel].forEach((component) => {
					container.appendChild(component);
				});
				return container;

			}
		};
	}

	// Only type that does not display all the default components of astd. That is why it doesn't use the default panel in tab.js
	toPanel() {
		return function(call) {
			var container = document.createElement("div");

			if (call.parent.tabs[0] !== call) {
				container.appendChild(call.tabPanel.createHeader());
			}
			[call.tabPanel.createInheritedVariables, call.callPanel.createCall].forEach((componentFunc) => {
				container.appendChild(componentFunc());
			});

			if (window.project.isInPlayMode) {
				[call.tabPanel.createRunMode, call.callPanel.createPlayMode].forEach((componentFunc) => {
					container.appendChild(componentFunc());
				});
			}

			return container;
		}(this);
	}

	save() {
		var call = super.save();
		call.class = Item.TYPES.CALL;
		call.target = this.target;
		call.argument = this.argument;
		return call;
	}

	load(call) {
		super.load(call);
		this.argument = call.argument === undefined ? [] : call.argument;
		this.target = call.target;
		this.getCallAstd();
		this.updateContentDisplay();
		this.forceLoadTabError();
		this.verify_calledAstdNull();
		this.verify_testParameters();
	}

	export(test_parameter, parameter_name) {
		let serialize = {
			called_astd_name: this.astd.label,
			call_arguments: []
		};
		if (this.astd.externalTest) {
			test_parameter.forEach((element) => {
				return serialize.called_astd_name += "_" + element;
			});
		}
		this.astd.parameters.forEach((parameter, index) => {
			if (this.argument[index]) {
				if (!isNaN(this.argument[index]) || this.argument[index][0] === "\"" && this.argument[index][this.argument[index].length - 1] === "\""
					|| this.argument[index] === "true" || this.argument[index] === "false" || this.argument[index][0] === "{"
					&& this.argument[index][this.argument[index].length - 1] === "}" ) {
					serialize.call_arguments.push({
						name: parameter.label,
						value: {
							type: "Constant",
							content: JSON.parse(this.argument[index])
						}
					});
				} else {
					serialize.call_arguments.push({
						name: parameter.label,
						value: {
							type: "Expression",
							content: this.argument[index]
						}
					});
				}
			}
		});
		if (this.astd.externalTest) {
			window.project.listExportOfParametrizeCallSubASTDs.push(this.astd.export(test_parameter, parameter_name));
		}

		return serialize;
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedCall(copiedParent, tab.astd, tab.argument, tab.target);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

// eslint-disable-next-line no-undef
class CopiedCall extends CopiedTab {
	constructor(parent = null, astd = null, argument = [], target = "null") {
		super(parent);
		this.astd = astd;
		this.target = target;
		this.argument = argument;
	}

	paste(parent) {
		return new Call(window.id.get(), parent, this.astd, this.argument);
	}
}