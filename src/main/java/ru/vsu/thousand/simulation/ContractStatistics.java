package ru.vsu.thousand.simulation;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Содержит агрегированную статистику по партиям и заказам в симуляции.
 *
 * @param totalGames количество сыгранных партий
 * @param totalContracts общее количество разыгранных заказов
 * @param successfulContracts количество выполненных заказов
 * @param failedContracts количество невыполненных заказов
 * @param averageContractSize средний размер заказа
 * @param winsByPlayer количество побед в партиях по именам игроков
 * @param contractsByPlayer количество сделанных заказов по именам игроков
 * @param successfulContractsByPlayer количество выполненных заказов по именам игроков
 * @param contractSizeDistribution распределение количества заказов по их номиналам
 */
public record ContractStatistics(
        int totalGames,
        long totalContracts,
        long successfulContracts,
        long failedContracts,
        double averageContractSize,
        Map<String, Long> winsByPlayer,
        Map<String, Long> contractsByPlayer,
        Map<String, Long> successfulContractsByPlayer,
        Map<Integer, Long> contractSizeDistribution
) {

    /**
     * Представляет запись об одном разыгранном заказе в раунде.
     *
     * @param playerName имя заказчика
     * @param contractPoints размер заказа
     * @param successful признак выполнения заказа
     */
    public record ContractRecord(
            String playerName,
            int contractPoints,
            boolean successful
    ) {

        /**
         * Проверяет корректность записи о заказе.
         */
        public ContractRecord {
            if (playerName == null || playerName.isBlank()) {
                throw new IllegalArgumentException("Имя игрока не может быть пустым");
            }
            if (contractPoints < 100 || contractPoints % 5 != 0) {
                throw new IllegalArgumentException("Некорректный размер заказа");
            }
        }
    }

    /**
     * Представляет итог одной завершённой партии в симуляции.
     *
     * @param gameIndex порядковый номер партии
     * @param seed использованный seed партии
     * @param winnerName имя победителя партии
     * @param contracts список всех разыгранных в партии заказов
     */
    public record GameOutcome(
            int gameIndex,
            long seed,
            String winnerName,
            List<ContractRecord> contracts
    ) {

        /**
         * Проверяет корректность данных завершённой партии.
         */
        public GameOutcome {
            if (winnerName == null || winnerName.isBlank()) {
                throw new IllegalArgumentException("Имя победителя не может быть пустым");
            }
            contracts = List.copyOf(Objects.requireNonNull(contracts, "Список заказов не может быть null"));
        }
    }

    /**
     * Вычисляет сводную статистику по списку результатов партий с использованием Stream API.
     *
     * @param outcomes список результатов партий
     * @return агрегированная статистика по заказам и победам
     */
    public static ContractStatistics fromOutcomes(List<GameOutcome> outcomes) {
        Objects.requireNonNull(outcomes, "Список результатов партий не может быть null");

        List<ContractRecord> allContracts = outcomes.stream()
                .flatMap(outcome -> outcome.contracts().stream())
                .toList();

        long totalContracts = allContracts.size();

        long successfulContracts = allContracts.stream()
                .filter(ContractRecord::successful)
                .count();

        long failedContracts = totalContracts - successfulContracts;

        double averageContractSize = allContracts.stream()
                .mapToInt(ContractRecord::contractPoints)
                .average()
                .orElse(0.0);

        Map<String, Long> winsByPlayer = outcomes.stream()
                .collect(Collectors.groupingBy(
                        GameOutcome::winnerName,
                        Collectors.counting()
                ));

        Map<String, Long> contractsByPlayer = allContracts.stream()
                .collect(Collectors.groupingBy(
                        ContractRecord::playerName,
                        Collectors.counting()
                ));

        Map<String, Long> successfulContractsByPlayer = allContracts.stream()
                .filter(ContractRecord::successful)
                .collect(Collectors.groupingBy(
                        ContractRecord::playerName,
                        Collectors.counting()
                ));

        Map<Integer, Long> contractSizeDistribution = allContracts.stream()
                .collect(Collectors.groupingBy(
                        ContractRecord::contractPoints,
                        Collectors.counting()
                ));

        return new ContractStatistics(
                outcomes.size(),
                totalContracts,
                successfulContracts,
                failedContracts,
                averageContractSize,
                Map.copyOf(winsByPlayer),
                Map.copyOf(contractsByPlayer),
                Map.copyOf(successfulContractsByPlayer),
                Map.copyOf(contractSizeDistribution)
        );
    }

    /**
     * Возвращает процент выполненных заказов от общего числа заказов.
     *
     * @return доля выполненных заказов в процентах (от 0.0 до 100.0)
     */
    public double getSuccessRatePercent() {
        if (totalContracts == 0) {
            return 0.0;
        }
        return (successfulContracts * 100.0) / totalContracts;
    }
}
