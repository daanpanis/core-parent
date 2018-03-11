package com.daanpanis.core.program;

public class Debugger {

    public static boolean debug = true;

    public static void println(String message) {
        if (debug)
            System.out.println(message);
    }

    public static void print(String message) {
        if (debug)
            System.out.print(message);
    }

}
