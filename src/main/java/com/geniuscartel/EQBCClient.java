package com.geniuscartel;

import com.geniuscartel.workers.AsyncRequestInterop;
import com.geniuscartel.workers.OutputWorker;
import com.geniuscartel.workers.RequestWorker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.*;

public class EQBCClient {
    private Socket s;
    private OutputStream socketOut;
    private BufferedReader socketIn;

    private boolean running = true;

    private RequestWorker<String> requestWorker;
    private OutputWorker outputWorker;
    private AsyncRequestInterop async;

    private ExecutorService IO_THREADS = Executors.newCachedThreadPool();

    public EQBCClient() {
        try {
            this.s = new Socket("localhost", 2112);
            this.socketOut = s.getOutputStream();
            this.socketIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException e) {
            System.out.println("Could not create socket...");
        }
        requestWorker = new RequestWorker<>();
        outputWorker = new OutputWorker(socketOut);
        async = new AsyncRequestInterop(IO_THREADS, outputWorker);

        requestWorker.setOutputWorker(outputWorker);
        outputWorker.setRequestWorker(requestWorker);

        requestWorker.setAsync(async);

        IO_THREADS.execute(requestWorker);
        IO_THREADS.execute(outputWorker);
    }


    public void shutDownWorkers(){
        //todo create a mechanism for shutting down the orchestrator from a character
        outputWorker.setRunning(false);
        requestWorker.setRunning(false);
        outputWorker.notifyQue();
        requestWorker.notifyQue();
        IO_THREADS.shutdown();
        try {
            IO_THREADS.awaitTermination(1, TimeUnit.MINUTES);
            s.close();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        System.out.println("Workers shut down");
    }

    public void run() {
        outputWorker.login();
        boolean triggered = false;
        while (running) {
            String currentRequest = null;
            if(!triggered){
                IO_THREADS.execute(testMessages());
                triggered = true;
            }
            try {
                currentRequest = socketIn.readLine();
                requestWorker.addToQue(currentRequest);
            } catch (IOException e) {
                System.out.println("Tried to pass a fucked up string to request worker: " + currentRequest);
            }
            requestWorker.notifyQue();
        }
    }

    private Runnable testMessages() {
        return () -> {
            try {
                Future f = async.submitRequest("Zomgharmtouch", "${Me.ID}");
                System.out.println(f.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        };
    }

    public void setRunning(boolean t){
        this.running = t;
    }
}
