jsPlumb.ready(function() {

	// Current console holder
	let consoleUI = null;

	// Part of the Nodejs death library,
	// sets an event listener for SIGINT, SIGQUIT and SIGTERM
	// commit with visualisation branch but bugged
	/*var onDeath = require('death');
	onDeath(function(signal, error) {
		if (consoleUI) {
			consoleUI.stop();
		}
	});
	*/

	// Fonction to call to build and run
	function compile() {
		if (!consoleUI) {
			consoleUI = new Console();
			window.project.consoleUI = consoleUI;
		}
		consoleUI.openConsole(false, true);
		consoleUI.play();
	}

	// Listener input file
	ipc.on("load-input", function(event, files) {
		consoleUI.inFilePath = files;
		consoleUI.openConsole(true, false);
		consoleUI.play();
	});

	// Fonction to call with input file
	function fileMenu(type) {
		if (!consoleUI) {
			consoleUI = new Console();
			window.project.consoleUI = consoleUI;
		}
		consoleUI.inputType = type;
		ipc.send("open-file-dialog", "load-input"); // Open dialog
	}

	// Bind events on menu icon : build
	$("#build").on("click", function() {
		if (!consoleUI) {
			consoleUI = new Console();
			window.project.consoleUI = consoleUI;
		}
		consoleUI.openConsole(false, true);
		consoleUI.build();
	});

	// Bind events on menu icon : run
	$("#run").on("click", compile);

	// Bind Menu to target event
	$.contextMenu({
		position: function(opt) {
			opt.$menu.css({
				top: $(opt.selector)[0].offsetHeight - 5,
				left: $(opt.selector)[0].offsetLeft
			});
		},
		selector: "#runBtn",
		trigger: "left",
		zIndex: 100,
		items: {
			runInteractive: {
				name: "Run iASTD - interactive",
				icon: "fas fa-play",
				callback: function() {
					if (!consoleUI) {
						consoleUI = new Console();
						window.project.consoleUI = consoleUI;
					}
					consoleUI.openConsole(false, false);
					window.project.isInPlayMode = true;
					consoleUI.play();
				}
			},
			runInput: {
				name: "Run iASTD with file...",
				icon: "fas fa-file-upload",
				items: {
					"inputNormal": {
						name: "Json syntax",
						icon: "fas fa-code",
						callback: fileMenu.bind(this, "json")
					},
					"inputShorthand": {
						name: "Shorthand syntax",
						icon: "fas fa-terminal",
						callback: fileMenu.bind(this, "shorthand")
					}
				}
			},
			compile: {
				name: "Run cASTD - interactive",
				icon: "fas fa-play",
				callback: compile.bind(this)
			},
			Runcompiled: {
				name: "Run cASTD with file...",
				icon: "fas fa-file-upload",
				items: {
					"inputNormal": {
						name: "Json syntax",
						icon: "fas fa-code",
						callback: fileMenu.bind(this, "json")
					},
					"inputShorthand": {
						name: "Shorthand syntax",
						icon: "fas fa-terminal",
						callback: fileMenu.bind(this, "shorthand")
					}
				}
			},
			Runcompiledfile: {
				name: "Compile",
				icon: "fas fa-hammer",
				callback: function() {
					if (!consoleUI)
						consoleUI = new Console();
					consoleUI.openConsole(false, true);
					consoleUI.build();
					//comoile.bind(this);
				}
			},
			close: {
				name: "Close console",
				icon: "fas fa-times",
				callback: function() {
					if (consoleUI)
						consoleUI.closeConsole();
					window.project.isInPlayMode = false;
				}
			}
		}
	});
});