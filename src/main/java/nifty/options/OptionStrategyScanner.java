package nifty.options;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Wait;

//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.firefox.FirefoxDriver;
//comment the above line and uncomment below line to use Chrome
//import org.openqa.selenium.chrome.ChromeDriver;
public class OptionStrategyScanner {


    public static void main(String[] args) throws InterruptedException {
        // declaration and instantiation of objects/variables
    	System.setProperty("webdriver.gecko.driver",  "/usr/local/bin/geckodriver");
		WebDriver driver = new FirefoxDriver();
		//comment the above 2 lines and uncomment below 2 lines to use Chrome
		//System.setProperty("webdriver.chrome.driver","G:\\chromedriver.exe");
		//WebDriver driver = new ChromeDriver();
    	
        String baseUrl = "https://web.sensibull.com/home";
        String expectedTitle = "Welcome: Mercury Tours";
        String actualTitle = "";

        // launch Fire fox and direct it to the Base URL
        driver.get(baseUrl);

        // get the actual value of the title
        actualTitle = driver.getTitle();
        System.out.println(actualTitle);
        /*
         * compare the actual title of the page with the expected one and print
         * the result as "Passed" or "Failed"
         */
//        if (actualTitle.contentEquals(expectedTitle)){
//            System.out.println("Test Passed!");
//        } else {
//            System.out.println("Test Failed");
//        }
        //close Fire fox
        TimeUnit.SECONDS.sleep(10);
        driver.close();
       
    }

}