package com.mythostrike.model.game.activity.system;

import com.mythostrike.controller.message.game.HighlightMessage;
import com.mythostrike.controller.message.game.PlayerCondition;
import com.mythostrike.model.game.activity.ActiveSkill;
import com.mythostrike.model.game.activity.Activity;
import com.mythostrike.model.game.activity.cards.Card;
import com.mythostrike.model.game.activity.events.handle.CardAskHandle;
import com.mythostrike.model.game.activity.events.handle.CardUseHandle;
import com.mythostrike.model.game.activity.events.handle.PlayerHandle;
import com.mythostrike.model.game.activity.events.type.EventTypeCardAsk;
import com.mythostrike.model.game.activity.events.type.EventTypeRequest;
import com.mythostrike.model.game.management.GameManager;
import com.mythostrike.model.game.player.Player;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PickCardToPLay extends Activity {
    public static final String NAME = PickCardToPLay.class.getSimpleName();
    public static final String DESCRIPTION = "pick card to play or skill to invoke," +
        " or end turn if no card/skill can/want be played";
    public static final int CARD_COUNT_TURN_START = 2;

    private final GameManager gameManager;
    private List<Card> selectedCards;
    private List<Player> selectedPlayers;

    public PickCardToPLay(GameManager gameManager) {
        super(NAME, DESCRIPTION);
        this.gameManager = gameManager;
    }

    @Override
    public void use() {
        Player player = gameManager.getGame().getCurrentPlayer();
        CardAskHandle cardAskHandle = new CardAskHandle(gameManager, "ask for play card", player,
            List.of(), player.getHandCards(), gameManager.getGame().getTablePile(), 1,
            false, true);
        gameManager.getEventManager().triggerEvent(EventTypeCardAsk.CARD_ASKED, cardAskHandle);

        List<Card> playableCards = getPlayableCards(player);


        List<Integer> cardIds = GameManager.convertCardsToInteger(playableCards);
        /*HighlightMessage highlightMessage = new HighlightMessage(cardIds, null,
            null, 1, 1, 0, 0, "Pick a Card to play", true, false);*/
        List<PlayerCondition> playerConditions = new ArrayList<>();
        for (Card card : playableCards) {
            playerConditions.add(card.getPlayerCondition());
        }
        List<Integer> skillIds = new ArrayList<>();
        List<PlayerCondition> skillPlayerConditions = new ArrayList<>();

        for (ActiveSkill skill : player.getActiveSkills()) {
            if (skill.checkCondition(new PlayerHandle(gameManager, "check if skill is invoke able", player))) {
                skillIds.add(skill.getId());
                skillPlayerConditions.add(skill.getPlayerCondition());
            }
        }

        HighlightMessage highlightMessage = HighlightMessage.builder()
            .cardIds(cardIds)
            .cardPlayerConditions(playerConditions)
            .cardCount(List.of(1))
            .skillIds(skillIds)
            .skillCount(List.of(1))
            .skillPlayerConditions(skillPlayerConditions)
            .reason(DESCRIPTION)
            .activateEndTurn(true)
            .build();

        PickRequest pickRequest = new PickRequest(player, gameManager, highlightMessage);
        gameManager.getEventManager().triggerEvent(EventTypeRequest.ACTIVE_TURN_REQUEST, pickRequest);
        PlayCard playCard = new PlayCard(gameManager, pickRequest);
        gameManager.queueActivity(playCard);
        gameManager.queueActivity(pickRequest);


    }

    private List<Card> getPlayableCards(Player player) {

        List<Card> playableCards = new ArrayList<>();
        for (Card card : player.getHandCards().getCards()) {
            CardUseHandle cardUseHandle = new CardUseHandle(
                gameManager, card, "check if card is playable", player, player,
                true);
            if (card.checkCondition(cardUseHandle)) {
                playableCards.add(card);
            }
        }
        return playableCards;
    }

}
