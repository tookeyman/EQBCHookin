package com.geniuscartel.characters.classes;

import com.geniuscartel.characters.EQCharacter;
import com.geniuscartel.characters.ShortClass;
import com.geniuscartel.workers.characterworkers.CharacterManager;

public class Magician extends EQCharacter {
    private ShortClass className = ShortClass.MAG;

    public Magician(String name, String[] NBPacket, CharacterManager boss) {
        super(name, NBPacket, boss);
    }

    @Override
    public void restStateAction() {
        synchronized (this) {
            try {
                /*String petID = queryForInfo("${Me.Pet.ID}");
                String petBuff = queryForInfo("${Me.PetBuff[Burnout V]}");
                int petGeared = 0;
                if (petID.equals("NULL")){
                    rawSlashCommand("/casting 3540");
                }
                if (petBuff.equals("NULL")){
                    rawSlashCommand("/casting 3237");
                }*/

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
