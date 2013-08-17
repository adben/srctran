package com.srctran.backend.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.github.antlrjavaparser.CompilationUnitListener;
import com.github.antlrjavaparser.Java7Lexer;
import com.github.antlrjavaparser.Java7Parser;
import com.github.antlrjavaparser.Java7Parser.CompilationUnitContext;
import com.github.antlrjavaparser.api.CompilationUnit;

public class Adapter {

  public static void main(String[] args) throws IOException {
    Java7Lexer lexer =
        // new Java7Lexer(new ANTLRInputStream("class A { String a = \"a\" + \"b\" + \"c\"; }"));
        new Java7Lexer(new ANTLRFileStream(
            "target/generated-sources/antlr4/com/srctran/backend/parser/antlr4/JavaParser.java"));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    Java7Parser parser = new Java7Parser(tokens);
    long time = System.currentTimeMillis();
    CompilationUnitContext unit = parser.compilationUnit();
    System.out.println("Time for parsing: " + (System.currentTimeMillis() - time) / 1000.0);

    ParseTreeWalker walker = new ParseTreeWalker();
    CompilationUnitListener listener = new CompilationUnitListener(tokens);
    walker.walk(listener, unit);

    CompilationUnit compilationUnit = listener.getCompilationUnit();
    //System.out.println(compilationUnit);

    serialize(compilationUnit);
  }

  private static <T> T serialize(T source) throws IOException {
    Kryo kryo = new Kryo();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    GZIPOutputStream zipOutputStream = new GZIPOutputStream(outputStream);
    Output output = new Output(zipOutputStream);
    long time = System.currentTimeMillis();
    kryo.writeClassAndObject(output, source);
    System.out.println("Time for serializing: " + (System.currentTimeMillis() - time) / 1000.0);
    output.close();

    byte[] buffer = outputStream.toByteArray();
    System.out.println("Size: " + buffer.length);

    InputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(buffer));
    Input input = new Input(inputStream);
    time = System.currentTimeMillis();
    @SuppressWarnings("unchecked")
    T t = (T) kryo.readClassAndObject(input);
    System.out.println("Time for deserializing: " + (System.currentTimeMillis() - time) / 1000.0);
    input.close();

    return t;
  }
}
