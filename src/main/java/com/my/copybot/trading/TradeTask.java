package com.my.copybot.trading;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.NewOrderRespType;
import com.binance.client.model.enums.OrderSide;
import com.binance.client.model.enums.OrderType;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.trade.Order;
import com.my.copybot.CopyBot;
import com.my.copybot.Log;
import com.my.copybot.exceptions.GeneralException;
import com.my.copybot.model.ExecutedOrder;
import com.my.copybot.model.Position;
import com.my.copybot.util.BinanceUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;


public class TradeTask implements Runnable {

    private final String symbol;
    private final Double alertPrice;
    private final Double btcAmount;
    private final Double usdtAmount;
    private final Double stopLossPercentage;
    private final boolean doTrailingStop;
    private final BinanceApiRestClient client;
    private final BinanceApiWebSocketClient liveClient;
    private final boolean makeAvg;
    private final Integer stopNoLoss;
    private final String type;   // (Short or Long)
    public Thread thisThread;
    private ExecutedOrder order = null;
    private boolean error = false;
    private String errorMessage = "";
    private Long lastPriceLog = 0L;
    private String startColorStr = " ";
    private final String endColorStr = "\u001B[0m";


    public TradeTask(BinanceApiRestClient client, BinanceApiWebSocketClient liveClient, String symbol, Double alertPrice, Double btcAmount, Double usdtAmount,
                     Double stopLossPercentage, boolean doTrailingStop, boolean makeAvg, Integer stopNoLoss, String type) {
//
//	public TradeTask( String symbol, Double alertPrice, Double btcAmount,
//			Double stopLossPercentage, boolean doTrailingStop) {
        this.symbol = symbol;
        this.alertPrice = alertPrice;
        this.btcAmount = btcAmount;
        this.stopLossPercentage = stopLossPercentage;
        this.doTrailingStop = doTrailingStop;
        this.usdtAmount = usdtAmount;
        this.client = client;
        this.liveClient = liveClient;
        this.makeAvg = makeAvg;
        this.stopNoLoss = stopNoLoss;
        this.type = type;
    }

    public void run() {

        try {
            // 1.- BUY, get order data - price and create ExecutedOrder with stoploss
            buy();

            // 2.- Suscribe to price ticks for the symbol, evaluate current price and update stoploss (if trailing stop)
            monitorPrice();


        } catch (GeneralException e) {
            Log.severe(getClass(), "Unable to create buy operation", e);
            error = true;
            errorMessage = e.getMessage();
            CopyBot.closeOrder(symbol, null, e.getMessage(), type);
        }

    }

    private synchronized void buy() throws GeneralException {
        String quantity = getAmount(alertPrice);
        Log.info(getClass(), "Trying to buy " + symbol + ", quantity: " + quantity);
        String priceReal = "";
        try {


            RequestOptions options = new RequestOptions();
            SyncRequestClient syncRequestClient = SyncRequestClient.create(BinanceUtils.getApiKey(), BinanceUtils.getApiSecret(),
                    options);
            // By now we will not be creating real orders
            Order orderNew;
            switch (type) {
                case "SHORT": {
                    orderNew = syncRequestClient.postOrder(symbol,
                            OrderSide.SELL, PositionSide.SHORT, OrderType.MARKET, null, quantity,
                            null, null, null, null, null, null, null, null, null,
                            NewOrderRespType.RESULT);
                    order = new ExecutedOrder();
                    order.setType(type);
                    order.setPrice(orderNew.getAvgPrice().doubleValue());
                    priceReal = orderNew.getAvgPrice().toString();
                    order.setCurrentStopLoss((100 + stopLossPercentage) * order.getPrice() / (100.0));
                    order.setSymbol(symbol);
                    order.setQuantity(quantity);
                    order.setInitialStopLoss(order.getCurrentStopLoss());
                    order.setOrderId(orderNew.getClientOrderId());
                    priceReal = orderNew.getAvgPrice().toString();
                    break;
                }
                case "LONG": {

                    orderNew = syncRequestClient.postOrder(symbol,
                            OrderSide.BUY, PositionSide.LONG, OrderType.MARKET, null, quantity,
                            null, null, null, null, null, null, null, null, null,
                            NewOrderRespType.RESULT);
                    order = new ExecutedOrder();
                    order.setType(type);
                    order.setPrice(orderNew.getAvgPrice().doubleValue());
                    priceReal = orderNew.getAvgPrice().toString();
                    order.setCurrentStopLoss((100.0 - (stopLossPercentage)) * alertPrice / (100.0));
                    order.setSymbol(symbol);
                    order.setQuantity(quantity);
                    order.setInitialStopLoss(order.getCurrentStopLoss());
                    order.setOrderId(orderNew.getClientOrderId());
                    priceReal = orderNew.getAvgPrice().toString();
                    break;
                }
                default:
                    throw new IllegalStateException("Unexpected value: " + type);
            }

            /*order.setSymbol(symbol);
            order.setQuantity(quantity);
           //  order.setPrice(orderNew.getAvgPrice().doubleValue());
                    //priceReal = orderNew.getAvgPrice().toString();
            order.setInitialStopLoss(order.getCurrentStopLoss());
            order.setOrderId(orderNew.getClientOrderId()); */

        } catch (Exception e) {
            sell(alertPrice);
            CopyBot.closeOrder(symbol, 0.00, null, type);
            throw new GeneralException(e);

        }
        //  order.setInitialStopLoss(order.getCurrentStopLoss());
        order.setCreationTime(System.currentTimeMillis());
        Log.info(getClass(), "Buy [" + type + "] ready : " + symbol + ", quantity: " + quantity + ",  " + priceReal);
    }

    private Position createStatisticPosition(String status) {
        String orderType = order.getType();
        if (orderType.equals("LONG")) {
            orderType = orderType + " ";
        }
        Position closePosition = new Position(order.getCreationTime(), order.getCloseTime(), orderType,
                order.getSymbol(), order.getPrice(), order.getClosePrice(), order.getQuantity(), order.getProfit(), status);
        return closePosition;
    }


    private String getAmount(Double price) {
        // This method should be refactored... there is a method in Binance API to get symbol info
        Double rawAmount = usdtAmount / price;

        if (rawAmount > 1) {
            Integer iAmount = Integer.valueOf(rawAmount.intValue());
            return "" + iAmount;
        } else if (rawAmount < 1 && rawAmount >= 0.1) {
            return StringUtils.replaceAll(String.format("%.2f", rawAmount), ",", ".");
        } else {
            return StringUtils.replaceAll(String.format("%.3f", rawAmount), ",", ".");
        }
    }

    private Double getAmountDouble(Double price) {
        // This method should be refactored... there is a method in Binance API to get symbol info
        Double rawAmount = usdtAmount / price;
        return rawAmount;
    }

    private void monitorPrice() {
        liveClient.onCandlestickEvent(symbol.toLowerCase(),
                CandlestickInterval.ONE_MINUTE, response -> {
                    try {
                        checkPrice(Double.valueOf(response.getClose()));
                    } catch (GeneralException ex) {
                        Logger.getLogger(TradeTask.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
    }

    /*private void buySecond() throws GeneralException {
        String quantity = getAmount(alertPrice);
        Log.info(getClass(), "Second to buy " + symbol + ", quantity: " + quantity);

        try {


            RequestOptions options = new RequestOptions();
            SyncRequestClient syncRequestClient = SyncRequestClient.create(BinanceUtils.getApiKey(), BinanceUtils.getApiSecret(),
                    options);
            // By now we will not be creating real orders

            Order orderNew = syncRequestClient.postOrder(symbol,
                    OrderSide.SELL, PositionSide.SHORT, OrderType.MARKET, null,quantity,
                    null, null, null,null, null, null, null, null, null,
                    NewOrderRespType.RESULT);

//                        order = new ExecutedOrderShort();
            //order.setSymbol(symbol);
            order.setQuantityDouble(order.getQuantityDouble()+getAmountDouble(alertPrice));
            order.setQuantity(getAmount(order.getQuantityDouble()));
            // orderNew.getPositionSide());
            order.setPrice((orderNew.getAvgPrice().doubleValue()+order.getPrice())/2);

            //order.setCurrentStopLoss((100 + stopLossPercentage) * alertPrice / (100.0));
            // order.setInitialStopLoss(order.getCurrentStopLoss());
            // order.setOrderId(orderNew.getClientOrderId());

        } catch (Exception e) {
            CopyBot.closeOrder(symbol, 0.00, null,0);
            throw new GeneralException(e);

        }

//		order = new ExecutedOrder();
//		order.setSymbol(symbol);
//		order.setQuantity(quantity);
//		order.setPrice(alertPrice);
//		// current stop loss - used for trailing stop
//		order.setCurrentStopLoss((100.0 - stopLossPercentage) * alertPrice / 100.0);
        order.setInitialStopLoss(order.getCurrentStopLoss());
        order.setCreationTime(System.currentTimeMillis());
    }
*/
    private void sell(Double price) {
        try {
//			NewOrder newOrder = marketSell(symbol, String.valueOf(order.getQuantity()));
//			client.newOrder(newOrder);

            RequestOptions options = new RequestOptions();
            SyncRequestClient syncRequestClient = SyncRequestClient.create(BinanceUtils.getApiKey(), BinanceUtils.getApiSecret(),
                    options);
            Order orderNew;
            switch (type) {
                case "SHORT": {
                    orderNew = syncRequestClient.postOrder(symbol,
                            OrderSide.BUY, PositionSide.SHORT, OrderType.MARKET, null, order.getQuantity(),
                            null, null, null, null, null, null, null, null, null,
                            NewOrderRespType.RESULT);
                    break;
                }
                case "LONG": {
                    orderNew = syncRequestClient.postOrder(symbol,
                            OrderSide.SELL, PositionSide.LONG, OrderType.MARKET, null, order.getQuantity(),
                            null, null, null, null, null, null, null, null, null,
                            NewOrderRespType.RESULT);
                    break;
                }
            }
            Log.info(getClass(), "Created CLOSE order: " + order.getOrderId() + " " + order.getSymbol());
            order.setClosePrice(price);
            order.setCloseTime(System.currentTimeMillis());
            // Добавить статистику!
            CopyBot.closeOrder(symbol, order.getProfit(), null, type);
            CopyBot.addPositionClosed(createStatisticPosition("Ok"));
        } catch (Exception e) {
            System.out.println(" --------------------------- " + symbol + "   closed");
            Log.severe(getClass(), "Unable to sell!", e);
            CopyBot.addPositionClosed(createStatisticPosition("Error"));
            // CopyBotSpot.closeOrder(symbol, order.getProfit(), null);
        }
    }

    public synchronized String getErrorMessage() {
        return errorMessage;
    }

    public synchronized boolean isClosed() {

        if (error) {
            return true;
        }
        return order == null || order.getCloseTime() != null;
    }

    public synchronized ExecutedOrder getOrder() {
        return order;
    }

    private String showPrice(Double price) {
        return String.format("%.8f", price);
    }

    public synchronized String getSymbol() {
        return symbol;
    }

    private Double setStopLoss(Double chkProffit, Double price) {
        Double proffitNew = 0.00;
        switch (type) {

            case "SHORT": {
                if (chkProffit > 24.00) {
                    startColorStr = "\u001B[36m";
                    proffitNew = order.getPrice() - (order.getPrice() - price) * 2 / 3;
                    return proffitNew;
                } else {
                    startColorStr = "\u001B[33m";
                    proffitNew = (order.getPrice() + price) / 2;
                    return proffitNew;
                }
            }
            case "LONG": {
                if (chkProffit > 24.00) {
                    startColorStr = "\u001B[36m";
                    proffitNew = order.getPrice() + ((price - order.getPrice()) * 2 / 3);
                    return proffitNew;
                } else {
                    startColorStr = "\u001B[33m";
                    proffitNew = (order.getPrice() + price) / 2;
                    return proffitNew;
                }
            }
            default:
                return null;
        }
    }

    private synchronized void checkPrice(Double price) throws GeneralException {
        Long now = System.currentTimeMillis();
        // Change p
//                if (Double.parseDouble(order.getCurrentProfit(price)) > 2) {
//                                  order.setCurrentStopLoss(order.getPrice()*1.1);
//                        }
        // This is a bit harcoded, but just trying to avoid too many logs..
        if ((now - lastPriceLog) > 60 * 1000L) {
            String proffit = order.getCurrentProfit(price).replace(",", ".");
            Double chkProffit = Double.parseDouble(proffit);
            if (chkProffit > stopNoLoss) {
                // Uppper StoppLoss level

                double temp = setStopLoss(chkProffit, price);
                // order.setCurrentStopLoss(setStopLoss(chkProffit));
                if (temp > order.getCurrentStopLoss() && (order.getType().equals("LONG"))) {
                    order.setCurrentStopLoss(temp);
                } else if ((temp < order.getCurrentStopLoss() && (order.getType().equals("SHORT")))) {
                    order.setCurrentStopLoss(temp);
                }
            }

            Log.info(getClass(),
                    startColorStr + type + " : " + symbol + ". Current price: " + showPrice(price)
                            + ", buy price: " + showPrice(order.getPrice())
                            + ", stoploss: "
                            + showPrice(order.getCurrentStopLoss())
                            + ", current profit: " + order.getCurrentProfit(price) + "%" + endColorStr);
            lastPriceLog = now;
        }
        // Implement avg buy
//                if (makeAvg &&(price > order.getPriceAvg()))
//                {
//                    makeAvg = false;
//                    buySecond();
//                }

        //if (trailingStopShouldCloseOrder(price) || CopyBotSpot.shouldCloseOrder(symbol)) //{
        switch (type) {
            case "SHORT": {
                if (price >= order.getCurrentStopLoss() || CopyBot.shouldCloseOrder(symbol))      // Close stopLoss
                {
                    sell(price);
                    Log.info(getClass(), "[STOP][" + type + "] :  ---------  Closed order for symbol: " + symbol
                            + ". Current price: " + showPrice(price) + ", profit: " + order.getProfit());
                    thisThread.stop();
                }
                break;
            }
            case "LONG": {
                if (price <= order.getCurrentStopLoss() || CopyBot.shouldCloseOrder(symbol))      // Close stopLoss
                {
                    sell(price);
                    Log.info(getClass(), "[STOP][" + type + "] :  ---------  Closed order for symbol: " + symbol
                            + ". Current price: " + showPrice(price) + ", profit: " + order.getProfit());
                    thisThread.stop();
                }
                break;
            }

        }
    }
}

