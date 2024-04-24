package com.my.copybot.util;

import com.binance.api.client.domain.market.Candlestick;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.DifferenceIndicator;
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
        EMAIndicator sma1 = new EMAIndicator(closePrice, 50);
        EMAIndicator sma2 = new EMAIndicator(closePrice, 100);
        EMAIndicator sma14 = new EMAIndicator(closePrice, 200);
        EMAIndicator sma24 = new EMAIndicator(closePrice, 400);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        MACDIndicator macdExit = new MACDIndicator(closePrice, 5, 15);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);
        EMAIndicator emaMacdEnter = new EMAIndicator(macd, 13);
        SMAIndicator shortTermSMA = new SMAIndicator(closePrice, 5);
        SMAIndicator longTermSMA = new SMAIndicator(closePrice, 9);
        EMAIndicator sma3 = new EMAIndicator(closePrice, 3);
        EMAIndicator sma5 = new EMAIndicator(closePrice, 5);


        Indicator<Decimal> macdDiff = new DifferenceIndicator(macdExit, macdExit);
        //Rule exitRule = new CrossedDownIndicatorRule(macdDiff, Decimal.ZERO);

//        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, stochasticPeriod);
//        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
//        RSIIndicator rsiIndicator = new RSIIndicator(closePrice, rsiPeriod);

        //	AverageDirectionalMovementIndicator adx = new AverageDirectionalMovementIndicator(series, adxPeriod);
        //	AverageTrueRangeIndicator atr = new AverageTrueRangeIndicator(series, adxPeriod);
        //	ChaikinMoneyFlowIndicator cmf = new ChaikinMoneyFlowIndicator(series, cmfPeriod);
        //#stat	WilliamsRIndicator williamsR = new WilliamsRIndicator(series, williamsRPeriod);
        System.out.println(series.getName() + "    " + macdDiff.getValue(macdDiff.getTimeSeries().getEndIndex()));
        Rule entryRule = new CrossedUpIndicatorRule(macd, emaMacdEnter)

                .and(new OverIndicatorRule(macd, emaMacd))
                .and(new OverIndicatorRule(sma14, sma24))
                .and(new OverIndicatorRule(sma1, sma2));

        Rule exitRule = ((new UnderIndicatorRule(macd, emaMacd))
                .and(new UnderIndicatorRule(macd, emaMacdEnter)))
                //.or(new CrossedDownIndicatorRule(macdDiff, Decimal.ZERO))
                .or(new CrossedUpIndicatorRule(sma1, sma2));


        return new BaseStrategy(entryRule, exitRule);
    }


    public static Strategy buildMacdStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator sma1 = new EMAIndicator(closePrice, 50);
        EMAIndicator sma2 = new EMAIndicator(closePrice, 100);
        EMAIndicator sma14 = new EMAIndicator(closePrice, 200);
        EMAIndicator sma24 = new EMAIndicator(closePrice, 400);
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        MACDIndicator macdExit = new MACDIndicator(closePrice, 5, 15);
        EMAIndicator emaMacdEnter = new EMAIndicator(macd, 13);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);

        SMAIndicator shortTermSMA = new SMAIndicator(closePrice, 5);
        SMAIndicator longTermSMA = new SMAIndicator(closePrice, 9);
        EMAIndicator sma3 = new EMAIndicator(closePrice, 3);
        EMAIndicator sma5 = new EMAIndicator(closePrice, 5);
        Indicator<Decimal> macdDiff = new DifferenceIndicator(macdExit, macdExit);



        Rule entryRule = new CrossedDownIndicatorRule(macd, emaMacdEnter)
                .and(new UnderIndicatorRule(macd, emaMacd))
                .and(new UnderIndicatorRule(sma14, sma24))
                .and(new UnderIndicatorRule(sma1, sma2));

        Rule exitRule = ((new OverIndicatorRule(macd, emaMacd))
                .and(new OverIndicatorRule(macd, emaMacdEnter)))
                //       .or(new CrossedUpIndicatorRule(macdDiff, Decimal.ZERO))
                .or(new OverIndicatorRule(shortTermSMA, longTermSMA));


        return new BaseStrategy(entryRule, exitRule);
    }

}

