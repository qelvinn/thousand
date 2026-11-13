package ru.vsu.thousand.engine;

import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.model.Rank;
import ru.vsu.thousand.model.Suit;
import ru.vsu.thousand.model.TrumpSuit;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Управляет объявлением марьяжей и сменой козырной масти.
 */
public class MarriageManager {
    private final TrumpSuit trumpSuit;
    private final Set<Suit> declaredMarriages;

    /**
     * Создаёт менеджер марьяжей.
     *
     * @param trumpSuit объект текущего козыря
     */
    public MarriageManager(TrumpSuit trumpSuit) {
        this.trumpSuit = Objects.requireNonNull(trumpSuit);
        this.declaredMarriages = new HashSet<>();
    }

    /**
     * Объявляет марьяж указанной масти.
     *
     * @param player игрок, объявляющий марьяж
     * @param suit масть короля и дамы
     * @return количество начисляемых очков
     */
    public int declare(Player player, Suit suit) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(suit);

        if (declaredMarriages.contains(suit)) {
            throw new IllegalStateException("Марьяж этой масти уже объявлен");
        }

        boolean hasKing = player.getHand().contains(new Card(suit, Rank.KING));
        boolean hasQueen = player.getHand().contains(new Card(suit, Rank.QUEEN));

        if (!hasKing || !hasQueen) {
            throw new IllegalArgumentException(
                    "Для объявления марьяжа нужны король и дама одной масти"
            );
        }

        declaredMarriages.add(suit);
        trumpSuit.setSuit(suit);

        return suit.getMarriagePoints();
    }

    /**
     * Проверяет, объявлялся ли марьяж указанной масти.
     *
     * @param suit проверяемая масть
     * @return true, если марьяж уже объявлен
     */
    public boolean isDeclared(Suit suit) {
        return declaredMarriages.contains(Objects.requireNonNull(suit));
    }
}