package com.my.copybot.util;


import org.ta4j.core.TimeSeries;

public class ChochCalculator {

    // Метод для поиска CHOCH на основе переданной серии данных (BarSeries)
    public Integer detectChoch(TimeSeries series, int lookbackPeriod) {
        int currentIndex = series.getEndIndex();

        // Если недостаточно данных для анализа
        if (currentIndex < lookbackPeriod) {
            return null;
        }

//        // Находим максимумы и минимумы за предыдущий период
//        double prevMax = findMax(series, currentIndex - lookbackPeriod, currentIndex - 1);
//        double prevMin = findMin(series, currentIndex - lookbackPeriod, currentIndex - 1);
//
//        double currentHigh = series.getBar(currentIndex).getMaxPrice().doubleValue();
//        double currentLow = series.getBar(currentIndex).getMinPrice().doubleValue();

        // Проходим через серию и ищем последний разворот (CHOCH)
        for (int i = currentIndex; i >= lookbackPeriod; i--) {
            double prevHigh = findMax(series, i - lookbackPeriod, i - 1);
            double prevLow = findMin(series, i - lookbackPeriod, i - 1);

            double currentHigh = series.getBar(i).getMaxPrice().doubleValue();
            double currentLow = series.getBar(i).getMinPrice().doubleValue();



            // Проверка на CHOCH (смена тренда)
        if (currentHigh > prevHigh) {
            System.out.print(" CHoCH Up | ");
            return 1;
        }
        if (currentLow < prevLow) {
            System.out.print(" CHoCH Down | ");
            return -1;
        }}
            System.out.print(" CHoCH Continue | ");
            return 0;
    }

    // Метод для поиска максимума за определенный период
    private double findMax(TimeSeries series, int startIndex, int endIndex) {
        double max = series.getBar(startIndex).getMaxPrice().doubleValue();
        for (int i = startIndex + 1; i <= endIndex; i++) {
            double high = series.getBar(i).getMaxPrice().doubleValue();
            if (high > max) {
                max = high;
            }
        }
        return max;
    }

    // Метод для поиска минимума за определенный период
    private double findMin(TimeSeries series, int startIndex, int endIndex) {
        double min = series.getBar(startIndex).getMinPrice().doubleValue();
        for (int i = startIndex + 1; i <= endIndex; i++) {
            double low = series.getBar(i).getMinPrice().doubleValue();
            if (low < min) {
                min = low;
            }
        }
        return min;
    }
}