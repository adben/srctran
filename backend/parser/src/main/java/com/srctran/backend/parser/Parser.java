package com.srctran.backend.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.objenesis.strategy.StdInstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.srctran.backend.parser.antlr4.JavaLexer;
import com.srctran.backend.parser.antlr4.JavaParser;
import com.srctran.backend.parser.antlr4.JavaParser.CompilationUnitContext;

public class Parser {

  public static void main(String[] args) throws IOException {

    JavaLexer lexer =
        new JavaLexer(new ANTLRFileStream(
            "target/generated-sources/antlr4/com/srctran/backend/parser/antlr4/JavaParser.java"));
    /*JavaLexer lexer =
        new JavaLexer(new ANTLRInputStream("class A { String a = \"a\" + \"b\" + \"c\"; }"));*/
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    JavaParser parser = new JavaParser(tokens);

    long time = System.currentTimeMillis();
    CompilationUnitContext context = parser.compilationUnit();
    System.out.println("Time for parsing: " + (System.currentTimeMillis() - time) / 1000.0);

    context = serialize(context);
    // System.out.println(context.toStringTree(parser));
  }

  private static <T> T serialize(T source) throws IOException {
    Kryo kryo = new Kryo() {
      @Override
      @SuppressWarnings({ "hiding", "rawtypes" })
      public <T> T readObject (Input input, Class<T> type, Serializer serializer) {
        if (CopyOnWriteArrayList.class.isAssignableFrom(type)) {
          return null;
        }
        return super.readObject(input, type, serializer);
      }

      @Override
      @SuppressWarnings({ "rawtypes" })
      public void writeObject (Output output, Object object, Serializer serializer) {
        if (CopyOnWriteArrayList.class.isInstance(object)) {
          return;
        }
        super.writeObject(output, object, serializer);
      }
    };
    kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());

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
