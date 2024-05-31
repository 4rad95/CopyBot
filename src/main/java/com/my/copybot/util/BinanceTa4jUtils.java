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


public class BinanceTa4jUtils {


	public static TimeSeries convertToTimeSeries(
			List<Candlestick> candlesticks, String symbol, String period) {
		List<Bar> ticks = new LinkedList<Bar>();
		for (Candlestick candlestick : candlesticks) {
			ticks.add(convertToTa4jTick(candlestick));
		}
		return new BaseTimeSeries(symbol + "_" + period, ticks);
	}

	public static Bar convertToTa4jTick(Candlestick candlestick) {
		Decimal volume = Decimal.valueOf(0);
		ZonedDateTime closeTime = getZonedDateTime(candlestick.getCloseTime());
		Duration candleDuration = Duration.ofMillis(candlestick.getCloseTime()
				- candlestick.getOpenTime());
		Decimal openPrice = Decimal.valueOf(candlestick.getOpen().substring(1, candlestick.getOpen().length() - 2));
		Decimal closePrice = Decimal.valueOf(candlestick.getClose().substring(1, candlestick.getClose().length() - 2));
		Decimal highPrice = Decimal.valueOf(candlestick.getHigh().substring(1, candlestick.getHigh().length() - 2));
		Decimal lowPrice = Decimal.valueOf(candlestick.getLow().substring(1, candlestick.getLow().length() - 2));
		if (candlestick.getVolume() != null) {
			volume = Decimal.valueOf(candlestick.getVolume().substring(1, candlestick.getVolume().length() - 2));
		} else {
			System.out.println("Volume = null ");
		}

		Decimal amount = Decimal.valueOf(candlestick.getQuoteAssetVolume().substring(1, candlestick.getQuoteAssetVolume().length() - 2));

		return new BaseBar(candleDuration, closeTime, openPrice, highPrice,
				lowPrice, closePrice, volume, amount);
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

	private static Strategy buildMacdStrategyLong(TimeSeries series) {
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
		EMAIndicator emaMacdHigh = new EMAIndicator(macd, 36);
		SMAIndicator shortTermSMA = new SMAIndicator(closePrice, shortTermPeriod);
		SMAIndicator longTermSMA = new SMAIndicator(closePrice, longTermPeriod);
		StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, stochasticPeriod);
		StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
		RSIIndicator rsiIndicator = new RSIIndicator(closePrice, rsiPeriod);


		//	AverageDirectionalMovementIndicator adx = new AverageDirectionalMovementIndicator(series, adxPeriod);
		//	AverageTrueRangeIndicator atr = new AverageTrueRangeIndicator(series, adxPeriod);
		//	ChaikinMoneyFlowIndicator cmf = new ChaikinMoneyFlowIndicator(series, cmfPeriod);
		//#stat	WilliamsRIndicator williamsR = new WilliamsRIndicator(series, williamsRPeriod);

		EMAIndicator sma1 = new EMAIndicator(closePrice, 5);
		EMAIndicator sma2 = new EMAIndicator(closePrice, 10);


		Rule entryRule = new CrossedUpIndicatorRule(macd, emaMacd)
				.and(new OverIndicatorRule(macdHigh, emaMacdHigh))
				.and(new OverIndicatorRule(shortTermSMA, longTermSMA))
				.and(new OverIndicatorRule(stochK, stochD))
				//		.and(new OverIndicatorRule(adx, Decimal.valueOf(25)))
				.and(new OverIndicatorRule(rsiIndicator, Decimal.valueOf(30)))
				.and(new UnderIndicatorRule(stochD, Decimal.valueOf(40)));


		Rule exitRule = new UnderIndicatorRule(macd, emaMacd)
				.or(new UnderIndicatorRule(stochK, stochD))
				.or(new UnderIndicatorRule(shortTermSMA, longTermSMA));


		return new BaseStrategy(entryRule, exitRule);
	}


//	public static Strategy buildStrategyShort(TimeSeries series, String strategyCode) {
//		if (STRATEGY.equals(strategyCode)) {
//			return buildMacdStrategyShort(series);
//		}
//		return null;
//	}

	private static Strategy buildMacdStrategyShort(TimeSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		//	EMAIndicator sma1 = new EMAIndicator(closePrice, 5);
		//	EMAIndicator sma2 = new EMAIndicator(closePrice, 10);
		// Параметризация периодов для индикаторов
		int shortTermPeriod = 50;
		int longTermPeriod = 200;
		int stochasticPeriod = 14;
		int rsiPeriod = 14;
		int cmfPeriod = 20;
		int williamsRPeriod = 14;

		MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
		MACDIndicator macdHigh = new MACDIndicator(closePrice, 48, 104);
		EMAIndicator emaMacd = new EMAIndicator(macd, 9);
		EMAIndicator emaMacdHigh = new EMAIndicator(macd, 36);
		SMAIndicator shortTermSMA = new SMAIndicator(closePrice, shortTermPeriod);
		SMAIndicator longTermSMA = new SMAIndicator(closePrice, longTermPeriod);
		StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, stochasticPeriod);
		StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
		RSIIndicator rsiIndicator = new RSIIndicator(closePrice, rsiPeriod);
		/*	ChaikinMoneyFlowIndicator cmf = new ChaikinMoneyFlowIndicator(series, cmfPeriod);*/
		WilliamsRIndicator williamsR = new WilliamsRIndicator(series, williamsRPeriod);
		StochasticRSIIndicator dd = new StochasticRSIIndicator(series, 19);

		Rule entryRule = new CrossedDownIndicatorRule(macd, emaMacd)
				.and(new UnderIndicatorRule(macdHigh, emaMacdHigh))
				.and(new UnderIndicatorRule(shortTermSMA, longTermSMA))
				.and(new UnderIndicatorRule(stochK, stochD))
				.and(new OverIndicatorRule(rsiIndicator, Decimal.valueOf(60)))
				.and(new OverIndicatorRule(stochD, Decimal.valueOf(70)));
//				.and(new OverIndicatorRule(rsiIndicator, Decimal.valueOf(50)))
//				.and(new UnderIndicatorRule(stochK, stochD))
//					.and(new OverIndicatorRule(shortTermSMA, longTermSMA))
//					.and(new UnderIndicatorRule(stochD, Decimal.valueOf(60)));

		Rule exitRule = new OverIndicatorRule(macd, emaMacd)
				.or(new OverIndicatorRule(stochK, stochD))
				.or(new OverIndicatorRule(shortTermSMA, longTermSMA));
		//	.or(new CrossedUpIndicatorRule(rsiIndicator, Decimal.valueOf(70)));

		return new BaseStrategy(entryRule, exitRule);
	}



}