package com.geniuscartel.characters.services;

import com.geniuscartel.characters.CharacterState;
import com.geniuscartel.characters.Command;
import com.geniuscartel.characters.EQCharacter;
import com.geniuscartel.characters.Location;

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

    public static Command emptyCommand(){
        return createSimpleCommand(() ->{
            //does nothing
        });
    }

    public static MovementCommand createMovementCommand(Location loc, EQCharacter mover){
        MovementCommand movementMacro = new MovementCommand() {
            @Override
            public CharacterState getSTATE() {
                return CharacterState.FOLLOWING;
            }
        };
        movementMacro.addCommand(createSimpleCommand(() -> {
            String command = String.format("/moveto loc %f %f", loc.getY(), loc.getX());
            mover.rawSlashCommand(command);
        }));
        movementMacro.addCommand(createSimpleCommand(() -> {
            synchronized (movementMacro) {
                while (mover.queryForInfo("${Me.Moving}").equals("FALSE")) {
                    try {
                        movementMacro.wait(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }));
        return movementMacro;
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

    public abstract static class MovementCommand extends MultiStageCommand implements Command {
        @Override
        public void apply() {
            super.apply();
        }
    }
}

