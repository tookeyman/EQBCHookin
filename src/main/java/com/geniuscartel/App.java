package com.geniuscartel;


import com.geniuscartel.workers.AsyncRequestInterop;

public class App {

    public static void main( String[] args ) throws InterruptedException {
        EQBCClient client = new EQBCClient();
        Thread.sleep(1000);

        client.run();
        Thread.sleep(2000);
        client.shutDownWorkers();
    }

}
