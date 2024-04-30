package com.my.copybot.util;

import com.binance.api.client.domain.market.Candlestick;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import org.ta4j.core.trading.rules.UnderIndicatorRule;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

public class StrategyMACD {


    public static String STRATEGY = "MACD";

    public static TimeSeries convertToTimeSeries(
            List<Candlestick> candlesticks, String symbol, String period) {
        List<Bar> ticks = new LinkedList<Bar>();
        for (Candlestick candlestick : candlesticks) {
            ticks.add(convertToTa4jTick(candlestick));
        }
        return new BaseTimeSeries(symbol + "_" + period, ticks);
    }

    public static Bar convertToTa4jTick(Candlestick candlestick) {
        ZonedDateTime closeTime = getZonedDateTime(candlestick.getCloseTime());
        Duration candleDuration = Duration.ofMillis(candlestick.getCloseTime()
                - candlestick.getOpenTime());
        Decimal openPrice = Decimal.valueOf(candlestick.getOpen());
        Decimal closePrice = Decimal.valueOf(candlestick.getClose());
        Decimal highPrice = Decimal.valueOf(candlestick.getHigh());
        Decimal lowPrice = Decimal.valueOf(candlestick.getLow());
        Decimal volume = Decimal.valueOf(candlestick.getVolume());

        return new BaseBar(candleDuration, closeTime, openPrice, highPrice,
                lowPrice, closePrice, volume);
    }

    public static ZonedDateTime getZonedDateTime(Long timestamp) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault());
    }

    public static boolean isSameTick(Candlestick candlestick, Bar tick) {
        return tick.getEndTime().equals(
                getZonedDateTime(candlestick.getCloseTime()));
    }

//	public static Strategy buildStrategyLong(TimeSeries series, String strategyCode) {
//		if (STRATEGY.equals(strategyCode)) {
//			return buildMacdStrategyLong(series);
//		}
//		return null;
//	}

    public static Strategy buildMacdStrategyLong(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator sma14 = new EMAIndicator(closePrice, 50);
        EMAIndicator sma24 = new EMAIndicator(closePrice, 100);
        MACDIndicator macd = new MACDIndicator(closePrice, 8, 17);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);

        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stoRsi = new StochasticRSIIndicator(closePrice, 14);
        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(stoRsi, 3, new MaxPriceIndicator(series), new MinPriceIndicator(series));
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
        StochasticOscillatorKIndicator ssK = new StochasticOscillatorKIndicator(series, 14);
        StochasticOscillatorDIndicator ssD = new StochasticOscillatorDIndicator(ssK);
        Decimal diffMacd = Decimal.valueOf(macd.getValue(macd.getTimeSeries().getEndIndex()).toDouble()
                - macd.getValue(macd.getTimeSeries().getEndIndex() - 1).toDouble());


        Decimal levelRsiMacd;


        Rule entryRule = new CrossedUpIndicatorRule(macd, emaMacd)
                //      .and(new OverIndicatorRule(sma14, sma24))
                .and(new OverIndicatorRule(stochK, stochD));

        //     .and(new Is(bullishHarami, Decimal.valueOf(1)));
                //  .and(new OverIndicatorRule(rsi, levelRsiStoch))
                //        .and(new UnderIndicatorRule(macdDirection, emaMacdDirection))

        if (diffMacd.toDouble() < 0) {
            levelRsiMacd = Decimal.valueOf(-2);
        } else {
            levelRsiMacd = Decimal.valueOf(101);
        }

        Rule exitRule = (new CrossedDownIndicatorRule(macd, emaMacd))
                .or(new UnderIndicatorRule(macd, emaMacd))
                .or(new UnderIndicatorRule(stochK, stochD))
                .or(new UnderIndicatorRule(ssK, ssD))
                .or(new OverIndicatorRule(rsi, levelRsiMacd));

        return new BaseStrategy(entryRule, exitRule);
    }


    public static Strategy buildMacdStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator sma14 = new EMAIndicator(closePrice, 50);
        EMAIndicator sma24 = new EMAIndicator(closePrice, 100);
        MACDIndicator macd = new MACDIndicator(closePrice, 8, 17);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);

        MACDIndicator macdDirection = new MACDIndicator(closePrice, 50, 100);
        EMAIndicator emaMacdDirection = new EMAIndicator(macd, 40);

        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stoRsi = new StochasticRSIIndicator(rsi, 3);
        //StochasticRSIIndicator stoRsi = new StochasticRSIIndicator(rsi, 14);

        MaxPriceIndicator maxPrice = new MaxPriceIndicator(series);
        MinPriceIndicator minPrice = new MinPriceIndicator(series);

        HighestValueIndicator highestValue = new HighestValueIndicator(stoRsi, 14);
        LowestValueIndicator lowestValue = new LowestValueIndicator(stoRsi, 14);
        StochasticOscillatorKIndicator ssK = new StochasticOscillatorKIndicator(series, 14);
        StochasticOscillatorDIndicator ssD = new StochasticOscillatorDIndicator(ssK);

        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(stoRsi, 14, maxPrice, minPrice);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);


        Decimal diffMacd = Decimal.valueOf(macd.getValue(macd.getTimeSeries().getEndIndex()).toDouble()
                - macd.getValue(macd.getTimeSeries().getEndIndex() - 1).toDouble());

        Decimal levelRsiMacd;

        Rule entryRule = new CrossedDownIndicatorRule(macd, emaMacd)
                //     .and(new UnderIndicatorRule(sma14, sma24))
                .and(new UnderIndicatorRule(stochK, stochD));



        if (diffMacd.toDouble() > 0) {
            levelRsiMacd = Decimal.valueOf(-2);
        } else {
            levelRsiMacd = Decimal.valueOf(101);
        }

//        RSIIndicator1 rsiMy = new RSIIndicator1(closePrice, 14);
//        Decimal rsiValue = rsiMy.getValue(closePrice.getTimeSeries().getEndIndex());
//
//        Decimal stochRsiK = calculateStochRSI(rsi, 3, series.getEndIndex()); // %K стохастического RSI
//
//
//        System.out.println(series.getName() + "  K = " + stochK.getValue(series.getEndIndex()) + "   D= "
//                                            + stochD.getValue(series.getEndIndex())
//                                            + "   K(my) = " + ssK.getValue(series.getEndIndex())
//                                            + "   D(my) = " + ssD.getValue(series.getEndIndex()));
////        System.out.println("RSI = " + rsiValue);

        Rule exitRule = (new CrossedUpIndicatorRule(macd, emaMacd))
                .or(new OverIndicatorRule(macd, emaMacd))
                .or(new OverIndicatorRule(stochK, stochD))
                .or(new OverIndicatorRule(ssK, ssD))
                .or(new OverIndicatorRule(rsi, levelRsiMacd));




        return new BaseStrategy(entryRule, exitRule);
    }

    public Decimal checkMacdTrend(TimeSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(closePrice, 5, 15);
        // Indicator<Decimal> macdDiff = new DifferenceIndicator(macdExit, macdExit);
        Decimal diff = Decimal.valueOf(macd.getValue(macd.getTimeSeries().getEndIndex()).toDouble() - macd.getValue(macd.getTimeSeries().getEndIndex() - 1).toDouble());
        return diff;
    }


    private static Decimal calculateStochRSI(RSIIndicator rsi, int period, int endIndex) {
        int startIndex = Math.max(0, endIndex - period + 1);
        int lookBackPeriod = endIndex - startIndex + 1;

        // Находим максимальное и минимальное значение RSI за период
        Decimal maxRSI = rsi.getValue(startIndex);
        Decimal minRSI = rsi.getValue(startIndex);
        for (int i = startIndex + 1; i <= endIndex; i++) {
            Decimal currentRSI = rsi.getValue(i);
            if (currentRSI.isGreaterThan(maxRSI)) {
                maxRSI = currentRSI;
            }
            if (currentRSI.isLessThan(minRSI)) {
                minRSI = currentRSI;
            }
        }

        // Вычисляем %K
        Decimal range = maxRSI.minus(minRSI);
        Decimal currentRSI = rsi.getValue(endIndex);
        return currentRSI.minus(minRSI).dividedBy(range);
    }


}


