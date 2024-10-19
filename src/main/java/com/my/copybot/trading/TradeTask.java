package com.my.copybot.trading;

import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.*;
import com.binance.client.model.trade.Order;
import com.my.copybot.CopyBot;
import com.my.copybot.Log;
import com.my.copybot.exceptions.GeneralException;
import com.my.copybot.model.ExecutedOrder;
import com.my.copybot.model.Position;
import com.my.copybot.util.BinanceUtils;
import org.apache.commons.lang3.StringUtils;
import org.ta4j.core.Decimal;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;

import static java.lang.Thread.sleep;
import static org.apache.commons.lang3.StringUtils.repeat;


public class TradeTask implements Runnable {

    private final String symbol;
    private final Double alertPrice;
    private final Double btcAmount;
    private final Double usdtAmount;
    private final Double stopLossPercentage;
    private final Integer waitOrderLimit;

    private final CopyBot copyBot;
    private final boolean makeAvg;
    private final Integer stopNoLoss;
    private final String type;   // (Short or Long)
    public Thread thisThread;
    private ExecutedOrder order = null;
    private boolean error = false;
    private String errorMessage = "";
    private final Long lastPriceLog = 0L;
    private String startColorStr = " ";
    private final String endColorStr = "\u001B[0m";
    private int counter = 10;
    private boolean stopThread = false;
    private Double maxPercent = 0.00;
    private Double minPercent = 0.00;
    private Integer identLimitOredr = 20;
    private final Decimal ATR;
    private final Double stopPrice;
    private Long idStopLossOrder = 0L;
    private String quantity;
    private String startDes = "";

    public TradeTask(String symbol, Double alertPrice, Double btcAmount, Double usdtAmount,
                     Double stopLossPercentage, Integer waitOrderLimit, boolean makeAvg, Integer stopNoLoss, String type, Integer identLimitOredr, CopyBot copyBot, Decimal ATR, Double stopPrice, String startDes) {

        this.symbol = symbol;
        this.alertPrice = alertPrice;
        this.btcAmount = btcAmount;
        this.stopLossPercentage = stopLossPercentage;
        this.usdtAmount = usdtAmount;
        this.waitOrderLimit = waitOrderLimit;
        this.makeAvg = makeAvg;
        this.stopNoLoss = stopNoLoss;
        this.type = type;
        this.identLimitOredr = identLimitOredr;
        this.copyBot = copyBot;
        this.ATR = ATR;
        this.stopPrice = stopPrice;
        this.startDes = startDes;
    }

    public static String multiplyAndRound(Double number, double multiplier) {
        // Если число null, вернуть пустую строку
        if (number == null) {
            return "";
        }

        // Умножение числа на множитель
        double result = number * multiplier;
        int resultInt = (int) result;

        // Получение количества знаков после запятой в исходном числе
        int decimalPlaces = getDecimalPlaces(number);

        // Создание форматтера для округления до исходного количества знаков после запятой
        DecimalFormat df = new DecimalFormat("#." + repeat('0', decimalPlaces));

        // Округление результата до исходного количества знаков после запятой и преобразование в строку
        String string = df.format(result);
        if (resultInt == 0) {
            string = "0" + string;
        }
        return string.replace(',', '.');
    }


    private static int getDecimalPlaces(double number) {
        String[] parts = Double.toString(number).split("\\.");
        return parts.length > 1 ? parts[1].length() : 0;
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

    /*
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


            } catch (Exception e) {
                sell(alertPrice);
                CopyBot.closeOrder(symbol, 0.00, null, type);
                throw new GeneralException(e);

            }
            order.setCreationTime(System.currentTimeMillis());
            Log.info(getClass(), "Buy [" + type + "] ready : " + symbol + ", quantity: " + quantity + ",  " + priceReal);
        }
    */
    private Position createStatisticPosition(String status) {
        String orderType = order.getType();
        if (orderType.equals("LONG")) {
            orderType = orderType + " ";
        }
        return new Position(order.getCreationTime(), order.getCloseTime(), orderType,
                order.getSymbol(), order.getPrice(), order.getClosePrice(), order.getPrice(), order.getQuantity(), order.getProfit(), order.getCurrentProfit(order.getPrice()) + " % ", status, startColorStr);
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

    public void run() throws RuntimeException {

        try {
            // 1.- BUY, get order data - price and create ExecutedOrder with stoploss
            buyLimit();

            // 2.- Suscribe to price ticks for the symbol, evaluate current price and update stoploss (if trailing stop)
            while (!stopThread) {
                RequestOptions options = new RequestOptions();
                SyncRequestClient syncRequestClient = SyncRequestClient.create(BinanceUtils.getApiKey(), BinanceUtils.getApiSecret(),
                        options);
                BigDecimal price = syncRequestClient.getMarkPrice(symbol).getFirst().getMarkPrice();
                if (!checkPrice(Double.parseDouble(price.toString()))) {
                    break;
                }
                try {
                    sleep(6000);   // 6c Cна


                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (GeneralException e) {
            Log.severe(getClass(), "Unable to create buy operation", e);
            error = true;
            errorMessage = e.getMessage();
            CopyBot.closeOrder(symbol, null, e.getMessage(), type);
        }

    }

    private void sell(Double price) {
        try {
            String str = CopyBot.ordersToBeClosed.get(order.getSymbol());
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
            Log.info(getClass(), "Created CLOSE order: " + order.getOrderId() + " " + order.getSymbol() + "  " + str + "  " + order.getCurrentProfit(price) + " %  ");
            order.setClosePrice(price);
            order.setCloseTime(System.currentTimeMillis());
            if (idStopLossOrder != 0L) {
                syncRequestClient.cancelOrder(symbol, idStopLossOrder, quantity);
            }
            // Добавить статистику!
            CopyBot.closeOrder(symbol, order.getProfit(), null, type);
            CopyBot.addPositionClosed(createStatisticPosition("Max.:" + String.format("%.2f", maxPercent) + "%, Min.:" + String.format("%.2f", minPercent) + " % profit: " + order.getCurrentProfit(price) + "%  " + startDes + str));
        } catch (Exception e) {
            CopyBot.closeOrder(symbol, 0.00, null, "Error");
            System.out.println(" --------------------------- " + symbol + "   closed");
            //           Log.severe(getClass(), "Unable to sell!", e);
            stopThread = true;
        }
    }

    public Double setMaxPercent(Double percentProfit) {
        if (percentProfit > maxPercent) {
            maxPercent = percentProfit;
        } else if (percentProfit < minPercent) {
            minPercent = percentProfit;
        }
//        if (maxPercent > stopNoLoss + 100) {
//            startColorStr = "\u001B[35m";
//        } else if (maxPercent > stopNoLoss + 50) {
//            startColorStr = "\u001B[36m";
//        } else if (maxPercent > stopNoLoss) {
//            startColorStr = "\u001B[33m";
//        }
        return maxPercent;
    }

    private Double setStopLoss(Double chkProffit, Double price) {

        Double proffitNew = 0.00;
        switch (type) {

            case "SHORT": {
                if (chkProffit > 100) {
                    proffitNew = order.getPrice() - (order.getPrice() - price) * 8 / 10;
                    startColorStr = "\u001B[35m";
                    return proffitNew;
                } else if (chkProffit > 50.00) {
                    proffitNew = order.getPrice() - (order.getPrice() - price) * 8 / 10;
                    startColorStr = "\u001B[36m";
                    return proffitNew;
                } else {
                    proffitNew = order.getPrice() - ((order.getPrice() - price)) * 8 / 10;
                    startColorStr = "\u001B[33m";
                    return proffitNew;
                }
            }
            case "LONG": {
                if (chkProffit > 100) {
                    proffitNew = order.getPrice() + (price - order.getPrice()) * 8 / 10;
                    startColorStr = "\u001B[35m";
                    return proffitNew;
                } else if (chkProffit > 50.00) {
                    proffitNew = order.getPrice() + ((price - order.getPrice()) * 8 / 10);
                    startColorStr = "\u001B[36m";
                    return proffitNew;
                } else {
                    proffitNew = order.getPrice() + ((price - order.getPrice()) * 8 / 10);
                    startColorStr = "\u001B[33m";
                    return proffitNew;
                }
            }
            default:
                return null;
        }

    }

    private synchronized boolean checkPrice(Double price) throws GeneralException {
       /*
        Long now = System.currentTimeMillis();
        // This is a bit harcoded, but just trying to avoid too many logs.. -*/
        try {
            String proffit = order.getCurrentProfit(price).replace(",", ".");
            String currProffit = order.getCurrentProfit(order.getProffit()).replace(",", ".");

            Double chkProffit = Double.parseDouble(proffit);
            Double chkMaxProffit = Double.parseDouble(currProffit);
            setMaxPercent(chkProffit);

             if (chkProffit > chkMaxProffit)  {//stopNoLoss) {


            Double temp = setStopLoss(chkProffit, price);
            if // (temp > order.getCurrentStopLoss() && order.getType().equals("LONG")) {
            (price >= order.getProffit() && order.getType().equals("LONG") && temp > order.getCurrentStopLoss()) {
                    order.setCurrentStopLoss(temp);
            } else if (price <= order.getProffit() && order.getType().equals("SHORT") && temp < order.getCurrentStopLoss()) {
                // (temp < order.getCurrentStopLoss() && order.getType().equals("SHORT")) {
                    order.setCurrentStopLoss(temp);
                }
            }
            //
            if (counter == 30) {
            String msg =
                    type + " : " + symbol + ". Curr : " + showPrice(price)
                            + ", buy : " + showPrice(order.getPrice())
                            + ", stop : " + showPrice(order.getCurrentStopLoss()) + " " + order.getCurrentProfit(order.getCurrentStopLoss()) + " % "
                            + ", profit : " + showPrice(order.getProffit()) + " " + order.getCurrentProfit(order.getProffit()) + " % "
                            + ", Max. : " + String.format("%.2f", maxPercent) + " % "
                            + ", Min. : " + String.format("%.2f", minPercent) + " % "
                            + ", profit: " + order.getCurrentProfit(price) + " %  "
                            + endColorStr;
            if (order.getCurrentProfit(price).charAt(0) == '-') {
                msg = "\u001B[31m" + msg;
            } else {
                msg = startColorStr + msg;
            }
            Log.info(getClass(), msg);
            counter = 0;
        }
        counter++;
        switch (type) {
            case "SHORT": {
                if (price <= (order.getPrice() - (ATR.doubleValue() * 0.4) ) && order.getCurrentStopLoss() > (order.getPrice() - (ATR.doubleValue() * 0.4) ) ) {
  //                      createStopLoss(PositionSide.SHORT,quantity, showPrice(order.getPrice() - (ATR.doubleValue() * 0.2) ));
                        order.setCurrentStopLoss(order.getPrice() - (ATR.doubleValue() * 0.2));
                        Log.info(getClass(), "[NEW STOPLOSS][" + type + "] :  ---------  " + symbol+ "  " + showPrice(order.getPrice() - (ATR.doubleValue() * 0.2) ));
                }
                if (price <= (order.getPrice() - (ATR.doubleValue() * 0.85) ) && order.getCurrentStopLoss() > (order.getPrice() - (ATR.doubleValue() * 0.5) ) ) {
//                    createStopLoss(PositionSide.SHORT,quantity, showPrice(order.getPrice() - (ATR.doubleValue() * 0.5) ));
                    order.setCurrentStopLoss(order.getPrice() - (ATR.doubleValue() * 0.5));
                    Log.info(getClass(), "[NEW STOPLOSS][" + type + "] :  ---------  " + symbol+ "  " + showPrice(order.getPrice() - (ATR.doubleValue() * 0.5) ));
                }
                if (price >= order.getCurrentStopLoss() || CopyBot.shouldCloseOrder(symbol)) //|| (price <= order.getProffit()))      // Close stopLoss
                {
                    sell(price);
                    Log.info(getClass(), "[NEW STOP][" + type + "] :  ---------  Closed order for symbol: " + symbol
                            + ". Current price: " + showPrice(price) + ", profit: " + order.getProfit());
                    stopThread = true;
                }
                break;
            }
            case "LONG": {
                if (price >= (order.getPrice() + (ATR.doubleValue() * 0.4) ) && order.getCurrentStopLoss() < (order.getPrice() + (ATR.doubleValue() * 0.2) ) ) {
                 //   createStopLoss(PositionSide.LONG,quantity, showPrice(order.getPrice() + (ATR.doubleValue() * 0.2) ));
                    order.setCurrentStopLoss(order.getPrice() + (ATR.doubleValue() * 0.2));
                    Log.info(getClass(), "[STOPLOSS][" + type + "] :  ---------  " + symbol+ "  " + showPrice(order.getPrice() + (ATR.doubleValue() * 0.2) ));
                }
                if (price >= (order.getPrice() - (ATR.doubleValue() * 0.85) ) && order.getCurrentStopLoss() < (order.getPrice() + (ATR.doubleValue() * 0.5) )  ) {
                    //createStopLoss(PositionSide.LONG,quantity, showPrice(order.getPrice() + (ATR.doubleValue() * 0.5) ));
                    order.setCurrentStopLoss(order.getPrice() + (ATR.doubleValue() * 0.2));
                    Log.info(getClass(), "[STOPLOSS][" + type + "] :  ---------  " + symbol+ "  " + showPrice(order.getPrice() + (ATR.doubleValue() * 0.5) ));
                }
                if (price <= order.getCurrentStopLoss() || CopyBot.shouldCloseOrder(symbol)) //|| (price >= order.getProffit()))      // Close stopLoss
                {
                    sell(price);
                    Log.info(getClass(), "[STOP][" + type + "] :  ---------  Closed order for symbol: " + symbol
                            + ". Current price: " + showPrice(price) + ", profit: " + order.getProfit());
                    stopThread = true;

                }
                break;
            }

        }
        } catch (NullPointerException e) {
            stopThread = true;
            CopyBot.closeOrder(symbol, 0.00, null, "Error");
        }
        return true;
    }

    private synchronized void buyLimit() {
        quantity = getAmount(alertPrice);
       // Log.info(getClass(),
        System.out.println("  Trying to buy " + symbol + ", quantity: " + quantity);
        String priceReal = "";
        try {


            RequestOptions options = new RequestOptions();
            SyncRequestClient syncRequestClient = SyncRequestClient.create(BinanceUtils.getApiKey(), BinanceUtils.getApiSecret(),
                    options);
            // By now we will not be creating real orders
            Order orderNew;

            switch (type) {

                case "SHORT": {

                    String priceTmp = multiplyAndRound(alertPrice, multikChange(identLimitOredr));
                    orderNew = (syncRequestClient.postOrder(symbol, OrderSide.SELL, PositionSide.SHORT, OrderType.LIMIT, TimeInForce.GTC,
                            quantity, priceTmp, null, null, null, null, null, null, null, null, NewOrderRespType.RESULT));
                    Long orderId = orderNew.getOrderId();
                    int count = 0;
                    while (true) {
                        sleep(20000);
                        orderNew = syncRequestClient.getOrder(symbol, orderId, null);
                        if (count % 15 == 0) {
                            Log.info(getClass(), "[" + type + "] Waiting to buy " + symbol + "    " + count * 20 + " s. ");
                        }
                        if (!orderNew.getStatus().equals("NEW")) {
                            break;
                        }
                        if ((count > 3 * waitOrderLimit) || (CopyBot.shouldCloseOrder(symbol))) {
                            orderNew = syncRequestClient.cancelOrder(symbol, orderId, null);
                            throw new IOException();
                        }
                        count++;
                    }

                    order = new ExecutedOrder();
                    order.setType(type);
                    order.setPrice(orderNew.getAvgPrice().doubleValue());
                    priceReal = orderNew.getAvgPrice().toString();
                    order.setCurrentStopLoss(stopPrice);
                    //order.setCurrentStopLoss((100 + stopLossPercentage) * order.getPrice() / (100.0));
                    order.setCurrentStopLoss(order.getPrice() + (ATR.doubleValue() * 1.5));
                    order.setProffit(order.getPrice() - (ATR.doubleValue() * 1));
                    order.setSymbol(symbol);
                    order.setQuantity(quantity);
                    order.setInitialStopLoss(order.getCurrentStopLoss());
                    order.setOrderId(orderNew.getClientOrderId());
                    priceReal = orderNew.getAvgPrice().toString();
                    break;
                }
                case "LONG": {
                    String priceTmp = multiplyAndRound(alertPrice, multikChange(identLimitOredr));
                    orderNew = (syncRequestClient.postOrder(symbol, OrderSide.BUY, PositionSide.LONG, OrderType.LIMIT, TimeInForce.GTC,
                            quantity, priceTmp, null, null, null, null, null, null, null, null, NewOrderRespType.RESULT));
                    Long orderId = orderNew.getOrderId();
                    int count = 0;
                    while (true) {
                        sleep(20000);
                        orderNew = syncRequestClient.getOrder(symbol, orderId, null);
                        if (count % 15 == 0) {
                            Log.info(getClass(), "[" + type + "] Waiting to buy " + symbol + "    " + count * 20 + " s. ");
                        }
                        if (!orderNew.getStatus().equals("NEW")) {
                            break;
                        }
                        if ((count >= 3 * waitOrderLimit) || (CopyBot.shouldCloseOrder(symbol))) {
                            orderNew = syncRequestClient.cancelOrder(symbol, orderId, null);
                            throw new IOException();
                        }
                        count++;

                    }

                    order = new ExecutedOrder();
                    order.setType(type);
                    order.setPrice(orderNew.getAvgPrice().doubleValue());
                    priceReal = orderNew.getAvgPrice().toString();
                    order.setCurrentStopLoss(stopPrice);
                    //order.setCurrentStopLoss((100.0 - (stopLossPercentage)) * alertPrice / (100.0));
                    order.setCurrentStopLoss(order.getPrice() - (ATR.floatValue() * 1.5));
                    order.setProffit(order.getPrice() + (ATR.floatValue() * 1));
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


            order.setCreationTime(System.currentTimeMillis());
        } catch (Exception e) {
            Log.info(getClass(), "Time out to buy " + symbol + ". Search next position ");
            CopyBot.closeOrder(symbol, 0.00, null, type);
            stopThread = true;
        }

        Log.info(getClass(), "Buy [" + type + "] ready : " + symbol + ", quantity: " + quantity + ",  " + priceReal);
    }

    private Double multikChange(Integer identLimitOredr) {
        if (type.equals("SHORT")) {
            return 1 + (double) identLimitOredr / (20 * 100);
        } else if (type.equals("LONG")) {
            return 1 - (double) identLimitOredr / (20 * 100);
        }
        return 1.00;
    }

private void createStopLoss(PositionSide typeOrder, String count, String price){

    //        String priceTmp = multiplyAndRound(alertPrice, multikChange(identLimitOredr));
            RequestOptions options = new RequestOptions();

            SyncRequestClient syncRequestClient = SyncRequestClient.create(BinanceUtils.getApiKey(), BinanceUtils.getApiSecret(),
            options);

            if (idStopLossOrder != 0L) {
                syncRequestClient.cancelOrder(symbol, idStopLossOrder,quantity);
            }
            Order orderNew = syncRequestClient.postOrder(
            symbol, // Торговая пара
            OrderSide.SELL, // Тип ордера - продажа
            //PositionSide.LONG, // Открытая позиция - длинная
            typeOrder,
            OrderType.STOP_MARKET, // Тип ордера - стоп-ордер
            TimeInForce.GTC, // Тайминг выполнения ордера: GTC (Good 'Till Canceled)
            quantity, // Количество актива, который ты продаешь
            null, // Цена исполнения ордера (не используется для STOP ордера)
            null, // Цена исполнения тейк-профита (если нужен)
            price , // Цена исполнения стоп-лимита (если нужен)
            price, // Стоп-цена (trigger price) - цена, при которой ордер будет активирован
            null , // Количество тейк-профита (если нужен)
            quantity, // Количество стоп-лимита (если нужен)
            null, // Отдельная стратегия исполнения, если есть
            null, // Время исполнения, если требуется
            null, // Рабочий процесс исполнения (например, если используешь OCO или другие сложные типы)
            NewOrderRespType.RESULT // Тип ответа, который ты ожидаешь (например, полное подтверждение о новом ордере)
    );
            idStopLossOrder = orderNew.getOrderId();
}
}

