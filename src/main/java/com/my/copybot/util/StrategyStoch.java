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


    public static String openStochStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        OpenPriceIndicator openPrice = new OpenPriceIndicator(series);
        MaxPriceIndicator maxPrice = new MaxPriceIndicator(series);
        MinPriceIndicator minPrice = new MinPriceIndicator(series);

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
        boolean checkLevel = true; //(checkLevelBreakout(series, maxIndex, 14) < 0);

            if (
                openPrice.getValue(maxIndex - 2).doubleValue() < closePrice.getValue(maxIndex - 2).doubleValue()
                        && openPrice.getValue(maxIndex - 3).doubleValue() < closePrice.getValue(maxIndex - 3).doubleValue()
                        && openPrice.getValue(maxIndex - 1).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue()
                        && openPrice.getValue(maxIndex - 2).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue()
                        && maxPrice.getValue(maxIndex-2).doubleValue() < maxPrice.getValue(maxIndex-1).doubleValue()
                        && (rsi.getValue(maxIndex-1).doubleValue() > 65 || rsi.getValue(maxIndex-2).doubleValue() > 65  )
                        && prev[1] < curr[1]
                        && checkLevel
        ) {
            //    Log.info(StrategyStoch.class,
                System.out.print("[SHORT]:" + series.getName() + " Bearish Engulfing 1 candle | ");
                return "[SHORT]:" + series.getName() + " Bearish Engulfing 1 candle | ";
            } else  if (
                    openPrice.getValue(maxIndex - 3).doubleValue() < closePrice.getValue(maxIndex - 3).doubleValue()
                            && openPrice.getValue(maxIndex - 4).doubleValue() < closePrice.getValue(maxIndex - 4).doubleValue()
                            && openPrice.getValue(maxIndex - 2).doubleValue() > closePrice.getValue(maxIndex - 2).doubleValue()
                            && openPrice.getValue(maxIndex - 3).doubleValue() > closePrice.getValue(maxIndex - 2).doubleValue()
                            && maxPrice.getValue(maxIndex-3).doubleValue() < maxPrice.getValue(maxIndex-2).doubleValue()
                            && (rsi.getValue(maxIndex-3).doubleValue() > 65 || rsi.getValue(maxIndex-2).doubleValue() > 65  )
                            && prev[1] < curr[1]
                            && checkLevel
            ) {
                //    Log.info(StrategyStoch.class,
                System.out.print("[SHORT]:" + series.getName() + " Bearish Engulfing 1 candle  + 1 candele | ");
                return "[SHORT]:" + series.getName() + " Bearish Engulfing 1 candle  + 1 candele | ";
            } else if (
                    openPrice.getValue(maxIndex - 1).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue()
                            && openPrice.getValue(maxIndex - 2).doubleValue() > closePrice.getValue(maxIndex - 2).doubleValue()
                            && openPrice.getValue(maxIndex - 3).doubleValue() > closePrice.getValue(maxIndex - 3).doubleValue()
                            && openPrice.getValue(maxIndex - 4).doubleValue() < closePrice.getValue(maxIndex - 4).doubleValue()
                            && (rsi.getValue(maxIndex-1).doubleValue() > 65 || rsi.getValue(maxIndex-2).doubleValue() > 65  )
                            && prev[1] < curr[1]
                            && checkLevel

            ) {
               // Log.info(StrategyStoch.class,
                System.out.print("[SHORT]:" + series.getName() + " Three Black Crows | ");
                return "[SHORT]:" + series.getName() + " Three Black Crows | ";
            } else if ( openPrice.getValue(maxIndex-3).doubleValue() < closePrice.getValue(maxIndex-3).doubleValue()
                    && openPrice.getValue(maxIndex-2).doubleValue() < closePrice.getValue(maxIndex-2).doubleValue()
                    && openPrice.getValue(maxIndex-1).doubleValue() > closePrice.getValue(maxIndex-1).doubleValue()
                    && ((maxPrice.getValue(maxIndex-1).doubleValue()-minPrice.getValue(maxIndex-1).doubleValue())/Math.abs(openPrice.getValue(maxIndex-1).doubleValue()-closePrice.getValue(maxIndex-1).doubleValue()) > 5)
                    && (rsi.getValue(maxIndex-1).doubleValue() > 65 || rsi.getValue(maxIndex-2).doubleValue() > 65  )
                    && checkLevel
                )
            {
                //Log.info(StrategyStoch.class,
                System.out.print("[SHORT]:" + series.getName() + " Inverted hammer  | ");
                return "[SHORT]:" + series.getName() + " Inverted hammer  | ";

        } else if (openPrice.getValue(maxIndex - 3).doubleValue() < closePrice.getValue(maxIndex - 3).doubleValue() // Бычья свеча
                && openPrice.getValue(maxIndex - 1).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue() // Медвежья свеча
                && (maxPrice.getValue(maxIndex - 2).doubleValue() - openPrice.getValue(maxIndex - 2).doubleValue()) > 4 * Math.abs(openPrice.getValue(maxIndex - 2).doubleValue() - closePrice.getValue(maxIndex - 2).doubleValue())
                && openPrice.getValue(maxIndex - 3).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue()
                && (rsi.getValue(maxIndex-1).doubleValue() > 65 || rsi.getValue(maxIndex-2).doubleValue() > 65  )
                && prev[1] < curr[1]
                && checkLevel
            ) {
            //Log.info(StrategyStoch.class,
                System.out.print("[SHORT]:" + series.getName() + " Evening Star  | ");
            return "[SHORT]:" + series.getName() + " Evening Star  | ";
        }
        return null;
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
        boolean checkLevel = checkLevelBreakout(series, maxIndex, 10) > 0;

        if (sma7.getValue(maxIndex).doubleValue() > sma25.getValue(maxIndex).doubleValue()
                && sma7.getValue(maxIndex - 1).doubleValue() < sma25.getValue(maxIndex - 1).doubleValue()) {
            Log.info(StrategyStoch.class, "[SHORT]:" + series.getName() + " [SMA7 & SMA25 ] SMA7 > SMA25  . Position close.");

            return "[SMA7 & SMA25 ] SMA7 > SMA25  . Position close.";

        } else if (
                openPrice.getValue(maxIndex - 2).doubleValue() > closePrice.getValue(maxIndex - 2).doubleValue()
                        && openPrice.getValue(maxIndex - 3).doubleValue() > closePrice.getValue(maxIndex - 3).doubleValue()
                        && openPrice.getValue(maxIndex - 1).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()
                        && openPrice.getValue(maxIndex - 2).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()
                        && minPrice.getValue(maxIndex-2).doubleValue() > minPrice.getValue(maxIndex-1).doubleValue()
//                        && (prev[0] < curr[0])
                        && checkLevel
        ) {
            Log.info(StrategyStoch.class, "[SHORT]:" + series.getName() + " Bullish Engulfing  . Position close.");
            return " Bullish Engulfing 1 candle";
        } else if (
                openPrice.getValue(maxIndex - 3).doubleValue() > closePrice.getValue(maxIndex - 3).doubleValue()
                        && openPrice.getValue(maxIndex - 2).doubleValue() == closePrice.getValue(maxIndex - 3).doubleValue()
                        && openPrice.getValue(maxIndex - 2).doubleValue() < closePrice.getValue(maxIndex - 2).doubleValue()
                        && openPrice.getValue(maxIndex - 1).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()
                        && openPrice.getValue(maxIndex - 3).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()
                        && minPrice.getValue(maxIndex-3).doubleValue() > minPrice.getValue(maxIndex-2).doubleValue()
                        && checkLevel
        ) {
            Log.info(StrategyStoch.class, "[SHORT]:" + series.getName() + " Bullish Engulfing 2 candle");
            return " Bullish Engulfing 2 candle";
        }
        return null;
    }

    public static String openStochStrategyLong(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        OpenPriceIndicator openPrice = new OpenPriceIndicator(series);

        MinPriceIndicator minPrice = new MinPriceIndicator(series);
        MaxPriceIndicator maxPrice = new MaxPriceIndicator(series);

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
        boolean checkLevel = true;// checkLevelBreakout(series, maxIndex, 14) > 0;

// nn [plus, minus]

            if (
                openPrice.getValue(maxIndex - 2).doubleValue() > closePrice.getValue(maxIndex - 2).doubleValue()
                        && openPrice.getValue(maxIndex - 3).doubleValue() > closePrice.getValue(maxIndex - 3).doubleValue()
                        && openPrice.getValue(maxIndex - 1).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()
                        && openPrice.getValue(maxIndex - 2).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()
                        && minPrice.getValue(maxIndex-2).doubleValue() > minPrice.getValue(maxIndex-1).doubleValue()
                        && (rsi.getValue(maxIndex-1).doubleValue() < 35 || rsi.getValue(maxIndex-2).doubleValue() > 35  )
                        && (prev[0] < curr[0])
                        && checkLevel
        ) {
              //  Log.info(StrategyStoch.class,
                System.out.print("[LONG]:" + series.getName() + " Bullish engulfing 1 candle | ");

                return "[LONG]:" + series.getName() + " Bullish engulfing 1 candle | ";
            } else if (
                    openPrice.getValue(maxIndex - 3).doubleValue() > closePrice.getValue(maxIndex - 3).doubleValue()
                            && openPrice.getValue(maxIndex - 4).doubleValue() > closePrice.getValue(maxIndex - 4).doubleValue()
                            && openPrice.getValue(maxIndex - 2).doubleValue() < closePrice.getValue(maxIndex - 2).doubleValue()
                            && openPrice.getValue(maxIndex - 3).doubleValue() < closePrice.getValue(maxIndex - 2).doubleValue()
                            && minPrice.getValue(maxIndex-3).doubleValue() > minPrice.getValue(maxIndex-2).doubleValue()
                            && (rsi.getValue(maxIndex-3).doubleValue() < 35 || rsi.getValue(maxIndex-2).doubleValue() > 35  )
                            && (prev[0] < curr[0])
                            && checkLevel
            ) {
                //  Log.info(StrategyStoch.class,
                System.out.print("[LONG]:" + series.getName() + " Bullish engulfing 1 candle + 1 candle | ");

                return "[LONG]:" + series.getName() + " Bullish engulfing 1 candle + 1 candle | ";
            } else if (
                         openPrice.getValue(maxIndex - 1).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()
                         && openPrice.getValue(maxIndex - 2).doubleValue() < closePrice.getValue(maxIndex - 2).doubleValue()
                         && openPrice.getValue(maxIndex - 3).doubleValue() < closePrice.getValue(maxIndex - 3).doubleValue()
                         && openPrice.getValue(maxIndex - 4).doubleValue() > closePrice.getValue(maxIndex - 4).doubleValue()
                         && (rsi.getValue(maxIndex-1).doubleValue() < 35 || rsi.getValue(maxIndex-2).doubleValue() > 35  )
                         && (prev[0] < curr[0])
                         && checkLevel
            ) {
               // Log.info(StrategyStoch.class,
                System.out.print("[LONG]:" + series.getName() + " 3 white soldiers | ");
                return "[LONG]:" + series.getName() + " 3 white soldiers | ";

            } else if ( openPrice.getValue(maxIndex-3).doubleValue() > closePrice.getValue(maxIndex-23).doubleValue()
                    && openPrice.getValue(maxIndex-2).doubleValue() > closePrice.getValue(maxIndex-2).doubleValue()
                    && openPrice.getValue(maxIndex-1).doubleValue() < closePrice.getValue(maxIndex-1).doubleValue()
                    && ((minPrice.getValue(maxIndex-1).doubleValue()-openPrice.getValue(maxIndex-1).doubleValue())/Math.abs(openPrice.getValue(maxIndex-1).doubleValue()-closePrice.getValue(maxIndex-1).doubleValue()) > 5 )
                    && (rsi.getValue(maxIndex-1).doubleValue() < 35 || rsi.getValue(maxIndex-2).doubleValue() > 35  )
                    && checkLevel )
              {
                // Log.info(StrategyStoch.class,
                System.out.print("[LONG]:" + series.getName() + " Hammer | ");
                return "[LONG]:" + series.getName() + " Hammer | ";
            } else if (openPrice.getValue(maxIndex - 3).doubleValue() > closePrice.getValue(maxIndex - 3).doubleValue() // Медвежья свеча
                    && openPrice.getValue(maxIndex - 1).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()  // Бычья свеча
                    && Math.abs(minPrice.getValue(maxIndex - 2).doubleValue() - maxPrice.getValue(maxIndex - 2).doubleValue()) > 4 * Math.abs(openPrice.getValue(maxIndex - 2).doubleValue() - closePrice.getValue(maxIndex - 2).doubleValue())
                    && openPrice.getValue(maxIndex - 3).doubleValue() < closePrice.getValue(maxIndex - 1).doubleValue()
                    && (rsi.getValue(maxIndex-1).doubleValue() < 35 || rsi.getValue(maxIndex-2).doubleValue() > 35  )
                    && prev[1] < curr[1]
                    && checkLevel
            ) {
                //Log.info(StrategyStoch.class,
                System.out.print("[LONG]:" + series.getName() + " Morning Star  | ");
                return "[LONG]:" + series.getName() + " Morning Star  | ";
        }
        return null;

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
        MinPriceIndicator minPrice = new MinPriceIndicator(series);
        PlusDMIndicator plusDM = new PlusDMIndicator(series);
        MinusDMIndicator minusDM = new MinusDMIndicator(series);
        SMAIndicator smoothedPlusDM = new SMAIndicator(plusDM, 14);
        SMAIndicator smoothedMinusDM = new SMAIndicator(minusDM, 14);
        DXIndicator dxIndicator = new DXIndicator(series, 14);

        ATRIndicator atr = new ATRIndicator(series, 14);
        double[] prev = {calculatePlusDI(smoothedPlusDM.getValue(maxIndex - 1).doubleValue(), atr.getValue(maxIndex - 1).doubleValue()), calculateMinusDI(smoothedMinusDM.getValue(maxIndex - 1).doubleValue(), atr.getValue(maxIndex - 1).doubleValue())};
        double[] curr = {calculatePlusDI(smoothedPlusDM.getValue(maxIndex).doubleValue(), atr.getValue(maxIndex).doubleValue()), calculateMinusDI(smoothedMinusDM.getValue(maxIndex).doubleValue(), atr.getValue(maxIndex).doubleValue())};
        boolean checkLevel = (checkLevelBreakout(series, maxIndex, 10) < 0);

        if (sma7.getValue(maxIndex).doubleValue() < sma25.getValue(maxIndex).doubleValue()
                && sma7.getValue(maxIndex - 1).doubleValue() > sma25.getValue(maxIndex - 1).doubleValue()) {
            Log.info(StrategyStoch.class, "[LONG]:" + series.getName() + " [SMA7 & SMA25 ] SMA7 < SMA25  . Position close.");
            return "[SMA7 & SMA25 ] SMA7 < SMA25  . Position close.";

        } else if (
                openPrice.getValue(maxIndex - 2).doubleValue() < closePrice.getValue(maxIndex - 2).doubleValue()
                        && openPrice.getValue(maxIndex - 3).doubleValue() < closePrice.getValue(maxIndex - 3).doubleValue()
                        && openPrice.getValue(maxIndex - 1).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue()
                        && openPrice.getValue(maxIndex - 2).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue()
                        && maxPrice.getValue(maxIndex-2).doubleValue() < maxPrice.getValue(maxIndex-1).doubleValue()
//                        && prev[1] < curr[1]
                        && checkLevel
        ) {
            Log.info(StrategyStoch.class, "[LONG]:" + series.getName() + " Bearish Engulfing  . Position close.");
            return "Bearish Engulfing 1 candle";

        } else if (
                openPrice.getValue(maxIndex - 3).doubleValue() < closePrice.getValue(maxIndex - 3).doubleValue()
                        && openPrice.getValue(maxIndex - 2).doubleValue() == closePrice.getValue(maxIndex - 3).doubleValue()
                        && openPrice.getValue(maxIndex - 2).doubleValue() > closePrice.getValue(maxIndex - 2).doubleValue()
                        && openPrice.getValue(maxIndex - 1).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue()
                        && openPrice.getValue(maxIndex - 3).doubleValue() > closePrice.getValue(maxIndex - 1).doubleValue()
                        && maxPrice.getValue(maxIndex-3).doubleValue() < maxPrice.getValue(maxIndex-2).doubleValue()
//                        && prev[1] < curr[1]
                        && checkLevel
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

    // Найдем минимумы и максимумы за последние N периодов
    public static double findSupportLevel(TimeSeries series, int maxIndex, int period) {
        double supportLevel = Double.MAX_VALUE;
        for (int i = maxIndex - period; i < maxIndex; i++) {
            //double low = series.getBar(i).getMinPrice().doubleValue();
            double low = series.getBar(i).getClosePrice().doubleValue();
            if (low < supportLevel) {
                supportLevel = low;
            }
        }
   //     Log.info(StrategyStoch.class,  series.getName() +" Price broke support level at " + supportLevel + "   " + series.getBar(maxIndex).getClosePrice() + "     " + series.getBar(maxIndex).getAmount() );
        return supportLevel;
    }

    public static double findResistanceLevel(TimeSeries series, int maxIndex, int period) {
        double resistanceLevel = Double.MIN_VALUE;
        for (int i = maxIndex - period; i < maxIndex; i++) {
            //double high = series.getBar(i).getMaxPrice().doubleValue();
            double high = series.getBar(i).getClosePrice().doubleValue();
            if (high > resistanceLevel) {
                resistanceLevel = high;
            }
        }
   //     Log.info(StrategyStoch.class,  series.getName() +" Price broke resistensce level at " + resistanceLevel + "   " + series.getBar(maxIndex).getClosePrice() + "     " + series.getBar(maxIndex).getAmount() );
        return resistanceLevel;
    }

    // Проверка пробоя уровней поддержки/сопротивления для открытия сделки
    public static int checkLevelBreakout(TimeSeries series, int maxIndex, int period) {
        double supportLevel = findSupportLevel(series, maxIndex, period);
        double resistanceLevel = findResistanceLevel(series, maxIndex, period);
        double currentClose = series.getBar(maxIndex).getClosePrice().doubleValue();

        // Проверка для шорта: пробой поддержки
        if (currentClose < supportLevel) {
       //     Log.info(StrategyStoch.class, "[SHORT]: "+ series.getName() +" Support level at " + supportLevel );
            return -1;
        }
        // Проверка для лонга: пробой сопротивления
        else if (currentClose > resistanceLevel) {
         //   Log.info(StrategyStoch.class, "[LONG]: " + series.getName() + "Resistance level at " + resistanceLevel);
            return 1;
        }

        return 0;
    }
}





