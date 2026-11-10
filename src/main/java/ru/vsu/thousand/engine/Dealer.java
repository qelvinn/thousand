package ru.vsu.thousand.engine;

import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.Deck;
import ru.vsu.thousand.model.GameSettings;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.rules.GameRules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Отвечает за перемешивание колоды, раздачу карт и формирование прикупа.
 */
public class Dealer {
    private final GameSettings settings;

    /**
     * Создаёт раздатчика с настройками партии.
     *
     * @param settings настройки партии и генератор случайных чисел
     */
    public Dealer(GameSettings settings) {
        this.settings = Objects.requireNonNull(settings);
    }

    /**
     * Раздаёт карты игрокам и формирует прикуп.
     *
     * @param players список трёх игроков
     * @return список из трёх карт прикупа
     * @throws IllegalArgumentException если количество игроков не равно трём
     * @throws IllegalStateException если у игроков уже есть карты
     */
    public List<Card> deal(List<Player> players) {
        Objects.requireNonNull(players);

        if (players.size() != GameRules.PLAYER_COUNT) {
            throw new IllegalArgumentException("Для игры нужны ровно три игрока");
        }

        for (Player player : players) {
            Objects.requireNonNull(player);

            if (!player.getHand().isEmpty()) {
                throw new IllegalStateException("Перед раздачей руки игроков должны быть пустыми");
            }
        }

        Deck deck = new Deck(settings.getRandom());

        for (int cardNumber = 0; cardNumber < GameRules.HAND_SIZE; cardNumber++) {
            for (Player player : players) {
                player.addCard(deck.draw());
            }
        }

        List<Card> talon = new ArrayList<>();

        for (int i = 0; i < GameRules.TALON_SIZE; i++) {
            talon.add(deck.draw());
        }

        return List.copyOf(talon);
    }
}