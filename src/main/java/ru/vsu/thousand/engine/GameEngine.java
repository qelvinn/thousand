package ru.vsu.thousand.engine;

import ru.vsu.thousand.events.GameLog;
import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.GameSettings;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.model.TrumpSuit;
import ru.vsu.thousand.rules.GameRules;
import ru.vsu.thousand.state.GamePhase;
import ru.vsu.thousand.state.GameState;
import ru.vsu.thousand.state.InitializationState;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Управляет ходом партии в игре «Тысяча».
 */
public class GameEngine {
    private final List<Player> players;
    private final GameSettings settings;
    private final Dealer dealer;
    private final TalonExchange talonExchange;
    private final TrumpSuit trumpSuit;
    private final MarriageManager marriageManager;
    private final RoundScorer roundScorer;
    private final GameLog gameLog;
    private final Map<Player, Integer> roundPoints;

    private GameState currentState;
    private List<Card> talon;
    private Auction auction;
    private Player declarer;
    private int contract;
    private int declarerPoints;
    private int roundNumber;

    /**
     * Создаёт игровой движок.
     *
     * @param players три участника партии
     * @param seed начальное значение генератора случайных чисел
     */
    public GameEngine(List<Player> players, long seed) {
        Objects.requireNonNull(players);

        if (players.size() != GameRules.PLAYER_COUNT
                || players.stream().anyMatch(Objects::isNull)
                || players.stream().distinct().count() != GameRules.PLAYER_COUNT) {
            throw new IllegalArgumentException(
                    "Для игры нужны три разных игрока"
            );
        }

        this.players = new ArrayList<>(players);
        this.settings = new GameSettings(seed);
        this.dealer = new Dealer(settings);
        this.talonExchange = new TalonExchange();
        this.trumpSuit = new TrumpSuit();
        this.marriageManager = new MarriageManager(trumpSuit);
        this.roundScorer = new RoundScorer();
        this.gameLog = new GameLog();
        this.roundPoints = new LinkedHashMap<>();
        this.talon = List.of();
        this.contract = 0;

        for (Player player : players) {
            roundPoints.put(player, 0);
        }
    }

    /** Запускает новую партию. */
    public void start() {
        if (currentState != null) {
            throw new IllegalStateException("Партия уже запущена");
        }
        currentState = new InitializationState();
    }

    /** Выполняет один шаг текущего состояния. */
    public void runStep() {
        if (currentState == null) {
            throw new IllegalStateException("Сначала необходимо запустить партию");
        }
        if (getPhase() == GamePhase.FINISHED) {
            throw new IllegalStateException("Партия уже завершена");
        }
        currentState.handle(this);
    }

    /**
     * Выполняет партию до завершения или достижения лимита шагов.
     *
     * @param maxSteps максимальное количество шагов
     */
    public void runUntilFinished(int maxSteps) {
        if (maxSteps < 1) {
            throw new IllegalArgumentException("Лимит шагов должен быть положительным");
        }
        if (currentState == null) {
            start();
        }

        int steps = 0;
        while (getPhase() != GamePhase.FINISHED && steps < maxSteps) {
            runStep();
            steps++;
        }

        if (getPhase() != GamePhase.FINISHED) {
            throw new IllegalStateException(
                    "Партия не завершилась за отведённое количество шагов"
            );
        }
    }

    /** @return участники партии */
    public List<Player> getPlayers() {
        return List.copyOf(players);
    }

    /** @return настройки партии */
    public GameSettings getSettings() {
        return settings;
    }

    /** @return раздатчик карт */
    public Dealer getDealer() {
        return dealer;
    }

    /** @return механизм обмена прикупа */
    public TalonExchange getTalonExchange() {
        return talonExchange;
    }

    /** @return текущий козырь */
    public TrumpSuit getTrumpSuit() {
        return trumpSuit;
    }

    /** @return менеджер марьяжей */
    public MarriageManager getMarriageManager() {
        return marriageManager;
    }

    /** @return подсчёт очков */
    public RoundScorer getRoundScorer() {
        return roundScorer;
    }

    /** @return карты прикупа */
    public List<Card> getTalon() {
        return List.copyOf(talon);
    }

    /**
     * Сохраняет карты прикупа.
     *
     * @param talon карты прикупа
     */
    public void setTalon(List<Card> talon) {
        this.talon = List.copyOf(Objects.requireNonNull(talon));
    }

    /** @return текущая торговля */
    public Auction getAuction() {
        return auction;
    }

    /**
     * Устанавливает торговлю.
     *
     * @param auction объект торговли
     */
    public void setAuction(Auction auction) {
        this.auction = Objects.requireNonNull(auction);
    }

    /** @return заказчик раунда */
    public Player getDeclarer() {
        return declarer;
    }

    /**
     * Устанавливает заказчика раунда.
     *
     * @param declarer заказчик
     */
    public void setDeclarer(Player declarer) {
        this.declarer = Objects.requireNonNull(declarer);
    }

    /** @return размер заказа */
    public int getContract() {
        return contract;
    }

    /**
     * Устанавливает размер заказа.
     *
     * @param contract заказанное количество очков
     */
    public void setContract(int contract) {
        if (contract < 100 || contract % 5 != 0) {
            throw new IllegalArgumentException("Некорректный размер заказа");
        }
        this.contract = contract;
    }

    /**
     * Сохраняет очки, набранные заказчиком.
     *
     * @param points количество очков
     */
    public void setDeclarerPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Очки не могут быть отрицательными");
        }
        declarerPoints = points;
        roundPoints.put(declarer, points);
    }

    /** @return очки заказчика за раунд */
    public int getDeclarerPoints() {
        return declarerPoints;
    }

    /**
     * Добавляет очки игроку за взятки и марьяжи.
     *
     * @param player игрок
     * @param points добавляемые очки
     */
    public void addRoundPoints(Player player, int points) {
        Objects.requireNonNull(player);
        if (!roundPoints.containsKey(player) || points < 0) {
            throw new IllegalArgumentException("Некорректные очки игрока");
        }
        roundPoints.merge(player, points, Integer::sum);
    }

    /** @return очки игроков за текущий раунд */
    public Map<Player, Integer> getRoundPoints() {
        return Map.copyOf(roundPoints);
    }

    /** Очищает очки текущего раунда. */
    public void resetRoundPoints() {
        for (Player player : players) {
            roundPoints.put(player, 0);
        }
        declarerPoints = 0;
    }

    /** @return текущее состояние партии */
    public GameState getCurrentState() {
        return currentState;
    }

    /**
     * Устанавливает новое состояние партии.
     *
     * @param state новое состояние
     */
    public void setState(GameState state) {
        currentState = Objects.requireNonNull(state);
    }

    /** @return текущая фаза партии */
    public GamePhase getPhase() {
        return currentState == null
                ? GamePhase.INITIALIZATION
                : currentState.getPhase();
    }

    /**
     * Добавляет событие в журнал партии.
     *
     * @param event описание события
     */
    public void addEvent(String event) {
        gameLog.add(event);
    }

    /**
     * Возвращает журнал событий партии.
     *
     * @return список событий
     */
    public List<String> getEvents() {
        return gameLog.getEntries();
    }

    /** @return победитель партии или null, если партия не завершена */
    public Player getWinner() {
        return players.stream()
                .filter(player -> player.getScore() >= GameRules.WINNING_SCORE)
                .findFirst()
                .orElse(null);
    }

    /**
     * Сбрасывает данные предыдущего раунда.
     */
    public void resetRoundData() {
        declarer = null;
        contract = 0;
        declarerPoints = 0;
        resetRoundPoints();
    }

    /**
     * Возвращает индекс первого игрока для текущего раунда.
     *
     * @return индекс первого игрока
     */
    public int getStartingPlayerIndex() {
        return roundNumber % players.size();
    }

    /** Увеличивает номер раунда. */
    public void advanceRound() {
        roundNumber++;
    }
}
