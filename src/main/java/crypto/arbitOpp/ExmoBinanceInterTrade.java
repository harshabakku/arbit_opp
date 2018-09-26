package crypto.arbitOpp;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Hello world!
 *
 */
public class ExmoBinanceInterTrade {
	public static void main(String[] args) throws Exception {
		JSONObject exmoPrices = getExmoData();
		Map<String, Double> binancePriceMap = getBinanceData();
		Double dollarRate = getDollarRate();
		System.out.println(" dollar rate should be around 64 "+ dollarRate);
        
		System.out.println(calculatePercentageDif(new Double(1),new Double( 1.3)) + "  should be 30 percent");
		
		System.out.println(calculatePercentageDif(new Double(1),new Double( 0.7))+ "  should be -30 percent");
		
		System.out.println(calculatePercentageDif(new Double(3),new Double( 3.5))+ " should be 16.66666666 percent");
		
		System.out.println(" ");
		
		
		for (String crypto : Arrays.asList("XRP", "ETH", "LTC", "BTC", "EOS", "ETC", "XLM" )) {
		calculatePricePercentageDiff( crypto+ "_USD", crypto+"USDT", exmoPrices, binancePriceMap, dollarRate);
		}
		for (String crypto : Arrays.asList("XRP", "ETH", "LTC", "BTC", "ETC", "XLM" )) {
			calculatePricePercentageDiffRUB( crypto+ "_RUB", crypto+"USDT", exmoPrices, binancePriceMap, getRUBRate());
			}
		
//		calculatePricePercentageDiffXRP( "XRP", "XRPBTC", exmoPrices, binancePriceMap, dollarRate);
//		
//		calculatePricePercentageDiffXRP( "OMG", "OMGBTC", exmoPrices, binancePriceMap, dollarRate);
//		
//		calculatePricePercentageDiffXRP( "REQ", "REQBTC", exmoPrices, binancePriceMap, dollarRate);
//		
//		calculatePricePercentageDiffXRP( "ZRX", "ZRXBTC", exmoPrices, binancePriceMap, dollarRate);
//		
////		calculatePricePercentageDiffXRP( "GNT", "GNTBTC", exmoPrices, binancePriceMap, dollarRate);
//		
//		calculatePricePercentageDiffXRP( "BAT", "BATBTC", exmoPrices, binancePriceMap, dollarRate);
//		
//		calculatePricePercentageDiffXRP( "AE", "AEBTC", exmoPrices, binancePriceMap, dollarRate);
//		
//		calculatePricePercentageDiffXRP( "TRX", "TRXBTC", exmoPrices, binancePriceMap, dollarRate);
//		
//		calculatePricePercentageDiffXRP( "XLM", "XLMBTC", exmoPrices, binancePriceMap, dollarRate);
////		
//
		
		//write all data append to a csv file, whenever the code is run for now including timestamp
		
		
	}

	private static void calculatePricePercentageDiff(String exmoPair,String binancePair, JSONObject exmoPrices, Map<String, Double> binancePriceMap, Double dollarRate) {
		JSONObject exmoPriceObject = (JSONObject)exmoPrices.get(exmoPair);
		
		Double exmoPrice = Double.valueOf((String) exmoPriceObject.get("last_trade"));
		Double binanceUSDPrice = binancePriceMap.get(binancePair);
		
		Double binancePriceInINR = binanceUSDPrice*dollarRate;
		
		System.out.println(exmoPair + " "+binancePair + ": Exmo: "+ exmoPrice+ ", BinanceUSD: "+ binanceUSDPrice +", inINR: " +binancePriceInINR);
		
		System.out.println("exmo to binance: "+ calculatePercentageDif(exmoPrice, binanceUSDPrice));
		System.out.println("binance to exmo: "+ calculatePercentageDif(binanceUSDPrice, exmoPrice));
		System.out.println( " ");
	}
	

	private static void calculatePricePercentageDiffRUB(String exmoPair,String binancePair, JSONObject exmoPrices, Map<String, Double> binancePriceMap, Double dollarRUBRate) {
		JSONObject exmoPriceObject = (JSONObject)exmoPrices.get(exmoPair);
		
		Double exmoPrice = Double.valueOf((String) exmoPriceObject.get("last_trade"));
		Double binanceUSDPrice = binancePriceMap.get(binancePair);
		
		Double binancePriceInRUB = binanceUSDPrice*dollarRUBRate;
		
		System.out.println(exmoPair + " "+binancePair + ": Exmo: "+ exmoPrice+ ", BinanceUSD: "+ binanceUSDPrice +", inRUB: " +binancePriceInRUB);
		
		System.out.println("exmo to binance: "+ calculatePercentageDif(exmoPrice, binancePriceInRUB));
		System.out.println("binance to exmo: "+ calculatePercentageDif(binancePriceInRUB, exmoPrice));
		System.out.println( " ");
	}

	private static void calculatePricePercentageDiffXRP(String exmoPair,String binancePair, JSONObject exmoPrices, Map<String, Double> binancePriceMap, Double dollarRate) {
		Double exmoPrice = Double.valueOf((String)exmoPrices.get(exmoPair));
		
		Double binancePriceInBTC =  binancePriceMap.get(binancePair);
		
		Double binancePriceInUSD = binancePriceMap.get("BTCUSDT")*binancePriceInBTC;
		
		Double binancePriceInINR = binancePriceInUSD*dollarRate;
		
		System.out.println(exmoPair+" "+ binancePair + ": Exmo: "+ exmoPrice+ ", BinanceUSD: "+ binancePriceInUSD +", inINR: " +binancePriceInINR);
		
		System.out.println("exmo to binance: "+ calculatePercentageDif(exmoPrice, binancePriceInINR));
		System.out.println("binance to exmo: "+ calculatePercentageDif(binancePriceInINR, exmoPrice));
		System.out.println( " ");
	}
	
	private static Double calculatePercentageDif(Double start,Double end){
		
		Double percentageDif = ((end - start)/start)*100.0;
		
		
		return percentageDif;
		
		
		
	}

	private static Double getDollarRate() throws Exception {

		String url = "http://data.fixer.io/api/latest?access_key=e7f958395688aca077db38a8bedb6508&symbols=USD,INR";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		JSONObject rates = (JSONObject) jsonObject.get("rates");
		//base currency is eur here
		Double inr = (Double) rates.get("INR");
		Double usd = (Double) rates.get("USD");
		Double dollarRate = inr/usd;
		return dollarRate;
	}

	private static Double getRUBRate() throws Exception {

		String url = "http://data.fixer.io/api/latest?access_key=e7f958395688aca077db38a8bedb6508&symbols=USD,RUB";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		JSONObject rates = (JSONObject) jsonObject.get("rates");
		//base currency is eur here
		Double inr = (Double) rates.get("RUB");
		Double usd = (Double) rates.get("USD");
		Double dollarRate = inr/usd;
		return dollarRate;
	}
	public static JSONObject getExmoData() throws Exception {
		String url = "https://api.exmo.com/v1/ticker/";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		
		return jsonObject;
	}

	public static Map<String, Double> getBinanceData() throws Exception {
		String url = "https://api.binance.com/api/v1/ticker/allPrices";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONArray jsonObject = (JSONArray) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		Iterator<JSONObject> iterator = jsonObject.iterator();

		Map<String, Double> binancePrices = new HashMap<String, Double>();
		while (iterator.hasNext()) {
			JSONObject priceObj = iterator.next();
			binancePrices.put((String) priceObj.get("symbol"), Double.valueOf((String) priceObj.get("price")));

		}

//		System.out.println("binance Prices: "+ binancePrices);
		return binancePrices;
	}

}
