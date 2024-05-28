
package com.my.copybot.util;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.my.copybot.exceptions.GeneralException;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class BinanceUtils {

	private static String API_KEY;
	private static String API_SECRET;

//	private static BinanceApiRestClient client = null;
//	private static BinanceApiWebSocketClient liveClient = null;

	public static String getApiKey() {
		return API_KEY;
	}

	public static void setApiKey(String apiKey) {
		API_KEY = apiKey;
	}

	public static String getApiSecret() {
		return API_SECRET;
	}

	public static void setApiSecret(String apiSecret) {
		API_SECRET = apiSecret;
	}

	public static List<String> getBitcoinSymbols() throws Exception {

		URL url = new URL("https://www.binance.com/fapi/v1/premiumIndex");
		URLConnection urc = url.openConnection();

        BufferedReader in = new BufferedReader(
                new InputStreamReader(urc.getInputStream()));

        String inputLine;
        inputLine = in.readLine();
        in.close();

        List<String> symbols = new LinkedList<String>();
        symbols = parserJsonMy(inputLine);

		return symbols;
	}

//	public static List<Candlestick> getCandlestickBars(String symbol,
//                                                       CandlestickInterval interval) throws GeneralException {
//		try {
//			return getRestClient().getCandlestickBars(symbol, interval);
//		} catch (Exception e) {
//			throw new GeneralException(e);
//
//		}
//	}

//	public static List<Candlestick> getLatestCandlestickBars(String symbol,
//                                                             CandlestickInterval interval) throws GeneralException {
//		try {
//			return getRestClient().getCandlestickBars(symbol, interval, 2,
//					null, null);
//		} catch (Exception e) {
//			//throw new GeneralException(e);
//            System.out.print("---" + symbol);
//            return null;
//		}
//	}

//	public static BinanceApiRestClient getRestClient() throws GeneralException {
//		if (client == null) {
//			try {
//                System.setProperty("wss://stream.binance.com:9443/ws", "wss://ws-fapi.binance.com/ws-fapi/v1");
//				BinanceApiClientFactory factory = BinanceApiClientFactory
//						.newInstance(API_KEY, API_SECRET);
//
//                client = factory.newRestClient();
//
//
//			} catch (Exception e) {
//				throw new GeneralException(e);
//			}
//		}
//		return client;
//
//	}

//	public static BinanceApiWebSocketClient getWebSocketClient() throws GeneralException {
//		if(liveClient == null) {
//			try {
//				String futuresBaseUrl = "https://fapi.binance.com";
//                //	System.setProperty("wss://stream.binance.com:9443/ws", "wss://ws-fapi.binance.com/ws-fapi/v1");
//
//				BinanceApiClientFactory factory = BinanceApiClientFactory
//						.newInstance(API_KEY, API_SECRET);
//
//
//				liveClient = factory.newWebSocketClient();
//
//			} catch (Exception e) {
//				throw new GeneralException(e);
//			}
//		}
//        return liveClient;
//	}

    public static void init(String binanceApiKey, String binanceApiSecret) throws GeneralException {
		if(StringUtils.isEmpty(binanceApiKey) || StringUtils.isEmpty(binanceApiSecret)) {
			throw new GeneralException("Binance API params cannot be empty; please check the config properties file");
		}
		API_KEY = binanceApiKey;
		API_SECRET = binanceApiSecret;
	}

    public static List<String> parserJsonMy(String string) throws Exception {

        List<String> names = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(string);

            for (int i = 0; i < array.length(); i++) {
                JSONObject objectInArray = array.getJSONObject(i);
                String temp = objectInArray.getString("symbol");
                if (temp.contains("USDT")) {
                    names.add(temp);
                }
            }
        } catch (Exception e) {
            System.out.print(e);
        }

        return names;

    }

	public static List<Candlestick> getLatestCandlestickBars(String symbol,
															 CandlestickInterval interval) throws GeneralException {
		try {
			return getCandelSeries(symbol, interval.getIntervalId(), 2);//getRestClient().getCandlestickBars(symbol, interval, 2,
//					null, null);
		} catch (Exception e) {
			//throw new GeneralException(e);
			System.out.print("---" + symbol);
			return null;
		}
	}

	public static List<Candlestick> getCandelSeries(String symbol, String interval, Integer limit) {
		try {
			URL url = new URL("https://www.binance.com/fapi/v1/klines?symbol=" + symbol + "&interval=" + interval + "&limit=" + limit);
			URLConnection urc = url.openConnection();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(urc.getInputStream()));
			String inputLine;
			inputLine = in.readLine();
			JSONArray array = new JSONArray(inputLine);
			List<Candlestick> candela = new ArrayList<>();
			for (int i = 0; i < array.length(); i++) {
				String str = String.valueOf(array.get(i)).substring(1, String.valueOf(array.get(i)).length() - 2);
				String[] strArray = str.split(",");
				Candlestick candlestick = new Candlestick();
				candlestick.setOpenTime(Long.valueOf(strArray[0]));
				candlestick.setCloseTime(Long.valueOf(strArray[6]));
				candlestick.setOpen(strArray[1]);
				candlestick.setHigh(strArray[2]);
				candlestick.setLow(strArray[3]);
				candlestick.setClose(strArray[4]);
				candlestick.setVolume(strArray[7]);
				candela.add(candlestick);
			}
			in.close();
			return candela;

		} catch (Exception ex) {
			System.out.println(" connected false " + ex);
			return null;
		}
	}
}