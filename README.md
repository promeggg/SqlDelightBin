# SqlDelightBin

A runnable jar that uses SqlDelight-Compiler to compile SQL into Java.

## Usage

1. `cd` into the module root directory, generally is the parent directory of `src`;
2. put your `*.sq` inside `src/main/sqldelight` (it's stronly recommended that you
use the official SqlDelight gradle plugin to debug your SQL statements first);
3. run `java -jar <path to SqlDelightBin jar> src/main/sqldelight`, the generated
Java file will be in `build/generated/source/sqldelight/` dir;
