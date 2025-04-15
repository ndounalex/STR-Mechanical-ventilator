window.konsole = (function() {

	$("#konsole").draggable({ handle: ".title" });

	toastr.options.positionClass = "toast-bottom-right";
	toastr.options.progressBar = true;

	var errors = [];
	return {
		errors: function() {
			return errors;
		},

		onlyWarning: function() {
			return errors.every((error) => {
				return error.type === "warning";
			});
		},

		containsErrors: function() {
			return errors.length > 0 && !this.onlyWarning();
		},

		reset: function() {
			errors = [];
		},

		update: function() {
			var html = "";
			for (var i = 0; i < errors.length; i++) {
				var item = errors[i].item.label === "" ? "empty" : errors[i].item.label;
				html += "<div class='" + errors[i].type + "'>" + errors[i].type.toUpperCase() + ": " +
					"<a href='#' onclick=\"window.konsole.click(" + i + ")\">" + item + "</a> " + errors[i].message + "</div>";
			}
			$("#konsole .content").html(html);
		},

		_log: function(file, item, message, type) {
			errors.push({
				file,
				item,
				message,
				type
			});
		},

		log_error: function(file, item, message) {
			this._log(file, item, message, "error");
		},

		log_warning: function(file, item, message) {
			this._log(file, item, message, "warning");
		},

		show: function() {
			this.update();

			if (errors.length === 0) {
				toastr.success("No error(s) found!");
			} else {
				toastr.error(errors.length + " error(s) or warning(s) found!");
				$("#konsole").show();
			}
		},

		show2: function() {
			this.update();

			if (errors.length === 0) {
				toastr.success("No error(s) found!");
			} else {
				toastr.error(errors.length + " error(s) or warning(s) found! Use the option verify to get more informations about these");
			}
		},

		hide: function() {
			$("#konsole").hide();
		},

		click: function(index) {
			if (errors[index].file.constructor.name === "Link" || errors[index].file.constructor.name === "State") {
				window.project.selectTransition(errors[index].file);
			} else {
				window.project.selectFile(errors[index].file.getFile());
				window.panel.selectItem(errors[index].item);
			}
		}
	};
})();