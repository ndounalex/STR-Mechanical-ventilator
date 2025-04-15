package com.udes.utils;

public class getopt4windows {

    public static String getopt4windows = "#ifndef GETOPT_H\n" +
            "#define GETOPT_H\n" +
            "\n" +
            "#include <stdio.h>\n" +
            "#include <string.h>\n" +
            "#include <windows.h>\n" +
            "\n" +
            "/* Constants */\n" +
            "#define no_argument        0\n" +
            "#define required_argument  1\n" +
            "#define optional_argument  2\n" +
            "\n" +
            "/* Variables */\n" +
            "extern int opterr;\n" +
            "extern int optind;\n" +
            "extern int optopt;\n" +
            "extern char *optarg;\n" +
            "\n" +
            "/* Functions */\n" +
            "int getopt(int argc, char * const argv[], const char *optstring);\n" +
            "int getopt_long(int argc, char * const argv[], const char *optstring,\n" +
            "                const struct option *longopts, int *longindex);\n" +
            "int getopt_long_only(int argc, char * const argv[], const char *optstring,\n" +
            "                     const struct option *longopts, int *longindex);\n" +
            "\n" +
            "/* Structs */\n" +
            "struct option {\n" +
            "    const char *name;\n" +
            "    int         has_arg;\n" +
            "    int        *flag;\n" +
            "    int         val;\n" +
            "};\n" +
            "\n" +
            "/* Variables */\n" +
            "static int optind = 1;\n" +
            "static int opterr = 1;\n" +
            "static int optopt;\n" +
            "\n" +
            "static char *optarg = NULL;\n" +
            "\n" +
            "/* Functions */\n" +
            "\n" +
            "int getopt(int argc, char * const argv[], const char *optstring)\n" +
            "{\n" +
            "    static char *next = NULL;\n" +
            "    if (optind >= argc) {\n" +
            "        return -1;\n" +
            "    }\n" +
            "    if (!next || !*next) {\n" +
            "        next = argv[optind];\n" +
            "        if (*next != '-') {\n" +
            "            return -1;\n" +
            "        }\n" +
            "        next++;\n" +
            "        if (*next == '\\0') {\n" +
            "            optind++;\n" +
            "            return -1;\n" +
            "        }\n" +
            "    }\n" +
            "    optopt = *next++;\n" +
            "    const char *p = strchr(optstring, optopt);\n" +
            "    if (!p) {\n" +
            "        if (opterr) {\n" +
            "            fprintf(stderr, \"Unknown option '-%c'\\n\", optopt);\n" +
            "        }\n" +
            "        return '?';\n" +
            "    }\n" +
            "    if (p[1] == ':') {\n" +
            "        if (*next) {\n" +
            "            optarg = next;\n" +
            "        } else if (optind < argc) {\n" +
            "            optarg = argv[optind];\n" +
            "            optind++;\n" +
            "        } else {\n" +
            "            if (opterr) {\n" +
            "                fprintf(stderr, \"Option '-%c' requires an argument\\n\", optopt);\n" +
            "            }\n" +
            "            return ':';\n" +
            "        }\n" +
            "        next = NULL;\n" +
            "    }\n" +
            "    return optopt;\n" +
            "}\n" +
            "\n" +
            "int getopt_long(int argc, char * const argv[], const char *optstring,\n" +
            "                const struct option *longopts, int *longindex)\n" +
            "{\n" +
            "    int option_index = -1;\n" +
            "    const char *opt_ptr = NULL;\n" +
            "    int opt_len = 0;\n" +
            "    if (optind >= argc) {\n" +
            "        return -1;\n" +
            "    }\n" +
            "    if (!strncmp(argv[optind], \"--\", 2)) {\n" +
            "        opt_ptr = argv[optind] + 2;\n" +
            "        opt_len = strlen(opt_ptr);\n" +
            "        optind++;\n" +
            "        for (int i = 0; longopts[i].name != NULL; i++) {\n" +
            "            if (strncmp(opt_ptr, longopts[i].name, opt_len) == 0) {\n" +
            "                if (opt_len == strlen(longopts[i].name)) {\n" +
            "                    option_index = i;\n" +
            "                    break;\n" +
            "                }\n" +
            "                if (option_index == -1) {\n" +
            "                    option_index = i;\n" +
            "                } else {\n" +
            "                    if (opterr) {\n" +
            "                        fprintf(stderr, \"Ambiguous option '%s'\\n\", opt_ptr);\n" +
            "                    }\n" +
            "                    return '?';\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "        if (option_index == -1) {\n" +
            "            if (opterr) {\n" +
            "                fprintf(stderr, \"Unknown option '%s'\\n\", opt_ptr);\n" +
            "            }\n" +
            "            return '?';\n" +
            "        }\n" +
            "        if (longopts[option_index].has_arg == required_argument ||\n" +
            "            longopts[option_index].has_arg == optional_argument) {\n" +
            "            if (optind < argc && *argv[optind] != '-') {\n" +
            "                optarg = argv[optind];\n" +
            "                optind++;\n" +
            "            } else if (longopts[option_index].has_arg == required_argument) {\n" +
            "                if (opterr) {\n" +
            "                    fprintf(stderr, \"Option '%s' requires an argument\\n\", opt_ptr);\n" +
            "                }\n" +
            "                return ':';\n" +
            "            }\n" +
            "        }\n" +
            "        if (longindex) {\n" +
            "            *longindex = option_index;\n" +
            "        }\n" +
            "        return longopts[option_index].val;\n" +
            "    } else {\n" +
            "        return getopt(argc, argv, optstring);\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "\n" +
            "int getopt_long_only(int argc, char * const argv[], const char *optstring,\n" +
            "                     const struct option *longopts, int *longindex)\n" +
            "{\n" +
            "    int option_index = -1;\n" +
            "    const char *opt_ptr = NULL;\n" +
            "    int opt_len = 0;\n" +
            "    if (optind >= argc) {\n" +
            "        return -1;\n" +
            "    }\n" +
            "    if (!strncmp(argv[optind], \"--\", 2)) {\n" +
            "        opt_ptr = argv[optind] + 2;\n" +
            "        opt_len = strlen(opt_ptr);\n" +
            "        optind++;\n" +
            "        for (int i = 0; longopts[i].name != NULL; i++) {\n" +
            "            if (strncmp(opt_ptr, longopts[i].name, opt_len) == 0) {\n" +
            "                if (opt_len == strlen(longopts[i].name)) {\n" +
            "                    option_index = i;\n" +
            "                    break;\n" +
            "                }\n" +
            "            if (option_index == -1) {\n" +
            "                option_index = i;\n" +
            "            } else {\n" +
            "                if (opterr) {\n" +
            "                    fprintf(stderr, \"Ambiguous option '%s'\\n\", opt_ptr);\n" +
            "                }\n" +
            "                return '?';\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "    if (option_index == -1) {\n" +
            "        return getopt(argc, argv, optstring);\n" +
            "    }\n" +
            "    if (longopts[option_index].has_arg == required_argument ||\n" +
            "        longopts[option_index].has_arg == optional_argument) {\n" +
            "        if (optind < argc && *argv[optind] != '-') {\n" +
            "            optarg = argv[optind];\n" +
            "            optind++;\n" +
            "        } else if (longopts[option_index].has_arg == required_argument) {\n" +
            "            if (opterr) {\n" +
            "                fprintf(stderr, \"Option '%s' requires an argument\\n\", opt_ptr);\n" +
            "            }\n" +
            "        return ':';\n" +
            "        }\n" +
            "    }\n" +
            "    if (longindex) {\n" +
            "        *longindex = option_index;\n" +
            "    }\n" +
            "    return longopts[option_index].val;\n" +
            "    } else {\n" +
            "    return getopt(argc, argv, optstring);\n" +
            "    }\n" +
            "}\n" +
            "\n" +
            "#endif /* GETOPT_H */";
}
