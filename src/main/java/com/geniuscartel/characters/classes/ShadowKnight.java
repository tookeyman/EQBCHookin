package com.geniuscartel.characters.classes;

import com.geniuscartel.characters.ShortClass;
import com.geniuscartel.workers.characterworkers.CharacterManager;

import java.util.List;
import java.util.function.Consumer;

public class ShadowKnight extends Character {
    private ShortClass className = ShortClass.SHD;

    public ShadowKnight(String name, String[] NBPacket, CharacterManager boss) {
        super(name, NBPacket, boss);
    }

    @Override
    void restStateAction() {
        synchronized (this) {
            try {
                checkBuffs();
                super.sendCommand(getName(), "//echo resting");
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    void followStateAction() {
        synchronized (this) {
            try {
                super.sendCommand(getName(), "//echo following");
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    void combatStateAction() {
        synchronized (this) {
            try {
                super.sendCommand(getName(), "//echo fighting");
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void checkBuffs(){
        super.getSelfBuffs().forEach(checkBuff);
    }

    private Consumer<Integer> checkBuff = x->{
        List<Integer> spells = super.getBuffs();
        if(!spells.contains(x)){
            super.cast(x);
        }
    };

}
