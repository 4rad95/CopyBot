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


public class StrategySMA {

    public static String STRATEGY = "SMA";

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

    public static Strategy buildSmaStrategyLong(TimeSeries series, String strategyCode) {
        if (STRATEGY.equals(strategyCode)) {
            return buildSmaStrategyLong(series);
        }
        return null;
    }

    public static Strategy buildSmaStrategyLong(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);


        SMAIndicator shortTermSMA = new SMAIndicator(closePrice, 100);
        SMAIndicator longTermSMA = new SMAIndicator(closePrice, 200);   // 100
        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, 14);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
        RSIIndicator rsiIndicator = new RSIIndicator(closePrice, 14);
        EMAIndicator sma1 = new EMAIndicator(closePrice, 20);
        EMAIndicator sma2 = new EMAIndicator(closePrice, 50);
        // Индикаторы из разных категорий

        Rule entryRule = new CrossedUpIndicatorRule(sma1, sma2)
                //      .and(new OverIndicatorRule(rsiIndicator, Decimal.valueOf(50)))
                //      .and(new UnderIndicatorRule(stochK, stochD))
                .and(new UnderIndicatorRule(shortTermSMA, longTermSMA));
                //	.and(new UnderIndicatorRule(williamsR, Decimal.valueOf(-50)))
        //         .and(new UnderIndicatorRule(stochD, Decimal.valueOf(60)));

        Rule exitRule = //new CrossedDownIndicatorRule(sma1, sma2)
                new UnderIndicatorRule(sma1, sma2)
                        .or(new CrossedDownIndicatorRule(rsiIndicator, Decimal.valueOf(50)));

        return new BaseStrategy(entryRule, exitRule);

    }


    public static Strategy buildStrategyShort(TimeSeries series, String strategyCode) {
        if (STRATEGY.equals(strategyCode)) {
            return buildSmaStrategyShort(series);
        }
        return null;
    }

    public static Strategy buildSmaStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        SMAIndicator shortTermSMA = new SMAIndicator(closePrice, 50);
        SMAIndicator longTermSMA = new SMAIndicator(closePrice, 100);   // 100
        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, 14);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
        RSIIndicator rsiIndicator = new RSIIndicator(closePrice, 14);
        EMAIndicator sma1 = new EMAIndicator(closePrice, 20);
        EMAIndicator sma2 = new EMAIndicator(closePrice, 50);

        // Индикаторы из разных категорий

        Rule entryRule = new CrossedDownIndicatorRule(sma1, sma2)
                //  .and(new UnderIndicatorRule(rsiIndicator, Decimal.valueOf(50)))
                //  .and(new UnderIndicatorRule(stochK, stochD))
                .and(new OverIndicatorRule(longTermSMA, shortTermSMA));
        //  .and(new OverIndicatorRule(stochD, Decimal.valueOf(40)));

        Rule exitRule = //new CrossedDownIndicatorRule(sma1, sma2)
                new OverIndicatorRule(sma1, sma2)    // ?
                        .or(new CrossedUpIndicatorRule(rsiIndicator, Decimal.valueOf(50)));

        return new BaseStrategy(entryRule, exitRule);
    }


}