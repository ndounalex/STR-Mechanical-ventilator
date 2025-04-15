jsPlumb.ready(function() {
	// Here we build the contextmenu for manage button. It is built only once during application lifecycle and then reused.
	// It must NOT be destroy at any time, otherwise you will have to relaunch the app to get it back.
	// NEVER use $.contextMenu("destroy") without specifying the selector for which context menu must be destroyed
	$.contextMenu({
		selector: "#manageBtn",
		trigger: "left",
		hideOnSecondTrigger: true,
		position: function(opt) {
			opt.$menu.css({
				top: $(opt.selector)[0].offsetHeight - 5,
				left: $(opt.selector)[0].offsetLeft
			});
		},
		zIndex: 100,
		items: {
			manageImports: {
				name: "Imports",
				icon: "far fa-edit",
				callback: SpecHeader.open.bind(SpecHeader)
			},
			manageTraces: {
				name: "Traces",
				icon: "far fa-edit",
				callback: TraceManager.open.bind(TraceManager)
			}
		}
	});
});