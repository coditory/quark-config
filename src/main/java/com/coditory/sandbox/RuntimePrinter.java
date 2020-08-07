package com.coditory.sandbox;

public class RuntimePrinter {
    public static void main(String... args) {
        System.out.println("JVM: " + System.getProperty("java.version"));
        System.out.println("CPU: " + Runtime.getRuntime().availableProcessors());
        System.out.println("MEM: " + Runtime.getRuntime().maxMemory() / 1024 / 1024);
    }
}
