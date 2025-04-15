package com.udes.args;

import com.udes.utils.Constants;
import com.udes.utils.Utils;
import org.apache.commons.cli.*;

public class CmdManager {

    /*
     * @brief Handles command line options
     * @param  input arguments
     * @return The input options
     */
    public static String[] handler(String[] args) {

        CommandLine commandLine;
        Option option_version = Option.builder("v")
                                .longOpt("version")
                                .desc("The cASTD version")
                                .build();
        Option option_help = Option.builder("h")
                                .longOpt("help")
                                .desc("Display cASTD arguments and their description")
                                .build();
        Option option_spec = Option.builder("s")
                              .argName("src_path")
                              .hasArg()
                              .longOpt("spec")
                              .argName("src_path")
                              .hasArg()
                              .desc("The input specification")
                              .build();
        // We already know the target C++
        /*Option option_target = Option.builder("t")
                               .argName("lang")
                               .longOpt("target-lang")
                               .argName("lang")
                               .hasArg()
                               .desc("The target language")
                               .build();*/
        Option option_output = Option.builder("o")
                               .argName("dest_path")
                               .hasArg()
                               .longOpt("output")
                               .argName("dest_path")
                               .hasArg()
                               .desc("The output source and executable code")
                               .build();
        Option option_library = Option.builder("L")
                                .argName("library_path")
                                .hasArg()
                                .longOpt("lib")
                                .argName("library_path")
                                .hasArg()
                                .desc("Include external libraries")
                                .build();
        Option option_include = Option.builder("I")
                                .argName("include_path")
                                .hasArg()
                                .longOpt("include")
                                .argName("include_path")
                                .hasArg()
                                .desc("Include custom headers")
                                .build();
        Option option_debug = Option.builder("d")
                                .longOpt("debug")
                                .desc("Activate the debug mode")
                                .build();
        Option option_kappaopt = Option.builder("k")
                                .longOpt("kappa-opt")
                                .desc("Kappa direct optimization")
                                .build();
        Option option_condopt = Option.builder("c")
                                .longOpt("condition-opt")
                                .desc("Condition optimization")
                                .build();
        Option option_execopt = Option.builder("e")
                                .longOpt("show-exec-state")
                                .desc("Show the execution state of the input ASTD")
                                .build();
        Option option_main = Option.builder("m")
                                .argName("main_astd")
                                .hasArg()
                                .longOpt("main")
                                .argName("main_astd")
                                .hasArg()
                                .desc("Name of main astd")
                                .build();
        Option option_stepvalue = Option.builder("step")
                .argName("define_step_value")
                .hasArg()
                .argName("step_value_in_sec")
                .hasArg()
                .desc("Define the value of a step in the chosen unit")
                .build();

        Option option_event_format = Option.builder("f")
                .argName("json | shorthandevents | all")
                .hasArg()
                .longOpt("format")
                .argName("json | shorthandevents | all")
                .hasArg()
                .desc("Use shorthand event format or json event format or both in the compiled program")
                .build();

        Option option_stepasflow = Option.builder("sf")
                .longOpt("step_as_flow")
                .desc("Step act as a flow")
                .build();

        Option option_time_simulation = Option.builder("ts")
                .longOpt("time_simulation")
                .desc("Create code for simulation with controlling time")
                .build();

        Option option_initial_time = Option.builder("it")
                .argName("initial time in seconds")
                .hasArg()
                .longOpt("initial_time")
                .argName("initial time in seconds")
                .hasArg()
                .desc("Initial time of the program for simulations")
                .build();

        Options options = new Options();
        CommandLineParser parser = new DefaultParser();

        options.addOption(option_version);
        options.addOption(option_help);
        options.addOption(option_spec);
        options.addOption(option_output);
        options.addOption(option_debug);
        options.addOption(option_kappaopt);
        options.addOption(option_condopt);
        options.addOption(option_execopt);
        options.addOption(option_main);
        options.addOption(option_stepvalue);
        options.addOption(option_stepasflow);
        options.addOption(option_event_format);
        options.addOption(option_library);
        options.addOption(option_include);
        options.addOption(option_time_simulation);
        options.addOption(option_initial_time);

        String[] outArgs = new String[10];
        try {
            commandLine = parser.parse(options, args);

            if (commandLine.hasOption("s")) {
                outArgs[0] = commandLine.getOptionValue("s");
            }

            if (commandLine.hasOption("spec")) {
                outArgs[0] = commandLine.getOptionValue("spec");
            }

            /*if (commandLine.hasOption("t")) {
                outArgs[1] = commandLine.getOptionValue("t");
            }

            if(commandLine.hasOption("target-lang")) {
                outArgs[1] = commandLine.getOptionValue("target-lang");
            }*/

            if (commandLine.hasOption("o")) {
                outArgs[1] = commandLine.getOptionValue("o");
            }

            if(commandLine.hasOption("output")) {
                outArgs[1] = commandLine.getOptionValue("output");
            }

            if (commandLine.hasOption("d") || commandLine.hasOption("debug")) {
                Constants.DEBUG = true;
            }

            if (commandLine.hasOption("k")) {
                Constants.KAPPA_DIRECT_OPTS = true;
            }

            if(commandLine.hasOption("kappa-opt")) {
                Constants.KAPPA_DIRECT_OPTS = true;
            }

            if (commandLine.hasOption("v") || commandLine.hasOption("version")) {
                System.out.println("OS version: " + Utils.getOperatingSystemType()
                        + "\nBuild version " + (Constants.CASTD_VERSION + Constants.CASTD_BUILD_VERSION));
                outArgs[2] = Constants.HAS_VERSION_ARG;
            }
            if (commandLine.hasOption("h") || commandLine.hasOption("help")) {
                String header = "               Options, flags and arguments may be in any order";
                String footer = "\ncASTD is a tool that helps to generate source and executable code \n"
                        + "in language (C++, Java, Ocaml, Zeek) from ASTD specifications.\n\n";
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("cASTD", header, options, footer, true);
                outArgs[3] = Constants.HAS_HELP_ARG;
            }

            if (commandLine.hasOption("c") || commandLine.hasOption("condition-opt")) {
                Constants.COND_OPT_OPTS = true;
            }
            
            if(commandLine.hasOption("e") || commandLine.hasOption("show-exec-state")) {
                Constants.EXEC_STATE_ACTIVATED = true;
            }

            if (commandLine.hasOption("f")) {
                outArgs[4] = commandLine.getOptionValue("f");
            }

            if(commandLine.hasOption("format")) {
                outArgs[4] = commandLine.getOptionValue("format");
            }

            if (commandLine.hasOption("m")) {
                outArgs[5] = commandLine.getOptionValue("m");
            }

            if(commandLine.hasOption("main")) {
                outArgs[5] = commandLine.getOptionValue("main");
            }

            if (commandLine.hasOption("L")) {
                outArgs[6] = commandLine.getOptionValue("L");
            }

            if(commandLine.hasOption("lib")) {
                outArgs[6] = commandLine.getOptionValue("lib");
            }

            if (commandLine.hasOption("I")) {
                outArgs[7] = commandLine.getOptionValue("I");
            }

            if(commandLine.hasOption("include")) {
                outArgs[7] = commandLine.getOptionValue("include");
            }
            if(commandLine.hasOption("step")){
                outArgs[8] = commandLine.getOptionValue("step");
            }
            if(commandLine.hasOption("sf") || commandLine.hasOption("step_as_flow")){
               Constants.STEP_AS_FLOW = true;
            }
            if(commandLine.hasOption("ts") || commandLine.hasOption("time_simulation")) {
                Constants.TIMED_SIMULATION = true;
            }
            if(commandLine.hasOption("it")) {
                outArgs[9] = commandLine.getOptionValue("it");
            }
            if(commandLine.hasOption("initial_time")){
                outArgs[9] = commandLine.getOptionValue("initial_time");
            }
        }
        catch (ParseException exception) {
            if(Constants.DEBUG) System.out.println("Parse Error: " + exception.getMessage());
        }

        return outArgs;
    }
}
