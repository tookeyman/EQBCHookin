package com.geniuscartel.Toon.classes;

import com.geniuscartel.Toon.ShortClass;
import com.geniuscartel.Toon.Toon;
import com.geniuscartel.workers.CharacterManager;

public class ShadowKnight extends Toon {
    private ShortClass className = ShortClass.SHD;

    public ShadowKnight(String name, String[] NBPacket, CharacterManager boss) {
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
