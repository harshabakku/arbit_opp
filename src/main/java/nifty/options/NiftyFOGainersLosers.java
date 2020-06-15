package nifty.options;

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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class NiftyFOGainersLosers {

	public static void main(String[] args) throws Exception {

//		System.out.println(calculatePercentageDif(new Double(3), new Double(3.5)) + " should be 16.66666666 percent\n");

		System.out.println("################ ############## \n");

		getNiftyTopFOData("gainers/fnoGainers1.json");

		System.out.println("################ ############## \n");

		getNiftyTopFOData("losers/fnoLosers1.json");


		System.out.println("################ ############## \n");

	}

	private static Double calculatePercentageDif(Double start, Double end) {

		Double percentageDif = ((end - start) / start) * 100.0;

		return percentageDif;

	}

	public static void getNiftyTopFOData(String urlSufffix) throws Exception {

		String url = "https://www1.nseindia.com/live_market/dynaContent/live_analysis/" + urlSufffix;
//		System.out.println(url);
		JSONObject jsonObject = getResponse(url);

		//System.out.println("jsonObject returned" + jsonObject);
		JSONArray dataArray = (JSONArray) jsonObject.get("data");
		prettyPrintNiftyFOData(dataArray);

	}

	private static void prettyPrintNiftyFOData(JSONArray dataArray) {
		Iterator<JSONObject> iterator = dataArray.iterator();

		System.out.println(" percentChange previousPrice openPrice highPrice lowPrice ltp tradedQuantity symbol ");
		while (iterator.hasNext()) {
			JSONObject dataObj = iterator.next();
//			System.out.println(dataObj);
			System.out.println( "      "+ dataObj.get("netPrice") + "      "+ dataObj.get("previousPrice")+ "      "+ dataObj.get("openPrice")+ "      "+ dataObj.get("highPrice")+ "      "+ dataObj.get("lowPrice") + "      "+ dataObj.get("ltp")+ "      "+ dataObj.get("tradedQuantity") + "      "+ dataObj.get("symbol"));
			

		}

		// System.out.println("cex Prices: " + cexPairTickers);

	}

	private static JSONObject getResponse(String url)
			throws MalformedURLException, IOException, ProtocolException, ParseException, UnsupportedEncodingException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("GET");
		// url is forbidden , fake browser hit

		// only for xml http kind of requests
		con.setRequestProperty("X-Requested-With", "XMLHttpRequest");

		con.setRequestProperty("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.163 Safari/537.36");

		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		System.out.println(jsonObject);
		inputStream.close();

		return jsonObject;
	}

}
