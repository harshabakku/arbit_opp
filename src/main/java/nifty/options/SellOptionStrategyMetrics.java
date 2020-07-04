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

import javax.print.attribute.standard.MediaSize.Other;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.opencsv.CSVWriter;

public class SellOptionStrategyMetrics {

	// use this to fill the individual csvs of stocks that we want to trace more
	// finer to observe deeper patterns.
	// cant be done for cause it takes more than 40 sec already before including
	// future buy/sell quantities and ratios
	public static void main(String[] args) throws Exception {

//		// individual csv header string
//		String headerString = "totalBSRatio," + "equityBSRatio," + "totalFutureBSRatio," + "equitiyBuyQuantity,"
//				+ "equitySellQuantity," + "totalFutureBuyQuantity," + "totalFutureSellQuantity," + "percentageChange,"
//				+ "lastTradedPrice," + "date";
//		System.out.println(headerString);
//use args list of strings for symbols that need to be tracked.
		while (true) {
//			System.out.println("tracking FOs");
			try {
				// eventually loop through args and add to trackList as well.
				ArrayList<String> trackList = new ArrayList<String>();
//         	    trackList.add("CHOLAFIN");
				trackList.add("IBULHSGFIN");
				trackFOs(trackList);
			} catch (Exception e) {
				throw e;
			}
			// TimeUnit.SECONDS.sleep(22);
			// TimeUnit.SECONDS.sleep(1);
		}
	}

	private static JSONObject getDerivatesData(String symbol) throws Exception {

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

	private static void trackFOs(ArrayList<String> trackList) throws Exception {
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		Iterator<String> trackListIterator = trackList.iterator();

		while (trackListIterator.hasNext()) {
			String symbol = trackListIterator.next();

			///// get equity depth chart data
			JSONObject equityDepthData = getEquityDepthChartData(symbol);
			float equityBuySellRatio;
//				System.out.println(depthData);
			Long equitySellQuantity = (Long) equityDepthData.get("totalSellQuantity");
			Long equityBuyQuantity = (Long) equityDepthData.get("totalBuyQuantity");
			try {
				equityBuySellRatio = ((float) equityBuyQuantity / equitySellQuantity);
				equityBuySellRatio = Float.valueOf(decimalFormat.format(equityBuySellRatio));
			} catch (Exception e) {
				equityBuySellRatio = 0.0F;
			}

			// get futures depth chart data

			// get underlying price as last traded price.

			JSONObject derivativesData = getDerivatesData(symbol);

			// currentPrice
			Double underlyingPrice = (Double) derivativesData.get("underlyingValue");

			// get total buy and sell quantities of futures.//enable commented log to verify
			// data integrity.

			JSONArray stockDerivates = (JSONArray) derivativesData.get("stocks");

			Iterator<JSONObject> derivativeIterator = stockDerivates.iterator();
			Long totalFutureSellQuantity = 0L;
			Long totalFutureBuyQuantity = 0L;
			int futureInstrumentCount = 0;
			Long lastOptionStrikePrice = 0L;
			Double lastOptionPremium = 0.0;

			Double callOptionStrikePrice = 0.0;
			Double putOptionStrikePrice = 0.0;
			Double callOptionIV = 0.0;
			Double putOptionIV = 0.0;
			String optionExpiryDate = null;
			Double callOptionShortestDist = 1000000000000000.0;
			Double putOptionShortestDist = 1000000000000000.0;
			Double dailyVolatility = 0.0;
			Double percentChange = null;
			while (derivativeIterator.hasNext()) {
				JSONObject stockObj = derivativeIterator.next();
				JSONObject stockMetadata = (JSONObject) stockObj.get("metadata");
				String instrumentType = (String) stockMetadata.get("instrumentType");
				String optionType = (String) stockMetadata.get("optionType");
				JSONObject marketDepthData = (JSONObject) stockObj.get("marketDeptOrderBook");

				if ((futureInstrumentCount < 2 & instrumentType.equalsIgnoreCase("Stock Futures"))) {
					futureInstrumentCount++;
					Long futureSellQuantity = (Long) marketDepthData.get("totalSellQuantity");
					Long futureBuyQuantity = (Long) marketDepthData.get("totalBuyQuantity");

//					System.out.println("future data of " + symbol + " expiryDate: " + stockMetadata.get("expiryDate")
//							+ " buy:" + futureBuyQuantity + "  sell: " + futureSellQuantity);

					totalFutureBuyQuantity += futureBuyQuantity;
					totalFutureSellQuantity += futureSellQuantity;
					JSONObject otherInfo = (JSONObject) marketDepthData.get("otherInfo");
					dailyVolatility = (Double) otherInfo.get("dailyvolatility");
					if (percentChange == null) {
						Double lastPrice = Double.valueOf(stockMetadata.get("lastPrice").toString());
						Double prevClose = Double.valueOf(stockMetadata.get("prevClose").toString());
						percentChange = ((lastPrice - prevClose) / prevClose) * 100.0;
					}

				}

				if (instrumentType.equalsIgnoreCase("Stock Options")) {
					if (optionExpiryDate == null) {
						optionExpiryDate = (String) stockMetadata.get("expiryDate");
					} else {
						String currentExpiryDate = (String) stockMetadata.get("expiryDate");
						if (!currentExpiryDate.contentEquals(optionExpiryDate)) {
							continue;
						}
					}
					// eliminate strikes somehow by comparing with callOptionStrikePrice and
					// putOptionStrikePrice;
					Double currentOptionStrikePrice = Double.valueOf((Long) stockMetadata.get("strikePrice"));
//					System.out.println(currentOptionStrikePrice);
					Double distStrikeUnderlying = Math.abs(currentOptionStrikePrice - underlyingPrice);

					JSONObject otherInfo = (JSONObject) marketDepthData.get("otherInfo");
//					System.out.println(otherInfo);
					double currentOptionIV = Double.valueOf(otherInfo.get("impliedVolatility").toString());
					if (optionType.equalsIgnoreCase("Call")) {
						if (distStrikeUnderlying < callOptionShortestDist) {
							callOptionShortestDist = distStrikeUnderlying;
							callOptionStrikePrice = currentOptionStrikePrice;
							callOptionIV = currentOptionIV;
						}
					} else {
						// put option
						if (distStrikeUnderlying < putOptionShortestDist) {
							putOptionShortestDist = distStrikeUnderlying;
							putOptionStrikePrice = currentOptionStrikePrice;
							putOptionIV = currentOptionIV;
						}
					}

				}

			}
			if (callOptionStrikePrice.compareTo(putOptionStrikePrice) != 0) {

				System.out.println(
						"WARNINGWARNINGWARNINGWARNINGWARNINGWARNINGWARNINGWARNINGWARNINGWARNINGWARNINGWARNING");
				System.out.println("strike price's do not match to calculate IV");
			}
//			System.out.println(callOptionStrikePrice);
//			System.out.println(putOptionStrikePrice);
//			System.out.println(callOptionIV);
//			System.out.println(putOptionIV);
			double optionIV = (callOptionIV + putOptionIV) / 2;
//			System.out.println(dailyVolatility);

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
			String filePath = "./data/" + new Date().getDate() + "/" + symbol + ".csv";

			File file = new File(filePath);
			FileWriter outputfile;
			CSVWriter writer;
			outputfile = new FileWriter(file, true);
			try {
				writer = new CSVWriter(outputfile);
				// cannot get percentageChange from both the apis

				// double percentChange from gainers losers list every now and then as
				// calculated from future prevClose of yesterday

				String[] nextLine = { optionIV + "", callOptionIV + "", putOptionIV + "", totalBuySellRatio + "",
						equityBuySellRatio + "", futureBuySellRatio + "", "" + equityBuyQuantity,
						"" + equitySellQuantity, "" + totalFutureBuyQuantity, "" + totalFutureSellQuantity,
						decimalFormat.format(percentChange) + "", underlyingPrice + "", callOptionStrikePrice + "",
						optionExpiryDate + "", dailyVolatility + "", new Date() + "" };
				writer.writeNext(nextLine);
				String printString = optionIV + " " + callOptionIV + " " + putOptionIV + " "+ totalBuySellRatio + " " + equityBuySellRatio + " " + futureBuySellRatio + " " + " "
						+ equityBuyQuantity + " " + equitySellQuantity + " " + totalFutureBuyQuantity + " "
						+ totalFutureSellQuantity + " " + decimalFormat.format(percentChange) + " " + underlyingPrice
						+ " " + +callOptionStrikePrice + "  " + optionExpiryDate + "  " + dailyVolatility + "  "
						+ new Date() + " ";
				System.out.println(printString);
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
//			}

			}

		}

	}
}
