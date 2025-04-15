/**
 * This file contains code for execution console
 */
class Console {

	constructor() {
		// Attributes
		this.isPlaying = false;
		this.isInput = false;
		this.isOpen = false;
		this.isCompile = false;
		this.curProcess = null;
		this.ipcSocket = null;

		// Path Attribute
		this.inFilePath = null;
		this.inputType = null;
		this.spec_path = window.project.rootPath + "/tmpRun.json";
		this.compiler_jar = __dirname + "/../../castd/castd.jar";
		this.iastd_path = __dirname + "/../../iASTD";
		this.iastd_exec = this.iastd_path + "/iASTD";
		this.compiled_exec = window.project.rootPath;

		// Bind button to console
		this.bindEvents();
	}

	set inFilePath(inputFilePath) {
		this.m_inFilePath = inputFilePath;
		this.isInput = true;
	}

	set inputType(inputType) {
		this.m_inputType = inputType;
	}

	/**
	 * This pretty much append text in a format way inside
	 * the console
	 * @param data receive from process
	 */
	writeIntoConsole(data, color) {
		let strData = `${data}`;
		strData.split("\n").forEach((lineIn) => {
			let textSpan = document.createElement("p");
			textSpan.innerHTML = lineIn;
			textSpan.style.color = color;
			$(".output-console").append(textSpan);
		});
		$(".output-console").animate({ scrollTop: $(".output-console")[0].scrollHeight });
	}

	/**
	 * This function launch a process
	 */
	launchProcess(cmd, args) {
		var that = this;

		// init process
		let process = spawn(cmd, args);

		// attach to castd process errors caused when spawned
		process.stderr.on("data", function(data) {
			that.writeIntoConsole(data, "red");
		});

		// attach to castd process output to console
		process.stdout.on("data", (data) => {
			that.writeIntoConsole(data, "white");
		});

		return process;
	}

	HandleDataFromJson(jsonObj, isNewEvent = true) {
		var currentStateJsonObjList,
		 rootAstd = window.project.currentFile.astd,
		 currentStateItemList = [];
		window.project.currentStateJsonObjList = [];

		if (isNewEvent) {
			window.project.lastTransList.push(jsonObj.executed_event);
		}

		currentStateJsonObjList = this.GetCurrentStateJsonObjectList(jsonObj.top_level_astd);

		$(".content > .item").removeClass("current");
		$(".astd").removeClass("current");

		var i;
		for (i = 0; i < currentStateJsonObjList.length; i++) {
			currentStateItemList.push(this.GetCurrentStateInASTD(rootAstd, currentStateJsonObjList[i].current_state_name, currentStateJsonObjList[i].name));
			window.project.currentStateJsonObjList.push(currentStateJsonObjList[i]);

			if (currentStateItemList[i] !== null) {
				rootAstd.setCurrent();
				currentStateItemList[i].setCurrent();
				window.panel.selectItem(currentStateItemList[i].parent);
			}
		}

	}

	GetCurrentStateJsonObjectList(obj) {
		if (obj.type === "QSynchronization") {
			var result = [];

			if (window.project.qvarValuePerName.get(obj.sub_states[0].qsynch_var) === undefined) {
				window.project.qvarValuePerName.set(obj.sub_states[0].qsynch_var, obj.sub_states[0].value);
			}

			var i;
			for (i = 0; i < obj.sub_states.length; i++) {

				if (window.project.qvarValuePerName.get(obj.sub_states[i].qsynch_var) === obj.sub_states[i].value) {
					var subStatesResults = this.GetCurrentStateJsonObjectList(obj.sub_states[i]),
					 j;
					for (j = 0; j < subStatesResults.length; j++) {
						result.push(subStatesResults[j]);
					}
				}
			}
			return result;
		} else if (obj.current_sub_state.type === "Elem") {
			return [obj];
		} else if (obj.current_sub_state.type === SYNCHRONIZATION || obj.current_sub_state.type === FLOW) {
			var leftResult = this.GetCurrentStateJsonObjectList(obj.current_sub_state.left),
			 rightResult = this.GetCurrentStateJsonObjectList(obj.current_sub_state.right);
			for (i = 0; i < rightResult.length; i++) {
				leftResult.push(rightResult[i]);
			}

			return leftResult;
		} else if (obj.current_sub_state.type == CALL) {
			var result = [];
			result.push(obj);
			if (obj.current_sub_state.called_astd.type !== null) {

				var calledAstdResult = this.GetCurrentStateJsonObjectList(obj.current_sub_state.called_astd),

				 i;
				for (i = 0; i < calledAstdResult.length; i++) {
					result.push(calledAstdResult[i]);
				}
			}
			return result;
		} else {
			return this.GetCurrentStateJsonObjectList(obj.current_sub_state);
		}
	}

	GetCurrentStateInASTD(astd, stateLabel, parentLabel) {
		var i;
		for (i = 0; i < astd.items.length; i++) {
			var currentState = null;
			if (astd.items[i] instanceof ASTD) {
				currentState = this.GetCurrentStateInASTD(astd.items[i], stateLabel, parentLabel);

				if (astd.items[i].label === stateLabel && astd.items[i].parent.label === parentLabel) {
					astd.items[i].setCurrent();
				}

				if (currentState instanceof State) {
					astd.items[i].setCurrent();
					return currentState;
				}
			} else {
				currentState = astd.items[i];

				var checkedParentLabel = astd.items[i].parent.label;

				if (currentState.parent.tabs.length > 1) {
					checkedParentLabel = currentState.parent.tabs[currentState.parent.tabs.length - 1].label;
				}

				if (currentState.label === stateLabel && checkedParentLabel === parentLabel) {
					return currentState;
				}
			}
		}
		return null;
	}

	/**
	 * This function create the arguments array to be given to the spawn process
	 */
	buildArgument() {
		let conf = project.getCurrentConf(),
			args = ["-jar", this.compiler_jar];

		// Input spec
		if (conf.input === "") {
			args.push("-s");
			args.push(this.spec_path);
		} else {
			args.push("-s");
			args.push(conf.input);
		}

		// Output spec
		if (conf.output === "") {
			args.push("-o");
			args.push(project.rootPath);
			this.compiled_exec = window.project.rootPath;
		} else {
			args.push("-o");
			args.push(conf.output);
			this.compiled_exec = conf.output;
		}

		// Show execution state
		args.push("-e");

		//Debug
		if (conf.debug)
		   args.push("-d");

		//Condition
		if (conf.cond)
		   args.push("-c");

		//Kappa
		if (conf.kappa)
		    args.push("-k");

		//main astd to compile
		var selected_astd_name = $("#files .active")[0].textContent;
		args.push("-m");
		args.push(selected_astd_name);

		// Run with input file
		if (this.isInput) {
			if (this.m_inputType === "shorthand") {
				args.push("-f");
				args.push("shorthandevents");
			} else {
				args.push("-f");
				args.push("json");

			}
		// Run interactive
		} else {
			args.push("-f");
			args.push("shorthandevents");
		}

		return args;
	}

	/**
	 * Create temporary spec
	 */
	generateSpec() {
		// If spec is valid, then export temporary spec for run
		let bFailedToExport = true;
		if (window.project.verify(true)) {
			const exp = window.project.export();
			if (exp) {
				bFailedToExport = false;
				fs.writeFileSync(this.spec_path, exp);
			}
		}

		// If export failed fix UI button
		if (bFailedToExport) {
			$("#consoleStop").addClass("disabled");
			$("#runIcon")[0].classList.replace("fa-sync", "fa-play");			// Toggle play/restart icon
		}
	}

	/**
	 * This function is called wheter we want to build
	 */
	build() {
		this.generateSpec();
		if (project.getCurrentConf() !== undefined)
			this.curProcess = this.launchProcess("java", this.buildArgument() );
		else
			this.writeIntoConsole("Error : no configuration specify", "red");
	}

	/**
	 * This function launch cASTD compiler inside a sub process
	 * The sub process use a python script since compilation
	 * need a lot of stuff to be done to ensure it will actually works
	 */
	compile() {
		var that = this;
		this.build();
		// Opens a Unix socket to receive a JSON object from cASTD containing informations about the
		// ASTD's execution (current states, values of parameters and arguments, etc.)
		var socketfd_path = this.openIPCSocket("exectojson", "cASTD", (data) => {
			// write here the callback function to handle the received data
			var jsonObj = JSON.parse(data);
			window.project.lastJsonObj = jsonObj;
			this.HandleDataFromJson(jsonObj);
			// for tests:
			this.writeIntoConsole("Data received from cASTD:\n" + data, "green");
		});
		this.curProcess.on("exit", function(code) {
			if (code !== 0)
				that.writeIntoConsole("Error code : " + code, "red");

			that.writeIntoConsole("Executable generated in folder : " + that.compiled_exec, "green");
			that.writeIntoConsole("Executable launched, enter event...", "green");

              		var execArgument = [];

			// Run with input file
			if (this.isInput) {
				execArgument.push("-i");
				execArgument.push(this.m_inFilePath);
			}

			// visualization
			execArgument.push("-e");
			execArgument.push(socketfd_path);

			var compExec = that.compiled_exec + "/" + $("#files .active")[0].textContent.toLowerCase();
			that.curProcess = that.launchProcess(compExec, execArgument);
		});

	}

	/**
	 * This function launch iASTD interpreter inside a sub process with given arguments
	 * There is 2 cases:
	 * 		- interactive mode
	 * 	 	- input file mode
	 * 	 		+ Shorthand syntax 	(i.e.   e1() )
	 * 	 		+ JSON syntax		(i.e. 	{"label" : "e1"} )
	 */
	interpret() {
		// Opens a Unix socket to receive a JSON object from iASTD containing informations about the
		// ASTD's execution (current states, values of parameters and arguments, etc.)
		var socketfd_path = this.openIPCSocket("exectojson", "iASTD", (data) => {
			// write here the callback function to handle the received data
				var jsonObj = JSON.parse(data);
				window.project.lastJsonObj = jsonObj;
				this.HandleDataFromJson(jsonObj);
				// for tests:
				this.writeIntoConsole("Data received from iASTD:\n" + data, "green");
			}),

		 selected_astd_name = $("#files .active")[0].textContent,
		    execArgument = ["-s", this.spec_path, "-m", selected_astd_name, "-r", this.iastd_path, "-e", socketfd_path];

		// Run with input file
		if (this.isInput) {
			execArgument.push("-i");
			execArgument.push(this.m_inFilePath);
			execArgument.push("-final");
			if (this.m_inputType === "shorthand") {
				execArgument.push("-shorthandevents");
			}
		// Run interactive
		} else {
			execArgument.push("-vvv");
			execArgument.push("-shorthandevents");
		}

		this.curProcess = this.launchProcess(this.iastd_exec, execArgument);
	}

	/**
	 * This method opens a Unix socket for inter process communication
	 * @param {string} fileDescriptor the name of the unix socket .sock file
	 * @param {Function} onDataCallback a callback function to handle the data received from the ipc socket
	 * @returns {string} the absolute path to the created .sock file
	 */
	openIPCSocket(fileDescriptor, execName, onDataCallback) {
		const that = this,
		 socketfd_path = window.project.rootPath + "/" + fileDescriptor + ".sock";
		var nodeConsole = require("console");
		var myConsole = new nodeConsole.Console(process.stdout, process.stderr);

		// clear leftover .sock file
		if (fs.existsSync(socketfd_path)) {
			fs.unlinkSync(socketfd_path);
		}

		const socketServer = require("net").createServer();

		// keep the Server and socket in an object for cleanup
		that.ipcSocket = {
			server: socketServer,
			socket: null
		};

		socketServer.on("connection", (socket) => {
			myConsole.log("connection established with " + execName);

			// link the callback function to the Socket data event
			socket.on("data", onDataCallback);

			// keep the Socket paired with its Server in an object for cleanup
			that.ipcSocket.socket = socket;
		});

		socketServer.listen(socketfd_path);

		return socketfd_path;
	}

	/**
	 * Function to call to terminate the Unix socket
	 */
	closeIPCSocket() {
		if (this.ipcSocket) {
			if (fs.existsSync(this.ipcSocket.server.address())) {
				fs.unlinkSync(this.ipcSocket.server.address());
			}

			if (this.ipcSocket.socket) {
				this.ipcSocket.socket.destroy();
				this.ipcSocket.server.close();
			}
		}
	}

	/**
	 * Function to call when stop button is called
	 * - Toggle button restart to play
	 * - Clear console content
	 * - Delete temporary spec
	 * - Close Unix socket
	 */
	stop() {
		if (this.isPlaying) {
			$("#consoleStop").addClass("disabled");												// Disable stop button
			$("#runIcon")[0].classList.replace("fa-sync", "fa-play"); 		// Toggle play/restart icon

			// Stop process
			this.isPlaying = false;
			$(".output-console").empty();
			this.curProcess.kill();

			// Delete tmp spec
			if (fs.existsSync(this.spec_path)) {
				fs.unlinkSync(this.spec_path);
			}

			// Close the ipc socket
			this.closeIPCSocket();
		}
	}

	/**
	 * Function to call when calling process
	 * - Toggle button
	 * - Clear console if it's trigger by a restart
	 * - Export spec
	 * - Choose compile or intepret process (cASTD vs iASTD)
 	 */
	play() {
		$("#consoleStop").removeClass("disabled");
		$("#runIcon")[0].classList.replace("fa-play", "fa-sync");			// Toggle play/restart icon

		$(".content > .item").draggable({ disabled: true });

		$("#panel").find("textarea").prop("disabled", window.project.isInPlayMode);
		$("#panel").find("input").prop("disabled", window.project.isInPlayMode);
		$("#panel").find("button").prop("disabled", window.project.isInPlayMode);
		$("#panel").find("select").prop("disabled", window.project.isInPlayMode);

		window.project.qvarValuePerName = new Map();
		window.project.lastTransList = [];

		// if already playing then restart else start
		if (this.isPlaying) {
			$(".output-console").empty();

			$(".content > .item").removeClass("current");
			$(".astd").removeClass("current");
			window.project.currentStateJsonObjList = [];
			window.panel.show();
		} else {
			this.isPlaying = true;
			window.project.isInPlayMode = true;
		}

		// Generate temporary spec file
		this.generateSpec();

		// If compile then call compile proc, otherwise intepret proc
		if (this.isCompile) {
			this.compile();
		} else {
			this.interpret();
		}

	}

	/**
	 * Function to call if you want to hide console
	 */
	closeConsole() {
		if (this.isOpen) {
			this.isInput = false;
			this.isOpen = false;
			this.isCompile = false;
			$("#console").addClass("d-none");
			$("#playground").resizable("destroy");
			$("#playground").height("calc(100% - 88px)");

			window.project.lastJsonObj = null;
			window.project.isInPlayMode = false;

			$(".content > .item").removeClass("current");
			$(".astd").removeClass("current");

			$(".content > .item").draggable({ disabled: false });
			//disable panel
			$("#panel").find("textarea").prop("disabled", window.project.isInPlayMode);
			$("#panel").find("input").prop("disabled", window.project.isInPlayMode);
			$("#panel").find("button").prop("disabled", window.project.isInPlayMode);
			$("#panel").find("select").prop("disabled", window.project.isInPlayMode);
			window.panel.show();
			this.stop();
		}
	}

	/**
	 * Function to call if you want to display console
	 * @param bInputDisabled if true you won't be able to use text box to add event
	 * @param bCompile put to true if it is called by compile
	 */
	openConsole(bInputDisabled, bCompile) {
		this.isInput = bInputDisabled;
		this.isCompile = bCompile;

		// If project not empty open a console
		if (!project.isEmpty()) {
			this.isOpen = true;
			$("#console").removeClass("d-none");
			$("#playground").height( "calc(100% - 300px)" );
			$(".output-console").height("180px");
			$("#consoleInput").prop("disabled", bInputDisabled);

			let consoleStartHeight = 0;
			$("#playground").resizable({
				handles: "s",
				minHeight: 450,
				maxHeight: $("#playground").height(),
				start: function() {
					consoleStartHeight = $(".output-console").height();
				},
				resize: function(ev, ui) {
					$(".output-console").height(consoleStartHeight + ( ui.originalSize.height - ui.size.height ));
				}
			});
		}
	}

	/**
	 * Bind console buttons to respective function
	 */
	bindEvents() {
		var that = this;

		// Bind stop on stop btn
		$("#consoleStop").on("click", this.stop.bind(this));

		// Bind close on console exit button
		$("#consoleExit").on("click", this.closeConsole.bind(this));

		// Bind play/replay on play button
		$("#consolePlay").on("click", this.play.bind(this));

		// Bind enter
		$("#consoleInput").on("keyup", function(ev) {
			if (ev.keyCode === 13) { // enter
				that.curProcess.stdin.write(ev.target.value + "\n");
				ev.target.value = "";
			}
		});
	}

}
