package com.geniuscartel.characters.classes;

import com.geniuscartel.characters.EQCharacter;
import com.geniuscartel.characters.ShortClass;
import com.geniuscartel.workers.characterworkers.CharacterManager;

public class Ranger extends EQCharacter {
    private ShortClass className = ShortClass.RNG;

    public Ranger(String name, String[] NBPacket, CharacterManager boss) {
        super(name, NBPacket, boss);
    }

    @Override
    public void restStateAction() {
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void followStateAction() {
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void combatStateAction() {
        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
