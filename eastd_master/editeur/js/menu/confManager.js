class confManager {

	/**
	 * Lazily load and open the dialog to manage config.
	 * If the dialog was not previously built or was deleted, we build it, otherwise we reuse the existing one
	 */
	static open() {
		if (!$("#configModal").length)
			window.document.body.appendChild(this.buildForm());
		$("#configModal").modal("show");

		// Re-init value
		$("#nameInput").val("");
		$("#outputDirectory").val("");
		$("#inputSpec").val("");
		$("#selectTargetBuild").val("CPP");
		$("#runWith").val("cASTD");
		$("#stepInput").val("");
		$("#debug").prop("checked", false);
		$("#condition").prop("checked", false);
		$("#kappa").prop("checked", true);
	}

	/**
	 * Build the concrete component used to manage the config, initialize components and attach actions on events
	 * @returns {HTMLDivElement} The modal dialog component to be added to the document
	 */
	static buildForm() {
		var elem = $(`
			<div class="modal fade" id="configModal" tabindex="-1" role="dialog" aria-labelledby="traceTitle" aria-hidden="true" data-backdrop="static" data-keyboard="false">
				<div class="modal-dialog" role="document">
					<div class="modal-content">
						<div class="modal-header d-inline-block">
							<h5 class="modal-title d-inline-block" id="confTitle">Configuration</h5>
							<i class="btn btn-outline-danger fas fa-trash float-right" onclick="confManager.delete();"></i>
						</div>
						<form class="modal-body" id="conf_form">

							<!-- Name -->
							<div class="input-group mb-3">
								<div class="input-group flex-nowrap">
									<div class="input-group-prepend">
										<span class="input-group-text" id="addon-wrapping">Name : </span>
									</div>
									<input id="nameInput" type="text" class="form-control" placeholder="Configuration name" aria-label="configuration name" aria-describedby="addon-wrapping" required><br>
								</div>
								<label id="nameRequiredError" class="form-check-label text-danger" hidden="true">
									This field is required!
							  	</label>
							</div>
							
							<!-- Output -->
							<div class="input-group mb-3">
								<input id="outputDirectory" type="text" class="form-control" placeholder="Compiled output path (Optional)" aria-label="output path" aria-describedby="button-addon2" title="Use project folder if empty">
								<div class="input-group-append">
									<button class="btn btn-outline-secondary" type="button" id="btnOutput">...</button>
								</div>
							</div>
							
							<!-- Spec input -->
							<div class="input-group mb-3">
							  	<input id="inputSpec" type="text" class="form-control" placeholder="External specification path (Optional)" aria-label="spec path" aria-describedby="button-addon2" title="Leave empty current astd if empty">
								<div class="input-group-append">
									<button class="btn btn-outline-secondary" type="button" id="btnInput">...</button>
								</div>
							</div>
							
							<!-- Target -->
							<div class="input-group mb-3">
								<div class="input-group-prepend">
									<label class="input-group-text" for="inputGroupSelect01">Target build</label>
							  	</div>
							  	<select id="selectTargetBuild" class="custom-select" id="inputGroupSelect01">
									<option value="default"selected>Choose...</option>
									<option value="CPP">CPP</option>
							 	</select>
							</div>

							<!-- Run -->
							<div class="input-group mb-3">
								<div class="input-group-prepend">
									<label class="input-group-text" for="inputGroupSelect01">Run with</label>
							  	</div>
							  	<select id="selectTargetBuild" class="custom-select" id="inputGroupSelect01">
									<option value="default"selected>iASTD</option>
									<option value="CPP">cASTD</option>
							 	</select>
							</div>

							<!-- Step -->
							<div class="input-group mb-3">
								<div class="input-group flex-nowrap">
									<div class="input-group-prepend">
										<span class="input-group-text" id="addon-wrapping">Step Interval : </span>
									</div>
									<input id="stepInput" type="text" class="form-control" placeholder="Interval used for step execution" aria-label="step" aria-describedby="addon-wrapping" required><br>
								</div>
							</div>

							<div class="input-group mb-3">
								<div class="input-group-prepend">
									<label class="input-group-text" for="inputGroupSelect01">Step Time Unit</label>
							  	</div>
							  	<select id="selectTimeUnit" class="custom-select" id="inputGroupSelect01">
									<option value="d">Days</option>
									<option value="h">Hours</option>
									<option value="s" selected>Seconds</option>
									<option value="ms">Milliseconds</option>
									<option value="us">Microseconds</option>
							 	</select>
							</div>
							
							<!-- Options -->
							<div class="form-check">
							  <input id="debug" class="form-check-input" type="checkbox" value="Debug">
							  <label class="form-check-label" for="debug">
								Debug
							  </label>
							  <br/>
							  <input id="condition" class="form-check-input" type="checkbox" value="Cond">
							  <label class="form-check-label" for="condition">
								Condition optimization
							  </label>
							  <br/>
							  <input id="kappa" class="form-check-input" type="checkbox" checked="checked">
							  <label class="form-check-label" for="kappa">
								Kappa optimization
							  </label>
							</div>
							
							<div class="modal-footer">
								<button type="button" class="btn btn-outline-primary" id="addBuildConf">Add config...</button>
								<button type="button" class="btn btn-dark" id="closeBtn">Close</button>
							</div>
						</form>
						
					</div>
				</div>
			</div>
		`);

		elem.find("#closeBtn").on("click", function() {
			$("#configModal").modal("hide");
		});

		elem.find("#addBuildConf").on("click", confManager.onAddConf.bind(elem.find("#addBuildConf")[0]));
		elem.find("#deleteBtn").on("click", confManager.delete.bind(elem.find("#deleteBtn")[0]));
		elem.find("#btnOutput").on("click", confManager.findOutput.bind(elem.find("#btnOutput")[0]));
		elem.find("#btnInput").on("click", confManager.findInput.bind(elem.find("#btnInput")[0]));

		return elem[0];
	}

	/**
	 * Add a new configuration to the project
	 */
	static onAddConf() {
		let conf = {
			name: $("#nameInput").val(),
			output: $("#outputDirectory").val(),
			input: $("#inputSpec").val(),
			target: $("#selectTargetBuild option:selected").text(),
			timeUnit: $("#selectTimeUnit option:selected").val(),
			prog: $("#runWith option:selected").text(),
			step: $("#stepInput").val(),
			debug: $("#debug").prop("checked"),
			cond: $("#condition").prop("checked"),
			kappa: $("#kappa").prop("checked")
		};

		// Clear old configuration if any
		if ( project.getConf(conf.name) ) {
			project.deleteConf(conf.name);
		}

		// Add conf to project
		if ( conf.name === "" ) {
			$("#nameRequiredError").prop("hidden", false);
		} else {
			project.addConf(conf);
			$("#nameRequiredError").prop("hidden", true);
			$("#configModal").modal("hide");
		}
	}

	/**
	 * Delete a configuration of the project
	 */
	static delete() {
		let confName = $("#optConfig option:selected").val();
		project.deleteConf( confName );
		$("#configModal").modal("hide");
	}

	/**
	 * Modify a configuration of the project
	 */
	static load() {
		let confName = $("#optConfig option:selected").val();
		if (confName !== "addConf") {
			$("#configModal").modal("show");
			let conf = project.getConf( confName );

			// Load value
			$("#nameInput").val(conf.name);
			$("#outputDirectory").val(conf.output);
			$("#inputSpec").val(conf.input);
			$("#selectTargetBuild").val(conf.target);
			$("#selectTimeUnit").val(conf.timeUnit);
			$("#runWith").val(conf.run);
			$("#stepInput").val(conf.step);
			$("#debug").prop("checked", conf.debug);
			$("#condition").prop("checked", conf.cond);
			$("#kappa").prop("checked", conf.kappa);
		}
	}

	static findOutput() {
		ipc.send("dir-select", "", "spec-output"); // Open dialog
	}

	static findInput() {
		ipc.send("open-file-dialog", "spec-input"); // Open dialog
	}
}

/**************************************************
 * 					IPC Channel                   *
 *************************************************/
ipc.on("spec-output", function(event, files) {
	$("#outputDirectory").val(files);
});

ipc.on("spec-input", function(event, files) {
	$("#inputSpec").val(files);
});