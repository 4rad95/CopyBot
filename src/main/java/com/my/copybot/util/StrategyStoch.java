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
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.helpers.*;

public class StrategyStoch {


    public static final String STRATEGY = "STOCH";
    private static final Logger log = LoggerFactory.getLogger(StrategyStoch.class);


    public static Boolean openStochStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        OpenPriceIndicator openPrice = new OpenPriceIndicator(series);
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
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3);

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


        if (curr[0] < curr[1]
                && prev[0] > prev[1]
                && prev[1] < curr[1]
                && (smoothedStochRsi.getValue(maxIndex).doubleValue() < stochRsiD.getValue(maxIndex).doubleValue())
                && (smoothedStochRsi.getValue(maxIndex).doubleValue() > 0.30)
                && (dxIndicator.getValue(maxIndex - 1).doubleValue() < dxIndicator.getValue(maxIndex).doubleValue())
                && (dxIndicator.getValue(maxIndex).doubleValue() < 30)
        ) {
            //       && (calculateADX(series, 14).getValue(maxIndex).doubleValue() > 25)) {
            //         && (maxPrice.getValue(maxIndex).doubleValue() > bbm.getValue(maxIndex).doubleValue())
            Log.info(StrategyStoch.class, "[SHORT]:" + series.getName() + " : ADX = " + calculateADX(series, 14).getValue(maxIndex) + "  D+ = " + curr[0] + "   D-= " + curr[1] + "   DX = " + dxIndicator.getValue(maxIndex));
                    return true;
        } else if (
                openPrice.getValue(maxIndex - 2).doubleValue() < closePrice.getValue(maxIndex - 2).doubleValue()
                        && openPrice.getValue(maxIndex - 3).doubleValue() < closePrice.getValue(maxIndex - 3).doubleValue()
                        && openPrice.getValue(maxIndex - 4).doubleValue() < closePrice.getValue(maxIndex - 4).doubleValue()
                        && openPrice.getValue(maxIndex - 1).doubleValue() == closePrice.getValue(maxIndex - 2).doubleValue()
                        && openPrice.getValue(maxIndex - 1).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue()
                        && openPrice.getValue(maxIndex - 2).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue()
                        && prev[0] > prev[1]
        ) {
            Log.info(StrategyStoch.class, "[SHORT]:" + series.getName() + " Bearish Engulfing 1 candle");
            return true;
//        } else if (
//                openPrice.getValue(maxIndex - 3).doubleValue() < closePrice.getValue(maxIndex - 3).doubleValue()
//                        && openPrice.getValue(maxIndex - 2).doubleValue() == closePrice.getValue(maxIndex - 3).doubleValue()
//                        && openPrice.getValue(maxIndex - 2).doubleValue() > closePrice.getValue(maxIndex - 2).doubleValue()
//                        && openPrice.getValue(maxIndex - 1).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue()
//                        && openPrice.getValue(maxIndex - 3).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue()
//                        && prev[0] > prev[1]
//        ) {
//            Log.info(StrategyStoch.class, "[SHORT]:" + series.getName() + " Bearish Engulfing 2 candle");
//            return true;
        }
        return false;
    }

    public static String closeStochStrategyShort(TimeSeries series) {

        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        OpenPriceIndicator openPrice = new OpenPriceIndicator(series);
        SMAIndicator sma7 = new SMAIndicator(closePrice, 7);
        SMAIndicator sma25 = new SMAIndicator(closePrice, 25);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3);
//        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);

        int maxIndex = series.getEndIndex();
        MinPriceIndicator minPrice = new MinPriceIndicator(series);
        MaxPriceIndicator maxPrice = new MaxPriceIndicator(series);

//        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(sma20);
//        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, new StandardDeviationIndicator(closePrice, 20), Decimal.valueOf(2));
//        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, new StandardDeviationIndicator(closePrice, 20), Decimal.valueOf(2));
        PlusDMIndicator plusDM = new PlusDMIndicator(series);
        MinusDMIndicator minusDM = new MinusDMIndicator(series);
        SMAIndicator smoothedPlusDM = new SMAIndicator(plusDM, 14);
        SMAIndicator smoothedMinusDM = new SMAIndicator(minusDM, 14);
        DXIndicator dxIndicator = new DXIndicator(series, 14);

        ATRIndicator atr = new ATRIndicator(series, 14);
        double[] prev = {calculatePlusDI(smoothedPlusDM.getValue(maxIndex - 1).doubleValue(), atr.getValue(maxIndex - 1).doubleValue()), calculateMinusDI(smoothedMinusDM.getValue(maxIndex - 1).doubleValue(), atr.getValue(maxIndex - 1).doubleValue())};
        double[] curr = {calculatePlusDI(smoothedPlusDM.getValue(maxIndex).doubleValue(), atr.getValue(maxIndex).doubleValue()), calculateMinusDI(smoothedMinusDM.getValue(maxIndex).doubleValue(), atr.getValue(maxIndex).doubleValue())};


        if (sma7.getValue(maxIndex).doubleValue() > sma25.getValue(maxIndex).doubleValue()
                && sma7.getValue(maxIndex - 1).doubleValue() < sma25.getValue(maxIndex - 1).doubleValue()) {
            Log.info(StrategyStoch.class, "[SHORT]:" + series.getName() + " [SMA7 & SMA25 ] SMA7 > SMA25  . Position close.");

            return "[SMA7 & SMA25 ] SMA7 > SMA25  . Position close.";
//        } else if (dxIndicator.getValue(maxIndex).toDouble() < dxIndicator.getValue(maxIndex - 1).toDouble()
//                && (dxIndicator.getValue(maxIndex).doubleValue() > 40)) {
//            Log.info(StrategyStoch.class, "DX Low.");
//            return "DX Low.";
//        } else if (bbu.getValue(maxIndex).doubleValue() < closePrice.getValue(maxIndex).doubleValue()) {
//            Log.info(StrategyStoch.class, "ClosePrice high BB. Position close.");
//            return true;
        } else if (
                openPrice.getValue(maxIndex - 2).doubleValue() > closePrice.getValue(maxIndex - 2).doubleValue()
                        && openPrice.getValue(maxIndex - 1).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()
                        && openPrice.getValue(maxIndex - 2).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()
        ) {
            Log.info(StrategyStoch.class, "[SHORT]:" + series.getName() + " Bullish Engulfing  . Position close.");
            return " Bullish Engulfing 1 candle";
        } else if (
                openPrice.getValue(maxIndex - 3).doubleValue() > closePrice.getValue(maxIndex - 3).doubleValue()
                        && openPrice.getValue(maxIndex - 2).doubleValue() == closePrice.getValue(maxIndex - 3).doubleValue()
                        && openPrice.getValue(maxIndex - 2).doubleValue() < closePrice.getValue(maxIndex - 2).doubleValue()
                        && openPrice.getValue(maxIndex - 1).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()
                        && openPrice.getValue(maxIndex - 3).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()
        ) {
            Log.info(StrategyStoch.class, "[SHORT]:" + series.getName() + " Bullish Engulfing 2 candle");
            return " Bullish Engulfing 2 candle";
        }
        return null;
    }

    public static Boolean openStochStrategyLong(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        OpenPriceIndicator openPrice = new OpenPriceIndicator(series);

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
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
        SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
        SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3);


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
                && (smoothedStochRsi.getValue(maxIndex).doubleValue() > stochRsiD.getValue(maxIndex).doubleValue())
                && (smoothedStochRsi.getValue(maxIndex).doubleValue() < 0.70)
                && (dxIndicator.getValue(maxIndex - 1).doubleValue() < dxIndicator.getValue(maxIndex).doubleValue())
                && (dxIndicator.getValue(maxIndex).doubleValue() < 20)) {
            //            && (calculateADX(series, 14).getValue(maxIndex).doubleValue() > 25)) {
            //           && (minPrice.getValue(maxIndex).doubleValue() < bbm.getValue(maxIndex).doubleValue()))
            //
            Log.info(StrategyStoch.class, "[LONG]:" + series.getName() + " : ADX = " + calculateADX(series, 14).getValue(maxIndex) + "  D+ = " + curr[0] + "   D-= " + curr[1] + "   DX = " + dxIndicator.getValue(maxIndex));
                    return true;
        } else if (
                openPrice.getValue(maxIndex - 2).doubleValue() > closePrice.getValue(maxIndex - 2).doubleValue()
                        && openPrice.getValue(maxIndex - 3).doubleValue() > closePrice.getValue(maxIndex - 3).doubleValue()
                        && openPrice.getValue(maxIndex - 4).doubleValue() > closePrice.getValue(maxIndex - 4).doubleValue()
                        && openPrice.getValue(maxIndex - 1).doubleValue() == closePrice.getValue(maxIndex - 2).doubleValue()
                        && openPrice.getValue(maxIndex - 1).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()
                        && openPrice.getValue(maxIndex - 2).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()
                        && prev[0] < prev[1]
        ) {
            Log.info(StrategyStoch.class, "[LONG]:" + series.getName() + " Bullish engulfing 1 candle");
            return true;
//        } else if (
//                openPrice.getValue(maxIndex - 3).doubleValue() > closePrice.getValue(maxIndex - 3).doubleValue()
//                        && openPrice.getValue(maxIndex - 2).doubleValue() == closePrice.getValue(maxIndex - 3).doubleValue()
//                        && openPrice.getValue(maxIndex - 2).doubleValue() < closePrice.getValue(maxIndex - 2).doubleValue()
//                        && openPrice.getValue(maxIndex - 1).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()
//                        && openPrice.getValue(maxIndex - 3).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()
//                        && prev[0] > prev[1]
//        ) {
//            Log.info(StrategyStoch.class, "[LONG]:" + series.getName() + " Bullish Engulfing 2 candle");
//            return true;
        }
        return false;



//        return (sma5.getValue(maxIndex - 1).multipliedBy(10000).intValue() < sma20.getValue(maxIndex - 1).multipliedBy(10000).intValue()
//                && sma5.getValue(maxIndex).multipliedBy(10000).intValue() > sma20.getValue(maxIndex).multipliedBy(10000).intValue()
//                && smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() - stochRsiD.getValue(maxIndex).multipliedBy(100).intValue() > 3
//                && (stochRsiD.getValue(maxIndex - 1).doubleValue() < stochRsiD.getValue(maxIndex).doubleValue())
//                && (smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() > smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue())
//                && (macd.getValue(maxIndex).doubleValue() > macd.getValue(maxIndex - 1).doubleValue()));

    }

    public static String closeStochStrategyLong(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        OpenPriceIndicator openPrice = new OpenPriceIndicator(series);
        SMAIndicator sma7 = new SMAIndicator(closePrice, 7);
        SMAIndicator sma25 = new SMAIndicator(closePrice, 25);

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

//        BollingerBandsMiddleIndicator bbm = new BollingerBandsMiddleIndicator(sma20);
//        BollingerBandsUpperIndicator bbu = new BollingerBandsUpperIndicator(bbm, new StandardDeviationIndicator(closePrice, 20), Decimal.valueOf(2));
//        BollingerBandsLowerIndicator bbl = new BollingerBandsLowerIndicator(bbm, new StandardDeviationIndicator(closePrice, 20), Decimal.valueOf(2));

        ATRIndicator atr = new ATRIndicator(series, 14);
        double[] prev = {calculatePlusDI(smoothedPlusDM.getValue(maxIndex - 1).doubleValue(), atr.getValue(maxIndex - 1).doubleValue()), calculateMinusDI(smoothedMinusDM.getValue(maxIndex - 1).doubleValue(), atr.getValue(maxIndex - 1).doubleValue())};
        double[] curr = {calculatePlusDI(smoothedPlusDM.getValue(maxIndex).doubleValue(), atr.getValue(maxIndex).doubleValue()), calculateMinusDI(smoothedMinusDM.getValue(maxIndex).doubleValue(), atr.getValue(maxIndex).doubleValue())};

//        if (curr[0] < curr[1] && prev[0] < prev[1] && curr[0] < prev[0]) {
//            Log.info(StrategyStoch.class, "DI Down. Change Trend");
//            return "DI Down. Change Trend";
//        } else
        if (sma7.getValue(maxIndex).doubleValue() < sma25.getValue(maxIndex).doubleValue()
                && sma7.getValue(maxIndex - 1).doubleValue() > sma25.getValue(maxIndex - 1).doubleValue()) {
            Log.info(StrategyStoch.class, "[LONG]:" + series.getName() + " [SMA7 & SMA25 ] SMA7 < SMA25  . Position close.");
            return "[SMA7 & SMA25 ] SMA7 < SMA25  . Position close.";
//        } else if (dxIndicator.getValue(maxIndex).toDouble() < dxIndicator.getValue(maxIndex - 1).toDouble()
//                && (dxIndicator.getValue(maxIndex).doubleValue() > 40)) {
//            Log.info(StrategyStoch.class, "DX Lower. Position close.");
//            return "DX Lower. Position close.";
//        } else if (bbl.getValue(maxIndex).doubleValue() > closePrice.getValue(maxIndex).doubleValue()) {
//            Log.info(StrategyStoch.class, "ClosePrice low BB .");
//            return true;
        } else if (
                openPrice.getValue(maxIndex - 2).doubleValue() < closePrice.getValue(maxIndex - 2).doubleValue()
                        && openPrice.getValue(maxIndex - 1).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue()
                        && openPrice.getValue(maxIndex - 2).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue()
            //           && prev[0] < prev[1]
        ) {
            Log.info(StrategyStoch.class, "[LONG]:" + series.getName() + " Bearish Engulfing  . Position close.");
            return "Bearish Engulfing 1 candle";
        } else if (
                openPrice.getValue(maxIndex - 3).doubleValue() < closePrice.getValue(maxIndex - 3).doubleValue()
                        && openPrice.getValue(maxIndex - 2).doubleValue() == closePrice.getValue(maxIndex - 3).doubleValue()
                        && openPrice.getValue(maxIndex - 2).doubleValue() > closePrice.getValue(maxIndex - 2).doubleValue()
                        && openPrice.getValue(maxIndex - 1).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue()
                        && openPrice.getValue(maxIndex - 3).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue()
        ) {
            Log.info(StrategyStoch.class, "[LONG]:" + series.getName() + " Bearish Engulfing 2 candle. Position close.");
            return "Bearish Engulfing 2 candle";
        }
        return null;
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





