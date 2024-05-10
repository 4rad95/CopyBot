package com.my.copybot.util;

import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.MaxPriceIndicator;
import org.ta4j.core.indicators.helpers.MinPriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
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
        StochasticOscillatorKIndicator ssK = new StochasticOscillatorKIndicator(rsi, 14, new MaxPriceIndicator(series), new MinPriceIndicator(series));
        StochasticOscillatorDIndicator ssD = new StochasticOscillatorDIndicator(ssK);

        int maxIndex = series.getEndIndex();
        Decimal diffMacd = Decimal.valueOf(macd.getValue(maxIndex).toDouble()
                - macd.getValue(maxIndex - 1).toDouble());
        Decimal diffMacd1 = Decimal.valueOf(macd.getValue(maxIndex - 1).toDouble()
                - macd.getValue(maxIndex - 2).toDouble());
        Decimal diffMacd2 = Decimal.valueOf(macd.getValue(maxIndex - 2).toDouble()
                - macd.getValue(maxIndex - 3).toDouble());

        boolean macdChange = (diffMacd1.doubleValue() < 0) && (diffMacd2.doubleValue() < 0) && (diffMacd.doubleValue() > 0);

        // Проверка MACD на слом направления движенмия

        Decimal diffSma = Decimal.valueOf(sma24.getValue(maxIndex).toDouble()
                - sma14.getValue(maxIndex).toDouble());
        Decimal diffSmaP = Decimal.valueOf(sma24.getValue(maxIndex - 1).toDouble()
                - sma14.getValue(maxIndex - 1).toDouble());

        boolean emaTrend = diffSma.doubleValue() > diffSmaP.doubleValue();

        // Проверка старших EMA на расширение

        Decimal deltaK = Decimal.valueOf(-2);

        if (macdChange && emaTrend) {
            deltaK = Decimal.valueOf(102);
        }

        Rule entryRule = new UnderIndicatorRule(macd, emaMacd)
                .and(new OverIndicatorRule(sma14, sma24))
                .and(new OverIndicatorRule(rsi, deltaK));


        if ((macdChange) && (!emaTrend)) {
            deltaK = Decimal.valueOf(-2);
        } else {
            deltaK = Decimal.valueOf(102);
        }

        Rule exitRule = new OverIndicatorRule(rsi, deltaK);

        return new BaseStrategy(entryRule, exitRule);
    }


    public static Strategy buildSmaStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        OpenPriceIndicator openPrice = new OpenPriceIndicator(series);

        EMAIndicator sma14 = new EMAIndicator(closePrice, 50);
        EMAIndicator sma24 = new EMAIndicator(closePrice, 100);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);

        int maxIndex = series.getEndIndex();
        Decimal diffMacd = Decimal.valueOf(macd.getValue(maxIndex).toDouble()
                - macd.getValue(maxIndex - 1).toDouble());
        Decimal diffMacd1 = Decimal.valueOf(macd.getValue(maxIndex - 1).toDouble()
                - macd.getValue(maxIndex - 2).toDouble());
        Decimal diffMacd2 = Decimal.valueOf(macd.getValue(maxIndex - 2).toDouble()
                - macd.getValue(maxIndex - 3).toDouble());

        boolean macdChange = (diffMacd1.doubleValue() > 0) && (diffMacd2.doubleValue() > 0) && (diffMacd.doubleValue() < 0);

        // Проверка MACD на слом направления движенмия

        Decimal diffSma = Decimal.valueOf(sma24.getValue(maxIndex).toDouble()
                - sma14.getValue(maxIndex).toDouble());
        Decimal diffSmaP = Decimal.valueOf(sma24.getValue(maxIndex - 1).toDouble()
                - sma14.getValue(maxIndex - 1).toDouble());

        boolean emaTrend = diffSma.doubleValue() > diffSmaP.doubleValue();
        // Проверка старших EMA на расширение

        Decimal deltaK = Decimal.valueOf(-2);

        if (macdChange && emaTrend) {
            deltaK = Decimal.valueOf(102);
        }

        Rule entryRule = new OverIndicatorRule(macd, emaMacd)
                .and(new UnderIndicatorRule(sma14, sma24))
                .and(new OverIndicatorRule(rsi, deltaK));


        if ((macdChange) && (!emaTrend)) {
            deltaK = Decimal.valueOf(-2);
        } else {
            deltaK = Decimal.valueOf(102);
        }

        Rule exitRule = new OverIndicatorRule(rsi, deltaK);

        return new BaseStrategy(entryRule, exitRule);
    }
}





