package crypto.arbitOpp.deprecated;

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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Hello world!
 *
 */
public class CoinDeltaBinanceInterTrade {
	public static void main(String[] args) throws Exception {
		JSONObject koinexPrices = getKoinexData();
		Map<String, Double> binancePriceMap = getBinanceData();
		Double dollarRate = getDollarRate();
		System.out.println(" dollar rate should be around 64 "+ dollarRate);
        
		System.out.println(calculatePercentageDif(new Double(1),new Double( 1.3)) + "  should be 30 percent");
		
		System.out.println(calculatePercentageDif(new Double(1),new Double( 0.7))+ "  should be -30 percent");
		
		System.out.println(calculatePercentageDif(new Double(3),new Double( 3.5))+ " should be 16.66666666 percent");
		
		System.out.println(" ");
		
		
		calculatePricePercentageDiff( "BTC", "BTCUSDT", koinexPrices, binancePriceMap, dollarRate);
		
		calculatePricePercentageDiff( "ETH", "ETHUSDT", koinexPrices, binancePriceMap, dollarRate);
		
		calculatePricePercentageDiff( "LTC", "LTCUSDT", koinexPrices, binancePriceMap, dollarRate);
		
		calculatePricePercentageDiffXRP( "XRP", "XRPBTC", koinexPrices, binancePriceMap, dollarRate);
		
		
		
	}

	private static void calculatePricePercentageDiff(String koinexPair,String binancePair, JSONObject koinexPrices, Map<String, Double> binancePriceMap, Double dollarRate) {
		Double koinexPrice = Double.valueOf((String)koinexPrices.get(koinexPair));
		
		Double binanceUSDPrice = binancePriceMap.get(binancePair);
		
		Double binancePriceInINR = binanceUSDPrice*dollarRate;
		
		System.out.println(koinexPair + ": Koinex: "+ koinexPrice+ ", BinanceUSD: "+ binanceUSDPrice +", inINR: " +binancePriceInINR);
		
		System.out.println("koinex to binance: "+ calculatePercentageDif(koinexPrice, binancePriceInINR));
		System.out.println("binance to koinex: "+ calculatePercentageDif(binancePriceInINR, koinexPrice));
		System.out.println( " ");
	}
	
	
	private static void calculatePricePercentageDiffXRP(String koinexPair,String binancePair, JSONObject koinexPrices, Map<String, Double> binancePriceMap, Double dollarRate) {
		Double koinexPrice = Double.valueOf((String)koinexPrices.get(koinexPair));
		
		Double binancePriceInBTC =  binancePriceMap.get(binancePair);
		
		Double binancePriceInUSD = binancePriceMap.get("BTCUSDT")*binancePriceInBTC;
		
		Double binancePriceInINR = binancePriceInUSD*dollarRate;
		
		System.out.println(koinexPair + ": Koinex: "+ koinexPrice+ ", BinanceUSD: "+ binancePriceInUSD +", inINR: " +binancePriceInINR);
		
		System.out.println("koinex to binance: "+ calculatePercentageDif(koinexPrice, binancePriceInINR));
		System.out.println("binance to koinex: "+ calculatePercentageDif(binancePriceInINR, koinexPrice));
		System.out.println( " ");
	}
	
	private static Double calculatePercentageDif(Double start,Double end){
		
		Double percentageDif = ((end - start)/start)*100.0;
		
		
		return percentageDif;
		
		
		
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

	public static JSONObject getKoinexData() throws Exception {
		Document doc = Jsoup.connect("https://coindelta.com/market?active=BTC-INR").followRedirects(true).timeout(30000).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36").get();
		System.out.println(doc.data());
		
		String url = "https://koinex.in/api/ticker";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		JSONObject prices = (JSONObject) jsonObject.get("prices");
		System.out.println("koinex Prices "+ jsonObject.toString());
		return prices;
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

		System.out.println("binance Prices: "+ binancePrices);
		return binancePrices;
	}

}
