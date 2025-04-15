jsPlumb.ready(function() {

	window.project = new Project();

	window.id = (function() {
		var current = 0;

		return {
			get: function() {
				return "id_" + current++;
			},
			set: function(id) {
				current = id;
			},
			last: function() {
				return current;
			}
		};
	})();
	// Used to dynamically recalculate height of any textArea on content change
	$(document).on( "change keyup keydown paste cut", "textarea", function() {
		$(this).outerHeight(35).outerHeight(this.scrollHeight);
	}).find( "textarea" ).change();
});