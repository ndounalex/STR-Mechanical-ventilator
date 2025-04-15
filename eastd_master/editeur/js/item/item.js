class Item {

	constructor(id = window.id.get(), parent = null, x = 0, y = 0, width = 50, height = 50, label = "?", initial = false, entryCode = "", stayCode = "", exitCode = "") {
		this.e_item = this.createItem();
		this.id = id;
		this.parent = parent;
		this.position = {
			x: x,
			y: y
		};
		this.width = width;
		this.height = height;
		this.label = label;
		this.initial = initial;
		this.entryCode = entryCode;
		this.stayCode = stayCode;
		this.exitCode = exitCode;
		this.com = "";
		this.stayCode_text_error = "";
		this.exitCode_text_error = "";
		this.entryCode_text_error = "";
		this.label_text_error = "";
		this.subscriber = [];
		this.test_parameters = [];
		this.invariant = "";
	}

	createTab(tab) {
		if (tab === Item.TYPES.AUTOMATON) {
			return new Automaton(undefined, this);
		} else if (tab === Item.TYPES.SEQUENCE) {
			return new Sequence(undefined, this);
		} else if (tab === Item.TYPES.FLOW) {
			return new Flow(undefined, this);
		} else if (tab === Item.TYPES.CHOICE) {
			return new Choice(undefined, this);
		} else if (tab === Item.TYPES.Q_CHOICE) {
			return new QChoice(undefined, this);
		} else if (tab === Item.TYPES.CALL) {
			return new Call(undefined, this);
		} else if (tab === Item.TYPES.CLOSURE) {
			return new Closure(undefined, this);
		} else if (tab === Item.TYPES.GUARD) {
			return new Guard(undefined, this);
		} else if (tab === Item.TYPES.PERSISTENT_GUARD) {
			return new PersistentGuard(undefined, this);
		} else if (tab === Item.TYPES.SYNCHRONIZATION) {
			return new Synchronization(undefined, this);
		} else if (tab === Item.TYPES.Q_SYNCHRONIZATION) {
			return new QSynchronization(undefined, this);
		} else if (tab === Item.TYPES.PARALLEL_COMPOSITION) {
			return new ParallelComposition(undefined, this);
		} else if (tab === Item.TYPES.Q_PARALLEL_COMPOSITION) {
			return new QParallelComposition(undefined, this);
		} else if (tab === Item.TYPES.INTERLEAVE) {
			return new Interleave(undefined, this);
		} else if (tab === Item.TYPES.Q_INTERLEAVE) {
			return new QInterleave(undefined, this);
		} else if (tab === Item.TYPES.INTERRUPT) {
			return new Interrupt(undefined, this);
		} else if (tab === Item.TYPES.DELAY) {
			return new Delay(undefined, this);
		} else if (tab === Item.TYPES.PERSISTENT_DELAY) {
			return new PersistentDelay(undefined, this);
		} else if (tab === Item.TYPES.TIMEOUT) {
			return new Timeout(undefined, this);
		} else if (tab === Item.TYPES.PERSISTENT_TIMEOUT) {
			return new PersistentTimeout(undefined, this);
		} else if (tab === Item.TYPES.TIMED_INTERRUPT) {
			return new TimedInterrupt(undefined, this);
		} else if (tab === Item.TYPES.Q_FLOW) {
			return new QFlow(undefined, this);
		} else { //Default case
			return new Tab(undefined, this);
		}
	}

	static get TYPES() {
		return {
			EMPTY: "",
			AUTOMATON: "Automaton",
			SEQUENCE: "➜ Sequence",
			CHOICE: "| Choice",
			FLOW: "Ψ Flow",
			Q_FLOW: "Ψ x : T",
			Q_CHOICE: "| x : T",
			CLOSURE: "★ Closure",
			GUARD: "⇒ Guard",
			PERSISTENT_GUARD: "|⇒ Persistent Guard",
			SYNCHRONIZATION: "|[...]| Synchronization",
			Q_SYNCHRONIZATION: "|[...]| x : T",
			INTERLEAVE: "||| Interleave",
			Q_INTERLEAVE: "||| x : T",
			PARALLEL_COMPOSITION: "|| Parallel composition",
			Q_PARALLEL_COMPOSITION: "|| x : T",
			CALL: "Call",
			INTERRUPT: "Interrupt",
			DELAY: "Delay",
			PERSISTENT_DELAY: "Persistent Delay",
			TIMEOUT: "Timeout",
			PERSISTENT_TIMEOUT: "Persistent timeout",
			TIMED_INTERRUPT: "Timed interrupt"

		};
	}

	createItem() {
		var e_item = document.createElement("div");
		e_item.className = "item";
		return e_item;
	}

	attach(component) {
		// Only add if we don't find the component (indexOf returns -1 in that case)
		if (this.subscriber.indexOf(component) === -1)
			this.subscriber.push(component);
	}

	detach(comp) {
		var index = this.subscriber.indexOf(comp);
		if (index !== -1)
			this.subscriber.splice(index, 1);
	}

	lastChild(astd, x, y, w, h, currentItem) {
		for (var i = 0; i < astd.items.length; i++) {
			if (astd.items[i] !== currentItem && astd.items[i] instanceof ASTD && x + w / 2 > astd.items[i].position.x && y + h / 2 > astd.items[i].position.y
				&& x + w / 2 < astd.items[i].position.x + astd.items[i].width
				&& y + h / 2 < astd.items[i].position.y + astd.items[i].height) {
				return this.lastChild(astd.items[i], x - astd.items[i].position.x, y - astd.items[i].position.y, w, h, currentItem);
			}
		}
		return astd;
	}

	draggable() {
		var that = this;

		$(this.e_item).draggable({
			containment: $(that.getFile().astd.e_item),
			drag: function() {
				that.parent.getFile().plumb.repaintEverything();

				that.subscriber.forEach((comp) => {
					comp.notify();
				});
			},
			stop: function() {
				that.position = {
					x: $(this).position().left,
					y: $(this).position().top
				};

				var notifiedLinks = [...that.subscriber];
				// Notify one last time then detach in case the parent has changed
				notifiedLinks.forEach((comp) => {
					comp.notify();
					comp.detachFromComponents();
				});

				var parent = that.parent,
					X = that.position.x,
					Y = that.position.y;
				while (parent !== that.getFile().astd) {
					X += parent.position.x;
					Y += parent.position.y;
					parent = parent.parent;
				}

				var newParent = that.lastChild(that.getFile().astd, X, Y, that.width, that.height, that),
					oldParent = that.parent;
				oldParent.removeItem(that);
				that.parent = newParent;

				if (oldParent.items.includes(newParent)) {
					parent = newParent;
					while (parent !== oldParent) {
						that.position = {
							x: that.position.x - parent.position.x,
							y: that.position.y - parent.position.y
						};
						parent = parent.parent;
					}
				} else if (newParent.items.includes(oldParent)) {
					parent = oldParent;
					while (parent !== newParent) {
						that.position = {
							x: that.position.x + parent.position.x,
							y: that.position.y + parent.position.y
						};
						parent = parent.parent;
					}
				} else {
					parent = oldParent;
					while (parent !== that.getFile().astd) {
						that.position = {
							x: that.position.x + parent.position.x,
							y: that.position.y + parent.position.y
						};
						parent = parent.parent;
					}

					parent = newParent;
					while (parent !== that.getFile().astd) {
						that.position = {
							x: that.position.x - parent.position.x,
							y: that.position.y - parent.position.y
						};
						parent = parent.parent;
					}
				}

				that.fixOverlap();
				// reattach linkViews with new parent and notify
				notifiedLinks.forEach((comp) => {
					comp.attachToComponents();
					comp.notify();
				});

				// Array from make a copy of the iterating array
				Array.from(that.getFile().links).forEach((link) => {
					if (link.source.parent.parent !== link.destination.parent
						&& link.source.parent !== link.destination.parent.parent
						&& link.source.parent !== link.destination.parent) {

						that.getFile().removeLink(link.id);
					}
					link.setArrowType();
				});

				that.parent.getFile().plumb.repaintEverything();
				window.panel.selectItem(that);
			}
		});
	}

	/**
	 * Make sure the current item is not overlapping a border of astd instances on the board and reposition it if so.
	 * Go through all ASTDs of the current parent and compare item position with top, left, bottom and right positions of other ASTDs
	 * and if the current item overlaps one border, we adjust the position to reduce overlaps.
	 * We then check for the borders of the parent to make sure the item is completely inside and if not, we adjust the position.
	 * Overlaps between items with same parent are not always fix because two conditions might conflict with each other.
	 * The positionning inside of the parent is prioritized over other items overlaps.
	 */
	fixOverlap() {
		// Reposition item if it overlaps an ASTD with the same parent
		this.parent.items.filter((item) => {
			return item !== this && item instanceof ASTD;
		}).forEach((item) => {
			const top = item.position.y,
				left = item.position.x,
				bottom = item.position.y + item.height,
				right = item.position.x + item.width;
			if (this.position.y < top && this.position.y + this.height > top && this.position.x + this.width > left && this.position.x < right) {
				this.position.y = top - this.height - 5; // overlap top
			} else if (this.position.y < bottom && this.position.y + this.height > bottom && this.position.x + this.width > left && this.position.x < right) {
				this.position.y = bottom + 5; // overlap bottom
			}
			if (this.position.x < left && this.position.x + this.width > left && this.position.y + this.height > top && this.position.y < bottom) {
				this.position.x = left - this.width - 5; // overlap left
			} else if (this.position.x < right && this.position.x + this.width > right && this.position.y + this.height > top && this.position.y < bottom) {
				this.position.x = right + 5; // overlap right
			}
		});

		//reposition item to avoid overlap of parent borders
		if (this.position.x + this.width > this.parent.width - 5) {
			this.position.x = this.parent.width - this.width - 5; // overlap right
		} else if (this.position.x < 0) {
			this.position.x = 5; // overlap left
		}
		if (this.position.y + this.height > this.parent.height - 5) {
			this.position.y = this.parent.height - this.height - 5; // overlap bottom
		} else if (this.position.y < 0) {
			this.position.y = 5; // overlap top
		}
		this.refreshPosition();
	}

	resizable() {
		var that = this;

		$(this.e_item).resizable({
			containment: "parent",
			start: function() {
				let minSize = {
					width: 20,
					height: 20
				};
				if (that.items && that.items.length > 0) {
					minSize = that.items.reduce((currentMax, item) => {
						return {
							width: Math.max(currentMax.width, item.position.x + item.width),
							height: Math.max(currentMax.height, item.position.y + item.height)
						};
					}, minSize);
				}
				$(that.e_item).resizable("option", "minWidth", minSize.width + 10);
				$(that.e_item).resizable("option", "minHeight", minSize.height + 10);
			},
			resize: function() {
				that.height = $(this).height();
				that.width = $(this).width();
				that.parent.getFile().plumb.repaintEverything();
				that.subscriber.forEach((comp) => {
					comp.notify();
				});
			},
			stop: function() {
				that.position.x = $(this).position().left;
				that.subscriber.forEach((comp) => {
					comp.notify();
				});
			}
		});
	}

	clickable() {
		var that = this;

		$(this.e_item).on("change", "select", function() {
			var t = that.createTab(this.value);
			t.rightClickable();
			that.tabs.push(t);
			that.refreshTabs();

			if (that.tabs[0] === t) {
				window.panel.show(that);
			} else {
				window.panel.show(t);
			}
		});

		$(this.e_item).on("click", "li", function(event) {
			event.stopPropagation();
			var selectedTab = that.tabs.find((x) => {
				return x.id === this.id;
			});

			that.tabs.forEach((tab) => {
				tab.e_tab.style.backgroundColor = tab === selectedTab ? "white" : "rgb(207,211,218)";
			});

			if (this.className !== "add") {
				if (that.tabs[0] === selectedTab)
					window.panel.selectItem(that);
				else
					window.panel.selectItem(selectedTab);
				that.refreshTabs();
			}
		});

		$(this.e_item).on("click", ".content", function(event) {
			event.stopPropagation();
			window.panel.show(that);

			if (that.tabs !== undefined && that.tabs !== null) {
				that.tabs.forEach((tab, index) => {
					tab.e_tab.style.backgroundColor = index === that.tabs.length - 1 ? "white" : "rgb(207,211,218)";
				});
				that.refreshTabs();
			}
			if (window.selector) {
				window.selector(that);
			} else {
				window.panel.selectItem(that);
				if (window.toolbar.selected() === "Transition") {
					window.selector = function(item) {
						var l;
						if (that.parent.getFile().astd.id === item.id) {
							window.selector = null;
							return;
						}
						if (that.parent.parent !== item.parent && that.parent !== item.parent.parent && that.parent !== item.parent) {
							window.selector = null;
							return;
						} else if (that.parent.parent.id === item.parent.id) {
							// @toCheck: without the next 3 lines, everything is visually the same, how does it affects the logic behind?
							l = new Link(window.id.get(), item, that);
							that.parent.getFile().addLink(l);
							item.getFile().removeLink(l.id);
						}
						if (item !== that.getFile().astd && that !== that.getFile().astd) {
							// Check if not flexpoint to avoid creating useless connection
							if (!(that instanceof FlexPoint) && !(item instanceof FlexPoint)) {
								if (!that.parent.getFile().containsLink(that, item)) {
									l = new Link(window.id.get(), that, item);
									window.panel.selectItem(l);
									if (that.parent.tabs) {
										if (that.parent.tabs[that.parent.tabs.length - 1] instanceof Sequence && that instanceof ASTD && item instanceof ASTD
											&& that.parent === item.parent) {
											l.type = SEQUENCE;
										}
										if (that.parent.tabs[that.parent.tabs.length - 1] instanceof Interrupt && that instanceof ASTD && item instanceof ASTD
											&& that.parent === item.parent) {
											l.type = TIMEOUT;
										}
										if (that.parent.tabs[that.parent.tabs.length - 1] instanceof Timeout && that instanceof ASTD && item instanceof ASTD
											&& that.parent === item.parent) {
											l.type = TIMEOUT;
										}
										if (that.parent.tabs[that.parent.tabs.length - 1] instanceof PersistentTimeout && that instanceof ASTD && item instanceof ASTD
											&& that.parent === item.parent) {
											l.type = TIMEOUT;
										}
										if (that.parent.tabs[that.parent.tabs.length - 1] instanceof TimedInterrupt && that instanceof ASTD && item instanceof ASTD
											&& that.parent === item.parent) {
											l.type = TIMEOUT;
										}
									}
									that.parent.getFile().addLink(l);
								}
							}
						}
						window.toolbar.unselect();
						window.selector = null;
					};
				}
				if (window.toolbar.selected() === "Cursor") {
					that.select();
				}
			}
			if (that.tabs) {
				if (that.tabs.length <= 1) {
					window.panel.show(that);
				} else {
					window.panel.show(that.tabs[that.tabs.length - 1]);
				}
			}
		});
	}

	/**
	 * Function building a jquery contextmenu for the items (astd and state).
	 * The selector is the e_item associated to the current item.
	 */
	buildContextMenu() {
		var that = this;
		$.contextMenu({
			selector: `#${this.id}`,
			build: function() {
				if (!window.project.isInPlayMode) {
					return {
						events: {
							show: function() {
								window.panel.selectItem(that);
							}
						},
						items: {
							deleteElem: {
								name: "Delete",
								icon: "far fa-trash-alt",
								callback: function() {
									that.parent.deleteItem(that);
								}
							},
							copyPasteElement: {
								name: window.copyPaste_label,
								icon: "far fa-trash-alt",
								callback: function(action, object, event) {
									that.copy_paste_action(action, object, event);
								}
							}
						}
						,
						zIndex: 100
					};
				}
			}
		});
	}

	get id() {
		return this.m_id;
	}

	set id(id) {
		this.e_item.id = id;

		this.m_id = id;
	}

	get parent() {
		return this.m_parent;
	}

	getInheritedVariables() {
		return this.parent.getFile().getInheritedVariables(this.parent);
	}

	set parent(parent) {
		if (parent) {
			parent.addItem(this);
		}
		this.m_parent = parent;
	}

	get position() {
		return this.m_pos;
	}

	set position(pos) {
		this.m_pos = pos;
		this.refreshPosition();
	}

	get width() {
		return this.m_width;
	}

	set width(width) {
		this.e_item.style.width = width + "px";
		this.getFile().plumb.repaintEverything();

		this.m_width = width;
	}

	get height() {
		return this.m_height;
	}

	set height(height) {
		this.e_item.style.height = height + "px";
		this.getFile().plumb.repaintEverything();

		this.e_item.getElementsByClassName("initial")[0].style.marginTop = height / 2 + "px";

		this.m_height = height;
	}

	get label() {
		return this.m_label;
	}

	set label(label) {
		this.m_label = label;
	}

	get test_parameters() {
		return this.m_test_parameters;
	}

	set test_parameters(test_parameters) {
		this.m_test_parameters = test_parameters;
	}

	get initial() {
		return this.m_initial;
	}

	set initial(initial) {
		if (initial) {
			this.e_item.getElementsByClassName("initial")[0].style.display = "block";
		} else {
			this.e_item.getElementsByClassName("initial")[0].style.display = "none";
		}

		this.m_initial = initial;
	}

	select() {
		this.e_item.classList.add("selected");
	}

	unselect() {
		this.e_item.classList.remove("selected");
	}

	setCurrent() {
		this.e_item.classList.add("current");
	}

	removeCurrent() {
		this.e_item.classList.remove("current");
	}

	getFile() {
		return this.parent.getFile();
	}

	delete() {
		var links = this.getFile().links.slice(0);

		for (var i = 0; i < links.length; i++) {
			if (links[i].source.id === this.id || links[i].destination.id === this.id) {
				this.getFile().removeLink(links[i].id);
			}
		}
		// manually remove contextMenu so it is built again even if we load a file with the same e_item.id value
		$.contextMenu("destroy", `#${this.id}`);
		this.e_item.remove();
		window.panel.show(null);
	}

	refreshPosition() {
		this.e_item.style.left = this.position.x + "px";
		this.e_item.style.top = this.position.y + "px";
		this.getFile().plumb.repaintEverything();
	}

	/**
	 * Validate the value of the specified code entry and updates the given error message attribute
	 * @param codeEntry: value to check against action grammar and action semantic validations
	 * @param textErrorName: name of the property containing the error text
	 * @returns {string} The resulting error text from validations
	 */
	validateCode(codeEntry, textErrorName) {
		if (codeEntry) {
			var verify_result = Validator.syntax_Verify(codeEntry, window.action_grammar_object);
			this[textErrorName] = verify_result.message;

			if (this[textErrorName].length === 0) {
				this[textErrorName] = Validator.checkActionSemantic(verify_result.result, this);
			}
		} else {
			this[textErrorName] = "";
		}
		return this[textErrorName];
	}

	/**
	 * validate the astd or state label against reserved labels for history states
	 * @returns {string} The error message resulting from the validation
	 */
	validateLabel() {
		if (!this.history && (this.label === "H" || this.label === "H*"))
			this.label_text_error = "Label reserved for history states only.";
		else
			this.label_text_error = "";

		return this.label_text_error;
	}

	/**
	 * check all codes associated to a state item (State or astd inside an automaton)
	 */
	validateEntryStayExitCode() {
		["entryCode", "stayCode", "exitCode"].forEach((codeName) => {
			this.validateCode(this[codeName], codeName + "_text_error");
		});
	}

	get itemPanel() {
		var that = this;

		/**
		 * Utility function to execute blur action on enter key pressed. Must be bind to the input associated to the action
		 * @param event: fired event object
		 */
		function executeOnKeyUp(event) {
			if (event.keyCode === 13) {
				this.blur();
			}
		}

		/**
		 * Utility function to generate a code block with label, textArea and error div. Once it gets the components, binds the blur action
		 * @param labelName: name to display on the label of the current code block
		 * @param attribute: property name associated to the current block components
		 * @param entry_error: error text property name associated to the current block
		 * @returns {(HTMLLabelElement|HTMLTextAreaElement|HTMLDivElement)[]}
		 */
		function codeBody(labelName, attribute, entry_error) {
			var codeBlock = CompBuilder.generateTextBlock("textarea", labelName, that.validateCode(that[attribute], entry_error), that[attribute], "Ex: M.f(args) | M::f(args) | {c++ code}");

			codeBlock[TEXT_INPUT].onblur = function() {
				that[attribute] = codeBlock[TEXT_INPUT].value;
				CompBuilder.setInputValidity(codeBlock[TEXT_INPUT], codeBlock[ERROR_LABEL], that.validateCode(that[attribute], entry_error));
			};

			return codeBlock;
		}

		return {
			createHeader: function(label, isFirstHeader = true) {
				var container = document.createElement("div"),
					title = document.createElement("h5"),
					labelInput = document.createElement("input"),
					labelError = CompBuilder.generateFieldError(),
					typeLabel = document.createElement("div"),
					parentLabel = document.createElement("div"),
					hr = document.createElement("hr");

				title.textContent = label;

				labelInput.className = "form-control inline-input itemNameInput";
				labelInput.placeholder = "Label";
				labelInput.value = that.label;
				if (isFirstHeader) {
					labelInput.onblur = function() {
						$(".itemNameInput").val(labelInput.value);
						that.label = labelInput.value;
						CompBuilder.setInputValidity(labelInput, labelError, that.validateLabel());
						if (that instanceof ASTD) {
							that.refreshTabs();
							that.called.forEach((astd) => {
								astd.tabs.forEach((tab) => {
									if (tab instanceof Call) {
										tab.astd = that;
									}
								});
							});
						} else {
							that.parent.refreshTabs();
						}
					};
					CompBuilder.setInputValidity(labelInput, labelError, that.label_text_error);
				}
				labelInput.onkeyup = executeOnKeyUp.bind(labelInput);
				if (that instanceof State && (that.history || that.deephistory) || !isFirstHeader) {
					labelInput.readOnly = true;
				}

				typeLabel.className = "panel-default";
				if (that instanceof ASTD && that.tabs[0] && label === "ASTD") {
					typeLabel.textContent = that.tabs[0].getType();
				}

				parentLabel.className = "panel-default";
				// If the item is not the top-level astd and we display the first header
				if (that.parent !== that.getFile() && isFirstHeader) {
					parentLabel.textContent = "Parent: " + that.parent.label;
					that.parent.tabs.forEach((tab) => {
						parentLabel.textContent += "/" + tab.constructor.name;
					});
				}

				[title, labelInput, labelError, typeLabel, parentLabel, hr].forEach((comp) => {
					container.appendChild(comp);
				});

				return container;
			},

			createStateCode: function() {
				var container = document.createElement("div"),
					labelStateCode = CompBuilder.generateCollapseLabel("State Code", "codeSection"),
					codeDiv = document.createElement("div"),
					entryCode = codeBody("Entry Code", "entryCode", "entryCode_text_error"),
					stayCode = codeBody("Stay Code", "stayCode", "stayCode_text_error"),
					exitCode = codeBody("Exit Code", "exitCode", "exitCode_text_error"),
					hr = document.createElement("hr");

				if (that.parent === that.getFile() || !(that.parent.tabs[that.parent.tabs.length - 1] instanceof Automaton) && !(that instanceof State) || that.history) {
					container.classList.add("d-none");
				}
				labelStateCode.classList.add("no-print");

				codeDiv.className = "collapse ml-3";
				codeDiv.id = "codeSection";
				[...entryCode, ...stayCode, ...exitCode].forEach((component) => {
					codeDiv.appendChild(component);
				});

				[labelStateCode, codeDiv, hr].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			},
			createCom: function() {
				var container = document.createElement("div"),
					comBlock = CompBuilder.generateTextBlock("textarea", "Comment:", "", that.com, "Ex: 'put what you want here'"),
					hr = document.createElement("hr");

				comBlock[TEXT_INPUT].onblur = function() {
					that.com = comBlock[TEXT_INPUT].value;
				};

				[...comBlock, hr].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			},
			createInvariant: function() {
				var container = document.createElement("div"),
					comBlock = CompBuilder.generateTextBlock("textarea", "Invariant:", "", that.invariant, "Ex: Invariant definition'"),
					hr = document.createElement("hr");

				comBlock[TEXT_INPUT].onblur = function() {
					that.invariant = comBlock[TEXT_INPUT].value;
				};

				[...comBlock, hr].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			}
		};
	}

	save() {
		return {
			id: this.id,
			parent: this.parent.id,
			position: this.position,
			width: this.width,
			height: this.height,
			label: this.label,
			initial: this.initial,
			entryCode: this.entryCode,
			stayCode: this.stayCode,
			exitCode: this.exitCode,
			invariant: this.invariant,
			comment: this.com
		};
	}

	load(item) {
		// To be compatible with older saved astds
		if (item.position) {
			this.position = item.position;
		} else {
			this.position = {
				x: item.x,
				y: item.y
			};
		}

		this.width = item.width;
		this.height = item.height;
		this.label = item.label;
		this.initial = item.initial;
		this.entryCode = item.entryCode;
		this.stayCode = item.stayCode;
		this.exitCode = item.exitCode;
		this.invariant = item.invariant;
		this.com = item.comment;
		this.validateLabel();
		this.validateEntryStayExitCode();
	}

	log_input_error(message_to_log, field, item = this) {
		window.konsole.log_error(this, item, message_to_log + " " + field + " error.");
	}

	verify() {
		if (this.entryCode && this.entryCode_text_error.length !== 0)
			this.log_input_error(this.entryCode_text_error, "Entry Code");

		if (this.stayCode && this.stayCode_text_error.length !== 0)
			this.log_input_error(this.stayCode_text_error, "Stay Code");

		if (this.exitCode && this.exitCode_text_error.length !== 0)
			this.log_input_error(this.exitCode_text_error, "Exit Code");
	}

	/**
	 * For COPY-PASTE functionnality
	 *
	 * */
	copy_paste_action(action, object, event) {
		if (window.copyPaste_label === "Copy") {
			// console.log("Item");
			// eslint-disable-next-line no-use-before-define
			let copyItem = new CopiedItem(this.id, this.parent, this.position.x, this.position.y, this.width, this.height, this.label, this.initial, this.entryCode, this.stayCode, this.exitCode);
			window.copiedItem = copyItem;
			window.copyPaste_label = "Paste";

		} else if (window.copyPaste_label === "Paste") {
			// console.log("mouse location:", event.clientX, event.clientY);
			window.copyPaste_label = "Copy";
			window.copiedItem.paste({
				x: event.clientX,
				y: event.clientY
			}, this);
		}
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, item, links = []) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedItem(copiedParent, item.position.x, item.position.y, item.width, item.height, item.label, item.initial, item.entryCode, item.stayCode, item.exitCode);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

class CopiedItem {
	constructor(id, parent = null, x = 0, y = 0, width = 50, height = 50, label = "?", initial = false, entryCode = "", stayCode = "", exitCode = "") {
		this.id = id;
		this.parent = parent;
		this.position = {
			x: x,
			y: y
		};
		this.width = width;
		this.height = height;

		if (this.constructor.name !== "CopiedState") {
			this.label = label + "_copy";
		} else {
			this.label = label;
		}

		this.initial = initial;
		this.entryCode = entryCode;
		this.stayCode = stayCode;
		this.exitCode = exitCode;
	}

	paste(position, parentItem) {
		// eslint-disable-next-line no-unused-vars
		let newItem = new Item(window.id.get(), parentItem, position.x, position.y, this.width, this.height, this.label, this.initial, this.entryCode, this.stayCode, this.exitCode);
	}
}