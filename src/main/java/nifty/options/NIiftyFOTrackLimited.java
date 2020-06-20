package nifty.options;

import java.util.ArrayList;

public class NIiftyFOTrackLimited {

	//use this to fill the individual csvs of stocks that we want to trace more finer to observe deeper patterns.
	//cant be done for cause it takes more than 40  sec already before including future buy/sell quantities and ratios
	public static void main(String[] args) throws Exception {

//use args list of strings for symbols that need to be tracked.
        while(true) {		
        	try {
        		// eventually loop through args and add to trackList as well.
        	    ArrayList<String> trackList = new ArrayList<String>();
        	    trackList.add("CHOLAFIN");
        		trackFOs();
        	}
        	catch(Exception e) {
        		throw e;
        	}
        	//TimeUnit.SECONDS.sleep(22);
        	//TimeUnit.SECONDS.sleep(1);
        }
	}

	private static void trackFOs() {
		// TODO Auto-generated method stub
		
	}



}
