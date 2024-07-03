package com.my.copybot.util;

import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

public class StrategyStoch {


    public static final String STRATEGY = "STOCH";


    public static Strategy buildStochStrategyLong(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator ema100 = new EMAIndicator(closePrice, 100);
        EMAIndicator ema50 = new EMAIndicator(closePrice, 50);
        MACDIndicator macd = new MACDIndicator(closePrice, 19, 39);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);

        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 7);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 7); // 3-периодное SMA

        int maxIndex = series.getEndIndex();



        Decimal deltaK = Decimal.valueOf(-2);
        if ((stochRsiD.getValue(maxIndex).multipliedBy(100).intValue() < smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue())
                && (smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue() < stochRsiD.getValue(maxIndex - 2).multipliedBy(100).intValue())
                && (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() > smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue())
                && smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() < 25) {

            deltaK = Decimal.valueOf(102);

        }

        Rule entryRule = ((new OverIndicatorRule(ema50, ema100))
                .and(new OverIndicatorRule(rsi, deltaK))
                .and(new OverIndicatorRule(smoothedStochRsi, stochRsiD))
                //               .and(new UnderIndicatorRule(smoothedStochRsi, Decimal.valueOf(0.25)))
                .and(new CrossedUpIndicatorRule(smoothedStochRsi, stochRsiD)));

        deltaK = Decimal.valueOf(102);
        if (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() > smoothedStochRsi.getValue(maxIndex - 3).multipliedBy(100).intValue()) {
            deltaK = Decimal.valueOf(-2);
            //         System.out.println("SHORT " +series.getName() + "   StochRSI %K at index : " + smoothedStochRsi.getValue(series.getBarCount()-1).multipliedBy(100) + "   StochRSI %D at index : " + stochRsiD.getValue(series.getBarCount()-1).multipliedBy(100));
        }
        Rule exitRule = ((new OverIndicatorRule(rsi, deltaK))
                .and(new UnderIndicatorRule(smoothedStochRsi, stochRsiD)));
//                .or(new CrossedDownIndicatorRule(smoothedStochRsi, stochRsiD));


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
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 7);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 7);

        int maxIndex = series.getEndIndex();
        Decimal deltaK = Decimal.valueOf(-2);

        if ((stochRsiD.getValue(maxIndex).multipliedBy(100).intValue() > smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue())
                && (smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue() > stochRsiD.getValue(maxIndex - 2).multipliedBy(100).intValue())
                && (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() < smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue())
                && (smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue() > 75)) {
            deltaK = Decimal.valueOf(102);
            //         System.out.println("SHORT " +series.getName() + "   StochRSI %K at index : " + smoothedStochRsi.getValue(series.getBarCount()-1).multipliedBy(100) + "   StochRSI %D at index : " + stochRsiD.getValue(series.getBarCount()-1).multipliedBy(100));
        }
        Rule entryRule = ((new UnderIndicatorRule(sma50, sma14))
                .and(new OverIndicatorRule(rsi, deltaK))
                .and(new UnderIndicatorRule(smoothedStochRsi, stochRsiD))
//                .and(new OverIndicatorRule(smoothedStochRsi, Decimal.valueOf(0.75)))
                .and(new CrossedDownIndicatorRule(smoothedStochRsi, stochRsiD)));

        deltaK = Decimal.valueOf(120);
        if (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() > smoothedStochRsi.getValue(maxIndex - 2).multipliedBy(100).intValue()) {
            deltaK = Decimal.valueOf(-20);
            //     System.out.println("SHORT "+series.getName()+ "   rsi = " + rsi.getValue(series.getBarCount()-1)); //+"   StochRSI %K at index : " + smoothedStochRsi.getValue(series.getBarCount()-1).multipliedBy(100) + "   StochRSI %D at index : " + stochRsiD.getValue(series.getBarCount()-1).multipliedBy(100));
        }

        Rule exitRule = ((new OverIndicatorRule(rsi, deltaK))
                .and(new OverIndicatorRule(smoothedStochRsi, stochRsiD)));
        //    .and(new CrossedUpIndicatorRule(smoothedStochRsi, stochRsiD));


        return new BaseStrategy(entryRule, exitRule);
    }

    public static Boolean openStochStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator sma14 = new EMAIndicator(closePrice, 100);
        EMAIndicator sma50 = new EMAIndicator(closePrice, 50);

        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);

        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3);

        int maxIndex = series.getEndIndex();
        Decimal deltaK = Decimal.valueOf(-2);

        return (stochRsiD.getValue(maxIndex).multipliedBy(100).intValue() > smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue())
                && (sma50.getValue(maxIndex).doubleValue() < sma14.getValue(maxIndex).doubleValue())
                && (smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue() > stochRsiD.getValue(maxIndex - 2).multipliedBy(100).intValue())
                && (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() < smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue())
                && (macd.getValue(maxIndex).doubleValue() < macd.getValue(maxIndex - 1).doubleValue())
                && (smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue() > 75);

    }

    public static Boolean closeStochStrategyShort(TimeSeries series) {

        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3);

        int maxIndex = series.getEndIndex();


        return smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() > smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue();
    }

    public static Boolean openStochStrategyLong(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator ema100 = new EMAIndicator(closePrice, 100);
        EMAIndicator ema50 = new EMAIndicator(closePrice, 50);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);

        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3); // 3-периодное SMA

        int maxIndex = series.getEndIndex();


        return (stochRsiD.getValue(maxIndex).multipliedBy(100).intValue() < smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue())
                && (ema50.getValue(maxIndex).doubleValue() > ema100.getValue(maxIndex).doubleValue())
                && (smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue() < stochRsiD.getValue(maxIndex - 2).multipliedBy(100).intValue())
                && (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() > smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue())
                && (macd.getValue(maxIndex).doubleValue() > macd.getValue(maxIndex - 1).doubleValue())
                && smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() < 25;

    }

    public static Boolean closeStochStrategyLong(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3); // 3-периодное SMA

        int maxIndex = series.getEndIndex();

        return smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() < smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue();
    }

}





