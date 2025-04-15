/**
 * This is a flexion point,
 * It's a basic component that inherits from item so it can be added to board
 * drag and drop in the board and easily manipulated
 */
class FlexPoint extends Item {

	constructor(id = window.id.get(), parent = null, source = null, destination = null, x = 0, y = 0, that = null) {
		super(id, parent, x, y, 10, 10);
		this.draggable();
		this.clickable();
		this.buildContextMenu();
		this.source = source;
		this.target = destination;
		this.transitionView = that;
		that.plumbConnect(this, destination);
		that.plumbConnect(source, this);
	}

	/**
	 * Function building a jquery contextmenu for the flexPoint. It overrides the function from item class because the action is different
	 */
	buildContextMenu() {
		var that = this;
		$.contextMenu({
			selector: `#${this.id}`,
			build: function() {
				return {
					zIndex: 100,
					items: {
						deleteElem: {
							name: "Delete point",
							icon: "far fa-trash-alt",
							callback: that.remove.bind(that)
						}
					}
				};
			}
		});
	}

	createItem() {
		var e_item = super.createItem(),
			initial = document.createElement("div"),
			content = document.createElement("div");
		e_item.className += " state";
		initial.className = "initial";
		content.className = "content";

		e_item.appendChild(initial);
		e_item.appendChild(content);
		return e_item;
	}

	remove() {
		this.target.source = this.source;
		this.source.target = this.target;
		this.transitionView.instance.deleteConnectionsForElement(this.e_item);
		this.transitionView.plumbConnect(this.source, this.target, true);
		this.transitionView.removePoint(this);
		this.parent.deleteItem(this);
	}



	delete() {
		// manually remove contextMenu so it is built again even if we load a file with the same e_item.id value
		$.contextMenu("destroy", `#${this.id}`);
		this.e_item.remove();
	}

	toPanel() {
		return this.transitionView.model.toPanel();
	}
}

