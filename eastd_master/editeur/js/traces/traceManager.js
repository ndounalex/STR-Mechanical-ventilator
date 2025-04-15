class TraceManager {
	/**
	 * Lazily load and open the dialog to manage traces.
	 * If the dialog was not previously built or was deleted, we build it, otherwise we reuse the existing one
	 */
	static open() {
		if (!$("#traceModal").length)
			window.document.body.appendChild(this.buildForm());
		$("#traceModal").modal("show");
	}

	/**
	 * Build the concrete component used to manage the traces, initialize components and attach actions on events
	 * @returns {HTMLDivElement} The modal dialog component to be added to the document
	 */
	static buildForm() {
		var elem = $(`
			<div class="modal fade" id="traceModal" tabindex="-1" role="dialog" aria-labelledby="traceTitle" aria-hidden="true" data-backdrop="static" data-keyboard="false">
				<div class="modal-dialog" role="document">
					<div class="modal-content">
						<div class="modal-header d-inline-block">
							<h5 class="modal-title d-inline-block" id="traceTitle">Traces</h5>
							<button class="btn btn-outline-primary btn-sm d-inline-block" id="traceAddBtn">+</button>
						</div>
						<form class="modal-body" id="traces_form">
							<ul class="list-group list-group-flush was-validated" id="traces_list"></ul>
						</form>
						<div class="modal-footer">
							<button type="button" class="btn btn-outline-primary" id="genTraces">Generate traces</button>
							<button type="button" class="btn btn-dark" id="closeBtn">Close</button>
						</div>
					</div>
				</div>
			</div>
		`);

		elem.find("#closeBtn").on("click", function() {
			$("#traceModal").modal("hide");
		});

		elem.find("#genTraces").on("click", function() {
			project.generateTraces();
			if ($("#traces_form")[0].checkValidity()) {
				$("#traceModal").modal("hide");
			}
		});

		elem.find("#traceAddBtn").on("click", TraceManager.onAddTrace.bind(elem.find("#traces_list")[0]));

		if (project.traces.length === 0) {
			elem.find("#traces_list")[0].textContent = "The current project doesn't have traces.";
		}
		project.traces.forEach((trace) => {
			TraceManager.addTraceComponent.call(elem.find("#traces_list")[0], trace);
		});

		return elem[0];
	}

	/**
	 * action to execute when the add trace button is pressed.
	 * 'this' must be bind to the traces_list component (elem.find("#traces_list")[0])
	 */
	static onAddTrace() {
		var traceObj = {
			name: "",
			content: ""
		};
		if (project.traces.length === 0)
			$(this).empty();

		project.addTrace(traceObj);
		TraceManager.addTraceComponent.call(this, traceObj);
	}

	/**
	 * build and add a new trace component to the UI
	 * 'this' must be bind to the traces_list component (elem.find("#traces_list")[0])
	 * @param traceObj: the trace object the component will be associated to
	 */
	static addTraceComponent(traceObj) {
		var that = this,
			contentError = Validator.syntax_Verify(traceObj.content, window.patron_trace_grammar_object, true).message,
			nameError = traceObj.name === "" ? "This field is mandatory" : "",
			placeholder = "Ex: {{ e1_1\n{\te2_$x1(\"hey\")\n\t{e3_$x3($x1,$x2)}$x3 : 5..6\n\t} $x2 : 3..4\n} $x1 : 1..2 }^2",
			traceDiv = document.createElement("div"),
			nameComp = CompBuilder.generateTextBlock("input", "Trace Name:", nameError, traceObj.name, "Name"),
			traceComp = CompBuilder.generateTextBlock("textarea", "Content:", contentError, traceObj.content, placeholder),
			remove = document.createElement("button");
		traceDiv.className = "list-group-item";

		remove.className = "close";
		remove.innerHTML = "&times;";
		remove.onclick = function() {
			project.removeTrace(traceObj);
			traceDiv.remove();
			if (project.traces.length === 0)
				that.textContent = "The project doesn't have traces.";
		};
		// bind name input actions and error validations
		nameComp[1].setCustomValidity(nameComp[2].textContent);
		nameComp[1].onblur = function() {
			traceObj.name = nameComp[1].value;
			nameComp[2].textContent = traceObj.name === "" ? "This field is mandatory" : "";
			nameComp[1].setCustomValidity(nameComp[2].textContent);
		};
		nameComp[1].onkeypress = function(ev) {
			if (ev.keyCode === 13) {
				ev.preventDefault();
				this.blur();
			}
		};
		// bind trace input actions and error validations
		traceComp[1].setCustomValidity(traceComp[2].textContent);
		traceComp[1].onblur = function() {
			traceObj.content = traceComp[1].value;
			traceComp[2].textContent = Validator.syntax_Verify(traceObj.content, window.patron_trace_grammar_object, true).message;
			traceComp[1].setCustomValidity(traceComp[2].textContent);
		};

		[...nameComp, ...traceComp, remove].forEach((component) => {
			traceDiv.appendChild(component);
		});
		this.appendChild(traceDiv);
		// refresh textarea height
		$(this).find("textarea").change();
	}
}