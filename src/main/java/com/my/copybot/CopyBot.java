/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.my.copybot;

/**
 *
 * @author radomir
 */

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.my.copybot.exceptions.GeneralException;
import com.my.copybot.model.Position;
import com.my.copybot.trading.TradeTask;
import com.my.copybot.util.BinanceTa4jUtils;
import com.my.copybot.util.BinanceUtils;
import com.my.copybot.util.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.ta4j.core.Decimal;
import org.ta4j.core.Strategy;
import org.ta4j.core.Tick;
import org.ta4j.core.TimeSeries;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static java.lang.System.currentTimeMillis;

public class CopyBot {

	// Config params
	private static Integer PAUSE_TIME_MINUTES = 5;
	private static Boolean DO_TRADES = true;
	private static Integer MAX_SIMULTANEOUS_TRADES = 0;
	private static Double TRADE_SIZE_BTC;
    private static Double TRADE_SIZE_USDT;
	private static Double STOPLOSS_PERCENTAGE = 1.00;
	private static Boolean DO_TRAILING_STOP = false;
	private static String TRADING_STRATEGY;

	public static Boolean MAKE_LONG = true;
	public static Boolean MAKE_SHORT = true;
	public static Boolean MAKE_TRADE_AVG = true;
	public static String BLACK_LIST = "";
	public static Integer STOP_NO_LOSS = 100;
	public static Long timer = currentTimeMillis();

	// We will store time series for every symbol
	private static final Map<String, TimeSeries> timeSeriesCache = new HashMap<String, TimeSeries>();

	private static final Map<String, TradeTask> openTradesLong = new HashMap<String, TradeTask>();
	private static final Map<String, TradeTask> openTradesShort = new HashMap<String, TradeTask>();
        
	private static final List<String> ordersToBeClosed = new LinkedList<String>();
	private static final List<Position> closedPositions = new LinkedList<Position>();

	private static BinanceApiRestClient client;
	private static BinanceApiWebSocketClient liveClient;

	private static Integer closedTrades = 0;
	private static Double totalProfit = 0.0;

	private static final List<String> badSymbols = new LinkedList<String>();
	private static Double totalProfitLong = 0.0;
	private static Integer closedTradesLong = 0;
	private static Double totalProfitShort = 0.0;
	private static Integer closedTradesShort = 0;

        
	private static CandlestickInterval interval = null;
	private static BigDecimal startBalance;
        
        

	public static void main(String[] args) throws IOException {
                    
               
		Log.info(CopyBot.class, "Initializing Binance bot");
		String configFilePath = System.getProperty("CONFIG_FILE_PATH");
		Log.info(CopyBot.class,
				"=== Detected config file path (VM argument, optional): "
						+ configFilePath + " ===");
		if (StringUtils.isNotEmpty(configFilePath)) {
			ConfigUtils.setSystemConfigFilePath(configFilePath);
		}
//                badSymbols.add("SCUSDT");
//                badSymbols.add("USDCUSDT");
		init();
		process();
	}
	
	public static void init() throws IOException {
		// Pause time
		String strPauseTimeMinutes = ConfigUtils
				.readPropertyValue(ConfigUtils.CONFIG_PAUSE_TIME_MINUTES);
		if (StringUtils.isNotEmpty(strPauseTimeMinutes)
				&& StringUtils.isNumeric(strPauseTimeMinutes)) {
			PAUSE_TIME_MINUTES = Integer.valueOf(strPauseTimeMinutes);
		}

		// Candle time frame
		String candleInterval = ConfigUtils
				.readPropertyValue(ConfigUtils.CONFIG_BINANCE_TICK_INTERVAL);
		CandlestickInterval[] intervals = CandlestickInterval.values();
		for (CandlestickInterval _interval : intervals) {
			if (_interval.getIntervalId().equalsIgnoreCase(candleInterval)) {
				Log.info(CopyBot.class, "Setting candlestick interval to: "
						+ candleInterval);
				interval = _interval;
			}
		}
		if (interval == null) {
			interval = CandlestickInterval.FOUR_HOURLY;
			Log.info(CopyBot.class, "Using default candlestick interval: "
					+ CandlestickInterval.FOUR_HOURLY.getIntervalId());
		}

		// Trading settings
		String strDoTrades = ConfigUtils
				.readPropertyValue(ConfigUtils.CONFIG_TRADING_DO_TRADES);
		if ("false".equalsIgnoreCase(strDoTrades) || "0".equals(strDoTrades)) {
			DO_TRADES = false;
		}
		if (DO_TRADES) {
			MAX_SIMULTANEOUS_TRADES = Integer
					.valueOf(ConfigUtils
							.readPropertyValue(ConfigUtils.CONFIG_TRADING_MAX_SIMULTANEOUS_TRADES));
			STOPLOSS_PERCENTAGE = Double
					.valueOf(ConfigUtils
							.readPropertyValue(ConfigUtils.CONFIG_TRADING_STOPLOSS_PERCENTAGE));
			TRADE_SIZE_USDT = Double
					.valueOf(ConfigUtils
							.readPropertyValue(ConfigUtils.CONFIG_TRADING_TRADE_SIZE_USDT));
                        TRADE_SIZE_BTC = Double
					.valueOf(ConfigUtils
							.readPropertyValue(ConfigUtils.CONFIG_TRADING_TRADE_SIZE_BTC));
                      
			String strDoTrailingStop = ConfigUtils
					.readPropertyValue(ConfigUtils.CONFIG_TRADING_DO_TRAILING_STOP);
			if ("true".equalsIgnoreCase(strDoTrailingStop)
					|| "1".equals(strDoTrailingStop)) {
				DO_TRAILING_STOP = true;
			}
			TRADING_STRATEGY = ConfigUtils
					.readPropertyValue(ConfigUtils.CONFIG_TRADING_STRATEGY);
                        
                        String makeLong= ConfigUtils
					.readPropertyValue(ConfigUtils.CONFIG_TRADING_LONG);
            MAKE_LONG = "true".equalsIgnoreCase(makeLong)
                    || "1".equals(makeLong);
                                
                        String makeShort= ConfigUtils
					.readPropertyValue(ConfigUtils.CONFIG_TRADING_SHORT);
            MAKE_SHORT = "true".equalsIgnoreCase(makeLong)
                    || "1".equals(makeLong);
                        String makeAvg= ConfigUtils
					.readPropertyValue(ConfigUtils.CONFIG_TRADING_AVRG);
            MAKE_TRADE_AVG = "true".equalsIgnoreCase(makeLong)
                    || "1".equals(makeLong);
                        BLACK_LIST = ConfigUtils
					.readPropertyValue(ConfigUtils.CONFIG_TRADING_BLACKLIST);
						String strStopNoLoss = ConfigUtils
					.readPropertyValue(ConfigUtils.CONFIG_TRADING_STOPNOLOSS);
						STOP_NO_LOSS = Integer.valueOf(strStopNoLoss);
                }
		try {
			BinanceUtils.init(ConfigUtils.readPropertyValue(ConfigUtils.CONFIG_BINANCE_API_KEY),
					ConfigUtils.readPropertyValue(ConfigUtils.CONFIG_BINANCE_API_SECRET));
			client = BinanceUtils.getRestClient();
			liveClient = BinanceUtils.getWebSocketClient();
			startBalance = printBalance();
			Runnable InputString = new InputString();
			Thread thread = new Thread(InputString);
			//InputString.thisThread = thread;
			thread.start();
		} catch (GeneralException e) {
			Log.severe(CopyBot.class, "Unable to generate Binance clients!", e);
		}
	}
        
	public static void process() {
		try {
                        
			List<String> symbols = BinanceUtils.getBitcoinSymbols();
                        symbols = blackListCheck(symbols);
			// 1.- Get ticks for every symbol and generate TimeSeries -> cache
			generateTimeSeriesCache(symbols);
			Long timeToWait = PAUSE_TIME_MINUTES * 60 * 1000L;
			if (timeToWait < 0) {
				timeToWait = 5 * 60 * 1000L;
			}
			while (true) {
                                //Thread.getAllStackTraces().keySet(); 
				if (DO_TRADES) {
					Long t0 = currentTimeMillis();

					int seconds = (int) ((t0 - timer) / 1000);
					int minutes = seconds / 60;
					int hours = minutes / 60;
					minutes = minutes - hours * 60;
					seconds = seconds - minutes * 60;
					String formattedTime = String.format("%d:%02d:%02d", hours, minutes, seconds);
					Log.info(CopyBot.class, "--------------------------------------------------------------------------------------------------------------------");
					Log.info(CopyBot.class, "\u001B[36m CopyBot 1.00001 Bis  Rad creating. Made to make money!!!!  \u001B[0m");
					Log.info(CopyBot.class," Open trades LONG: " + openTradesLong.keySet().size() +" SHORT:" + openTradesShort.keySet().size());
					Log.info(CopyBot.class," LONG:  " + openTradesLong.keySet());
					Log.info(CopyBot.class," SHORT: " + openTradesShort.keySet());
					Log.info(CopyBot.class, "--------------------------------------------------------------------------------------------------------------------");
					Log.info(CopyBot.class, "\u001B[32m Start time       : " + new Date(timer) + " \u001B[0m ");
					Log.info(CopyBot.class, "\u001B[32m Execute time     : " + formattedTime + " \u001B[0m ");
					Log.info(CopyBot.class, "\u001B[32m Start Balance    : " + startBalance + "               Current  Balance : " + printBalance() + " \u001B[0m ");
					Log.info(CopyBot.class, "--------------------------------------------------------------------------------------------------------------------");
					if (DO_TRADES && closedTrades > 0) {
                                                
						Log.info(
								CopyBot.class,
								"\u001B[32mClosed trades: " + closedTrades + " Long: " + closedTradesLong + " Short: " + closedTradesShort
										+ ", total profit: " + String.format("%.8f", totalProfit)
										+ ", LONG: " + String.format("%.2f", totalProfitLong)
										+ ", SHORT: " + String.format("%.2f", totalProfitShort) + "\u001B[0m ");
						Log.info(CopyBot.class, "--------------------------------------------------------------------------------------------------------------------");
                                                
					}
					if ((openTradesLong.keySet().size()+openTradesShort.keySet().size()) >= MAX_SIMULTANEOUS_TRADES) {
						// We will not continue trading... avoid checking
						
						try {
							Thread.sleep(timeToWait);
						} catch (InterruptedException e) {
							Log.severe(CopyBot.class, "Error sleeping", e);
						}
						continue;
					}
				}
				Long t0 = currentTimeMillis();
				// 2.- Get two last ticks for symbol and update cache.
				for (String symbol : symbols) {
					try {
						checkSymbol(symbol);
					} catch (Exception e) {
						Log.severe(CopyBot.class, "Error checking symbol "
								+ symbol, e);
					}
				}
				Long t1 = currentTimeMillis() - t0;
				Log.info(CopyBot.class, "All symbols analyzed, time elapsed: "
						+ (t1 / 1000.0) + " seconds.");

				try {
					Thread.sleep(timeToWait);
				} catch (InterruptedException e) {
					Log.severe(CopyBot.class, "Error sleeping", e);
				}
			}
		} catch (Exception e) {
			Log.severe(CopyBot.class, "Unable to get symbols", e);
		}
	}
	
	private static void checkSymbol(String symbol) {
                
		if (check(symbol)){
                // Log.debug(CopyBotSpot.class, "Checking symbol: " + symbol);
			Long t0 = currentTimeMillis();
		try {
			List<Candlestick> latestCandlesticks = BinanceUtils.getLatestCandlestickBars(symbol, interval);
			TimeSeries series = timeSeriesCache.get(symbol);
			if (BinanceTa4jUtils.isSameTick(latestCandlesticks.get(1), series.getLastTick())) {
				// We are still in the same tick - just update the last tick with the fresh data
				updateLastTick(symbol, latestCandlesticks.get(1));
			} else {
				// We have just got a new tick - update the previous one and include the new tick
				updateLastTick(symbol, latestCandlesticks.get(0));
				series.addTick(BinanceTa4jUtils.convertToTa4jTick(latestCandlesticks.get(1)));
			}
			// Now check the TA strategy with the refreshed time series
			int endIndex = series.getEndIndex();
			Strategy strategyLong = BinanceTa4jUtils.buildStrategyLong(series, TRADING_STRATEGY);
			Strategy strategyShort = BinanceTa4jUtils.buildStrategyShort(series, TRADING_STRATEGY);

			checkStrategy(strategyLong, strategyShort, endIndex, symbol);
                        

			if (strategyLong.shouldEnter(endIndex)) {

				//		Decimal checkRSIStr = BinanceTa4jUtils.StochasticRSIIndicatorTest(series, 14);
				// If we have an open trade for the symbol, we do not create a new one
				if (DO_TRADES && openTradesLong.get(symbol) == null&& (MAKE_LONG)) {
					Decimal currentPrice = series.getLastTick().getClosePrice();
					//        MainForm.addStringTextEdit("Bullish signal for symbol: \" + symbol + \", price: \" + currentPrice)");
					//Log.info(CopyBotSpot.class, "LONG signal for symbol: " + symbol + ", price: " + currentPrice);
					//Order newOrder = new Order(symbol,currentPrice);
					// newOrder.addNewOrder(symbol,currentPrice);
					//if (false) {
                                        if (((openTradesLong.keySet().size()+openTradesShort.keySet().size()) < MAX_SIMULTANEOUS_TRADES))                                       
                                        {

											// We create a new thread to trade with the symbol

											// We create a new position to trade with the symbol
											addTrade(symbol, "LONG");
											//	openTradesLong.put(symbol, "LONG";

					} else {
					//	Log.info(CopyBotSpot.class, "-------------Skipping LONG signal for symbol  " + symbol + " Wait!" );
					}
				}
			} else if (strategyShort.shouldEnter(endIndex)) //&& openTrades.get(symbol) != null && !DO_TRAILING_STOP)
                                {
									// If we use trailing stop, the order will be closed when the moving stoploss is hit
									//	Log.info(CopyBotSpot.class, "SHORT signal for symbol: " + symbol + ", price: " + series.getLastTick().getClosePrice()
									//	);
				// This object is scanned by the symbol trading thread
									// ordersToBeClosed.add(symbol);

									if (DO_TRADES && openTradesShort.get(symbol) == null && MAKE_SHORT) {
										Decimal currentPrice = series.getLastTick().getClosePrice();

										Log.info(CopyBot.class, "SHORT signal for symbol: " + symbol + ", price: " + currentPrice);

										if (((openTradesLong.keySet().size() + openTradesShort.keySet().size()) < MAX_SIMULTANEOUS_TRADES)) {

											// We create a new thread to short trade with the symbol

											// We create a new position to trade with the symbol
											addTrade(symbol, "SHORT");
											//		openTradesShort.put(symbol, tradeTask);
										}
									}
                                }

			//	Log.debug(CopyBotSpot.class, "Symbol " + symbol + " checked in " + ((System.currentTimeMillis() - t0) / 1000.0) + " seconds");
		} catch (GeneralException e) {
			Log.severe(CopyBot.class, "Unable to check symbol " + symbol + "Error: " + e);
		}
	}}
	
	private static void updateLastTick(String symbol, Candlestick candlestick) {
		TimeSeries series = timeSeriesCache.get(symbol);
		List<Tick> seriesTick = series.getTickData();
		seriesTick.remove(series.getEndIndex());
		seriesTick.add(BinanceTa4jUtils.convertToTa4jTick(candlestick));
	}
	
	private static void generateTimeSeriesCache(List<String> symbols) {
		for (String symbol : symbols) {
                    if (check(symbol)){
			Log.info(CopyBot.class, "Generating time series for " + symbol);
			try {
				List<Candlestick> candlesticks = BinanceUtils.getCandlestickBars(symbol, interval);
				TimeSeries series = BinanceTa4jUtils.convertToTimeSeries(candlesticks, symbol, interval.getIntervalId());
				timeSeriesCache.put(symbol, series);
			} catch (Exception e) {
				// Log.severe(CopyBotSpot.class, "Unable to generate time series / strategy for " + symbol, e);
				System.out.println("\u001B[32m" + symbol + "  -  Not used symbol !!! \u001B[0m");
				badSymbols.add(symbol);
			}}
		}
	}

	/**
	 * The open thread invokes this method to mark an order as closed
	 * @param symbol
	 * @param profit
	 * @param errorMessage
	 */
	public static void closeOrder(String symbol, Double profit, String errorMessage, String type) // 0-short, 1-long
               
                {
                int delta=0;
		if (StringUtils.isNotEmpty(errorMessage)) {
                        
			Log.info(CopyBot.class, "Trade " + symbol + " is closed due to error: " + errorMessage);
                        badSymbols.add(symbol);
                        
		} else {
			Log.info(CopyBot.class, "[Close]------- Trade " + symbol + " is closed with profit: " + String.format("%.8f", profit));
                        delta=1;
                        
                }
               // if (openTradesLong.containsKey(symbol)){
             if (profit == null ){profit = 0.00;}
					if (type.equals("SHORT")) {
                        closedTradesShort+=delta;
                        totalProfitShort +=profit;
					} else {
                        closedTradesLong+=delta;
                        totalProfitLong +=profit;
             
             }
                        closedTrades+=delta;
                        totalProfit += profit;
                        ordersToBeClosed.remove(symbol);
                         
                        openTradesLong.remove(symbol);
                        openTradesShort.remove(symbol);
        }

	/**
	 * The open thread invokes this method to check if an order should be closed
	 * @param symbol
	 * @return if it should be closed or not
	 */
	public static boolean shouldCloseOrder(String symbol) {
        return ordersToBeClosed.contains(symbol);
    }
        
        public static boolean check(String symbol){

            for (String badSymbol:badSymbols){
                if (badSymbol == symbol)
                        return false;
            }
            return true;
        }

	public static boolean checkOpenOrder(String Symbol){
        
            
            return false;
        }
        
        
        public static List<String> blackListCheck(List<String> symbols){
        String[] badSymbols = BLACK_LIST.split(",");
        for (int i=0; i < symbols.size();i++){
        for (int j=0;j<badSymbols.length;j++){
                if (symbols.get(i).equalsIgnoreCase(badSymbols[j])){
                        symbols.remove(i);
                        break;
                }}
            }
        return symbols;
        }
        
        private static void checkStrategy(Strategy strategyLong,Strategy strategyShort, int endIndex,String symbol){
            if (strategyShort.shouldEnter(endIndex)||strategyLong.shouldExit(endIndex)){
                    if (null != openTradesLong.get(symbol))
                    {
                        ordersToBeClosed.add(symbol);
						Log.info(CopyBot.class, "\u001B[33m [Close]  Close strategy for symbol = " + symbol + "\u001B[0m");
                    }}
            
            else if (strategyShort.shouldExit(endIndex)||strategyLong.shouldEnter(endIndex)){
                    if (null != openTradesShort.get(symbol))
                    {
                        ordersToBeClosed.add(symbol);
						Log.info(CopyBot.class, "\u001B[33m [Close]  Close strategy for symbol = " + symbol + "\u001B[0m");
                    }}
        }
        private static BigDecimal printBalance()
        {
                RequestOptions options = new RequestOptions();
        		SyncRequestClient syncRequestClient = SyncRequestClient.create( BinanceUtils.getApiKey(), BinanceUtils.getApiSecret(),
                options);


			return  syncRequestClient.getBalance().get(6).getBalance();
                
        }

	public static void codeInput(String inputString) throws IOException {
		if (inputString.equals("END")) {
			closeAllOrders();
			MAX_SIMULTANEOUS_TRADES = 0;
		} else if (inputString.equals("CHK")) {
			System.out.println("Used value MAX_SIMULTANEOUS_TRADES = " + MAX_SIMULTANEOUS_TRADES);
			System.out.println("Used value TRADE_SIZE_USDT = " + TRADE_SIZE_USDT);
			System.out.println("Used value STOPLOSS_PERCENTAGE = " + STOPLOSS_PERCENTAGE);
			System.out.println("Used value STOP_NO_LOSS = " + STOP_NO_LOSS);
		} else if (inputString.equals("STOP")) {
			MAX_SIMULTANEOUS_TRADES = 0;
		} else if (inputString.equals("STAT")) {
			outputPositionClosed(closedPositions);
		} else if (inputString.charAt(0) == 'D') {
			ordersToBeClosed.add(inputString.substring(1) + "USDT");
		} else {
			String inputTemp = inputString.substring(0, 2);
			inputString.substring(2);
			if (inputTemp.equals("TC")) {
				MAX_SIMULTANEOUS_TRADES = Integer.parseInt(inputString.substring(2));
				System.out.println("New value MAX_SIMULTANEOUS_TRADES = " + MAX_SIMULTANEOUS_TRADES);
			} else if (inputTemp.equals("OR")) {
				TRADE_SIZE_USDT = Double.parseDouble(inputString.substring(2));
				System.out.println("New value TRADE_SIZE_USDT = " + TRADE_SIZE_USDT);
			} else if (inputTemp.equals("SL")) {
				STOPLOSS_PERCENTAGE = Double.parseDouble(inputString.substring(2));
				System.out.println("New value STOPLOSS_PERCENTAGE = " + STOPLOSS_PERCENTAGE);
			} else if (inputTemp.equals("SN")) {
				STOP_NO_LOSS = Integer.parseInt(inputString.substring(2));
				System.out.println("New value STOP_NO_LOSS = " + STOP_NO_LOSS);
			} else if (inputTemp.equals("AL")) {
				System.out.println("Add new position LONG ..... " + inputString.substring(2) + "USDT");
				addTrade(inputString.substring(2) + "USDT", "LONG");
			} else if (inputTemp.equals("AS")) {
				System.out.println("Add new position SHORT  ..... " + inputString.substring(2) + "USDT");
				addTrade(inputString.substring(2) + "USDT", "SHORT");
			} else if (inputTemp.equals("RP")) {
				System.out.println("Remove position in list (WARNING!!! Position not closed !!)  ..... " + inputString.substring(2) + "USDT");
				clearPosition(inputString.substring(2) + "USDT");
			}

		}
	}

	public static void closeAllOrders() {
		for (String symbol : openTradesLong.keySet()) {
			ordersToBeClosed.add(symbol);
		}
		for (String symbol : openTradesShort.keySet()) {
			ordersToBeClosed.add(symbol);
		}
	}

	public static void addTrade(String symbol, String type) {

		TradeTask tradeTask = new TradeTask(client, liveClient, symbol, getCurrentPrice(symbol).toDouble(),
				TRADE_SIZE_BTC, TRADE_SIZE_USDT, STOPLOSS_PERCENTAGE, DO_TRAILING_STOP, MAKE_TRADE_AVG, STOP_NO_LOSS, type);
		Thread thread = new Thread(tradeTask);
		tradeTask.thisThread = thread;
		thread.start();
		switch (type) {
			case "LONG": {
				openTradesLong.put(symbol, tradeTask);
				break;
			}
			case "SHORT": {
				openTradesShort.put(symbol, tradeTask);
				break;
			}
		}
	}

	public static Decimal getCurrentPrice(String symbol) {

		TimeSeries series = timeSeriesCache.get(symbol);
		Decimal currentPrice = series.getLastTick().getClosePrice();
		return currentPrice;
	}


	public static void addPositionClosed(Position closedPosition) {
		closedPositions.add(closedPosition);
	}

	public static void outputPositionClosed(List<Position> closedPosition) {
		System.out.println("| StartTime          | Work Time | TYPE  | Symbol        |  Open          |  Close         |  Profit   ");
		//                  |19/04/2024 15:08:03 | 0:01:43   | SHORT | PHBUSDT       | 1.7599         | 1.7616         | -0.018700000000000383
		for (Position position : closedPosition) {
			position.printPosition();
		}
	}

	private static void clearPosition(String symbol) {
		ordersToBeClosed.remove(symbol);
		openTradesLong.remove(symbol);
		openTradesShort.remove(symbol);
	}


}
