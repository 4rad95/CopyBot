package com.my.copybot.util;

import com.binance.api.client.domain.market.Candlestick;
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
        EMAIndicator emaShort = new EMAIndicator(closePrice, 5);
        EMAIndicator emaLong = new EMAIndicator(closePrice, 15);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);

        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        //      StochasticRSIIndicator stoRsi = new StochasticRSIIndicator(closePrice, 14);
        //       StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(stoRsi, 3, new MaxPriceIndicator(series), new MinPriceIndicator(series));
        //       StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
//        StochasticOscillatorKIndicator ssK = new StochasticOscillatorKIndicator(series, 20);
//        StochasticOscillatorDIndicator ssD = new StochasticOscillatorDIndicator(ssK);

        Decimal diffEma = Decimal.valueOf(emaShort.getValue(emaShort.getTimeSeries().getEndIndex()).toDouble()
                - emaShort.getValue(emaShort.getTimeSeries().getEndIndex() - 1).toDouble());


        Decimal diffMacd = Decimal.valueOf(macd.getValue(macd.getTimeSeries().getEndIndex()).toDouble()
                - macd.getValue(macd.getTimeSeries().getEndIndex() - 1).toDouble());

        Decimal deltaK = Decimal.valueOf(102);
        if ((diffEma.doubleValue() > 0) && (diffMacd.doubleValue() > 0)) {
            deltaK = Decimal.valueOf(-2);
        }

        Rule entryRule = new CrossedUpIndicatorRule(emaShort, emaLong)
                .and(new OverIndicatorRule(sma14, sma24))
                //    .and(new UnderIndicatorRule(ssK, Decimal.valueOf(40)))
                //.and(new UnderIndicatorRule(macd, emaMacd))
                .and(new OverIndicatorRule(rsi, deltaK));
//                .and(new OverIndicatorRule(ssK, ssD))
//                .and(new OverIndicatorRule(emaShort, emaLong));
        //       .and(new OverIndicatorRule(ssK, ssD));


//        Rule entryRule = new CrossedUpIndicatorRule(macd, emaMacd)
//                .and(new OverIndicatorRule(sma14, sma24))
//                .and(new OverIndicatorRule(ssK, ssD))
//                .and(new OverIndicatorRule(emaShort, emaLong))
//                .and(new OverIndicatorRule(stochK, stochD));

        //     .and(new Is(bullishHarami, Decimal.valueOf(1)));
        //  .and(new OverIndicatorRule(rsi, levelRsiStoch))
        //        .and(new UnderIndicatorRule(macdDirection, emaMacdDirection))

        if ((diffMacd.toDouble() < 0) && (diffEma.doubleValue() < 0)) {
            deltaK = Decimal.valueOf(-2);
        } else {
            deltaK = Decimal.valueOf(102);
        }


        Rule exitRule = // (new UnderIndicatorRule(ssK, ssD))
        //(new CrossedDownIndicatorRule(macd, emaMacd))
                // (new UnderIndicatorRule(emaShort, emaLong))
                (new OverIndicatorRule(sma14, sma24))
                        .or(new OverIndicatorRule(rsi, deltaK));


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
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);

//        MACDIndicator macdDirection = new MACDIndicator(closePrice, 50, 100);
//        EMAIndicator emaMacdDirection = new EMAIndicator(macd, 40);

        RSIIndicator rsi = new RSIIndicator(closePrice, 14);

//        StochasticOscillatorKIndicator ssK = new StochasticOscillatorKIndicator(series, 20);
//        StochasticOscillatorDIndicator ssD = new StochasticOscillatorDIndicator(ssK);

        int maxIndex = series.getEndIndex();

        Decimal diffEma = Decimal.valueOf(emaShort.getValue(emaShort.getTimeSeries().getEndIndex()).toDouble()
                - emaShort.getValue(emaShort.getTimeSeries().getEndIndex() - 1).toDouble());

        Decimal diffMacd = Decimal.valueOf(macd.getValue(macd.getTimeSeries().getEndIndex()).toDouble()
                - macd.getValue(macd.getTimeSeries().getEndIndex() - 1).toDouble());


        // Decimal diffPrice = Decimal.valueOf((closePrice.getValue(maxIndex-1).doubleValue()-openPrice.getValue(maxIndex-1).doubleValue()));
        //                closePrice.getValue(maxIndex-1).doubleValue()-openPrice.getValue(maxIndex-1).doubleValue()+
        //                closePrice.getValue(maxIndex-2).doubleValue()-openPrice.getValue(maxIndex-2).doubleValue())/3);


        Decimal deltaK = Decimal.valueOf(-2);
        if ((diffEma.doubleValue() < 0) && (diffMacd.doubleValue() < 0)) {
            deltaK = Decimal.valueOf(102);
        }

//        Rule entryRule = new CrossedDownIndicatorRule(macd, emaMacd)
//                .and(new UnderIndicatorRule(sma14, sma24))
//                .and(new UnderIndicatorRule(ssK, ssD))
//                .and(new UnderIndicatorRule(emaShort, emaLong))
//                .and(new UnderIndicatorRule(stochK, stochD));

        Rule entryRule = new CrossedDownIndicatorRule(emaShort, emaLong)
                .and(new UnderIndicatorRule(sma14, sma24))
                //        .and(new OverIndicatorRule(macd, emaMacd))
                .and(new OverIndicatorRule(rsi, deltaK));
        // .and(new UnderIndicatorRule(ssK, ssD));
        //  .and(new UnderIndicatorRule(emaShort, emaLong));
        //.and(new UnderIndicatorRule(stochK, stochD));


        if ((diffMacd.toDouble() > 0) && (diffEma.doubleValue() > 0)) {
            deltaK = Decimal.valueOf(-2);
        } else {
            deltaK = Decimal.valueOf(102);
        }

//        RSIIndicator1 rsiMy = new RSIIndicator1(closePrice, 14);
//        Decimal rsiValue = rsiMy.getValue(closePrice.getTimeSeries().getEndIndex());
//

//
//        System.out.println(series.getName() + "  Long = " + (closePrice.getValue(maxIndex)) + "   D= " +(openPrice.getValue(maxIndex))
//                + " Diff = " + diffPrice);
//                + "   K(my) = " + ssK.getValue(series.getEndIndex())
//                + "   D(my) = " + ssD.getValue(series.getEndIndex())
//                + "   K%(my) = " + stochRsiK);
////        System.out.println("RSI = " + rsiValue);


        Rule exitRule = // (new OverIndicatorRule(ssK, ssD))
        //(new CrossedUpIndicatorRule(macd, emaMacd))
                new OverIndicatorRule(sma14, sma24)
                        .or(new OverIndicatorRule(rsi, deltaK));
        //     .or(new OverIndicatorRule(macd, emaMacd))
                //      .or(new OverIndicatorRule(stochK, stochD))
        //    .or(new OverIndicatorRule(ssK, ssD));
        // .or(new OverIndicatorRule(rsi, levelRsiMacd));


        return new BaseStrategy(entryRule, exitRule);
    }

    public Decimal checkMacdTrend(TimeSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        MACDIndicator macd = new MACDIndicator(closePrice, 5, 15);
        // Indicator<Decimal> macdDiff = new DifferenceIndicator(macdExit, macdExit);
        Decimal diff = Decimal.valueOf(macd.getValue(macd.getTimeSeries().getEndIndex()).toDouble() - macd.getValue(macd.getTimeSeries().getEndIndex() - 1).toDouble());
        return diff;
    }




}





