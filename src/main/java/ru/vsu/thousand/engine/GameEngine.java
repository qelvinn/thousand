package ru.vsu.thousand.engine;

import ru.vsu.thousand.bot.DefaultBotStrategy;
import ru.vsu.thousand.bot.PlayerStrategy;
import ru.vsu.thousand.events.GameLog;
import ru.vsu.thousand.model.Card;
import ru.vsu.thousand.model.Deck;
import ru.vsu.thousand.model.GameSettings;
import ru.vsu.thousand.model.Player;
import ru.vsu.thousand.model.TrumpSuit;
import ru.vsu.thousand.rules.GameRules;
import ru.vsu.thousand.state.BiddingState;
import ru.vsu.thousand.state.DealingState;
import ru.vsu.thousand.state.DiscardingState;
import ru.vsu.thousand.state.FinishedState;
import ru.vsu.thousand.state.GamePhase;
import ru.vsu.thousand.state.GameState;
import ru.vsu.thousand.state.InitializationState;
import ru.vsu.thousand.state.PlayingState;
import ru.vsu.thousand.state.ScoringState;
import ru.vsu.thousand.storage.GameSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final Map<Player, PlayerStrategy> strategies;

    private GameState currentState;
    private List<Card> talon;
    private Auction auction;
    private Player declarer;
    private int contract;
    private int declarerPoints;
    private int roundNumber;

    /**
     * Создаёт игровой движок со стратегией по умолчанию для всех игроков.
     *
     * @param players три участника партии
     * @param seed начальное значение генератора случайных чисел
     */
    public GameEngine(List<Player> players, long seed) {
        this(players, seed, Map.of());
    }

    /**
     * Создаёт игровой движок с заданными стратегиями игроков.
     *
     * @param players три участника партии
     * @param seed начальное значение генератора случайных чисел
     * @param customStrategies карта стратегий для игроков
     */
    public GameEngine(
            List<Player> players,
            long seed,
            Map<Player, PlayerStrategy> customStrategies
    ) {
        Objects.requireNonNull(players);
        Objects.requireNonNull(customStrategies);

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
        this.strategies = new LinkedHashMap<>();
        this.talon = List.of();
        this.contract = 0;

        PlayerStrategy defaultStrategy = new DefaultBotStrategy();
        for (Player player : players) {
            roundPoints.put(player, 0);
            PlayerStrategy assigned = customStrategies.getOrDefault(player, defaultStrategy);
            strategies.put(player, Objects.requireNonNull(assigned));
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

    /**
     * Возвращает стратегию принятия решений указанного игрока.
     *
     * @param player участник партии
     * @return стратегия игрока
     */
    public PlayerStrategy getStrategy(Player player) {
        Objects.requireNonNull(player);
        PlayerStrategy strategy = strategies.get(player);
        if (strategy == null) {
            throw new IllegalArgumentException("Игрок не участвует в партии");
        }
        return strategy;
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

    /**
     * Возвращает номер текущего раунда.
     *
     * @return номер раунда
     */
    public int getRoundNumber() {
        return roundNumber;
    }

    /** Увеличивает номер раунда. */
    public void advanceRound() {
        roundNumber++;
    }

    /**
     * Создаёт полный снимок текущего состояния партии.
     *
     * @return снимок состояния партии
     */
    public GameSnapshot createSnapshot() {
        List<GameSnapshot.CardSnapshot> talonSnapshots = talon.stream()
                .map(card -> new GameSnapshot.CardSnapshot(card.getSuit(), card.getRank()))
                .toList();

        List<GameSnapshot.PlayerSnapshot> playerSnapshots = players.stream()
                .map(player -> new GameSnapshot.PlayerSnapshot(
                        player.getName(),
                        player.getScore(),
                        player.getConsecutiveFailedContracts(),
                        player.getBolts(),
                        player.getHand().stream()
                                .map(card -> new GameSnapshot.CardSnapshot(card.getSuit(), card.getRank()))
                                .toList()
                ))
                .toList();

        Map<String, Integer> roundPointsByName = new LinkedHashMap<>();
        for (Player player : players) {
            roundPointsByName.put(player.getName(), roundPoints.getOrDefault(player, 0));
        }

        return new GameSnapshot(
                settings.getSeed(),
                roundNumber,
                getPhase(),
                trumpSuit.getSuit().orElse(null),
                List.copyOf(marriageManager.getDeclaredMarriages()),
                declarer != null ? declarer.getName() : null,
                contract,
                declarerPoints,
                talonSnapshots,
                playerSnapshots,
                roundPointsByName,
                gameLog.getEntries()
        );
    }

    /**
     * Восстанавливает игровой движок из сохранённого снимка состояния.
     *
     * @param snapshot снимок состояния партии
     * @return восстановленный экземпляр игрового движка
     */
    public static GameEngine fromSnapshot(GameSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "Снимок состояния не может быть null");
        if (snapshot.players().size() != GameRules.PLAYER_COUNT) {
            throw new IllegalArgumentException("В снимке должно быть три игрока");
        }

        List<Player> restoredPlayers = new ArrayList<>();
        for (GameSnapshot.PlayerSnapshot playerSnapshot : snapshot.players()) {
            Player player = new Player(playerSnapshot.name());
            List<Card> restoredHand = playerSnapshot.hand().stream()
                    .map(card -> new Card(card.suit(), card.rank()))
                    .toList();
            player.restoreState(
                    playerSnapshot.score(),
                    playerSnapshot.consecutiveFailedContracts(),
                    playerSnapshot.bolts(),
                    restoredHand
            );
            restoredPlayers.add(player);
        }

        GameEngine engine = new GameEngine(restoredPlayers, snapshot.seed());
        engine.roundNumber = snapshot.roundNumber();

        for (int i = 0; i < snapshot.roundNumber(); i++) {
            new Deck(engine.settings.getRandom());
        }

        if (snapshot.trumpSuit() != null) {
            engine.trumpSuit.setSuit(snapshot.trumpSuit());
        }
        engine.marriageManager.restoreDeclared(new HashSet<>(snapshot.declaredMarriages()));

        List<Card> restoredTalon = snapshot.talon().stream()
                .map(card -> new Card(card.suit(), card.rank()))
                .toList();
        engine.setTalon(restoredTalon);

        Map<String, Player> playersByName = restoredPlayers.stream()
                .collect(Collectors.toMap(Player::getName, player -> player));

        if (snapshot.declarerName() != null) {
            Player foundDeclarer = playersByName.get(snapshot.declarerName());
            if (foundDeclarer == null) {
                throw new IllegalArgumentException("Заказчик из снимка не найден среди игроков");
            }
            engine.declarer = foundDeclarer;
        }

        engine.contract = snapshot.contract();
        engine.declarerPoints = snapshot.declarerPoints();

        for (Player player : restoredPlayers) {
            int pts = snapshot.roundPoints().getOrDefault(player.getName(), 0);
            engine.roundPoints.put(player, pts);
        }

        engine.gameLog.restore(snapshot.events());
        engine.currentState = createStateForPhase(snapshot.phase());

        if (snapshot.phase() == GamePhase.BIDDING) {
            int startIdx = snapshot.roundNumber() > 0
                    ? (snapshot.roundNumber() - 1) % restoredPlayers.size()
                    : 0;
            engine.auction = new Auction(restoredPlayers, startIdx);
        }

        return engine;
    }

    private static GameState createStateForPhase(GamePhase phase) {
        return switch (phase) {
            case INITIALIZATION -> new InitializationState();
            case DEALING -> new DealingState();
            case BIDDING -> new BiddingState();
            case DISCARDING -> new DiscardingState();
            case PLAYING -> new PlayingState();
            case SCORING -> new ScoringState();
            case FINISHED -> new FinishedState();
        };
    }
}