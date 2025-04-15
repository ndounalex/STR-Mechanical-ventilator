class ASTD extends Item {

	constructor(id = window.id.get(), parent = null, x = 0, y = 0, width = 250, height = 250, label = "Unnamed", initial = false, parameters = [], tabs = [], items = [], state = "non-final", called = []) {
		super(id, parent, x, y, width, height, label, initial);

		this.parameters = parameters;
		this.test_parameters = [];
		this.external_test_parameters = [];
		this.tabs = tabs;
		this.items = items;
		this.state = state;
		this.indice_state = 0;
		this.label = label;
		this.called = called;

		this.orderNumber = "";
		this.orderNumberError = "";
	}

	createItem() {
		var e_item = super.createItem();

		e_item.className += " astd";

		var initial = document.createElement("div");
		initial.className = "initial";

		e_item.appendChild(initial);

		var ul = document.createElement("ul");

		ul.setAttribute("id", "sort");

		var container = document.createElement("div");
		container.className = "content";
		container.appendChild(ul);

		e_item.appendChild(container);

		return e_item;
	}

	sortableTabs() {
		var that = this;
		$(this.e_item.getElementsByTagName("ul")).sortable({

			items: "li:not(.add)",
			start: function(event, ui) {
				ui.item.startPos = ui.item.index();

			},
			stop: function(event, ui) {
				var tab = that.tabs.find((x) => {
					return x.id === ui.item.attr("id");
				});
				if (tab instanceof Automaton || tab instanceof Call || tab instanceof Choice || tab instanceof Flow || tab instanceof Interleave || tab instanceof ParallelComposition || tab instanceof Sequence || tab instanceof Synchronization || tab instanceof Interrupt || tab instanceof Timeout || tab instanceof PersistentTimeout || tab instanceof TimedInterrupt) {
					$(this).sortable("cancel");
				} else if (that.tabs[ui.item.index()] instanceof Automaton || that.tabs[ui.item.index()] instanceof Call || that.tabs[ui.item.index()] instanceof Choice || tab instanceof Flow || that.tabs[ui.item.index()] instanceof Interleave || that.tabs[ui.item.index()] instanceof ParallelComposition || that.tabs[ui.item.index()] instanceof Sequence || that.tabs[ui.item.index()] instanceof Synchronization || that.tabs[ui.item.index()] instanceof Interrupt || that.tabs[ui.item.index()] instanceof Timeout || that.tabs[ui.item.index()] instanceof PersistentTimeout || that.tabs[ui.item.index()] instanceof TimedInterrupt) {
					$(this).sortable("cancel");
				} else {
					that.removeTab(tab.id);
					that.tabs.splice(ui.item.index(), 0, tab);
					that.refreshTabs();
				}
			}
		});
	}

	dblClickable() {

		var that = this,
			item = null;

		$(this.e_item).dblclick(function() {
			if (window.toolbar.selected() !== "Transition" && window.toolbar.selected() !== "Cursor" && !window.project.isInPlayMode) {
				if (window.toolbar.selected() === "State") {
					item = new State(window.id.get(), that.getFile().astd, event.pageX - $(this).offset().left - 15, event.pageY - 15 - $(this).offset().top);

				} else if (window.toolbar.selected() === "ASTD") {
					item = new ASTD(window.id.get(), that.getFile().astd, event.pageX - $(this).offset().left - 25, event.pageY - 37 - $(this).offset().top);
					item.sortableTabs();
				}
				item.draggable();
				item.resizable();
				item.clickable();
				item.buildContextMenu();

				var X = item.position.x,
					Y = item.position.y,
					newParent = that.lastChild(that.getFile().astd, X, Y, item.width, item.height, item),
					oldParent = item.parent;

				oldParent.removeItem(item);
				item.parent = newParent;
				var p = newParent;
				while (p !== that.getFile().astd) {
					item.position = {
						x: item.position.x - p.position.x,
						y: item.position.y - p.position.y
					};

					p = p.parent;
				}
				item.fixOverlap();
				item.label += newParent.indice_state;
				newParent.indice_state++;
				window.panel.selectItem(item);
			}
		});
	}

	get label() {
		return super.label;
	}

	set label(label) {
		super.label = label;
		this.refreshASTDLabel();
	}

	refreshASTDLabel() {
		if (this.parent === this.getFile()) {
			this.getFile().refreshLabel();
		}
	}

	get parameters() {
		return this.m_parameters;
	}

	set parameters(parameters) {
		this.m_parameters = parameters;
	}

	get tabs() {
		return this.m_tabs;
	}

	set tabs(tabs) {
		this.refreshTabs(tabs);
		this.m_tabs = tabs;
	}

	get test_parameters() {
		var par = this.parent,
			testParameters = [];
		if (this.parent.getFile().astd.externalTest) {
			testParameters.push("ex");
		} else if (this.parent.getFile().astd.external_test_parameters) {
			this.parent.getFile().astd.external_test_parameters.forEach((element) => {
				return testParameters.push(element);
			});
		}
		while (par !== this.parent.getFile() && par !== undefined && par !== null) {
			if (par.tabs[par.tabs.length - 1] && par.tabs[par.tabs.length - 1].test) {
				testParameters.push(par.tabs[par.tabs.length - 1].parameter_name);
			}
			par = par.parent;
		}
		return testParameters;
	}

	set test_parameters(test_parameters) {
		super.test_parameters = test_parameters;
	}

	refreshTabs(tabs = this.tabs) {
		var ul = $("#" + this.id).find("> div > ul"),
			label = document.createElement("label"),
			select = document.createElement("select");

		ul.empty();
		label.id = "addTab";
		select.id = "selectTab";

		[Item.TYPES.EMPTY, Item.TYPES.AUTOMATON, Item.TYPES.SEQUENCE, Item.TYPES.FLOW, Item.TYPES.CHOICE, Item.TYPES.Q_CHOICE, Item.TYPES.CLOSURE, Item.TYPES.GUARD, Item.TYPES.PERSISTENT_GUARD, Item.TYPES.SYNCHRONIZATION, Item.TYPES.Q_SYNCHRONIZATION, Item.TYPES.INTERLEAVE, Item.TYPES.Q_INTERLEAVE, Item.TYPES.PARALLEL_COMPOSITION, Item.TYPES.Q_PARALLEL_COMPOSITION, Item.TYPES.CALL, Item.TYPES.INTERRUPT, Item.TYPES.TIMEOUT, Item.TYPES.PERSISTENT_TIMEOUT, Item.TYPES.TIMED_INTERRUPT, Item.TYPES.DELAY, Item.TYPES.PERSISTENT_DELAY, Item.TYPES.Q_FLOW].forEach(function(option) {
			var element = document.createElement("option");
			element.value = option;
			element.innerHTML = option;

			select.appendChild(element);
		});

		label.appendChild(select);

		var liAdd = document.createElement("li");
		liAdd.className = "add";
		liAdd.id = "add";

		var width = 0;

		for (var i = 0; i < tabs.length; i++) {
			var li = tabs[i].e_tab;

			li.innerHTML = "";
			li.style.paddingLeft = "10px";
			li.style.paddingRight = "10px";
			li.style.borderTop = "none";

			if (tabs[i] instanceof Call) {
				if (tabs[i].label) {
					li.innerHTML += tabs[i].label + " , " + tabs[i].toHtml();
				} else {
					li.innerHTML += tabs[i].toHtml();
				}
			} else {
				if (i === 0) {
					li.innerHTML += this.label;
				} else {
					li.innerHTML += tabs[i].label;
				}

				if (tabs[i].parent.test_parameters) {
					tabs[i].parent.test_parameters.forEach((element) => {
						return li.innerHTML += "_$" + element;
					});
				}
				if (i === 0 && this.label) {
					li.innerHTML += " , ";
				} else if (i !== 0 && tabs[i].label) {
					li.innerHTML += " , ";
				}
				li.innerHTML += tabs[i].toHtml();
				if (tabs[i].test) {
					li.innerHTML += " $" + tabs[i].parameter_name + " : " + tabs[i].parameter_domain;
				}
			}

			ul.append(li.outerHTML);
			width += tabs[i].id.length * 11; // valeur exact?
		}

		// add the plus tab only if there is no tabs or the type authorize it
		if (this.tabs && this.tabs.length !== 0) {
			var lastTab = this.tabs[this.tabs.length - 1];
			if (lastTab instanceof QTab || lastTab instanceof Closure || lastTab instanceof Guard) {
				liAdd.append(label);
				ul.append(liAdd);
			}
		} else {
			liAdd.append(label);
			ul.append(liAdd);
		}

		// Calculer un width approprie pour ses enfants (le plus a droite, le plus bas)
		if (this.width !== width && this.width < width) {
			this.width = width < 250 ? 250 : width;
		}

		if (this.getFile().astd && this.getFile().astd.id === this.id && tabs.length > 0 && this.parent.id === this.getFile().id) {
			this.getFile().e_file.innerHTML = this.label;
		}
	}

	get items() {
		return this.m_items;
	}

	set items(items) {
		this.m_items = items;
	}

	get state() {
		return this.m_state;
	}

	set state(state) {
		var astdComponentHandle = $("#" + this.id);
		astdComponentHandle.removeClass(this.m_state);

		this.m_state = state;

		astdComponentHandle.addClass(this.m_state);
	}

	getItem(id) {
		for (var i = 0; i < this.items.length; i++) {
			if (this.items[i].id === id) {
				return this.items[i];
			}

			if (this.items[i] instanceof ASTD) {
				var item = this.items[i].getItem(id);

				if (item) {
					return item;
				}
			}
		}
	}

	isAutomaton() {
		var isAutomaton = false;

		for (var i = 0; i < this.tabs.length; i++) {
			if (this.tabs[i] instanceof Automaton) {
				isAutomaton = true;
				break;
			}
		}

		return isAutomaton;
	}

	removeTab(id) {
		for (var i = 0; i < this.tabs.length; i++) {
			if (this.tabs[i].id === id) {
				this.tabs.splice(i, 1);
				break;
			}
		}
	}

	addItem(item) {
		if (item) {
			this.e_item.getElementsByClassName("content")[0].appendChild(item.e_item);
		}
		this.items.push(item);
	}

	removeItem(item) {
		for (var i = 0; i < this.items.length; i++) {
			if (this.items[i].id === item.id) {
				this.items.splice(i, 1);
				break;
			}
		}
	}

	deleteItem(item) {
		this.removeItem(item);
		item.delete();
	}

	delete() {
		var items = this.items.slice(0);
		for (var i = 0; i < items.length; i++) {
			items[i].delete();
		}
		// Force deletion of each tab in the astd
		this.tabs.forEach((tab) => {
			tab.delete(true);
		});

		if (this.parent.id === this.getFile().id) {
			$("#" + this.parent.id).remove();
			$("li[m_id='" + this.parent.id + "']").remove();
		}

		super.delete();
	}

	get astdPanel() {
		var that = this;

		return {
			createStateStatus: function() {
				var container = document.createElement("div"),
					checkInitial = CompBuilder.generateCheckboxBlock("Initial", that.initial),
					select = CompBuilder.generateSelectBlock(["non-final", "shallow-final", "deep-final"], that.state),
					hr = document.createElement("hr");

				checkInitial.children[CHECKBOX].onclick = function() {
					that.initial = event.target.checked;
				};

				select.onchange = function() {
					that.state = select.value;
				};

				[checkInitial, select, hr].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			},

			createParameters: function() {
				var container = document.createElement("div"),
					components = CompBuilder.generateVarsListSection("Parameters", "paramList", that.parameters, "There is no parameters");
				// attach add button action
				components[VARS_ADD_BTN].onclick = function() {
					var newParam = new Parameter(that);
					// remove the empty text if it is the first parameter to add
					if (that.parameters.length === 0) {
						$(components[VARS_LIST]).empty();
					}
					that.parameters.push(newParam);
					components[VARS_LIST].appendChild(newParam.toPanel());
					// Trigger a change on new element child textarea to resize it properly
					$(components[VARS_LIST].lastElementChild).find("textarea").change();
					that.refreshTabs();
				};

				components.forEach((component) => {
					container.appendChild(component);
				});
				return container;
			},

			createExternalTestParameters: function() {
				var container = document.createElement("div"),
					checkbox = CompBuilder.generateCheckboxBlock("External Test Parameter", that.externalTest),
					hr = document.createElement("hr");
				// attach click event on the checkbox input
				checkbox.children[CHECKBOX].onclick = function(event) {
					that.externalTest = event.target.checked;
					that.refreshTabs();
				};
				container.appendChild(checkbox);
				container.appendChild(hr);

				return container;
			},

			createOrderNumber: function() {
				var container = document.createElement("div"),
					comBlock_orderNumber = CompBuilder.generateTextBlock("input", "Order Number:", that.validateOrderNumber(), that.orderNumber, "Ex: 1");

				comBlock_orderNumber[TEXT_INPUT].onblur = function() {
					that.orderNumber = comBlock_orderNumber[TEXT_INPUT].value;
					CompBuilder.setInputValidity(comBlock_orderNumber[TEXT_INPUT], comBlock_orderNumber[ERROR_LABEL], that.validateOrderNumber());
					that.changeItemOrder();
				};

				comBlock_orderNumber[TEXT_INPUT].onkeyup = function(key) {
					if (key.code === "Enter") {
						that.orderNumber = comBlock_orderNumber[TEXT_INPUT].value;
						CompBuilder.setInputValidity(comBlock_orderNumber[TEXT_INPUT], comBlock_orderNumber[ERROR_LABEL], that.validateOrderNumber());
						that.changeItemOrder();
					}
				};

				comBlock_orderNumber.forEach((component) => {
					container.appendChild(component);
				});

				return container;
			}

		};
	}

	toPanel() {
		let that = this;
		return (function(astdModel) {
			var container = document.createElement("div"),
				isNotState = true;

			// If parent is an automaton, create state header, state options, stateCode
			if (astdModel.parent.tabs && astdModel.parent.tabs[astdModel.parent.tabs.length - 1] instanceof Automaton) {
				isNotState = false;
				[astdModel.itemPanel.createHeader("State"), astdModel.astdPanel.createStateStatus(), astdModel.itemPanel.createStateCode()].forEach((component) => {
					container.appendChild(component);
				});
			}
			// Always create astd header
			container.appendChild(astdModel.itemPanel.createHeader("ASTD", isNotState));
			// If topAstd
			if (astdModel.parent.id === astdModel.parent.getFile().id) {
				container.appendChild(astdModel.astdPanel.createParameters());
				container.appendChild(astdModel.astdPanel.createExternalTestParameters());
			}
			container.appendChild(astdModel.itemPanel.createCom());
			container.appendChild(astdModel.itemPanel.createInvariant());
			// display first tab
			if (astdModel.tabs.length > 0) {
				container.appendChild(astdModel.tabs[0].toPanel());
			}
			// If not topAstd
			if ((astdModel.parent.id !== astdModel.parent.getFile().id)) {
				let hasParentNary = that.parent.tabs.some((element) => (element instanceof Flow) || (element instanceof Choice) || (element instanceof Synchronization) || (element instanceof Interleave) || (element instanceof ParallelComposition));
				if (hasParentNary) {
					container.appendChild(astdModel.astdPanel.createOrderNumber());
				}
			}

			return container;

		})(this);
	}

	save() {
		var astd = super.save();

		astd.class = "astd";
		astd.tabs = [];
		astd.state = this.state;
		astd.orderNumber = this.orderNumber;
		astd.items = [];
		astd.parameters = [];
		astd.initial = this.initial;
		astd.test_parameters = this.test_parameters;
		astd.externalTest = this.externalTest;

		this.tabs.forEach(function(tab) {
			astd.tabs.push(tab.save());
		});

		this.items.forEach(function(item) {
			if (!(item instanceof FlexPoint)) astd.items.push(item.save());
		});

		this.parameters.forEach(function(parameter) {
			astd.parameters.push(parameter.save());
		});

		if (!(this.parent.tabs && this.parent.tabs[this.parent.tabs.length - 1] instanceof Automaton)) {
			astd.entryCode = astd.stayCode = astd.exitCode = "";
		}
		return astd;
	}

	load(astd) {
		// Recuperation des attributs de la classe parente (Item)
		super.load(astd);

		// Recuperation des attributs de la classe ASTD
		if (astd.orderNumber === undefined) {
			this.orderNumber = "";
		} else {
			this.orderNumber = astd.orderNumber;
		}

		var i;
		for (i = 0; i < astd.parameters.length; i++) {
			var parameter = new Parameter(this);
			parameter.load(astd.parameters[i]);
			this.parameters.push(parameter);
		}

		for (i = 0; i < astd.items.length; i++) {
			var item;
			if (astd.items[i].class === "astd") {
				item = new ASTD(astd.items[i].id, this);
			} else if (astd.items[i].class === "state") {
				item = new State(astd.items[i].id, this);
			}

			if (item instanceof ASTD) {
				item.sortableTabs();
			}
			item.clickable();
			item.resizable();
			item.draggable();
			item.buildContextMenu();
			item.load(astd.items[i]);
		}

		astd.tabs.forEach((tab) => {
			let subAstdInitialized = this.createTab(tab.class);
			if (subAstdInitialized) {
				subAstdInitialized.rightClickable();
				subAstdInitialized.load(tab);
			}
			this.tabs.push(subAstdInitialized);
			this.refreshTabs();
		});

		this.state = astd.state;
		this.test_parameters = astd.test_parameters;
		this.externalTest = astd.externalTest;
		this.refreshTabs();
		// On remet
		this.changeItemOrder();
	}

	notAutomatonAndContainsStates() {
		var error = false;

		for (var i = 0; i < this.items.length; i++) {
			if (this.items[i] instanceof State && !this.isAutomaton()) {
				error = true;
			}
		}

		if (error) {
			window.konsole.log_error(this, this, "contains states but is not an Automaton.");
		}
	}

	linksInCorrectASTD() {
		var astdLinks = this.getFile().getDirectLinksOf(this),
			instance = this.tabs[this.tabs.length - 1];
		if (!this.isAutomaton() && !(instance instanceof Sequence || instance instanceof Interrupt || instance instanceof TimedInterrupt || instance instanceof Timeout || instance instanceof PersistentTimeout) && astdLinks.length > 0) window.konsole.log_error(this, this, "contains links but is not an Automaton or a Sequence.");
		if (instance instanceof Sequence && astdLinks.length == 0) window.konsole.log_error(this, this, "is a Sequence but contains no links.");
		if (instance instanceof Interrupt && astdLinks.length !== 1) window.konsole.log_error(this, this, "is an Interrupt but contains no links.");
		if (instance instanceof TimedInterrupt && astdLinks.length != 1) window.konsole.log_error(this, this, "is a Timed Interrupt but contains no links.");
		if (instance instanceof Timeout && astdLinks.length !== 1) window.konsole.log_error(this, this, "is a Timeout but contains no links.");
		if (instance instanceof PersistentTimeout && astdLinks.length !== 1) window.konsole.log_error(this, this, "is a Persistent Timeout but contains no links.");

	}

	automatonInitial() {
		if (this.isAutomaton()) {
			var initial = false;

			for (var i = 0; i < this.items.length; i++) {
				if (this.items[i].initial) {
					initial = true;
				}
			}

			if (!initial) {
				window.konsole.log_error(this, this, "does not have an initial state.");
			}
		}
	}

	automatonFinal() {
		if (this.isAutomaton()) {
			var final = false;

			for (var i = 0; i < this.items.length; i++) {
				if (this.items[i].final || this.items[i].m_state === "shallow-final" || this.items[i].m_state === "deep-final") {
					final = true;
				}
			}

			if (!final) {
				window.konsole.log_warning(this, this, "does not have a final state.");
			}
		}
	}

	checkAllLabels() {

		if (this.label_text_error.length !== 0) this.log_input_error(this.label_text_error, "Label");

		if (this.label === "?" || this.label === "") {
			//window.konsole.log_warning(this, this, "does not have a label.");
			this.label = "Unnamed" + window.project.filesID;
		}

		this.items.forEach((item) => {
			if (item instanceof State) {
				if (item.label === "?" || item.label === "") {
					//window.konsole.log_warning(this, item, "does not have a label.");
					this.label = "Unnamed" + window.project.filesID;
					window.project.filesID++;
				}

				if (item.label_text_error.length !== 0) this.log_input_error(item.label_text_error, "Label", item);
			}
		});

		this.tabs.forEach((tab) => {
			if (tab.label_text_error.length !== 0) this.log_input_error(tab.label_text_error, "Label", tab);
			window.project.filesID++;
		});
	}

	unary(idxTab) {
		// Check if a right tab is define as operand
		if (idxTab === this.tabs.length - 1) {

			// If non check if there is a child astd in board as operand
			if (this.items.length !== 1) {
				window.konsole.log_error(this, this, this.tabs[idxTab].getType() + " is a unary operand must have exactly 1 child ASTD");
			}
		}
	}

	binary(idxTab) {
		if (!this.tabs[idxTab].test && this.items.length !== 2) {
			window.konsole.log_error(this, this, this.tabs[idxTab].getType() + " is a binary operand have must have exactly 2 children ASTDs");

		}
	}

	nary(idxTab) {
		if (!this.tabs[idxTab].test && this.items.length < 2) {
			window.konsole.log_error(this, this, this.tabs[idxTab].getType() + " is a nary operand have must have at least 2 children ASTDs");

		}
	}

	binaryTest(idxTab) {
		if (this.tabs[idxTab].test && this.items.length !== 1) {
			window.konsole.log_error(this, this, this.tabs[idxTab].getType() + " is a test binary operand  must have exactly 1 child ASTD");
		}
	}

	parameterBinaryTest(idxTab) {
		if (this.tabs[idxTab].test) {
			if (this.tabs[idxTab].name_parameter_error) {
				window.konsole.log_error(this, this, " is a test binary operand. it's name test parameter is a " + this.tabs[idxTab].name_parameter_error);
			}
			if (this.tabs[idxTab].domain_parameter_error) {
				window.konsole.log_error(this, this, " is a test binary operand. it's domain test parameter is a " + this.tabs[idxTab].domain_parameter_error);
			}
		}
	}

	synchronizationTest(idxTab) {
		if (this.tabs[idxTab].test) {
			var syncSet = this.parent.getFile().getAllLinksFrom(this.items[0]);
			syncSet.forEach((ev) => {
				if (!this.tabs[idxTab].syncEvents.includes(ev.label)) {
					this.qsynchronization(idxTab);
				} else {
					if (this.tabs[idxTab].parameter_name) {
						if (ev.test_parameters.includes(this.tabs[idxTab].parameter_name)) {
							window.konsole.log_warning(this, this, "Event (" + ev.label + ") has as test parameter the parameter of the synchronization ASTD. Possible deadlock.");
						}
					}
				}
			});
		}

	}

	synchronizationSetInTab(syncSet, ev) {
		for (let link = 0; link < syncSet.length; link++) {
			if (syncSet[link].label === ev.trim()) {
				return true;
			}
		}
		return false;
	}

	qsynchronization(idxTab) {
		var syncSet = this.parent.getFile().getAllLinksFrom(this.items[0]);
		this.tabs[idxTab].syncEvents.forEach((ev) => {
			if (!this.synchronizationSetInTab(syncSet, ev)) {
				window.konsole.log_warning(this, this, "Event (" + ev + ") is missing in child ASTD.");
			}
		});
	}

	synchronization(idxTab) {
		let index = idxTab,
			that = this,
			syncs = this.items.map((item) => {
				return this.parent.getFile().getAllLinksFrom(item);
			});
		this.tabs[index].synchronizationSet = this.tabs[index].syncEvents;

		if (!this.tabs[idxTab].test) {
			this.tabs[idxTab].syncEvents.forEach((ev) => {
				let finds = syncs.map((sync) => {
					return this.synchronizationSetInTab(sync, ev);
				});

				// Found in all
				if (finds.every((elem) => elem === true)) {
					return;
				}

				// Couldnt find in any
				if (finds.every((elem) => {
					return elem === false;
				})) {
					// that.tabs[index].synchronizationSet = that.tabs[index].synchronizationSet.filter((elem) => {
					// 	return elem !== ev;
					// });
					window.konsole.log_warning(this, this, "Event (" + ev + ") is missing from all child ASTD.");
				}
				// Found at least one
				if (finds.some((elem) => {
					return elem === false;
				})) {
					// that.tabs[index].synchronizationSet = that.tabs[index].synchronizationSet.filter((elem) => elem !== ev);
					window.konsole.log_warning(that, that, "Event (" + ev + ") is missing from " + that.items.filter((elem, i) => finds[i] === false).map((item) => {
						return item.m_label;
					}).toString() + " child ASTD.");
				}
			});
		}
	}

	checkTabs() {
		if (this.tabs.length === 0) {
			window.konsole.log_error(this, this, "does not have any tabs.");
		}
	}

	call() {
		var tab = this.tabs[this.tabs.length - 1];
		tab.tabLabelError = [];
		tab.calledAstdNull = "";
		tab.callError = "";
		tab.forceLoadTabError();
		tab.verify_calledAstdNull();
		tab.verify_testParameters();
		tab.tabLabelError.forEach((param_label_error) => {
			if (param_label_error !== "") {
				this.log_input_error(param_label_error, " Call parameters ");
			}
		});

		if (tab.callError) {
			window.konsole.log_error(this, this, tab.callError);
		}
		if (tab.calledAstdNull) {
			window.konsole.log_error(this, this, tab.calledAstdNull);
		}
	}

	check_executable_code() {
		if (this.tabs) {
			for (var i = 0; i < this.tabs.length; i++) {
				if (this.tabs[i].code) {
					if (this.tabs[i].action_text_error.length !== 0) {
						this.log_input_error(this.tabs[i].action_text_error, "ASTD action");
					}
				}
				if (this.tabs[i].interruptCode) {
					if (this.tabs[i].action_text_error.length !== 0) {
						this.log_input_error(this.tabs[i].action_text_error, "Interrupt action");
					}
				}
			}
		}
	}

	checkQVar() {
		if (this.tabs) {
			this.tabs.forEach((tab, index) => {
				if (tab instanceof QTab) {
					this.checkVarErrors(index, tab.variable, "Quantified variable");
				}
			});
		}
	}

	/**
	 * check the given variable (attribute or quantified variable)
	 * @param tabIndex: the index of the current tab astd
	 * @param varToCheck: the element variable that needs verification
	 * @param field: the field associated to the variable (Attributes or Quantified Variable)
	 */
	checkVarErrors(tabIndex, varToCheck, field) {
		const item = tabIndex === 0 ? this : this.tabs[tabIndex];
		if (varToCheck.label_text_error.length !== 0) this.log_input_error(varToCheck.label_text_error, field, item);

		if (varToCheck.field_text_error.length !== 0) this.log_input_error(varToCheck.field_text_error, field, item);
	}

	checkAttributes() {
		if (this.tabs) {
			this.tabs.forEach((tab, index) => {
				tab.attributes.forEach((attribute) => {
					this.checkVarErrors(index, attribute, "Attributes");
				});
			});
		}
	}

	checkParameters() {
		this.parameters.forEach((param) => {
			if (param.label_text_error.length !== 0) {
				this.log_input_error(param.label_text_error, "Parameters");
			}
			if (param.field_text_error.length !== 0) {
				this.log_input_error(param.field_text_error, "Parameters");
			}
		});
	}

	checkDuplicateName() {
		var i;
		for (i = 0; i < this.items.length; i++) {
			var j;
			for (j = 0; j < this.items.length; j++) {
				if (this.items[i].label === this.items[j].label && i !== j && !(this.items[i] instanceof FlexPoint) && !(this.items[j] instanceof FlexPoint)) {
					window.konsole.log_error(this, this, "'s states have duplicate labels");
				}
			}
		}
	}

	checkInitialState() {
		let initalState = this.items.filter((item) => {
			return item.initial;
		});
		if (initalState.length > 1) {
			window.konsole.log_error(this, this, "There is at least 2 initial state in this ASTD");
		}
	}

	/**
	 * Verifie les numeros pour l'ordre des operandes dans un ASTD n'aires
	 */
	checkOrderNumber() {
		let orderNumbers = [],
			duplicateOrderNumbers = false;
		this.items.forEach((item) => {
			orderNumbers.push(item.orderNumber);
		});
		duplicateOrderNumbers = orderNumbers.some((element, index) => {
			return orderNumbers.indexOf(element) !== index || element === undefined || isNaN(element) || element === "";
		});
		if (duplicateOrderNumbers) {
			window.konsole.log_warning(this, this, "There is some undefined, duplicate or wrong order number for a n'ary operation");
		}
	}

	/**
	 * Pour les operation n'aires
	 * Toujours garder la liste d'item d'un ASTD bien classee selon l'ordre dicte par l'utilisateur
	 */
	changeItemOrder() {
		if (this.parent.items) {
			this.parent.items.sort((e1, e2) => {
				return parseFloat(e1.orderNumber) > parseFloat(e2.orderNumber) ? 1 : -1;
			});
		} else {
			this.items.sort((e1, e2) => {
				return parseFloat(e1.orderNumber) > parseFloat(e2.orderNumber) ? 1 : -1;
			});
		}
	}

	/**
	 * validate the tab order number
	 */
	validateOrderNumber() {
		if (isNaN(this.orderNumber)) {
			this.orderNumberError = "N'est pas un nombre";
		} else {
			let duplicate = this.parent.items.filter((on) => (on.orderNumber === this.orderNumber) && on.orderNumber !== "").length;

			if (duplicate > 1) {
				this.orderNumberError = "Duplicata!";
			} else {
				this.orderNumberError = "";
			}

		}
		return this.orderNumberError;
	}

	verify() {
		// check entry/stay/exit code only if the parent is an Automaton, so it is also a state
		if (this.parent.tabs && this.parent.tabs[this.parent.tabs.length - 1] instanceof Automaton) {
			super.verify();
		}
		this.checkTabs();
		this.checkParameters();
		this.notAutomatonAndContainsStates();
		this.linksInCorrectASTD();
		this.automatonInitial();
		this.automatonFinal();
		this.checkAllLabels();
		this.check_executable_code();
		this.checkQVar();
		this.checkAttributes();
		this.checkDuplicateName();

		if (this.tabs[this.tabs.length - 1] instanceof Call) {
			this.call();
		}

		// Validate unary and binary subtab
		for (let i = 0; i < this.tabs.length; i++) {
			const tab = this.tabs[i];
			if (tab instanceof QTab || tab instanceof Closure || tab instanceof Guard || tab instanceof Delay || tab instanceof PersistentDelay || tab instanceof PersistentGuard) {

				this.unary(i);

				if (tab instanceof QSynchronization) {
					this.qsynchronization(i);
				}

			} else if (!(tab instanceof Automaton) && !(tab instanceof Call)) {
				// Pour les operations N'aires
				if (tab instanceof Flow || tab instanceof Choice || tab instanceof Synchronization || tab instanceof Interleave || tab instanceof ParallelComposition) {

					// S'assure qu'on a de bon numero pour l'ordre des operandes.
					this.checkOrderNumber();
					this.changeItemOrder();
					this.nary(i);

					// Pour les tests
					this.binaryTest(i);
					this.parameterBinaryTest(i);

					// Pour la synchronisation
					if (tab instanceof Synchronization) {
						this.synchronization(i);
						this.synchronizationTest(i);
					}

				} else {
					this.binary(i);
					this.binaryTest(i);
					this.parameterBinaryTest(i);

				}

			} else if (tab instanceof Automaton) {
				this.checkInitialState();
			}
		}

		for (let i = 0; i < this.items.length; i++) {
			this.items[i].verify();
		}
	}

	getAllTestParameters() {
		if (this.tabs[this.tabs.length - 1] && this.tabs[this.tabs.length - 1].test) {
			if (this.tabs[this.tabs.length - 1].parameter_name) {
				this.getFile().allTestParameters.push(this.tabs[this.tabs.length - 1].parameter_name);
			}
		}
		for (var i = 0; i < this.items.length; i++) {
			if (!(this.items[i] instanceof State)) {
				this.items[i].getAllTestParameters();
			}
		}
	}

	// Export Top Level ASTD
	export(test_parameter, parameter_name) {
		let topLvlAstd = this.texport(0, test_parameter, null, null, [], null, parameter_name, test_parameter.length !== 0, this.items.length > 2, 0, this.items.length, null), // First Tab
			L = {
				top_level_astds: [{
					name: this.label,
					parameters: this.parameters.map((param) => {
						return param.export();
					}),
					type: topLvlAstd.type,
					invariant: this.invariant,
					typed_astd: topLvlAstd.typed_astd
				}]
			};
		test_parameter.forEach((element) => {
			L.top_level_astds[0].name += "_" + element;
		});
		return L;
	}

	/**
	 * This is the function we call when exporting a tab from the ASTD
	 * It is call by the export from ASTD once for every file and then
	 * from tab implementations
	 *
	 * @param {*} index
	 * @param test_parameter garde les valeurs courantes des test parametres
	 * [valeur des external_test_parameters(s'ils existent), valeur des test_parameters]
	 * @param min permet d'avancer d'un niveau d'arborescence a un autre
	 * @param max la borne superieure du domaine de test de l'ASTD binaire
	 * @param parameter_name garde les noms des parametres de test
	 * @param iD permet de differencier les noms de l'ASTD binaire avec un parametre
	 * de test lors de l'extension
	 * @param external_parameter_name la liste des noms des external_test_parameters
	 * (Un external test parameter ‘ex’ est ajouté à un top level ASTD, s’il serait
	 * appeler par un Call ayant des test_parameter.)
	 * @param test booleen exprimant s'il y a des parametre de test a exporter
	 * @param nary booleen exprimant si l'on exporte une operation naire
	 * @param operationIndex nombre de l'index courant dans l'operation naire
	 * @param operations_maxLength nombre d'operande dans l'operation naire
	 * @param operationID nombre pour differencier les ASTDs lors de l'exportation
	 */
	texport(index, test_parameter, min, max, parameter_name = [], iD = 0, external_parameter_name, test = false, nary = false, operationIndex = 0, operations_maxLength = 0, operationID = null) {
		let childAstd = {};
		if (index === 0) {
			childAstd.name = this.label;
		} else {
			childAstd.name = this.tabs[index].label;
		}
		// Ajout d'artefact dans le nom de l'ASTD pour qu'il soit different (pour les tests)
		if (iD) {
			childAstd.name += "e" + iD;
		}
		// Ajout d'artefact dans le nom de l'ASTD pour qu'il soit different (pour les operations naires)
		if (operationID) {
			childAstd.name += "_o" + operationID;
		}

		test_parameter.forEach((element) => {
			return childAstd.name += "_" + element;
		});

		childAstd.type = this.tabs[index].classNameForExport();

		if (index === 0) {
			childAstd.invariant = this.invariant;
		} else {
			childAstd.invariant = this.tabs[index].invariant;
		}
		const tab = this.tabs[index];
		if (!tab.test) {
			if (tab instanceof Closure || tab instanceof Guard) {
				childAstd.typed_astd = this.tabs[index].export();

			} else if (tab instanceof Automaton || tab instanceof Call || tab instanceof QTab) {
				childAstd.typed_astd = this.tabs[index].export(test_parameter, parameter_name, external_parameter_name);

			} else if (tab instanceof Flow || tab instanceof Choice || tab instanceof Synchronization || tab instanceof Interleave || tab instanceof ParallelComposition) {
				console.log("============== my tab ============ ", {tab, items:tab.parent.items})
				childAstd.typed_astd = this.tabs[index].export(
          null,
          null,
          parameter_name,
          test_parameter,
          null,
          null,
          external_parameter_name,
          test,
          nary,
          operationIndex,
          tab instanceof Synchronization?tab.parent.items.length:operations_maxLength,
          operationID
        );

			} else {
				childAstd.typed_astd = this.tabs[index].export(null, null, parameter_name, test_parameter, null, null, external_parameter_name);
			}
			// Ici, pour les tests
		} else if (min && max) {
			//la suite de l'extension
			childAstd.typed_astd = this.tabs[index].export(min, max, parameter_name, test_parameter, iD, index, external_parameter_name);
		} else {
			//premiere extension passage des bornes du domaine de test
			childAstd.typed_astd = this.tabs[index].export(this.tabs[index].getLowerBoundOfTestDomain(), this.tabs[index].getUpperBoundOfTestDomain(), parameter_name.concat(this.tabs[index].parameter_name), test_parameter, 0, index, external_parameter_name);
		}

		let serialize = childAstd.typed_astd;

		// Unary
		if (this.tabs[index] instanceof Closure || this.tabs[index] instanceof Guard || this.tabs[index] instanceof PersistentGuard || this.tabs[index] instanceof QTab || this.tabs[index] instanceof Delay || this.tabs[index] instanceof PersistentDelay) {

			// If right tab exist
			if (this.tabs[index + 1] !== undefined && this.tabs[index + 1] !== null) {
				if (this.tabs[index + 1].test) {
					serialize.sub_astd = this.texport(index + 1, test_parameter, this.tabs[index + 1].getLowerBoundOfTestDomain(), this.tabs[index + 1].getUpperBoundOfTestDomain(), parameter_name, 0, external_parameter_name, test, nary, operationIndex, operations_maxLength, operationID);

				} else {
					serialize.sub_astd = this.texport(index + 1, test_parameter, null, null, parameter_name, null, external_parameter_name, test, nary, operationIndex, operations_maxLength, operationID);
				}
			} else {
				serialize.sub_astd = this.items[0].texport(0, test_parameter, null, null, parameter_name, null, external_parameter_name, test, nary, operationIndex, operations_maxLength, operationID);
			}
		}
		return childAstd;
	}

	print() {
		var container = document.createElement("div");

		container.appendChild(this.toPanel());
		for (var i = 1; i < this.tabs.length; i++) {
			container.appendChild(this.tabs[i].toPanel());
		}
		// print transitions of astd only if last tab is an automaton
		if (this.tabs[this.tabs.length - 1] instanceof Automaton) {
			this.getFile().getDirectLinksOf(this).forEach((link) => {
				container.appendChild(link.toPanel());
			});
		}
		this.items.forEach((item) => {
			if (!(item instanceof FlexPoint)) container.appendChild(item.print());
		});
		return container;
	}

	/**
	 * For COPY-PASTE functionnality
	 *
	 * */
	copy_paste_action(action, object, event) {
		if (window.copyPaste_label === "Copy") {
			if (this.parent instanceof File) {
				alert("I cannot copy top-level ASTD");
			} else {
				// let links = this.getFile().getDirectLinksOf(this);
				let links = this.getFile().getDirectLinksOf(this);
				// eslint-disable-next-line no-use-before-define
				window.copiedItem = new CopiedASTD(this.id, this.parent, this.position.x, this.position.y, this.width, this.height, this.label, this.initial, this.parameters, this.tabs, this.items, this.state, this.called, links);
				window.copyPaste_label = "Paste";
			}

		} else if (window.copyPaste_label === "Paste") {
			window.copyPaste_label = "Copy";
			window.copiedItem.paste({
				x: event.pageX - $(this.e_item).offset().left,
				y: event.pageY - $(this.e_item).offset().top
			}, this);
		}
	}

	copy(copiedParent, item, links = []) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedASTD(item.id, copiedParent, item.position.x, item.position.y, item.width, item.height, item.label, item.initial, item.parameters, item.tabs, item.items, item.state, item.called, links);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

/**
 * Classe memoire d'un ASTD pour fonctionnalite copier-coller
 */
// eslint-disable-next-line no-undef
class CopiedASTD extends CopiedItem {
	constructor(id, parent = null, x = 0, y = 0, width = 250, height = 250, label = "Unnamed",
		initial = false, parameters = [], tabs = [], items = [], state = "non-final",
		called = [], links = []) {
		super(id, parent, x, y, width, height, label, initial);
		this.parameters = parameters;
		this.state = state;
		this.called = called;
		this.items = [];
		this.tabs = [];
		this.links = [];

		items.forEach((item) => {
			let tmpLinks = item.getFile().getDirectLinksOf(item),
				tmpItem = item.copy(this, item, tmpLinks);
			this.items.push(tmpItem);
		});
		tabs.forEach((tab) => {
			let tmpTab = tab.copy(this, tab);
			this.tabs.push(tmpTab);
		});
		links.forEach((link) => {
			let tmpLink = link.copy(link);
			this.links.push(tmpLink);
		});
	}

	paste(position, parentItem) {

		let item = new ASTD(window.id.get(), parentItem, position.x, position.y, this.width, this.height, this.label, this.initial, this.parameters);

		this.tabs.forEach((copiedTab) => {
			let tmp = copiedTab.paste(item);
			item.tabs.push(tmp);
		});

		let source,
			destination;
		const that = this;

		this.items.forEach((copiedElem) => {
			let newItem = copiedElem.paste({
				x: copiedElem.position.x,
				y: copiedElem.position.y
			}, item);

			that.links.forEach((copiedLink) => {					// Trouver les sources et destination pour creer les bons liens
				if (copiedElem.id === copiedLink.source.id) {
					source = newItem;
				}
				if (copiedElem.id === copiedLink.destination.id) {
					destination = newItem;
				}
				if (source !== undefined && destination !== undefined) {
					let newtmp = copiedLink.paste(source, destination);
					item.parent.getFile().addLink(newtmp);
					source = undefined;
					destination = undefined;
				}
			});
		});

		item.sortableTabs();
		item.draggable();
		item.resizable();
		item.clickable();
		item.buildContextMenu();
		item.fixOverlap();
		window.panel.selectItem(item);
		item.refreshTabs();

	}
}