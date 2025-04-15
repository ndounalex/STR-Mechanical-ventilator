class File {

	constructor(parent = null, astd = null, links = []) {
		this.e_file = this.createFile();
		this.e_playground = this.createPlayground();

		this.plumb = jsPlumb.getInstance({ Container: this.e_playground });

		this.parent = parent;
		this.links = links;
		this.astd = astd;
		this.allTestParameters = [];
		this.nbr = 0;
	}

	createFile() {
		let that = this,
			e_file = document.createElement("li");
		e_file.innerHTML = "Unnamed";
		e_file.className = "list-group-item";
		e_file.id = window.id.get();
		e_file.onclick = function() {
			if (!that.e_file.classList.contains("active")) {
				project.selectFile(that);
				for (let i = 0; i < that.astd.tabs.length; i++) {
					that.astd.tabs[i].e_tab.style.backgroundColor = i === 0 ? "white" : "rgb(207,211,218)";
				}
				that.astd.refreshTabs();
			}
		};

		$.contextMenu({
			selector: `#${e_file.id}`,
			build: this.buildContextMenu.bind(this)
		});

		return e_file;
	}

	/**
	 * Function defining the items of the context menu for the top-level astds (on the bottom menu) and the actions to execute for each selection
	 * @returns object: options to set on the contextMenu once it is built
	 */
	buildContextMenu() {
		var that = this;
		return {
			zIndex: 100,
			events: {
				show: function() {
					that.e_file.click();
				}
			},
			items: {
				"clear": {
					name: "Clear",
					icon: "fas fa-eraser",
					callback: function() {
						that.astd.delete();
						that.astd = new ASTD(window.id.get(), that, 10, 2, 910, 670, that.e_file.textContent);
						window.panel.show(that.astd);
					}
				},
				"print": {
					name: "Print",
					icon: "fas fa-print",
					callback: function() {
						var WinPrint = window.open("", "print");

						$(that.e_playground).toggleClass("print");
						html2canvas(that.e_playground, {
							foreignObjectRendering: true,
							x: 0,
							y: 0,
							width: that.astd.e_item.offsetWidth + 10,
							height: that.astd.e_item.offsetHeight + 10
						}).then((canvas) => {
							$(that.e_playground).toggleClass("print");
							// set the canvas to fit the width of the new window
							$(canvas).width(WinPrint.innerWidth - 30);
							$(canvas).height((WinPrint.innerWidth - 30) / (canvas.width / canvas.height));
							WinPrint.focus();
							WinPrint.document.body.appendChild(canvas);
							WinPrint.document.body.appendChild(that.print());
							WinPrint.print();
							WinPrint.close();
						});
					}
				},
				"deleteFile": {
					name: "Delete",
					icon: "far fa-trash-alt",
					callback: function() {
						if (that.e_file.classList.contains("active") && project.files.includes(that)) {
							const index = project.files.indexOf(that);
							if (index > 0) {
								project.selectFile(project.files[index - 1]);
							} else if (project.files.length > 1) {
								project.selectFile(project.files[index + 1]);
							}
							that.delete(false);
						}
					}
				}
			}
		};
	}

	createPlayground() {
		var e_playground = document.createElement("div");
		e_playground.className = "file";
		return e_playground;
	}

	get parent() {
		return this.m_parent;
	}

	set parent(parent) {
		if (parent) {
			parent.addFile(this);
		}

		this.m_parent = parent;
	}

	get astd() {
		return this.m_astd;
	}

	set astd(astd) {
		if (!astd) {
			return;
		}

		astd.resizable();
		astd.clickable();
		astd.dblClickable();
		astd.buildContextMenu();

		this.m_astd = astd;
	}

	get links() {
		return this.m_links;
	}

	set links(links) {
		this.m_links = links;
	}

	refreshLabel() {
		if (this.astd) {
			this.e_file.innerHTML = this.astd.label;
		}
	}

	/**
	 * Gets the inherited variables, attributes, parameters for a given astd at a specific tab index.
	 * The index is needed since some tabs represent unary component and hierarchically inherit from their left tab.
	 * Most cases are gonna call idx as the far right tab but some cases needed to call a specific idx.
	 *
	 * @param astdParent: represent the astd item in which the component is in.
	 * @param idx: index to start iteration on the astdParent (last tab index to consider)
	 * @returns {[]}
	 */
	getInheritedVariableForParentAtIndex(astdParent, idx) {
		var variables = [];
		for (var i = idx; i >= 0; i--) {
			// Get variables
			if (astdParent.tabs[i].variable) {
				variables.push(astdParent.tabs[i].variable);
			}
			// Get Attributes
			astdParent.tabs[i].attributes.forEach((attr) => {
				variables.push(attr);
			});
		}
		// Get Parameters
		astdParent.parameters.forEach((param) => {
			variables.push(param);
		});

		return variables;
	}

	/**
	 * loops through all items until it reaches the parent file and append all inherited variables found in each of iterated parent together
	 * @param par is the parent of the component which we want to get inherited variables from
	 * @returns {*[]} all the inherited variables with the top level astd inherited variables first
	 */
	getInheritedVariables(par) {
		var variables = [];
		while (par !== this) {
			variables = variables.concat(this.getInheritedVariableForParentAtIndex(par, par.tabs.length - 1));
			par = par.parent;
		}
		return variables.reverse();
	}

	getAllLinksFrom(source) {
		return this.getAllLinksTrimCall(source, []);
	}

	getAllLinksTrimCall(source, calledList) {
		var linkTotal = this.getLinks(source);

		if (source instanceof ASTD) {
			for (let idxTab = 0; idxTab < source.tabs.length; idxTab++) {
				if (source.tabs[idxTab] instanceof Call) {
					let callAstd = source.tabs[idxTab].astd;
					if (!calledList.includes(callAstd)) {
						calledList.push(callAstd);
						linkTotal = linkTotal.concat(callAstd.parent.getFile().getAllLinksTrimCall(callAstd, calledList));
					}
				}
			}
			for (var i = 0; i < source.items.length; i++) {
				linkTotal = linkTotal.concat(this.getAllLinksTrimCall(source.items[i], calledList));
			}
		}

		return linkTotal;
	}

	getLinks(source) {
		return this.m_links.filter((link) => {
			return link.source === source;
		});
	}

	getDirectLinksOf(astd) {
		return this.m_links.filter((link) => {
			return link.source.parent === astd;
		});
	}

	getFile() {
		return this;
	}

	addItem(item) {
		if (item && window.project.isInPlayMode === false) {
			this.e_playground.appendChild(item.e_item);
		}
	}

	removeItem(item) {
		if (item) {
			this.e_playground.removeChild(item.e_item);
			item.delete();
			this.parent.removeFile(this);
		}
	}

	deleteItem(item) {
		if (confirm("Are you sure to want to delete this object?")) {
			if (this.astd === item) {
				this.astd = null;
				item.delete();
				window.project.removeFile(this);
			}
		}
	}

	deleteItem_forLoading(item) {
		if (this.astd === item) {
			this.astd = null;
			item.delete();
			window.project.removeFile(this);
		}
	}

	select() {
		this.e_file.classList.add("active");
		this.e_playground.classList.add("selected");
		if (this.astd) {
			window.panel.show(this.astd);
		}
	}

	unselect() {
		this.e_file.classList.remove("active");
		this.e_playground.classList.remove("selected");
	}

	addLink(link) {
		this.m_links.push(link);
	}

	removeLink(id) {
		for (var i = 0; i < this.links.length; i++) {
			if (this.m_links[i].id === id) {
				this.m_links[i].delete();
				this.m_links.splice(i, 1);
				break;
			}
		}
	}

	removeLinkSequence() {
		this.m_links.forEach((link) => {
			if (link.type === SEQUENCE) {
				this.removeLink(link.id);
			}
		});
	}

	removeLinkTimed() {
		this.m_links.forEach((link) => {
			// eslint-disable-next-line no-undef
			if (link.type === TIMEOUT) {
				this.removeLink(link.id);
			}
		});
	}

	containsLink(source, destination) {
		this.links.forEach((link) => {
			if (link.source === source && link.destination === destination) {
				return true;
			}
		});
		return false;
	}

	delete(forLoading = false) {
		if (forLoading) {
			this.deleteItem_forLoading(this.astd);
		}
		else {
			this.deleteItem(this.astd);
		}
	}

	save() {
		var file = {
			astd: this.astd.save(),
			links: []
		};

		this.links.forEach(function(link) {
			file.links.push(link.save());
		});

		return file;
	}

	load(file) {
		this.astd = new ASTD(file.astd.id, this);
		this.astd.load(file.astd);
		for (var i = 0; i < file.links.length; i++) {
			var link = new Link(file.links[i].id, this.astd.getItem(file.links[i].source), this.astd.getItem(file.links[i].destination));
			link.load(file.links[i]);
			this.addLink(link);
		}
	}

	verify() {
		this.astd.verify();
		this.links.forEach(function(link) {
			link.verify();
		});
	}

	export() {
		return this.astd.export("");
	}

	print() {
		var toPrint = this.astd.print();
		// Remove all elements that should not appear in print result
		toPrint.querySelectorAll(".no-print").forEach((elem) => {
			elem.remove();
		});
		// Remove placeholders to leave input fields blank if empty
		toPrint.querySelectorAll("input[placeholder], textarea[placeholder]").forEach((elem) => {
			elem.removeAttribute("placeholder");
		});
		return toPrint;
	}
}