class Tab {

	constructor(id = window.id.get(), parent = null, attributes = [], code = "", interruptCode = "", label = "") {

		this.e_tab = this.createItem();
		this.id = id;
		this.parent = parent;
		this.attributes = attributes;
		this.code = code;
		this.interruptCode = interruptCode;
		this.label = label;
		this.action_text_error = "";
		this.test = false;
		this.parameter_name = "";
		this.parameter_domain = "";
		this.name_parameter_error = "";
		this.domain_parameter_error = "";
		this.label_text_error = "";

	}

	classNameForExport() {
		return this.constructor.name;
	}

	createItem() {
		var e_tab = document.createElement("li");
		e_tab.className = "tab";
		return e_tab;
	}

	// select/unselect are used for css styling here. We set the selected style on the parent as it contains the graphical elements of tabs
	select() {
		this.parent.select();
	}

	unselect() {
		this.parent.unselect();
	}

	rightClickable() {
		var that = this,
			s = ".tab#" + that.id,

			items1 = {
				"addRight": {
					"name": "Add right",
					"icon": "fas fa-plus",
					"items": {
						"timedAstd": {
							"name": "Timed ASTD",
							"items": {
								"rtimeout": { "name": Item.TYPES.TIMEOUT },
								"rpto": { "name": Item.TYPES.PERSISTENT_TIMEOUT },
								"rti": { "name": Item.TYPES.TIMED_INTERRUPT },
								"rdelay": { "name": Item.TYPES.DELAY },
								"rperdelay": { "name": Item.TYPES.PERSISTENT_DELAY }
							}
						},
						"parallel": {
							"name": "Parallel ASTD",
							"items": {
								"rsynch": { "name": Item.TYPES.SYNCHRONIZATION },
								"rqsynch": { "name": Item.TYPES.Q_SYNCHRONIZATION },
								"rinterleave": { "name": Item.TYPES.INTERLEAVE },
								"rqinterleave": { "name": Item.TYPES.Q_INTERLEAVE },
								"rpcomp": { "name": Item.TYPES.PARALLEL_COMPOSITION },
								"rqpcomp": { "name": Item.TYPES.Q_PARALLEL_COMPOSITION },
								"rqflow": { "name": Item.TYPES.Q_FLOW }
							}
						},
						"raut": { "name": Item.TYPES.AUTOMATON },
						"rinterrupt": { "name": Item.TYPES.INTERRUPT },
						"rseq": { "name": Item.TYPES.SEQUENCE },
						"rflow": { "name": Item.TYPES.FLOW },
						"rchoice": { "name": Item.TYPES.CHOICE },
						"rqchoice": { "name": Item.TYPES.Q_CHOICE },
						"rclosure": { "name": Item.TYPES.CLOSURE },
						"rguard": { "name": Item.TYPES.GUARD },
						"rpguard": { "name": Item.TYPES.PERSISTENT_GUARD },
						"rcall": { "name": Item.TYPES.CALL }
					}
				},
				"addLeft": {
					"name": "Add left",
					"icon": "fas fa-plus",
					"items": {
						"timedAstd": {
							"name": "Timed ASTD",
							"items": {
								"ldelay": { "name": Item.TYPES.DELAY },
								"lperdelay": { "name": Item.TYPES.PERSISTENT_DELAY }
							}
						},
						"parallel": {
							"name": "Parallel ASTD",
							"items": {
								"rqsynch": { "name": Item.TYPES.Q_SYNCHRONIZATION },
								"rqinterleave": { "name": Item.TYPES.Q_INTERLEAVE },
								"rqpcomp": { "name": Item.TYPES.Q_PARALLEL_COMPOSITION },
								"rqflow": { "name": Item.TYPES.Q_FLOW }
							}
						},
						"lqchoice": { "name": Item.TYPES.Q_CHOICE },
						"lclosure": { "name": Item.TYPES.CLOSURE },
						"lguard": { "name": Item.TYPES.GUARD },
						"lpguard": { "name": Item.TYPES.PERSISTENT_GUARD }
					}
				},
				"changetype": {
					"name": "Change Type",
					"icon": "far fa-edit",
					"items": {
						"timedAstd": {
							"name": "Timed ASTD",
							"items": {
								"ctimeout": { "name": Item.TYPES.TIMEOUT },
								"cpto": { "name": Item.TYPES.PERSISTENT_TIMEOUT },
								"cti": { "name": Item.TYPES.TIMED_INTERRUPT },
								"cdelay": { "name": Item.TYPES.DELAY },
								"cperdelay": { "name": Item.TYPES.PERSISTENT_DELAY }
							}
						},
						"parallel": {
							"name": "Parallel ASTD",
							"items": {
								"rsynch": { "name": Item.TYPES.SYNCHRONIZATION },
								"rqsynch": { "name": Item.TYPES.Q_SYNCHRONIZATION },
								"rinterleave": { "name": Item.TYPES.INTERLEAVE },
								"rqinterleave": { "name": Item.TYPES.Q_INTERLEAVE },
								"rpcomp": { "name": Item.TYPES.PARALLEL_COMPOSITION },
								"rqpcomp": { "name": Item.TYPES.Q_PARALLEL_COMPOSITION },
								"rqflow": { "name": Item.TYPES.Q_FLOW }
							}
						},
						"caut": { "name": Item.TYPES.AUTOMATON },
						"cinterrupt": { "name": Item.TYPES.INTERRUPT },
						"cseq": { "name": Item.TYPES.SEQUENCE },
						"cflow": { "name": Item.TYPES.FLOW },
						"cchoice": { "name": Item.TYPES.CHOICE },
						"cqchoice": { "name": Item.TYPES.Q_CHOICE },
						"cclosure": { "name": Item.TYPES.CLOSURE },
						"cguard": { "name": Item.TYPES.GUARD },
						"cpguard": { "name": Item.TYPES.PERSISTENT_GUARD },
						"ccall": { "name": Item.TYPES.CALL }
					}
				},
				"delete": {
					"name": "Delete",
					"icon": "far fa-trash-alt"
				}
			},
			items2 = {
				"addRight": {
					"name": "Add right",
					"icon": "fas fa-plus",
					"items": {
						"timedAstd": {
							"name": "Timed ASTD",
							"items": {
								"rdelay": { "name": Item.TYPES.DELAY },
								"rperdelay": { "name": Item.TYPES.PERSISTENT_DELAY }
							}
						},
						"parallel": {
							"name": "Parallel ASTD",
							"items": {
								"rqsynch": { "name": Item.TYPES.Q_SYNCHRONIZATION },
								"rqinterleave": { "name": Item.TYPES.Q_INTERLEAVE },
								"rqpcomp": { "name": Item.TYPES.Q_PARALLEL_COMPOSITION },
								"rqflow": { "name": Item.TYPES.Q_FLOW }
							}
						},
						"rqchoice": { "name": Item.TYPES.Q_CHOICE },
						"rclosure": { "name": Item.TYPES.CLOSURE },
						"rguard": { "name": Item.TYPES.GUARD },
						"rpguard": { "name": Item.TYPES.PERSISTENT_GUARD },
						"rcall": { "name": Item.TYPES.CALL }
					}
				},
				"addLeft": {
					"name": "Add left",
					"icon": "fas fa-plus",
					"items": {
						"timedAstd": {
							"name": "Timed ASTD",
							"items": {
								"ldelay": { "name": Item.TYPES.DELAY },
								"lperdelay": { "name": Item.TYPES.PERSISTENT_DELAY }
							}
						},
						"parallel": {
							"name": "Parallel ASTD",
							"items": {
								"rqsynch": { "name": Item.TYPES.Q_SYNCHRONIZATION },
								"rqinterleave": { "name": Item.TYPES.Q_INTERLEAVE },
								"rqpcomp": { "name": Item.TYPES.Q_PARALLEL_COMPOSITION },
								"rqflow": { "name": Item.TYPES.Q_FLOW }
							}
						},
						"lqchoice": { "name": Item.TYPES.Q_CHOICE },
						"lclosure": { "name": Item.TYPES.CLOSURE },
						"lguard": { "name": Item.TYPES.GUARD },
						"lpguard": { "name": Item.TYPES.PERSISTENT_GUARD },
						"lcall": { "name": Item.TYPES.CALL }
					}
				},
				"changetype": {
					"name": "Change Type",
					"icon": "far fa-edit",
					"items": {
						"timedAstd": {
							"name": "Timed ASTD",
							"items": {
								"cdelay": { "name": Item.TYPES.DELAY },
								"cperdelay": { "name": Item.TYPES.PERSISTENT_DELAY }
							}
						},
						"parallel": {
							"name": "Parallel ASTD",
							"items": {
								"cqsynch": { "name": Item.TYPES.Q_SYNCHRONIZATION },
								"cqinterleave": { "name": Item.TYPES.Q_INTERLEAVE },
								"cqpcomp": { "name": Item.TYPES.Q_PARALLEL_COMPOSITION },
								"cqflow": { "name": Item.TYPES.Q_FLOW }
							}
						},
						"cqchoice": { "name": Item.TYPES.Q_CHOICE },
						"cclosure": { "name": Item.TYPES.CLOSURE },
						"cguard": { "name": Item.TYPES.GUARD },
						"cpguard": { "name": Item.TYPES.PERSISTENT_GUARD },
						"ccall": { "name": Item.TYPES.CALL }
					}
				},
				"delete": {
					"name": "Delete",
					"icon": "far fa-trash-alt"
				}
			},
			items3 = {
				"addLeft": {
					"name": "Add left",
					"icon": "fas fa-plus",
					"items": {
						"timedAstd": {
							"name": "Timed ASTD",
							"items": {
								"ldelay": { "name": Item.TYPES.DELAY },
								"lperdelay": { "name": Item.TYPES.PERSISTENT_DELAY }
							}
						},
						"parallel": {
							"name": "Parallel ASTD",
							"items": {
								"lqsynch": { "name": Item.TYPES.Q_SYNCHRONIZATION },
								"lqinterleave": { "name": Item.TYPES.Q_INTERLEAVE },
								"lqpcomp": { "name": Item.TYPES.Q_PARALLEL_COMPOSITION },
								"lqflow": { "name": Item.TYPES.Q_FLOW }
							}
						},
						"lqchoice": { "name": Item.TYPES.Q_CHOICE },
						"lclosure": { "name": Item.TYPES.CLOSURE },
						"lguard": { "name": Item.TYPES.GUARD },
						"lpguard": { "name": Item.TYPES.PERSISTENT_GUARD },
						"lcall": { "name": Item.TYPES.CALL }
					}
				},
				"changetype": {
					"name": "Change Type",
					"icon": "far fa-edit",
					"items": {
						"timedAstd": {
							"name": "Timed ASTD",
							"items": {
								"ctimeout": { "name": Item.TYPES.TIMEOUT },
								"cpto": { "name": Item.TYPES.PERSISTENT_TIMEOUT },
								"cti": { "name": Item.TYPES.TIMED_INTERRUPT },
								"cdelay": { "name": Item.TYPES.DELAY },
								"cperdelay": { "name": Item.TYPES.PERSISTENT_DELAY }
							}
						},
						"parallel": {
							"name": "Parallel ASTD",
							"items": {
								"csynch": { "name": Item.TYPES.SYNCHRONIZATION },
								"cqsynch": { "name": Item.TYPES.Q_SYNCHRONIZATION },
								"cinterleave": { "name": Item.TYPES.INTERLEAVE },
								"cqinterleave": { "name": Item.TYPES.Q_INTERLEAVE },
								"cpcomp": { "name": Item.TYPES.PARALLEL_COMPOSITION },
								"cqpcomp": { "name": Item.TYPES.Q_PARALLEL_COMPOSITION },
								"cqflow": { "name": Item.TYPES.Q_FLOW }
							}
						},
						"caut": { "name": Item.TYPES.AUTOMATON },
						"cinterrupt": { "name": Item.TYPES.INTERRUPT },
						"cseq": { "name": Item.TYPES.SEQUENCE },
						"cflow": { "name": Item.TYPES.FLOW },
						"cchoice": { "name": Item.TYPES.CHOICE },
						"cqchoice": { "name": Item.TYPES.Q_CHOICE },
						"cclosure": { "name": Item.TYPES.CLOSURE },
						"cguard": { "name": Item.TYPES.GUARD },
						"cpguard": { "name": Item.TYPES.PERSISTENT_GUARD },
						"ccall": { "name": Item.TYPES.CALL }
					}
				},
				"delete": {
					"name": "Delete",
					"icon": "far fa-trash-alt"
				}
			};
		$.contextMenu({
			selector: s,
			build: function() {
				var options = {
						zIndex: 100,
						callback: function(key) {
							var index,
								i;

							if (that.parent.tabs) {
								for (i = 0; i < that.parent.tabs.length; i++) {
									if (that.parent.tabs[i] === that) {
										index = i;
									}
								}
								var left = ["laut", "lseq", "linterrupt", "ltimeout", "lpto", "ldelay", "lperdelay", "lti", "lflow", "lchoice", "lqchoice", "lclosure", "lguard", "lpguard", "lsynch", "lqsynch", "linterleave", "lqinterleave", "lpcomp", "lqpcomp", "lqflow"],
									change = ["caut", "cseq", "cinterrupt", "ctimeout", "cpto", "cdelay", "cperdelay", "cti", "cflow", "cchoice", "cqchoice", "cclosure", "cguard", "cpguard", "csynch", "cqsynch", "cinterleave", "cqinterleave", "cpcomp", "cqpcomp", "ccall", "cqflow"];
								if (!(that.parent.tabs[index] instanceof Automaton || that.parent.tabs[index] instanceof Call || that.parent.tabs[index] instanceof Choice || that.parent.tabs[index] instanceof Flow || that.parent.tabs[index] instanceof Interleave || that.parent.tabs[index] instanceof ParallelComposition || that.parent.tabs[index] instanceof Sequence || that.parent.tabs[index] instanceof Synchronization || that.parent.tabs[index] instanceof Timeout || that.parent.tabs[index] instanceof Interrupt || that.parent.tabs[index] instanceof PersistentTimeout || that.parent.tabs[index] instanceof TimedInterrupt) || left.includes(key) || change.includes(key)) {
									var t,
										oldTabType = that.parent.tabs.length > 0 ? that.parent.tabs[index].constructor.name : "";
									if (key === "caut") {
										if (that.parent.tabs[index] instanceof Automaton) {
											return;
										} else {
											t = new Automaton(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "raut") {
										t = new Automaton(window.id.get(), that.parent);
										t.rightClickable();
										if (that.parent.tabs[index] === that.parent.tabs[that.parent.tabs.length - 1]) {
											that.parent.tabs.push(t);
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
											for (i = 0; i < that.parent.tabs.length; i++) {
												that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
											}
											t.e_tab.style.backgroundColor = "white";

										}
									} else if (key === "cseq") {
										if (that.parent.tabs[index] instanceof Sequence) {
											return;
										} else {
											t = new Sequence(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rseq") {
										t = new Sequence(window.id.get(), that.parent);
										t.rightClickable();
										if (that.parent.tabs[index] === that.parent.tabs[that.parent.tabs.length - 1]) {
											that.parent.tabs.push(t);
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
											for (i = 0; i < that.parent.tabs.length; i++) {
												that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
											}
											t.e_tab.style.backgroundColor = "white";
										}
									} else if (key === "cinterrupt") {
										if (that.parent.tabs[index] instanceof Interrupt) {
											return;
										} else {
											t = new Interrupt(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rinterrupt") {
										t = new Interrupt(window.id.get(), that.parent);
										t.rightClickable();
										if (that.parent.tabs[index] === that.parent.tabs[that.parent.tabs.length - 1]) {
											that.parent.tabs.push(t);
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
											for (i = 0; i < that.parent.tabs.length; i++) {
												that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
											}
											t.e_tab.style.backgroundColor = "white";
										}
									} else if (key === "ctimeout") {
										if (that.parent.tabs[index] instanceof Timeout) {
											return;
										} else {
											t = new Timeout(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rtimeout") {
										t = new Timeout(window.id.get(), that.parent);
										t.rightClickable();
										if (that.parent.tabs[index] === that.parent.tabs[that.parent.tabs.length - 1]) {
											that.parent.tabs.push(t);
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
											for (i = 0; i < that.parent.tabs.length; i++) {
												that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
											}
											t.e_tab.style.backgroundColor = "white";
										}
									} else if (key === "cpto") {
										if (that.parent.tabs[index] instanceof PersistentTimeout) {
											return;
										} else {
											t = new PersistentTimeout(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rpto") {
										t = new PersistentTimeout(window.id.get(), that.parent);
										t.rightClickable();
										if (that.parent.tabs[index] === that.parent.tabs[that.parent.tabs.length - 1]) {
											that.parent.tabs.push(t);
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
											for (i = 0; i < that.parent.tabs.length; i++) {
												that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
											}
											t.e_tab.style.backgroundColor = "white";
										}
									} else if (key === "cti") {
										if (that.parent.tabs[index] instanceof TimedInterrupt) {
											return;
										} else {
											t = new TimedInterrupt(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rti") {
										t = new TimedInterrupt(window.id.get(), that.parent);
										t.rightClickable();
										if (that.parent.tabs[index] === that.parent.tabs[that.parent.tabs.length - 1]) {
											that.parent.tabs.push(t);
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
											for (i = 0; i < that.parent.tabs.length; i++) {
												that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
											}
											t.e_tab.style.backgroundColor = "white";
										}
									} else if (key === "cchoice") {
										if (that.parent.tabs[index] instanceof Choice) {
											return;
										} else {
											t = new Choice(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rchoice") {
										t = new Choice(window.id.get(), that.parent);
										t.rightClickable();
										if (that.parent.tabs[index] === that.parent.tabs[that.parent.tabs.length - 1]) {
											that.parent.tabs.push(t);
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
											for (i = 0; i < that.parent.tabs.length; i++) {
												that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
											}
											t.e_tab.style.backgroundColor = "white";
										}
									} else if (key === "cflow") {
										if (that.parent.tabs[index] instanceof Flow) {
											return;
										} else {
											t = new Flow(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rflow") {
										t = new Flow(window.id.get(), that.parent);
										t.rightClickable();
										if (that.parent.tabs[index] === that.parent.tabs[that.parent.tabs.length - 1]) {
											that.parent.tabs.push(t);
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
											for (i = 0; i < that.parent.tabs.length; i++) {
												that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
											}
											t.e_tab.style.backgroundColor = "white";
										}
									} else if (key === "cqflow") {
										if (that.parent.tabs[index] instanceof QFlow) {
											return;
										} else {
											t = new QFlow(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code, that.parent.tabs[index].labels, that.parent.tabs[index].variable);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rqflow" || key === "lqflow") {
										t = new QFlow(window.id.get(), that.parent);
										t.rightClickable();
										if (key === "rqflow") {
											that.parent.tabs.splice(index + 1, 0, t);

										} else {
											that.parent.tabs.splice(index, 0, t);

										}
										if (that.parent.tabs[0] === t) {
											window.panel.show(that.parent);
										} else {
											window.panel.show(t);
										}
										for (i = 0; i < that.parent.tabs.length; i++) {

											that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
										}
										t.e_tab.style.backgroundColor = "white";
									} else if (key === "cqchoice") {
										if (that.parent.tabs[index] instanceof QChoice) {
											return;
										} else {
											t = new QChoice(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code, that.parent.tabs[index].variable);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rqchoice" || key === "lqchoice") {
										t = new QChoice(window.id.get(), that.parent);
										t.rightClickable();
										//that.parent.tabs.push(t);
										if (key === "rqchoice" || key === "cqchoice") {
											that.parent.tabs.splice(index + 1, 0, t);

										} else {
											that.parent.tabs.splice(index, 0, t);

										}
										if (that.parent.tabs[0] === t) {
											window.panel.show(that.parent);
										} else {
											window.panel.show(t);
										}
										for (i = 0; i < that.parent.tabs.length; i++) {
											that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
										}
										t.e_tab.style.backgroundColor = "white";
									} else if (key === "ccall") {
										if (that.parent.tabs[index] instanceof Call) {
											return;
										} else {
											t = new Call(window.id.get(), that.parent, that.parent.tabs[index].astd, that.parent.tabs[index].argument);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rcall") {
										t = new Call(window.id.get(), that.parent);
										t.rightClickable();
										if (that.parent.tabs[index] === that.parent.tabs[that.parent.tabs.length - 1]) {
											that.parent.tabs.push(t);
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
											for (i = 0; i < that.parent.tabs.length; i++) {

												that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
											}
											t.e_tab.style.backgroundColor = "white";
										}
									} else if (key === "cclosure") {
										if (that.parent.tabs[index] instanceof Closure) {
											return;
										} else {
											t = new Closure(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rclosure" || key === "lclosure") {
										t = new Closure(window.id.get(), that.parent);
										t.rightClickable();
										if (key === "rclosure" || key === "cclosure") {
											that.parent.tabs.splice(index + 1, 0, t);

										} else {
											that.parent.tabs.splice(index, 0, t);

										}
										if (that.parent.tabs[0] === t) {
											window.panel.show(that.parent);
										} else {
											window.panel.show(t);
										}
										for (i = 0; i < that.parent.tabs.length; i++) {

											that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
										}
										t.e_tab.style.backgroundColor = "white";
									} else if (key === "cguard") {
										if (that.parent.tabs[index] instanceof Guard) {
											return;
										} else {
											t = new Guard(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code, that.parent.tabs[index].predicate);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rguard" || key === "lguard") {
										t = new Guard(window.id.get(), that.parent);
										t.rightClickable();
										if (key === "rguard" || key === "cguard") {
											that.parent.tabs.splice(index + 1, 0, t);

										} else {
											that.parent.tabs.splice(index, 0, t);

										}
										if (that.parent.tabs[0] === t) {
											window.panel.show(that.parent);
										} else {
											window.panel.show(t);
										}
										for (i = 0; i < that.parent.tabs.length; i++) {
											that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
										}
										t.e_tab.style.backgroundColor = "white";
									} else if (key === "cpguard") {
										if (that.parent.tabs[index] instanceof PersistentGuard) {
											return;
										} else {
											t = new PersistentGuard(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code, that.parent.tabs[index].predicate);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rpguard" || key === "lpguard") {
										t = new PersistentGuard(window.id.get(), that.parent);
										t.rightClickable();
										if (key === "rpguard" || key === "cpguard") {
											that.parent.tabs.splice(index + 1, 0, t);

										} else {
											that.parent.tabs.splice(index, 0, t);

										}
										if (that.parent.tabs[0] === t) {
											window.panel.show(that.parent);
										} else {
											window.panel.show(t);
										}
										for (i = 0; i < that.parent.tabs.length; i++) {
											that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
										}
										t.e_tab.style.backgroundColor = "white";
									} else if (key === "cdelay") {
										if (that.parent.tabs[index] instanceof Delay) {
											return;
										} else {
											t = new Delay(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code, that.parent.tabs[index].timer);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rdelay" || key === "ldelay") {
										t = new Delay(window.id.get(), that.parent);
										t.rightClickable();
										if (key === "rdelay" || key === "cdelay") {
											that.parent.tabs.splice(index + 1, 0, t);

										} else {
											that.parent.tabs.splice(index, 0, t);

										}
										if (that.parent.tabs[0] === t) {
											window.panel.show(that.parent);
										} else {
											window.panel.show(t);
										}
										for (i = 0; i < that.parent.tabs.length; i++) {
											that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
										}
										t.e_tab.style.backgroundColor = "white";
									} else if (key === "cperdelay") {
										if (that.parent.tabs[index] instanceof PersistentDelay) {
											return;
										} else {
											t = new PersistentDelay(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code, that.parent.tabs[index].timer);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rperdelay" || key === "lperdelay") {
										t = new PersistentDelay(window.id.get(), that.parent);
										t.rightClickable();
										if (key === "rperdelay" || key === "cperdelay") {
											that.parent.tabs.splice(index + 1, 0, t);

										} else {
											that.parent.tabs.splice(index, 0, t);

										}
										if (that.parent.tabs[0] === t) {
											window.panel.show(that.parent);
										} else {
											window.panel.show(t);
										}
										for (i = 0; i < that.parent.tabs.length; i++) {
											that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
										}
										t.e_tab.style.backgroundColor = "white";
									} else if (key === "csynch") {
										if (that.parent.tabs[index] instanceof Synchronization) {
											return;
										} else {
											t = new Synchronization(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code, that.parent.tabs[index].labels);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rsynch") {
										t = new Synchronization(window.id.get(), that.parent);
										t.rightClickable();
										if (that.parent.tabs[index] === that.parent.tabs[that.parent.tabs.length - 1]) {
											that.parent.tabs.push(t);
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
											for (i = 0; i < that.parent.tabs.length; i++) {
												that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
											}
											t.e_tab.style.backgroundColor = "white";
										}
									} else if (key === "cqsynch") {
										if (that.parent.tabs[index] instanceof QSynchronization) {
											return;
										} else {
											t = new QSynchronization(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code, that.parent.tabs[index].labels, that.parent.tabs[index].variable);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rqsynch" || key === "lqsynch") {
										t = new QSynchronization(window.id.get(), that.parent);
										t.rightClickable();
										if (key === "rqsynch") {
											that.parent.tabs.splice(index + 1, 0, t);

										} else {
											that.parent.tabs.splice(index, 0, t);

										}
										if (that.parent.tabs[0] === t) {
											window.panel.show(that.parent);
										} else {
											window.panel.show(t);
										}
										for (i = 0; i < that.parent.tabs.length; i++) {

											that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
										}
										t.e_tab.style.backgroundColor = "white";
									} else if (key === "cpcomp") {
										if (that.parent.tabs[index] instanceof ParallelComposition) {
											return;
										} else {
											t = new ParallelComposition(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rpcomp") {
										t = new ParallelComposition(window.id.get(), that.parent);
										t.rightClickable();
										if (that.parent.tabs[index] === that.parent.tabs[that.parent.tabs.length - 1]) {
											that.parent.tabs.push(t);
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
											for (i = 0; i < that.parent.tabs.length; i++) {
												that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
											}
											t.e_tab.style.backgroundColor = "white";
										}
									} else if (key === "cqpcomp") {
										if (that.parent.tabs[index] instanceof QParallelComposition) {
											return;
										} else {
											t = new QParallelComposition(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code, that.parent.tabs[index].variable);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rqpcomp" || key === "lqpcomp") {
										t = new QParallelComposition(window.id.get(), that.parent);
										t.rightClickable();
										if (key === "rqpcomp") {
											that.parent.tabs.splice(index + 1, 0, t);
										} else {
											that.parent.tabs.splice(index, 0, t);
										}
										if (that.parent.tabs[0] === t) {
											window.panel.show(that.parent);
										} else {
											window.panel.show(t);
										}
										for (i = 0; i < that.parent.tabs.length; i++) {

											that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
										}
										t.e_tab.style.backgroundColor = "white";
									} else if (key === "cinterleave") {
										if (that.parent.tabs[index] instanceof Interleave) {
											return;
										} else {
											t = new Interleave(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rinterleave") {
										t = new Interleave(window.id.get(), that.parent);
										t.rightClickable();
										if (that.parent.tabs[index] === that.parent.tabs[that.parent.tabs.length - 1]) {
											that.parent.tabs.push(t);
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
											for (i = 0; i < that.parent.tabs.length; i++) {
												that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
											}
											t.e_tab.style.backgroundColor = "white";
										}
									} else if (key === "cqinterleave") {
										if (that.parent.tabs[index] instanceof QInterleave) {
											return;
										} else {
											t = new QInterleave(window.id.get(), that.parent, that.parent.tabs[index].attributes, that.parent.tabs[index].code, that.parent.tabs[index].variable);
											t.label = that.parent.tabs[index].label;
											t.rightClickable();
											that.parent.tabs[index] = t;
											if (that.parent.tabs[0] === t) {
												window.panel.show(that.parent);
											} else {
												window.panel.show(t);
											}
										}
									} else if (key === "rqinterleave" || key === "lqinterleave") {
										t = new QInterleave(window.id.get(), that.parent);
										t.rightClickable();
										if (key === "rqinterleave") {
											that.parent.tabs.splice(index + 1, 0, t);

										} else {
											that.parent.tabs.splice(index, 0, t);

										}
										if (that.parent.tabs[0] === t) {
											window.panel.show(that.parent);
										} else {
											window.panel.show(t);
										}
										for (i = 0; i < that.parent.tabs.length; i++) {
											that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
										}
										t.e_tab.style.backgroundColor = "white";
									}
								}

								// Specific action on change tab
								if (oldTabType === Item.TYPES.CALL) {
									$(that.e_label).empty();
								}
								if (oldTabType === SEQUENCE && that.parent.tabs[index].constructor.name !== SEQUENCE) {
									that.parent.getFile().removeLinkSequence();
								}

								if (!((oldTabType === INTERRUPT || oldTabType === TIMEOUT || oldTabType === PERSISTENTTIMEOUT || oldTabType === TIMEDINTERRUPT) && (that.parent.tabs[index].constructor.name === INTERRUPT || that.parent.tabs[index].constructor.name === TIMEOUT || that.parent.tabs[index].constructor.name === TIMEDINTERRUPT || that.parent.tabs[index].constructor.name === PERSISTENTTIMEOUT))) {
									that.parent.getFile().removeLinkTimed();
								}

								that.parent.refreshTabs();
							}
							if (key === "delete") {
								var tab = that.parent.tabs[index];
								//that.parent.getFile().removeLinkSequence();
								if (tab.delete()) {
									that.parent.removeTab(tab.id);
									that.parent.refreshTabs();
									window.panel.show(that.parent);
								}
								if (that.parent.tabs !== undefined && that.parent.tabs !== null) {
									for (i = 0; i < that.parent.tabs.length; i++) {
										if (i === 0) {
											that.parent.tabs[i].e_tab.style.backgroundColor = "white";
										} else {
											that.parent.tabs[i].e_tab.style.backgroundColor = "rgb(207,211,218)";
										}
									}
									that.parent.refreshTabs();
								}
							}
						},
						items: {}
					},
					list = that.parent.tabs,
					index;
				for (var i = 0; i < list.length; i++) {
					if (list[i] === that) {
						index = i;
					}
				}
				if (list[list.length - 1].id === that.id) {
					if (list[index] instanceof Automaton || list[index] instanceof Call || list[index] instanceof Choice || list[index] instanceof Flow || list[index] instanceof Interleave || list[index] instanceof ParallelComposition || list[index] instanceof Sequence || list[index] instanceof Synchronization || list[index] instanceof Timeout || list[index] instanceof Interrupt || list[index] instanceof PersistentTimeout || list[index] instanceof TimedInterrupt) {
						options.items = items3;
					} else {
						options.items = items1;
					}
				} else {
					if (list[index] instanceof Automaton || list[index] instanceof Call || list[index] instanceof Choice || list[index] instanceof Flow || list[index] instanceof Interleave || list[index] instanceof ParallelComposition || list[index] instanceof Sequence || list[index] instanceof Synchronization || list[index] instanceof Timeout || list[index] instanceof Interrupt || list[index] instanceof PersistentTimeout || list[index] instanceof TimedInterrupt) {
						options.items = items2;
					} else {
						var items4 = JSON.parse(JSON.stringify(items2));
						delete items4.addLeft.items["lcall"];
						options.items = items4;
					}
				}
				return options;
			}
		});
	}

	get id() {
		return this.m_id;
	}

	set id(id) {
		this.e_tab.id = id;

		this.m_id = id;
	}

	get parent() {
		return this.m_parent;
	}

	set parent(parent) {
		this.m_parent = parent;
	}

	get attributes() {
		return this.m_attributes;
	}

	set attributes(attributes) {
		this.m_attributes = attributes;
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
	}

	delete() {
		// manually remove contextMenu so it is built again even if we load a file with the same e_tab.id value
		$.contextMenu("destroy", `.tab#${this.id}`);
		return true;
	}

	getLowerBoundOfTestDomain() {
		if (this.test && this.domain_parameter_error === "") {
			return parseInt(this.parameter_domain.split("..")[0]);
		}
	}

	getUpperBoundOfTestDomain() {
		if (this.test && this.domain_parameter_error === "") {
			return parseInt(this.parameter_domain.split("..")[1]);
		}
	}

	clickable() {
		$(this.e_tab).click(function() {
			event.stopPropagation();
		});
	}

	getAllTopLevelTestParameters() {
		var allTopLevelTestParameters = [];
		window.project.files.forEach(function(file) {
			//refresh de allTestParameters pour empecher la duplication
			file.allTestParameters = [];
			//charger allTestParameters par les test_parameters du file courant
			file.astd.getAllTestParameters();
			allTopLevelTestParameters = allTopLevelTestParameters.concat(file.allTestParameters);

		});
		return allTopLevelTestParameters;
	}

	/**
	 * Validate the value of the executable code and get the error message
	 * @returns {string} The resulting error text from validations
	 */
	validateCode() {
		if (this.code) {
			var verify_result = Validator.syntax_Verify(this.code, window.action_grammar_object);
			this.action_text_error = verify_result.message;

			if (this.action_text_error.length === 0) {
				this.action_text_error = Validator.checkActionSemantic(verify_result.result, this);
			}
		} else {
			this.action_text_error = "";
		}
		return this.action_text_error;
	}

	/**
	 * validate the tab (astd) label against reserved labels for history states. Doesn't need to check history because tabs never have history state
	 * @returns {string} The error message resulting from the validation
	 */
	validateLabel() {
		if (this.label === "H" || this.label === "H*") this.label_text_error = "Label reserved for history states only."; else this.label_text_error = "";

		return this.label_text_error;
	}

	/**
	 * Utility function to execute blur action on enter key pressed. Must be bind to the input associated to the action
	 * @param event: fired event object
	 */
	executeOnKeyUp(event) {
		if (event.keyCode === 13) {
			this.blur();
		}
	}

	/**
	 * Validates the values of the synchronization set. Only used for synchronization and Qsynchronization
	 * @returns {string}: return the error message to display in the panel
	 */
	validateSyncEvents() {
		var error = "";
		this.syncEvents.forEach((ev) => {
			let ver = Validator.syntax_Verify(ev, window.event_name_grammar_object);
			if (!ver.isValid) {
				error += "Error event (" + ev + ") : " + ver.message + "<br />";
			}
		});
		return error;
	}

	/**
	 * Utility getter used to create specific components of a synchronization
	 * qsynchronization and synchronization need the same extra components, Since they both extend from tab,
	 * is has been decided to move it here to reduce the code duplication
	 * @return: object containing functions to call to create a component
	 */
	get syncPanel() {
		var that = this;
		return {
			createLabels: function() {
				var container = document.createElement("div"),
					labelsBlock = CompBuilder.generateTextBlock("input", "Synchronization set:", that.validateSyncEvents(), that.labels, "Ex: e1, e2"),
					hr = document.createElement("hr");
				// attach actions to input field events
				labelsBlock[TEXT_INPUT].onblur = function() {
					that.labels = event.target.value;
					// Validate Syntax for event in Synchronization Set
					CompBuilder.setInputValidity(labelsBlock[TEXT_INPUT], labelsBlock[ERROR_LABEL], that.validateSyncEvents());
					that.parent.refreshTabs();
				};
				labelsBlock[TEXT_INPUT].onkeyup = that.executeOnKeyUp.bind(labelsBlock[TEXT_INPUT]);

				[...labelsBlock, hr].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			}
		};
	}

	/**
	 * Utility getter used to create generic components of a tab
	 * @return: object containing functions to call to create a component
	 */
	get tabPanel() {
		var that = this;

		return {
			createHeader: function() {
				var container = document.createElement("div"),
					title = document.createElement("h5"),
					labelInput = document.createElement("input"),
					labelError = CompBuilder.generateFieldError(),
					typeLabel = document.createElement("div"),
					hr = document.createElement("hr");

				title.textContent = "ASTD";
				labelInput.className = "form-control inline-input itemNameInput";
				labelInput.formNoValidate = true;
				labelInput.placeholder = "Label";
				labelInput.value = that.label;
				labelInput.onblur = function() {
					that.label = event.target.value;
					CompBuilder.setInputValidity(labelInput, labelError, that.validateLabel());
					that.parent.refreshTabs();
				};
				labelInput.onkeyup = that.executeOnKeyUp.bind(labelInput);
				CompBuilder.setInputValidity(labelInput, labelError, that.label_text_error);

				typeLabel.className = "panel-default";
				typeLabel.textContent = that.getType();

				[title, labelInput, labelError, typeLabel, hr].forEach((comp) => {
					container.appendChild(comp);
				});

				return container;
			},
			createAttributes: function() {
				var container = document.createElement("div"),
					components = CompBuilder.generateVarsListSection("Attributes", "attributesList", that.attributes, "There is no attributes");
				// attach add button action
				components[VARS_ADD_BTN].onclick = function() {
					var newAttribute = new Attribute(that);
					// remove the empty text if it is the first attribute to add
					if (that.attributes.length === 0) {
						$(components[VARS_LIST]).empty();
					}
					that.attributes.push(newAttribute);
					components[VARS_LIST].appendChild(newAttribute.toPanel());
					// Trigger a change on new element child textarea to resize it properly
					$(components[VARS_LIST].lastElementChild).find("textarea").change();
				};

				components.forEach((component) => {
					container.appendChild(component);
				});
				return container;
			},

			createCode: function() {
				var codeErrorLabel = CompBuilder.generateFieldError(),
					container = document.createElement("div"),
					codeBlock = CompBuilder.generateTextBlock("textarea", "ASTD Action:", that.validateCode(), that.code, "Ex: M.f(args) | M::f(args) | {c++ code}"),
					hr = document.createElement("hr");
				// attach action to blur event of textarea
				codeBlock[TEXT_INPUT].onblur = function() {
					codeErrorLabel.innerHTML = "";
					that.code = event.target.value;
					CompBuilder.setInputValidity(codeBlock[TEXT_INPUT], codeBlock[ERROR_LABEL], that.validateCode());
				};

				[...codeBlock, hr].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			},

			createInterruptAction: function() {
				var codeErrorLabel = CompBuilder.generateFieldError(),
					container = document.createElement("div"),
					codeBlock = CompBuilder.generateTextBlock("textarea", "Interrupt Action:", that.validateCode(), that.interruptCode, "Ex: M.f(args) | M::f(args) | {c++ code}"),
					hr = document.createElement("hr");
				// attach action to blur event of textarea
				codeBlock[TEXT_INPUT].onblur = function() {
					codeErrorLabel.innerHTML = "";
					that.interruptCode = event.target.value;
					CompBuilder.setInputValidity(codeBlock[TEXT_INPUT], codeBlock[ERROR_LABEL], that.validateCode());
				};

				[...codeBlock, hr].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			},

			createTestSection: function() {
				var container = document.createElement("div"),
					checkBlock = CompBuilder.generateCheckboxBlock("Test parameter", that.test),
					nameBlock = CompBuilder.generateTextBlock("input", "Name:", that.name_parameter_error, that.parameter_name, "Name"),
					domainBlock = CompBuilder.generateTextBlock("input", "Domain:", that.domain_parameter_error, that.parameter_domain, "Ex: 1..10"),
					hr = document.createElement("hr");

				nameBlock.concat(domainBlock).forEach((comp) => {
					comp.classList.add("test-param-check-hide");
					if (!that.test) comp.classList.add("d-none");
				});
				// attach action on checkbox click
				checkBlock.children[CHECKBOX].onclick = function() {
					that.test = event.target.checked;
					$(".test-param-check-hide").toggleClass("d-none");
					if (that.test) {
						CompBuilder.setInputValidity(nameBlock[TEXT_INPUT], nameBlock[ERROR_LABEL], "Required field");
						CompBuilder.setInputValidity(domainBlock[TEXT_INPUT], domainBlock[ERROR_LABEL], "Required field");
						that.domain_parameter_error = that.name_parameter_error = "Required field";
					} else {
						CompBuilder.setInputValidity(nameBlock[TEXT_INPUT], nameBlock[ERROR_LABEL], "");
						CompBuilder.setInputValidity(domainBlock[TEXT_INPUT], domainBlock[ERROR_LABEL], "");
						that.domain_parameter_error = that.name_parameter_error = "";
						nameBlock[TEXT_INPUT].value = that.parameter_name = "";
						domainBlock[TEXT_INPUT].value = that.parameter_domain = "";
					}
					that.parent.refreshTabs();
				};
				// attach action on name input blur and enter key up events
				nameBlock[TEXT_INPUT].onblur = function() {
					if (nameBlock[TEXT_INPUT].value) {
						if (that.getAllTopLevelTestParameters().indexOf(nameBlock[TEXT_INPUT].value) !== -1 && nameBlock[TEXT_INPUT].value !== that.parameter_name) {
							that.name_parameter_error = "parameter already used";
						} else {
							that.name_parameter_error = "";
						}
					} else {
						that.name_parameter_error = "Required field";
					}
					that.parameter_name = nameBlock[TEXT_INPUT].value;
					CompBuilder.setInputValidity(nameBlock[TEXT_INPUT], nameBlock[ERROR_LABEL], that.name_parameter_error);
					that.parent.refreshTabs();
				};
				nameBlock[TEXT_INPUT].onkeyup = that.executeOnKeyUp.bind(nameBlock[TEXT_INPUT]);
				// attach action on domain input blur and enter key events
				domainBlock[TEXT_INPUT].onblur = function() {
					if (domainBlock[TEXT_INPUT].value) {
						that.domain_parameter_error = Validator.checkTestDomainSyntax(domainBlock[TEXT_INPUT].value);
					} else {
						that.domain_parameter_error = "Required field";
					}
					that.parameter_domain = domainBlock[TEXT_INPUT].value;
					that.parent.refreshTabs();
					CompBuilder.setInputValidity(domainBlock[TEXT_INPUT], domainBlock[ERROR_LABEL], that.domain_parameter_error);
				};
				domainBlock[TEXT_INPUT].onkeyup = that.executeOnKeyUp.bind(domainBlock[TEXT_INPUT]);

				[checkBlock, ...nameBlock, ...domainBlock, hr].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			},

			createInheritedVariables: function() {
				return CompBuilder.generateInheritedVarsSection(that.getInheritedVariables());
			},

			createRunMode: function() {
				var container = document.createElement("div"),
					title = document.createElement("h5"),
					currentStateText = document.createElement("div"),
					labelHistory = CompBuilder.generateCollapseLabel("History", "historyList"),
					historyDiv = document.createElement("div"),
					emptyDiv = document.createElement("div"),
					labelCurrentStateAttribute = CompBuilder.generateCollapseLabel("Attributes", "attributeList"),
					attributeListDiv = document.createElement("div"),
					attributeList = [];

				title.textContent = "Current State";

				var currentStateString = "",
					i;
				for (i = 0; i < window.project.currentStateJsonObjList.length; i++) {
					if (window.project.currentStateJsonObjList[i].current_sub_state.type === "Elem") {
						currentStateString = currentStateString.concat(window.project.currentStateJsonObjList[i].current_state_name);

						if (i + 1 < window.project.currentStateJsonObjList.length) {
							currentStateString = currentStateString.concat(", ");
						}
					}
				}
				currentStateText.textContent = "Current State: " + currentStateString;

				historyDiv.className = "collapse";
				historyDiv.id = "historyList";

				if (window.project.lastTransList.length === 0) {
					historyDiv.textContent = "No transition yet.";
				} else {
					var historyString = "";
					for (i = 0; i < window.project.lastTransList.length; i++) {
						historyString = historyString.concat(window.project.lastTransList[i]);

						if (i + 1 < window.project.lastTransList.length) {
							historyString = historyString.concat(", ");
						}
					}
				}
				historyDiv.textContent = historyString;

				attributeListDiv.className = "collapse";
				attributeListDiv.id = "attributeList";

				for (i = 0; i < that.attributes.length; i++) {
					attributeList.push(document.createElement("div"));
					attributeList[i].textContent = that.attributes[i].label + " = " + that.attributes[i].value;
				}

				attributeList.forEach((attrib) => {
					attributeListDiv.appendChild(attrib);
				});

				[title, currentStateText, labelHistory, historyDiv, emptyDiv, labelCurrentStateAttribute, attributeListDiv].forEach((component) => {
					container.appendChild(component);
				});

				var showQvar = false;
				if (that.parent.tabs.length > 1) {
					for (i = 0; i < that.parent.tabs.length; i++) {
						if (that.parent.tabs[i] instanceof QTab && !(that instanceof QTab)) {
							showQvar = true;
						}
					}
				}

				if (showQvar) {
					var qvarDiv = document.createElement("div");
					qvarDiv.textContent = "Quantified Variable";
					var qvar = [],
						showQVarDiv = document.createElement("div");

					window.project.qvarValuePerName.forEach(function(value, key) {
						var temp = document.createElement("div");
						temp.textContent = key + " = " + value + "\n";
						qvar.push(temp);
					});
					qvar.forEach((attrib) => {
						showQVarDiv.appendChild(attrib);
					});
					[qvarDiv, showQVarDiv].forEach((component) => {
						container.appendChild(component);
					});
				}

				return container;
			},
			createInvariant: function() {
				var container = document.createElement("div"),
					comBlock = CompBuilder.generateTextBlock("textarea", "Invariant:", "", that.invariant, "'Ex: Invariant definition'"),
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

	/**
	 * This is a special case of getInheritedVariables, the function getInheritedVariableForParentAtIndex is called before to make sure
	 * we get inherited variable which are from the left tab of this astd and then we concat them with all the others inherited ones.
	 * @returns {*[]}
	 */
	getInheritedVariables() {
		var variables = this.parent.getFile().getInheritedVariableForParentAtIndex(this.parent, this.parent.tabs.indexOf(this));
		return this.parent.getFile().getInheritedVariables(this.parent.parent).concat(variables.reverse());
	}

	/**
	 *  Generate a default panel for the regular types
	 * @param extraComponents(Array:function): all extra components func to add to the panel other than the common ones. Always added at the end of the panel
	 * @return(function): the executing function to be called in the toPanel() function
	 */
	defaultPanel(extraComponents = [], extraComponentRunMode = null) {
		var that = this,
			components = [this.tabPanel.createInheritedVariables, this.tabPanel.createAttributes, this.tabPanel.createCode];
		if (window.project.isInPlayMode) {
			extraComponents.push(this.tabPanel.createRunMode);
			if (extraComponentRunMode !== null) {
				extraComponents.push(extraComponentRunMode);
			}
		}

		return function() {
			var container = document.createElement("div");

			if (that.parent.tabs[0] !== that) {
				container.appendChild(that.tabPanel.createHeader());
				container.appendChild(that.tabPanel.createInvariant());
			}
			components.concat(extraComponents).forEach((componentFunc) => {
				container.appendChild(componentFunc());
			});

			return container;
		};
	}

	/**
	 * Generate a default panel for all the binary types. It generates default components, any extra component and the test section at the end
	 * @param extraComponents: all extra components func to add to the panel other than the common ones. Always added at the end of the panel
	 * @returns {function(): HTMLDivElement}: the executing function to be called in the toPanel() function
	 */
	defaultBinaryPanel(extraComponents = [], extraComponentsRunMode = null) {
		return this.defaultPanel(extraComponents.concat([this.tabPanel.createTestSection]), extraComponentsRunMode);
	}

	toPanel() {
		return this.defaultPanel()();
	}

	save() {

		var tab = {
			id: this.id,
			label: this.label,
			parent: this.parent.id,
			attributes: [],
			code: this.code,
			interruptCode: this.interruptCode,
			test: this.test,
			parameter_name: this.parameter_name,
			parameter_domain: this.parameter_domain,
			invariant: this.invariant
		};

		this.attributes.forEach(function(attribute) {
			tab.attributes.push(attribute.save());
		});

		return tab;
	}

	load(tab) {
		var i;

		for (i = 0; i < tab.attributes.length; i++) {
			var attribute = new Attribute(this);
			attribute.load(tab.attributes[i]);
			this.attributes.push(attribute);
		}
		this.label = tab.label;
		this.code = tab.code;
		this.interruptCode = tab.interruptCode;
		this.test = tab.test;
		this.parameter_name = tab.parameter_name;
		this.parameter_domain = tab.parameter_domain;
		this.invariant = tab.invariant;
		this.validateLabel();
	}

	export() {
		return this.defaultExport();
	}

	defaultExport() {
		return {
			attributes: this.attributes.map((attr) => {
				return attr.export();
			}),
			code: this.code,
			interruptCode: this.interruptCode
		};
	}

	// eslint-disable-next-line no-unused-vars
	binaryExport(min_domain, max_domain, parameter_name, test_parameter, iD, index, external_parameter_name, test = false, nary = false, operationIndex = 0, operations_maxLength = 0, operationID = null) {
		let serialize = this.defaultExport();
		if (min_domain !== null && max_domain !== null) {
			if (min_domain + 1 === max_domain) {
				serialize.left_astd = this.parent.items[0].texport(0, test_parameter.concat(min_domain), null, null, parameter_name, null, external_parameter_name, test, nary, 0, operations_maxLength, null);
				serialize.right_astd = this.parent.items[0].texport(0, test_parameter.concat(max_domain), null, null, parameter_name, null, external_parameter_name, test, nary, 0, operations_maxLength, null);
			} else {
				serialize.left_astd = this.parent.items[0].texport(0, test_parameter.concat(min_domain), null, null, parameter_name, null, external_parameter_name, test, nary, 0, operations_maxLength, null);
				serialize.right_astd = this.parent.texport(index, test_parameter, min_domain + 1, max_domain, parameter_name, iD + 1, external_parameter_name, test, nary, 0, operations_maxLength, null);
			}
		} else {
			serialize.left_astd = this.parent.items[operationIndex].texport(0, test_parameter, null, null, parameter_name, null, external_parameter_name, test, nary, 0, operations_maxLength, null);
			serialize.right_astd = this.parent.items[operationIndex + 1].texport(0, test_parameter, null, null, parameter_name, null, external_parameter_name, test, nary, 0, operations_maxLength, null);
		}
		return serialize;
	}

	// eslint-disable-next-line no-unused-vars
	naryExport(min_domain, max_domain, parameter_name, test_parameter, iD, index, external_parameter_name, test = false, nary = false, operationIndex = 0, operations_maxLength = 0, operationID = null) {
		let serialize = this.defaultExport();
		if (operationIndex + 2 === operations_maxLength) {
			serialize.left_astd = this.parent.items[operationIndex].texport(0, test_parameter, null, null, parameter_name, null, external_parameter_name, test, nary, 0, operations_maxLength, null);
			serialize.right_astd = this.parent.items[operationIndex + 1].texport(0, test_parameter, null, null, parameter_name, null, external_parameter_name, test, nary, 0, operations_maxLength, null);
		} else {
			serialize.left_astd = this.parent.items[operationIndex].texport(0, test_parameter, null, null, parameter_name, null, external_parameter_name, test, nary, 0, operations_maxLength, null);
			serialize.right_astd = this.parent.texport(0, test_parameter, null, null, parameter_name, null, external_parameter_name, test, nary, operationIndex + 1, operations_maxLength, operationID + 1);
		}

		return serialize;
	}

	// eslint-disable-next-line no-unused-vars,no-empty-function
	copy(copiedParent, tab) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedTab(copiedParent, tab.attributes, tab.code, tab.interruptCode, tab.label);
	}

	// eslint-disable-next-line no-empty-function
	paste() {
	}
}

class CopiedTab {
	constructor(parent = null, attributes = [], code = "", interruptCode = "", label = "") {
		this.parent = parent;
		this.attributes = attributes;
		this.code = code;
		this.interruptCode = interruptCode;
		this.label = label;
	}

	paste(parent) {
		let tab = new Tab(window.id.get(), parent, this.attributes, this.code, this.interruptCode, this.label);
		tab.rightClickable();
		tab.load(parent);
		return tab;
	}
}