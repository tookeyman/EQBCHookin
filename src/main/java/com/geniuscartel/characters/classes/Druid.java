package com.geniuscartel.characters.classes;

import com.geniuscartel.characters.ShortClass;
import com.geniuscartel.workers.characterworkers.CharacterManager;

public class Druid extends Character {
    private ShortClass className = ShortClass.DRU;

    public Druid(String name, String[] NBPacket, CharacterManager boss) {
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


}