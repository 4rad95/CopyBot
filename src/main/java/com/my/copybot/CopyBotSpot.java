/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.my.copybot;

/**
 *
 * @author radomir
 */

import com.my.copybot.exceptions.GeneralException;
import com.my.copybot.trading.TradeTask;
import com.my.copybot.trading.TradeTaskShort;
import com.my.copybot.util.BinanceTa4jUtils;
import com.my.copybot.util.BinanceUtils;
import com.my.copybot.util.ConfigUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.ta4j.core.Decimal;
import org.ta4j.core.Strategy;
import org.ta4j.core.Tick;
import org.ta4j.core.TimeSeries;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;

//import com.mycompany.copybotspot.MainForm.addStringTextEdit;
import java.io.IOException;
import java.math.BigDecimal;

public class CopyBotSpot { 

	// Config params
	private static Integer PAUSE_TIME_MINUTES = 5;
	private static Boolean DO_TRADES = true;
	private static Integer MAX_SIMULTANEOUS_TRADES = 0;
	private static Double TRADE_SIZE_BTC;
        private static Double TRADE_SIZE_USDT;
	private static Double STOPLOSS_PERCENTAGE = 1.00;
	private static Boolean DO_TRAILING_STOP = false;
	private static String TRADING_STRATEGY;
        
        public static Boolean MAKE_LONG  = true;
        public static Boolean MAKE_SHORT = true;
        public static Boolean MAKE_TRADE_AVG   = true;
        public static String BLACK_LIST ="";
        

	// We will store time series for every symbol
	private static Map<String, TimeSeries> timeSeriesCache = new HashMap<String, TimeSeries>();

	private static Map<String, TradeTask> openTradesLong = new HashMap<String, TradeTask>();
        private static Map<String, TradeTaskShort> openTradesShort = new HashMap<String, TradeTaskShort>();
        
	private static List<String> ordersToBeClosed = new LinkedList<String>();
        

	private static BinanceApiRestClient client;
	private static BinanceApiWebSocketClient liveClient;

	private static Integer closedTrades = 0;
	private static Double totalProfit = 0.0;

        private static Integer closedTradesLong = 0;
	private static Double totalProfitLong = 0.0;

        private static Integer closedTradesShort = 0;
	private static Double totalProfitShort = 0.0;
        
        private static BigDecimal startBalance;

        
	private static CandlestickInterval interval = null;
        
        private static  List<String> badSymbols = new LinkedList<String> ();
        
        

	public static void main(String[] args) throws IOException {
                    
               
		Log.info(CopyBotSpot.class, "Initializing Binance bot");
		String configFilePath = System.getProperty("CONFIG_FILE_PATH");
		Log.info(CopyBotSpot.class,
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
				Log.info(CopyBotSpot.class, "Setting candlestick interval to: "
						+ candleInterval);
				interval = _interval;
			}
		}
		if (interval == null) {
			interval = CandlestickInterval.FOUR_HOURLY;
			Log.info(CopyBotSpot.class, "Using default candlestick interval: "
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
                        if ("true".equalsIgnoreCase(makeLong)
					|| "1".equals(makeLong)) {
				MAKE_LONG = true;}
                        else {MAKE_LONG= false; }
                                
                        String makeShort= ConfigUtils
					.readPropertyValue(ConfigUtils.CONFIG_TRADING_SHORT);
                        if ("true".equalsIgnoreCase(makeLong)
					|| "1".equals(makeLong)) {
				MAKE_SHORT = true;}
                        else {MAKE_SHORT = false;}
                        String makeAvg= ConfigUtils
					.readPropertyValue(ConfigUtils.CONFIG_TRADING_AVRG);
                        if ("true".equalsIgnoreCase(makeLong)
					|| "1".equals(makeLong)) {
				MAKE_TRADE_AVG = true;}
                        else {MAKE_TRADE_AVG = false;}
                        BLACK_LIST = ConfigUtils
					.readPropertyValue(ConfigUtils.CONFIG_TRADING_BLACKLIST);
                }
		try {
			BinanceUtils.init(ConfigUtils.readPropertyValue(ConfigUtils.CONFIG_BINANCE_API_KEY),
							ConfigUtils.readPropertyValue(ConfigUtils.CONFIG_BINANCE_API_SECRET));
			client = BinanceUtils.getRestClient();
			liveClient = BinanceUtils.getWebSocketClient();
                        startBalance = printBalance();
		} catch (GeneralException e) {
			Log.severe(CopyBotSpot.class, "Unable to generate Binance clients!", e);
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
                                        Log.info(CopyBotSpot.class,"----------------------------------------------------------------------------------------------");
					Log.info(CopyBotSpot.class," CopyBot 1.00 Rad creating. It is Work!!!  ");
					Log.info(CopyBotSpot.class," Open trades LONG: " + openTradesLong.keySet().size() +" SHORT:" + openTradesShort.keySet().size());
					Log.info(CopyBotSpot.class," LONG:  " + openTradesLong.keySet());
					Log.info(CopyBotSpot.class," SHORT: " + openTradesShort.keySet());
                                            Log.info(CopyBotSpot.class,"----------------------------------------------------------------------------------------------");
                                            Log.info(CopyBotSpot.class,"Start Balance : " + startBalance + "               Current  Balance : "+ printBalance());
                                            Log.info(CopyBotSpot.class,"----------------------------------------------------------------------------------------------");
					if (DO_TRADES && closedTrades > 0) {
                                                
						Log.info(
								CopyBotSpot.class,
								"Closed trades: " + closedTrades +" Long: "+closedTradesLong+" Short: "+closedTradesShort
										+ ", total profit: "
										+ String.format("%.8f", totalProfit)
                                                                                + ", LONG: " 
										+ String.format("%.2f", totalProfitLong)	
                                                                                + ", SHORT: " 
										+ String.format("%.2f", totalProfitShort))
                                                                                ;
						Log.info(CopyBotSpot.class,"----------------------------------------------------------------------------------------------");
                                                
					}
					if ((openTradesLong.keySet().size()+openTradesShort.keySet().size()) >= MAX_SIMULTANEOUS_TRADES) {
						// We will not continue trading... avoid checking
						
						try {
							Thread.sleep(timeToWait);
						} catch (InterruptedException e) {
							Log.severe(CopyBotSpot.class, "Error sleeping", e);
						}
						continue;
					}
				}
				Long t0 = System.currentTimeMillis();
				// 2.- Get two last ticks for symbol and update cache.
				for (String symbol : symbols) {
					try {
						checkSymbol(symbol);
					} catch (Exception e) {
						Log.severe(CopyBotSpot.class, "Error checking symbol "
								+ symbol, e);
					}
				}
				Long t1 = System.currentTimeMillis() - t0;
				Log.info(CopyBotSpot.class, "All symbols analyzed, time elapsed: "
						+ (t1 / 1000.0) + " seconds.");

				try {
					Thread.sleep(timeToWait);
				} catch (InterruptedException e) {
					Log.severe(CopyBotSpot.class, "Error sleeping", e);
				}
			}
		} catch (Exception e) {
			Log.severe(CopyBotSpot.class, "Unable to get symbols", e);
		}
	}
	
	private static void checkSymbol(String symbol) {
                
		if (check(symbol)){
                // Log.debug(CopyBotSpot.class, "Checking symbol: " + symbol);
		Long t0 = System.currentTimeMillis();
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
                        
                        checkStrategy(strategyLong, strategyShort,endIndex,symbol); 
                        
                       // System.out.println("---- symbol :"+symbol);
			if (strategyLong.shouldEnter(endIndex)) {
                         
                            Decimal checkRSIStr = BinanceTa4jUtils.StochasticRSIIndicatorTest(series, 14);
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
                                                
						TradeTask tradeTask = new TradeTask(client, liveClient, symbol, currentPrice.toDouble(),
                                     			TRADE_SIZE_BTC,TRADE_SIZE_USDT, STOPLOSS_PERCENTAGE, DO_TRAILING_STOP,MAKE_TRADE_AVG);
						Thread thread=new Thread(tradeTask);
                                                tradeTask.thisThread = thread;
                                                thread.start();
                                                //new Thread(tradeTask).start();
                                                 
						openTradesLong.put(symbol, tradeTask);
						//ordersToBeClosed.remove(symbol); // I know... just in case
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
                                        System.out.println(symbol);
                                                               
					Log.info(CopyBotSpot.class, "SHORT signal for symbol: " + symbol + ", price: " + currentPrice);
                                        //Order newOrder = new Order(symbol,currentPrice);
                                        // newOrder.addNewOrder(symbol,currentPrice);
					//if (false) {
                                        if (((openTradesLong.keySet().size()+openTradesShort.keySet().size()) < MAX_SIMULTANEOUS_TRADES))
                                         {
                                                                               
                                         // We create a new thread to short trade with the symbol
                                                
						TradeTaskShort tradeTask = new TradeTaskShort(client, liveClient, symbol, currentPrice.toDouble(),
                                     			TRADE_SIZE_BTC,TRADE_SIZE_USDT, STOPLOSS_PERCENTAGE, DO_TRAILING_STOP,MAKE_TRADE_AVG);
						Thread thread=new Thread(tradeTask);
                                                tradeTask.thisThread = thread;
                                                thread.start();
                                               	openTradesShort.put(symbol, tradeTask);
                                        }}
                                
                                
                                }

		//	Log.debug(CopyBotSpot.class, "Symbol " + symbol + " checked in " + ((System.currentTimeMillis() - t0) / 1000.0) + " seconds");
		} catch (GeneralException e) {
		//	Log.severe(CopyBotSpot.class, "Unable to check symbol " + symbol);
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
			Log.info(CopyBotSpot.class, "Generating time series for " + symbol);
			try {
				List<Candlestick> candlesticks = BinanceUtils.getCandlestickBars(symbol, interval);
				TimeSeries series = BinanceTa4jUtils.convertToTimeSeries(candlesticks, symbol, interval.getIntervalId());
				timeSeriesCache.put(symbol, series);
			} catch (Exception e) {
				// Log.severe(CopyBotSpot.class, "Unable to generate time series / strategy for " + symbol, e);
                                System.out.println("\u001B[32m"  +symbol + "  -  Not used symbol !!! \u001B[0m");
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
	public static void closeOrder(String symbol, Double profit, String errorMessage,int flag) // 0-short, 1-long
               
                {
                int delta=0;
		if (StringUtils.isNotEmpty(errorMessage)) {
                        
			Log.info(CopyBotSpot.class, "Trade " + symbol + " is closed due to error: " + errorMessage);
                        badSymbols.add(symbol);
                        
		} else {
			Log.info(CopyBotSpot.class, "[Close]------- Trade " + symbol + " is closed with profit: " + String.format("%.8f", profit));
                        delta=1;
                        
                }
               // if (openTradesLong.containsKey(symbol)){
             if (profit == null ){profit = 0.00;}
             if (flag==0) {
                        closedTradesShort+=delta;
                        totalProfitShort +=profit;
             }
             else {
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
		if (ordersToBeClosed.contains(symbol)) {
			return true;
		}
		return false;
	}
        
        public static boolean check(String symbol){

            for (String badSymbol:badSymbols){
                if (badSymbol == symbol)
                        return false;
            }
            return true;
        }
        
   //   public String checkRSI(TimeSeries series,int i){
   //      return BinanceTa4jUtils.StochasticRSIIndicatorTest(series, i);
     //     }
        
        
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
                        Log.info(CopyBotSpot.class, "[Close]  Close strategy for symbol = " + symbol);
                    }}
            
            else if (strategyShort.shouldExit(endIndex)||strategyLong.shouldEnter(endIndex)){
                    if (null != openTradesShort.get(symbol))
                    {
                        ordersToBeClosed.add(symbol);
                        Log.info(CopyBotSpot.class, "[Close]  Close strategy for symbol = " + symbol);
                    }}
        }
        private static BigDecimal printBalance()
        {
                RequestOptions options = new RequestOptions();
        		SyncRequestClient syncRequestClient = SyncRequestClient.create( BinanceUtils.getApiKey(), BinanceUtils.getApiSecret(),
                options);

        
        return  syncRequestClient.getBalance().get(6).getBalance();
                
        }
}
