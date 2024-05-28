package com.my.copybot.util;

import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
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
        EMAIndicator sma50 = new EMAIndicator(closePrice, 50);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        MACDIndicator macdLong = new MACDIndicator(closePrice, 19, 39);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);
        EMAIndicator emaMacdLong = new EMAIndicator(macd, 25);

        EMAIndicator ema22 = new EMAIndicator(closePrice, 22);
        EMAIndicator ema10 = new EMAIndicator(closePrice, 10);

        RSIIndicator rsi = new RSIIndicator(closePrice, 14);


        int maxIndex = series.getEndIndex();


        boolean macdChange = (macd.getValue(maxIndex - 3).doubleValue() > macd.getValue(maxIndex - 2).doubleValue())
                && (macd.getValue(maxIndex - 2).doubleValue() > macd.getValue(maxIndex - 1).doubleValue())
                && (macd.getValue(maxIndex - 1).doubleValue() < macd.getValue(maxIndex).doubleValue());


        boolean macdTrend = (macdLong.getValue(maxIndex - 1).doubleValue() < macdLong.getValue(maxIndex).doubleValue());

        boolean emaTrend = (sma14.getValue(maxIndex).doubleValue() > sma14.getValue((maxIndex - 1)).doubleValue());

        Decimal deltaK = Decimal.valueOf(-2);

        if (macdChange && macdTrend && emaTrend) {
            deltaK = Decimal.valueOf(102);
        }

        Rule entryRule = ((new UnderIndicatorRule(macd, emaMacd))
                .and(new UnderIndicatorRule(rsi, deltaK))
                .and(new OverIndicatorRule(macdLong, emaMacdLong))
                .and(new UnderIndicatorRule(closePrice, ema22)))
                .or((new CrossedUpIndicatorRule(macdLong, emaMacdLong))
                        .and(new OverIndicatorRule(macd, emaMacd))
                );


        macdChange = emaMacd.getValue(maxIndex).doubleValue() < emaMacd.getValue(maxIndex - 1).doubleValue();
        emaTrend = (ema22.getValue(maxIndex).doubleValue() < ema22.getValue((maxIndex - 1)).doubleValue());

        if (macdChange && !emaTrend) {
            deltaK = Decimal.valueOf(-2);
        } else {
            deltaK = Decimal.valueOf(102);
        }

        Rule exitRule = (new OverIndicatorRule(rsi, deltaK))
                .or(new UnderIndicatorRule(macd, emaMacd));

        return new BaseStrategy(entryRule, exitRule);
    }


    public static Strategy buildSmaStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator sma14 = new EMAIndicator(closePrice, 100);
        //     EMAIndicator sma50 = new EMAIndicator(closePrice, 50);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        MACDIndicator macdLong = new MACDIndicator(closePrice, 19, 39);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);
        EMAIndicator emaMacdLong = new EMAIndicator(macd, 25);

        EMAIndicator ema22 = new EMAIndicator(closePrice, 22);
        EMAIndicator ema10 = new EMAIndicator(closePrice, 10);

        RSIIndicator rsi = new RSIIndicator(closePrice, 14);

//        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, 14);
//        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
//
//        RSIIndicator r = new RSIIndicator(closePrice, 14);
//        StochasticRSIIndicator rsi1 = new StochasticRSIIndicator(closePrice, 14);
//
//        StochasticOscillatorKIndicator stochK1= new StochasticOscillatorKIndicator(rsi1,3,new MaxPriceIndicator(series), new MinPriceIndicator(series));
//        StochasticOscillatorDIndicator stochD1 = new StochasticOscillatorDIndicator(stochK); // time frame always 3, maybe we should constructor parameter for this
//


        int maxIndex = series.getEndIndex();
        //      System.out.println(series.getName() + "  K="+stochK1.getValue(maxIndex)+"   D="+stochD1.getValue(maxIndex) );

        boolean macdChange = macd.getValue(maxIndex - 3).doubleValue() > macd.getValue(maxIndex - 2).doubleValue()
                && (macd.getValue(maxIndex - 2).doubleValue() > macd.getValue(maxIndex - 1).doubleValue())
                && (macd.getValue(maxIndex - 1).doubleValue() < macd.getValue(maxIndex).doubleValue());


        boolean macdTrend = (macdLong.getValue(maxIndex - 1).doubleValue() > macdLong.getValue(maxIndex).doubleValue());

        boolean emaTrend = (sma14.getValue(maxIndex).doubleValue() < sma14.getValue(maxIndex - 1).doubleValue());

        Decimal deltaK = Decimal.valueOf(-2);

        if (macdChange && macdTrend && emaTrend) {
            deltaK = Decimal.valueOf(102);
        }

        Rule entryRule = (new OverIndicatorRule(macd, emaMacd)
                .and(new UnderIndicatorRule(rsi, deltaK))
                .and(new UnderIndicatorRule(macdLong, emaMacdLong))
                .and(new OverIndicatorRule(closePrice, ema22)))

                .or((new CrossedDownIndicatorRule(macdLong, emaMacdLong))
                                .and(new UnderIndicatorRule(macd, emaMacd))
                );

        macdChange = emaMacd.getValue(maxIndex).doubleValue() > emaMacd.getValue(maxIndex - 3).doubleValue();
        emaTrend = (ema22.getValue(maxIndex).doubleValue() > ema22.getValue((maxIndex - 2)).doubleValue());


        if (!emaTrend && macdChange) {

            deltaK = Decimal.valueOf(-2);
        } else {
            deltaK = Decimal.valueOf(102);
        }

        Rule exitRule = (new OverIndicatorRule(rsi, deltaK))
                .or(new OverIndicatorRule(macd, emaMacd));

        return new BaseStrategy(entryRule, exitRule);

    }
}





