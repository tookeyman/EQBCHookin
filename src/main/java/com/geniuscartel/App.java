package com.geniuscartel;

public class App {
    public static boolean verbose = false;

    public static void main( String[] args ) throws InterruptedException {
        EQBCClient client = new EQBCClient();
        Thread.sleep(1000);
        client.run();
        client.shutDownWorkers();
    }

    public static void debug(Object bug){
        System.out.printf("[DEBUG]\t%s\r\n", bug);
    }
}
