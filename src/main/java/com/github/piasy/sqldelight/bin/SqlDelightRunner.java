package com.github.piasy.sqldelight.bin;

import com.squareup.javapoet.JavaFile;
import com.squareup.sqldelight.SqliteCompiler;
import com.squareup.sqldelight.SqliteLexer;
import com.squareup.sqldelight.SqliteParser;
import com.squareup.sqldelight.Status;
import com.squareup.sqldelight.model.FileKt;
import com.squareup.sqldelight.resolution.query.QueryResults;
import com.squareup.sqldelight.types.SymbolTable;
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
        String relativePath =
            joinToString(FileKt.relativePath(sq.getAbsolutePath(), File.separatorChar),
                File.separator);
        symbolTable = symbolTable.plus(
            new SymbolTable(parseContext, sq.getName(), relativePath, Collections.emptyList()));
        SqlDelightValidator validator = new SqlDelightValidator();
        Status.ValidationStatus validationStatus =
            validator.validate(relativePath, parseContext, symbolTable);
        if (validationStatus instanceof Status.ValidationStatus.Invalid) {
          ((Status.ValidationStatus.Invalid) validationStatus).getErrors()
              .forEach(error -> System.out.println(
                  error.getOriginatingElement() + ": " + error.getErrorMessage()));
          System.exit(1);
        }
        List<QueryResults> queries =
            ((Status.ValidationStatus.Validated) validationStatus).getQueries();
        Status compileStatus = SqliteCompiler.Companion.compile(parseContext, queries,
            ((Status.ValidationStatus.Validated) validationStatus).getViews(), relativePath);
        if (compileStatus instanceof Status.Failure) {
          System.out.println(compileStatus.getOriginatingElement()
              + ": " + ((Status.Failure) compileStatus).getErrorMessage());
          System.exit(1);
        } else if (compileStatus instanceof Status.Success) {
          JavaFile.builder(joinToString(
              dropLast(FileKt.relativePath(sq.getAbsolutePath(), File.separatorChar), 1), "."),
              ((Status.Success) compileStatus).getModel()).build()
              .writeTo(new File(output));
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static String joinToString(List<String> list, String separator) {
    String result = "";
    for (int i = 0, size = list.size(); i < size; i++) {
      result += list.get(i);
      if (i != size - 1) {
        result += separator;
      }
    }
    return result;
  }

  private static <T> List<T> dropLast(List<T> list, int n) {
    ArrayList<T> dropped = new ArrayList<>(list.size() - n);
    for (int i = 0, last = list.size() - n; i < last; i++) {
      dropped.add(list.get(i));
    }
    return dropped;
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
