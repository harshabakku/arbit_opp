package nifty.options;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.opencsv.CSVWriter;

public class NiftyFOGainersLosers {

	public static void main(String[] args) throws Exception {

//		System.out.println(calculatePercentageDif(new Double(3), new Double(3.5)) + " should be 16.66666666 percent\n");

        while(true) {		
        	printNiftyData();
        	TimeUnit.SECONDS.sleep(22);
        }
	}

	private static void printNiftyData() throws Exception {
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

	private static void prettyPrintNiftyFOData(JSONArray dataArray) throws Exception {
		Iterator<JSONObject> iterator = dataArray.iterator();

		System.out.println(" percentChange buyQ sellQ buy/sell>2<0.5 previousPrice openPrice highPrice lowPrice ltp tradedQuantity symbol ");
		while (iterator.hasNext()) {
			JSONObject dataObj = iterator.next();
//			System.out.println(dataObj);
			
			JSONObject depthData = getDepthChartData(String.valueOf(dataObj.get("symbol")));
			Double buySellRatio;
			Object symbol = dataObj.get("symbol");
			Object totalSellQuantity = depthData.get("totalSellQuantity");
			Object totalBuyQuantity = depthData.get("totalBuyQuantity");
			try {
			buySellRatio = (double) ((Long)totalBuyQuantity/(Long)depthData.get("totalSellQuantity"));
			}catch(Exception e) {
				buySellRatio = 0.0;
			}
			String filePath= "./depthChartData/" + symbol + ".csv";

			File file = new File(filePath); 
			FileWriter outputfile;
			CSVWriter writer; 
			outputfile = new FileWriter(file, true); 
			try {
				writer = new CSVWriter(outputfile); 
				//csvHeader is  "buySellRation >2 <0.5","totalBuyQuantity","totalSellQuantity","date"
				String[] nextLine = { buySellRatio+ "" ,""+ totalBuyQuantity, ""+ totalSellQuantity, new Date()+"" }; 
				writer.writeNext(nextLine); 

				writer.close(); 
			}  catch (IOException e) { 
			// TODO Auto-generated catch block 
			e.printStackTrace(); 
		    } 			
			System.out.println( "      "+ dataObj.get("netPrice") + "      "+ totalBuyQuantity+ "      "+ totalSellQuantity+ "      "+ buySellRatio + "      "+ dataObj.get("previousPrice")+ "      "+ dataObj.get("openPrice")+ "      "+ dataObj.get("highPrice")+ "      "+ dataObj.get("lowPrice") + "      "+ dataObj.get("ltp")+ "      "+ dataObj.get("tradedQuantity") + "      "+ symbol);
			

		}


	}

	private static JSONObject getDepthChartData(String symbol) throws MalformedURLException, ProtocolException, UnsupportedEncodingException, IOException, ParseException {
		
		
        String url = "https://www.nseindia.com/api/quote-equity?symbol="+symbol  + "&section=trade_info";		
//		System.out.println(url);
		JSONObject jsonObject = getResponse(url);
		//System.out.println(jsonObject);
		return (JSONObject) jsonObject.get("marketDeptOrderBook");
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
//		System.out.println(jsonObject);
		inputStream.close();

		return jsonObject;
	}

}
