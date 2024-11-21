package com.my.copybot.util;

import org.ta4j.core.TimeSeries;

public class ChochTrendDetector {

    /**
     * Метод для определения направления CHOCH и экстремумов, пробой которых означает слом тренда.
     * @param series - свечной график
     * @return массив с направлением CHOCH и экстремумами:
     *         [0] - направление CHOCH ("Up", "Down", "No Change"),
     *         [1] - уровень максимума для пробоя (последний ключевой максимум),
     *         [2] - уровень минимума для пробоя (последний ключевой минимум).
     */
    public static Double[] detectChochWithExtremes(TimeSeries series) {
        int currentIndex = series.getEndIndex();

        // Проверяем, достаточно ли данных для анализа
        if (currentIndex < 3) {
            return new Double[]{0.00, null, null};
        }

        Double lastHigh = null; // Последний ключевой максимум
        Double lastLow = null;  // Последний ключевой минимум
        Double chochDirection = 0.00; // Направление CHOCH

        // Ищем ключевые экстремумы
        for (int i = currentIndex; i >= 2; i--) {
            double prevHigh = series.getBar(i - 1).getMaxPrice().doubleValue();
            double prevLow = series.getBar(i - 1).getMinPrice().doubleValue();

            double currentHigh = series.getBar(i).getMaxPrice().doubleValue();
            double currentLow = series.getBar(i).getMinPrice().doubleValue();

            // Условие CHOCH вверх
            if (currentHigh > prevHigh && (lastLow == null || currentLow < lastLow)) {
                chochDirection = 1.00;
                lastHigh = currentHigh;
                lastLow = prevLow;
                break;
            }

            // Условие CHOCH вниз
            if (currentLow < prevLow && (lastHigh == null || currentHigh > lastHigh)) {
                chochDirection = -1.00;
                lastHigh = prevHigh;
                lastLow = currentLow;
                break;
            }
        }

        return new Double[]{chochDirection, lastHigh, lastLow};
    }
}