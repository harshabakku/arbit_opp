
package crypto.arbitOpp.deprecated;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.print.attribute.standard.MediaSize.Other;
import javax.swing.plaf.basic.BasicSliderUI.TrackListener;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class CollectYesterdayPCRInputFile {

	// use this to fill the individual csvs of stocks that we want to trace more
	// finer to observe deeper patterns.
	// cant be done for cause it takes more than 40 sec already before including
	// future buy/sell quantities and ratios
	public static void main(String[] args) throws Exception {
		// need to automate at some point
		String expiryDate = "2020-08-27";
		String filePath = "./data/" + "OIDirection" + ".csv";

		System.out.println("sdljf");
		File file = new File(filePath);
		Reader inputfile;
		CSVReader reader;
		inputfile = new FileReader(file);

		reader = new CSVReader(inputfile);
		Iterator<String[]> iterator = reader.iterator();

		// writer data
		String outfilePath = "./data/" + "OIDirectionPCR" + ".csv";

		File outFile = new File(outfilePath);
		FileWriter outputfile;
		CSVWriter writer;
		outputfile = new FileWriter(outFile, true);

		writer = new CSVWriter(outputfile);
		JSONObject pcrData = getPCRData();
		while (iterator.hasNext()) {
			String[] nextLine = iterator.next();
			String symbol = nextLine[0];
			String oiDirection = nextLine[1];

			

			JSONObject symbolIVData = (JSONObject) ((JSONObject) ((JSONObject) pcrData.get(symbol)).get("per_expiry_data"))
					.get(expiryDate);
//				if(symbolIVData == null) {
////				data does not exist for this expiry date for this symbol
//					continue;
//				}

			float putCallRatio = Float.valueOf(symbolIVData.get("pcr").toString());
			
			String[] nextLine2 = {symbol, oiDirection, putCallRatio+ ""};
			System.out.println(symbol+ oiDirection + putCallRatio);
			writer.writeNext(nextLine2);

			
		}
		writer.close();
		reader.close();

	}

	public static JSONObject getPCRData() throws Exception {
		String url = "https://api.sensibull.com/v1/instrument_details";
		JSONObject jsonObject = getResponse(url);

		Map<String, JSONObject> ivData = new HashMap<String, JSONObject>();
		// System.out.println("jsonObject returned" + jsonObject);
		JSONObject data = (JSONObject) jsonObject.get("data");


		return data;
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
//			System.out.println(jsonObject);
		inputStream.close();

		return jsonObject;
	}

}
