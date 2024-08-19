package com.my.copybot.util;

import com.my.copybot.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ta4j.core.Decimal;
import org.ta4j.core.Indicator;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.StochasticRSIIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

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
        DXIndicator dxIndicator = new DXIndicator(series, 14);
        int maxIndex = series.getEndIndex();
        SMAIndicator sma20 = new SMAIndicator(closePrice, 20);
        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(sma20);

        ATRIndicator atr = new ATRIndicator(series, 14);
        double[] prev = {calculatePlusDI(smoothedPlusDM.getValue(maxIndex - 1).doubleValue(), atr.getValue(maxIndex - 1).doubleValue()), calculateMinusDI(smoothedMinusDM.getValue(maxIndex - 1).doubleValue(), atr.getValue(maxIndex - 1).doubleValue())};
        double[] curr = {calculatePlusDI(smoothedPlusDM.getValue(maxIndex).doubleValue(), atr.getValue(maxIndex).doubleValue()), calculateMinusDI(smoothedMinusDM.getValue(maxIndex).doubleValue(), atr.getValue(maxIndex).doubleValue())};


        if ((curr[0] < curr[1])
                && (prev[0] > prev[1]
                && (prev[1] < curr[1])
                && (dxIndicator.getValue(maxIndex).doubleValue() < 10)
                && (calculateADX(series, 14).getValue(maxIndex).doubleValue() > 25))
            //         && (maxPrice.getValue(maxIndex).doubleValue() > bbm.getValue(maxIndex).doubleValue())
        ) {
            Log.info(StrategyStoch.class, series.getName() + " : ADX = " + calculateADX(series, 14).getValue(maxIndex) + "  D+ = " + curr[0] + "   D-= " + curr[1]);
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

        SMAIndicator sma20 = new SMAIndicator(closePrice, 20);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3);
//        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);

        int maxIndex = series.getEndIndex();
        MinPriceIndicator minPrice = new MinPriceIndicator(series);
        MaxPriceIndicator maxPrice = new MaxPriceIndicator(series);

        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(sma20);
        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, new StandardDeviationIndicator(closePrice, 20), Decimal.valueOf(2));
        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, new StandardDeviationIndicator(closePrice, 20), Decimal.valueOf(2));
        PlusDMIndicator plusDM = new PlusDMIndicator(series);
        MinusDMIndicator minusDM = new MinusDMIndicator(series);
        SMAIndicator smoothedPlusDM = new SMAIndicator(plusDM, 14);
        SMAIndicator smoothedMinusDM = new SMAIndicator(minusDM, 14);
        DXIndicator dxIndicator = new DXIndicator(series, 14);

        ATRIndicator atr = new ATRIndicator(series, 14);
        double[] prev = {calculatePlusDI(smoothedPlusDM.getValue(maxIndex - 1).doubleValue(), atr.getValue(maxIndex - 1).doubleValue()), calculateMinusDI(smoothedMinusDM.getValue(maxIndex - 1).doubleValue(), atr.getValue(maxIndex - 1).doubleValue())};
        double[] curr = {calculatePlusDI(smoothedPlusDM.getValue(maxIndex).doubleValue(), atr.getValue(maxIndex).doubleValue()), calculateMinusDI(smoothedMinusDM.getValue(maxIndex).doubleValue(), atr.getValue(maxIndex).doubleValue())};


        if (curr[0] > curr[1] && prev[0] > prev[1]) {
            Log.info(StrategyStoch.class, "DI UP. Change Trend");
            return true;
        } else if (smoothedStochRsi.getValue(maxIndex).doubleValue() > stochRsiD.getValue(maxIndex).doubleValue()) {
            Log.info(StrategyStoch.class, "[StochRsi] K > D  . Position close.");
            return true;
        } else if (dxIndicator.getValue(maxIndex).toDouble() < dxIndicator.getValue(maxIndex - 1).toDouble()
                && (dxIndicator.getValue(maxIndex).doubleValue() > 40)) {
            Log.info(StrategyStoch.class, "DX Low.");
            return true;
        } else if (bbu.getValue(maxIndex).doubleValue() < closePrice.getValue(maxIndex).doubleValue()) {
            Log.info(StrategyStoch.class, "ClosePrice high BB. Position close.");
            return true;
        }

        return false;
    }

    public static Boolean openStochStrategyLong(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        MinPriceIndicator minPrice = new MinPriceIndicator(series);
//
//        SMAIndicator sma5 = new SMAIndicator(closePrice, 5);
//        SMAIndicator sma20 = new SMAIndicator(closePrice, 20);
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
        DXIndicator dxIndicator = new DXIndicator(series, 14);
        SMAIndicator sma20 = new SMAIndicator(closePrice, 20);
        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(sma20);

        ATRIndicator atr = new ATRIndicator(series, 14);

        int maxIndex = series.getEndIndex();
        double[] prev = {calculatePlusDI(smoothedPlusDM.getValue(maxIndex - 1).doubleValue(), atr.getValue(maxIndex - 1).doubleValue()), calculateMinusDI(smoothedMinusDM.getValue(maxIndex - 1).doubleValue(), atr.getValue(maxIndex - 1).doubleValue())};
        double[] curr = {calculatePlusDI(smoothedPlusDM.getValue(maxIndex).doubleValue(), atr.getValue(maxIndex).doubleValue()), calculateMinusDI(smoothedMinusDM.getValue(maxIndex).doubleValue(), atr.getValue(maxIndex).doubleValue())};
// nn [plus, minus]
        if (curr[0] > curr[1]
                && (prev[0] < prev[1])
                && (prev[0] < curr[0])
                && (dxIndicator.getValue(maxIndex).doubleValue() < 10)
            //           && (calculateADX(series, 14).getValue(maxIndex).doubleValue() > 25))
            //           && (minPrice.getValue(maxIndex).doubleValue() < bbm.getValue(maxIndex).doubleValue()))
            //
        ) {
            Log.info(StrategyStoch.class, series.getName() + " : ADX = " + calculateADX(series, 14).getValue(maxIndex) + "  D+ = " + curr[0] + "   D-= " + curr[1]);
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
//        SMAIndicator sma5 = new SMAIndicator(closePrice, 5);
        SMAIndicator sma20 = new SMAIndicator(closePrice, 20);

        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3); // 3-периодное SMA
//        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);

        int maxIndex = series.getEndIndex();
        MaxPriceIndicator maxPrice = new MaxPriceIndicator(series);
        PlusDMIndicator plusDM = new PlusDMIndicator(series);
        MinusDMIndicator minusDM = new MinusDMIndicator(series);
        SMAIndicator smoothedPlusDM = new SMAIndicator(plusDM, 14);
        SMAIndicator smoothedMinusDM = new SMAIndicator(minusDM, 14);
        DXIndicator dxIndicator = new DXIndicator(series, 14);

        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(sma20);
        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, new StandardDeviationIndicator(closePrice, 20), Decimal.valueOf(2));
        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, new StandardDeviationIndicator(closePrice, 20), Decimal.valueOf(2));

        ATRIndicator atr = new ATRIndicator(series, 14);
        double[] prev = {calculatePlusDI(smoothedPlusDM.getValue(maxIndex - 1).doubleValue(), atr.getValue(maxIndex - 1).doubleValue()), calculateMinusDI(smoothedMinusDM.getValue(maxIndex - 1).doubleValue(), atr.getValue(maxIndex - 1).doubleValue())};
        double[] curr = {calculatePlusDI(smoothedPlusDM.getValue(maxIndex).doubleValue(), atr.getValue(maxIndex).doubleValue()), calculateMinusDI(smoothedMinusDM.getValue(maxIndex).doubleValue(), atr.getValue(maxIndex).doubleValue())};

        if (curr[0] < curr[1] && prev[0] < prev[1]) {
            Log.info(StrategyStoch.class, "DI Down. Change Trend");
            return true;
        } else if (smoothedStochRsi.getValue(maxIndex).doubleValue() < stochRsiD.getValue(maxIndex).doubleValue()) {
            Log.info(StrategyStoch.class, "[StochRsi ] K < D  . Position close.");
            return true;
        } else if (dxIndicator.getValue(maxIndex).toDouble() < dxIndicator.getValue(maxIndex - 1).toDouble()
                && (dxIndicator.getValue(maxIndex).doubleValue() > 40)) {
            Log.info(StrategyStoch.class, "DX Lower. Position close.");
            return true;
        } else if (bbl.getValue(maxIndex).doubleValue() > closePrice.getValue(maxIndex).doubleValue()) {
            Log.info(StrategyStoch.class, "ClosePrice low BB .");
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

    public static double calculatePlusDI(double plusDM, double trueRange) {
        return (plusDM / trueRange) * 100;
    }

    public static double calculateMinusDI(double minusDM, double trueRange) {
        return (minusDM / trueRange) * 100;
    }
}





