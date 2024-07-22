package com.my.copybot;

/**
 * @author radomir
 */

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.my.copybot.exceptions.GeneralException;
import com.my.copybot.model.Position;
import com.my.copybot.trading.Frozen;
import com.my.copybot.trading.TradeTask;
import com.my.copybot.util.*;
import org.apache.commons.lang3.StringUtils;
import org.ta4j.core.Bar;
import org.ta4j.core.Decimal;
import org.ta4j.core.Strategy;
import org.ta4j.core.TimeSeries;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.lang.Thread.sleep;

public class CopyBot {

    public static final Map<String, Integer> frozenTrade = Collections.synchronizedMap(new HashMap<String, Integer>());
    // We will store time series for every symbol
    private static final Map<String, TimeSeries> timeSeriesCache = Collections.synchronizedMap(new HashMap<String, TimeSeries>());
    private static final Map<String, String> openTradesLong = Collections.synchronizedMap(new HashMap<String, String>());
    private static final Map<String, String> openTradesShort = Collections.synchronizedMap(new HashMap<String, String>());
    private static final List<String> ordersToBeClosed = Collections.synchronizedList(new LinkedList<String>());
    private static final List<Position> closedPositions = Collections.synchronizedList(new LinkedList<Position>());
    private static final List<String> badSymbols = new LinkedList<String>();
    public static Boolean MAKE_LONG = true;
    public static Boolean MAKE_SHORT = true;
    public static Boolean MAKE_TRADE_AVG = true;
    public static String BLACK_LIST = "";
    public static Integer STOP_NO_LOSS = 100;
    public static Integer WAIT_LIMIT_ORDER = 15;
    public static Long timer = currentTimeMillis();
    public static Integer WAIT_FROZEN = 20;
    // Config params
    private static Integer PAUSE_TIME_MINUTES = 5;
    private static Boolean DO_TRADES = true;
    private static Integer MAX_SIMULTANEOUS_TRADES = 0;
    private static Double TRADE_SIZE_BTC;
    private static Double TRADE_SIZE_USDT;
    private static Double STOPLOSS_PERCENTAGE = 1.00;
    private static Boolean DO_TRAILING_STOP = false;
    private static String TRADING_STRATEGY;
    private static Boolean BEEP = false;

//	private static BinanceApiRestClient client;
//	private static BinanceApiWebSocketClient liveClient;
    private static Integer IDENT_LIMIT_ORDER = 20;
    private static Integer closedTrades = 0;
    private static Double totalProfit = 0.0;
    private static Double totalProfitLong = 0.0;
    private static Integer closedTradesLong = 0;
    private static Double totalProfitShort = 0.0;
    private static Integer closedTradesShort = 0;


    private static CandlestickInterval interval = null;
    private static CandlestickInterval interval1 = null;
    private static CandlestickInterval interval2 = null;
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
        String beep = ConfigUtils
                .readPropertyValue(ConfigUtils.CONFIG_SYSTEM_BEEP);
        if ("true".equalsIgnoreCase(beep)
                || "1".equals(beep)) {
            BEEP = true;
        }
        // Candle time frame
        String candleInterval = ConfigUtils
                .readPropertyValue(ConfigUtils.CONFIG_BINANCE_TICK_INTERVAL);
        String candleInterval1 = ConfigUtils
                .readPropertyValue(ConfigUtils.CONFIG_BINANCE_ADD1_INTERVAL);
        String candleInterval2 = ConfigUtils
                .readPropertyValue(ConfigUtils.CONFIG_BINANCE_ADD2_INTERVAL);
        CandlestickInterval[] intervals = CandlestickInterval.values();
        for (CandlestickInterval _interval : intervals) {
            if (_interval.getIntervalId().equalsIgnoreCase(candleInterval)) {
                Log.info(CopyBot.class, "Setting candlestick interval to: "
                        + candleInterval);
                interval = _interval;
            }
            if (_interval.getIntervalId().equalsIgnoreCase(candleInterval1)) {
                Log.info(CopyBot.class, "Setting add1 candlestick interval to: "
                        + candleInterval1);
                interval1 = _interval;
            }
            if (_interval.getIntervalId().equalsIgnoreCase(candleInterval2)) {
                Log.info(CopyBot.class, "Setting add2 candlestick interval to: "
                        + candleInterval2);
                interval2 = _interval;
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

            String makeLong = ConfigUtils
                    .readPropertyValue(ConfigUtils.CONFIG_TRADING_LONG);
            MAKE_LONG = "true".equalsIgnoreCase(makeLong)
                    || "1".equals(makeLong);

            String makeShort = ConfigUtils
                    .readPropertyValue(ConfigUtils.CONFIG_TRADING_SHORT);
            MAKE_SHORT = "true".equalsIgnoreCase(makeShort)
                    || "1".equals(makeShort);
            String makeAvg = ConfigUtils
                    .readPropertyValue(ConfigUtils.CONFIG_TRADING_AVRG);
            MAKE_TRADE_AVG = "true".equalsIgnoreCase(makeAvg)
                    || "1".equals(makeAvg);
            BLACK_LIST = ConfigUtils
                    .readPropertyValue(ConfigUtils.CONFIG_TRADING_BLACKLIST);
            String strStopNoLoss = ConfigUtils
                    .readPropertyValue(ConfigUtils.CONFIG_TRADING_STOPNOLOSS);
            STOP_NO_LOSS = Integer.valueOf(strStopNoLoss);
            String waitLimitOrder = ConfigUtils
                    .readPropertyValue(ConfigUtils.CONFIG_TRADING_WAIT_LIMIT);
            WAIT_LIMIT_ORDER = Integer.valueOf(waitLimitOrder);
            String waiFrozen = ConfigUtils
                    .readPropertyValue(ConfigUtils.CONFIG_TRADING_WAIT_FROZEN);
            WAIT_FROZEN = Integer.valueOf(waiFrozen);
            String identLimit = ConfigUtils
                    .readPropertyValue(ConfigUtils.CONFIG_TRADING_IDENT_LIMIT);
            IDENT_LIMIT_ORDER = Integer.valueOf(identLimit);

        }
        try {

            BinanceUtils.init(ConfigUtils.readPropertyValue(ConfigUtils.CONFIG_BINANCE_API_KEY),
                    ConfigUtils.readPropertyValue(ConfigUtils.CONFIG_BINANCE_API_SECRET));


            startBalance = printBalance();
            Runnable InputString = new InputString();
            Thread thread = new Thread(InputString);
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
            {
                Frozen frozen = new Frozen(WAIT_FROZEN);
                Thread thread = new Thread(frozen);
                frozen.thisThread = thread;
                thread.start();
            }
            List<String> finalSymbols = symbols;
            Runnable r = () -> mainProcess(finalSymbols);

            while (true) {
                try {
//					List<String> finalSymbols = symbols;
//					Runnable r = () -> mainProcess(finalSymbols);
//					Thread myThread = new Thread(r, "Search thread");
                    Thread mainThread = new Thread(r, "Search thread");
                    mainThread.start();
                    sleep(timeToWait);

                } catch (Exception e) {
                    System.out.println("Error in 1 : " + e);

                }
            }

        } catch (Exception e) {
            Log.severe(CopyBot.class, "Unable to get symbols", e);
        }
    }

    private static void checkSymbol(String symbol) {

        if (check(symbol) && frozenTrade.get(symbol) == null) {

            Long t0 = currentTimeMillis();
            try {
                List<Candlestick> latestCandlesticks = BinanceUtils.getLatestCandlestickBars(symbol, interval);
                TimeSeries series = timeSeriesCache.get(symbol);
                if (BinanceTa4jUtils.isSameTick(latestCandlesticks.get(1), series.getLastBar())) {
                    // We are still in the same tick - just update the last tick with the fresh data
                    updateLastTick(symbol, latestCandlesticks.get(1));
                } else {
                    // We have just got a new tick - update the previous one and include the new tick
                    updateLastTick(symbol, latestCandlesticks.get(0));
                    series.addBar(BinanceTa4jUtils.convertToTa4jTick(latestCandlesticks.get(1)));
                }
                // Now check the TA strategy with the refreshed time series
                int endIndex = series.getEndIndex();

                if ((openTradesLong.get(symbol) != null) || (openTradesShort.get(symbol) != null) ||
                        ((openTradesLong.keySet().size() + openTradesShort.keySet().size()) < MAX_SIMULTANEOUS_TRADES)) {
//                    Strategy strategyLong = buildStrategyLong(series, TRADING_STRATEGY);
//                    Strategy strategyShort = buildStrategyShort(series, TRADING_STRATEGY);
//
//                    checkStrategy(strategyLong, strategyShort, endIndex, symbol);
//


                        // If we have an open trade for the symbol, we do not create a new one
                        if (DO_TRADES && openTradesLong.get(symbol) == null && (MAKE_LONG)) {
                            //Decimal currentPrice = series.getLastBar().getClosePrice();
                            if (StrategyStoch.openStochStrategyLong(series)) {
                            if (((openTradesLong.keySet().size() + openTradesShort.keySet().size()) < MAX_SIMULTANEOUS_TRADES)) {

                                TimeSeries series1 = BinanceTa4jUtils.convertToTimeSeries(
                                        Objects.requireNonNull(BinanceUtils.getCandelSeries(symbol, interval1.getIntervalId(), 100))
                                        , symbol, interval1.getIntervalId());
                                TimeSeries series2 = BinanceTa4jUtils.convertToTimeSeries(
                                        Objects.requireNonNull(BinanceUtils.getCandelSeries(symbol, interval2.getIntervalId(), 100))
                                        , symbol, interval2.getIntervalId());
                                out.println("Long  " + symbol);
                                if (BinanceTa4jUtils.checkStrategyLong(series1)
                                        && BinanceTa4jUtils.checkStrategyLong(series2)) {

                                    if (BEEP) {
                                        Sound.tone(15000, 100);
                                    }
                                    addTrade(symbol, "LONG");
                                }
                            }
                        }
                    }

                        if (DO_TRADES && openTradesShort.get(symbol) == null && MAKE_SHORT) {
                            if (StrategyStoch.openStochStrategyShort(series)) {
                                //	Decimal currentPrice = series.getLastBar().getClosePrice();
                            if (((openTradesLong.keySet().size() + openTradesShort.keySet().size()) < MAX_SIMULTANEOUS_TRADES)) {
                                // We create a new thread to short trade with the symbol

                                TimeSeries series1 = BinanceTa4jUtils.convertToTimeSeries(
                                        Objects.requireNonNull(BinanceUtils.getCandelSeries(symbol, interval1.getIntervalId(), endIndex))
                                        , symbol, interval1.getIntervalId());
                                TimeSeries series2 = BinanceTa4jUtils.convertToTimeSeries(
                                        Objects.requireNonNull(BinanceUtils.getCandelSeries(symbol, interval2.getIntervalId(), endIndex))
                                        , symbol, interval2.getIntervalId());

                                out.println("Short: " + symbol);
                                if (BinanceTa4jUtils.checkStrategyShort(series1)
                                        && BinanceTa4jUtils.checkStrategyShort(series2)) {

                                    if (BEEP) {
                                        Sound.tone(15000, 100);
                                    }
                                    addTrade(symbol, "SHORT");
                                }
                            }
                        }
                    }

                    if ((null != openTradesLong.get(symbol)) && (StrategyStoch.closeStochStrategyLong(series))) {
                        ordersToBeClosed.add(symbol);
                        Log.info(CopyBot.class, "\u001B[33m [Close]  Close strategy for symbol = " + symbol + "\u001B[0m");

                    } else if ((null != openTradesShort.get(symbol)) && (StrategyStoch.closeStochStrategyShort(series))) {
                        ordersToBeClosed.add(symbol);
                        Log.info(CopyBot.class, "\u001B[33m [Close]  Close strategy for symbol = " + symbol + "\u001B[0m");
                    }

                }
            } catch (GeneralException e) {
                Log.severe(CopyBot.class, "Unable to check symbol " + symbol + "Error: " + e);
            } catch (LineUnavailableException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void updateLastTick(String symbol, Candlestick candlestick) {
        TimeSeries series = timeSeriesCache.get(symbol);
        List<Bar> seriesTick = series.getBarData();
        seriesTick.remove(series.getEndIndex());
        seriesTick.add(BinanceTa4jUtils.convertToTa4jTick(candlestick));
    }

    private static void generateTimeSeriesCache(List<String> symbols) {
        for (String symbol : symbols) {
            if (check(symbol)) {
                Log.info(CopyBot.class, "Generating time series for " + symbol);
                try {
                    List<Candlestick> candlesticks = BinanceUtils.getCandelSeries(symbol, interval.getIntervalId(), 500);
                    TimeSeries series = BinanceTa4jUtils.convertToTimeSeries(candlesticks, symbol, interval.getIntervalId());
                    timeSeriesCache.put(symbol, series);

                } catch (Exception e) {
                    System.out.println("\u001B[32m" + symbol + "  -  Not used symbol !!! \u001B[0m");
                    badSymbols.add(symbol);
                }
            }
        }
    }


    public static synchronized void closeOrder(String symbol, Double profit, String errorMessage, String type) // 0-short, 1-long
    {
        int delta = 0;
        if (StringUtils.isNotEmpty(errorMessage)) {
            Log.info(CopyBot.class, "Trade " + symbol + " is closed due to error: " + errorMessage);
            badSymbols.add(symbol);
        } else {
            Log.info(CopyBot.class, "[Close]------- Trade " + symbol + " is closed with profit: " + String.format("%.8f", profit));
            delta = 1;
        }
        if (profit == 0.00) {
            delta = 0;
            clearPosition(symbol);
        }
        if (profit == null) {
            profit = 0.00;
        }
        if (type.equals("SHORT")) {
            closedTradesShort += delta;
            totalProfitShort += profit;
        } else if (type.equals("LONG")) {
            closedTradesLong += delta;
            totalProfitLong += profit;

        }
        closedTrades += delta;
        totalProfit += profit;
        ordersToBeClosed.remove(symbol);
        frozenTrade.put(symbol, 0);
        openTradesLong.remove(symbol);
        openTradesShort.remove(symbol);
    }

    /**
     * The open thread invokes this method to check if an order should be closed
     *
     * @param symbol
     * @return if it should be closed or not
     */
    public static boolean shouldCloseOrder(String symbol) {
        return ordersToBeClosed.contains(symbol);
    }

    public static boolean check(String symbol) {

        for (String badSymbol : badSymbols) {
            if (badSymbol == symbol)
                return false;
        }
        return true;
    }

    public static boolean checkOpenOrder(String Symbol) {


        return false;
    }


    public static List<String> blackListCheck(List<String> symbols) {
        String[] badSymbols = BLACK_LIST.split(",");
        for (int i = 0; i < symbols.size(); i++) {
            for (String badSymbol : badSymbols) {
                if (symbols.get(i).equalsIgnoreCase(badSymbol)) {
                    symbols.remove(i);
                    break;
                }
            }
        }
        return symbols;
    }

    private static void checkStrategy(Strategy strategyLong, Strategy strategyShort, int endIndex, String symbol) {
        if ((null != openTradesLong.get(symbol)) && (strategyLong.shouldExit(endIndex))) {
                ordersToBeClosed.add(symbol);
                Log.info(CopyBot.class, "\u001B[33m [Close]  Close strategy for symbol = " + symbol + "\u001B[0m");

        } else if ((null != openTradesShort.get(symbol)) && (strategyShort.shouldExit(endIndex))) {
                ordersToBeClosed.add(symbol);
                Log.info(CopyBot.class, "\u001B[33m [Close]  Close strategy for symbol = " + symbol + "\u001B[0m");
        }
    }

    private static BigDecimal printBalance() {
        try {
            RequestOptions options = new RequestOptions();
            SyncRequestClient syncRequestClient = SyncRequestClient.create(BinanceUtils.getApiKey(), BinanceUtils.getApiSecret(),
                    options);

            return syncRequestClient.getBalance().get(4).getBalance();
        } catch (Exception e) {
            System.out.println(e);
            return BigDecimal.valueOf(0.00);
        }

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

        CopyBot copyBot = new CopyBot();
        TradeTask tradeTask = new TradeTask(symbol, getCurrentPrice(symbol).toDouble(),
                TRADE_SIZE_BTC, TRADE_SIZE_USDT, STOPLOSS_PERCENTAGE, WAIT_LIMIT_ORDER, MAKE_TRADE_AVG, STOP_NO_LOSS, type, IDENT_LIMIT_ORDER, copyBot);
        Thread thread = new Thread(tradeTask);
        tradeTask.thisThread = thread;
        thread.start();
        switch (type) {
            case "LONG": {
                openTradesLong.put(symbol, "LONG");
                break;
            }
            case "SHORT": {
                openTradesShort.put(symbol, "SHORT");
                break;
            }
        }

    }

    public static Decimal getCurrentPrice(String symbol) {

        TimeSeries series = timeSeriesCache.get(symbol);
        Decimal currentPrice = series.getLastBar().getClosePrice();
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

    private synchronized static void clearPosition(String symbol) {
        ordersToBeClosed.remove(symbol);
        openTradesLong.remove(symbol);
        openTradesShort.remove(symbol);
    }

    public static Strategy buildStrategyLong(TimeSeries series, String strategyCode) {
        switch (strategyCode) {
            case "MACD": {
                return StrategyMACD.buildMacdStrategyLong(series);
            }
            case "SMA": {
                return StrategySMA.buildSmaStrategyLong(series);
            }
            case "STOCH": {
                return StrategyStoch.buildStochStrategyLong(series);
            }
            default:
                return null;
        }

    }

    public static Strategy buildStrategyShort(TimeSeries series, String strategyCode) {
        switch (strategyCode) {
            case "MACD": {
                return StrategyMACD.buildMacdStrategyShort(series);
            }
            case "SMA": {
                return StrategySMA.buildSmaStrategyShort(series);
            }
            case "STOCH": {
                return StrategyStoch.buildStochStrategyShort(series);
            }
            default:
                return null;
        }
    }

    public synchronized static void updateMapPosition(Position position) {
        String switchString = position.getType();
        switch (switchString) {
            case "SHORT": {
                openTradesShort.put(position.getSymbol(), "position");
                break;
            }
            case "LONG": {
                openTradesLong.put(position.getSymbol(), "position");
                break;
            }
        }
    }

    public static synchronized void checkStrategyOpenPosition(Map<String, String> mapPosition) {
        for (Map.Entry position : mapPosition.entrySet()) {
            checkSymbol(position.getKey().toString());
        }
    }

    public static synchronized void modifyFrozenList() {
        for (Map.Entry<String, Integer> entry : frozenTrade.entrySet()) {
            entry.setValue(entry.getValue() + 1);
        }
        frozenTrade.entrySet().removeIf(entry -> entry.getValue() > WAIT_FROZEN);
    }

    public static void mainProcess(List<String> symbols) {

        try {
            Long t0 = currentTimeMillis();

            int seconds = (int) ((t0 - timer) / 1000);
            int minutes = seconds / 60;
            int hours = minutes / 60;
            minutes = minutes - hours * 60;
            seconds = seconds - minutes * 60;
            String formattedTime = String.format("%d:%02d:%02d", hours, minutes, seconds);
            Log.info(CopyBot.class, "--------------------------------------------------------------------------------------------------------------------");
            Log.info(CopyBot.class, "\u001B[36m CopyBot 1.016 ( test Edition beta. Good and Best!)    \u001B[0m");
            //		Log.info(CopyBot.class, "\u001B[36m Using new re-Made Trade Strategy  \u001B[0m");
            Log.info(CopyBot.class, " Open trades LONG: " + openTradesLong.keySet().size() + " SHORT:" + openTradesShort.keySet().size());
            Log.info(CopyBot.class, " LONG:  " + openTradesLong.keySet());
            Log.info(CopyBot.class, " SHORT: " + openTradesShort.keySet());
            Log.info(CopyBot.class, "--------------------------------------------------------------------------------------------------------------------");
            Log.info(CopyBot.class, "\u001B[32m Start time       : " + new Date(timer) + " \u001B[0m ");
            Log.info(CopyBot.class, "\u001B[32m Execute time     : " + formattedTime + " \u001B[0m ");
            Log.info(CopyBot.class, "\u001B[32m Start Balance    : " + startBalance + "               Current  Balance : " + printBalance() + " \u001B[0m ");
            //	Log.info(CopyBot.class, "--------------------------------------------------------------------------------------------------------------------");
            Log.info(CopyBot.class, "\u001B[32m Max. Position:   : " + MAX_SIMULTANEOUS_TRADES + "                         USDT Size : " + TRADE_SIZE_USDT + " \u001B[0m ");
            Log.info(CopyBot.class, "--------------------------------------------------------------------------------------------------------------------");
            //	Log.info(CopyBot.class, "|Start time          | Work time | Symbol        | Open price       | Current price    | Stop loss        |  Profit");
            //	outputPosition();
            if (DO_TRADES && closedTrades > 0) {
                Log.info(
                        CopyBot.class,
                        "\u001B[32mClosed trades: " + closedTrades + " Long: " + closedTradesLong + " Short: " + closedTradesShort
                                + ", total profit: " + String.format("%.8f", totalProfit)
                                + ", LONG: " + String.format("%.2f", totalProfitLong)
                                + ", SHORT: " + String.format("%.2f", totalProfitShort) + "\u001B[0m ");
                Log.info(CopyBot.class, "--------------------------------------------------------------------------------------------------------------------");

            }
//            if ((openTradesLong.keySet().size() + openTradesShort.keySet().size()) >= MAX_SIMULTANEOUS_TRADES) {
//                // We will not continue trading... avoid checking
//                checkStrategyOpenPosition(openTradesLong);
//                checkStrategyOpenPosition(openTradesShort);
//            }

        } catch (Exception e) {
            System.out.println(e);
        }
        Long t0 = currentTimeMillis();
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

    }


}