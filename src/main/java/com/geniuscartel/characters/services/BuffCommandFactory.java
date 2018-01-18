package com.geniuscartel.characters.services;

public class BuffCommandFactory {
    public static BuffCommand create(BuffRequest br) {
        return new BuffCommand() {
            @Override
            public void apply() {
                int buffID = Integer.parseInt(br.getRequester().queryForInfo(String.format("${Spell[%s].ID}", br.getSpellName())));
                if(br.getRequester().getBuffs().contains(buffID)) {
                    br.getReleaseRequest().run();
                    return;
                }
                final String buffCommand = String.format("/casting \"%s\" -targetID|%d", br.getSpellName(), br.getRequester().getId());
                br.getCaster().slashCommand(buffCommand);
                br.getReleaseRequest().run();
            }
        };
    }
}
