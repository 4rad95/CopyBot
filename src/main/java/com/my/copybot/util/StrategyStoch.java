package com.my.copybot.util;

import com.my.copybot.Log;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

public class StrategyStoch {


    public static final String STRATEGY = "STOCH";


    public static Boolean openStochStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        SMAIndicator sma5 = new SMAIndicator(closePrice, 5);
        SMAIndicator sma20 = new SMAIndicator(closePrice, 20);

        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);

        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3);

        int maxIndex = series.getEndIndex();


        return (sma5.getValue(maxIndex - 1).multipliedBy(10000).intValue() > sma20.getValue(maxIndex - 1).multipliedBy(10000).intValue()
                && sma5.getValue(maxIndex).multipliedBy(10000).intValue() < sma20.getValue(maxIndex).multipliedBy(10000).intValue()
                && stochRsiD.getValue(maxIndex).multipliedBy(100).intValue() - smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() > 3
                && (stochRsiD.getValue(maxIndex - 1).doubleValue() > stochRsiD.getValue(maxIndex).doubleValue())
                && (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() < smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue())
                && (macd.getValue(maxIndex).doubleValue() < macd.getValue(maxIndex - 1).doubleValue()));

    }

    public static Boolean closeStochStrategyShort(TimeSeries series) {

        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        SMAIndicator sma5 = new SMAIndicator(closePrice, 5);
        SMAIndicator sma20 = new SMAIndicator(closePrice, 20);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);

        int maxIndex = series.getEndIndex();

        if (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() > smoothedStochRsi.getValue(maxIndex - 2).multipliedBy(100).intValue())
            Log.info(StrategyStoch.class, "Exit SHORT " + series.getName() + " : K[last] > K[last-2] : " + smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() + " > " + smoothedStochRsi.getValue(maxIndex - 2).multipliedBy(100).intValue());

        if (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() > stochRsiD.getValue(maxIndex).multipliedBy(100).intValue())
            Log.info(StrategyStoch.class, "Exit SHORT " + series.getName() + " : K > D : " + smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() + " > " + stochRsiD.getValue(maxIndex).multipliedBy(100).intValue());

        return sma5.getValue(maxIndex - 1).multipliedBy(10000).intValue() < sma20.getValue(maxIndex - 1).multipliedBy(10000).intValue()
                || (sma20.getValue(maxIndex).doubleValue() > sma20.getValue(maxIndex - 1).doubleValue())
                ;
    }

    public static Boolean openStochStrategyLong(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        SMAIndicator sma5 = new SMAIndicator(closePrice, 5);
        SMAIndicator sma20 = new SMAIndicator(closePrice, 20);

        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);

        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3);

        int maxIndex = series.getEndIndex();


        return (sma5.getValue(maxIndex - 1).multipliedBy(10000).intValue() < sma20.getValue(maxIndex - 1).multipliedBy(10000).intValue()
                && sma5.getValue(maxIndex).multipliedBy(10000).intValue() > sma20.getValue(maxIndex).multipliedBy(10000).intValue()
                && smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() - stochRsiD.getValue(maxIndex).multipliedBy(100).intValue() > 3
                && (stochRsiD.getValue(maxIndex - 1).doubleValue() < stochRsiD.getValue(maxIndex).doubleValue())
                && (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() > smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue())
                && (macd.getValue(maxIndex).doubleValue() > macd.getValue(maxIndex - 1).doubleValue()));

    }

    public static Boolean closeStochStrategyLong(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        SMAIndicator sma5 = new SMAIndicator(closePrice, 5);
        SMAIndicator sma20 = new SMAIndicator(closePrice, 20);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3); // 3-периодное SMA
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);

        int maxIndex = series.getEndIndex();

        if (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() < smoothedStochRsi.getValue(maxIndex - 2).multipliedBy(100).intValue())
            Log.info(StrategyStoch.class, "Exit LONG " + series.getName() + " K[last] < K[last-2] : " + smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() + " < " + smoothedStochRsi.getValue(maxIndex - 2).multipliedBy(100).intValue());

        if (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() < stochRsiD.getValue(maxIndex).multipliedBy(100).intValue())
            Log.info(StrategyStoch.class, "Exit LONG " + series.getName() + " K < D : " + smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() + " < " + stochRsiD.getValue(maxIndex).multipliedBy(100).intValue());

        return sma5.getValue(maxIndex - 1).multipliedBy(10000).intValue() > sma20.getValue(maxIndex - 1).multipliedBy(10000).intValue()
                || (sma20.getValue(maxIndex).doubleValue() < sma20.getValue(maxIndex - 1).doubleValue())
                ;
    }

}





