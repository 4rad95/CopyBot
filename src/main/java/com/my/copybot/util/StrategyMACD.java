package com.my.copybot.util;

import com.binance.api.client.domain.market.Candlestick;
import org.ta4j.core.*;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.SMAIndicator;
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


        // Параметризация периодов для индикаторов

        int shortTermPeriod = 50;
        int longTermPeriod = 200;
        int stochasticPeriod = 14;
        int rsiPeriod = 14;
        int adxPeriod = 14;
        int cmfPeriod = 20;
        int williamsRPeriod = 14;

        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        MACDIndicator macdHigh = new MACDIndicator(closePrice, 48, 104);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);
        EMAIndicator emaMacdHigh = new EMAIndicator(macdHigh, 9);
        SMAIndicator shortTermSMA = new SMAIndicator(closePrice, shortTermPeriod);
        SMAIndicator longTermSMA = new SMAIndicator(closePrice, longTermPeriod);
//        StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, stochasticPeriod);
//        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
//        RSIIndicator rsiIndicator = new RSIIndicator(closePrice, rsiPeriod);


        //	AverageDirectionalMovementIndicator adx = new AverageDirectionalMovementIndicator(series, adxPeriod);
        //	AverageTrueRangeIndicator atr = new AverageTrueRangeIndicator(series, adxPeriod);
        //	ChaikinMoneyFlowIndicator cmf = new ChaikinMoneyFlowIndicator(series, cmfPeriod);
        //#stat	WilliamsRIndicator williamsR = new WilliamsRIndicator(series, williamsRPeriod);

        EMAIndicator sma1 = new EMAIndicator(closePrice, 50);
        EMAIndicator sma2 = new EMAIndicator(closePrice, 100);

        Rule entryRule = new CrossedUpIndicatorRule(macd, emaMacd)
                //               .and(new OverIndicatorRule(macdHigh, emaMacdHigh))
                .and(new OverIndicatorRule(sma1, sma2));
//                .and(new OverIndicatorRule(shortTermSMA, longTermSMA));
//                .and(new OverIndicatorRule(stochK, stochD))
//                //		.and(new OverIndicatorRule(adx, Decimal.valueOf(25)))
//                .and(new OverIndicatorRule(rsiIndicator, Decimal.valueOf(30)))
//                .and(new UnderIndicatorRule(stochD, Decimal.valueOf(40)));


        Rule exitRule = new UnderIndicatorRule(macd, emaMacd)
                .or(new CrossedUpIndicatorRule(sma1, sma2));
        //       .or(new UnderIndicatorRule(stochK, stochD))
        //       .or(new UnderIndicatorRule(shortTermSMA, longTermSMA));


        return new BaseStrategy(entryRule, exitRule);
    }


//	public static Strategy buildStrategyShort(TimeSeries series, String strategyCode) {
//		if (STRATEGY.equals(strategyCode)) {
//			return buildMacdStrategyShort(series);
//		}
//		return null;
//	}

    public static Strategy buildMacdStrategyShort(TimeSeries series) {
        if (series == null) {
            throw new IllegalArgumentException("Series cannot be null");
        }

        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        EMAIndicator sma1 = new EMAIndicator(closePrice, 50);
        EMAIndicator sma2 = new EMAIndicator(closePrice, 100);
        // Параметризация периодов для индикаторов
        int shortTermPeriod = 50;
        int longTermPeriod = 200;
        int stochasticPeriod = 14;
        int rsiPeriod = 14;
        int cmfPeriod = 20;
        int williamsRPeriod = 14;

        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
        //    MACDIndicator macdHigh = new MACDIndicator(closePrice, 48, 104);
        EMAIndicator emaMacd = new EMAIndicator(macd, 9);
        //  EMAIndicator emaMacdHigh = new EMAIndicator(macd, 36);
        SMAIndicator shortTermSMA = new SMAIndicator(closePrice, shortTermPeriod);
        SMAIndicator longTermSMA = new SMAIndicator(closePrice, longTermPeriod);
        //StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, stochasticPeriod);
        //StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
        //RSIIndicator rsiIndicator = new RSIIndicator(closePrice, rsiPeriod);
        /*	ChaikinMoneyFlowIndicator cmf = new ChaikinMoneyFlowIndicator(series, cmfPeriod);*/
        //WilliamsRIndicator williamsR = new WilliamsRIndicator(series, williamsRPeriod);


        Rule entryRule = new CrossedDownIndicatorRule(macd, emaMacd)
                .and(new UnderIndicatorRule(sma1, sma2));
          /*      .and(new UnderIndicatorRule(macdHigh, emaMacdHigh))
                .and(new UnderIndicatorRule(shortTermSMA, longTermSMA))
                .and(new UnderIndicatorRule(stochK, stochD))
                .and(new OverIndicatorRule(rsiIndicator, Decimal.valueOf(60)))
                .and(new OverIndicatorRule(stochD, Decimal.valueOf(70)));
//				.and(new OverIndicatorRule(rsiIndicator, Decimal.valueOf(50)))
//				.and(new UnderIndicatorRule(stochK, stochD))
//					.and(new OverIndicatorRule(shortTermSMA, longTermSMA))
//					.and(new UnderIndicatorRule(stochD, Decimal.valueOf(60)));
*/
        Rule exitRule = new OverIndicatorRule(macd, emaMacd)
                .or(new CrossedDownIndicatorRule(sma1, sma2));
        //             .or(new OverIndicatorRule(stochK, stochD))
        //            .or(new OverIndicatorRule(shortTermSMA, longTermSMA));
        //	.or(new CrossedUpIndicatorRule(rsiIndicator, Decimal.valueOf(70)));

        return new BaseStrategy(entryRule, exitRule);
    }


}
