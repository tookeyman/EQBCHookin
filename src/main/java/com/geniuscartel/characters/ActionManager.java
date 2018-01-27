package com.geniuscartel.characters;

import com.geniuscartel.characters.services.CommandFactory;

public class ActionManager {
    private EQCharacter ME;

    public ActionManager(EQCharacter me) {
        this.ME = me;
    }

    public void cast(String spellID) {//sends a command to the character to cast a spell... real basic
        final CommandFactory.MultiStageCommand casting = createCastCommand(String.format("//casting \"%s\"", spellID));
        ME.enqueCommand(casting);
    }

    public void castAtTarget(String spellID, int ID) {
        final CommandFactory.MultiStageCommand command = createCastCommand(String.format("/casting \"%s\" -targetID|%d", spellID, ID));
        ME.enqueCommand(command);
    }

    private CommandFactory.MultiStageCommand createCastCommand(String commandString){
        //creates a short macro to cast a spell
        final CommandFactory.MultiStageCommand macro = CommandFactory.createMultiStageCommand(CharacterState.ANY);
        final Command slashCommand = CommandFactory.createSimpleCommand(() -> ME.rawSlashCommand(commandString));
        macro.addCommand(slashCommand);
        macro.addCommand(waitForCasting);
        macro.addCommand(waitForCastingFinished);
        //todo rewrite to support mq2cast and check for result success
        return macro;
    }

    private CommandFactory.SimpleCommand waitForCasting = CommandFactory.createSimpleCommand(()->{
        synchronized (ME) {
            try {
                while (ME.getStatus().getCasting() == -1) {
                    ME.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    private CommandFactory.SimpleCommand waitForCastingFinished = CommandFactory.createSimpleCommand(() -> {
        synchronized (ME) {
            while (ME.getStatus().getCasting() > -1) {
                try {
                    ME.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });
}
