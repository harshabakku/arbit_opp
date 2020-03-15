package crypto.arbitOpp;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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
public class WazirxCexInterTrade {
	public static void main(String[] args) throws Exception {


		
		
		 JSONObject wazirxPrices = getWazirxData();
		 Map<String, Map<String, Double>> cexPairTickers = getCexData();
		
		 Double dollarRate = getDollarRate();
		 System.out.println(" dollar rate should be around 64 "+ dollarRate);
		
		 System.out.println(" ");
		
		 calculatePricePercentageDiff( "BTC", "BTC:USD", wazirxPrices, cexPairTickers,
		 dollarRate);
		
		 calculatePricePercentageDiff( "ETH", "ETH:USD", wazirxPrices, cexPairTickers,
		 dollarRate);
		
		 calculatePricePercentageDiff( "BCH", "BCH:USD", wazirxPrices, cexPairTickers,
		 dollarRate);
		
		 calculatePricePercentageDiff( "XRP", "XRP:USD", wazirxPrices, cexPairTickers,
		 dollarRate);
		
		
		 System.out.println("percentage dif for euro rate ");
		 Double oneDollarInEuro = getDollarRateInINR();
		
		 calculatePricePercentageDiff( "BTC", "BTC:EUR", wazirxPrices, cexPairTickers,
		 oneDollarInEuro);
		
		 calculatePricePercentageDiff( "ETH", "ETH:EUR", wazirxPrices, cexPairTickers,
		 oneDollarInEuro);
		
		 calculatePricePercentageDiff( "BCH", "BCH:EUR", wazirxPrices, cexPairTickers,
		 oneDollarInEuro);
		
		 calculatePricePercentageDiff( "XRP", "XRP:EUR", wazirxPrices, cexPairTickers,
		 oneDollarInEuro);

	}

	private static void calculatePricePercentageDiff(String koinexPair, String cexPair, JSONObject koinexPrices,
			Map<String, Map<String, Double>> cexPairTickers, Double dollarRate) {
		Double koinexPrice = Double.valueOf((String) koinexPrices.get(koinexPair));

		// selling in cex
		Double cexUSDPrice = cexPairTickers.get(cexPair).get("bid");

		Double cexPriceInINR = cexUSDPrice * dollarRate;

		System.out
				.println(cexPair + ": Koinex: " + koinexPrice + ", cex: " + cexUSDPrice + ", inINR: " + cexPriceInINR);

		Double tempStoretoPrint = calculatePercentageDif(koinexPrice, cexPriceInINR);

		// buy from cex
		cexUSDPrice = cexPairTickers.get(cexPair).get("ask");

		cexPriceInINR = cexUSDPrice * dollarRate;

		System.out
				.println(cexPair + ": Koinex: " + koinexPrice + ", cex: " + cexUSDPrice + ", inINR: " + cexPriceInINR);

		System.out.println("koinex to cex: " + tempStoretoPrint);
		System.out.println("cex to koinex: " + calculatePercentageDif(cexPriceInINR, koinexPrice));
		System.out.println(" ");
	}

	private static Double calculatePercentageDif(Double start, Double end) {

		Double percentageDif = ((end - start) / start) * 100.0;

		return percentageDif;

	}

	private static Double getDollarRateInINR() throws Exception {

		String url = "https://api.fixer.io/latest?base=EUR&symbols=EUR,INR";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		JSONObject rates = (JSONObject) jsonObject.get("rates");
		Double dollarRate = (Double) rates.get("INR");
		System.out.println("one euro in INR " + dollarRate);
		return dollarRate;
	}

	private static Double getDollarRate() throws Exception {

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

	public static JSONObject getWazirxData() throws Exception {
		String url = "https://koinex.in/api/ticker";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		JSONObject prices = (JSONObject) ((JSONObject) jsonObject.get("prices")).get("inr");
		// System.out.println("koinex Prices "+ jsonObject.toString());
		return prices;
	}

	public static Map<String, Map<String, Double>> getCexData() throws Exception {

		String url = "https://cex.io/api/tickers/USD/EUR";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		// url is forbidden , fake browser hit
		con.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));

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

}
