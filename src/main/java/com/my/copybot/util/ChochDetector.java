package com.my.copybot.util;

import org.ta4j.core.TimeSeries;

public class ChochDetector {

    // Метод для определения направления CHoCH
    public static int detectChochDirection(TimeSeries series) {
        int currentIndex = series.getEndIndex();

        // Проверка, достаточно ли данных для анализа
        if (currentIndex < 5) {
            return 0;
        }

        // Находим последние два локальных максимума и минимума
        Double lastHigh = findLastHigh(series);
        Double secondLastHigh = findSecondLastHigh(series);

        Double lastLow = findLastLow(series);
        Double secondLastLow = findSecondLastLow(series);

        // Текущие значения high и low
        double currentHigh = series.getBar(currentIndex).getMaxPrice().doubleValue();
        double currentLow = series.getBar(currentIndex).getMinPrice().doubleValue();

        // Проверяем направление CHoCH
        if (currentHigh > lastHigh && lastHigh > secondLastHigh) {
     //       System.out.print(" CHoCH Up | ");
            return 1;
        }
        if (currentLow < lastLow && lastLow < secondLastLow) {
       //     System.out.print(" CHoCH Down | ");
            return -1;
        }

        return 0;
    }

    // Метод для поиска последнего локального максимума
    private static Double findLastHigh(TimeSeries series) {
        int currentIndex = series.getEndIndex();
        for (int i = currentIndex - 1; i >= 1; i--) {
            double prevHigh = series.getBar(i - 1).getMaxPrice().doubleValue();
            double currentHigh = series.getBar(i).getMaxPrice().doubleValue();
            double nextHigh = series.getBar(i + 1).getMaxPrice().doubleValue();
            if (currentHigh > prevHigh && currentHigh > nextHigh) {
                return currentHigh;
            }
        }
        return 0.00;
    }

    // Метод для поиска предпоследнего локального максимума
    private static Double findSecondLastHigh(TimeSeries series) {
        int currentIndex = series.getEndIndex();
        int count = 0;
        for (int i = currentIndex - 1; i >= 1; i--) {
            double prevHigh = series.getBar(i - 1).getMaxPrice().doubleValue();
            double currentHigh = series.getBar(i).getMaxPrice().doubleValue();
            double nextHigh = series.getBar(i + 1).getMaxPrice().doubleValue();
            if (currentHigh > prevHigh && currentHigh > nextHigh) {
                count++;
                if (count == 2) {
                    return currentHigh;
                }
            }
        }
        return 0.00;
    }

    // Метод для поиска последнего локального минимума
    private static Double findLastLow(TimeSeries series) {
        int currentIndex = series.getEndIndex();
        for (int i = currentIndex - 1; i >= 1; i--) {
            double prevLow = series.getBar(i - 1).getMinPrice().doubleValue();
            double currentLow = series.getBar(i).getMinPrice().doubleValue();
            double nextLow = series.getBar(i + 1).getMinPrice().doubleValue();
            if (currentLow < prevLow && currentLow < nextLow) {
                return currentLow;
            }
        }
        return 10000000.00;
    }

    // Метод для поиска предпоследнего локального минимума
    private static Double findSecondLastLow(TimeSeries series) {
        int currentIndex = series.getEndIndex();
        int count = 0;
        for (int i = currentIndex - 1; i >= 1; i--) {
            double prevLow = series.getBar(i - 1).getMinPrice().doubleValue();
            double currentLow = series.getBar(i).getMinPrice().doubleValue();
            double nextLow = series.getBar(i + 1).getMinPrice().doubleValue();
            if (currentLow < prevLow && currentLow < nextLow) {
                count++;
                if (count == 2) {
                    return currentLow;
                }
            }
        }
        return 10000000.00;
    }
}