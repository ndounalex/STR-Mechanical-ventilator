import sys


def main():
    file1 = sys.argv[1]
    file2 = sys.argv[2]
    file1 = open(file1, "r")
    file2 = open(file2, "r")
    lines1 = file1.readlines()
    lines2 = file2.readlines()
    if len(lines1) != len(lines2):
        print("failure")
        exit(1)
    elif len(lines1) == 0:
        print("success")
        exit(0)
    for i in range(len(lines1)):
        if lines1[i].strip() != lines2[i].strip():
            print("failure")
            exit(1)
        print("success")
        exit(0)


if __name__ == "__main__":
    main()
