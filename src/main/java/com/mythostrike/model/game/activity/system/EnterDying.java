package com.mythostrike.model.game.activity.system;

import com.mythostrike.model.game.activity.Activity;
import com.mythostrike.model.game.activity.cards.CardFilter;
import com.mythostrike.model.game.management.GameManager;
import com.mythostrike.model.game.player.Player;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * this class will do the job, that if player is about to die:
 * 1.ask all the players (including the dying player),
 * 2.create a list with player, and ask player1 if he want to use 1 heal for the player dying
 * 3.check if player plays, if true then heal and check if dying player has hp > 0, else back to step 2
 * 4.if player1 don't want to play heal, go to the next player to the list
 * 5.this end if list is empty (no more player to ask), player die or dying player has hp > 0, survive
 */
public class EnterDying extends Activity {
    public static final String NAME = EnterDying.class.getSimpleName();
    public static final String DESCRIPTION = "if player is about to die";
    public static final CardFilter FILTER = new CardFilter("Heal");

    private final Player player;
    private final GameManager gameManager;
    private final List<Player> players;
    @Setter
    private PickRequest pickRequest;
    @Getter
    private boolean end;

    public EnterDying(Player player, GameManager gameManager) {
        super(NAME, DESCRIPTION);
        this.player = player;
        this.gameManager = gameManager;
        players = new ArrayList<>(gameManager.getGame().getAlivePlayers());
        end = false;
    }

    @Override
    public void activate() {
        //TODO:update message to frontend, that this player is about to die
        gameManager.queueActivity(this);
    }

    @Override
    public void use() {
        gameManager.getPlayerManager().killPlayer(player);
        end = true;

        /*if (pickRequest != null) {
            Player healer = pickRequest.getPlayer();
            if (pickRequest.getSelectedCards() != null && !pickRequest.getSelectedCards().isEmpty()) {
                DamageHandle damageHandle = new DamageHandle(gameManager, null,
                    "use heal to heal hp", healer, player,
                    -1, DamageType.HEAL);
                gameManager.getPlayerManager().applyDamage(damageHandle);
            } else {
                players.remove(healer);
            }
        }
        int count = 1 - player.getCurrentHp();
        // player is already alive again
        if (count < 1) {
            end = true;
            return;
        }
        // no one else can heal him now
        if (players.isEmpty()) {
            gameManager.getPlayerManager().killPlayer(player);
            end = true;
        } else {
            Player healer = players.get(0);
            List<Card> cards = FILTER.filter(healer.getHandCards().getCards());
            List<Integer> cardIds = GameManager.convertCardsToInteger(cards);
            //TODO implement
            String hint
                = "Player " + player.getUsername() + "is about to die, he needs " + count + "heal(s) to get alive"
                + "if you want to heal him, pick heal and click confirm";

            HighlightMessage highlightMessage = HighlightMessage.builder()
                .cardIds(cardIds)
                .cardCount(List.of(1))
                .reason(hint)
                .build();
            pickRequest = new PickRequest(player, gameManager, highlightMessage);
            gameManager.queueActivity(pickRequest);
        }*/
    }

    @Override
    public boolean end() {
        return end;
    }
}
