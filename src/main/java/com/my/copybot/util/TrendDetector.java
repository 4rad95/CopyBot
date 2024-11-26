package com.my.copybot.util;

import org.ta4j.core.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TrendDetector {

    public static class Extremum {
        public String type;

        public int index;
        public double value;
        public ZonedDateTime time;

        public Extremum(String type, int index, double value, ZonedDateTime time) {
            this.type = type;

            this.index = index;
            this.value = value;
            this.time = time;
        }

        @Override
        public String toString() {
            return "Extremum{" +
                    "type='" + type + '\'' +
                    ", index=" + index +
                    ", value=" + value +
                    ", time=" + time +
                    '}';
        }
    }

    public static class TrendResult {
        public String trend;
        public int typeD;
        public String name;
        public List<Extremum> lowExtremes;
        public List<Extremum> highExtremes;

        public TrendResult(String trend,String name, int typeD, List<Extremum> lowExtremes, List<Extremum> highExtremes) {
            this.trend = trend;
            this.name = name;
            this.typeD = typeD;
            this.lowExtremes = lowExtremes;
            this.highExtremes = highExtremes;
        }

        @Override
        public String toString() {
            return  "TrendResult{" + name +
                    "   trend='" + trend + '\n' +
                    ", lowExtremes=" + lowExtremes + '\n'+
                    ", highExtremes=" + highExtremes + '\n'+
                    '}';
        }
    }

    public static TrendResult detectTrendWithExtremes(TimeSeries series, int range, int filterRange) {
        List<Extremum> allMinima = new ArrayList<>();
        List<Extremum> allMaxima = new ArrayList<>();
        String name = series.getName();
        int endIndex = series.getEndIndex();
        boolean[] excluded = new boolean[endIndex + 1];
        if (endIndex < range) {
            return new TrendResult("Insufficient Data",name,0, new ArrayList<>(),new ArrayList<>());
        }

        // Сбор всех минимумов и максимумов
        for (int i = endIndex - range + 1; i <= endIndex; i++) {
            if (excluded[i]) {
                continue; // Пропускаем свечи, попадающие в фильтр
            }
            double currentHigh = series.getBar(i).getMaxPrice().doubleValue();
            double currentLow = series.getBar(i).getMinPrice().doubleValue();
            ZonedDateTime currentTime = series.getBar(i).getEndTime();

            if (isSwingHigh(series, i)) {
                allMaxima.add(new Extremum("High", i, currentHigh, currentTime));
                excludeRange(excluded, i, filterRange);
            }
            if (isSwingLow(series, i)) {
                allMinima.add(new Extremum("Low", i, currentLow, currentTime));
                excludeRange(excluded, i, filterRange);
            }
        }

        // Сортируем минимумы и максимумы по значению
        allMinima.sort(Comparator.comparingDouble(e -> e.value));
        allMaxima.sort((e1, e2) -> Double.compare(e2.value, e1.value)); // Обратная сортировка для максимумов

        // Берем 3 самых значимых экстремума
        List<Extremum> keyMinima = allMinima.subList(0, Math.min(3, allMinima.size()));
        List<Extremum> keyMaxima = allMaxima.subList(0, Math.min(3, allMaxima.size()));

        // Определяем тренд
        String trend = determineTrend(keyMinima, keyMaxima);
        int typeD = 0;
        if (trend == "Uptrend")  {typeD = 1;}
        if (trend == "Downtrend") {typeD = -1;}
        // Объединяем ключевые экстремумы
        List<Extremum> lowExtremes = new ArrayList<>();
        lowExtremes.addAll(keyMinima);
        List<Extremum> highExtremes = new ArrayList<>();
        highExtremes.addAll(keyMaxima);

        // Возвращаем результат
        return new TrendResult(trend, name, typeD, lowExtremes, highExtremes);
    }

    private static void excludeRange(boolean[] excluded, int index, int filterRange) {
        for (int i = Math.max(0, index - filterRange); i <= Math.min(excluded.length - 1, index + filterRange); i++) {
            excluded[i] = true;
        }
    }
    private static boolean isSwingHigh(TimeSeries series, int index) {
        if (index < 2 || index > series.getEndIndex() - 2) {
            return false;
        }
        double currentHigh = series.getBar(index).getMaxPrice().doubleValue();
        double leftHigh = series.getBar(index - 1).getMaxPrice().doubleValue();
        double rightHigh = series.getBar(index + 1).getMaxPrice().doubleValue();
        return currentHigh > leftHigh && currentHigh > rightHigh;
    }

    private static boolean isSwingLow(TimeSeries series, int index) {
        if (index < 2 || index > series.getEndIndex() - 2) {
            return false;
        }
        double currentLow = series.getBar(index).getMinPrice().doubleValue();
        double leftLow = series.getBar(index - 1).getMinPrice().doubleValue();
        double rightLow = series.getBar(index + 1).getMinPrice().doubleValue();
        return currentLow < leftLow && currentLow < rightLow;
    }

    private static String determineTrend(List<Extremum> minima, List<Extremum> maxima) {
        if (minima.isEmpty() || maxima.isEmpty()) {
            return "Undefined";
        }

        // Сравниваем индексы ключевых минимумов и максимумов
        int minIndex = minima.get(0).index;
        int maxIndex = maxima.get(0).index;

        if (minIndex < maxIndex) {
            return "Uptrend";
        } else {
            return "Downtrend";
        }
    }
}