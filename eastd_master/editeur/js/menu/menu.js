jsPlumb.ready(function() {

	// Here we build the contextmenu for project button. It is built only once during application lifecycle and then reused.
	// It must NOT be destroy at any time, otherwise you will have to relaunch the app to get it back.
	// NEVER use $.contextMenu("destroy") without specifying the selector for which context menu must be destroyed
	$.contextMenu({
    position: function (opt) {
      opt.$menu.css({
        top: $(opt.selector)[0].offsetHeight - 5,
        left: $(opt.selector)[0].offsetLeft,
      });
    },
    selector: "#projectBtn",
    trigger: "left",
    hideOnSecondTrigger: true,
    zIndex: 100,
    items: {
      newProject: {
        name: "New",
        icon: "fas fa-folder-plus",
        callback: function () {
          if (!project.isEmpty()) {
            if (confirm("If you create a new project, your current project will be erased.")) {
              ipc.send("open-save-dialog", "path-modify", project.rootPath + "/" + project.name);
            }
          } else {
            ipc.send("open-save-dialog", "path-modify", project.rootPath + "/" + project.name);
          }
        },
      },
      loadProject: {
        name: "Open",
        icon: "fas fa-download",
        callback: function () {
          if (!project.isEmpty()) {
            if (confirm("If you load a project, your current project will be erased."))
              ipc.send("open-file-dialog", "load-project", ["eastd"]);
          } else {
            ipc.send("open-file-dialog", "load-project", ["eastd"]);
          }
        },
      },
      saveProject: {
        name: "Save",
        icon: "fas fa-save",
        callback: function () {
          if (!project.isEmpty()) {
            ipc.send("save", "save-project", project.rootPath + "/" + project.name);
            if (window.project.verify2(true)) ipc.send("save", "export-spec", project.rootPath + "/" + project.name);
          }
        },
      },
      saveasProject: {
        name: "Save as",
        icon: "fas fa-save",
        callback: function () {
          if (!project.isEmpty()) ipc.send("open-save-dialog", "saveas-project", project.rootPath + "/" + project.name);
        },
      },
      verifyProject: {
        name: "Verify",
        icon: "fas fa-check-square",
        callback: function () {
          window.project.verify(false);
        },
      },
      exportTraces: {
        name: "Generate traces",
        className: "export-trace",
        icon: "fas fa-file-code",
        callback: function () {
          project.generateTraces();
        },
      },
      exportToCheddar: {
        name: "Export to Cheddar",
        className: "export-trace",
        icon: "fas fa-upload",
        callback: function () {
          project.generateCheddarFiles();
        },
      },
    },
  });
});