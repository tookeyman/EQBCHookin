package com.geniuscartel.characters.classes;

import com.geniuscartel.characters.ShortClass;
import com.geniuscartel.characters.classes.archetypes.Buffer;
import com.geniuscartel.characters.classes.archetypes.Healer;
import com.geniuscartel.characters.classes.archetypes.ManaUser;
import com.geniuscartel.workers.characterworkers.CharacterManager;

import java.util.ArrayList;
import java.util.List;

public class Cleric extends Character implements Healer, Buffer, ManaUser{
    private ShortClass className = ShortClass.CLR;

    public Cleric(String name, String[] NBPacket, CharacterManager boss) {
        super(name, NBPacket, boss);
    }

    @Override
    void restStateAction() {
        synchronized (this) {
            try {
                watchMana();
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
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void efficientHeal(int targetID) {

    }

    @Override
    public void heavyHeal(int targetID) {

    }

    @Override
    public void groupHeal(int targetID) {

    }

    @Override
    public void watchMana() {
        if(getStats().pctMana() < 50.0){
            if(queryForInfo("${Me.Standing}").equals("TRUE"))
                command("/sit");
        }
    }

    @Override
    public List<String> getManaRegenEffects() {
        return new ArrayList<>();
    }

    @Override
    public List<Integer> getAvailableBuffs() {
        return new ArrayList<>();
    }
}
