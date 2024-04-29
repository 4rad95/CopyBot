package com.my.copybot.util;

import com.binance.api.client.domain.market.Candlestick;
import org.ta4j.core.*;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.trading.rules.CrossedDownIndicatorRule;
import org.ta4j.core.trading.rules.CrossedUpIndicatorRule;

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
        SMAIndicator sma = new SMAIndicator(closePrice, 10);

        Decimal entryLevel = Decimal.valueOf("1.10").multipliedBy(sma.getValue(series.getEndIndex())); // Уровень входа: 110% от средней цены закрытия
        Decimal exitLevel = Decimal.valueOf("1.05").multipliedBy(sma.getValue(series.getEndIndex()));  // Уровень выхода: 105% от средней цены закрытия

        Rule entryRule = new CrossedDownIndicatorRule(closePrice, entryLevel);
        Rule exitRule = new CrossedUpIndicatorRule(closePrice, exitLevel);

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
        SMAIndicator sma = new SMAIndicator(closePrice, 10);

        Decimal entryLevel = Decimal.valueOf("0.90").multipliedBy(sma.getValue(series.getEndIndex())); // Уровень входа: 90% от средней цены закрытия
        Decimal exitLevel = Decimal.valueOf("0.95").multipliedBy(sma.getValue(series.getEndIndex()));  // Уровень выхода: 95% от средней цены закрытия

        Rule entryRule = new CrossedUpIndicatorRule(closePrice, entryLevel);
        Rule exitRule = new CrossedDownIndicatorRule(closePrice, exitLevel);

        return new BaseStrategy(entryRule, exitRule);

    }


}