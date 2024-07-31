//package com.my.copybot.util;
//
//import org.ta4j.core.*;
//import org.ta4j.core.indicators.*;
//import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
//import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
//import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
//import org.ta4j.core.trading.rules.OverIndicatorRule;
//import org.ta4j.core.trading.rules.UnderIndicatorRule;
//
//public class StrategySMA {
//
//
//    public static final String STRATEGY = "SMA";
//
//
//    public static Strategy buildSmaStrategyLong(TimeSeries series) {
//        if (series == null) {
//            throw new IllegalArgumentException("Series cannot be null");
//        }
//
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//
//        EMAIndicator sma14 = new EMAIndicator(closePrice, 100);
//        EMAIndicator sma50 = new EMAIndicator(closePrice, 50);
//        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
//        MACDIndicator macdLong = new MACDIndicator(closePrice, 19, 39);
//        EMAIndicator emaMacd = new EMAIndicator(macd, 9);
//        EMAIndicator emaMacdLong = new EMAIndicator(macd, 25);
//
//        EMAIndicator ema22 = new EMAIndicator(closePrice, 22);
//        EMAIndicator ema10 = new EMAIndicator(closePrice, 10);
//
//        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
//
//
//        int maxIndex = series.getEndIndex();
//
//
//        boolean macdChange = (macd.getValue(maxIndex - 3).doubleValue() > macd.getValue(maxIndex - 2).doubleValue())
//                && (macd.getValue(maxIndex - 2).doubleValue() > macd.getValue(maxIndex - 1).doubleValue())
//                && (macd.getValue(maxIndex - 1).doubleValue() < macd.getValue(maxIndex).doubleValue());
//
//
//        boolean macdTrend = (macdLong.getValue(maxIndex - 1).doubleValue() < macdLong.getValue(maxIndex).doubleValue());
//
//        boolean emaTrend = (sma14.getValue(maxIndex).doubleValue() > sma14.getValue((maxIndex - 1)).doubleValue());
//
//        Decimal deltaK = Decimal.valueOf(-2);
//
//        if (macdChange && macdTrend && emaTrend) {
//            deltaK = Decimal.valueOf(102);
//        }
//
//        Rule entryRule = ((new UnderIndicatorRule(macd, emaMacd))
//                .and(new UnderIndicatorRule(rsi, deltaK))
//                .and(new OverIndicatorRule(macdLong, emaMacdLong))
//                .and(new UnderIndicatorRule(closePrice, ema22))
//                .and(new OverIndicatorRule(sma50, sma14)))
//
//                .or((new CrossedUpIndicatorRule(macdLong, emaMacdLong))
//                        .and(new OverIndicatorRule(macd, emaMacd))
//                        .and(new OverIndicatorRule(sma50, sma14))
//                );
//
//
//        macdChange = emaMacd.getValue(maxIndex).doubleValue() < emaMacd.getValue(maxIndex - 1).doubleValue();
//        emaTrend = (ema22.getValue(maxIndex).doubleValue() < ema22.getValue((maxIndex - 1)).doubleValue());
//
//        if (macdChange && !emaTrend) {
//            deltaK = Decimal.valueOf(-2);
//        } else {
//            deltaK = Decimal.valueOf(102);
//        }
//
//        Rule exitRule = (new OverIndicatorRule(rsi, deltaK))
//                .or(new UnderIndicatorRule(macd, emaMacd))
//                .or(new UnderIndicatorRule(sma50, sma14));
//
//        return new BaseStrategy(entryRule, exitRule);
//    }
//
//
//    public static Strategy buildSmaStrategyShort(TimeSeries series) {
//        if (series == null) {
//            throw new IllegalArgumentException("Series cannot be null");
//        }
//
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//
//        EMAIndicator sma14 = new EMAIndicator(closePrice, 100);
//        EMAIndicator sma50 = new EMAIndicator(closePrice, 50);
//        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
//        MACDIndicator macdLong = new MACDIndicator(closePrice, 19, 39);
//        EMAIndicator emaMacd = new EMAIndicator(macd, 9);
//        EMAIndicator emaMacdLong = new EMAIndicator(macd, 25);
//
//        EMAIndicator ema22 = new EMAIndicator(closePrice, 22);
//        EMAIndicator ema10 = new EMAIndicator(closePrice, 10);
//
//        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
//
//
//        int maxIndex = series.getEndIndex();
//
//        boolean macdChange = macd.getValue(maxIndex - 3).doubleValue() > macd.getValue(maxIndex - 2).doubleValue()
//                && (macd.getValue(maxIndex - 2).doubleValue() > macd.getValue(maxIndex - 1).doubleValue())
//                && (macd.getValue(maxIndex - 1).doubleValue() < macd.getValue(maxIndex).doubleValue());
//
//
//        boolean macdTrend = (macdLong.getValue(maxIndex - 1).doubleValue() > macdLong.getValue(maxIndex).doubleValue());
//
//        boolean emaTrend = (sma14.getValue(maxIndex).doubleValue() < sma14.getValue(maxIndex - 1).doubleValue());
//
//        Decimal deltaK = Decimal.valueOf(-2);
//
//        if (macdChange && macdTrend && emaTrend) {
//            deltaK = Decimal.valueOf(102);
//        }
//
//        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
//
////        System.out.println(series.getName());
////        for (int i = 0; i < series.getBarCount(); i++) {
////            System.out.println("StochRSI %K at index " + i + ": " + stochRsi.getValue(i));
////        }
//        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
//        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3); // 3-периодное SMA
//
//        //  for (int i = 495; i < series.getBarCount(); i++) {
//        if (smoothedStochRsi.getValue(series.getBarCount() - 1).compareTo(stochRsiD.getValue(series.getBarCount() - 1)) > 0
//                && smoothedStochRsi.getValue(series.getBarCount() - 2).compareTo(stochRsiD.getValue(series.getBarCount() - 2)) < 0
//                && smoothedStochRsi.getValue(series.getBarCount() - 1).multipliedBy(100).intValue() < 30) {
//            System.out.println("LONG " + series.getName() + "   StochRSI %K at index : " + smoothedStochRsi.getValue(series.getBarCount() - 1).multipliedBy(100) + "   StochRSI %D at index : " + stochRsiD.getValue(series.getBarCount() - 1).multipliedBy(100));
//        }
//
//        if (smoothedStochRsi.getValue(series.getBarCount() - 1).compareTo(stochRsiD.getValue(series.getBarCount() - 1)) < 0
//                && smoothedStochRsi.getValue(series.getBarCount() - 2).compareTo(stochRsiD.getValue(series.getBarCount() - 2)) > 0
//                && smoothedStochRsi.getValue(series.getBarCount() - 1).multipliedBy(100).intValue() > 80) {
//            System.out.println("SHORT " + series.getName() + "   StochRSI %K at index : " + smoothedStochRsi.getValue(series.getBarCount() - 1).multipliedBy(100) + "   StochRSI %D at index : " + stochRsiD.getValue(series.getBarCount() - 1).multipliedBy(100));
//        }
//
//
//        Rule entryRule = (new OverIndicatorRule(macd, emaMacd)
//                .and(new UnderIndicatorRule(rsi, deltaK))
//                .and(new UnderIndicatorRule(macdLong, emaMacdLong))
//                .and(new OverIndicatorRule(closePrice, ema22))
//                .and(new UnderIndicatorRule(sma50, sma14)))
//
//                .or((new CrossedDownIndicatorRule(macdLong, emaMacdLong))
//                        .and(new UnderIndicatorRule(macd, emaMacd))
//                        .and(new UnderIndicatorRule(sma50, sma14))
//
//                );
//
//        macdChange = emaMacd.getValue(maxIndex).doubleValue() > emaMacd.getValue(maxIndex - 3).doubleValue();
//        emaTrend = (ema22.getValue(maxIndex).doubleValue() > ema22.getValue((maxIndex - 2)).doubleValue());
//
//
//        if (!emaTrend && macdChange) {
//
//            deltaK = Decimal.valueOf(-2);
//        } else {
//            deltaK = Decimal.valueOf(102);
//        }
//
//        Rule exitRule = (new OverIndicatorRule(rsi, deltaK))
//                .or(new OverIndicatorRule(macd, emaMacd))
//                .or(new OverIndicatorRule(sma50, sma14));
//
//        return new BaseStrategy(entryRule, exitRule);
//
//    }
//}
//
//
//
//
//
