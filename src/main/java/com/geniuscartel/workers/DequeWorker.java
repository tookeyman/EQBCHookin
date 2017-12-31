package com.geniuscartel.workers;

import java.util.ArrayDeque;

public abstract class DequeWorker<T> implements Runnable{
    private final ArrayDeque<T> queue = new ArrayDeque<T>();
    private boolean running = true;

    public void setRunning(boolean t){
        this.running = t;
    }

    public void addToQue(T item){
        synchronized (queue){
            queue.add(item);
        }
    }

    public void notifyQue(){
        synchronized (queue){
            this.queue.notify();
        }
    }

    @Override
    public void run() {
        while (running || !queue.isEmpty()) {
            synchronized (queue) {
                while(queue.isEmpty()){
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                        //do nothing
                    }
                    if(!running && queue.isEmpty())return;
                }
                doWorkerTask(queue.removeFirst());
            }
        }
        System.out.println("run over");
    }

    abstract void doWorkerTask(T item);

    public int queLength() {
        synchronized (queue) {
            return this.queue.size();
        }
    }
}
