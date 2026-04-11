package com.gabe.animalia.general;

import com.gabe.animalia.enums.ActionCategoryEnum;
import com.gabe.animalia.enums.ActionEnum;

public class Tick extends Action {

    public Tick(Critter subject, String log) {
        super(subject, null, null, null, ActionEnum.NONE);
        this.setBehaviour("Tick");
        this.setLogStr(log);

        // Explicitly safety-check these for the system update
        this.setEffectImage("none");
        this.setEffectDescription("none");
    }

    @Override
    public String getName() {
        return "Status Update";
    }

    @Override
    public ActionCategoryEnum getType() {
        return ActionCategoryEnum.NONE;
    }

    @Override
    public Targetable getTarget() {
        // Ticks are self-targeting, so we return null or the subject
        return null;
    }

    // Inside your Tick.java
    @Override
    public String getBehaviour() {
        return "Tick"; // Force it to return Tick for the manifest
    }
}
