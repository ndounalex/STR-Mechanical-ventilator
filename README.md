# STR-Mechanical-ventilator
Modelisation du mechanical ventilator en ASTD et test d'ordonancement sur Cheddar

[![Code Quality Status](https://img.shields.io/badge/code%20quality-B%2B-yellowgreen)](https://img.shields.io/badge/code%20quality-B%2B-yellowgreen)
[![Build](https://img.shields.io/badge/build-passing-green)](https://img.shields.io/badge/build-passing-green)
[![Docs](https://img.shields.io/badge/docs-passing-green)](https://img.shields.io/badge/docs-passing-green)
[![Kubernetes](https://img.shields.io/badge/kubernetes-automated-blue)](https://img.shields.io/badge/kubernetes-automated-blue)

# cASTD and eASTD installation
The installation procedure for eASTD and cASTD can be found in the [here](https://github.com/ndounalex/ASTD-tools).

## Wiki

Go to page [cASTD Wiki](https://depot.gril.usherbrooke.ca/lionel-tidjon/castd/wikis/home)


## Technical documentation

The translation rules and the compilation approach are available [here](https://github.com/DiegoOliveiraUDES/astd-tech-report-27).

## Description

our warehouse is mainly made up of the following elements:
- the .eastd file, which contains the eastd specification for the case study.
- the .json file, which corresponds to the JSON generated by eastd and used by cASTD to generate the C++ code.
- the TCS_case_with_ccsl_observer.eastd file, which contains the eastd specification of our observer allowing us to validate our specification of the case study, here based on the translation presented in the document we have translated the CCSL specification into ASTD and used it as an observer by coupling it to our ASTD specification to validate the latter.
- the execution folder contains the various test cases carried out on our specification. For each test case we have an input file containing all the commands executed for the test case and an output file containing the output obtained during the test.




## ReadTheDocs

Check our full documentation [online](https://castd.readthedocs.io/).

## Slack channel

Join project discussions [here](https://astd-cse.slack.com/)

