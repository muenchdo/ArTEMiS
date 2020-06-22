export const programmingExerciseProblemStatementC =
    '#### General Tasks\n' +
    '1. [task][Compile](TestCompile)\n' +
    '\n' +
    '#### Adress Sanitizer\n' +
    '1. [task][Compile Address Sanitizer](TestCompileASan)\n' +
    '\n' +
    '#### Undefined Behavior Sanitizer\n' +
    '1. [task][Compile Undefined Behavior Sanitizer](TestCompileUBSan)\n' +
    '\n' +
    '#### Leak Sanitizer\n' +
    '1. [task][Compile Leak Sanitizer](TestCompileLeak)';

export const buildErrorContentC = {
    newFiles: [],
    content: [
        {
            fileName: 'rotX.c',
            fileContent: 'a',
        },
    ],
};

export const someSuccessfulErrorContentC = {
    newFiles: [],
    content: [
        {
            fileName: 'rotX.c',
            fileContent: 'int main(void) {\n' + '\treturn 0; // Success\n' + '}\n',
        },
    ],
};

export const allSuccessfulContentC = {
    newFiles: [],
    content: [
        {
            fileName: 'rotX.c',
            fileContent:
                '#include <ctype.h> // isalpha(...), isupper(...)\n' +
                '#include <stdlib.h> // size_t\n' +
                '#include <unistd.h> // read(...)\n' +
                '#include <stdio.h> // printf(...)\n' +
                '\n' +
                '#define MAX_BUFFER_SIZE 1024\n' +
                '\n' +
                'char rotX(char in, unsigned rot);\n' +
                'unsigned readRotCount();\n' +
                '\n' +
                'char rotX(char in, unsigned rot) {\n' +
                '\tif(isalpha(in)) { // We only want to convert alphabet characters\n' +
                '\t\tif(isupper(in)) {\n' +
                "\t\t\treturn 'A' + ((in - 'A') + rot) % 26;\n" +
                '\t\t}\n' +
                "\t\treturn 'a' + ((in - 'a') + rot) % 26;\n" +
                '\t}\n' +
                '\treturn in;\n' +
                '}\n' +
                '\n' +
                'unsigned readRotCount() {\n' +
                '\tint rot = -1;\n' +
                '\tdo\n' +
                '\t{   \n' +
                '\t\tprintf("Enter Rot:\\n");\n' +
                '\t\tfflush(stdout);\n' +
                '\t\tif(!scanf("%i", &rot)) {\n' +
                '\t\t\t// Clear input if user did not enter a valid int:\n' +
                '\t\t\tint c;\n' +
                "\t\t\twhile ((c = getchar()) != '\\n' && c != EOF);\n" +
                '\t\t}\n' +
                '\t} while (rot < 0);\n' +
                '\treturn (unsigned)rot;\n' +
                '}\n' +
                '\n' +
                'int main() {\n' +
                '\tunsigned rot = readRotCount();\n' +
                '\tchar buff[MAX_BUFFER_SIZE];\n' +
                '\n' +
                '\tprintf("Enter text:\\n");\n' +
                "\t// Read MAX_BUFFER_SIZE - 1 chars. Don't forget about the '\0' at the end!\n" +
                '\tsize_t n = read(STDIN_FILENO, buff, MAX_BUFFER_SIZE - 1);\n' +
                '\tfor (size_t i = 0; i < n && buff[i]; i++)\n' +
                '\t{\n' +
                '\t\t// Replace character by character:\n' +
                '\t\tbuff[i] = rotX(buff[i], rot);\n' +
                '\t}\n' +
                '\t// Print the result:\n' +
                '\tprintf("%s", buff);\n' +
                '\t\n' +
                '}\n',
        },
    ],
};
