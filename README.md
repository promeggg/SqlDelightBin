# SqlDelightBin

A runnable jar that uses SqlDelight-Compiler to compile SQL into Java.

## Usage

1. put your `*.sq` inside `src/main/sqldelight` (it's stronly recommended that you
use the official SqlDelight gradle plugin to debug your SQL statements first);
3. run `java -jar <path to SqlDelightBin jar> <path to src/main/sqldelight> <output dir>`, the generated
Java file will be in `<output dir>/build/generated/source/sqldelight/` dir;
