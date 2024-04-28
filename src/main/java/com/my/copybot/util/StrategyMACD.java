package com.my.copybot.util;

import com.binance.api.client.domain.market.Candlestick;
import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
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
        List<Tick> ticks = new LinkedList<Tick>();
        for (Candlestick candlestick : candlesticks) {
            ticks.add(convertToTa4jTick(candlestick));
        }
        return new BaseTimeSeries(symbol + "_" + period, ticks);
    }

    public static Tick convertToTa4jTick(Candlestick candlestick) {
        ZonedDateTime closeTime = getZonedDateTime(candlestick.getCloseTime());
        Duration candleDuration = Duration.ofMillis(candlestick.getCloseTime()
                - candlestick.getOpenTime());
        Decimal openPrice = Decimal.valueOf(candlestick.getOpen());
        Decimal closePrice = Decimal.valueOf(candlestick.getClose());
        Decimal highPrice = Decimal.valueOf(candlestick.getHigh());
        Decimal lowPrice = Decimal.valueOf(candlestick.getLow());
        Decimal volume = Decimal.valueOf(candlestick.getVolume());

        return new BaseTick(candleDuration, closeTime, openPrice, highPrice,
                lowPrice, closePrice, volume);
    }

    public static ZonedDateTime getZonedDateTime(Long timestamp) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                ZoneId.systemDefault());
    }

    public static boolean isSameTick(Candlestick candlestick, Tick tick) {
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

        EMAIndicator sma14 = new EMAIndicator(closePrice, 200);
        EMAIndicator sma24 = new EMAIndicator(closePrice, 400);
        MACDIndicator macd = new MACDIndicator(closePrice, 50, 100);
        EMAIndicator emaMacd = new EMAIndicator(macd, 49);

        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, 15);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);


        Decimal diffStoch = Decimal.valueOf(stochK.getValue(stochK.getTimeSeries().getEndIndex()).toDouble()
                - stochK.getValue(stochK.getTimeSeries().getEndIndex() - 1).toDouble());

        Decimal diffMacd = Decimal.valueOf(macd.getValue(macd.getTimeSeries().getEndIndex()).toDouble()
                - macd.getValue(macd.getTimeSeries().getEndIndex() - 1).toDouble());

        Decimal diffMacdPrev = Decimal.valueOf(macd.getValue(macd.getTimeSeries().getEndIndex() - 1).toDouble()
                - macd.getValue(macd.getTimeSeries().getEndIndex() - 2).toDouble());

        // System.out.println(series.getName()+"  diff = " + diff);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        RSIIndicator rsiMacd = new RSIIndicator(closePrice, 14);

        Decimal levelRsiMacd;
        Decimal levelRsiStoch;

        if (diffMacdPrev.toDouble() < 0 && diffMacd.toDouble() > 0) {
            levelRsiMacd = Decimal.valueOf(-2);
        } else {
            levelRsiMacd = Decimal.valueOf(101);
        }

        if (diffStoch.toDouble() > 0) {
            levelRsiStoch = Decimal.valueOf(-2);
        } else {
            levelRsiStoch = Decimal.valueOf(101);
        }
//  new UnderIndicatorRule(rsi, levelRsi)
        Rule entryRule = new CrossedUpIndicatorRule(macd, emaMacd)
                .and(new OverIndicatorRule(rsi, levelRsiStoch))
                //       .and(new OverIndicatorRule(rsi, levelRsiMacd))
                .and(new OverIndicatorRule(sma14, sma24));


        if (diffMacd.toDouble() < 0) {
            levelRsiMacd = Decimal.valueOf(-2);
        } else {
            levelRsiMacd = Decimal.valueOf(101);
        }

        Rule exitRule = (new CrossedDownIndicatorRule(macd, emaMacd))
                .or(new UnderIndicatorRule(rsi, levelRsiStoch));
        //   .or(new OverIndicatorRule(rsi, levelRsiMacd));



        return new BaseStrategy(entryRule, exitRule);
    }


    public static Strategy buildMacdStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        EMAIndicator sma14 = new EMAIndicator(closePrice, 200);
        EMAIndicator sma24 = new EMAIndicator(closePrice, 400);
        MACDIndicator macd = new MACDIndicator(closePrice, 50, 100);
        EMAIndicator emaMacd = new EMAIndicator(macd, 49);

        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, 15);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);


        Decimal diffStoch = Decimal.valueOf(stochK.getValue(stochK.getTimeSeries().getEndIndex()).toDouble()
                - stochK.getValue(stochK.getTimeSeries().getEndIndex() - 1).toDouble());

        Decimal diffMacd = Decimal.valueOf(macd.getValue(macd.getTimeSeries().getEndIndex()).toDouble()
                - macd.getValue(macd.getTimeSeries().getEndIndex() - 1).toDouble());

        Decimal diffMacdPrev = Decimal.valueOf(macd.getValue(macd.getTimeSeries().getEndIndex() - 1).toDouble()
                - macd.getValue(macd.getTimeSeries().getEndIndex() - 2).toDouble());

        // System.out.println(series.getName()+"  diff = " + diff);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        RSIIndicator rsiMacd = new RSIIndicator(closePrice, 14);

        Decimal levelRsiMacd;
        Decimal levelRsiStoch;

        if (diffMacdPrev.toDouble() > 0 && diffMacd.toDouble() < 0) {
            levelRsiMacd = Decimal.valueOf(-2);
        } else {
            levelRsiMacd = Decimal.valueOf(101);
        }

        if (diffStoch.toDouble() < 0) {
            levelRsiStoch = Decimal.valueOf(-2);
        } else {
            levelRsiStoch = Decimal.valueOf(101);
        }
//  new UnderIndicatorRule(rsi, levelRsi)
        Rule entryRule = new CrossedDownIndicatorRule(macd, emaMacd)
                .and(new OverIndicatorRule(rsi, levelRsiStoch))
                //       .and(new OverIndicatorRule(rsi, levelRsiMacd))
                .and(new UnderIndicatorRule(sma14, sma24));



        if (diffMacd.toDouble() > 0) {
            levelRsiMacd = Decimal.valueOf(-2);
        } else {
            levelRsiMacd = Decimal.valueOf(101);
        }

        Rule exitRule = (new CrossedUpIndicatorRule(macd, emaMacd))
                .or(new UnderIndicatorRule(rsi, levelRsiStoch));
        //   .or(new OverIndicatorRule(rsi, levelRsiMacd));



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

