class Project {

	constructor(files = []) {
		this.e_files = document.getElementById("files");
		this.e_playgrounds = document.getElementById("playground");

		this.isInPlayMode = false;
		this.currentStateJsonObjList = [];
		this.lastJsonObj;
		this.lastTransList = [];
		this.qvarValuePerName = new Map();

		this.files = files;
		this.currentFile;
		this.filesID = 1;
		this.complexStructure = new SchemaStructure();
		this.types = [];
		this.types.push({
			name: "int",
			type: "basic"
		});
		this.types.push({
			name: "bool",
			type: "basic"
		});
		this.types.push({
			name: "string",
			type: "basic"
		});
		this.types.push({
			name: "float",
			type: "basic"
		});
		this.eventSignatures = [];
		this.moduleImports = [];
		this.targetLanguage = "C++";
		this.listExportOfParametrizeCallSubASTDs = [];
		this.traces = [];
		this.config = [];
		this.setProjectEnv();
		this.consoleUI;
		window.copyPaste_label = "Copy";
	}

	setProjectEnv(rootPath = __dirname, name = "Untitled") {
		this.rootPath = rootPath;
		this.name = name;
		$("#projectPathName").text("Name: " + name);
		$("#projectPathName").attr("title", "Project folder: " + rootPath + ", Name: " + name);
	}

	get files() {
		return this.m_files;
	}

	set files(files) {
		this.m_files = files;
	}

	get filesID() {
		return this.m_filesID;
	}

	set filesID(filesID) {
		this.m_filesID = filesID;
	}

	get currentFile() {
		return this.m_currentFile;
	}

	set currentFile(file) {
		this.m_currentFile = file;
		this.refreshConsoleCurrentState();
	}

	// add a schema to the schema validator instance and add the new type in the project types list
	addSchema(schema, path) {
		if (this.complexStructure.addSchema(schema)) {
			this.types.push({
				name: schema.$id,
				path: path,
				type: "complex"
			});
			window.panel.show();
		}
	}

	removeSchema(id) {
		this.complexStructure.removeSchema(id);
		this.types = this.types.filter((type) => {
			return type.name !== id;
		});
	}

	// Change the saved path of the given schema (id) to an absolute path or relative and return the result to the view
	toggleSchemaRelativePath(id) {
		return this.togglePath(this.types, "name", id);
	}

	// check if the type value is of the given type (complex, native or basic)
	isGivenType(givenType, typeValue) {
		return project.types.filter((type) => {
			return type.type === givenType;
		}).some((type) => {
			return typeValue === type.name;
		});
	}

	// check if the type is a basic type or not
	isBaseType(typeToCheck) {
		return this.isGivenType("basic", typeToCheck);
	}

	// check if the type is a complex type or not
	isComplexType(typeToCheck) {
		return this.isGivenType("complex", typeToCheck);
	}

	// check if the type is a native type or not and return a boolean
	isNativeType(typeToCheck) {
		return this.isGivenType("native", typeToCheck);
	}

	// check if the given type exists in the project
	isValidType(typeName) {
		return this.types.some((type) => {
			return type.name === typeName;
		});
	}

	newFile() {
		var file = new File(this);
		file.astd = new ASTD(window.id.get(), file, 10, 2, 910, 750);
		file.astd.label = "Unnamed" + window.project.filesID;
		window.project.filesID++;
		window.panel.show(file.astd);
		file.astd.sortableTabs();

		return file;
	}

	addFile(file) {
		this.e_files.insertBefore(file.e_file, this.e_files.lastElementChild);
		this.e_playgrounds.appendChild(file.e_playground);
		this.files.push(file);
		this.selectFile(file);
	}

	removeFile(file) {
		if (file) {
			for (var i = 0; i < this.files.length; i++) {
				if (this.files[i] === file) {
					this.files.splice(i, 1);
					break;
				}
			}
			// manually remove contextMenu so it is built again even if we load a file with the same e_file.id value
			$.contextMenu("destroy", `#${file.e_file.id}`);
			this.e_files.removeChild(file.e_file);
			this.e_playgrounds.removeChild(file.e_playground);
		}
		if (this.files.length === 0) {
			window.panel.show(null);
		}
	}

	selectFile(fileSelected) {
		this.files.forEach((file) => {
			this.unselectFile(file);
		});
		fileSelected.select();
		this.currentFile = fileSelected;
	}

	unselectFile(file) {
		if (file) {
			file.unselect();
		}
	}

	selectTransition(transition) {
		window.panel.show(transition);
	}

	delete() {
		var files = this.files.slice(0);

		for (var i = 0; i < files.length; i++) {
			files[i].delete(true);
		}
		this.complexStructure.delete();
		this.types.splice(4);
		this.eventSignatures = [];
		this.moduleImports = [];
		this.targetLanguage = "C++";
		this.traces = [];
		$("#specHeaderModal").remove();
		$("#traceModal").remove();
	}

	save() {
		var project = {
			id: window.id.last(),
			schemas: this.types.filter((type) => {
				return type.type === "complex";
			}).map((schema) => {
				return schema.path;
			}),
			native_types: this.types.filter((type) => {
				return type.type === "native";
			}),
			imports: this.moduleImports,
			eventSignatures: this.eventSignatures.map((eventSign) => {
				return {
					path: eventSign.path,
					id: eventSign.id
				};
			}),
			target: this.targetLanguage,
			files: [],
			traces: this.traces,
			config: this.config
		};

		this.files.forEach(function(file) {
			project.files.push(file.save());
		});

		return project;
	}

	load(project) {
		this.delete();

		window.id.set(project.id);
		this.targetLanguage = project.target;
		this.types = this.types.concat(project.native_types);

		project.schemas.forEach((schemaPath) => {
			try {
				this.addSchema(this.getJSONFileContent(schemaPath), schemaPath);
			} catch (error) {
				toastr.error(error.message);
			}
		});

		project.eventSignatures.forEach((eventSign) => {
			try {
				const content = this.getJSONFileContent(eventSign.path);
				if (!Validator.ajv.validate("__event_signature__", content)) throw new Error("The file '" + eventSign.path + "'is not valid. It should only contains an object with string arrays.");
				this.addEventSignatures(content, eventSign.path, eventSign.id);
			} catch (error) {
				toastr.error(error.message);
			}
		});

		project.imports.forEach((moduleImport) => {
			var resolvedPath = path.isAbsolute(moduleImport.path) ? moduleImport.path : this.rootPath + "/" + moduleImport.path;
			if (fs.existsSync(resolvedPath)) this.moduleImports.push(moduleImport); else toastr.error("Unable to find the file with path '" + moduleImport.path + "'. It must be added via the Manage Imports.");
		});

		$("#optConfig").empty();
		$("#optConfig").append($("<option>", {
			value: "addConf",
			text: "No Configuration"
		}));
		this.traces = project.traces;
		this.config = project.config;
		if (this.config !== undefined) {
			this.config.forEach((conf) => {
				$("#optConfig").append($("<option>", {
					value: conf.name,
					text: conf.name
				}));
			});
		}
		/*
		if (this.targetLanguage === "C++") {
		}*/

		for (var i = 0; i < project.files.length; i++) {
			var file = new File(this);
			file.load(project.files[i]);
		}
		this.selectFile(this.files[0]);
	}

	getJSONFileContent(filePath) {
		var resolvedPath = path.isAbsolute(filePath) ? filePath : this.rootPath + "/" + filePath;
		return JSON.parse(fs.readFileSync(resolvedPath));
	}

	getJsonObjByType(obj, p_type, p_name) {
		if (obj.type === "QSynchronization") {
			if (p_type === obj.type && p_name === obj.name) {
				return obj;
			} else {
				var i;
				for (i = 0; i < obj.sub_states.length; i++) {
					var result = this.getJsonObjByType(obj.sub_states[i], p_type, p_name);
					if (result !== null) {
						return result;
					}
				}
			}
		} else if (obj.current_sub_state.type === p_type && obj.current_sub_state.name === p_name) {
			return obj.current_sub_state;
		} else if (obj.current_sub_state.type === SYNCHRONIZATION || obj.current_sub_state.type === FLOW) {
			var rightResult = this.getJsonObjByType(obj.current_sub_state.right, p_type, p_name);
			if (rightResult !== null) {
				return rightResult;
			}
			var leftResult = this.getJsonObjByType(obj.current_sub_state.left, p_type, p_name);
			if (leftResult !== null) {
				return leftResult;
			}
		} else if (obj.current_sub_state.type === CALL) {
			return this.getJsonObjByType(obj.current_sub_state.called_astd, p_type, p_name);
		} else if (obj.current_sub_state.type !== "Elem") {
			return this.getJsonObjByType(obj.current_sub_state, p_type, p_name);
		}
		return null;
	}

	refreshConsoleCurrentState() {
		if (this.isInPlayMode && this.consoleUI !== undefined) {
			this.consoleUI.HandleDataFromJson(this.lastJsonObj, false);
		}
	}

	verifyNameUnicity() {
		let allASTD = window.project.files.map((file) => {
			let astds = [];
			astds.push(file.astd);
			let items = file.astd.items.filter((item) => {
				return item.constructor.name === "ASTD";
			});
			astds.push(items);
			astds = astds.flat();
			return astds;
		});
		// eslint-disable-next-line no-unused-vars
		allASTD = allASTD.flat();

		let allASTD_name = allASTD.map((e) => {
				return e.label;
			}),
			allUniqueArray = allASTD.filter(function(item, pos) {
				return allASTD_name.indexOf(item.label) !== pos;
			});

		if (allUniqueArray.length !== 0) {
			allUniqueArray.forEach((astd) => {
				window.konsole.log_error(this, astd, " is not unique in projet");
			});
		}
	}

	verify(bConfirmationNeeded) {
		window.konsole.reset();
		window.project.files.forEach(function(oneFile) {
			oneFile.verify();
		});

		this.verifyNameUnicity();

		if (bConfirmationNeeded && window.konsole.onlyWarning() && window.konsole.errors().length > 0) {
			var keepGoing = confirm("Your eASTD contains only warnings. Do you want to continue?");
			if (!keepGoing) {
				window.konsole.show();
				return false;
			}
		} else {
			window.konsole.show();
		}

		return !window.konsole.containsErrors();
	}

	verify2(bConfirmationNeeded) {
		window.konsole.reset();
		window.project.files.forEach(function(oneFile) {
			oneFile.verify();
		});

		this.verifyNameUnicity();

		if (bConfirmationNeeded && window.konsole.onlyWarning() && window.konsole.errors().length > 0) {
			var keepGoing = confirm("Your eASTD contains only warnings. Do you want to continue?");
			if (!keepGoing) {
				window.konsole.show2();
				return false;
			}
		} else {
			window.konsole.show2();
		}

		return !window.konsole.containsErrors();
	}

	export() {
		// Validation export
		if (window.project.files.length <= 0) {
			alert("No ASTDs to export.");
			return;
		}

		// Export
		var L = {
			target: this.targetLanguage.toLowerCase(),
			imports: this.moduleImports.map((moduleImport) => {
				return moduleImport.path;
			}),
			type_definitions: {
				schemas: this.types.filter((type) => {
					return type.type === "complex";
				}).map((type) => {
					return type.path;
				}),
				native_types: {},
				events: this.eventSignatures.map((eventSign) => {
					return eventSign.path;
				})
			},
			top_level_astds: [],
			conf: []
		};

		this.types.filter((type) => {
			return type.type === "native";
		}).forEach((nativeType) => {
			L.type_definitions.native_types[nativeType.name] = {
				type_symbol: nativeType.symbol,
				destructor: nativeType.destructor
			};
		});

		this.listExportOfParametrizeCallSubASTDs = [];
		for (var i = 0; i < project.files.length; i++) {
			if (!project.files[i].astd.externalTest) {
				L.top_level_astds.push(project.files[i].astd.export([]).top_level_astds[0]);
			}
		}
		this.listExportOfParametrizeCallSubASTDs.forEach((element) => {
			return L.top_level_astds.push(element.top_level_astds[0]);
		});

		return JSON.stringify(L);
	}

	addEventSignatures(result, path, id) {
		this.eventSignatures.push({
			id: id,
			path: path,
			signatures: result
		});
	}

	removeEventSignatures(id) {
		this.eventSignatures = this.eventSignatures.filter((eventSign) => {
			return eventSign.id !== id;
		});
	}

	// Change the saved path of the given event signature (id) to an absolute path or relative and return the result to the view
	toggleEventSignRelativePath(id) {
		return this.togglePath(this.eventSignatures, "id", id);
	}

	/**
	 * Toggle between an absolute and relative path assign the resulted path to the given object
	 * @param arrayToModify: Array where to search for the element path to modify
	 * @param propertyToCompare: property of the element path object to compare against to find the right object to modify
	 * @param idToFind: id the component associated to the path element we are modifying
	 * @returns {string}: resulting path
	 */
	togglePath(arrayToModify, propertyToCompare, idToFind) {
		var pathToModify = arrayToModify.find((element) => {
			return element[propertyToCompare] === idToFind;
		});

		pathToModify.path = path.isAbsolute(pathToModify.path) ? path.relative(this.rootPath, pathToModify.path) : path.resolve(this.rootPath, pathToModify.path);
		return pathToModify.path;
	}

	addModule(path, id) {
		var extension = path.split(".");
		if (this.moduleImports.every((moduleImport) => {
			return moduleImport.path !== path || moduleImport.id !== id;
		}) && extension[extension.length - 1] === "ml" || extension[extension.length - 1] === "cpp") {
			this.moduleImports.push({
				id: id,
				path: path
			});
			return true;
		}
		return false;
	}

	removeModule(id) {
		var index = this.moduleImports.findIndex((moduleImport) => {
			return moduleImport.id === id;
		});
		if (index !== -1) this.moduleImports.splice(index, 1);
	}

	// Change the saved path of the given module import (id) to an absolute path or relative and return the result to the view
	toggleModuleRelativePath(id) {
		return this.togglePath(this.moduleImports, "id", id);
	}

	addNativeType(id) {
		this.types.push({
			name: "",
			type: "native",
			id: id,
			symbol: "",
			destructor: ""
		});
	}

	removeNativeType(id) {
		this.types = this.types.filter((type) => {
			return type.id !== id; // if type.id is undefined, will return true
		});
	}

	getNativeType(id) {
		return this.types.find((type) => {
			return type.id === id;
		});
	}

	isEmpty() {
		return this.files.length === 0 && this.types.length === 4 && this.moduleImports.length === 0 && this.eventSignatures.length === 0;
	}

	/**
	 * =======================================================
	 * 					Traces Section
	 * =======================================================
	 */

	addTrace(traceObj) {
		this.traces.push(traceObj);
	}

	removeTrace(traceObj) {
		var index = this.traces.indexOf(traceObj);
		if (index !== -1) this.traces.splice(index, 1);
	}

	BellmanFord(states, actions){
		let initialState = null;
		let finalStates = [];
		const distance = {};
		const precedence = {}
		for(const state in states){
			if(states[state].final)finalStates.push(state);
			if(!initialState && states[state].initial){initialState=state;}
			distance[state]=null;
			precedence[state]={pred:null, ...states[state]};
		}
		distance[initialState]=0;
		for(let i=0;i<Object.keys(states).length;i++){
			for(const action of actions){
				const weight =
          -1 * ((action.constraints.delay ? action.constraints.delay.delay : 0) +
          (action.constraints.timeout ? action.constraints.timeout.duration : 0));
				// console.log("============== actions =============== ", {action, weight});
				if(distance[action.from]==null)continue;
				if((distance[action.to]==null)||((distance[action.from]+weight)<distance[action.to])){
					distance[action.to]=distance[action.from]+weight;
					precedence[action.to] = {
            ...precedence[action.to],
            pred: action.from,
            constraints: { ...action.constraints },
            action: action.name,
          };
				}
			}
		}
		let maxDistance = null;
		for(const finalItem of finalStates){
			if(maxDistance==null){maxDistance = {state:finalItem, distance:distance[finalItem]};continue;}
			if(maxDistance.distance>distance[finalItem]){
				maxDistance = { state: finalItem, distance: distance[finalItem] };
			}
		}
		let maxPath = [];
		let currentTrans = precedence[maxDistance.state];
		maxPath.push({...currentTrans});
		while(currentTrans.pred){
			currentTrans = precedence[currentTrans.pred];
			maxPath = [{ ...currentTrans }, ...maxPath];
		}
		return maxPath;
	}

	getConstraints(constraints, transition, solution){
		for (const constraintItem of constraints) {
			if (constraintItem.allTransitions.includes(transition)) {
				if (constraintItem.delay) {solution.delay.push({ delay: constraintItem.delay, unit: constraintItem.delay_unit })};
				if (constraintItem.duration){
					solution.duration.push({ duration: constraintItem.duration, unit: constraintItem.duration_unit });
				}
			}
		}
	}

	extractPathExecution(executionTree, parent, solution, constraints, last, top, allTransitions, parentName, states) {
		if(Object.keys(executionTree.child||{}).length==0){
			let index=0;
			if(executionTree.from && !executionTree[executionTree.from]){
				solution.name = executionTree.transition;
				solution.to = executionTree.to;
				solution.from = executionTree.from;
				solution.next = [...last];
				solution.delay = [];
				solution.duration = [];
				this.getConstraints(constraints, executionTree.transition, solution);
				const trans = { ...solution };
        		delete trans.next;
				if(solution.name!=="Step"){
					
					const { delay, timeout } = this.computeTransitionWeight(trans);
					/* const durationTrans = (delay?delay.delay:0)+(timeout?timeout.duration:0);
					trans.weight = {duration:durationTrans, unit:(delay||timeout).unit};*/
					trans.constraints = {delay, timeout};
					if(timeout){
						trans.name = `Step_${trans.name}`;
						if(!delay || (delay.delay<timeout.duration)){
							trans.constraints.delay = {delay:(delay?delay.delay:0)+timeout.duration, unit:timeout.unit};
							trans.constraints.timeout = null;
						}
					}
					trans.to = `${trans.to}_${parentName || ""}`;
					trans.from = `${trans.from}_${parentName || ""}`;
					trans.delay=delay;
					delete trans.duration;
					trans.timeout=timeout;
					allTransitions.push(trans);
					if (!states[trans.from]) states[trans.from] = {};
					if (!states[trans.to]) states[trans.to] = {};
					states[trans.from] = {
						initial: states[trans.from].initial || executionTree.isInitial,
						final: states[trans.from].final || false,
					};
					states[trans.to] = {
						initial: states[trans.to].initial || false,
						final: states[trans.to].final || executionTree.isFinal,
					};
				}
				/* states[trans.to] = states[trans.to]||executionTree.isFinal;
				states[trans.from]=states[trans.from]||false;*/


				


				for(const itemLast of last){
					if(itemLast.name=="Step")continue;
					const tempItem = {...itemLast};
					tempItem.from=trans.from;
					const { delay, timeout } = this.computeTransitionWeight(tempItem);
					/*const durationTrans = (delay ? delay.delay : 0) + (timeout ? timeout.duration : 0);
					tempItem.weight = { duration: durationTrans, unit: (delay || timeout).unit };*/
					tempItem.constraints = {delay,timeout}
					if (timeout) {
						tempItem.name = `Step_${trans.name}`;
						if (!delay || delay.delay < timeout.duration) {
							tempItem.constraints.delay = (delay ? delay.delay : 0) + timeout.duration;
							tempItem.constraints.timeout = null;
						}
					}
					if(!states[tempItem.to])states[tempItem.to]={};
					states[tempItem.to].final=true;
					delete tempItem.next;
					tempItem.delay=delay;
					delete tempItem.duration;
					tempItem.timeout=timeout;
					allTransitions.push(tempItem);
				}
			}else if(executionTree.from){
				/* if (executionTree[executionTree.from].length == 0) {
					//solution[index].next.push(lastItem);
					const lastItem = { name: executionTree.transition, next: [], delay: [], duration: [] };
          			this.getConstraints(constraints, executionTree.transition, lastItem);
					if (solution.next) {
						solution.next.push(lastItem);
					} else if (Array.isArray(solution)) {
						solution.push(lastItem);
					} else {
						solution = lastItem;
					}
					}*/
			}
			const children = executionTree[executionTree.from]||executionTree.path
			for(const pathItem of children){
				if(pathItem[pathItem.from]){
					const lastItem = {name: pathItem.transition, next:[], delay:[], duration:[], to:`${pathItem.to}_${parentName||""}`, isFinal:pathItem.isFinal};
					this.getConstraints(constraints, pathItem.transition, lastItem);
					/* const currentItem = {	
						name : null,
						next : [],
						delay: [],
						duration: []
					};*/
					/* solution[index]={};
					solution[index].name = null;
					solution[index].next = [];
					solution[index].delay = [];
					solution[index].duration = [];*/
					const nextItem = [];
					this.extractPathExecution(
						pathItem, 
						executionTree, 
						nextItem, 
						constraints, [...last, { ...lastItem }], 
						null, allTransitions,
						parentName?`${parentName}_${pathItem.from}`:pathItem.from,
						states
						);
					if (solution.next) {
						solution.next.push(nextItem);
					} else if (Array.isArray(solution)) {
						solution.push(nextItem);
					} else {
						solution = nextItem;
					}
					/* for (const subPath of pathItem[pathItem.from]) {
						const nextItem = {};
						this.extractPathExecution(subPath, pathItem, nextItem, constraints, [...last,{...lastItem}], null);
						if(solution.next){
							solution.next.push(nextItem)
						}else if(Array.isArray(solution)){
							solution.push(nextItem);
						}else{
							solution = nextItem;
						}
						// solution[index].next.push(nextItem);
					}*/
					if(pathItem[pathItem.from].length==0){
						//solution[index].next.push(lastItem);
						if (solution.next) {
							solution.next.push(lastItem);
						} else if (Array.isArray(solution)) {
							solution.push(lastItem);
						} else {
							solution = lastItem;
						}
						const tempItem = {...lastItem};
						tempItem.from = parentName?`${parentName}_${pathItem.from}`:pathItem.from;
						delete tempItem.next;
						if(!states[tempItem.from])states[tempItem.from] ={}
						if(!states[tempItem.to])states[tempItem.to] ={}
						states[tempItem.from] = {
							initial: states[tempItem.from].initial || tempItem.isInitial,
							final: states[tempItem.from].final || false,
						};
						states[tempItem.to] = {
						initial: states[tempItem.to].initial || false,
						final: states[tempItem.to].final || tempItem.isFinal,
						};
						delete tempItem.delay;
						delete tempItem.duration;
						allTransitions.push(tempItem);
					}
				}else{
					/* solution[index]={};
					solution[index].name = pathItem.transition;
					solution[index].to = pathItem.to;
					solution[index].from = pathItem.from;
					solution[index].next = [...last];
					solution[index].delay = [];
					solution[index].duration = [];
					this.getConstraints(constraints, pathItem.transition, solution[index]);*/
					/*for(const constraintItem of constraints){
						if(constraintItem.allTransitions.includes(pathItem.transition)){
							if(constraintItem.delay)solution.delay[{duration:constraintItem.delay, unit:constraintItem.delay_unit}]
							if(constraintItem.duration)solution.duration[{ duration: constraintItem.delay, unit: constraintItem.delay_unit }];
						}
					}*/
					const nextItem = {};
					this.extractPathExecution(pathItem, executionTree, nextItem, constraints, last, null, allTransitions, parentName, states);
          			if (solution.next) {
						solution.next.push(nextItem);
					} else if (Array.isArray(solution)) {
						solution.push(nextItem);
					} else {
						solution = nextItem;
					}
					/* for(const subPath of pathItem.path){
						const nextItem={}
						this.extractPathExecution(subPath, pathItem,nextItem, constraints, last, null);
						solution[index].next.push(nextItem);
					}*/
				}
				index++;
			}
			let linkToArray = null;
			if(Array.isArray(solution)){
				linkToArray = solution;
			}else if(solution.next){
				linkToArray = solution.next;
			}
			// if(Array.isArray(solution)){
				for(const solutionItem of linkToArray){
					if(solutionItem.name=="Step"){
						const multipleStep=[];
						let multipleStepIndex=0;
						for(const action of linkToArray){
							if((action.name!=="Step") && (action.from==solutionItem.from) && (action.to!=solutionItem.to)){
								if(action.duration.length){
									const { delay, timeout } = this.computeTransitionWeight(action);
									let newTrans = {};
									for(const trans of allTransitions){
										if(trans.name==`Step_${action.name}`){
											newTrans = {...trans};
											newTrans.name = action.name
											/* if(newTrans.constraints.delay && newTrans.constraints.timeout && (newTrans.constraints.delay<newTrans.constraints.timeout)){
												newTrans.constraints.delay={...newTrans.constraints.timeout};
												newTrans.constraints.timeout=null;
											}*/
											trans.constraints.delay= {delay:(trans.constraints.delay?trans.constraints.delay.delay:0)+(trans.constraints.timeout?trans.constraints.timeout.duration:0), unit:"Second"};
											trans.constraints.timeout = null;
											let splitName = trans.to.split("_");
											splitName[0]=solutionItem.to;
											trans.to = splitName.join("_");
											newTrans.constraints={delay:trans.delay?{...trans.delay}:null, timeout:trans.timeout?{...trans.timeout}:null};
											break;
										}
									}
									allTransitions.push(newTrans);
								}
								// break;
							}
						}
					}/*else if(solutionItem.delay.length){
						for(const action of linkToArray){
							if((action.name!==solutionItem.name)&&(action.name!=="Step")){

							}
						}
					}*/
				}
			/* }else if(solution.next){
				console.log("================ my solution next ============ ", {solution:solution.next});
			}*/
		}else{
			let index = 0;
			let solutions = null;
			let transitions = null;
			let allStates = null;
			let keyName="";
			if(executionTree.type=="Sequence"){
				solutions = [[],[]];
				transitions= [[],[]];
				allStates = {0:{}, 1:{}};
			}
			for(const key in executionTree.child){
				if(["Timeout", "Delay", "TimedInterrupt"].includes(executionTree.child[key].type))continue;
				keyName=keyName==""?key:`${keyName}_${key}`;
				const currentItem={};
				this.extractPathExecution(
					executionTree.child[key],
					executionTree,
					solutions!==null?solutions[index]:solution,
					// solution,
					executionTree.constraints,
					last,
					top,
					transitions!==null?transitions[index]:allTransitions,
					parentName,
					allStates!==null?allStates[index]:states
					);
				// solution[key]=currentItem;
				index++;
			}
			if(solutions!==null){
				solution.push(solutions[0]);
				solution.push(solutions[1]);
				allTransitions.push(transitions[0]);
				allTransitions.push(transitions[1]);
				states[keyName]={...allStates};

			}
			// solution = solutions!==null?solutions:solution;
			// console.log("================== solutions ================ ", executionTree.type, solutions, solution, executionTree);
		}
		
	}

	buildMaxPath(path, executionTree){
		const maxPath = [];
		for(const item of executionTree){
			if(Array.isArray(item)){
				this.buildMaxPath(path,item);
			}else{
				const pathItem = {...item}
				delete pathItem.next;
				path.push(item);
				this.buildMaxPath(path,item.next);
			}

		}
	}

	computeTransitionWeight(transition){
		// ajouter la conversion vers la même unité de temps
		let delay=null;
		let timeout=null;
		for(const delayItem of transition.delay){
			let delayValue = delayItem.delay.split("+");
			if (delayValue.length !== 1) {
				delayValue = delayValue[0].split("(")[1];
			}
			delayValue = parseFloat(delayValue);
			if (delay == null || (delay.delay < delayValue)) {
				delay = { ...delayItem, delay: delayValue };
			} 
		}
		for (const durationItem of transition.duration) {
			let durationValue = durationItem.duration.split("+");
			if(durationValue.length!==1){
				durationValue = durationValue[0].split("(")[1];
			}
			durationValue = parseFloat(durationValue);
			if (timeout == null || durationValue < timeout.duration) {
				timeout = { duration: durationValue, unit: durationItem.unit };
			} 
		}
		return {delay, timeout};
	}

	buildPathFromState(executionTree, astd, parent, state){
		var index = 0;
		//parent.constraints={};
		// const isTimeConstraint = ["Timeout", "Delay"].includes(astd.type);
		for(const item of astd.typed_astd.transitions){
			if(parent.allTransitions){
				parent.allTransitions.push(item.event_template.label);
			}
			if(item.arrow.from_state_name==state){
				executionTree.push({
					transition: item.event_template.label,
					to:item.arrow.to_state_name,
					isInitial: astd.typed_astd.initial_state_name == item.arrow.from_state_name,
					from: state,
					isFinal:astd.typed_astd.shallow_final_state_names.includes(item.arrow.to_state_name),
					path:[]
				})
				this.buildPathFromState(
					executionTree[executionTree.length - 1].path,
					astd,
					executionTree[executionTree.length - 1],
					item.arrow.to_state_name
					);
			}
		}
		for(const stateItem of executionTree) {
			for (const item of astd.typed_astd.states) {
				if ((item.name == stateItem.from)||(item.name==stateItem.to)) {
					stateItem["type"] = item.astd.type;
					if (item.astd.type !== "Elem") {
						stateItem[item.name] = [];
						stateItem.shallow_final_state_names = [...astd.typed_astd.shallow_final_state_names];
						stateItem.deep_final_state_names = [...astd.typed_astd.deep_final_state_names];
						// stateItem[state]=[];
						this.buildPathFromState(
						stateItem[item.name],
						//stateItem[state],
						item.astd,
						stateItem,
						item.astd.typed_astd.initial_state_name
						);
					}
					// break;
				}
			}
		}
	}

	buildAllPath(executionTree, astd, parent){
		executionTree[astd.name] = { type: astd.type, child: {} };
		const isTimeConstraint = ["Timeout", "Delay", "TimedInterrupt"].includes(astd.type);
		if(isTimeConstraint)executionTree[astd.name].constraints = [{
			delay: astd.typed_astd.delay,
			delay_unit:astd.typed_astd.delay_unit,
			duration: astd.typed_astd.duration,
			duration_unit: astd.typed_astd.duration_unit
		}];
		if(astd.typed_astd.sub_astd){
			this.buildAllPath(executionTree[astd.name].child, astd.typed_astd.sub_astd, executionTree[astd.name]);
			
		}else if(astd.typed_astd.left_astd && astd.typed_astd.right_astd){
			if(astd.type=="Synchronization"){
				executionTree[astd.name].temporal_actions = [...astd.typed_astd.synchronization_set];
				// executionTree[astd.name].temporal_constraints = {};
			}
			this.buildAllPath(executionTree[astd.name].child, astd.typed_astd.left_astd, executionTree[astd.name]);
			this.buildAllPath(executionTree[astd.name].child, astd.typed_astd.right_astd, executionTree[astd.name]);
		}else{
			if(astd.typed_astd.initial_state_name){
				const initialState = astd.typed_astd.initial_state_name;
				executionTree[astd.name].path=[];
				executionTree[astd.name].allTransitions=[];
				this.buildPathFromState(executionTree[astd.name].path, astd, executionTree[astd.name], initialState);
				if(parent.constraints){
					parent.constraints[0].allTransitions = [
						...(parent.constraints[0].allTransitions||[]),
						...executionTree[astd.name].allTransitions,
					];
				}
				delete executionTree[astd.name].allTransitions;
				return;
			}
		}
		if(executionTree[astd.name].constraints && parent){
			parent.constraints = parent.constraints ? [...parent.constraints, ...executionTree[astd.name].constraints] : [...executionTree[astd.name].constraints];
		}
	}

	generateCheddarFiles(){
		const { create } = require("xmlbuilder2");
		const xmlStr =
      '<?xml version="1.0" encoding="utf-8"?><cheddar><core_units><core_unit id="id_1"><object_type>CORE_OBJECT_TYPE</object_type><name>core1</name><scheduling><scheduling_parameters><scheduler_type>POSIX_1003_HIGHEST_PRIORITY_FIRST_PROTOCOL</scheduler_type><quantum>0</quantum>  <preemptive_type>NOT_PREEMPTIVE</preemptive_type><capacity>0</capacity><period>0</period><priority>0</priority><start_time>0</start_time></scheduling_parameters></scheduling><speed>1</speed><worstcase_perequest_intrabank_memory_interference>0</worstcase_perequest_intrabank_memory_interference><worstcase_perequest_interbank_memory_interference>0</worstcase_perequest_interbank_memory_interference><isa>I386</isa></core_unit></core_units><processors><mono_core_processor id="id_2"><object_type>PROCESSOR_OBJECT_TYPE</object_type><name>P1</name><processor_type>MONOCORE_TYPE</processor_type><migration_type>NO_MIGRATION_TYPE</migration_type><core ref="id_1"></core></mono_core_processor></processors><address_spaces> <address_space id="id_3"><object_type>ADDRESS_SPACE_OBJECT_TYPE</object_type><name>ea</name><cpu_name>P1</cpu_name><text_memory_size>0</text_memory_size><stack_memory_size>0</stack_memory_size><data_memory_size>0</data_memory_size><heap_memory_size>0</heap_memory_size><scheduling> <scheduling_parameters><scheduler_type>NO_SCHEDULING_PROTOCOL</scheduler_type><quantum>0</quantum><preemptive_type>PREEMPTIVE</preemptive_type>  <capacity>0</capacity><period>0</period><priority>0</priority><start_time>0</start_time></scheduling_parameters></scheduling><mils_confidentiality_level>TOP_SECRET</mils_confidentiality_level><mils_integrity_level>HIGH</mils_integrity_level><mils_component>SLS</mils_component><mils_partition>DEVICE</mils_partition><mils_compliant>TRUE</mils_compliant></address_space></address_spaces><tasks><aperiodic_task id="id_4"><object_type>TASK_OBJECT_TYPE</object_type><name>inspiratory_pause_on</name><task_type>APERIODIC_TYPE</task_type><cpu_name>P1</cpu_name><address_space_name>ea</address_space_name><capacity>1</capacity><capacity_low>0</capacity_low><energy_consumption>0</energy_consumption><deadline>16</deadline><start_time>15</start_time><priority>1</priority><blocking_time>0</blocking_time><policy>SCHED_FIFO</policy><text_memory_size>0</text_memory_size><text_memory_start_address>0</text_memory_start_address><stack_memory_size>0</stack_memory_size><criticality>0</criticality><context_switch_overhead>0</context_switch_overhead><cfg_relocatable>FALSE</cfg_relocatable><mils_confidentiality_level>TOP_SECRET</mils_confidentiality_level><mils_integrity_level>HIGH</mils_integrity_level><mils_component>SLS</mils_component><mils_task>APPLICATION</mils_task><mils_compliant>TRUE</mils_compliant><access_memory_number>0</access_memory_number><maximum_number_of_memory_request_per_job>0</maximum_number_of_memory_request_per_job></aperiodic_task></tasks></cheddar>';
		// const rootXML = 
		/*create({version:'1.0', encoding:'utf8'})
		.ele('cheddar')
		.ele('core_units')
		.ele('core_unit', {id:'id_1'})
		.ele('object_type').txt('CORE_OBJECT_TYPE')
		.ele('name').txt('core1')
		.ele('scheduling')
		.ele('scheduling_parameters')
		.ele
		
		;*/

		const rootXml = create(xmlStr);

		// convert the XML tree to string
		const xml = rootXml.end({ prettyPrint: true });
		const xmlObject = rootXml.end({format: 'object'});
		console.log({xmlObject});
		var astd = [];
        fetch("../decomposition.json").then((response) => response.json())
		.then((json) => {astd=json;
			let executionTree = {};
			for (const item of astd.iastd.top_level_astds) {
				this.buildAllPath(executionTree, item, null);
			}
			// const solution = {};
			let solution = [];
			let allTransitions = [];
			let states = {};
			this.extractPathExecution(
				executionTree[Object.keys(executionTree)[0]],
				null,
				solution,
				executionTree[Object.keys(executionTree)[0]].constraints,
				[],
				[],
				allTransitions,
				null,
				states
			); 
			let maxPath = [];
			for (const key in states) {
				let index = 0;
				for (const itemSolution of allTransitions) {
				maxPath = [...maxPath, ...this.BellmanFord(states[key][index], itemSolution)];
				index++;
				}
			}
			console.log("===================== my max path ===================== ", maxPath);
			let taskTemplate = {...xmlObject.cheddar.tasks.aperiodic_task};
			const aperiodicTasks = [];
			console.log({taskTemplate});
			let index=parseInt(taskTemplate["@id"].split("_")[1]);
			const dependencies = [];
			let startTime = 0;
			let pred=null;
			let dependencyTemplate = {
				type_of_dependency:"PRECEDENCE_DEPENDENCY",
				precedence_sink:{"@ref":null},
				precedence_source:{"@ref":null}
			};
			const taskGroups =
				{
				transaction_task_group:[ {
					"@id": null,
					object_type: "TASK_GROUP_OBJECT_TYPE",
					name: "Group1",
					task_list:{periodic_task:[]},
					task_group_type:"TRANSACTION_TYPE",
					deadline:40,
					start_time:0,
					priority:0,
					period:40,
					jitter:0
				}],
				};
			for(const tasks of maxPath) {
				if(!tasks.pred)continue;
				// taskTemplate = {...taskTemplate, "@id":`id_${index}`, name:tasks.action};
				startTime =
          startTime +
          ((tasks.constraints.timeout && tasks.constraints.timeout.duration) ||
            (tasks.constraints.delay && tasks.constraints.delay.delay) ||
            0);
				aperiodicTasks.push({
					...taskTemplate,
					"@id": `id_${index}`,
					name: tasks.action,
					capacity:1,
					start_time:startTime,
					deadline: startTime+1// (tasks.constraints.timeout && tasks.constraints.timeout.duration) || taskTemplate.deadline,
					 // period: (tasks.constraints.timeout && tasks.constraints.timeout.duration) || taskTemplate.deadline,
					});
				startTime++;
				taskGroups.transaction_task_group[0].task_list.periodic_task.push({ "@ref": `id_${index}` });
				if(pred!==null){
					// dependencyTemplate.precedence_sink["@ref"] = `id_${index}`;
					// dependencyTemplate.precedence_source["@ref"] = pred;
					dependencies.push({
						...dependencyTemplate,
						precedence_sink: { "@ref": `id_${index}` },
						precedence_source: { "@ref": pred },
					});
					dependencies.push({
						type_of_dependency:"TIME_TRIGGERED_COMMUNICATION_DEPENDENCY",
						time_triggered_communication_sink: { "@ref": `id_${index}` },
						time_triggered_communication_source: { "@ref": pred },
						time_triggered_timing_property:"IMMEDIATE_TIMING"
					})
				}
				pred = `id_${index}`;
				index++;
			}
			/* taskGroups.transaction_task_group[0]["@id"] = `id_${index}`;
			if(taskGroups.transaction_task_group.length){
				xmlObject.cheddar.task_groups=taskGroups;
			}
			if(dependencies.length){
				xmlObject.cheddar.dependencies = {dependency:dependencies};
			}*/
			xmlObject.cheddar.tasks.aperiodic_task = aperiodicTasks.length!=1?aperiodicTasks:aperiodicTasks[0];
			console.log({xmlObject});
			console.log({xml:create(xmlObject).end({prettyPrint:true})});
			console.log(create(xmlObject).end({ prettyPrint: true }));

		});
		
	}

	generateTraces() {
		if (this.traces.length <= 0) {
			alert("No traces to generate.");
			return;
		}

		var tracesToGen = [],
			errors = [];
		// select only valid traces to generate
		this.traces.forEach((trace) => {
			const validResult = Validator.syntax_Verify(trace.content, window.patron_trace_grammar_object, true);
			if (validResult.result && trace.name !== "") {
				tracesToGen.push({
					name: trace.name,
					toGenerate: validResult.result
				});
			} else {
				errors.push(trace.name);
			}
		});

		// Warn if any trace is in error and don't start generation process if there is no valid traces
		if (errors.length > 0 && tracesToGen.length === 0) {
			toastr.error("All traces contain errors and couldn't be generated.");
			return;
		} else if (errors.length > 0) {
			toastr.warning(errors.join(", ") + " contain errors and will not be generated.");
		}

		var isResultOk = false,
			genToast = toastr.info("", "<div class=\"spinner-border load-trace\"></div><span class=\"load-trace\">Generating traces...</span>", {
				timeOut: 0,
				extendedTimeOut: 0,
				onHidden: function() { // Attach toast message to display once the loading generation is over depending on success/error
					if (isResultOk) toastr.success("Valid traces were successfully generated in project folder."); else toastr.error("At least one trace encountered an issue and couldn't be generated.");
				}
			});

		// Execute generation process for all valid traces and listen for a reply once it is completed
		const traceGen = fork("editeur/js/traces/traceGenerator.js", [this.rootPath]);
		traceGen.send(JSON.stringify(tracesToGen));

		traceGen.on("message", function(message) {
			isResultOk = message; // Affect the result to var used to display success/error toast then simulate a click to trigger onHidden of genToast
			$(genToast).click();
			traceGen.kill();
		});
	}

	/**
	 * =======================================================
	 * 					Config Section
	 * =======================================================
	 */
	addConf(conf) {
		// Add conf to list
		this.config.push(conf);

		// Add conf to selector
		$("#optConfig").append($("<option>", {
			value: conf.name,
			text: conf.name
		}));

		// Select new config
		$("#optConfig option[value=" + conf.name + "]").attr("selected", "selected");

	}

	getConf(conf) {
		var ret = undefined;
		this.config.forEach((c) => {
			if (c.name === conf) {
				ret = c;
			}
		});
		return ret;
	}

	loadConf() {
		this.config.forEach((conf) => {
			$("#optConfig").append($("<option>", {
				value: conf.name,
				text: conf.name
			}));
		});
	}

	getCurrentConf() {
		let confName = $("#optConfig option:selected").val();
		return this.getConf(confName);
	}

	deleteConf(conf) {
		this.config.splice(this.config.indexOf(this.getConf(conf)), 1);

		// Remove conf from selector
		if (conf !== "addConf") $("#optConfig option[value='" + conf + "']").remove();
	}

}

/************************************************************************************************************************
 * Global listeners for the channels used to communicate between the main process and the renderer process.				*
 * They are not part of Project class, but regrouped here to avoid searching them.							  			*
 ************************************************************************************************************************/

ipc.on("saveas-project", function(event, pathToSave) {
	var L = {};

	try {
		L.iastd = JSON.parse(project.export());
	} catch (err) {
		L.iastd = {};
	}
	project.setProjectEnv(path.dirname(pathToSave), path.basename(pathToSave, path.extname(pathToSave)));
	L.editor = project.save();

	if (!pathToSave.includes(".eastd")) {
		pathToSave += ".eastd";
	}

	fs.writeFileSync(pathToSave, JSON.stringify(L));
	toastr.success("Project successfully saved to file: " + pathToSave);

	if (window.project.verify2(true)) {
		if (pathToSave.includes(".eastd")) {
			pathToSave = pathToSave.substring(0, pathToSave.length - 6);
			pathToSave += ".json";
		} else {
			pathToSave += ".json";
		}
		fs.writeFileSync(pathToSave, project.export());
		toastr.success("Specification successfully exported to file: " + pathToSave);
	}
});

/**
 * Action to execute when the main process returns an export-spec event after the user selected a file name and directory
 */
ipc.on("export-spec", function(event, pathToSave) {
	project.setProjectEnv(path.dirname(pathToSave), path.basename(pathToSave, path.extname(pathToSave)));

	if (!pathToSave.includes(".json")) {
		pathToSave += ".json";
	}

	fs.writeFileSync(pathToSave, project.export());
	toastr.success("Specification successfully exported to file: " + pathToSave);
});

/**
 * Action to execute when the main process returns a path-modify event after the user selected a folder where to save spec
 */
ipc.on("path-modify", function(event, pathToSave) {

	project.setProjectEnv(pathToSave);
	window.konsole.hide();
	project.delete();
	project.filesID = 1;
	project.newFile();

	var L = {};

	try {
		L.iastd = JSON.parse(project.export());
	} catch (err) {
		L.iastd = {};
	}

	project.setProjectEnv(path.dirname(pathToSave), path.basename(pathToSave, path.extname(pathToSave)));
	L.editor = project.save();

	if (!pathToSave.includes(".eastd")) pathToSave += ".eastd";

	fs.writeFileSync(pathToSave, JSON.stringify(L));
	toastr.success("Project successfully saved to file: " + pathToSave);
});

/**
 * Action to execute when the main process returns a save-spec event after the user selected a file name and directory
 */
ipc.on("save-project", function(event, pathToSave) {
	var L = {};

	try {
		L.iastd = JSON.parse(project.export());
	} catch (err) {
		L.iastd = {};
	}
	project.setProjectEnv(path.dirname(pathToSave), path.basename(pathToSave, path.extname(pathToSave)));
	L.editor = project.save();

	if (!pathToSave.includes(".eastd")) pathToSave += ".eastd";

	fs.writeFileSync(pathToSave, JSON.stringify(L));
	toastr.success("Project successfully saved to file: " + pathToSave);
});

/**
 * Action to execute when the main process returns a load-project event after the user selected the project file to load
 */
ipc.on("load-project", function(event, files) {
	window.konsole.hide();
	// We accept multiple selections, but only load the first project now. We might want to load multiple in the future
	try {
		const result = JSON.parse(fs.readFileSync(files[0]));
		project.setProjectEnv(path.dirname(files[0]), path.basename(files[0], path.extname(files[0])));
		project.load(result.editor);
	} catch (error) {
		toastr.error(error.message);
	}
});

/**
 * Action to execute when the main process returns a module-import event after the user selected module files to import in the project
 */
ipc.on("module-import", function(event, filePaths) {
	SpecHeader.onModuleImports.call($("#modules_list")[0], filePaths);
});

/**
 * Action to execute when the main process returns a schema-import event after the user selected user defined schemas to import in the project
 */
ipc.on("schema-import", function(event, filePaths) {
	SpecHeader.onFileSelect.call($("#schemas_list")[0], filePaths, SpecHeader.loadSchema);
});

/**
 * Action to execute when the main process returns a event-signs-import event after the user selected event signatures files to add in the project
 */
ipc.on("event-signs-import", function(event, filePaths) {
	SpecHeader.onFileSelect.call($("#events_list")[0], filePaths, SpecHeader.loadEventSignature);
});