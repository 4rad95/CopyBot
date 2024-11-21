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
        int currentIndex = series.getEndIndex();

        // Проверка достаточности данных
        if (currentIndex < 3) {
            return new Double[]{0.00, null, null};
        }

        Double lastHigh = null; // Ключевой максимум
        Double lastLow = null;  // Ключевой минимум
        double trendDirection = 0.00; // Направление тренда

        // Перебор баров с конца
        for (int i = currentIndex; i >= 2; i--) {
            double prevHigh = series.getBar(i - 1).getMaxPrice().doubleValue();
            double prevLow = series.getBar(i - 1).getMinPrice().doubleValue();

            double currentHigh = series.getBar(i).getMaxPrice().doubleValue();
            double currentLow = series.getBar(i).getMinPrice().doubleValue();

            // Проверка слома тренда (CHOCH)
            if (lastHigh != null && currentLow < lastLow) {
                trendDirection = -1.00;
                break;
            }
            if (lastLow != null && currentHigh > lastHigh) {
                trendDirection = 1.00;
                break;
            }

            // Проверка подтверждения тренда
            if (currentHigh > prevHigh) {
                trendDirection = 1.00;
                lastHigh = currentHigh;
                lastLow = prevLow; // Минимум в восходящем тренде
            } else if (currentLow < prevLow) {
                trendDirection = -1.00;
                lastLow = currentLow;
                lastHigh = prevHigh; // Максимум в нисходящем тренде
            }
        }

        return new Double[]{trendDirection, lastHigh, lastLow};
    }
}