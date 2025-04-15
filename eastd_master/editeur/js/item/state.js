const { statSync } = require("original-fs");

class State extends Item {

	constructor(id = window.id.get(), parent = null, x = 0, y = 0, width = 30, height = 30, label = "S", initial = false, final = false, history = false, deephistory = false) {
		super(id, parent, x, y, width, height, label, initial);

		this.final = final;
		this.history = history;
		this.deephistory = deephistory;
	}

	createItem() {
		var e_item = super.createItem();

		e_item.className += " state";

		var initial = document.createElement("div");
		initial.className = "initial";

		e_item.appendChild(initial);

		var content = document.createElement("div");
		content.className = "content";

		if (this.final) {
			content.className += " final";
		}

		var label = document.createElement("p");
		label.className = "label";
		label.innerHTML = this.label;

		content.appendChild(label);

		e_item.appendChild(initial);
		e_item.appendChild(content);

		return e_item;
	}

	get final() {
		return this.m_final;
	}

	set final(final) {
		this.m_final = final;

		if (final) {
			this.e_item.classList.add("final");
		} else {
			this.e_item.classList.remove("final");
		}
	}

	get history() {
		return this.m_history;
	}

	set history(history) {
		this.m_history = history;
	}

	get label() {
		return super.label;
	}

	set label(label) {
		var width = label.length * 5.76 + 19.2;

		if (width < 30) {
			width = 30;
		}

		this.width = width;

		this.e_item.getElementsByClassName("label")[0].innerHTML = label;

		this.m_label = label;
	}

	get deephistory() {
		return this.m_deephistory;
	}

	set deephistory(deephistory) {
		this.m_deephistory = deephistory;
	}

	get height() {
		return super.height;
	}

	set height(height) {
		super.height = height;

		this.e_item.getElementsByClassName("label")[0].style.marginTop = height / 2 - 25 + "px";
	}

	get test_parameters() {
		return this.parent.test_parameters;
	}

	set test_parameters(test_parameters) {
		this.parent.test_parameters = test_parameters;
	}

	// Function to run initial checkbox click
	runOnInitial() {
		this.initial = event.target.checked;
	}

	// Function to run final checkbox click
	runOnFinale() {
		this.final = event.target.checked;
	}

	// Function to run history checkbox click
	runOnHistory() {
		var isConfirm = true;
		if ((this.entryCode !== "" || this.stayCode !== "" || this.exitCode !== "") && event.target.checked) {
			isConfirm = confirm("You will lose any state code. Are you sure you want to change the state to history?");
		}

		if (event.target.checked && isConfirm) {
			this.history = event.target.checked;
			this.entryCode = this.stayCode = this.exitCode = "";
			this.final = false;
			this.initial = false;
			this.label = "H";
			this.label_text_error = "";
			window.panel.show();
		} else if (!event.target.checked) {
			this.history = event.target.checked;
			this.deephistory = false;
			this.label = "S";
			window.panel.show();
		} else {
			event.target.checked = this.history;
		}
	}

	// Function to run deephistory checkbox click
	runOnDeepHistory() {
		this.deephistory = event.target.checked;
		if (this.deephistory) {
			$(".itemNameInput").val("H*");
			this.label = "H*";
		} else if (this.history) {
			$(".itemNameInput").val("H");
			this.label = "H";
		}
	}

	/**
	 * Calling getPanel will return a handle which give you access to checkBox component individually
	 */
	get statePanel() {
		var that = this;

		/**
		 * Utility function to areate a checkbox controlled by history checkbox selection and bind the class attribute / event corresponding to it
		 * @param name of the checkbox label ( what will be display )
		 * @param comp class attribute linked to this checkbox
		 * @param func function to run on event ( click )
		 * @param isHidden: defines if the component is initially hidden or not. By default, it is always displayed.
		 * @returns Html Container with the checkbox linked to the attribute to be displayed
		 */
		function createCheckBox(name, comp, func, isHidden = false) {
			var container = CompBuilder.generateCheckboxBlock(name, comp);
			container.className += isHidden ? " d-none" : "";

			if (that.history) {
				container.classList.toggle("d-none");
			}

			container.children[CHECKBOX].id = name.replace(" ", "") + "_input";
			container.children[CHECKBOX].onclick = func.bind(that);

			return container;
		}

		return {
			createStateStatus: function() {
				var container = document.createElement("div"),
					initial = createCheckBox("Initial", that.initial, that.runOnInitial),
					final = createCheckBox("Final", that.final, that.runOnFinale),
					history = CompBuilder.generateCheckboxBlock("History", that.history),
					deepHistory = createCheckBox("Deep history", that.deephistory, that.runOnDeepHistory, true),
					hr = document.createElement("hr");

				history.children[CHECKBOX].onclick = that.runOnHistory.bind(that);

				[initial, final, history, deepHistory, hr].forEach((component) => {
					container.appendChild(component);
				});
				return container;
			}
		};
	}

	/**
	 * @returns the container to display in the panel
	 */
	toPanel() {
		return (function(stateModel) {
			var container = document.createElement("div");

			[
				stateModel.itemPanel.createHeader("State"),
				stateModel.statePanel.createStateStatus(),
				stateModel.itemPanel.createStateCode(),
				stateModel.itemPanel.createCom(),
				stateModel.itemPanel.createInvariant()
			].forEach((component) => {
				container.appendChild(component);
			});
			return container;
		})(this);
	}

	save() {
		var state = super.save();
		state.class = "state";
		state.final = this.final;
		state.history = this.history;
		state.initial = this.initial;
		state.deephistory = this.deephistory;
		return state;
	}

	load(state) {
		super.load(state);
		this.final = state.final;
		this.history = state.history;
		this.deephistory = state.deephistory;
		this.width = state.width;
	}

	export() {
		return {
			name: this.label,
			astd: {
				type: "Elem",
				typed_astd: {}
			},
			entry_code: this.entryCode,
			stay_code: this.stayCode,
			exit_code: this.exitCode,
			invariant: this.invariant
		};
	}

	print() {
		return this.toPanel();
	}

	// eslint-disable-next-line no-unused-vars
	copy_paste_action(action, object, event) {
		if (window.copyPaste_label === "Copy") {
			// eslint-disable-next-line no-use-before-define
			let copyItem = new CopiedState(this.id, this.parent, this.position.x, this.position.y, this.width, this.height, this.label, this.initial, this.final, this.history, this.deephistory);
			window.copiedItem = copyItem;
			window.copyPaste_label = "Paste";
		} else if (window.copyPaste_label === "Paste") {
			alert("Can't copy item in state");
			// window.copyPaste_label = "Copy";
			// window.copiedItem.paste({
			// 	x: event.clientX,
			// 	y: event.clientY
			// }, this);
		}
	}

	// eslint-disable-next-line no-unused-vars
	copy(copiedParent, item, links = []) {
		// eslint-disable-next-line no-use-before-define
		return new CopiedState(item.id, copiedParent, item.position.x, item.position.y, item.width, item.height, item.label, item.initial, item.final, item.history, item.deephistory);
	}

	// eslint-disable-next-line no-empty-function
	paste() {}
}

// eslint-disable-next-line no-undef
class CopiedState extends CopiedItem {
	constructor(id, parent = null, x = 0, y = 0, width = 30, height = 30, label = "S", initial = false, final = false, history = false, deephistory = false) {
		super(id, parent, x, y, width, height, label, initial);

		this.final = final;
		this.history = history;
		this.deephistory = deephistory;
	}

	paste(position, parentItem) {
		let state = new State(window.id.get(), parentItem, position.x, position.y, this.width, this.height, this.label, this.initial, this.final, this.history, this.deephistory);

		state.draggable();
		state.resizable();
		state.clickable();
		state.buildContextMenu();
		state.fixOverlap();
		window.panel.selectItem(state);
		return state;
	}
}