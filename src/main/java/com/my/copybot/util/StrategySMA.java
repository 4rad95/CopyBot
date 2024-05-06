package com.my.copybot.util;

import org.ta4j.core.*;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;


public class StrategySMA {

    public static String STRATEGY = "SMA";

    public static Strategy buildSmaStrategyLong(TimeSeries series, String strategyCode) {
        if (STRATEGY.equals(strategyCode)) {
            return buildSmaStrategyLong(series);
        }
        return null;
    }

    public static Strategy buildSmaStrategyLong(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator sma = new SMAIndicator(closePrice, 10);

        Decimal entryLevel = Decimal.valueOf("1.10").multipliedBy(sma.getValue(series.getEndIndex())); // Уровень входа: 110% от средней цены закрытия
        Decimal exitLevel = Decimal.valueOf("1.05").multipliedBy(sma.getValue(series.getEndIndex()));  // Уровень выхода: 105% от средней цены закрытия

        Rule entryRule = new CrossedDownIndicatorRule(closePrice, entryLevel);
        Rule exitRule = new CrossedUpIndicatorRule(closePrice, exitLevel);

        return new BaseStrategy(entryRule, exitRule);

    }


    public static Strategy buildStrategyShort(TimeSeries series, String strategyCode) {
        if (STRATEGY.equals(strategyCode)) {
            return buildSmaStrategyShort(series);
        }
        return null;
    }

    public static Strategy buildSmaStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator sma = new SMAIndicator(closePrice, 10);

        Decimal entryLevel = Decimal.valueOf("0.90").multipliedBy(sma.getValue(series.getEndIndex())); // Уровень входа: 90% от средней цены закрытия
        Decimal exitLevel = Decimal.valueOf("0.95").multipliedBy(sma.getValue(series.getEndIndex()));  // Уровень выхода: 95% от средней цены закрытия

        Rule entryRule = new CrossedUpIndicatorRule(closePrice, entryLevel);
        Rule exitRule = new CrossedDownIndicatorRule(closePrice, exitLevel);

        return new BaseStrategy(entryRule, exitRule);

    }


}