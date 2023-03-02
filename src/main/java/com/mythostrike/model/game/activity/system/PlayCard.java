package com.mythostrike.model.game.activity.system;

import com.mythostrike.model.game.activity.Activity;
import com.mythostrike.model.game.activity.cards.Card;
import com.mythostrike.model.game.activity.events.type.EventTypeCardUse;
import com.mythostrike.model.game.management.GameManager;
import lombok.Getter;

@Getter
public class PlayCard extends Activity {
    public static final String NAME = PlayCard.class.getSimpleName();
    public static final String DESCRIPTION = "Play a card";
    public static final int CARD_COUNT_TURN_START = 2;

    private final GameManager gameManager;
    private final PickRequest pickRequest;

    public PlayCard(GameManager gameManager, PickRequest pickRequest) {
        super(NAME, DESCRIPTION);
        this.gameManager = gameManager;
        this.pickRequest = pickRequest;
    }

    @Override
    public void use() {
        gameManager.getCurrentActivity().addFirst(new PickCardToPLay(gameManager));
        if (pickRequest.isClickedCancel()) {
            return;
        }
        if (pickRequest.getSelectedActiveSkill() != null) {
            pickRequest.getSelectedActiveSkill().activate();
            return;
        }

        if (pickRequest.getSelectedCards() == null || pickRequest.getSelectedCards().isEmpty()) {
            //TODO: jack is fixing this
            //throw new IllegalInputException("No card selected");
        } else {
            for (Card card : pickRequest.getSelectedCards()) {

                card.setPickRequest(pickRequest);
                gameManager.getEventManager().triggerEvent(EventTypeCardUse.BEFORE_CARD_USE, card.getCardUseHandle());
                card.activate();
                gameManager.getEventManager().triggerEvent(EventTypeCardUse.CARD_USED, card.getCardUseHandle());

            }
        }
    }
}
