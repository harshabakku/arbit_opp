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
public class BitMEXDataPreparation {
	public static void main(String[] args) throws Exception {

		String filePath = "./bitmex_XBT_USD.csv";
		File file = new File(filePath); 
		try { 
			// create FileWriter object with file as parameter 
			FileWriter outputfile = new FileWriter(file); 

			// create CSVWriter object filewriter object as parameter 
			CSVWriter writer = new CSVWriter(outputfile); 

			// adding header to csv 
			String[] header = { "t", "o", "h", "l", "c", "v" }; 
			writer.writeNext(header); 
			
		

		Long  currentEpoch = Calendar.getInstance().getTimeInMillis();
		
		int resolution = 5; // in minutes
        int candlesNo = 10000;
        int duration = 23; //2 year data
        int   fromEpoch = (int) ((currentEpoch/1000 - duration*candlesNo*resolution*60));
        
        System.out.println(currentEpoch/1000 + "  from  " +fromEpoch);
        String to = new String();
        String from = String.valueOf(fromEpoch);
        for(int i=0; i<duration ;i++) {
		//String to = String.valueOf((int) (currentEpoch/1000));
            if(!to.isEmpty()) {from = to;}		
        	to = String.valueOf(Integer.valueOf(from)+candlesNo*resolution*60);
		     
			JSONObject bitmexData = getBitMEXChartData(from,to,String.valueOf(resolution));

			
			
//			System.out.println("candle count: "+ ((JSONArray)bitmexData.get("t")).size());
			
			writeDataLineByLine(filePath, bitmexData, writer);
        }
        writer.close(); 
		}catch (IOException e) { 
			// TODO Auto-generated catch block 
			e.printStackTrace(); 
		} 
	}
	
	public static JSONObject getBitMEXChartData(String from, String to, String resolution) throws Exception {
	    //3000 candles bulk fetch allowed

		String url = "https://www.bitmex.com/api/udf/history?symbol=XBTUSD&resolution=" + resolution
				+ "&from=" + from + "&to=" + to;
		
			
		System.out.println(url);
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");

		InputStream inputStream = con.getInputStream();

		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
		//System.out.println("bitmex data"+ jsonObject.toString());
		
		return jsonObject;
	}


	
	public static void writeDataLineByLine(String filePath, JSONObject candles, CSVWriter writer) 
	{ 
		// first create file object for file placed at location 
		// specified by filepath 
//		File file = new File(filePath); 
//		try { 
			// create FileWriter object with file as parameter 
//			FileWriter outputfile = n)ew FileWriter(file); 
//
//			// create CSVWriter object filewriter object as parameter 
//			CSVWriter writer = new CSVWriter(outputfile); 

			// adding header to csv 
			//String[] header = { "c", "t", "v", "h", "l", "o" }; 
			//writer.writeNext(header); 
            
		    JSONArray t = (JSONArray)candles.get("t");
		    JSONArray o = (JSONArray)candles.get("o");
		    JSONArray h = (JSONArray)candles.get("h");
		    JSONArray l = (JSONArray)candles.get("l");
		    JSONArray c = (JSONArray)candles.get("c");
		    JSONArray v = (JSONArray)candles.get("v");
			
		    
			//System.out.println(candles);
			for(int i=0; i<=10000; i++) {
				//System.out.println(candle);
				String[] data1 = { String.valueOf(t.get(i)), String.valueOf(o.get(i))
						,String.valueOf(h.get(i)),String.valueOf(l.get(i)),String.valueOf(c.get(i)),String.valueOf(v.get(i))
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
