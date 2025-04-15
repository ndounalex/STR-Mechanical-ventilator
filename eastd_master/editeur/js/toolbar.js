jsPlumb.ready(function() {
	window.toolbar = (function() {

		var toolbar = $("#toolbar"),
			selectedItem = null;

		toolbar.draggable();

		toolbar.on("click", ".option", function() {
			if (selectedItem) {
				selectedItem.removeClass("selected");
			}
			selectedItem = $(this);
			selectedItem.addClass("selected");
			if (selectedItem.attr("id") === "Transition") {
				$(".ui-resizable-handle").removeAttr("style").hide();
				$(".content > .item").draggable({ disabled: true });
			} else {
				$(".ui-resizable-handle").show();
				$(".content > .item").draggable({ disabled: false });
			}
		});

		toolbar.find(".option")[0].click();

		return {
			selected: function() {
				return selectedItem.attr("id");
			},
			unselect: function() {
				return toolbar.find(".option")[3].click();
			}
		};
	})();
});