package com.geniuscartel.characters;

import com.geniuscartel.App;
import com.geniuscartel.characters.services.SaveService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BuffManager {
    /**
     * All this class should do is manage the buffs that you have
     * It is not supposed to indicate what buffs you have right now
     * that job is for status
     *
     * */
    private final EQCharacter me;
    private final SaveService saveService;
    private List<String> neededBuffs = null;
    private List<String> availableBuffs = null;
    private List<String> selfBuffs = null;

    public BuffManager(EQCharacter me, SaveService saveService) {
        this.me = me;
        this.saveService = saveService;
    }

    public List<String> getNeededBuffs() {//lazyily load
        if (neededBuffs == null) {
            neededBuffs = populateNeededBuffs();
        }
        return neededBuffs;
    }

    public List<String> getAvailableBuffs() {//lazyily loaded
        if (availableBuffs == null) {
            availableBuffs = populateAvailableBuffs();
        }
        return availableBuffs;
    }

    public List<String> getSelfBuffs() {//lazily loaded
        if (selfBuffs == null) {
            selfBuffs = populateSelfBuffs();
        }
        return selfBuffs;
    }

    private List<String> populateNeededBuffs() {
        return saveService.getMyNeededBuffs(me);
    }

    private List<String> populateSelfBuffs() {
        return saveService.getMySelfBuffs(me);
    }

    private List<String> populateAvailableBuffs() {
        return saveService.getAvailableBuffs(me);
    }

    void checkForExpiredBuffs() {
        final HashMap<String, Integer> durationMap = createDurationMap();
        durationMap.entrySet().stream()
            .filter(x -> x.getValue() < 20)
            .forEach(x -> {
                try {
                    me.getCharacterManager().requestBuff(me, x.getKey());
                } catch (Error e) {
                    if (App.verbose) {
                        System.out.println(e.getMessage());
                    }
                }
            });
    }

    private HashMap<String, Integer> createDurationMap() {
        final HashMap<String, Integer> duration = new HashMap<>();
        final String buffDurationString = getNeededBuffs().stream().map(spellNameToDuration).collect(Collectors.joining());
        final String response = me.queryForInfo(buffDurationString);
        final List<String> results = Arrays.asList(response.split("\\|"));
        results.forEach(buffComposition -> processDurationResponse(buffComposition, duration));
        return duration;
    }

    private void processDurationResponse(String buffComposition, HashMap<String, Integer> duration){
        final String[] temp = buffComposition.split(":");
        if (temp.length > 1) {
            final int dur = temp[1].equals("NULL") ? -1 : Integer.parseInt(temp[1]);
            duration.put(temp[0], dur);
        }
    }
    Function<String, String> spellNameToDuration = x -> String.format("%s:${Me.Buff[%s].Duration}|", x, x);
}