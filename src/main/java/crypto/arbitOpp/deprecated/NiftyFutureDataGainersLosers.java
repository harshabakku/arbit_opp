package crypto.arbitOpp.deprecated;

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

public class NiftyFutureDataGainersLosers {

	public static void main(String[] args) throws Exception {

//		System.out.println(calculatePercentageDif(new Double(3), new Double(3.5)) + " should be 16.66666666 percent\n");

		while (true) {
			try {
				printNiftyData();
			} catch (Exception e) {

			}
			// TimeUnit.SECONDS.sleep(22);
			TimeUnit.SECONDS.sleep(1);
		}
	}

	private static void printNiftyData() throws Exception {
		System.out.println("################ ############## \n");
		System.out.println(new Date());
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

		// System.out.println("jsonObject returned" + jsonObject);
		JSONArray dataArray = (JSONArray) jsonObject.get("data");
		prettyPrintNiftyFOData(dataArray);

	}

	private static void prettyPrintNiftyFOData(JSONArray dataArray) throws Exception {
		Iterator<JSONObject> iterator = dataArray.iterator();

		System.out.println(
				" percentChange tRatio eRatio fRatio ebuyQ esellQ fbuyQ fSellQ  previousPrice openPrice highPrice lowPrice ltp symbol ");
		while (iterator.hasNext()) {
			JSONObject dataObj = iterator.next();
//			System.out.println(dataObj);

			String symbol = String.valueOf(dataObj.get("symbol"));
			// get equity depth chart data
			JSONObject equityDepthData = getEquityDepthChartData(symbol);
			float equityBuySellRatio;
//							System.out.println(depthData);
			Long equitySellQuantity = (Long) equityDepthData.get("totalSellQuantity");
			Long equityBuyQuantity = (Long) equityDepthData.get("totalBuyQuantity");
			try {
				equityBuySellRatio = ((float) equityBuyQuantity / equitySellQuantity);
				DecimalFormat decimalFormat = new DecimalFormat("#.##");
				equityBuySellRatio = Float.valueOf(decimalFormat.format(equityBuySellRatio));
			} catch (Exception e) {
				equityBuySellRatio = 0.0F;
			}

			// get futures depth chart data

			// get underlying price as last traded price.
			String lastTradedPrice = "";

			JSONObject futureData = getFutureDepthChartData(symbol);

			lastTradedPrice = String.valueOf(futureData.get("underlyingValue"));

			// get total buy and sell quantities of futures.//enable commented log to verify
			// data integrity.

			JSONArray stockDerivates = (JSONArray) futureData.get("stocks");

			Iterator<JSONObject> derivateIterator = stockDerivates.iterator();
			Long totalFutureSellQuantity = 0L;
			Long totalFutureBuyQuantity = 0L;
			int futureInstrumentCount = 0;
			while (derivateIterator.hasNext()) {
				JSONObject stockObj = derivateIterator.next();
				JSONObject stockMetadata = (JSONObject) stockObj.get("metadata");
				String instrumentType = (String) stockMetadata.get("instrumentType");
				if (instrumentType.equalsIgnoreCase("Stock Futures")) {
					futureInstrumentCount++;
					JSONObject futureDepthData = (JSONObject) stockObj.get("marketDeptOrderBook");
					Long futureSellQuantity = (Long) futureDepthData.get("totalSellQuantity");
					Long futureBuyQuantity = (Long) futureDepthData.get("totalBuyQuantity");

//					System.out.println("future data of " + symbol + " expiryDate: " + stockMetadata.get("expiryDate")
//							+ " buy:" + futureBuyQuantity + "  sell: " + futureSellQuantity);

					totalFutureBuyQuantity += futureBuyQuantity;
					totalFutureSellQuantity += futureSellQuantity;
					if (futureInstrumentCount == 2) {
						break;
					}
				} else {
					continue;
				}

			}

			float futureBuySellRatio;
			try {
				futureBuySellRatio = ((float) totalFutureBuyQuantity / totalFutureSellQuantity);
				DecimalFormat decimalFormat = new DecimalFormat("#.##");
				futureBuySellRatio = Float.valueOf(decimalFormat.format(futureBuySellRatio));
			} catch (Exception e) {
				futureBuySellRatio = 0.0F;
			}

			/////// calculate total buy sell ratio.................

			float totalBuySellRatio;
			try {
				Long totalBuyQuantity = equityBuyQuantity + totalFutureBuyQuantity;
				Long totalSellQuantity = equitySellQuantity + totalFutureSellQuantity;
				totalBuySellRatio = ((float) totalBuyQuantity / totalSellQuantity);
				DecimalFormat decimalFormat = new DecimalFormat("#.##");
				totalBuySellRatio = Float.valueOf(decimalFormat.format(totalBuySellRatio));

			} catch (Exception e) {
				System.out.println("Alert: total buy sell ratio is unusual");
				totalBuySellRatio = 0.0F;
			}

			//////////////////////////////////////// write to csv here
			String filePath = "./depthChartData/" + new Date().getDate() + "/" + symbol + ".csv";
            
			
			//individual csv header string
			String headerString = "totalBSRatio," + "equityBSRatio," + "totalFutureBSRatio,"+ "equitiyBuyQuantity," + "equitySellQuantity," + "totalFutureBuyQuantity,"
					+ "totalFutureSellQuantity,"  + "percentageChange,"
					+ "lastTradedPrice," + "date";
			//System.out.println(headerString);
			File file = new File(filePath);
			FileWriter outputfile;
			CSVWriter writer;
			outputfile = new FileWriter(file, true);
			try {
				writer = new CSVWriter(outputfile);
				// cannot get percentageChange from both the apis
				String percentageChange = "--";
				String[] nextLine = { totalBuySellRatio + "", equityBuySellRatio + "", futureBuySellRatio + "",
						 "" + equityBuyQuantity, "" + equitySellQuantity,
						"" + totalFutureBuyQuantity, "" + totalFutureSellQuantity, percentageChange + "", lastTradedPrice + "", new Date() + "" };
				writer.writeNext(nextLine);
				
				String csvPrintString = totalBuySellRatio + " " + equityBuySellRatio + " " + futureBuySellRatio + " "
						+ " " + equityBuyQuantity + " "
						+ equitySellQuantity + " " + totalFutureBuyQuantity + " " + totalFutureSellQuantity + " "+ percentageChange + " " + lastTradedPrice + " " 
						+ new Date() + " ";

				//System.out.println(printString);
				writer.close();
				Object netPrice = dataObj.get("netPrice");
				Object ltPrice = dataObj.get("ltp");
				System.out.println("      " + netPrice + "    " + totalBuySellRatio + "    "+ equityBuySellRatio + "    "+ futureBuySellRatio + "    " + equityBuyQuantity + "     "
						+ equitySellQuantity + "     " + totalFutureBuyQuantity + "      "
						+ totalFutureSellQuantity + "      " + dataObj.get("previousPrice") + "      " + dataObj.get("openPrice")
						+ "      " + dataObj.get("highPrice") + "      " + dataObj.get("lowPrice") + "      " + ltPrice
						+ "      " + "      " + symbol);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private static JSONObject getFutureDepthChartData(String symbol) throws Exception {

		if (symbol.contains("&")) {
			// url already has & to separate params
			symbol = symbol.replace("&", "%26");
		}
		String url = "https://www.nseindia.com/api/quote-derivative?symbol=" + symbol;
//		System.out.println(url);
		JSONObject jsonObject = getResponse(url);
		// System.out.println(jsonObject);

		return jsonObject;

	}

	private static JSONObject getEquityDepthChartData(String symbol)
			throws MalformedURLException, ProtocolException, UnsupportedEncodingException, IOException, ParseException {

		if (symbol.contains("&")) {
			// url already has & to separate params
			symbol = symbol.replace("&", "%26");
		}
		String url = "https://www.nseindia.com/api/quote-equity?symbol=" + symbol + "&section=trade_info";
//		System.out.println(url);
		JSONObject jsonObject = getResponse(url);
		// System.out.println(jsonObject);
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
