
package com.mycompany.copybotspot.util;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.BinanceApiWebSocketClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.api.client.domain.market.TickerPrice;
import com.binance.client.RequestOptions;
import com.binance.client.SyncRequestClient;
import com.mycompany.copybotspot.PrivateConfig;
import com.mycompany.copybotspot.exceptions.GeneralException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

public class BinanceUtils {

	private static String API_KEY;
	private static String API_SECRET;

	private static BinanceApiRestClient client = null;
	private static BinanceApiWebSocketClient liveClient = null;

            public static List<String> getBitcoinSymbols() throws GeneralException, MalformedURLException, Exception {
            
              URL url = new URL ("https://www.binance.com/fapi/v1/premiumIndex");
           
           URLConnection urc = url.openConnection();
           System.out.println(" connected true ");
           
           BufferedReader in = new BufferedReader(
                new InputStreamReader (urc.getInputStream()));
           
           String inputLine;
           inputLine = in.readLine();
           in.close();

                List<String> symbols = new LinkedList<String>();
                symbols = parserJsonMy(inputLine);
                
               // BinanceApiRestClient client = getRestClient();
	//	List<TickerPrice> prices = client.getAllPrices();
            
//		List<String> symbols = new LinkedList<String>();
//		BinanceApiRestClient client = getRestClient();
//		List<TickerPrice> prices = client.getAllPrices();
//		for (TickerPrice tickerPrice : prices) {
//			if (StringUtils.endsWith(tickerPrice.getSymbol(), "USDT")) {
//				symbols.add(tickerPrice.getSymbol());
//			}
//		}
		return symbols;
	}

	public static List<Candlestick> getCandlestickBars(String symbol,
			CandlestickInterval interval) throws GeneralException {
		try {
                    

			return getRestClient().getCandlestickBars(symbol, interval);
		} catch (Exception e) {
			throw new GeneralException(e);
                        
		}
	}

	public static List<Candlestick> getLatestCandlestickBars(String symbol,
			CandlestickInterval interval) throws GeneralException {
		try {
			return getRestClient().getCandlestickBars(symbol, interval, 2,
					null, null);
		} catch (Exception e) {
			//throw new GeneralException(e);
                        System.out.print("---"+ symbol);           
                        return null; 
		}
	}

	public static BinanceApiRestClient getRestClient() throws GeneralException {
		if (client == null) {
			try {

				BinanceApiClientFactory factory = BinanceApiClientFactory
						.newInstance(API_KEY, API_SECRET);
                                                
                                
                                client = factory.newRestClient();
                                                          
                       
			} catch (Exception e) {
				throw new GeneralException(e);
			}
		}
		return client;

	}
	
	public static BinanceApiWebSocketClient getWebSocketClient() throws GeneralException {
		if(liveClient == null) {
			try {
				BinanceApiClientFactory factory = BinanceApiClientFactory
						.newInstance(API_KEY, API_SECRET);
				liveClient = factory.newWebSocketClient();
			} catch (Exception e) {
				throw new GeneralException(e);
			}
		}
		return liveClient;		
	}
	
	public static void init(String binanceApiKey, String binanceApiSecret) throws GeneralException {
		if(StringUtils.isEmpty(binanceApiKey) || StringUtils.isEmpty(binanceApiSecret)) {
			throw new GeneralException("Binance API params cannot be empty; please check the config properties file");
		}
		API_KEY = binanceApiKey;
		API_SECRET = binanceApiSecret;
	}
        
                public static List<String> parserJsonMy(String string) throws Exception{
       
                    List<String> names = new ArrayList<>();
                   
              try {
                    JSONArray array = new JSONArray(string);
                  
                    for (int i = 0; i < array.length(); i++) {
                            JSONObject objectInArray = array.getJSONObject(i);
                              String temp = objectInArray.getString("symbol");
                          //  String temp = objectInArray.getString("symbol")+";"+objectInArray.getString("markPrice");
                            if (temp.contains("USDT")){
                                names.add(temp);}
                    }              
                    } catch (Exception e) {
                           System.out.print(e);
                     }
       
        return names;
        
    }

}
