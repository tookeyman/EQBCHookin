package com.geniuscartel.characters.services;

import com.geniuscartel.characters.CharacterState;
import com.geniuscartel.characters.Command;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandFactory {
    public static BuffCommand createBuffCommand(BuffService.BuffRequest br) {
        return new BuffCommand() {
            @Override
            public void apply() {
            int buffID = Integer.parseInt(br.getRequester().queryForInfo(String.format("${Spell[%s].ID}", br.getSpellName())));
                if(br.getRequester().getStatus().getBuffs() == null) {
                    return;
                }
                if(br.getRequester().getStatus().getBuffs().contains(buffID)) {
                br.getReleaseRequest().run();
                return;
            }
            br.getCaster().getActionManager().castAtTarget(br.getSpellName(), br.getRequester().getStatus().getId());
            br.getReleaseRequest().run();
            }
        };
    }

    public static MultiStageCommand createMultiStageCommand(CharacterState state){
        return new MultiStageCommand() {
            @Override
            public CharacterState getSTATE() {
                return state;
            }
        };
    }

    public static SimpleCommand createSimpleCommand(Runnable r, CharacterState state){
        return new SimpleCommand() {
            @Override
            public void apply() {
                r.run();
            }

            @Override
            public CharacterState getSTATE() {
                return state;
            }
        };
    }

    public static SimpleCommand createSimpleCommand(Runnable r){
        return createSimpleCommand(r, CharacterState.ANY);
    }

    public abstract static class BuffCommand implements Command {
        @Override
        public CharacterState getSTATE() {
            return CharacterState.REST;
        }
    }

    public abstract static class SimpleCommand implements Command { }

    public abstract static class MultiStageCommand implements Command {
        private Deque<Command> eventQue = new ArrayDeque<>();

        public void addCommand(Command c){
            this.eventQue.add(c);
        }

        @Override
        public void apply() {
            while(eventQue.size()>0){
                eventQue.removeFirst().apply();
            }
        }
    }
}

