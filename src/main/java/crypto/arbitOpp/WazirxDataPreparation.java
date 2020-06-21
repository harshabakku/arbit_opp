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
import java.util.Calendar;
import java.util.Date;
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
public class WazirxDataPreparation {
	public static void main(String[] args) throws Exception {

		String filePath = "./wazirx_USDT_INR.csv";
		File file = new File(filePath); 
		try { 
			// create FileWriter object with file as parameter 
			FileWriter outputfile = new FileWriter(file); 

			// create CSVWriter object filewriter object as parameter 
			CSVWriter writer = new CSVWriter(outputfile); 

			// adding header to csv 
			String[] header = { "t", "o", "h", "l", "c", "v" }; 
			writer.writeNext(header); 
			
		

//		Long  currentEpoch = Calendar.getInstance().getTimeInMillis();
		int resolution = 30; // in minutes
//        int candlesNo = 3000;
//        int duration = 10;
//        int   fromEpoch = (int) ((currentEpoch/1000 - duration*candlesNo*resolution*60));
//        String to = new String();
//        String from = String.valueOf(fromEpoch);
//        for(int i=0; i<=10;i++) {
		//String to = String.valueOf((int) (currentEpoch/1000));
//            if(!to.isEmpty()) {from = to;}		
//        	to = String.valueOf(Integer.valueOf(from)+candlesNo*resolution*60);
		     
			JSONArray wazirxData = getWazirxChartData(String.valueOf(resolution));

//			
//			ArrayList<JSONObject> candles = (ArrayList<JSONObject>) wazirxData.get("candles");
//			
			System.out.println("candle count: "+ wazirxData.size());
//			
			writeDataLineByLine(filePath, wazirxData, writer);
//        }
        writer.close(); 
		}catch (IOException e) { 
			// TODO Auto-generated catch block 
			e.printStackTrace(); 
		} 
	}
	
	public static JSONArray getWazirxChartData(String resolution) throws Exception {
	    //3000 candles bulk fetch allowed

//		String url = "https://chart.exmoney.com/ctrl/chart/history?symbol=USDT_USD&resolution=" + resolution
//				+ "&from=" + from + "&to=" + to;
		String url = "https://x.wazirx.com/api/v2/k?market=usdtinr&limit=10000&period=" + resolution;
		
		System.out.println(url);
		JSONArray jsonArray = getResponse(url);
	//	System.out.println("result data"+ jsonArray.toString());
		
		return jsonArray;
	}


	private static JSONArray getResponse(String url)
			throws MalformedURLException, IOException, ProtocolException, ParseException, UnsupportedEncodingException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		// url is forbidden , fake browser hit
		con.setRequestProperty("User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONArray jsonArray = (JSONArray) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		return jsonArray;
	}



	
	public static void writeDataLineByLine(String filePath, JSONArray candles, CSVWriter writer) 
	{ 
		// first create file object for file placed at location 
		// specified by filepath 
//		File file = new File(filePath); 
//		try { 
			// create FileWriter object with file as parameter 
//			FileWriter outputfile = new FileWriter(file); 
//
//			// create CSVWriter object filewriter object as parameter 
//			CSVWriter writer = new CSVWriter(outputfile); 

			// adding header to csv 
			//String[] header = { "c", "t", "v", "h", "l", "o" }; 
			//writer.writeNext(header); 
            
			
			//System.out.println(candles);
			for(Object candl : candles) {
				JSONArray candle = (JSONArray) candl;
				System.out.println(candle);
				String[] data1 = { String.valueOf(candle.get(0)), String.valueOf(candle.get(1))
						,String.valueOf(candle.get(2)),String.valueOf(candle.get(3)),String.valueOf(candle.get(4)),String.valueOf(candle.get(5))
						}; 
				writer.writeNext(data1, false); 
			
			}
			
			

			// closing writer connection 
//			writer.close(); 
//		} 
//		catch (IOException e) { 
//			// TODO Auto-generated catch block 
//			e.printStackTrace(); 
//		} 
	} 


}
