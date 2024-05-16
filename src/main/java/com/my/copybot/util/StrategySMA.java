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

        EMAIndicator sma14 = new EMAIndicator(closePrice, 100);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        MACDIndicator macdLong = new MACDIndicator(closePrice, 50, 100);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);
        EMAIndicator emaMacdLong = new EMAIndicator(macd, 50);


        RSIIndicator rsi = new RSIIndicator(closePrice, 14);

        int maxIndex = series.getEndIndex();


        boolean macdChange = (macd.getValue(maxIndex - 3).doubleValue() > macd.getValue(maxIndex - 2).doubleValue())
                && (macd.getValue(maxIndex - 2).doubleValue() > macd.getValue(maxIndex - 1).doubleValue())
                && (macd.getValue(maxIndex - 1).doubleValue() < macd.getValue(maxIndex).doubleValue());


        boolean macdTrend = (macdLong.getValue(maxIndex - 2).doubleValue() < macdLong.getValue(maxIndex).doubleValue());

        boolean emaTrend = (sma14.getValue(maxIndex).doubleValue() > sma14.getValue((maxIndex - 1)).doubleValue());

        Decimal deltaK = Decimal.valueOf(-2);

        if (macdChange && macdTrend && emaTrend) {
            deltaK = Decimal.valueOf(102);
        }

        Rule entryRule = new UnderIndicatorRule(macd, emaMacd)
                .and(new UnderIndicatorRule(rsi, deltaK))
                .and(new UnderIndicatorRule(macdLong, emaMacdLong))
                //      .and(new UnderIndicatorRule(rsi, Decimal.valueOf(50)))
                ;

        macdTrend = macd.getValue(maxIndex).doubleValue() < macd.getValue(maxIndex - 1).doubleValue()
                && macd.getValue(maxIndex - 1).doubleValue() < macd.getValue(maxIndex - 2).doubleValue();

        if (!emaTrend || macdTrend) {
            deltaK = Decimal.valueOf(-2);
        } else {
            deltaK = Decimal.valueOf(102);
        }

        Rule exitRule = new OverIndicatorRule(rsi, deltaK)
                .or(new UnderIndicatorRule(macdLong, emaMacdLong));

        return new BaseStrategy(entryRule, exitRule);
    }


    public static Strategy buildSmaStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator sma14 = new EMAIndicator(closePrice, 100);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        MACDIndicator macdLong = new MACDIndicator(closePrice, 50, 100);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);
        EMAIndicator emaMacdLong = new EMAIndicator(macd, 50);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);

        int maxIndex = series.getEndIndex();


        boolean macdChange = macd.getValue(maxIndex - 3).doubleValue() > macd.getValue(maxIndex - 2).doubleValue()
                && (macd.getValue(maxIndex - 2).doubleValue() > macd.getValue(maxIndex - 1).doubleValue())
                && (macd.getValue(maxIndex - 1).doubleValue() < macd.getValue(maxIndex).doubleValue());


        boolean macdTrend = (macdLong.getValue(maxIndex - 2).doubleValue() > macdLong.getValue(maxIndex).doubleValue());

        boolean emaTrend = (sma14.getValue(maxIndex).doubleValue() < sma14.getValue(maxIndex - 1).doubleValue());

        Decimal deltaK = Decimal.valueOf(-2);

        if (macdChange && macdTrend && emaTrend) {
            deltaK = Decimal.valueOf(102);
        }

        Rule entryRule = new OverIndicatorRule(macd, emaMacd)
                .and(new UnderIndicatorRule(rsi, deltaK))
                .and(new OverIndicatorRule(macdLong, emaMacdLong))
                //        .and(new OverIndicatorRule(rsi, Decimal.valueOf(50)))
                ;

        macdTrend = macd.getValue(maxIndex).doubleValue() > macd.getValue(maxIndex - 1).doubleValue()
                && macd.getValue(maxIndex - 1).doubleValue() > macd.getValue(maxIndex - 2).doubleValue();

        if (macdTrend || !emaTrend) {

            deltaK = Decimal.valueOf(-2);
        } else {
            deltaK = Decimal.valueOf(102);
        }

        Rule exitRule = new OverIndicatorRule(rsi, deltaK)
                .or(new OverIndicatorRule(macdLong, emaMacdLong));

        return new BaseStrategy(entryRule, exitRule);

    }
}





