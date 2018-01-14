package com.geniuscartel.Toon.classes;

import com.geniuscartel.Toon.ShortClass;
import com.geniuscartel.workers.characterworkers.CharacterManager;

public class Bard extends Character {
    private ShortClass className = ShortClass.BRD;

    public Bard(String name, String[] NBPacket, CharacterManager boss) {
        super(name, NBPacket, boss);
    }


    @Override
    public void run() {
        while(isRunning()){
            synchronized (this){
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
