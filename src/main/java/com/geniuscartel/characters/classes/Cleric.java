package com.geniuscartel.characters.classes;

import com.geniuscartel.characters.EQCharacter;
import com.geniuscartel.characters.ShortClass;
import com.geniuscartel.characters.classes.archetypes.Healer;
import com.geniuscartel.characters.classes.archetypes.ManaUser;
import com.geniuscartel.workers.characterworkers.CharacterManager;

import java.util.ArrayList;
import java.util.List;

public class Cleric extends EQCharacter implements Healer, ManaUser{
    private ShortClass className = ShortClass.CLR;

    public Cleric(String name, String[] NBPacket, CharacterManager boss) {
        super(name, NBPacket, boss);
    }

    @Override
    public void restStateAction() {
        synchronized (this) {
            try {
                //int clrevo = Integer.parseInt(queryForInfo("${Me.Skill[Evocation]}"));
                //if (clrevo <= 300){
                    //rawSlashCommand("/casting 0014 -targetid|9");
                //}
                String harmonyofspirit = queryForInfo("${Me.Buff[Harmony of Spirit III].Duration}");
                if (harmonyofspirit.equals("NULL")){
                    rawSlashCommand("/casting 9073");
                }

                if ((getStatus().getCasting() == -1) && queryForInfo("${NetBots[Nekclr].Sitting}").equals("FALSE") && queryForInfo("${Stick.Status}").equals("OFF")){
                    rawSlashCommand("/sit");}

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

                String harmonyofspirit = queryForInfo("${Me.Buff[Harmony of Spirit III].Duration}");
                if (harmonyofspirit.equals("NULL")){
                    rawSlashCommand("/casting 9073");
                }

                String tankhot = queryForInfo("${NetBots[Neksk].Buff}");
                String tankID = queryForInfo("${NetBots[Neksk].ID}");
                if (!tankhot.contains("5259")){
                    rawSlashCommand("/casting 5259 -targetid|${NetBots[Neksk].ID}");
                }

                if ((getStatus().getCasting() == -1) && queryForInfo("${NetBots[Nekclr].Sitting}").equals("FALSE") && queryForInfo("${Stick.Status}").equals("OFF")){
                    rawSlashCommand("/sit");}

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

    }

    @Override
    public List<String> getManaRegenEffects() {
        return new ArrayList<>();
    }


}
