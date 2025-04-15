// Modules to control application life and create native browser window
const {
		app, BrowserWindow
	} = require("electron"),
	path = require("path"),
	ipc = require("electron").ipcMain,
	dialog = require("electron").dialog;

var mainWindow;

function createWindow() {
	// Create the browser window.
	mainWindow = new BrowserWindow({
		width: 1920,
		height: 1080,
		webPreferences: {
			preload: path.join(__dirname, "preload.js"),
			enableRemoteModule: false,
			nodeIntegration: true,
			nativeWindowOpen: true
		}
	});
	// and load the index.html of the app.
	mainWindow.loadFile("editeur/index.html");
	// Open the DevTools.
	// mainWindow.webContents.openDevTools();

	// create a new window when the one requested is for printing
	mainWindow.webContents.on("new-window", (event, url, frameName, disposition, options) => {
		if (frameName === "print") {
			event.preventDefault();
			Object.assign(options, {
				modal: true,
				parent: mainWindow,
				width: 1000,
				height: 700
			});
			event.newGuest = new BrowserWindow(options);
		}
	});
}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.whenReady().then(() => {
	createWindow();

	app.on("activate", function() {
		// On macOS it's common to re-create a window in the app when the
		// dock icon is clicked and there are no other windows open.
		if (BrowserWindow.getAllWindows().length === 0) createWindow();
	});
});

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on("window-all-closed", function() {
	if (process.platform !== "darwin") {
		app.quit();
	}
});

// In this file you can include the rest of your app's specific main process
// code. You can also put them in separate files and require them here.

ipc.on("open-file-dialog", function(event, targetChannel, extensions) {

	var files = dialog.showOpenDialogSync(mainWindow, {
		filters: [{
			name: "astd",
			extensions: extensions
		}],
		properties: ["openFile", "multiSelections"]
	});
	if (files)
		event.sender.send(targetChannel, files);
});

ipc.on("dir-select", function(event, defaultDir, targetChannel) {

	var newDirectory = dialog.showOpenDialogSync(mainWindow, {
		properties: ["openDirectory", "createDirectory"],
		defaultPath: defaultDir
	});
	if (newDirectory)
		event.sender.send(targetChannel, newDirectory[0]);
});

ipc.on("open-save-dialog", function(event, targetChannel, defaultPath, extensions) {

	var savePath = dialog.showSaveDialogSync({
		defaultPath: defaultPath,
		filters: [{
			name: "astd",
			extensions: extensions
		}],
		properties: ["createDirectory"]
	});
	if (savePath)
		event.sender.send(targetChannel, savePath);
});

ipc.on("save", function(event, targetChannel, defaultPath) {
	event.sender.send(targetChannel, defaultPath);
});