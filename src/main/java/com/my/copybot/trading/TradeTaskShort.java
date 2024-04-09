/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import com.my.copybot.model.ExecutedOrderShort;
import com.my.copybot.util.BinanceUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TradeTaskShort implements Runnable {

	private final String symbol;
	private final Double alertPrice;
	private final Double btcAmount;
        private final Double usdtAmount;
	private final Double stopLossPercentage;
	private final boolean doTrailingStop;
	
	private ExecutedOrderShort order = null;
	private boolean error = false;
	private String errorMessage = "";

	private final BinanceApiRestClient client;
	private final BinanceApiWebSocketClient liveClient;
	
	private Long lastPriceLog = 0L;
        public Thread thisThread;
        private final boolean makeAvg;
		private final Integer stopNoLoss;

        public TradeTaskShort(BinanceApiRestClient client, BinanceApiWebSocketClient liveClient, String symbol, Double alertPrice, Double btcAmount, Double usdtAmount,
			Double stopLossPercentage, boolean doTrailingStop,boolean makeAvg, Integer stopNoLoss) {
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
			CopyBot.closeOrder(symbol, null, e.getMessage(),0);
		}

	}

	private synchronized void buy() throws GeneralException {
		String quantity = getAmount(alertPrice);
		Log.info(getClass(), "Trying to buy " + symbol + ", quantity: " + quantity);
		//NewOrder newOrder = marketBuy(symbol, quantity);
              //NewOrder newOrder = marketBuy(symbol, quantity);
                //Order newOrder = Order(symbol,quantity);
                String priceReal="";
		try {
                    
                    
                    RequestOptions options = new RequestOptions();
                    SyncRequestClient syncRequestClient = SyncRequestClient.create(BinanceUtils.getApiKey(), BinanceUtils.getApiSecret(),
                                options);
			// By now we will not be creating real orders

                        Order orderNew = syncRequestClient.postOrder(symbol, 
                                OrderSide.SELL, PositionSide.SHORT, OrderType.MARKET, null,quantity,
                            null, null, null,null, null, null, null, null, null, 
                                NewOrderRespType.RESULT);

               //         System.out.println(orderNew.toString());
                        order = new ExecutedOrderShort();
                        order.setSymbol(symbol);
                        order.setQuantity(quantity);
                        order.setQuantityDouble(getAmountDouble(alertPrice));
                                // orderNew.getPositionSide());
                        order.setPrice(orderNew.getAvgPrice().doubleValue());
                        priceReal = orderNew.getAvgPrice().toString();
                        order.setPriceAvg((100 + (stopLossPercentage/2)) * alertPrice / (100.0));
                        order.setCurrentStopLoss((100 + stopLossPercentage) * alertPrice / (100.0));
                        order.setInitialStopLoss(order.getCurrentStopLoss());
                        order.setOrderId(orderNew.getClientOrderId());                      
                         
		} catch (Exception e) {
                          sell(alertPrice);  
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
                Log.info(getClass(), "Buy [SHORT] ready : " + symbol + ", quantity: " + quantity+",  "+ priceReal);
	}
	private void buySecond() throws GeneralException {
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
        
	private void sell(Double price) {
		try {
//			NewOrder newOrder = marketSell(symbol, String.valueOf(order.getQuantity()));
//			client.newOrder(newOrder);

                        RequestOptions options = new RequestOptions();
                        SyncRequestClient syncRequestClient = SyncRequestClient.create(BinanceUtils.getApiKey(), BinanceUtils.getApiSecret(),
                                options);
			// By now we will not be creating real orders

                        Order orderNew = syncRequestClient.postOrder(symbol, 
                                OrderSide.BUY, PositionSide.SHORT, OrderType.MARKET, null,order.getQuantity(),
                            null, null, null,null, null, null, null, null, null, 
                                NewOrderRespType.RESULT);

                              
			Log.info(getClass(), "Created CLOSE order: " + order.getOrderId()+ " "+ order.getSymbol());
			order.setClosePrice(price);
			order.setCloseTime(System.currentTimeMillis());
                        CopyBot.closeOrder(symbol, order.getProfit(), null,0);
//                        if(Thread.currentThread().isInterrupted()) {
//                            break;}

		} catch (Exception e) {
                        System.out.println(" --------------------------- " + symbol + "   closed");
			Log.severe(getClass(), "Unable to sell!", e);
                // CopyBotSpot.closeOrder(symbol, order.getProfit(), null);
		}
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
        
	private  void monitorPrice() {
		liveClient.onCandlestickEvent(symbol.toLowerCase(),
				CandlestickInterval.ONE_MINUTE, response -> {
                    try {
                        checkPrice(Double.valueOf(response.getClose()));
                    } catch (GeneralException ex) {
                        Logger.getLogger(TradeTaskShort.class.getName()).log(Level.SEVERE, null, ex);
                    }
				});
	}

	private void checkPrice(Double price) throws GeneralException {
		Long now = System.currentTimeMillis();
                // Change p
//                if (Double.parseDouble(order.getCurrentProfit(price)) > 2) {
//                                  order.setCurrentStopLoss(order.getPrice()*1.1);
//                        }
		// This is a bit harcoded, but just trying to avoid too many logs..
		if((now - lastPriceLog) > 60 * 1000L) {
                                      String proffit = order.getCurrentProfit(price).replace(",", ".");
            Double chkProffit =  Double.parseDouble(proffit);
                                    if (chkProffit > stopNoLoss) {
                                        // Uppper StoppLoss level
                                                  order.setCurrentStopLoss(setStopLoss(chkProffit));
										System.out.println("\u001B[33m !!!-------------Change StopLoss for " + symbol + " to " + showPrice(order.getCurrentStopLoss()) + "\u001B[0m");
                                    }
                                    
                                Log.info(getClass(),
					"SHORT: " + symbol + ". Current price: " + showPrice(price)
							+ ", buy price: " + showPrice(order.getPrice())
							+ ", stoploss: "
							+ showPrice(order.getCurrentStopLoss())
							+ ", current profit: " + order.getCurrentProfit(price) + "%");
			lastPriceLog = now;
		}
                // Implement avg buy
//                if (makeAvg &&(price > order.getPriceAvg()))
//                {
//                    makeAvg = false;
//                    buySecond();
//                }
                
		//if (trailingStopShouldCloseOrder(price) || CopyBotSpot.shouldCloseOrder(symbol)) //{		
                if (price >=  order.getCurrentStopLoss() || CopyBot.shouldCloseOrder(symbol) )      // Close stopLoss
                {
			sell(price);
			Log.info(getClass(), "[STOP][SHORT] :  ---------  Closed order for symbol: " + symbol 
					+ ". Current price: " + showPrice(price) + ", profit: " + order.getProfit());
			//CopyBotSpot.closeOrder(symbol, order.getProfit(), null);
                        thisThread.stop();
                        //thisThread.
                        
		}
//		if (doTrailingStop && price > order.getPrice()) { 
//			// Trailing stop, price is higher, update stoploss
//			Double newStopLoss = (100.0 - (stopLossPercentage)) * price / 100.0;
//			if (order.getCurrentStopLoss() < newStopLoss) {
//				order.setCurrentStopLoss(newStopLoss);
//			}
//			Log.info(getClass(),"Symbol: " + symbol + ". Trailing stop; increasing stoploss to "
//					+ showPrice(order.getCurrentStopLoss()));
//		}
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

	public synchronized ExecutedOrderShort getOrder() {
		return order;
	}

	private String showPrice(Double price) {
		return String.format("%.8f", price);
	}

	public synchronized String getSymbol() {
		return symbol;
	}
        
        private synchronized Double setStopLoss(Double chkProffit){
            Double proffitNew = 0.00;
           if (chkProffit > 350.00) {
                                        // Uppper StoppLoss level
                                            proffitNew = order.getPrice()*0.6;
                                            return  proffitNew;
                                    }
                                    else if (chkProffit > 250.00) {
                                            proffitNew = order.getPrice()*0.78;
                                            return  proffitNew;                                        
                                    }
                                    else if (chkProffit > 200.00) {
                                            proffitNew = order.getPrice()*0.83;
                                            return  proffitNew;                                        
                                    }
                                    else if (chkProffit > 150.00) {
                                            proffitNew = order.getPrice()*0.88;
                                            return  proffitNew;                                        // Uppper StoppLoss level
                                    }
                                    else if (chkProffit > 100.00) {
                                            proffitNew = order.getPrice()*0.92;
                                            return  proffitNew;                                        // Uppper StoppLoss level
                                    }
                                    else  if (chkProffit > 70.00) {
                                            proffitNew = order.getPrice()*0.94;
                                            return  proffitNew;                                        // Uppper StoppLoss level
                                    }
                                    else  if (chkProffit >40 ) {
			   proffitNew = order.getPrice() * 0.985;
                                            return  proffitNew;                                        
                                    } else if (chkProffit > 30) {
			   proffitNew = order.getPrice() * 0.99;
			   return proffitNew;
		   }
                                    else {
			   proffitNew = order.getPrice() * 0.9925;
                                            return  proffitNew;
                                    }
        
}
}

