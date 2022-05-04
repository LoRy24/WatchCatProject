package com.github.lory24.watchcatproxy.proxy.logger;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.PrintStream;
import java.util.Locale;

public class LoggerPrintStream extends PrintStream {

    private static final String newLine = System.getProperty("line.separator");

    @Getter
    private final StringBuffer buffer = new StringBuffer();

    private final PrintStream printStream;

    public LoggerPrintStream(PrintStream printStream) {
        super(System.out);
        this.printStream = printStream;
    }

    @Override
    public void print(boolean b) {
        buffer.append(b);
        printStream.print(b);
    }

    @Override
    public void print(char c) {
        buffer.append(c);
        printStream.print(c);
    }

    @Override
    public void print(int i) {
        buffer.append(i);
        printStream.print(i);
    }

    @Override
    public void print(long l) {
        buffer.append(l);
        printStream.print(l);
    }

    @Override
    public void print(float f) {
        buffer.append(f);
        printStream.print(f);
    }

    @Override
    public void print(double d) {
        buffer.append(d);
        printStream.print(d);
    }

    @Override
    public void print(@NotNull char[] s) {
        buffer.append(s);
        printStream.print(s);
    }

    @Override
    public void print(@Nullable String s) {
        buffer.append(s);
        printStream.print(s);
    }

    @Override
    public void print(@Nullable Object obj) {
        buffer.append(obj);
        printStream.print(obj);
    }

    @Override
    public void println() {
        buffer.append(newLine);
        printStream.println();
    }

    @Override
    public void println(boolean x) {
        buffer.append(x).append(newLine);
        printStream.println(x);
    }

    @Override
    public void println(char x) {
        buffer.append(x).append(newLine);
        printStream.println(x);
    }

    @Override
    public void println(int x) {
        buffer.append(x).append(newLine);
        printStream.println(x);
    }

    @Override
    public void println(long x) {
        buffer.append(x).append(newLine);
        printStream.println(x);
    }

    @Override
    public void println(float x) {
        buffer.append(x).append(newLine);
        printStream.println(x);
    }

    @Override
    public void println(double x) {
        buffer.append(x).append(newLine);
        printStream.println(x);
    }

    @Override
    public void println(@NotNull char[] x) {
        buffer.append(x).append(newLine);
        printStream.println(x);
    }

    @Override
    public void println(@Nullable String x) {
        buffer.append(x).append(newLine);
        printStream.println(x);
    }

    @Override
    public void println(@Nullable Object x) {
        buffer.append(x).append(newLine);
        printStream.println(x);
    }

    @Override
    public PrintStream printf(@NotNull String format, Object... args) {
        buffer.append(String.format(format, args));
        return printStream.printf(format, args);
    }

    @Override
    public PrintStream printf(Locale l, @NotNull String format, Object... args) {
        buffer.append(String.format(l, format, args));
        return printStream.printf(l, format, args);
    }
}
