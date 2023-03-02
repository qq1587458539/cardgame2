package com.mythostrike.model.game.activity.system;

import com.mythostrike.model.game.activity.PassiveSkill;
import com.mythostrike.model.game.activity.events.handle.DamageHandle;
import com.mythostrike.model.game.activity.events.type.EventTypeDamage;
import com.mythostrike.model.game.management.EventManager;
import com.mythostrike.model.game.player.Player;

public class CheckDying extends PassiveSkill {
    public static final String NAME = CheckDying.class.getSimpleName();
    public static final String DESCRIPTION = "if player has low hp";
    private DamageHandle damageHandle;

    public CheckDying() {
        super(NAME, DESCRIPTION);
    }

    @Override
    public boolean checkCondition(DamageHandle damageHandle) {
        this.damageHandle = damageHandle;
        return damageHandle.getTo().getCurrentHp() < 1;
    }

    @Override
    public void activate() {
        EnterDying enterDying = new EnterDying(damageHandle.getTo(), damageHandle.getGameManager());
        enterDying.activate();
    }

    /**
     * put it into DamageComplete, so that every player after get damage check if he is about dying
     *
     * @param eventManager the eventManager registering the check dying
     * @param player       doesn't matter, since every player has to check if he is dying
     */
    @Override
    public void register(EventManager eventManager, Player player) {
        eventManager.registerEvent(EventTypeDamage.DAMAGE_COMPLETE, this, player, true);
    }
}
