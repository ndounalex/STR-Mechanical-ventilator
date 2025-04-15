jsPlumb.ready(function() {
	window.panel = (function() {

		var element = document.getElementById("panel"),
			_currentItemShow = null;

		$("#panel").resizable({ handles: "e" });

		return {
			selectItem: function(item) {
				if (this.selectedItem === item) {
					this.show(item);
					return;
				}

				if (this.selectedItem) {
					this.selectedItem.unselect();
				}

				this.selectedItem = item;
				item.select();
				this.show(item);
			},

			show: function(item = _currentItemShow) {
				$("#panel > .content").html("");

				if (item) {
					_currentItemShow = item;
					element.getElementsByClassName("content")[0].appendChild(item.toPanel());
				}

				// Trigger a change on textArea to properly recalculate its height on label expend click
				$(".to-expend").click(function() {
					$("#panel").find("textarea").change();
				});
				// Trigger a change on visible textArea of the panel
				$("#panel").find("textarea").change();

				$("#panel").find("textarea").prop("disabled", window.project.isInPlayMode);
				$("#panel").find("input").prop("disabled", window.project.isInPlayMode);
				$("#panel").find("button").prop("disabled", window.project.isInPlayMode);
				$("#panel").find("select").prop("disabled", window.project.isInPlayMode);
				$(".play-mode-select").prop("disabled", false);
			}
		};
	})();
});