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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.print.attribute.standard.MediaSize.Other;
import javax.swing.plaf.basic.BasicSliderUI.TrackListener;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.opencsv.CSVWriter;

public class OptionDataSensibullNse {

	// use this to fill the individual csvs of stocks that we want to trace more
	// finer to observe deeper patterns.
	// cant be done for cause it takes more than 40 sec already before including
	// future buy/sell quantities and ratios
	public static void main(String[] args) throws Exception {

		float ivpLimit = 0;
		String expiryDate = "2020-07-30";
		ArrayList<String> trackList = new ArrayList<String>();

		trackList.add("NIFTY");
		trackList.add("BANKNIFTY");

		// individual csv header string
		String headerString = "totalBSRatio,percentChange,IVPercentile,optionIV,prevIV," + "equityBSRatio,"
				+ "totalFutureBSRatio,putCallRatio," + "equityBuyQuantity," + "equitySellQuantity,"
				+ "totalFutureBuyQuantity," + "totalFutureSellQuantity,"
				+ "lastTradedPrice,optionStrikePrice,optionExpiryDate," + "date";
		System.out.println(headerString);

		while (true) {
			Map<String, JSONObject> ivpData = getIVPercentileData(expiryDate, ivpLimit, trackList);
			try {
				System.out.println("\ntotal no. of stocks with IVP > " + ivpLimit + " : " + ivpData.keySet().size());
				System.out.println(
						"###########################################################################################");
				trackFOs(ivpData, trackList);
			} catch (Exception e) {
				System.out.println(e);
			}
//			TimeUnit.SECONDS.sleep(20);
		}
	}

	public static Map<String, JSONObject> getIVPercentileData(String expiryDate, float ivpLimit,
			ArrayList<String> trackList) throws Exception {
		String url = "https://api.sensibull.com/v1/instrument_details";
		JSONObject jsonObject = getResponse(url);

		Map<String, JSONObject> ivData = new HashMap<String, JSONObject>();
		// System.out.println("jsonObject returned" + jsonObject);
		JSONObject data = (JSONObject) jsonObject.get("data");

		Iterator<String> iterator = data.keySet().iterator();

		while (iterator.hasNext()) {

			String symbol = iterator.next();
//			System.out.println(dataObj);
//			if (symbol.contentEquals("NIFTY") || symbol.contentEquals("BANKNIFTY")) {
//				continue;
//			}

			JSONObject symbolIVData = (JSONObject) ((JSONObject) ((JSONObject) data.get(symbol)).get("per_expiry_data"))
					.get(expiryDate);
			Double ivPercentile = Double.valueOf(symbolIVData.get("iv_percentile").toString());
			Long callOI = Long.valueOf(symbolIVData.get("call_oi").toString());
			Long putOI = Long.valueOf(symbolIVData.get("put_oi").toString());
			Long lotSize = (Double.valueOf(symbolIVData.get("lot_size").toString())).longValue();
			Long totalOI = (callOI + putOI);
			Long liquidity = totalOI / lotSize; // when lot size is more options tend to be illiquid for some reason.
			/**
			 * glenmark example with no liquidity and too far spread "lot_size": 2300.0,
			 * "call_oi": 7838400, "put_oi": 6467600, (7838400+ 6467600)*2300=32903800000
			 * our formula for liquidity is lot-size*(callOI + putOI)
			 */
			if (trackList.isEmpty()) {
				if (ivPercentile.longValue() > ivpLimit
//					& liquidity > 10000L
				) {
					ivData.put(symbol, symbolIVData);
//		    	  System.out.println(symbolIVData);
				}
			} else {
				if (trackList.contains(symbol)) {
					ivData.put(symbol, symbolIVData);
				}
			}
		}
		return ivData;
	}

	private static JSONObject getDerivatesData(String symbol) throws Exception {

		if (symbol.contains("&")) {
			// url already has & to separate params
			symbol = symbol.replace("&", "%26");
		}
		String url = "https://www.nseindia.com/api/quote-derivative?symbol=" + symbol;
//		System.out.println(url);
		JSONObject jsonObject = getResponse(url);
//		 System.out.println(jsonObject);

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

	private static void trackFOs(Map<String, JSONObject> ivpData, ArrayList<String> trackList) throws Exception {
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		//note that ivp Data is filtered based on trackList if exists, if not using IVP, and OI liquidity
		Iterator<String> ivpDataIterator = ivpData.keySet().iterator();
		while (ivpDataIterator.hasNext()) {
			try {
			String symbol = ivpDataIterator.next();
			JSONObject symbolIVData = ivpData.get(symbol);
			
			Long equitySellQuantity = 0L;
			Long equityBuyQuantity = 0L;
			float equityBuySellRatio  =0.0F;
			
			if (!symbol.contentEquals("NIFTY") & !symbol.contentEquals("BANKNIFTY")) {
			
			///// get equity depth chart data
			JSONObject equityDepthData = getEquityDepthChartData(symbol);
//				System.out.println(depthData);
			equitySellQuantity = (Long) equityDepthData.get("totalSellQuantity");
			equityBuyQuantity = (Long) equityDepthData.get("totalBuyQuantity");
			try {
				equityBuySellRatio = ((float) equityBuyQuantity / equitySellQuantity);
				equityBuySellRatio = Float.valueOf(decimalFormat.format(equityBuySellRatio));
			} catch (Exception e) {
				equityBuySellRatio = 0.0F;
			}
			Float equityMarketCap  = Float.valueOf(((JSONObject)equityDepthData.get("tradeInfo")).get("totalMarketCap").toString());
			
			//if there are stocks in trackList, do not filter using IV, OI liquidity, equity marketCap
			if(trackList.isEmpty()) {
				//filter based on equity market cap only if tracklist is empty.
//				System.out.println(equityMarketCap);
//				System.out.println((float) 19582869);
				if(!(equityMarketCap.compareTo((float) 19582869)>0)) {
					//reliance data used as a base here.
					
					//skip the stock that does not have enough market cap
					continue;
					}
				
			}
			}
			// get futures depth chart data

			// get underlying price as last traded price.

			JSONObject derivativesData = getDerivatesData(symbol);

			// currentPrice
			Double underlyingPrice = Double.valueOf(derivativesData.get("underlyingValue").toString());
			// get total buy and sell quantities of futures.//enable commented log to verify
			// data integrity.

			JSONArray stockDerivates = (JSONArray) derivativesData.get("stocks");
			
			Iterator<JSONObject> derivativeIterator = stockDerivates.iterator();
			Long totalFutureSellQuantity = 0L;
			Long totalFutureBuyQuantity = 0L;
			int futureInstrumentCount = 0;

			String optionExpiryDate = "";
			Double percentChange = null;
			while (derivativeIterator.hasNext()) {
				JSONObject stockObj = derivativeIterator.next();
				JSONObject stockMetadata = (JSONObject) stockObj.get("metadata");
				String instrumentType = (String) stockMetadata.get("instrumentType");
				JSONObject marketDepthData = (JSONObject) stockObj.get("marketDeptOrderBook");

				if ((futureInstrumentCount < 2 & (instrumentType.equalsIgnoreCase("Stock Futures") || instrumentType.equalsIgnoreCase("Index Futures")))) {
					futureInstrumentCount++;
					Long futureSellQuantity = (Long) marketDepthData.get("totalSellQuantity");
					Long futureBuyQuantity = (Long) marketDepthData.get("totalBuyQuantity");

//					System.out.println("future data of " + symbol + " expiryDate: " + stockMetadata.get("expiryDate")
//							+ " buy:" + futureBuyQuantity + "  sell: " + futureSellQuantity);

					totalFutureBuyQuantity += futureBuyQuantity;
					totalFutureSellQuantity += futureSellQuantity;
					JSONObject otherInfo = (JSONObject) marketDepthData.get("otherInfo");
					//
					if (percentChange == null) {
						Double lastPrice = Double.valueOf(stockMetadata.get("lastPrice").toString());
						Double prevClose = Double.valueOf(stockMetadata.get("prevClose").toString());
						percentChange = ((lastPrice - prevClose) / prevClose) * 100.0;
					}

				}
			}
			double optionIV = Double.valueOf(symbolIVData.get("impliedVolatility").toString());
			double optionIVPercentile = Double.valueOf(symbolIVData.get("iv_percentile").toString());
			double previousIV = Double.valueOf(symbolIVData.get("prev_iv").toString());
			float putCallRatio = Float.valueOf(symbolIVData.get("pcr").toString());

			optionExpiryDate = symbolIVData.get("expiry").toString();
			String optionStrikePrice = symbolIVData.get("strike").toString();

			float futureBuySellRatio;
			try {
				futureBuySellRatio = ((float) totalFutureBuyQuantity / totalFutureSellQuantity);
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

				totalBuySellRatio = Float.valueOf(decimalFormat.format(totalBuySellRatio));

			} catch (Exception e) {
				System.out.println("Alert: total buy sell ratio is unusual");
				totalBuySellRatio = 0.0F;
			}

			//////////////////////////////////////// write to csv here
			String filePath = "./data/" + new Date().getMonth() + "/" + symbol + ".csv";

			File file = new File(filePath);
			FileWriter outputfile;
			CSVWriter writer;
			outputfile = new FileWriter(file, true);
			
				writer = new CSVWriter(outputfile);
				// cannot get percentageChange from both the apis

				// double percentChange from gainers losers list every now and then as
				// calculated from future prevClose of yesterday

				String[] nextLine = { totalBuySellRatio + "", decimalFormat.format(percentChange) + "",
						decimalFormat.format(optionIVPercentile) + "", decimalFormat.format(optionIV) + "",
						decimalFormat.format(previousIV) + "", equityBuySellRatio + "", futureBuySellRatio + "",decimalFormat.format(putCallRatio)+"",
						"" + equityBuyQuantity, "" + equitySellQuantity, "" + totalFutureBuyQuantity,
						"" + totalFutureSellQuantity, underlyingPrice + "", optionStrikePrice + "",
						optionExpiryDate + "", new Date() + "" };
				writer.writeNext(nextLine);
				String printString = totalBuySellRatio + "  " + decimalFormat.format(percentChange) + "   "
						+ decimalFormat.format(optionIVPercentile) + " " + decimalFormat.format(optionIV) + " "
						+ decimalFormat.format(previousIV) + "   " + +equityBuySellRatio + " " + futureBuySellRatio
						+ " " + decimalFormat.format(putCallRatio)+ "  " + equityBuyQuantity + " " + equitySellQuantity + " " + totalFutureBuyQuantity + " "
						+ totalFutureSellQuantity + " " + underlyingPrice + " " + optionStrikePrice + "  "
						+ optionExpiryDate + "  " + new Date() + "  " + symbol;
//				if(totalBuySellRatio>1.5 || totalBuySellRatio < 0.666) {
					
					System.out.println(printString);
//				}
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			}
			
		}
	}
}