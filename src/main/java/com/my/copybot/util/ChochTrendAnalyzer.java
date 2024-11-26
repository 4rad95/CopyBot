package com.my.copybot.util;

import org.ta4j.core.TimeSeries;

public class ChochTrendAnalyzer {

    /**
     * Метод для определения направления CHOCH и ключевых уровней.
     * @param series - свечной график
     * @return массив:
     *         [0] - направление ("Up" - восходящий, "Down" - нисходящий, "CHOCH Up", "CHOCH Down"),
     *         [1] - уровень ключевого максимума,
     *         [2] - уровень ключевого минимума.
     */
    public static Double[] detectChochAndTrend(TimeSeries series) {
        int endIndex = series.getEndIndex();

        // Если данных недостаточно, возвращаем ошибку
        if (endIndex < 3) {
            return new Double[]{0.0, null};  // Недостаточно данных
        }

        // Переменные для хранения ключевых уровней
        Double lastHigh = null;
        Double lastLow = null;
        Double prevHigh = null;
        Double prevLow = null;

        // Перебор данных начиная с конца
        for (int i = endIndex; i >= 2; i--) {
            double currentHigh = series.getBar(i).getMaxPrice().doubleValue();
            double currentLow = series.getBar(i).getMinPrice().doubleValue();
            double previousHigh = series.getBar(i - 1).getMaxPrice().doubleValue();
            double previousLow = series.getBar(i - 1).getMinPrice().doubleValue();

            // CHOCH вниз: если текущий минимум пробивает предыдущий минимум
            if (lastLow != null && currentLow < lastLow) {
                return new Double[]{-1.0, lastLow, currentLow};
            }

            // CHOCH вверх: если текущий максимум пробивает предыдущий максимум
            if (lastHigh != null && currentHigh > lastHigh) {
                return new Double[]{1.0, lastHigh, currentHigh};
            }

            // Подтверждение восходящего тренда (по максимумам)
            if (currentHigh > previousHigh) {
                lastHigh = currentHigh;
                lastLow = previousLow;  // Минимум подтверждает восходящий тренд
            }

            // Подтверждение нисходящего тренда (по минимумам)
            if (currentLow < previousLow) {
                lastLow = currentLow;
                lastHigh = previousHigh;  // Максимум подтверждает нисходящий тренд
            }
        }

        // Если изменений тренда не произошло, возвращаем "нет изменений"
        return new Double[]{0.0, null};
    }
}