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

	public static String MACD_STRATEGY = "MACD";

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

	public static Strategy buildStrategyLong(TimeSeries series, String strategyCode) {
		if (MACD_STRATEGY.equals(strategyCode)) {
			return buildMacdStrategyLong(series);
		}
		return null;
	}

	private static Strategy buildMacdStrategyLong(TimeSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

	/*	MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
		EMAIndicator emaMacd = new EMAIndicator(macd, 9);
		SMAIndicator shortTermSMA = new SMAIndicator(closePrice, 50);
		SMAIndicator longTermSMA = new SMAIndicator(closePrice, 100);   // 100

		StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, 14);
		StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
		RSIIndicator rsiIndicator = new RSIIndicator(closePrice, 14);


		// Индикаторы из разных категорий
		AverageDirectionalMovementIndicator adx = new AverageDirectionalMovementIndicator(series, 14);
		AverageTrueRangeIndicator atr = new AverageTrueRangeIndicator(series, 14);
		ChaikinMoneyFlowIndicator cmf = new ChaikinMoneyFlowIndicator(series, 20);
		//	MoneyFlowIndicator mfi = new MoneyFlowIndicator(series, 14);
		WilliamsRIndicator williamsR = new WilliamsRIndicator(series, 14);*/

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


		//AverageDirectionalMovementIndicator adx = new AverageDirectionalMovementIndicator(series, adxPeriod);
		//	AverageTrueRangeIndicator atr = new AverageTrueRangeIndicator(series, adxPeriod);
		//	ChaikinMoneyFlowIndicator cmf = new ChaikinMoneyFlowIndicator(series, cmfPeriod);
		WilliamsRIndicator williamsR = new WilliamsRIndicator(series, williamsRPeriod);

		EMAIndicator sma1 = new EMAIndicator(closePrice, 5);
		EMAIndicator sma2 = new EMAIndicator(closePrice, 10);


		Rule entryRule = new CrossedUpIndicatorRule(macd, emaMacd)
				.and(new OverIndicatorRule(macdHigh, emaMacdHigh))
				.and(new OverIndicatorRule(shortTermSMA, longTermSMA))
				.and(new OverIndicatorRule(stochK, stochD))
				//		.and(new OverIndicatorRule(adx, Decimal.valueOf(25)))
				.and(new OverIndicatorRule(rsiIndicator, Decimal.valueOf(30)))
				.and(new UnderIndicatorRule(stochD, Decimal.valueOf(60)));
//				.and(new OverIndicatorRule(rsiIndicator, Decimal.valueOf(50)))
//				.and(new UnderIndicatorRule(stochK, stochD))
//					.and(new OverIndicatorRule(shortTermSMA, longTermSMA))
//					.and(new UnderIndicatorRule(stochD, Decimal.valueOf(60)));


		//		.and(new UnderIndicatorRule(cmf, Decimal.ZERO))
				//			.and(new OverIndicatorRule(atr, closePrice.getValue(2)))
		//		.and(new OverIndicatorRule(williamsR, Decimal.valueOf(-20)));  // Add ich

		/*		.and(new UnderIndicatorRule(cmf, Decimal.ZERO))
				//			.and(new OverIndicatorRule(mfi, Decimal.valueOf(20)))
				.and(new UnderIndicatorRule(williamsR, Decimal.valueOf(-20)));*/

		Rule exitRule = new UnderIndicatorRule(macd, emaMacd)
				.or(new UnderIndicatorRule(stochD, stochK))
				.or(new UnderIndicatorRule(sma1, sma2))
				.or(new CrossedDownIndicatorRule(rsiIndicator, Decimal.valueOf(30)));
		//	.or(new UnderIndicatorRule(adx, Decimal.valueOf(20)))
			//	.or(new UnderIndicatorRule(rsiIndicator, Decimal.valueOf(70)))
		//	.or(new UnderIndicatorRule(williamsR, Decimal.valueOf(-80)));
        //.or(new UnderIndicatorRule(atr, closePrice.getValue(0) .multipliedBy(Decimal.valueOf(0.5)))); */
/*		double willya =  (williamsR.getValue(12).toDouble()+ williamsR.getValue(13).toDouble()+ williamsR.getValue(14).toDouble())/3;

		if (williamsR.getValue(14).toDouble() > -10 ) {
			System.out.println(series.getName() + "  SHORT  Will [12] = "+williamsR.getValue(12) +
					" Will [13] = " + williamsR.getValue(0) + "  Will [14] = "+williamsR.getValue(14) 	);}
		if (williamsR.getValue(14).toDouble() < -80 ) {
			System.out.println(series.getName() + "  LONG  Will [12] = "+williamsR.getValue(12) +
					" Will [13] = " + williamsR.getValue(0) + "  Will [14] = "+williamsR.getValue(14) 	);}
		if (willya > -10 ) {
			System.out.println(series.getName() + "  SHORT  " + willya);
		}
		if (willya < -80 ) {
			System.out.println(series.getName() + "  LONG  " + willya);
		}*/


//		Rule entryRule = null;
//	/*	if (williamsR.getValue(14).toDouble() < -80) {
//			entryRule = new CrossedUpIndicatorRule(sma1, sma2)
//					.and(new OverIndicatorRule(rsiIndicator, Decimal.valueOf(50)))
//					.and(new UnderIndicatorRule(stochK, stochD))
//					.and(new OverIndicatorRule(shortTermSMA, longTermSMA))
//					.and(new UnderIndicatorRule(stochD, Decimal.valueOf(60)));
//		} else {*/
//			entryRule = new CrossedUpIndicatorRule(sma1, sma2)
//					.and(new OverIndicatorRule(rsiIndicator, Decimal.valueOf(80)))
//					.and(new UnderIndicatorRule(stochK, stochD))
//					.and(new OverIndicatorRule(shortTermSMA, longTermSMA))
//					.and(new UnderIndicatorRule(williamsR, Decimal.valueOf(-80)))
//					.and(new UnderIndicatorRule(stochD, Decimal.valueOf(20)));
//		//	}
//		Rule exitRule = //new CrossedDownIndicatorRule(sma1, sma2)
//				new UnderIndicatorRule(sma1, sma2)
//						.or(new CrossedDownIndicatorRule(rsiIndicator, Decimal.valueOf(50)));

		return new BaseStrategy(entryRule, exitRule);
	}



	public static Strategy buildStrategyShort(TimeSeries series, String strategyCode) {
		if (MACD_STRATEGY.equals(strategyCode)) {
			return buildMacdStrategyShort(series);
		}
		return null;
	}

	private static Strategy buildMacdStrategyShort(TimeSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}

		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		EMAIndicator sma1 = new EMAIndicator(closePrice, 5);
		EMAIndicator sma2 = new EMAIndicator(closePrice, 10);
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


		Rule entryRule = new CrossedDownIndicatorRule(macd, emaMacd)
				.and(new UnderIndicatorRule(macdHigh, emaMacdHigh))
				.and(new UnderIndicatorRule(shortTermSMA, longTermSMA))
				.and(new UnderIndicatorRule(stochK, stochD))
				.and(new UnderIndicatorRule(rsiIndicator, Decimal.valueOf(30)))
				.and(new OverIndicatorRule(stochD, Decimal.valueOf(70)));
//				.and(new OverIndicatorRule(rsiIndicator, Decimal.valueOf(50)))
//				.and(new UnderIndicatorRule(stochK, stochD))
//					.and(new OverIndicatorRule(shortTermSMA, longTermSMA))
//					.and(new UnderIndicatorRule(stochD, Decimal.valueOf(60)));


		//		.and(new UnderIndicatorRule(cmf, Decimal.ZERO))
		//			.and(new OverIndicatorRule(atr, closePrice.getValue(2)))
		//		.and(new OverIndicatorRule(williamsR, Decimal.valueOf(-20)));  // Add ich

		/*		.and(new UnderIndicatorRule(cmf, Decimal.ZERO))
				//			.and(new OverIndicatorRule(mfi, Decimal.valueOf(20)))
				.and(new UnderIndicatorRule(williamsR, Decimal.valueOf(-20)));*/

		Rule exitRule = new OverIndicatorRule(macd, emaMacd)
				.or(new OverIndicatorRule(stochD, stochK))
				.or(new OverIndicatorRule(sma1, sma2))
				.or(new CrossedUpIndicatorRule(rsiIndicator, Decimal.valueOf(70)));

//		Rule entryRule = null;
//			entryRule = new CrossedDownIndicatorRule(sma1, sma2)
//					.and(new UnderIndicatorRule(rsiIndicator, Decimal.valueOf(20)))
//					.and(new UnderIndicatorRule(stochK, stochD))
//					.and(new OverIndicatorRule(longTermSMA, shortTermSMA))
//					.and(new OverIndicatorRule(williamsR, Decimal.valueOf(-20)))
//					.and(new OverIndicatorRule(stochD, Decimal.valueOf(80)));
//

//		Rule exitRule = //new CrossedDownIndicatorRule(sma1, sma2)
//				new OverIndicatorRule(sma1, sma2)    // ?
//						.or(new CrossedUpIndicatorRule(rsiIndicator, Decimal.valueOf(50)));

		/*	MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);
		EMAIndicator emaMacd = new EMAIndicator(macd, 9);
		SMAIndicator shortTermSMA = new SMAIndicator(closePrice, 50);
		SMAIndicator longTermSMA = new SMAIndicator(closePrice, 200); //100

		StochasticOscillatorKIndicator stochK = new StochasticOscillatorKIndicator(series, 14);
		StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
		RSIIndicator rsiIndicator = new RSIIndicator(closePrice, 14);


		// Индикаторы из разных категорий
		ChaikinMoneyFlowIndicator cmf = new ChaikinMoneyFlowIndicator(series, 20);
		//MoneyFlowIndicator mfi = new MoneyFlowIndicator(series, 14);
		WilliamsRIndicator williamsR = new WilliamsRIndicator(series, 14);

		// Правила входа и выхода
		Rule entryRule = new CrossedDownIndicatorRule(emaMacd, macd)
				.and(new OverIndicatorRule(longTermSMA, shortTermSMA))   // 	.and(new OverIndicatorRule(longTermSMA, shortTermSMA))
				.and(new UnderIndicatorRule(stochD, stochK))
		//		.and(new OverIndicatorRule(cmf, Decimal.ZERO))          // OverIndicatorRule(cmf, Decimal.ZERO)
				//			.and(new OverIndicatorRule(mfi, Decimal.valueOf(20)))
				.and(new UnderIndicatorRule(williamsR, Decimal.valueOf(-80)));

		Rule exitRule = new CrossedUpIndicatorRule(macd, emaMacd)
				.or(new CrossedUpIndicatorRule(stochK, stochD))
		//		.or(new UnderIndicatorRule(cmf, Decimal.ZERO))
				//.or(new UnderIndicatorRule(mfi, Decimal.valueOf(80)))
				.or(new OverIndicatorRule(williamsR, Decimal.valueOf(-20))); */

		return new BaseStrategy(entryRule, exitRule);
	}


	private static void StochasticRSIIndicator(TimeSeries series, int i) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}