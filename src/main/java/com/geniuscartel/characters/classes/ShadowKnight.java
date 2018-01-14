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
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    void followStateAction() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    void combatStateAction() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
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
