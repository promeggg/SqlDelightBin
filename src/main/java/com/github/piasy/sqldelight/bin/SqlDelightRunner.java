package com.github.piasy.sqldelight.bin;

import com.squareup.sqldelight.SqliteCompiler;
import com.squareup.sqldelight.SqliteLexer;
import com.squareup.sqldelight.SqliteParser;
import com.squareup.sqldelight.Status;
import com.squareup.sqldelight.model.FileKt;
import com.squareup.sqldelight.types.SymbolTable;
import com.squareup.sqldelight.validation.QueryResults;
import com.squareup.sqldelight.validation.SqlDelightValidator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * Created by piasy on 6/27/16.
 */
public class SqlDelightRunner {
  public static void main(String[] argv) {
    String input = argv[0];
    String output = argv[1];
    File inputDir = new File(input);
    for (File sq : listAllFiles(inputDir)) {
      System.out.println("compiling " + sq.getAbsolutePath() + " ...");
      try {
        SqliteLexer lexer = new SqliteLexer(new ANTLRInputStream(new FileInputStream(sq)));
        SqliteParser parser = new SqliteParser(new CommonTokenStream(lexer));
        SqliteParser.ParseContext parseContext = parser.parse();

        SymbolTable symbolTable = new SymbolTable();
        String relativePath = FileKt.relativePath(sq.getAbsolutePath(), parseContext);
        symbolTable = symbolTable.plus(
            new SymbolTable(parseContext, sq.getName(), relativePath, Collections.emptyList()));
        SqlDelightValidator validator = new SqlDelightValidator();
        Status.ValidationStatus status = validator.validate(relativePath, parseContext, symbolTable);
        if (status instanceof Status.ValidationStatus.Invalid) {
          ((Status.ValidationStatus.Invalid) status).getErrors()
              .forEach(error -> System.out.println(
                  error.getOriginatingElement() + ": " + error.getErrorMessage()));
          System.exit(1);
        } else {
          List<QueryResults> queries = ((Status.ValidationStatus.Validated) status).getQueries();
          SqliteCompiler.Companion.write(parseContext, queries, relativePath, output);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static List<File> listAllFiles(File dir) {
    if (dir.listFiles() == null) {
      return Collections.emptyList();
    }
    List<File> files = new ArrayList<>();
    for (File file : dir.listFiles()) {
      if (file.isDirectory()) {
        files.addAll(listAllFiles(file));
      } else {
        files.add(file);
      }
    }
    return files;
  }
}
