package com.geniuscartel;

import com.geniuscartel.workers.characterworkers.CharacterManager;
import com.geniuscartel.workers.ioworkers.AsyncRequestInterop;
import com.geniuscartel.workers.ioworkers.OutputWorker;
import com.geniuscartel.workers.ioworkers.RequestWorker;

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

    private final static ExecutorService IO_THREADS = Executors.newCachedThreadPool();
    private CharacterManager characters;

    public EQBCClient() {
        try {
            this.s = new Socket("localhost", 2112);
            this.socketOut = s.getOutputStream();
            this.socketIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException e) {
            System.out.println("[CLIENT]\tCould not create socket...");
        }
        outputWorker = new OutputWorker(socketOut);


        async = new AsyncRequestInterop(IO_THREADS, outputWorker);


        characters = new CharacterManager(IO_THREADS, async);

        requestWorker = new RequestWorker<>(characters);
        requestWorker.setOutputWorker(outputWorker);
        requestWorker.setAsync(async);
        outputWorker.setRequestWorker(requestWorker);

        IO_THREADS.execute(outputWorker);
        IO_THREADS.execute(requestWorker);


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
        System.out.println("[CLIENT]\tWorkers shut down");
    }

    public void run() {
        outputWorker.login();
        boolean triggered = false;
        while (running) {
            String currentRequest = null;


            if(!triggered){

                triggered = true;
            }


            try {
                currentRequest = socketIn.readLine();
                requestWorker.addToQue(currentRequest);
            } catch (IOException e) {
                System.out.println("[CLIENT]\tTried to pass a fucked up string to request worker: " + currentRequest);
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
