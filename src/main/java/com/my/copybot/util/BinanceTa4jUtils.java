package com.my.copybot.util;

import com.binance.api.client.domain.market.Candlestick;
import org.ta4j.core.*;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.StochasticRSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

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
		Decimal amount = Decimal.valueOf(0);
		ZonedDateTime closeTime = getZonedDateTime(candlestick.getCloseTime());
		Duration candleDuration = Duration.ofMillis(candlestick.getCloseTime()
				- candlestick.getOpenTime());
		Decimal openPrice = Decimal.valueOf(candlestick.getOpen().substring(1, candlestick.getOpen().length() - 2));
		Decimal closePrice = Decimal.valueOf(candlestick.getClose().substring(1, candlestick.getClose().length() - 2));
		Decimal highPrice = Decimal.valueOf(candlestick.getHigh().substring(1, candlestick.getHigh().length() - 2));
		Decimal lowPrice = Decimal.valueOf(candlestick.getLow().substring(1, candlestick.getLow().length() - 2));
		try {
			volume = Decimal.valueOf(candlestick.getVolume().substring(1, candlestick.getVolume().length() - 2));
		} catch (Exception e) {
			volume = Decimal.valueOf(0.00);
		}
		try {
			amount = Decimal.valueOf(candlestick.getQuoteAssetVolume().substring(1, candlestick.getVolume().length() - 2));
		} catch (Exception e) {
			amount = Decimal.valueOf(0.00);
		}

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

	public static Boolean checkStrategyLong(TimeSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		RSIIndicator rsi = new RSIIndicator(closePrice, 14);
		StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
		SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
		SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3);
		int maxIndex = series.getEndIndex();


		return smoothedStochRsi.getValue(maxIndex).compareTo(stochRsiD.getValue(maxIndex)) > 0;
//				&& smoothedStochRsi.getValue(maxIndex - 1).compareTo(stochRsiD.getValue(maxIndex - 1)) < 0
		//		&& smoothedStochRsi.getValue(maxIndex).multipliedBy(100).intValue() < 50
		//          && smoothedStochRsi.getValue(maxIndex).compareTo(smoothedStochRsi.getValue(maxIndex - 1)) > 0;
	}


	public static Boolean checkStrategyShort(TimeSeries series) {
		if (series == null) {
			throw new IllegalArgumentException("Series cannot be null");
		}
		ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
		RSIIndicator rsi = new RSIIndicator(closePrice, 14);
		StochasticRSIIndicator stochRsi = new StochasticRSIIndicator(rsi, 14);
		SMAIndicator smoothedStochRsi = new SMAIndicator(stochRsi, 3);
		SMAIndicator stochRsiD = new SMAIndicator(smoothedStochRsi, 3);
		int maxIndex = series.getEndIndex();

		return smoothedStochRsi.getValue(maxIndex).compareTo(stochRsiD.getValue(maxIndex)) < 0;
		//			&& smoothedStochRsi.getValue(maxIndex - 1).multipliedBy(100).intValue() > 50
                //&& smoothedStochRsi.getValue(maxIndex - 1).compareTo(stochRsiD.getValue(maxIndex - 2)) > 0
		//             && smoothedStochRsi.getValue(maxIndex).compareTo(smoothedStochRsi.getValue(maxIndex - 1)) < 0;
	}

}