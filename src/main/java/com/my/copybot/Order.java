/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.my.copybot;

//import com.binance.api.client.domain.OrderSide;
//import com.binance.api.client.domain.OrderType;
//import com.binance.api.client.domain.TimeInForce;

import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.enums.NewOrderRespType;
import com.binance.client.model.enums.OrderSide;
import com.binance.client.model.enums.OrderType;
import com.binance.client.model.enums.PositionSide;
import com.binance.client.model.enums.TimeInForce;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import com.my.copybot.util.BinanceUtils;
import org.ta4j.core.Decimal;

import java.util.ArrayList;


/**
 *
 * @author radomir
 */
public class Order {
    
    /**
     *
     */
            private String symbol;
            private Decimal price;
            ArrayList<String> orderList;

    public Order(String symbol, Decimal price) {
        this.symbol = symbol;
        this.price = price;
    }
            
      
        public boolean addNewOrder(String symbol,Decimal price ){
        
            
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(BinanceUtils.getApiKey(), BinanceUtils.getApiSecret(),
                options);
        System.out.println();
  //      System.out.println(syncRequestClient.getSymbolOrderBookTicker(symbol));
    //    System.out.println();
    //    System.out.println(syncRequestClient.getOldTrades(symbol, 5, null));
    //    System.out.println();        
        
        // System.out.println(syncRequestClient.getSymbolOrderBookTicker(null));
        System.out.println(syncRequestClient.getBalance().get(6));
    //            System.out.println();        
 //       System.out.println(syncRequestClient.getAllOrders("DOGEUSDT", null, null, null, 10));
            Decimal count = Decimal.valueOf(20 / price.toDouble());
            String str="";
            if (price.toDouble() > 200) {
                    str = String.format("%.6f",count.toDouble());}
            else if (price.toDouble() > 50) {
                    str = String.format("%.3f",count.toDouble());}
            else if (price.toDouble() > 5) {
                    str = String.format("%.1f",count.toDouble());}
            
            else { str = String.format("%.0f",count.toDouble());
            
            }
     //       Decimal countNew  = Decimal.valueOf(Double.parseDouble(str)); 
            str = str.replace(",", ".");
            Decimal stopPrice = Decimal.valueOf(price.toDouble()-0.02*count.toDouble());
         
   
        

      //      System.out.println("--------------------"+strCount);
        try{    
                if (syncRequestClient.getOpenOrders(symbol).isEmpty()){
                   System.out.println(syncRequestClient.postOrder(symbol, 
                 OrderSide.BUY,
                 PositionSide.LONG, OrderType.MARKET, null,
                 str,
                 null, null, null,null, null, null, null, null, null, 
                NewOrderRespType.RESULT));
                   
//                                      System.out.println(syncRequestClient.postOrder(symbol, 
//                 OrderSide.BUY,
//                 PositionSide.LONG, OrderType.MARKET, TimeInForce.GTC,
//                 str,
//                 price.toString(), null, null,null, null, null, null, null, null, 
//                NewOrderRespType.RESULT));

                   
//         Decimal stopPrice = Decimal.valueOf(price.toDouble()-0.02*count.toDouble());
//         String str1 = String.format("%.8f",stopPrice.toDouble());
//         System.out.println(syncRequestClient.postOrder(symbol, 
//                 OrderSide.BUY,
//                 PositionSide.LONG, OrderType.STOP, TimeInForce.GTC,
//                 str,
//                 str1, null, null,null, null, null, null, null, null, 
//                NewOrderRespType.RESULT));
       
                orderList.add(symbol+";"+str);
       }
        } catch (Exception e)
                {
                    System.out.println("----------------"+symbol + "   Count: "+ str+"  Error:  "  +e);
               }
        System.out.println();  

          
            
            return false;
        }
             
        public void checkPosition(String symbol) {
        
            
        
        }
                
            

}
