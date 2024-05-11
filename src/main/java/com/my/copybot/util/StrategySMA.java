package com.my.copybot.util;

import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

public class StrategySMA {


    public static final String STRATEGY = "SMA";


    public static Strategy buildSmaStrategyLong(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator sma14 = new EMAIndicator(closePrice, 50);
        EMAIndicator sma24 = new EMAIndicator(closePrice, 100);
        EMAIndicator emaShort = new EMAIndicator(closePrice, 10);
        EMAIndicator emaLong = new EMAIndicator(closePrice, 15);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);

        RSIIndicator rsi = new RSIIndicator(closePrice, 14);

        int maxIndex = series.getEndIndex();


        boolean macdChange = (macd.getValue(maxIndex - 3).doubleValue() > macd.getValue(maxIndex - 2).doubleValue())
                && (macd.getValue(maxIndex - 2).doubleValue() > macd.getValue(maxIndex - 1).doubleValue())
                && (macd.getValue(maxIndex - 1).doubleValue() < macd.getValue(maxIndex).doubleValue());


        // Проверка MACD на слом направления движенмия

        Double diffSma = Math.abs(sma24.getValue(maxIndex).toDouble()
                - sma14.getValue(maxIndex).toDouble());
        Double diffSmaP = Math.abs(sma24.getValue(maxIndex - 1).toDouble()
                - sma14.getValue(maxIndex - 1).toDouble());


        boolean emaTrend = diffSma.doubleValue() > diffSmaP.doubleValue();

        // Проверка старших EMA на расширение

        Decimal deltaK = Decimal.valueOf(-2);

        if (macdChange && emaTrend) {
            deltaK = Decimal.valueOf(102);
        }

        Rule entryRule = new UnderIndicatorRule(macd, emaMacd)
                .and(new OverIndicatorRule(sma14, sma24))
                .and(new UnderIndicatorRule(rsi, deltaK));


//        if (Math.abs(diffSma) < Math.abs(diffSmaP)) {
//            deltaK = Decimal.valueOf(-2);
//        } else {
//            deltaK = Decimal.valueOf(102);
//        }

        Rule exitRule = new OverIndicatorRule(sma14, emaShort);

        return new BaseStrategy(entryRule, exitRule);
    }


    public static Strategy buildSmaStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator sma14 = new EMAIndicator(closePrice, 50);
        EMAIndicator sma24 = new EMAIndicator(closePrice, 100);
        EMAIndicator emaShort = new EMAIndicator(closePrice, 10);
        EMAIndicator emaLong = new EMAIndicator(closePrice, 15);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);

        int maxIndex = series.getEndIndex();


        boolean macdChange = macd.getValue(maxIndex - 3).doubleValue() > macd.getValue(maxIndex - 2).doubleValue()
                && (macd.getValue(maxIndex - 2).doubleValue() > macd.getValue(maxIndex - 1).doubleValue())
                && (macd.getValue(maxIndex - 1).doubleValue() < macd.getValue(maxIndex).doubleValue());


        // Проверка MACD на слом направления движенмия

        Double diffSma = (sma24.getValue(maxIndex).toDouble()
                - sma14.getValue(maxIndex).toDouble());
        Double diffSmaP = (sma24.getValue(maxIndex - 1).toDouble()
                - sma14.getValue(maxIndex - 1).toDouble());

        boolean emaTrend = false;

        if (Math.abs(diffSma) > Math.abs(diffSmaP)) {
            macdChange = true;
        }

        // Проверка старших EMA на расширение

        Decimal deltaK = Decimal.valueOf(-2);

        if (macdChange && emaTrend) {
            deltaK = Decimal.valueOf(102);
        }

        Rule entryRule = new OverIndicatorRule(macd, emaMacd)
                .and(new UnderIndicatorRule(sma14, sma24))
                .and(new UnderIndicatorRule(rsi, deltaK));


//        if (Math.abs(diffSma) < Math.abs(diffSmaP)) {
//
//            deltaK = Decimal.valueOf(-2);
//        } else {
//            deltaK = Decimal.valueOf(102);
//        }

        Rule exitRule = new UnderIndicatorRule(sma14, emaShort);


        return new BaseStrategy(entryRule, exitRule);
    }
}





