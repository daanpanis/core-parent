package com.daanpanis.core.program;

public class Debugger {

    public static boolean debug = true;

    public static void println(Object message) {
        if (debug)
            System.out.println(message);
    }

    public static void print(Object message) {
        if (debug)
            System.out.print(message);
    }

}
