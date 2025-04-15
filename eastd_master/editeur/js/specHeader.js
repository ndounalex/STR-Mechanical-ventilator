class SpecHeader {
	/**
	 * Lazily load and open the dialog to manage imports.
	 * If the dialog was not previously built or was deleted, we build it, otherwise we reuse the existing one
	 */
	static open() {
		if (!$("#specHeaderModal").length)
			window.document.body.appendChild(this.buildForm());
		$("#specHeaderModal").modal("show");
	}

	/**
	 * Build the concrete component form used to manage the imports, initialize components and attach actions on events
	 * @returns {HTMLDivElement} The modal dialog component to be added to the document
	 */
	static buildForm() {
		var elem = $(`
			<div class="modal fade" id="specHeaderModal" tabindex="-1" role="dialog" aria-labelledby="specTitle" aria-hidden="true" data-backdrop="static" data-keyboard="false">
				<div class="modal-dialog" role="document">
					<div class="modal-content">
						<div class="modal-header">
							<h5 class="modal-title" id="specTitle">Imports</h5>
						</div>
						<div class="modal-body">
							<form id="importsForm">
								<fieldset class="form-group">
									<legend class="col-form-label">Target Language:</legend>
<!--									<div class="form-check form-check-inline">-->
<!--									  <input class="form-check-input" type="radio" name="targetOptions" id="ocamlTarget" value="OCaml">-->
<!--									  <label class="form-check-label" for="ocamlTarget">OCaml</label>-->
<!--									</div>-->
									<div class="form-check form-check-inline">
									  <input class="form-check-input" type="radio" name="targetOptions" id="cppTarget" value="C++" checked>
									  <label class="form-check-label" for="cppTarget">C++</label>
									</div>
								</fieldset>
								<div class="form-group" id="imports">
									<label>Executable Code Modules:</label>
									<input type="button" class="col-0 btn btn-outline-primary btn-sm" value="&plus;" onclick="ipc.send('open-file-dialog', 'module-import', ['ml', 'cpp']);" />
									<ul class="list-group list-group-flush" id="modules_list"></ul>
								</div>
<!--								<div class="form-group" id="schemas">-->
<!--									<label>Types defined in JSON schemas:</label>-->
<!--									<input type="button" class="col-0 btn btn-outline-primary btn-sm" value="&plus;" onclick="ipc.send('open-file-dialog', 'schema-import', ['json']);" />-->
<!--									<ul class="list-group list-group-flush" id="schemas_list"></ul>-->
<!--								</div>-->
								<div class="form-group" id="native_types">
									<label>Types defined in target execution language:</label>
									<input type="button" class="col-0 btn btn-outline-primary btn-sm" id="nativesAddBtn" value="&plus;"/>
									<ul class="list-group list-group-flush" id="natives_list"></ul>
								</div>
<!--								<div class="form-group" id="events">-->
<!--									<label>Event Signatures:</label>-->
<!--									<input type="button" class="col-0 btn btn-outline-primary btn-sm" value="&plus;" onclick="ipc.send('open-file-dialog', 'event-signs-import', ['json']);" />-->
<!--									<ul class="list-group list-group-flush" id="events_list"></ul>-->
<!--								</div>-->
							</form>
						</div>
						<div class="modal-footer">
							<button type="button" class="btn btn-dark" id="closeBtn">Close</button>
						</div>
					</div>
				</div>
			</div>
		`);

		// Cocher le langage par defaut du projet (AU CAS QU'IL Y AIE UN JOUR D'AUTRES LANGAGES POSSIBLE)
		// for (let i = 0; i < elem.find(".form-check-input").length; ++i) {
		// 	elem.find(".form-check-input")[i].checked = (elem.find(".form-check-input")[i].value === project.targetLanguage);
		// }

		elem.find(".form-check-input").on("click", function(ev) {
			project.targetLanguage = ev.target.value;
		});

		elem.find("#closeBtn").on("click", function() {
			if ($("#importsForm")[0].checkValidity()) {
				$("#native_types")[0].classList.remove("was-validated");
				$("#specHeaderModal").modal("hide");
			} else {
				$("#native_types")[0].classList.add("was-validated");
			}
		});

		elem.find("#nativesAddBtn").on("click", SpecHeader.onAddNativeType.bind(elem.find("#natives_list")[0]));
		// initialize components with project elements from load
		project.types.filter((type) => {
			return type.type !== "basic";
		}).forEach((typeElem) => {
			if (typeElem.type === "complex") {
				SpecHeader.addFileComponent.call(elem.find("#schemas_list")[0], typeElem.path, typeElem.name, project.removeSchema, project.toggleSchemaRelativePath);
			} else { // native type
				SpecHeader.addNativeTypeComponent.call(elem.find("#natives_list")[0], typeElem.id, typeElem.name, typeElem.symbol, typeElem.destructor);
			}
		});

		project.moduleImports.forEach((moduleImport) => {
			SpecHeader.addFileComponent.call(elem.find("#modules_list")[0], moduleImport.path, moduleImport.id, project.removeModule, project.toggleModuleRelativePath);
		});

		project.eventSignatures.forEach((eventSign) => {
			SpecHeader.addFileComponent.call(elem.find("#events_list")[0], eventSign.path, eventSign.id, project.removeEventSignatures, project.toggleEventSignRelativePath);
		});

		return elem[0];
	}

	/**
	 * action to execute when the add native type button is pressed.
	 * 'this' must be bind to the natives_list component (elem.find("#natives_list")[0])
	 */
	static onAddNativeType() {
		var id = window.id.get();
		project.addNativeType(id);
		SpecHeader.addNativeTypeComponent.call(this, id);
	}

	/**
	 * build and add a new native type component to the UI
	 * 'this' must be bind to the natives_list component (elem.find("#natives_list")[0])
	 * @param compId: id of the resulting component
	 * @param nameVal: Initial value of the name input (empty by default)
	 * @param symbolVal: Initial value of the symbol input (empty by default)
	 * @param destVal: Initial value of the destructor input (empty by default)
	 */
	// eslint-disable-next-line no-unused-vars
	static addNativeTypeComponent(compId, nameVal = "", symbolVal = "", destVal = "") {
		// name = SpecHeader.addInput("name", "ID", compId , nameVal),
		var typeName = SpecHeader.addInput("symbol", "Type name", compId, symbolVal),
			destruct = SpecHeader.addInput("destructor", "Destructor", compId, destVal),
			remove = document.createElement("button"),
			li = document.createElement("li"),
			div = document.createElement("div");

		li.className = "list-group-item";
		li.id = compId;
		div.className = "col-12";
		remove.className = "close";
		remove.innerHTML = "&times;";
		remove.onclick = function() {
			project.removeNativeType(compId);
			li.remove();
		};

		[typeName, destruct].forEach((component) => {
			div.appendChild(component);
		});
		[div, remove].forEach((component) => {
			li.appendChild(component);
		});

		// typeName.onblur = SpecHeader.onInputBlur_changeName.bind(this, compId);
		this.appendChild(li);
	}

	/**
	 * Action to execute once the user confirmed the selected files from the file dialog.
	 * We reset the value of the target to always get notified even if the user selected an already existing file, because there could be new ones added.
	 * 'this' must be bind to the proper list component (elem.find("#schemas_list")[0] or elem.find("#events_list")[0])
	 * @param executeOnLoad: Action to execute once the file content has been read. Must be bind to 'this' and has a parameter containing the file path
	 * @param files: Array of the paths returned by the main process
	 */
	static onFileSelect(files, executeOnLoad) {
		files.forEach((filePath) => {
			try {
				const result = JSON.parse(fs.readFileSync(filePath, "utf8"));
				executeOnLoad.call(this, filePath, result);
			} catch (error) {
				toastr.error(error.message);
			}
		});
	}

	/**
	 * Action to execute once the module files to import has been selected.
	 * 'this' must be bind to the modules list component (elem.find("#modules_list")[0])
	 * @param filePaths: Array of the paths returned by the main process
	 */
	static onModuleImports(filePaths) {
		filePaths.forEach((path) => {
			var id = window.id.get();
			if (project.addModule(path, id))
				SpecHeader.addFileComponent.call(this, path, id, project.removeModule, project.toggleModuleRelativePath);
			else
				toastr.error("The given module already exists or has an invalid file extension.");
		});
	}

	/**
	 * builds and add a new file component to the UI, which means the path of a file and option to delete it from the project
	 * @param pathToFile: Value of the path to add
	 * @param compId: id of the resulting component. Used to sync with project model
	 * @param executeOnDelete: Action the execute when the component is deleted. It is bind to the project and must pass the component id
	 * 						   so the project know which element to delete and what action to do.
	 * @param executeOnRelative: Action to execute when the checkbox for relative path is clicked. It is bind to the project and
	 * 							 must pass the component id so the project know which element path to update.
	 */
	static addFileComponent(pathToFile, compId, executeOnDelete, executeOnRelative) {
		var newLabel = document.createElement("label"),
			btnDel = document.createElement("button"),
			relInput = document.createElement("input"),
			relLabel = document.createElement("label"),
			check = document.createElement("div"),
			div = document.createElement("li");
		div.className = "list-group-item";
		div.id = compId;
		newLabel.className = "filePathLabel";
		newLabel.innerText = pathToFile;
		btnDel.className = "close";
		btnDel.innerHTML = "&times;";
		btnDel.onclick = function() {
			executeOnDelete.call(project, div.id);
			div.remove();
		};

		check.className = "form-check form-check-inline";
		relInput.type = "checkbox";
		relInput.className = "form-check-input";
		relInput.id = compId + "RelInput";
		relLabel.className = "form-check-label col-form-label-sm";
		relLabel.for = relInput.id;
		relLabel.innerText = "Use relative path?";
		relInput.checked = !path.isAbsolute(pathToFile);
		relInput.onclick = function() {
			newLabel.innerText = executeOnRelative.call(project, div.id);
		};

		check.appendChild(relInput);
		check.appendChild(relLabel);
		div.appendChild(newLabel);
		div.appendChild(btnDel);
		div.appendChild(check);
		this.appendChild(div);
	}

	/**
	 * Builds a component for input with an attached label and attach actions on blur and key press events
	 * @param propertyToBind: name of the property the input blur event must be attached to
	 * @param labelText: Value of the label associated to the input field
	 * @param id: id corresponding to the project native type id to know which one is modified on blur event
	 * @param value: Initial value of the input field
	 * @returns {HTMLDivElement}: the resulting component to be added to the aprent that called this method
	 */
	static addInput(propertyToBind, labelText, id, value) {
		var container = document.createElement("div"),
			label = document.createElement("label"),
			input = document.createElement("input"),
			errorFeedback = document.createElement("div");
		errorFeedback.className = "invalid-feedback";
		errorFeedback.innerText = labelText + " must be filled.";

		container.className = "row form-group";
		label.innerText = labelText;
		label.className = "col-3 col-form-label col-form-label-sm";

		input.type = "text";
		input.value = value;
		input.required = true;
		input.className = "form-control form-control-sm col-8";
		input.onblur = SpecHeader.onInputBlur.bind(this, id, propertyToBind);

		// if (propertyToBind === "symbol") {
		// 	input.onblur = SpecHeader.onInputBlur.bind(this, id, "name");
		// }
		input.onkeypress = function(ev) {
			if (ev.keyCode === 13) {
				ev.preventDefault();
				this.blur();
			}
		};

		[label, input, errorFeedback].forEach((comp) => {
			container.appendChild(comp);
		});
		return container;
	}

	/**
	 * Action to execute once a schema file is read. Adds the content to the project and a file component to the UI if there is no error
	 * @param fileName: name of the file we just read
	 * @param content: parsed content of the file we read
	 */
	static loadSchema(fileName, content) {
		if (!content.$schema)
			throw new Error("The given file is not a json schema.");
		project.addSchema(content, fileName);
		SpecHeader.addFileComponent.call(this, fileName, content.$id, project.removeSchema, project.toggleSchemaRelativePath);
	}

	/**
	 * Action to execute once an event signatures file is read. Adds the content to the project and a file component to the UI if there is no error
	 * @param fileName: name of the file we just read
	 * @param content: parsed content of the file we read
	 */
	static loadEventSignature(fileName, content) {
		var id = window.id.get();
		if (!Validator.ajv.validate("__event_signature__", content))
			throw new Error("The file is not valid. It should only contains an object with string arrays.");
		project.addEventSignatures(content, fileName, id);
		SpecHeader.addFileComponent.call(this, fileName, id, project.removeEventSignatures, project.toggleEventSignRelativePath);
	}

	/**
	 * Assigns the value of the target input to the right native type property of project.
	 * @param id: id of the native type to apply changes to in the project model
	 * @param property: name of the property we want to assign the input value
	 * @param event: blur event that triggered the function call
	 */
	static onInputBlur(id, property, event) {
		project.getNativeType(id)[property] = event.target.value;

		if (property === "symbol") {
			project.getNativeType(id)["name"] = event.target.value;
		}
	}

}