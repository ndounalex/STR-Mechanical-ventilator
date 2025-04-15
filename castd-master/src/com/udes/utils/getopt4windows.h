#ifndef GETOPT_H
#define GETOPT_H

#include <stdio.h>
#include <string.h>
#include <windows.h>

/* Constants */
#define no_argument        0
#define required_argument  1
#define optional_argument  2

/* Variables */
extern int opterr;
extern int optind;
extern int optopt;
extern char *optarg;

/* Functions */
int getopt(int argc, char * const argv[], const char *optstring);
int getopt_long(int argc, char * const argv[], const char *optstring,
                const struct option *longopts, int *longindex);
int getopt_long_only(int argc, char * const argv[], const char *optstring,
                     const struct option *longopts, int *longindex);

/* Structs */
struct option {
    const char *name;
    int         has_arg;
    int        *flag;
    int         val;
};

/* Variables */
static int optind = 1;
static int opterr = 1;
static int optopt;

static char *optarg = NULL;

/* Functions */

int getopt(int argc, char * const argv[], const char *optstring)
{
    static char *next = NULL;
    if (optind >= argc) {
        return -1;
    }
    if (!next || !*next) {
        next = argv[optind];
        if (*next != '-') {
            return -1;
        }
        next++;
        if (*next == '\0') {
            optind++;
            return -1;
        }
    }
    optopt = *next++;
    const char *p = strchr(optstring, optopt);
    if (!p) {
        if (opterr) {
            fprintf(stderr, "Unknown option '-%c'\n", optopt);
        }
        return '?';
    }
    if (p[1] == ':') {
        if (*next) {
            optarg = next;
        } else if (optind < argc) {
            optarg = argv[optind];
            optind++;
        } else {
            if (opterr) {
                fprintf(stderr, "Option '-%c' requires an argument\n", optopt);
            }
            return ':';
        }
        next = NULL;
    }
    return optopt;
}

int getopt_long(int argc, char * const argv[], const char *optstring,
                const struct option *longopts, int *longindex)
{
    int option_index = -1;
    const char *opt_ptr = NULL;
    int opt_len = 0;
    if (optind >= argc) {
        return -1;
    }
    if (!strncmp(argv[optind], "--", 2)) {
        opt_ptr = argv[optind] + 2;
        opt_len = strlen(opt_ptr);
        optind++;
        for (int i = 0; longopts[i].name != NULL; i++) {
            if (strncmp(opt_ptr, longopts[i].name, opt_len) == 0) {
                if (opt_len == strlen(longopts[i].name)) {
                    option_index = i;
                    break;
                }
                if (option_index == -1) {
                    option_index = i;
                } else {
                    if (opterr) {
                        fprintf(stderr, "Ambiguous option '%s'\n", opt_ptr);
                    }
                    return '?';
                }
            }
        }
        if (option_index == -1) {
            if (opterr) {
                fprintf(stderr, "Unknown option '%s'\n", opt_ptr);
            }
            return '?';
        }
        if (longopts[option_index].has_arg == required_argument ||
            longopts[option_index].has_arg == optional_argument) {
            if (optind < argc && *argv[optind] != '-') {
                optarg = argv[optind];
                optind++;
            } else if (longopts[option_index].has_arg == required_argument) {
                if (opterr) {
                    fprintf(stderr, "Option '%s' requires an argument\n", opt_ptr);
                }
                return ':';
            }
        }
        if (longindex) {
            *longindex = option_index;
        }
        return longopts[option_index].val;
    } else {
        return getopt(argc, argv, optstring);
    }
}


int getopt_long_only(int argc, char * const argv[], const char *optstring,
                     const struct option *longopts, int *longindex)
{
    int option_index = -1;
    const char *opt_ptr = NULL;
    int opt_len = 0;
    if (optind >= argc) {
        return -1;
    }
    if (!strncmp(argv[optind], "--", 2)) {
        opt_ptr = argv[optind] + 2;
        opt_len = strlen(opt_ptr);
        optind++;
        for (int i = 0; longopts[i].name != NULL; i++) {
            if (strncmp(opt_ptr, longopts[i].name, opt_len) == 0) {
                if (opt_len == strlen(longopts[i].name)) {
                    option_index = i;
                    break;
                }
            if (option_index == -1) {
                option_index = i;
            } else {
                if (opterr) {
                    fprintf(stderr, "Ambiguous option '%s'\n", opt_ptr);
                }
                return '?';
            }
        }
    }
    if (option_index == -1) {
        return getopt(argc, argv, optstring);
    }
    if (longopts[option_index].has_arg == required_argument ||
        longopts[option_index].has_arg == optional_argument) {
        if (optind < argc && *argv[optind] != '-') {
            optarg = argv[optind];
            optind++;
        } else if (longopts[option_index].has_arg == required_argument) {
            if (opterr) {
                fprintf(stderr, "Option '%s' requires an argument\n", opt_ptr);
            }
        return ':';
        }
    }
    if (longindex) {
        *longindex = option_index;
    }
    return longopts[option_index].val;
    } else {
    return getopt(argc, argv, optstring);
    }
}

#endif /* GETOPT_H */