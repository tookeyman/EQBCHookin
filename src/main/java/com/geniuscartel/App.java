package com.geniuscartel;

public class App {

    public static void main( String[] args ) throws InterruptedException {
        EQBCClient client = new EQBCClient();
        Thread.sleep(1000);
        client.run();
        client.shutDownWorkers();
    }

}
