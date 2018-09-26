package crypto.arbitOpp;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Hello world!
 *
 */
public class BelfricsExInterTrade {
	public static void main(String[] args) throws Exception {
		        
		System.out.println(calculatePercentageDif(new Double(1),new Double( 1.3)) + "  should be 30 percent");
		
		System.out.println(calculatePercentageDif(new Double(1),new Double( 0.7))+ "  should be -30 percent");
		
		System.out.println(calculatePercentageDif(new Double(3),new Double( 3.5))+ " should be 16.66666666 percent");
		
		
		
		Double oneDollarInInr = getDollarRateInINR();
		System.out.println(" dollar rate should be around 64 "+ oneDollarInInr);

	
		
		Map<String, Map<String, Double>> marketPairTickers = getBelfricsPriceData();

		System.out.println(" ");
		
		calculatePricePercentageDiff( "BETINR", "BETUSD", marketPairTickers, oneDollarInInr);
		calculatePricePercentageDiff( "BTCINR", "BTCUSD", marketPairTickers, oneDollarInInr);
		
		
		System.out.println("percentage dif for euro rate ");
		Double oneKESInINR = getEURORateInINR();
		
//        calculatePricePercentageDiff( "BTC", "BTC:EUR", koinexPrices, marketPairTickers, oneDollarInEuro);
//		
//		calculatePricePercentageDiff( "ETH", "ETH:EUR", koinexPrices, marketPairTickers, oneDollarInEuro);
//		
//		calculatePricePercentageDiff( "BCH", "BCH:EUR", koinexPrices, marketPairTickers, oneDollarInEuro);
//		
//		calculatePricePercentageDiff( "XRP", "XRP:EUR", koinexPrices, marketPairTickers, oneDollarInEuro);
//		
		
		
		
	}

	private static void calculatePricePercentageDiff(String inrPair,String usdPair, Map<String, Map<String, Double>> marketPairTickers, Double dollarRate) {
		Double koinexPrice = marketPairTickers.get(inrPair).get("ask");
		
		//selling in cex
		Double cexUSDPrice = marketPairTickers.get(usdPair).get("bid");
		
		Double cexPriceInINR = cexUSDPrice*dollarRate;
		
		System.out.println("INR TO USD: "+ inrPair + ": "+ koinexPrice+", " + usdPair + ": "+ cexUSDPrice +", inINR: " +cexPriceInINR);
		
		Double tempStoretoPrint = calculatePercentageDif(koinexPrice, cexPriceInINR);
		
		
	
		//buy from cex
		koinexPrice = marketPairTickers.get(inrPair).get("bid");
		
        cexUSDPrice = marketPairTickers.get(usdPair).get("ask");
		
		cexPriceInINR = cexUSDPrice*dollarRate;
		
		System.out.println("USD TO INR: "+ inrPair + ": "+ koinexPrice+", " + usdPair + ": "+ cexUSDPrice +", inINR: " +cexPriceInINR);
		
		System.out.println(inrPair + " to " +usdPair + ": "+ tempStoretoPrint);
		System.out.println(usdPair + " to " +inrPair + ": "+ calculatePercentageDif(cexPriceInINR, koinexPrice));
		System.out.println( " ");
	}
	
	
	
	
	private static Double calculatePercentageDif(Double start,Double end){
		
		Double percentageDif = ((end - start)/start)*100.0;
		
		
		return percentageDif;
		
		
		
	}

	
	private static Double getEURORateInINR() throws Exception {

		String url = "https://api.fixer.io/latest?base=EUR&symbols=EUR,INR";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		JSONObject rates = (JSONObject) jsonObject.get("rates");
		Double dollarRate = (Double) rates.get("INR");
		System.out.println("one euro in INR "+ dollarRate);
		return dollarRate;
	}
	
	private static Double getDollarRateInINR() throws Exception {

		String url = "https://api.fixer.io/latest?base=USD&symbols=USD,INR";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		JSONObject rates = (JSONObject) jsonObject.get("rates");
		Double dollarRate = (Double) rates.get("INR");
		return dollarRate;
	}

	

	public static Map<String, Map<String, Double>> getBelfricsPriceData() throws Exception {

		Map<String, Map<String, Double>> belfricsTickerDataMap = new HashMap<String, Map<String, Double>>();
		
		String baseUrl = "https://belfrics.io/belfrics/api/v1/marketalldata?marketName=";
		
		List<String> tickerPairs = Arrays.asList("BTCINR", "BETINR", "BTCUSD", "BETUSD", "ETHUSD", "LTCUSD" ,"XRPUSD", "BTCKES");
		Iterator<String> iterator = tickerPairs.iterator();
		while (iterator.hasNext()) {
			String tickerPair = iterator.next();
		URL obj = new URL(baseUrl+ tickerPair);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		// url hit from code is forbidden , fake browser hit
		con.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));

//		System.out.println(jsonObject);
		
		JSONArray dataArray = (JSONArray) jsonObject.get("data");
		JSONObject priceObj  = (JSONObject) dataArray.get(0);

		
			HashMap<String, Double> priceMap = new HashMap<String, Double>();
			priceMap.put("ask", Double.valueOf(String.valueOf(priceObj.get("AskPrice"))));
			priceMap.put("bid", Double.valueOf(String.valueOf(priceObj.get("BidPrice"))));

			belfricsTickerDataMap.put((String) priceObj.get("MarketName"), priceMap);

		}

		System.out.println("belfrics Prices INR, USD, KES: " + belfricsTickerDataMap);
		return belfricsTickerDataMap;
	}

}
