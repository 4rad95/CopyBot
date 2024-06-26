package com.my.copybot.util;

import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

public class StrategyStoch {


    public static final String STRATEGY = "STOCH";


    public static Strategy buildStochStrategyLong(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator sma14 = new EMAIndicator(closePrice, 100);
        EMAIndicator sma50 = new EMAIndicator(closePrice, 50);
        MACDIndicator macd = new MACDIndicator(closePrice, 19, 39);


        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3); // 3-периодное SMA

        int maxIndex = series.getEndIndex();



        Decimal deltaK = Decimal.valueOf(-2);
        if (smoothedStochRsi.getValue(maxIndex).compareTo(stochRsiD.getValue(maxIndex)) > 0
                && smoothedStochRsi.getValue(maxIndex - 1).compareTo(stochRsiD.getValue(maxIndex - 1)) < 0
                && smoothedStochRsi.getValue(maxIndex).compareTo(smoothedStochRsi.getValue(maxIndex - 1)) > 0
                && macd.getValue(maxIndex).compareTo(macd.getValue(maxIndex - 1)) > 0
                && smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() < 25
                && macd.getValue(maxIndex).compareTo(macd.getValue(maxIndex - 1)) > 0) {

            deltaK = Decimal.valueOf(102);

        }


        Rule entryRule = (new OverIndicatorRule(sma50, sma14))
                .and(new UnderIndicatorRule(rsi, deltaK));


        deltaK = Decimal.valueOf(102);

        if (stochRsiD.getValue(maxIndex).multipliedBy(100).intValue() < smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue()) {

            deltaK = Decimal.valueOf(-2);
            //         System.out.println("SHORT " +series.getName() + "   StochRSI %K at index : " + smoothedStochRsi.getValue(series.getBarCount()-1).multipliedBy(100) + "   StochRSI %D at index : " + stochRsiD.getValue(series.getBarCount()-1).multipliedBy(100));
        }
        Rule exitRule = (new OverIndicatorRule(rsi, deltaK))
                .or(new UnderIndicatorRule(sma50, sma14));
        //          .or(new UnderIndicatorRule(smoothedStochRsi, stochRsiD));


        return new BaseStrategy(entryRule, exitRule);
    }

    public static Strategy buildStochStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator sma14 = new EMAIndicator(closePrice, 100);
        EMAIndicator sma50 = new EMAIndicator(closePrice, 50);

        MACDIndicator macd = new MACDIndicator(closePrice, 19, 39);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);

        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3);

        int maxIndex = series.getEndIndex();
        Decimal deltaK = Decimal.valueOf(-2);

        if (smoothedStochRsi.getValue(maxIndex).compareTo(stochRsiD.getValue(maxIndex)) < 0
                && smoothedStochRsi.getValue(maxIndex - 1).compareTo(stochRsiD.getValue(maxIndex - 2)) > 0
                && smoothedStochRsi.getValue(maxIndex).compareTo(smoothedStochRsi.getValue(maxIndex - 1)) < 0
                && macd.getValue(maxIndex).compareTo(macd.getValue(maxIndex - 1)) < 0
                && smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue() > 75
                && macd.getValue(maxIndex).compareTo(macd.getValue(maxIndex - 1)) < 0) {
            deltaK = Decimal.valueOf(102);
            //         System.out.println("SHORT " +series.getName() + "   StochRSI %K at index : " + smoothedStochRsi.getValue(series.getBarCount()-1).multipliedBy(100) + "   StochRSI %D at index : " + stochRsiD.getValue(series.getBarCount()-1).multipliedBy(100));
        }
        Rule entryRule = (new UnderIndicatorRule(sma50, sma14))
                .and(new UnderIndicatorRule(rsi, deltaK));

        deltaK = Decimal.valueOf(120);
        if (stochRsiD.getValue(maxIndex).multipliedBy(100).intValue() > smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue()) {
            deltaK = Decimal.valueOf(-100);
            //         System.out.println("SHORT "+series.getName()+ "   rsi = " + rsi.getValue(series.getBarCount()-1)); //+"   StochRSI %K at index : " + smoothedStochRsi.getValue(series.getBarCount()-1).multipliedBy(100) + "   StochRSI %D at index : " + stochRsiD.getValue(series.getBarCount()-1).multipliedBy(100));
        }

        Rule exitRule = (new OverIndicatorRule(rsi, deltaK))
                .or(new OverIndicatorRule(sma50, sma14));
        //                .or(new OverIndicatorRule(smoothedStochRsi, stochRsiD));

        return new BaseStrategy(entryRule, exitRule);
    }
}





