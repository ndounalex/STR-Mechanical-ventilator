class SchemaStructure {
	constructor(classes = {}, incompleteDefs = []) {
		this.ajv = new Ajv();
		this.classes = classes;
		this.incompleteDefs = incompleteDefs;
		this.warn = "";
	}

	// add a schema to the schema instance
	addSchema(schema) {
		if (schema.$id) {
			this.ajv.addSchema(schema); // throws an error if already exists and catched in specHeader.js
			this.updateTypesStructure(schema);
			if (this.warn) {
				toastr.warning(this.warn, "Schema '" + schema.$id + "' imported with warning", { showDuration: 1000 });
				this.warn = "";
			}
		}
		return !!schema.$id;
	}

	removeSchema(schemaId) {
		//@todo: Maybe check if the type is used before deletion and don't do it with a warning if it is the case
		this.ajv.removeSchema(schemaId);
		delete this.classes[schemaId];
	}

	// Utility function to define a property with a given value
	static addProperty(object, propertyName, value) {
		Object.defineProperty(object, propertyName, {
			value: value,
			enumerable: true,
			configurable: true
		});
	}

	/**
	 * Updates a simplified representation of a json schema to easily validate an instance property call.
	 * (E.g. toto.property1.prop or toto.prop[0].prop1 are valid calls)
	 * @param schema: current schema object
	 */
	updateTypesStructure(schema) {
		var realValue = schema; // if items exists, then it is an array, manage like regular property, but from items
		while (realValue.items) {
			realValue = realValue.items;
		}
		if (schema.allOf) {
			SchemaStructure.addProperty(this.classes, schema.$id, this.getStructure(schema, schema.allOf[schema.allOf.length - 1].properties));
			this.assignInheritedProperties(schema);
		} else if (realValue.type === "object") {
			SchemaStructure.addProperty(this.classes, schema.$id, this.getStructure(schema, realValue.properties));
		} else {
			SchemaStructure.addProperty(this.classes, schema.$id, realValue.type);
		}

		// for object inheritance ('allOf' keyword in json schema), fill objects with proper schema and consider completed so remove from array
		this.incompleteDefs = this.incompleteDefs.filter((elem) => {
			if (elem.ref === schema.$id) {
				Object.assign(this.classes[elem.id], this.classes[elem.ref]);
			}
			return elem.ref !== schema.$id;
		});
	}

	/**
	 * For each schema reference, add it's properties to the current schema if the source schema is already imported.
	 * Otherwise, wait for the referenced schema to be imported to add its properties
	 * @param schema: current schema object
	 */
	assignInheritedProperties(schema) {
		for (var i = 0; i < schema.allOf.length - 1; i++) {
			var refName = schema.allOf[i].$ref.substring(0, schema.allOf[i].$ref.length - 1);
			if (this.classes[refName]) {
				Object.assign(this.classes[schema.$id], this.classes[refName]);
			} else {
				this.incompleteDefs.push({
					id: schema.$id,
					ref: refName
				});
				this.warn = "You must import the schema with name '" + refName + "' before using this type.";
			}
		}
	}

	/**
	 * Recursive function building an object based on the structure of a given schema and returning the resulting object.
	 * @param schema: current schema object
	 * @param prop: properties of the current object we are parsing from the schema
	 * @param currentDefinition: name of the definition we are in. Used to determine if we are building the structure of a definition and
	 * 							 detect if the given definition is recursive to stop the calls and avoid infinite loop.
	 * @returns {Object}
	 */
	getStructure(schema, prop, currentDefinition) {
		var obj = {};
		for (let [key, value] of Object.entries(prop)) {
			while (value.items) { // if items exists, then it is an array, go deeper until getting ultimate type
				value = value.items;
			}
			if (value.$ref) {
				var path = value.$ref.split("/");
				// check if the definition is recursive, if so, do not parse again
				if (currentDefinition && path[path.length - 1] === currentDefinition) {
					SchemaStructure.addProperty(obj, key, currentDefinition);
				} else {
					SchemaStructure.addProperty(obj, key, this.parseDefinition(schema, path));
				}
			} else if (value.type === "object") {
				SchemaStructure.addProperty(obj, key, this.getStructure(schema, value.properties));
			} else {
				SchemaStructure.addProperty(obj, key, value.type);
			}
		}
		return obj;
	}

	/**
	 * Parse the path of a definition reference and get the structure if the definition is inside the current schema.
	 * Otherwise, it refers to an external schema. If it is not imported yet, add a null property with the referenced definition name.
	 * @param schema: current schema object
	 * @param path: array representing the imbrication to the referenced object definition
	 * @returns {string} Always return the name of the definition so we don't have to set the reference later, the user only need to import it.
	 */
	parseDefinition(schema, path) {
		if (path[0] === "#" && !this.classes[path[path.length - 1]]) { // internal schema reference
			if (schema[path[1]][path[2]].properties) // definition is an object
				SchemaStructure.addProperty(this.classes, path[path.length - 1], this.getStructure(schema, schema[path[1]][path[2]].properties, path[path.length - 1]));
			else // definition is a basic type
				SchemaStructure.addProperty(this.classes, path[path.length - 1], schema[path[1]][path[2]].type);
		} else if (!this.classes[path[0].substring(0, path[0].length - 1)]) { // external schema reference not imported yet
			SchemaStructure.addProperty(this.classes, path[path.length - 1], null);
			this.warn = "You must import a valid schema with definition '" + path[path.length - 1] + "' before using this type";
		}
		return path[path.length - 1];
	}

	/**
	 * Check if the given object has properties that are not valid for the given type in the custom structure
	 * @param dataObject : object data to validate
	 * @param type: schema id representing the expected type of the data
	 * @param tokens: Array representing the path to the current property. Empty if at the root object
	 * @returns {boolean}
	 */
	hasNoUnknownProperties(dataObject, type, tokens = []) {
		for (let key of Object.keys(dataObject)) {
			if (dataObject[key] instanceof Object && !(dataObject[key] instanceof Array) && !$.isEmptyObject(dataObject[key])) {
				if (!this.hasNoUnknownProperties(dataObject[key], type, tokens.concat([key]))) {
					return false;
				}
			} else if (dataObject[key] instanceof Array && dataObject[key][0] instanceof Object) {
				if (!this.hasNoUnknownProperties(dataObject[key][0], type, tokens.concat([key]))) {
					return false;
				}
			} else if (!this.propertyExists(type, tokens.concat([key]))) {
				this.ajv.errors = [{ message: "Property '" + key + "' doesn't exist for type '" + type + "'." }];
				return false;
			}
		}
		return true;
	}

	/**
	 * Iterates over the custom types structure to see if the property exists for the given type
	 * @param baseType : type of the object we want to validate against
	 * @param tokens : Array representing the path to the property. (e.g. [prop1, prop2] for path baseType.prop1.prop2
	 * @returns {boolean} true if the property exists, false if any token refers to an undefined property
	 */
	propertyExists(baseType, tokens) {
		var iterator = this.classes[baseType],
			i = 0;
		while (iterator && i < tokens.length) {
			if (this.classes[iterator[tokens[i]]]) {
				iterator = this.classes[iterator[tokens[i]]];
			} else {
				iterator = iterator[tokens[i]];
			}
			i++;
		}
		return !!iterator;
	}

	validate(schemaId, data) {
		return this.ajv.validate(schemaId, data) && this.hasNoUnknownProperties(data, schemaId);
	}

	delete() {
		this.classes = {};
		this.incompleteDefs = [];
		project.types.filter((type) => {
			return type.type === "complex";
		}).forEach((schema) => {
			this.ajv.removeSchema(schema.name);
		});
	}

	get error() {
		return this.ajv.errors[0];
	}
}