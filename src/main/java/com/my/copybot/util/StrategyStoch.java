package com.my.copybot.util;

import com.my.copybot.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.StochasticRSIIndicator;
import org.ta4j.core.indicators.helpers.*;

public class StrategyStoch {


    public static final String STRATEGY = "STOCH";
    private static final Logger log = LoggerFactory.getLogger(StrategyStoch.class);


    public static Boolean openStochStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        MaxPriceIndicator maxPrice = new MaxPriceIndicator(series);

//
//        SMAIndicator sma5 = new SMAIndicator(closePrice, 5);
//        SMAIndicator sma20 = new SMAIndicator(closePrice, 20);
//
//        // Полосы Боллинджера
//        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(sma20);
//        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, new StandardDeviationIndicator(closePrice, 20), Decimal.valueOf(2));
//        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, new StandardDeviationIndicator(closePrice, 20), Decimal.valueOf(2));
//
//        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
//        EMAIndicator emaMacd = new EMAIndicator(macd, 9);
//
//        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
//        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
//        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
//        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3);

        PlusDMIndicator plusDM = new PlusDMIndicator(series);
        MinusDMIndicator minusDM = new MinusDMIndicator(series);
        SMAIndicator smoothedPlusDM = new SMAIndicator(plusDM, 14);
        SMAIndicator smoothedMinusDM = new SMAIndicator(minusDM, 14);

        int maxIndex = series.getEndIndex();

        if ((smoothedPlusDM.getValue(maxIndex).doubleValue() < smoothedMinusDM.getValue(maxIndex).doubleValue())
                && (smoothedPlusDM.getValue(maxIndex - 1).doubleValue() > smoothedMinusDM.getValue(maxIndex - 1).doubleValue()
                && (calculateADX(series, 14).getValue(maxIndex).doubleValue() > 25))) {
            Log.info(StrategyStoch.class, series.getName() + " : ADX = " + calculateADX(series, 14).getValue(maxIndex) + "  D+ = " + smoothedPlusDM.getValue(maxIndex) + "   D-= " + smoothedMinusDM.getValue(maxIndex));
            return true;
        } else return false;

//        return (sma5.getValue(maxIndex - 1).multipliedBy(10000).intValue() > sma20.getValue(maxIndex - 1).multipliedBy(10000).intValue()
//                && sma5.getValue(maxIndex).multipliedBy(10000).intValue() < sma20.getValue(maxIndex).multipliedBy(10000).intValue()
//                && stochRsiD.getValue(maxIndex).multipliedBy(100).intValue() - smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() > 3
//                && (stochRsiD.getValue(maxIndex - 1).doubleValue() > stochRsiD.getValue(maxIndex).doubleValue())
//                && (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() < smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue())
//                && (macd.getValue(maxIndex).doubleValue() < macd.getValue(maxIndex - 1).doubleValue()));

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

        PlusDMIndicator plusDM = new PlusDMIndicator(series);
        MinusDMIndicator minusDM = new MinusDMIndicator(series);
        SMAIndicator smoothedPlusDM = new SMAIndicator(plusDM, 14);
        SMAIndicator smoothedMinusDM = new SMAIndicator(minusDM, 14);

        if (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() > smoothedStochRsi.getValue(maxIndex - 2).multipliedBy(100).intValue())
            Log.info(StrategyStoch.class, "Exit SHORT " + series.getName() + " : K[last] > K[last-2] : " + smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() + " > " + smoothedStochRsi.getValue(maxIndex - 2).multipliedBy(100).intValue());

        if (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() > stochRsiD.getValue(maxIndex).multipliedBy(100).intValue())
            Log.info(StrategyStoch.class, "Exit SHORT " + series.getName() + " : K > D : " + smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() + " > " + stochRsiD.getValue(maxIndex).multipliedBy(100).intValue());


        if (calculateADX(series, 14).getValue(maxIndex).doubleValue() < 25) {
            Log.info(StrategyStoch.class, "ADX = " + calculateADX(series, 14).getValue(maxIndex) + " < 25");
            return true;
        } else if (smoothedMinusDM.getValue(maxIndex).doubleValue() - smoothedPlusDM.getValue(maxIndex).doubleValue()
                < smoothedMinusDM.getValue(maxIndex - 1).doubleValue() - smoothedPlusDM.getValue(maxIndex - 1).doubleValue()
                && smoothedMinusDM.getValue(maxIndex - 1).doubleValue() - smoothedPlusDM.getValue(maxIndex - 1).doubleValue()
                < smoothedMinusDM.getValue(maxIndex - 2).doubleValue() - smoothedPlusDM.getValue(maxIndex - 2).doubleValue()) {
            Log.info(StrategyStoch.class, "Delta Lower. Position close.");
            return true;
        }
        return false;
    }

    public static Boolean openStochStrategyLong(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
//        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
//        MinPriceIndicator minPrice = new MinPriceIndicator(series);
//
//        SMAIndicator sma5 = new SMAIndicator(closePrice, 5);
//        SMAIndicator sma20 = new SMAIndicator(closePrice, 20);
//
//        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(sma20);
//        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, new StandardDeviationIndicator(closePrice, 20), Decimal.valueOf(2));
//        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, new StandardDeviationIndicator(closePrice, 20), Decimal.valueOf(2));
//
//
//        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
//        EMAIndicator emaMacd = new EMAIndicator(macd, 9);
//
//        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
//        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
//        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
//        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3);
//

        PlusDMIndicator plusDM = new PlusDMIndicator(series);
        MinusDMIndicator minusDM = new MinusDMIndicator(series);
        SMAIndicator smoothedPlusDM = new SMAIndicator(plusDM, 14);
        SMAIndicator smoothedMinusDM = new SMAIndicator(minusDM, 14);

        int maxIndex = series.getEndIndex();

        if (smoothedPlusDM.getValue(maxIndex).doubleValue() > smoothedMinusDM.getValue(maxIndex).doubleValue()
                && (smoothedPlusDM.getValue(maxIndex - 1).doubleValue() < smoothedMinusDM.getValue(maxIndex - 1).doubleValue()
                && (calculateADX(series, 14).getValue(maxIndex).doubleValue() > 25))) {
            Log.info(StrategyStoch.class, series.getName() + " : ADX = " + calculateADX(series, 14).getValue(maxIndex) + "  D+ = " + smoothedPlusDM.getValue(maxIndex) + "   D-= " + smoothedMinusDM.getValue(maxIndex));
            return true;
        } else return false;



//        return (sma5.getValue(maxIndex - 1).multipliedBy(10000).intValue() < sma20.getValue(maxIndex - 1).multipliedBy(10000).intValue()
//                && sma5.getValue(maxIndex).multipliedBy(10000).intValue() > sma20.getValue(maxIndex).multipliedBy(10000).intValue()
//                && smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() - stochRsiD.getValue(maxIndex).multipliedBy(100).intValue() > 3
//                && (stochRsiD.getValue(maxIndex - 1).doubleValue() < stochRsiD.getValue(maxIndex).doubleValue())
//                && (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() > smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue())
//                && (macd.getValue(maxIndex).doubleValue() > macd.getValue(maxIndex - 1).doubleValue()));

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


        PlusDMIndicator plusDM = new PlusDMIndicator(series);
        MinusDMIndicator minusDM = new MinusDMIndicator(series);
        SMAIndicator smoothedPlusDM = new SMAIndicator(plusDM, 14);
        SMAIndicator smoothedMinusDM = new SMAIndicator(minusDM, 14);


        if (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() < smoothedStochRsi.getValue(maxIndex - 2).multipliedBy(100).intValue())
            Log.info(StrategyStoch.class, "Exit LONG " + series.getName() + " K[last] < K[last-2] : " + smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() + " < " + smoothedStochRsi.getValue(maxIndex - 2).multipliedBy(100).intValue());

        if (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() < stochRsiD.getValue(maxIndex).multipliedBy(100).intValue())
            Log.info(StrategyStoch.class, "Exit LONG " + series.getName() + " K < D : " + smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() + " < " + stochRsiD.getValue(maxIndex).multipliedBy(100).intValue());

        if (calculateADX(series, 14).getValue(maxIndex).doubleValue() < 25) {
            Log.info(StrategyStoch.class, "ADX = " + calculateADX(series, 14).getValue(maxIndex) + " < 25");
            return true;
        } else if ((smoothedPlusDM.getValue(maxIndex).doubleValue() - smoothedMinusDM.getValue(maxIndex).doubleValue()
                < smoothedPlusDM.getValue(maxIndex - 1).doubleValue() - smoothedMinusDM.getValue(maxIndex - 1).doubleValue())
                && (smoothedPlusDM.getValue(maxIndex - 1).doubleValue() - smoothedMinusDM.getValue(maxIndex - 1).doubleValue()
                < smoothedPlusDM.getValue(maxIndex - 2).doubleValue() - smoothedMinusDM.getValue(maxIndex - 2).doubleValue())) {
            Log.info(StrategyStoch.class, "Delta Lower. Position close.");
            return true;
        }
        return false;
    }

    public static Indicator<Decimal> calculateADX(TimeSeries series, int period) {
        if (series.getBarCount() < period) {
            throw new IllegalArgumentException("Not enough data to calculate ADX");
        }

        // Создание индикаторов
        PlusDMIndicator plusDM = new PlusDMIndicator(series);
        MinusDMIndicator minusDM = new MinusDMIndicator(series);
        TRIndicator trueRange = new TRIndicator(series);

        SMAIndicator smoothedPlusDM = new SMAIndicator(plusDM, period);
        SMAIndicator smoothedMinusDM = new SMAIndicator(minusDM, period);
        SMAIndicator smoothedTrueRange = new SMAIndicator(trueRange, period);

        return new Indicator<Decimal>() {
            @Override
            public Decimal getValue(int index) {
                if (index < period - 1) {
                    return Decimal.valueOf(0.0); // Не хватает данных
                }
                // Рассчитываем +DI и -DI
                Decimal smoothedPlusDMValue = smoothedPlusDM.getValue(index);
                Decimal smoothedMinusDMValue = smoothedMinusDM.getValue(index);
                Decimal smoothedTrueRangeValue = smoothedTrueRange.getValue(index);

                double plusDI = (smoothedPlusDMValue.doubleValue() / smoothedTrueRangeValue.doubleValue()) * 100;
                double minusDI = (smoothedMinusDMValue.doubleValue() / smoothedTrueRangeValue.doubleValue()) * 100;

                // Рассчитываем DX
                double dx = Math.abs(plusDI - minusDI) / (plusDI + minusDI) * 100;

                // Сглаживаем DX для получения ADX
                double adx = 0.0;
                for (int i = index - period + 1; i <= index; i++) {
                    adx += dx;
                }
                adx /= period;

                return Decimal.valueOf(adx);
            }

            @Override
            public TimeSeries getTimeSeries() {
                return series;
            }
        };
    }
}





