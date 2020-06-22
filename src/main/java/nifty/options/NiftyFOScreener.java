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
import java.security.acl.LastOwnerException;
import java.text.DecimalFormat;
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

public class NiftyFOScreener {

	public static void main(String[] args) throws Exception {

//		System.out.println(calculatePercentageDif(new Double(3), new Double(3.5)) + " should be 16.66666666 percent\n");

        while(true) {		
        	try {
        	printNiftyData();}
        	catch(Exception e) {
        		//throw e;
        	}
        	//TimeUnit.SECONDS.sleep(22);
        	//TimeUnit.SECONDS.sleep(1);
        }
	}

	private static void printNiftyData() throws Exception {
		System.out.println("################ ############## \n");
        System.out.println("current Time: " + new Date()); 
		getEntireNiftyFOData();

		System.out.println("################ ############## \n");
	}


	public static void getEntireNiftyFOData() throws Exception {
        // all F&Os are about 140 of them and it takes more than 2 min to get the data.
		String url = "https://www.nseindia.com/api/equity-stockIndices?index=SECURITIES%20IN%20F%26O";
//		System.out.println(url);
		//String url = "https://www.nseindia.com/api/equity-stockIndices?index=NIFTY%2050";
		JSONObject jsonObject = getResponse(url);

		//System.out.println("jsonObject returned" + jsonObject);
		JSONArray dataArray = (JSONArray) jsonObject.get("data");
		sortPrettyPrintNiftyFOData(dataArray);

	}

	private static void sortPrettyPrintNiftyFOData(JSONArray dataArray) throws Exception {
		Iterator<JSONObject> iterator = dataArray.iterator();

		System.out.println(" percentChange totalRatio equityRatio futureRatio eBuyQ    eSellQ   fBuyQ fSellQ previousDayClose openPrice highPrice lowPrice ltp  symbol ");
		while (iterator.hasNext()) {
			try{
			JSONObject dataObj = iterator.next();
//			System.out.println(dataObj);
			String symbol = (String) dataObj.get("symbol");
			if(symbol.contentEquals("NIFTY 50")) {
				continue;
			}
			
			// get equity depth chart data 
			JSONObject equityDepthData = getEquityDepthChartData(symbol);
			float equityBuySellRatio;
//			System.out.println(depthData);
			Long equitySellQuantity = (Long) equityDepthData.get("totalSellQuantity");
			Long equityBuyQuantity = (Long) equityDepthData.get("totalBuyQuantity");
			try {
			equityBuySellRatio = ((float)equityBuyQuantity/equitySellQuantity);
			DecimalFormat decimalFormat = new DecimalFormat("#.##");
			equityBuySellRatio = Float.valueOf(decimalFormat.format(equityBuySellRatio));
			}catch(Exception e) {
				equityBuySellRatio = 0.0F;
			}
			
			//get futures depth chart data
			JSONObject futureDepthData = getFutureDepthChartData(symbol);
			float futureBuySellRatio;
//			System.out.println(depthData);
			Long futureSellQuantity = (Long) futureDepthData.get("totalSellQuantity");
			Long futureBuyQuantity = (Long) futureDepthData.get("totalBuyQuantity");
			try {
			futureBuySellRatio = ((float)futureBuyQuantity/futureSellQuantity);
			DecimalFormat decimalFormat = new DecimalFormat("#.##");
			futureBuySellRatio = Float.valueOf(decimalFormat.format(futureBuySellRatio));
			}catch(Exception e) {
				futureBuySellRatio = 0.0F;
			}
			
			
			float totalBuySellRatio;
			try {
				Long totalBuyQuantity = equityBuyQuantity + futureBuyQuantity;
				Long totalSellQuantity = equitySellQuantity + futureSellQuantity;
				totalBuySellRatio = ((float)totalBuyQuantity/totalSellQuantity);
				DecimalFormat decimalFormat = new DecimalFormat("#.##");
				totalBuySellRatio = Float.valueOf(decimalFormat.format(totalBuySellRatio));
				
			}catch (Exception e) {
				System.out.println("Alert: total buy sell ratio is unusual");
				totalBuySellRatio = 0.0F;
			}
			
			//write to csv here
			String filePath= "./depthChartData/" + new Date().getDate()+ "/"+ symbol + ".csv";

			File file = new File(filePath); 
			FileWriter outputfile;
			CSVWriter writer; 
			outputfile = new FileWriter(file, true); 
			Object percentageChange = dataObj.get("pChange");
			Object lastTradedPrice = dataObj.get("lastPrice");
			try {
				writer = new CSVWriter(outputfile); 
				//csvHeader is  "totalBSRatio","equityBSRatio","futureBSRatio","percentageChange","lastTradedPrice","equitiyBuyQuantity","equitySellQuantity"","futureBuyQuantity","futureSellQuantity","date"
				String[] nextLine = { totalBuySellRatio+ "", equityBuySellRatio+ "" , futureBuySellRatio+ "" , percentageChange+ "" ,lastTradedPrice +"" , ""+ equityBuyQuantity, ""+ equitySellQuantity,""+ futureBuyQuantity, ""+ futureSellQuantity, new Date()+"" }; 
				writer.writeNext(nextLine); 

				writer.close(); 
			}  catch (IOException e) { 
			// TODO Auto-generated catch block 
			e.printStackTrace(); 
		    } 			

			System.out.println( "      "+ percentageChange + "      " + totalBuySellRatio + "      " + equityBuySellRatio + "      " + futureBuySellRatio + "      " + equityBuyQuantity+ "      "+ equitySellQuantity+ "      " + futureBuyQuantity+ "      "+ futureSellQuantity+
					"    "+ dataObj.get("previousClose")+ "    "+ dataObj.get("open")+ "    "+ dataObj.get("dayHigh")+ "    "+ dataObj.get("dayLow") + "    "+ lastTradedPrice+ "   "+ symbol);
			}catch(Exception e) {
				
			e.printStackTrace();
			}

		}


	}

	private static JSONObject getFutureDepthChartData(String symbol) throws Exception{


		if (symbol.contains("&")) {
			//url already has & to separate params
			symbol = symbol.replace("&", "%26");
		}
		String url=  "https://www.nseindia.com/api/quote-derivative?symbol="+symbol  ;
//		System.out.println(url);
		JSONObject jsonObject = getResponse(url);
		//System.out.println(jsonObject);
		JSONArray stocks = (JSONArray) jsonObject.get("stocks");
		JSONObject futureStock = (JSONObject) stocks.get(0);
		 
		return (JSONObject) futureStock.get("marketDeptOrderBook");

	}

	private static JSONObject getEquityDepthChartData(String symbol) throws Exception{
		
		if (symbol.contains("&")) {
			//url already has & to separate params
			symbol = symbol.replace("&", "%26");
		}
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
