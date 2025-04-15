# eASTD

eASTD is the graphical editor of ASTD specifications. It also allows to run the ASTD interpreter iASTD or the ASTD compiler cASTD. 


## Installation of eASTD only

1. Make sure to have node.js installed on your computer : https://nodejs.org/en/ (Recommended is enough)
2. in the root folder of the project, run the command line: `npm install`
3. To launch the app: `npm start`

***Note:** Execution of iASTD and cASTD will not work unless you proceed to the complete ASTD suite installation instead*

## Installation of the complete ASTD suite (eASTD, iASTD, cASTD)
*  See [wiki page](https://depot.gril.usherbrooke.ca/fram1801/eASTD/wikis/Installation/Installation#installation)

## Developpers

1. To autofix syntax standards and detect other errors and warnings: `npm run fix`
2. To only detect syntax errors and warning: `npm run check`
3. Run command to verify if exported spec is still valid: `npm run testExport`
4. Run command for each tests : `npm run runAllTest`
5. Run command before commit which fix syntax and run all tests before : `npm run runBeforeCommit`

Those are for your own good. Please consider using it. 
