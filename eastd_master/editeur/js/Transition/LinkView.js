const CUSTOM_OVERLAY = ["Custom", {
		create: function() {
			return $("<div></div>");
		},
		location: 0.5,
		id: "customOverlay"
	}],

	OVERLAYS_DEFAULT = [
		["Arrow",
			{
				width: 10,
				length: 10,
				location: 1,
				id: "default"
			}
		],
		["Arrow",
			{
				width: 10,
				length: 10,
				location: 1,
				id: "step"
			}
		],
		[
			"PlainArrow",
			{
				width: 30,
				length: 30,
				location: 1,
				id: SEQUENCE
			}
		],
		[
			"Arrow",
			{
				width: 30,
				length: 30,
				location: 1,
				paintStyle: {
					fillStyle: "#020aed",
					strokeStyle: "#020aed"
				},
				id: TIMEOUT
			},
			["Label", {
				label: "foo",
				location: 0.25,
				id: "myLabel"
			}]
		],
		CUSTOM_OVERLAY
	],

	// COLOR
	SEQUENCE_COLOR = "#000000",
	TIMEOUT_COLOR = "#020aed";

// Final Endpoint
FINAL_TYPE_ENDPOINT = {
	fill: SEQUENCE_COLOR,
	stroke: SEQUENCE_COLOR,
	radius: 5
},

	TIMEOUT_TYPE_ENDPOINT = {
		fill: TIMEOUT_COLOR,
		stroke: TIMEOUT_COLOR,
		radius: 5
	};

// STRING CONST
DEFAULT = "default",
	FINAL = "final",
	STEP = "step";

let connectionTypes = {
	"illumined-connection": {
		paintStyle: {
			stroke: "#E8565666",
			strokeWidth: 5
		},
		hoverPaintStyle: {
			stroke: "#666666",
			strokeWidth: 2
		}
	},
	"normal-connection": {
		paintStyle: {
			stroke: "#666666",
			strokeWidth: 2
		},
		hoverPaintStyle: {
			stroke: "#E8565666",
			strokeWidth: 5
		}
	}
};

/**
 * This class serves to a transition, all the UI related logic on the transition component
 * should be added here.
 * see flexPoint.js
 */
class LinkView {
	constructor(transition, source, target) {
		this.model = transition;
		this.source = source;
		this.target = target;
		this.instance = source.getFile().plumb; // !Important

		this.jsPlumbConnection = this.plumbConnect(source, target);
		this.labelConnection = this.jsPlumbConnection;
		this.e_item = this.generateLabel();
		this.buildContextMenu();
		source.getFile().addItem(this);

		// Variable for flex point
		this.flexPoints = [];
		this.sourceConnection = this.jsPlumbConnection; // Reference the connection which target the source for arrow appearance
		this.targetConnection = this.jsPlumbConnection; // Reference the connection which target the destination for arrow appearance

		// Variable for drag n drop Label
		this.moveTarget = this.target; //Source Component of plumb connection where label is held
		this.moveSource = this.source; //Target Component of plumb connection where label is held
		this.attachToComponents();
		this.fix = true;
	}

	/**
	 * =====================================================================
	 * 					Debut code pour le Label
	 * =====================================================================
	 */

	get fix() {
		return this.m_fix;
	}

	set fix(fix) {
		this.m_fix = fix;
		this.notify();
	}

	// Attach the linkview to source, target and parents for dragging and resizing notification
	attachToComponents() {
		this.moveSource.attach(this);
		this.moveTarget.attach(this);

		var tempParent = this.moveSource.parent;
		while (!(tempParent instanceof File)) {
			tempParent.attach(this);
			tempParent = tempParent.parent;
		}

		tempParent = this.moveTarget.parent;
		while (!(tempParent instanceof File)) {
			tempParent.attach(this);
			tempParent = tempParent.parent;
		}
	}

	// detach the linkview to source, target and parents to stop being notified for dragging and resizing
	detachFromComponents() {
		this.moveTarget.detach(this);
		this.moveSource.detach(this);

		var tempParent = this.moveSource.parent;
		while (!(tempParent instanceof File)) {
			tempParent.detach(this);
			tempParent = tempParent.parent;
		}

		tempParent = this.moveTarget.parent;
		while (!(tempParent instanceof File)) {
			tempParent.detach(this);
			tempParent = tempParent.parent;
		}

	}

	// Source and destination which label listen to drag
	setSourceLabel(moveSource, moveTarget) {
		if (this.fix) {
			this.detachFromComponents();
			this.moveSource = moveSource;
			this.moveTarget = moveTarget;
			this.attachToComponents();
			this.setLabelMiddlePosition();
		}
	}

	// Set label in the middle of a connection according to the set source and destination
	setLabelMiddlePosition() {
		var connectionCenter = this.labelConnection.getOverlay("customOverlay").getElement();

		this.e_item.style.left = connectionCenter.offsetLeft - 10 + "px"; // this.e_item.offsetWidth / 2 + "px";
		this.e_item.style.top = connectionCenter.offsetTop - 10 + "px"; //this.e_item.offsetHeight / 2 + "px";
	}

	// Action to do when dragging, resizing components we are listening to
	notify() {
		if (this.fix)
			this.setLabelMiddlePosition();
	}

	// Generate a label when the linkView constructor is called and attach drag events
	generateLabel() {
		var container = document.createElement("div"),
			compactLabel = document.createElement("div"),
			compactParams = document.createElement("div"),
			fullLabel = document.createElement("div"),
			fullParams = document.createElement("div");

		container.id = "labelComp_" + this.model.id;
		container.className = "shadowLabel";
		container.style.position = "absolute";

		compactLabel.id = "event";
		compactParams.id = "event";

		fullLabel.id = "parameters";
		fullParams.id = "parameters";

		[compactLabel, compactParams, fullLabel, fullParams].forEach((component) => {
			container.appendChild(component);
		});

		this.dragElement(container);
		container.onclick = this.focusPanel.bind(this);
		container.onmouseenter = this.toggleContainer.bind(container);
		container.onmouseleave = this.toggleContainer.bind(container);
		return container;
	}

	toggleContainer() {
		this.classList.toggle("expanded");
	}

	dragElement(elmnt) {
		var pos1 = 0,
			pos2 = 0,
			pos3 = 0,
			pos4 = 0,
			hasBeenDragged = false,
			that = this;

		function elementDrag(e) {
			e.preventDefault();
			// calculate the new cursor position:
			pos1 = pos3 - e.clientX;
			pos2 = pos4 - e.clientY;
			pos3 = e.clientX;
			pos4 = e.clientY;
			elmnt.style.top = elmnt.offsetTop - pos2 + "px";
			elmnt.style.left = elmnt.offsetLeft - pos1 + "px";
			hasBeenDragged = true;
		}

		function closeDragElement() {
			if (that.fix) {
				if (hasBeenDragged) {
					// Find the nearest connector of the current label position
					var nearestConnector = that.sourceConnection,
						currentOverlay = nearestConnector.getOverlay("customOverlay").getElement(),
						distance = Math.hypot(currentOverlay.offsetLeft - this.offsetLeft, currentOverlay.offsetTop - this.offsetTop);

					let connections = that.instance.getConnections({ scope: that.model.id });
					connections.forEach((currentConnection) => {
						currentOverlay = currentConnection.getOverlay("customOverlay").getElement();
						const currentDistance = Math.hypot(currentOverlay.offsetLeft - this.offsetLeft, currentOverlay.offsetTop - this.offsetTop);
						if (currentDistance < distance) {
							nearestConnector = currentConnection;
							distance = currentDistance;
						}
					});

					// Trigger event to clip the label to the nearest connector
					nearestConnector.fire("clip", nearestConnector);
				}
			}
			hasBeenDragged = false;
			document.onmousemove = null;
			elmnt.onmouseup = null;
		}

		function dragMouseDown(e) {
			// activate dragging only if left button is pressed
			if (e.button === 0) {
				e.preventDefault();
				// get the mouse cursor position at startup:
				pos3 = e.clientX;
				pos4 = e.clientY;
				document.onmousemove = elementDrag;
				elmnt.onmouseup = closeDragElement;
			}
		}

		elmnt.onmousedown = dragMouseDown;
	}

	refreshLabel(compactLabel, compactParams, fullLabel, fullParams) {
		if (this.e_item.children.length === 4) {
			this.e_item.children[0].innerHTML = compactLabel;
			this.e_item.children[1].innerHTML = compactParams;
			this.e_item.children[2].innerHTML = fullLabel;
			this.e_item.children[3].innerHTML = fullParams;
		}
		if (this.fix)
			this.setLabelMiddlePosition();
	}

	/**
	 * =====================================================================
	 * 					Fin code pour le Label
	 * =====================================================================
	 */

	/**
	 * Function building a jquery contextmenu for the linkView. The selectors are the label and any connector associated to this linkview.
	 */
	buildContextMenu() {
		var that = this;
		$.contextMenu({
			selector: `#${this.e_item.id}, .${this.model.id}`,
			build: function() {
				return {
					zIndex: 100,
					events: {
						show: function() {
							window.panel.show(that.model);
						}
					},
					items: {
						"delete": {
							name: "Delete",
							icon: "far fa-trash-alt",
							callback: function() {
								that.model.source.parent.getFile().removeLink(that.model.id);
								window.panel.show(null);
							}
						}
					}
				};
			}
		});
	}

	get jsPlumbConnection() {
		return this.m_jsPlumbConnection;
	}

	set jsPlumbConnection(jsPlumbConnection) {
		this.m_jsPlumbConnection = jsPlumbConnection;
		this.instance.repaintEverything();
	}

	// Update both endpoint and overlay
	updateConnection() {
		this.updateConnectionEndpoint();
		this.updateConnectionOverlay();
	}

	// Update connection endpoint according to type
	updateConnectionEndpoint() {
		if (this.model.type === DEFAULT || this.model.type === STEP) {
			this.sourceConnection.endpoints[0].setPaintStyle({});
			if (this.model.final) {
				this.sourceConnection.endpoints[0].setPaintStyle(FINAL_TYPE_ENDPOINT);
			}
		} else if (this.model.type === SEQUENCE && this.source instanceof ASTD && this.target instanceof ASTD) {
			if (this.source !== this.target) {
				this.sourceConnection.endpoints[0].setPaintStyle(FINAL_TYPE_ENDPOINT);
				this.targetConnection.setPaintStyle(FINAL_TYPE_ENDPOINT);
			}
		} else if (this.model.type === TIMEOUT && this.source instanceof ASTD && this.target instanceof ASTD) {
			if (this.source !== this.target) {
				//this.sourceConnection.endpoints[0].setPaintStyle(TIMEOUT_TYPE_ENDPOINT);
				this.targetConnection.setPaintStyle(TIMEOUT_TYPE_ENDPOINT);
			}
		}
	}

	// Update connection overlay according to type
	updateConnectionOverlay() {
		this.targetConnection.hideOverlay(DEFAULT);
		this.targetConnection.hideOverlay(STEP);
		this.targetConnection.hideOverlay(SEQUENCE);
		this.targetConnection.hideOverlay(TIMEOUT);

		if (this.model.type === DEFAULT) {
			this.targetConnection.showOverlay(DEFAULT);
			$(this.e_item).show();
		} else if (this.model.step) {
			this.targetConnection.showOverlay(STEP);
		}/* else if (this.model.final) {
			this.targetConnection.showOverlay( DEFAULT );
		}*/ else if (this.model.type === SEQUENCE && this.source instanceof ASTD && this.target instanceof ASTD) {
			this.targetConnection.showOverlay(SEQUENCE);
			if (this.source === this.target) {
				this.detachFromComponents();
				this.instance.deleteConnection(this.targetConnection);
			}
			this.e_item.remove();
		} else if (this.model.type === TIMEOUT && this.source instanceof ASTD && this.target instanceof ASTD) {
			this.targetConnection.showOverlay(TIMEOUT);
			if (this.source === this.target) {
				this.detachFromComponents();
				this.instance.deleteConnection(this.targetConnection);
			}
			$(this.e_item).show();
			let lastTab = this.source.parent.m_tabs.length - 1;
			if (this.source.parent.m_tabs[lastTab] instanceof Interrupt) {
				this.e_item.innerHTML = "Interrupt";
			} else if (this.source.parent.m_tabs[lastTab] instanceof Timeout) {
				this.e_item.innerHTML = "Timeout";
			} else if (this.source.parent.m_tabs[lastTab] instanceof PersistentTimeout) {
				this.e_item.innerHTML = "PTO";
			} else if (this.source.parent.m_tabs[lastTab] instanceof TimedInterrupt) {
				this.e_item.innerHTML = "TI";
			}
		}
	}

	/**
	 * This function create a connection between 2 component
	 * It also bind specific events to each connection
	 * @param s source
	 * @param d destination
	 * @param isDelete the connection to create is after a deletion of a flexpoint
	 * @return created connection
	 */
	plumbConnect(s, d, isDelete) {
		// Copy reference of the linkview
		var that = this;

		// Make sure we can create loopBack on FlexPoint
		if (s === d && d instanceof FlexPoint && s instanceof FlexPoint) {
			return;
		}

		// Create the connection
		let connectPlumb;
		connectPlumb = that.instance.connect({
			source: s.id,
			target: d.id,
			detachable: false,
			anchor: "Continuous",
			cssClass: "stateArrowClass " + this.model.id,
			hoverClass: "stateArrowHover",
			endpoint: [
				"Dot",
				{ radius: 1 }
			],
			connector: s === d ? ["StateMachine"] : ["Straight"],
			overlays: d instanceof FlexPoint ? [CUSTOM_OVERLAY] : OVERLAYS_DEFAULT,
			scope: this.model.m_id
		});

		// Define double-click on a link
		// @Action : Add a new inflexion Point
		connectPlumb.bind("dblclick", function(connection, event) {
			var x = event.pageX - $(s.parent.e_item).offset().left - 25,
				y = event.pageY - $(s.parent.e_item).offset().top - 37;

			if (s === d) {
				that.add2Point(connection, s, d, x + 100, y + 100);
			} else {
				that.addPoint(connection, s, d, x, y);
			}
		});

		// Define single-click on a link
		// @Action : Select the connection
		connectPlumb.bind("click", that.focusPanel.bind(that));

		// Define clipping on a connection when label is released
		// @Action : Change the label connection and update the source label
		connectPlumb.bind("clip", function(connection) {
			that.labelConnection = connection;
			that.setSourceLabel(s, d);
		});

		// set the current plumbConnection for reference
		this.jsPlumbConnection = connectPlumb;
		// When create new flexpoint set label on source to point
		// When from deletion of flexpoint, set connection if the target is the same as move
		if (s === this.moveSource || isDelete && d === this.moveTarget) {
			this.labelConnection = connectPlumb;
			this.setSourceLabel(s, d);
		}

		// Update is done separately to avoid null problem when creating a new connection
		// If new connection is linked to the source, update the endpoint according to type
		if (s === this.source) {
			this.sourceConnection = connectPlumb;
			this.updateConnectionEndpoint();
		}

		// If new connection is linked to the target, update the overlay according to type
		if (d === this.target) {
			this.targetConnection = connectPlumb;
			this.updateConnectionOverlay();
		}

		return connectPlumb;
	}

	// Delete all the sub-link and flexion point associated with this transition
	delete() {
		Array.from(this.flexPoints).forEach((point) => {
			point.remove();
		});
		this.instance.deleteConnection(this.jsPlumbConnection);
		// Manually remove the contextMenu so it is rebind even if a new link has the same selectors
		$.contextMenu("destroy", `#${this.e_item.id}, .${this.model.id}`);
		this.e_item.remove();
		// stop to notify the linkView on drag or resize of old source/destination
		this.detachFromComponents();
	}

	// Remove a flexPoint from the array
	removePoint(flexPoint) {
		var indexToRemove = this.flexPoints.indexOf(flexPoint);
		return this.flexPoints.splice(indexToRemove, 1);
	}

	// Add a point in the array and create 2 new links
	addPoint(currentConnection, source, target, x, y) {
		// Make sure that we cant add inflexion point on loopBack Connection
		//if (source === target) return;

		// Delete old connection and create new one between source target and the new inflexion point
		this.instance.deleteConnection(currentConnection);
		// console.log(this);
		var item = new FlexPoint(window.id.get(), source.parent, source, target, x, y, this);
		item.source.target = item;
		item.target.source = item;
		this.flexPoints.push(item);
		return [item.source, item];
	}

	add2Point(currentConnection, source, target, x, y) {
		var l = this.instance.getConnections().length;
		this.instance.deleteConnection(currentConnection);
		var item1 = new FlexPoint(window.id.get(), source.parent, source, target, x, y, this),
			item2 = new FlexPoint(window.id.get(), source.parent, item1, target, x, y + 60, this);
		item1.target = item2;
		item1.source = source;
		item2.source = item1;
		item2.target = target;
		source.target = item1;
		target.source = item2;
		this.flexPoints.push(item1);
		this.flexPoints.push(item2);
		this.instance.deleteConnection(this.instance.getConnections()[l - 1]);
	}

	// Fonction call when clicking label or transition
	// Display info in to panel
	focusPanel() { // fromLabel = false
		// first show the panel, otherwise, we can't retrieve the input label
		window.panel.selectItem(this.model);

		// Illuminer toute la transition en rouge/orange
		this.instance.select({ scope: this.model.m_id }).setHover(true);

		var that = this,
			selectorLabel = $("#linkNameInputId");

		if (selectorLabel.val() === "?") {
			selectorLabel.val("");
		}

		selectorLabel.on("change paste keyup", function() {
			that.model.label = selectorLabel.val();
		});

		// S'assure que la transition ne reste pas illuminee
		selectorLabel.on("blur", function() {
			that.unfocusConnections();
		});
		// console.log(this.e_item)
		selectorLabel.focus();
	}

	/**
	 * Remette la transition a l'etat "non-selectionnee".
	 */
	unfocusConnections() {
		this.instance.select({ scope: this.model.m_id }).setHover(false);
	}

	save() {
		var ret = [];
		this.flexPoints.forEach((p) => {
			ret.push(p.save());
		});
		return ret;
	}

	load(points) {
		var that = this;
		// add all points one after the other and draw connections each time
		let s = that.source,
			d = that.target;
		points.forEach((point) => {
			[s, d] = that.addPoint(that.jsPlumbConnection, s, d, point.position.x, point.position.y);
		});
	}
}