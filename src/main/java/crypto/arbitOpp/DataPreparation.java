package crypto.arbitOpp;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jws.Oneway;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.opencsv.CSVWriter;

/**
 * Hello world!
 *
 */
public class DataPreparation {
	public static void main(String[] args) throws Exception {


//		 JSONObject binanceData = getBinanceChartData();
			JSONObject exmoData = getExmoChartData();
			
			ArrayList<JSONObject> candles = (ArrayList<JSONObject>) exmoData.get("candles");
			
			writeDataLineByLine("./USDT_USD.csv", candles);

		
	}
	
	public static JSONObject getExmoChartData() throws Exception {
		String url = "https://chart.exmoney.com/ctrl/chart/history?symbol=USDT_USD&resolution=30&from=1579815298&to=1584999299";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		//System.out.println("exmo data"+ jsonObject.toString());
		
		return jsonObject;
	}


	public static JSONObject getBinanceChartData() throws Exception {
		String interval = "30m";
		String url = "https://www.binance.com/api/v1/klines?symbol=XRPUSDT&interval=" + interval;
		JSONObject jsonObject = getResponse(url);
//		JSONObject prices = (JSONObject) ((JSONObject) jsonObject.get("prices")).get("inr");
		System.out.println("binance data"+ jsonObject.toString());
		return jsonObject;
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
	
	public static void writeDataLineByLine(String filePath, ArrayList<JSONObject> candles) 
	{ 
		// first create file object for file placed at location 
		// specified by filepath 
		File file = new File(filePath); 
		try { 
			// create FileWriter object with file as parameter 
			FileWriter outputfile = new FileWriter(file); 

			// create CSVWriter object filewriter object as parameter 
			CSVWriter writer = new CSVWriter(outputfile); 

			// adding header to csv 
			String[] header = { "c", "t", "v", "h", "l", "o" }; 
			writer.writeNext(header); 

			
			//System.out.println(candles);
			for(JSONObject candle : candles) {
				System.out.println(candle);
				String[] data1 = { String.valueOf(candle.get("c")), String.valueOf(candle.get("t"))
						,String.valueOf(candle.get("v")),String.valueOf(candle.get("h")),String.valueOf(candle.get("l")),String.valueOf(candle.get("o"))
						}; 
				writer.writeNext(data1); 
			
			}
			
			

			// closing writer connection 
			writer.close(); 
		} 
		catch (IOException e) { 
			// TODO Auto-generated catch block 
			e.printStackTrace(); 
		} 
	} 


}
