|Code Quality Status| |Build| |Docs| |Kubernetes|

Installation
============

Unzip all files in the cASTD project. Next, execute the following
commands.

Install Java Development Toolkit
--------------------------------

Linux
~~~~~

::

    ~# sudo apt-get install build-essential
    ~# sudo add-apt-repository ppa:openjdk-r/ppa
    ~# sudo apt-get update
    ~# sudo apt-get install openjdk-8-jdk
    ~# sudo update-alternatives --config java
    ~# sudo update-alternatives --config javac
    ~# sudo apt-get install ant

Windows
~~~~~~~

-  JAVA >= 1.8 : https://jdk.java.net/
-  GCC >= 7.0

Compile source code
-------------------

::

    ~# ant

Run cASTD
---------

::

    ~# java -jar castd.jar -v -h
    OS version: linux
    Build version 1.21
    usage: cASTD [-c] [-d] [-e] [-f <json | shorthandevents | all>] [-h] [-I
           <include_path>] [-k] [-L <library_path>] [-m <main_astd>] [-o
           <dest_path>] [-s <src_path>] [-v]
                   Options, flags and arguments may be in any order
     -c,--condition-opt                           Condition optimization
     -d,--debug                                   Activate the debug mode
     -e,--show-exec-state                         Show the execution state of
                                                  the input ASTD
     -f,--format <json | shorthandevents | all>   Use shorthand event format
                                                  or json event format or both
                                                  in the compiled program
     -h,--help                                    Display cASTD arguments and
                                                  their description
     -I,--include <include_path>                  Include custom headers
     -k,--kappa-opt                               Kappa direct optimization
     -L,--lib <library_path>                      Include external libraries
     -m,--main <main_astd>                        Name of main astd
     -o,--output <dest_path>                      The output source and
                                                  executable code
     -s,--spec <src_path>                         The input specification
     -v,--version                                 The cASTD version

cASTD is a tool that helps to generate source and executable code in C++
from ASTD specifications.

cASTD architecture
==================

The compilation methodology is divided into 4 steps: parsing,
translation from the ASTD language to the Intermediate Model (IM),
translation from the IM model to the target code, and code optimization.
Several optimizations such as removing redundant calculations are
applied to the output code during the generation of the program.

.. figure:: https://depot.gril.usherbrooke.ca/lionel-tidjon/castd/wikis/uploads/58eabfffb23394c39cefee11552934d7/castd_architecture.png
   :alt: castd\_architecture

   castd\_architecture
ASTD object model
-----------------

The first step reads an ASTD specification in JSON and produces an ASTD
object model.

.. figure:: https://depot.gril.usherbrooke.ca/lionel-tidjon/castd/wikis/uploads/1fa703553e6f7864ee5a798bab83e424/astd_object.png
   :alt: astd\_object

   astd\_object
IL model
--------

The second step generates a program represented in an intermediate
language. The last step translates an intermediate model of the program
into a concrete programming language like C++.

.. figure:: https://depot.gril.usherbrooke.ca/lionel-tidjon/castd/wikis/uploads/f217c2dc9c65b92b50b31d676aa6a37a/il_model.png
   :alt: il\_model

   il\_model
Output structure
----------------

cASTD takes a JSON spec and user files (e.g. *.cpp, *.h) as input, and
generates a binary executable and C++ source code. For complex types and
visualization of the execution state of the ASTD, you need a JSON
library called ``nlohmann``. It is a sophisticated library to manipulate
JSON documents. This library is automatically included when using JSON
event formats or types.

.. figure:: https://depot.gril.usherbrooke.ca/lionel-tidjon/castd/wikis/uploads/94bb1e4586563826fd41822cce3bd5a3/castd.png
   :alt: castd

   castd
During the visualization of the execution state of an ASTD, with the
option ``-e | -show-execution-state``, two other files are generated.
The first one ``client.h`` allows sending the current execution state of
an ASTD to eASTD through a socket file. The second one ``exec_schema.h``
maps features in the JSON schema ``exec.schema.json`` to their ID
values.

eASTD and cASTD integration
===========================

| The ASTD project is a composition 3 different projects:
| Which can all be installed separately. Refers to the page for
installation.

1. `iASTD <https://depot.gril.usherbrooke.ca/lionel-tidjon/iASTD>`__
2. `cASTD <https://depot.gril.usherbrooke.ca/lionel-tidjon/castd>`__
3. `eASTD <https://depot.gril.usherbrooke.ca/fram1801/eASTD>`__

Procedure
---------

1. | Start by downloading the install script. *Right-Click,
   download...*.
   | `install.py <https://depot.gril.usherbrooke.ca/fram1801/eASTD/wikis/uploads/d2684a03b3e595239b1127807756a6b5/install.py>`__

2. | Now at this point you should know the path of the install.py file.
   If you don't, it's probably in your Downloads folder. I recommend you
   put the file where you want to install ASTD *(i.e. Desktop)*.
   | Now copy the file from the download folder *( or wherever you saved
   it )* to the location you want.

3. Open a terminal and navigate to that location.

4. | Launch the install script with python3.
   | ``python3 install.py``.

5. If successful you should see this, if not refer to the trouble
   shooting version.

6. You should by now have a folder *(astd)* that contains 3 folder

-  castd
-  eASTD
-  iASTD

7. You can now `launch eASTD <https://depot.gril.usherbrooke.ca/fram1801/eASTD/wikis/Installation/Launch%20Editor>`__

| ***ATTENTION***
| If you want castd to be functional with the editor you **MUST** put it
in the ``astd`` folder along with other tools (i.e., iASTD, eASTD).

8. Load an ASTD example

.. figure:: https://depot.gril.usherbrooke.ca/lionel-tidjon/castd/wikis/uploads/72b4be5718dc4eb631594a894eb670c9/castd_example.png
   :alt: castd\_example

   castd\_example

9. Setup castd configuration from eASTD

.. figure:: https://depot.gril.usherbrooke.ca/lionel-tidjon/castd/wikis/uploads/624db8aef4c3928cfd94951ab9d49954/eastd_config.png
   :alt: eastd\_config

   eastd\_config

10. Run cASTD from the eASTD editor

.. figure:: https://depot.gril.usherbrooke.ca/lionel-tidjon/castd/wikis/uploads/5cd15de4ddbec4a48bbfa2f86fe85ebd/castd_run.png
   :alt: castd\_run

   castd\_run

11. Visualize the execution state

cASTD can execute in interactive mode by manually entering events. It
can also run from an event file.

.. figure:: https://depot.gril.usherbrooke.ca/lionel-tidjon/castd/wikis/uploads/50aa70de584cc1ac8ae317fb36ff6df0/castd_kleene.png
   :alt: castd\_kleene

   castd\_kleene
Examples
========

Run spec from eASTD
-------------------

1. Load your ``*.eastd`` project or create a new spec in the eASTD
   editor.

2. Configures the cASTD execution and save your configuration following
   instructions in Section `eASTD and cASTD
   integration <https://depot.gril.usherbrooke.ca/lionel-tidjon/castd/wikis/eASTD%20and%20cASTD%20integration>`__.

3. Run your spec in interactive mode by clicking the green button
   ``|>``.

Run spec from command-line
--------------------------

After editing your spec in the eASTD editor, export it to JSON. Now,
open a command-line following instructions in Section
`Installation <https://depot.gril.usherbrooke.ca/lionel-tidjon/castd/wikis/Installation>`__.

ASTD execution
~~~~~~~~~~~~~~

Let us consider the test case ``tests/testcases/specs/Flow_Automaton``.

Interactive mode
^^^^^^^^^^^^^^^^

To compile this test case, run command

::

    ~# java -jar castd.jar -s tests/testcases/specs/Flow_Automaton/*.json -o tests/testcases/specs/Flow_Automaton/
    [stage 1] Helper code generation succeeded ...
    [stage 2] Main code generation succeeded ...
    [stage 3] Makefile successfully generated ...
    [Success] cASTD successfully generated code. Done !!!
    ~# ./main -h
    This program has been compiled by cASTD.
    ./my_program [-i <event file>]  [-h]
    [OPTIONS]                                        
       -i <event  file>  Read an event file in Shorthand format.
                         If an event file is not given, it runs in interactive
                         mode from command line
       -h                Show this help
    ~# ./main
    e1(1)
    e2(1)
    val=2
    e1(1)
    e2(1)
    val=4
    e5(1)
    Event is not recognized
    e1("test")
    Event is not executable
    ~# 

This command generates a program with the default input event format
``shorthandevents``. Note that since the option ``-m <main_astd_name>``
is not specified, cASTD will execute the default astd ``MAIN``.

To generate a program that can read a JSON event format, add the option
``-f json``, i.e.

::

    ~# java -jar castd.jar -s tests/testcases/specs/Flow_Automaton/*.json -o tests/testcases/specs/Flow_Automaton/ -f json
    [stage 1] Helper code generation succeeded ...
    [stage 2] Main code generation succeeded ...
    [stage 3] Makefile successfully generated ...
    [Success] cASTD successfully generated code. Done !!!
    ~# ./main -h
    This program has been compiled by cASTD.
    ./my_program [-i <event file>]  [-h]
    [OPTIONS]                                        
       -i <event  file>  Read an event file in JSON format.
                         If an event file is not given, it runs in interactive
                         mode from command line
       -h                Show this help
    ~# ./main -i input.json
    val=2
    val=4
    Event is not recognized
    Event is not executable

As you see, the compiled program can take an input event file.

Using an input event file
^^^^^^^^^^^^^^^^^^^^^^^^^

To generate a program that can read either the shordhand event format or
the JSON event format, add the option ``-f all``, i.e.

::

    ~# java -jar castd.jar -s tests/testcases/specs/Flow_Automaton/*.json -o tests/testcases/specs/Flow_Automaton/ -f all
    [stage 1] Helper code generation succeeded ...
    [stage 2] Main code generation succeeded ...
    [stage 3] Makefile successfully generated ...
    [Success] cASTD successfully generated code. Done !!!
    ~# ./main -h
    This program has been compiled by cASTD.
    ./my_program [-i <event file>] [-f <event format>]  [-h]
    [OPTIONS]                                        
       -i <event  file>  Read an event file in JSON or Shorthand format.
                         If an event file is not given, it runs in interactive
                         mode from command line
       -f <event format> Event format. It can be a JSON or Shorthand format
       -h                Show this help
    ~# ./main -i input.json -f json 
    val=2
    val=4
    Event is not recognized
    Event is not executable
    ~# ./main -i input.txt -f shorthandevents
    val=2
    val=4
    Event is not recognized
    Event is not executable

If-fi optimization
~~~~~~~~~~~~~~~~~~

To do if-fi optimization (see ``cASTD/doc``), add argument
``--condition-opt | -c`` and run command

::

    ~# java -jar castd.jar -s tests/testcases/specs/Flow_Automaton/*.json -o tests/testcases/specs/Flow_Automaton/ -c

Kappa optimization
~~~~~~~~~~~~~~~~~~

To do direct kappa optimization (see ``cASTD/doc``), add argument
``--kappa-opt | -k`` and run command

::

    ~# java -jar castd.jar -s tests/testcases/specs/Flow_Automaton/*.json -o tests/testcases/specs/Flow_Automaton/ -c -k

Indirect kappa optimization is currently not supported.

Complex types
~~~~~~~~~~~~~

For complex types, the nlohmann JSON library automatically included (see
Section `cASTD architecture <https://depot.gril.usherbrooke.ca/lionel-tidjon/castd/wikis/cASTD%20architecture>`__/Output structure).
Let us consider the test case ``tests/testcases/specs/Call``. Run command

::

    ~# java -jar castd.jar -s tests/testcases/specs/Call/*.json -o tests/testcases/specs/Call/ -f json

It shows an example of network packet execution. The packet is encoded
in JSON and it is decoded using the JSON library during compilation.

::

    ~# java -jar castd.jar -s tests/testcases/specs/Call/*.json -o tests/testcases/specs/Call/ -f json

Visualization of the current state of an ASTD
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

To enable the visualization of the current state in the eASTD editor,
add argument ``--show-state-activation | -e`` in the command line.

Let us consider the test case ``tests/testcases/specs/Automaton_Automaton``.
Run command

::

    ~# java -jar castd.jar -s tests/testcases/specs/Automaton_Automaton/*.json -o tests/testcases/specs/Automaton_Automaton/ -c -f all -m test -k -e

Then, execute the generated program with option ``-e`` and ``-v``. The
option ``-e`` allows one to send the current state of the ASTD to eASTD
using the socket file ``exectojson.sock``. The option ``-v`` allows one
to print out the current execution state in JSON.

::

    ~# cat inputs.trace
    e1(0)
    e2(2)
    e1("test")
    ~# ./test -h
    This program has been compiled by cASTD.
    ./my_program [-i <event file>] [-e <socket file>] [-v][-f <event format>]  [-h]
    [OPTIONS]                                        
       -i <event  file>  Read an event file in JSON or Shorthand format.
                         If an event file is not given, it runs in interactive
                         mode from command line
       -e <socket file>  Socket file to send the execution state to eASTD
       -f <event format> Event format. It can be a JSON or Shorthand format
       -v                Print the current execution state in console (verbose)
       -h                Show this help
    ~# ./test -i inputs.trace -f shorthandevents -e exectojson.sock -v
    Connection succeeded !
    Sent event: {
        "executed_event": "e1",
        "top_level_astd": {
            "attributes": [
                {
                    "current_value": 0,
                    "name": "a",
                    "type": "int"
                }
            ],
            "current_state_name": "S1",
            "current_sub_state": {
                "attributes": [
                    {
                        "current_value": 0,
                        "name": "b",
                        "type": "int"
                    }
                ],
                "current_state_name": "SS0",
                "current_sub_state" : {
                   "type" : "Elem"
                }
                "name": "S0",
                "type": "Automaton"
            },
            "name": "test",
            "type": "Automaton"
        }
    }
    Event is not recognized
    Event is not executable

In this case, Event ``e1(0)`` is executed but ``e2(1)`` and
``e1("test")`` is not accepted.

Execution on large specifications
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Performance tests are located in the folder ``tests/perf``. Let us
consider the case ``tests/perf/flow+kleene+autx100`` generated from
eASTD.

.. figure:: https://depot.gril.usherbrooke.ca/lionel-tidjon/castd/wikis/uploads/766914c55926a1d0f011c1e998bae86b/Screenshot_from_2020-09-11_02-40-20.png
   :alt: Screenshot\_from\_2020-09-11\_02-40-20

   Screenshot\_from\_2020-09-11\_02-40-20
Run the following command

::

    ~# java -jar castd.jar -s tests/perf/flow+kleene+autx100/*.json -o tests/perf/flow+kleene+autx100/ -c

Then, execute the compiled program

::

    ~# cat events.txt
    e1(1)
    e2(2)
    e3(3)
    e1(1)
    e2(2)
    e3(3)
    ~# cat a.cpp
    #include "Code.cpp"
    #include "helper.h"
    #define stringify(name) #  name
    enum KleeneState
    {
      KLEENE_NOTSTARTED,
      KLEENE_STARTED
    };
    const char* kleeneState[] = 
    {
      stringify(KLEENE_NOTSTARTED),
      stringify(KLEENE_STARTED)
    };
    enum AutState
    {
      S0,
      S1
    };
    const char* autState[] = 
    {
      stringify(S0),
      stringify(S1)
    };
    struct TState_C_100
    {
      AutState  autState;

    };
    struct TState_B_100
    {
      KleeneState  kleeneState;
      TState_C_100  ts_C_100;

    };
    struct TState_C_99
    {
      AutState  autState;
    ...

    ~# ./a -i events.txt

Technical documentation
=======================

The translation rules and the compilation approach are available
`here <https://depot.gril.usherbrooke.ca/lionel-tidjon/tr-26/blob/master/TR-26.pdf>`__.

Slack channel
=============

Join project discussions `here <https://astd-cse.slack.com/>`__

.. |Code Quality Status| image:: https://img.shields.io/badge/code%20quality-B%2B-yellowgreen
   :target: https://img.shields.io/badge/code%20quality-B%2B-yellowgreen
.. |Build| image:: https://img.shields.io/badge/build-passing-green
   :target: https://img.shields.io/badge/build-passing-green
.. |Docs| image:: https://img.shields.io/badge/docs-passing-green
   :target: https://img.shields.io/badge/docs-passing-green
.. |Kubernetes| image:: https://img.shields.io/badge/kubernetes-automated-blue
   :target: https://img.shields.io/badge/kubernetes-automated-blue