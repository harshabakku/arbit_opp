package crypto.arbitOpp;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Hello world!
 *
 */
public class CexIntraTradeUSDEUR {
	public static void main(String[] args) throws Exception {
		Double oneDollarInEuro = getDollarRateInEuro();
		System.out.println(" dollar rate for euro should be around 0.83, 1 dollar = " + oneDollarInEuro + " Euro");
		Map<String, Map<String, Double>> cexPairTickers = getCexData();

		System.out.println(calculatePercentageDif(new Double(1), new Double(1.3)) + "  should be 30 percent");

		System.out.println(calculatePercentageDif(new Double(1), new Double(0.7)) + "  should be -30 percent");

		System.out.println(calculatePercentageDif(new Double(3), new Double(3.5)) + " should be 16.66666666 percent");

		System.out.println(" ");

		Set<String> cryptoCoins = new HashSet<String>();
		for (String coinPair : cexPairTickers.keySet()) {
			String[] pairArray = coinPair.split(":");
			cryptoCoins.add(pairArray[0]);
		}
		System.out.println(cryptoCoins);
		for (String coin : cryptoCoins) {
			calculatePricePercentageDiff(coin, cexPairTickers, oneDollarInEuro);
		}

	}

	private static void calculatePricePercentageDiff(String coin, Map<String, Map<String, Double>> cexPairTickers,
			Double oneDollarInEuro) {

		String euroPair = coin + ":EUR";
		String usdPair = coin + ":USD";

		// buying in euro
		Double coinPriceInEuro = cexPairTickers.get(euroPair).get("ask");

		// selling in usd
		Double coinPriceInUSD = cexPairTickers.get(usdPair).get("bid");

		Double coinPriceUSDInEuro = coinPriceInUSD * oneDollarInEuro;

		System.out.println(
				coin + ": Euro: " + coinPriceInEuro + ", USD: " + coinPriceInUSD + ", inEuro: " + coinPriceUSDInEuro);

		System.out.println(coin + ": euro to usd: " + calculatePercentageDif(coinPriceInEuro, coinPriceUSDInEuro));
		System.out.println(" ");
		///////////////////////////////
		// selling in euro
		coinPriceInEuro = cexPairTickers.get(euroPair).get("bid");

		// buying in usd
		coinPriceInUSD = cexPairTickers.get(usdPair).get("ask");

		coinPriceUSDInEuro = coinPriceInUSD * oneDollarInEuro;

		System.out.println(
				coin + ": Euro: " + coinPriceInEuro + ", USD: " + coinPriceInUSD + ", inEuro: " + coinPriceUSDInEuro);

		System.out.println(coin + ": usd to euro: " + calculatePercentageDif(coinPriceUSDInEuro, coinPriceInEuro));
		System.out.println(" ");
	}

	private static Double calculatePercentageDif(Double start, Double end) {

		Double percentageDif = ((end - start) / start) * 100.0;

		return percentageDif;

	}

	private static Double getDollarRateInEuro() throws Exception {

		String url = "https://api.fixer.io/latest?base=USD&symbols=USD,EUR";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		JSONObject rates = (JSONObject) jsonObject.get("rates");
		Double dollarRate = (Double) rates.get("EUR");
		return dollarRate;
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

		System.out.println(jsonObject);
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

		System.out.println("cex Prices: " + cexPairTickers);
		return cexPairTickers;
	}

}
