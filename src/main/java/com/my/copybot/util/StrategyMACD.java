package com.my.copybot.util;

import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.OpenPriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

public class StrategyMACD {


    public static final String STRATEGY = "MACD";


    public static Strategy buildMacdStrategyLong(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator sma14 = new EMAIndicator(closePrice, 50);
        EMAIndicator sma24 = new EMAIndicator(closePrice, 100);
        EMAIndicator emaShort = new EMAIndicator(closePrice, 5);
        EMAIndicator emaLong = new EMAIndicator(closePrice, 15);
        MACDIndicator macd = new MACDIndicator(closePrice, 10, 30);
        EMAIndicator emaMacd = new EMAIndicator(macd, 20);

        RSIIndicator rsi = new RSIIndicator(closePrice, 14);


        int maxIndex = series.getEndIndex();

        Decimal diffEmaLong = Decimal.valueOf(emaLong.getValue(maxIndex).toDouble()
                - emaLong.getValue(maxIndex - 1).toDouble());

        Decimal diffEmaShort = Decimal.valueOf(emaShort.getValue(maxIndex).toDouble()
                - emaShort.getValue(maxIndex - 1).toDouble());

        Decimal diffMacd = Decimal.valueOf(macd.getValue(maxIndex).toDouble()
                - macd.getValue(maxIndex - 1).toDouble());


        Decimal deltaK = Decimal.valueOf(102);

        if ((diffEmaShort.doubleValue() > 0) && (diffMacd.doubleValue() > 0)) {
            deltaK = Decimal.valueOf(-2);
        }

        Rule entryRule = new CrossedUpIndicatorRule(macd, emaMacd) // (new CrossedUpIndicatorRule(emaShort, emaLong)
                .and(new OverIndicatorRule(sma14, sma24))
                .and(new OverIndicatorRule(rsi, deltaK));
//                .or((new CrossedUpIndicatorRule(sma14, sma24)
//                        .and(new OverIndicatorRule(emaShort, emaLong))
//                        .and(new OverIndicatorRule(rsi, deltaK))));



//        Rule entryRule = new CrossedUpIndicatorRule(macd, emaMacd)
//                .and(new OverIndicatorRule(sma14, sma24))
//                .and(new OverIndicatorRule(ssK, ssD))
//                .and(new OverIndicatorRule(emaShort, emaLong))
//                .and(new OverIndicatorRule(stochK, stochD));

        //     .and(new Is(bullishHarami, Decimal.valueOf(1)));
        //  .and(new OverIndicatorRule(rsi, levelRsiStoch))
        //        .and(new UnderIndicatorRule(macdDirection, emaMacdDirection))
// && (diffEmaShort.doubleValue() < 0) && (diffEmaLong.doubleValue() < 0)

        if ((diffMacd.doubleValue() < 0) && (diffEmaShort.doubleValue() < 0) && (diffEmaLong.doubleValue() < 0)) {
            deltaK = Decimal.valueOf(-2);
        } else {
            deltaK = Decimal.valueOf(102);
        }


        Rule exitRule = // (new UnderIndicatorRule(ssK, ssD))
        //(new CrossedDownIndicatorRule(macd, emaMacd))
                // (new UnderIndicatorRule(emaShort, emaLong))
                //(new OverIndicatorRule(sma14, sma24));
                (new OverIndicatorRule(rsi, deltaK));


        return new BaseStrategy(entryRule, exitRule);
    }


    public static Strategy buildMacdStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        OpenPriceIndicator openPrice = new OpenPriceIndicator(series);

        EMAIndicator sma14 = new EMAIndicator(closePrice, 50);
        EMAIndicator sma24 = new EMAIndicator(closePrice, 100);
        EMAIndicator emaShort = new EMAIndicator(closePrice, 5);
        EMAIndicator emaLong = new EMAIndicator(closePrice, 15);
        MACDIndicator macd = new MACDIndicator(closePrice, 10, 30);
        EMAIndicator emaMacd = new EMAIndicator(macd, 20);


        RSIIndicator rsi = new RSIIndicator(closePrice, 14);


        int maxIndex = series.getEndIndex();

        Decimal diffEmaLong = Decimal.valueOf(emaLong.getValue(maxIndex).toDouble()
                - emaLong.getValue(maxIndex - 1).toDouble());

        Decimal diffEmaShort = Decimal.valueOf(emaShort.getValue(maxIndex).toDouble()
                - emaShort.getValue(maxIndex - 1).toDouble());

        Decimal diffMacd = Decimal.valueOf(macd.getValue(maxIndex).toDouble()
                - macd.getValue(maxIndex - 1).toDouble());


        Decimal deltaK = Decimal.valueOf(-2);

        if ((diffEmaShort.doubleValue() < 0) && (diffMacd.doubleValue() < 0)) {
            deltaK = Decimal.valueOf(102);
        }

//        Rule entryRule = new CrossedDownIndicatorRule(macd, emaMacd)
//                .and(new UnderIndicatorRule(sma14, sma24))
//                .and(new UnderIndicatorRule(ssK, ssD))
//                .and(new UnderIndicatorRule(emaShort, emaLong))
//                .and(new UnderIndicatorRule(stochK, stochD));

        Rule entryRule = new CrossedDownIndicatorRule(macd, emaMacd) // (new CrossedDownIndicatorRule(emaShort, emaLong)
                .and(new UnderIndicatorRule(sma14, sma24))
                .and(new OverIndicatorRule(rsi, deltaK));
//                .or((new CrossedDownIndicatorRule(sma14, sma24)
//                        .and(new UnderIndicatorRule(emaShort, emaLong))
//                        .and(new OverIndicatorRule(rsi, deltaK))));

        if ((diffMacd.doubleValue() > 0) && (diffEmaShort.doubleValue() > 0) && (diffEmaLong.doubleValue() > 0)) {
            deltaK = Decimal.valueOf(-2);
        } else {
            deltaK = Decimal.valueOf(102);
        }

//
//        System.out.println(series.getName() + "  Long = " + (closePrice.getValue(maxIndex)) + "   D= " +(openPrice.getValue(maxIndex))
//                + " Diff = " + diffPrice);
//                + "   K(my) = " + ssK.getValue(series.getEndIndex())
//                + "   D(my) = " + ssD.getValue(series.getEndIndex())
//                + "   K%(my) = " + stochRsiK);
////        System.out.println("RSI = " + rsiValue);


        Rule exitRule = // (new OverIndicatorRule(ssK, ssD))
                //   new CrossedUpIndicatorRule(macd, emaMacd)
                (new OverIndicatorRule(sma14, sma24))
                        .or(new OverIndicatorRule(rsi, deltaK));
        //     .or(new OverIndicatorRule(macd, emaMacd))
        //      .or(new OverIndicatorRule(stochK, stochD));


        return new BaseStrategy(entryRule, exitRule);
    }
}





