package crypto.arbitOpp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jws.Oneway;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Hello world!
 *
 */
public class WazirxCexInterTrade {
	public static void main(String[] args) throws Exception {


//        System.out.println(calculatePercentageDif(new Double(1),new Double( 1.3)) + "  should be 30 percent");
//		
//		System.out.println(calculatePercentageDif(new Double(1),new Double( 0.7))+ "  should be -30 percent");
//		
		System.out.println(calculatePercentageDif(new Double(3),new Double( 3.5))+ " should be 16.66666666 percent\n");
//		
		
		 JSONObject wazirxPrices = getWazirxData();
		 Map<String, Map<String, Double>> cexPairTickers = getCexData();
		
		 Double dollarRate = getDollarRate();
		 Double oneEuroInINR = getEuroRate();
		 System.out.println("dollar rate should be around 64 "+ dollarRate+ " euro rate : "+ oneEuroInINR);
		

		 System.out.println(" ");
		
		 calculatePricePercentageDiff( "btcinr", "BTC:USD", wazirxPrices, cexPairTickers,
		 dollarRate);
		
		 calculatePricePercentageDiff( "ethinr", "ETH:USD", wazirxPrices, cexPairTickers,
		 dollarRate);
		
		 
		 calculatePricePercentageDiff( "xrpinr", "XRP:USD", wazirxPrices, cexPairTickers,
		 dollarRate);
		
		
		 System.out.println("################ percentage dif for euro rate ############## \n");
		
		 calculatePricePercentageDiff( "btcinr", "BTC:EUR", wazirxPrices, cexPairTickers,
		 oneEuroInINR);
		
		 calculatePricePercentageDiff( "ethinr", "ETH:EUR", wazirxPrices, cexPairTickers,
		 oneEuroInINR);
		
		 calculatePricePercentageDiff( "xrpinr", "XRP:EUR", wazirxPrices, cexPairTickers,
		 oneEuroInINR);

	}

	private static void calculatePricePercentageDiff(String wazirxPair, String cexPair, JSONObject wazirxPrices,
			Map<String, Map<String, Double>> cexPairTickers, Double dollarRate) {
		Double wazirxBuyPrice = Double.valueOf((String)((JSONObject) wazirxPrices.get(wazirxPair)).get("buy"));
        
		Double wazirxSellPrice = Double.valueOf((String)((JSONObject) wazirxPrices.get(wazirxPair)).get("sell"));
		// selling in cex
		Double cexUSDPrice = cexPairTickers.get(cexPair).get("bid");

		Double cexPriceInINR = cexUSDPrice * dollarRate;

		System.out
				.println(cexPair + ": wazirx: " + wazirxBuyPrice + ", to cex: " + cexUSDPrice + ", inINR: " + cexPriceInINR);

		Double tempStoretoPrint = calculatePercentageDif(wazirxBuyPrice, cexPriceInINR);
		System.out.println("wazirx to cex: " + tempStoretoPrint+ "\n----");

		// buy from cex
		cexUSDPrice = cexPairTickers.get(cexPair).get("ask");

		cexPriceInINR = cexUSDPrice * dollarRate;

		System.out
				.println(cexPair + ": wazirx: " + wazirxSellPrice + ", cex: " + cexUSDPrice + ", inINR: " + cexPriceInINR);

		System.out.println("cex to wazirx: " + calculatePercentageDif(cexPriceInINR, wazirxSellPrice));
		System.out.println("\n ");
	}

	private static Double calculatePercentageDif(Double start, Double end) {

		Double percentageDif = ((end - start) / start) * 100.0;

		return percentageDif;

	}



	private static Double getEuroRate() throws Exception {

		String url = "http://data.fixer.io/api/latest?access_key=e7f958395688aca077db38a8bedb6508&symbols=EUR,INR";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		JSONObject rates = (JSONObject) jsonObject.get("rates");
		//base currency is eur here
		Double inr = (Double) rates.get("INR");
//		Double usd = (Long) rates.get("EUR");
//		Double dollarRate = inr/usd;
		return inr;
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

	public static JSONObject getWazirxData() throws Exception {
		String url = "https://api.wazirx.com/api/v2/tickers";
		JSONObject jsonObject = getResponse(url);
//		JSONObject prices = (JSONObject) ((JSONObject) jsonObject.get("prices")).get("inr");
//		System.out.println("wazirx Prices "+ jsonObject.toString());
		return jsonObject;
	}

	public static Map<String, Map<String, Double>> getCexData() throws Exception {

		String url = "https://cex.io/api/tickers/USD/EUR";
		JSONObject jsonObject = getResponse(url);

		// System.out.println(jsonObject);
		JSONArray dataArray = (JSONArray) jsonObject.get("data");
		Iterator<JSONObject> iterator = dataArray.iterator();

		Map<String, Map<String, Double>> cexPairTickers = new HashMap<String, Map<String, Double>>();
		while (iterator.hasNext()) {
			JSONObject priceObj = iterator.next();
			HashMap<String, Double> priceMap = new HashMap<String, Double>();
			priceMap.put("ask", Double.valueOf(String.valueOf(priceObj.get("ask"))));
			priceMap.put("bid", Double.valueOf(String.valueOf(priceObj.get("bid"))));

			cexPairTickers.put((String) priceObj.get("pair"), priceMap);

		}

		// System.out.println("cex Prices: " + cexPairTickers);
		return cexPairTickers;
	}

	private static JSONObject getResponse(String url)
			throws MalformedURLException, IOException, ProtocolException, ParseException, UnsupportedEncodingException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		// url is forbidden , fake browser hit
		con.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		return jsonObject;
	}

}
