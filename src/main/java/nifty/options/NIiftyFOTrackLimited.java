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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.opencsv.CSVWriter;

public class NIiftyFOTrackLimited {

	//use this to fill the individual csvs of stocks that we want to trace more finer to observe deeper patterns.
	//cant be done for cause it takes more than 40  sec already before including future buy/sell quantities and ratios
	public static void main(String[] args) throws Exception {

//use args list of strings for symbols that need to be tracked.
        while(true) {		
        	System.out.println("tracking FOs");
        	try {
        		// eventually loop through args and add to trackList as well.
        	    ArrayList<String> trackList = new ArrayList<String>();
        	    trackList.add("CHOLAFIN");
        	    trackList.add("GAIL");
        		trackFOs(trackList);
        	}
        	catch(Exception e) {
        		throw e;
        	}
        	//TimeUnit.SECONDS.sleep(22);
        	//TimeUnit.SECONDS.sleep(1);
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
		
		return jsonObject;
		

	}

	private static JSONObject getEquityDepthChartData(String symbol) throws MalformedURLException, ProtocolException, UnsupportedEncodingException, IOException, ParseException {
		
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


	private static void trackFOs(ArrayList<String> trackList) throws Exception {
		
			Iterator<String> iterator = trackList.iterator();

			while (iterator.hasNext()) {
				String symbol = iterator.next();
				
				
				// get equity depth chart data 
				JSONObject equityDepthData = getEquityDepthChartData(symbol);
				float equityBuySellRatio;
//				System.out.println(depthData);
				Long equitySellQuantity = (Long) equityDepthData.get("totalSellQuantity");
				Long equityBuyQuantity = (Long) equityDepthData.get("totalBuyQuantity");
				try {
				equityBuySellRatio = ((float)equityBuyQuantity/equitySellQuantity);
				DecimalFormat decimalFormat = new DecimalFormat("#.##");
				equityBuySellRatio = Float.valueOf(decimalFormat.format(equityBuySellRatio));
				}catch(Exception e) {
					equityBuySellRatio = 0.0F;
				}

				//get underlying price as last traded price.
				String lastTradedPrice = "" ;
				//get futures depth chart data
				JSONObject futureData = getFutureDepthChartData(symbol);
		
				lastTradedPrice = String.valueOf(futureData.get("underlyingValue"));
				JSONObject futureStock = (JSONObject)((JSONArray) futureData.get("stocks")).get(0);
				 
				JSONObject futureDepthData =  (JSONObject) futureStock.get("marketDeptOrderBook");
				
				float futureBuySellRatio;
//				System.out.println(depthData);
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
				try {
					writer = new CSVWriter(outputfile); 
					//cannot get percentageChange from both the apis
					String percentageChange = "--" ;
					//csvHeader is  "totalBSRatio","equityBSRatio","futureBSRatio","percentageChange","lastTradedPrice","equitiyBuyQuantity","equitySellQuantity"","futureBuyQuantity","futureSellQuantity","date"
					String[] nextLine = { totalBuySellRatio+ "", equityBuySellRatio+ "" , futureBuySellRatio+ "" , percentageChange + "" ,lastTradedPrice +"" , ""+ equityBuyQuantity, ""+ equitySellQuantity,""+ futureBuyQuantity, ""+ futureSellQuantity, new Date()+"" }; 
					writer.writeNext(nextLine); 
					String printString =  totalBuySellRatio+  " "+  equityBuySellRatio+ " " + futureBuySellRatio + " " +  percentageChange + " " + lastTradedPrice +" " +  " "+ equityBuyQuantity + " "+ equitySellQuantity + " "+ futureBuyQuantity +  " "+ futureSellQuantity + " "+ new Date()+" "; 
					
                    System.out.println(printString);
					writer.close(); 
				}  catch (IOException e) { 
				// TODO Auto-generated catch block 
				e.printStackTrace(); 
			    } 			

				

			}

		
	}



}
