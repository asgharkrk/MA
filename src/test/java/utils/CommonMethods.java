package utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.v85.page.Page;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.Set;

public class CommonMethods extends PageInitializer {

    public static WebDriver driver;


    public static void openBrowserAndLaunchApplication()  {
        switch (ConfigReader.read("browser")) {
            case "Chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--headless");
                driver = new ChromeDriver(chromeOptions);
                break;
            case "FireFox":
                driver = new FirefoxDriver();
                break;
            case "Edge":
                driver = new EdgeDriver();
                break;
            default:
                throw new RuntimeException("Invalid Browser Name");
        }
        // Implicit wait
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(20));
        driver.manage().window().maximize();
        driver.get(ConfigReader.read("url"));
        initializePageObjects();

        // Load cookies from file
        loadCookies("cookies.data");
    }

    private static void loadCookies(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("Cookie file not found: " + file.getAbsolutePath());
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] token = line.split(";");
                String name = token[0];
                String value = token[1];
                String domain = token[2];
                String path = token[3];
                Date expiry = null;
                if (!token[4].equals("null") && !token[4].isEmpty()) {
                    expiry = sdf.parse(token[4]);
                }
                boolean isSecure = Boolean.parseBoolean(token[5]);
                boolean isHttpOnly = Boolean.parseBoolean(token[6]);

                Cookie cookie = new Cookie(name, value, domain, path, expiry, isSecure, isHttpOnly);
                driver.manage().addCookie(cookie);
            }
            driver.navigate().refresh();  // Refresh to apply cookies
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }


    private static void saveCookies(String filePath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            Set<Cookie> cookies = driver.manage().getCookies();
            for (Cookie cookie : cookies) {
                bw.write((cookie.getName() != null ? cookie.getName() : "") + ";"
                        + (cookie.getValue() != null ? cookie.getValue() : "") + ";"
                        + (cookie.getDomain() != null ? cookie.getDomain() : "") + ";"
                        + (cookie.getPath() != null ? cookie.getPath() : "") + ";"
                        + (cookie.getExpiry() != null ? cookie.getExpiry() : "") + ";"
                        + cookie.isSecure() + ";"
                        + cookie.isHttpOnly());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }}

    public void closeBrowser() {
        if(driver!= null) {
            saveCookies("cookies.data");
            driver.quit();
        }
    }

    public void sendText(String text, WebElement element){
        element.clear();
        element.sendKeys(text);
    }

    public void selectFromDropDown(WebElement dropDown, String visibleText){
        Select sel =new Select(dropDown);
        sel.selectByVisibleText(visibleText);
    }
    public void selectFromDropDown(String value, WebElement dropDown ){
        Select sel =new Select(dropDown);
        sel.selectByValue(value);
    }
    public void selectFromDropDown( WebElement dropDown,int index ){
        Select sel =new Select(dropDown);
        sel.selectByIndex(index);
    }

    public WebDriverWait getwait(){
        WebDriverWait wait= new WebDriverWait(driver, Duration.ofSeconds(Constants.EXPLICIT_WAIT));
        return  wait;
    }

    public void waitForElementToBeClickAble(WebElement element){
        getwait().until(ExpectedConditions.elementToBeClickable(element));
    }

    public void click(WebElement element){
        waitForElementToBeClickAble(element);
        element.click();
    }

    public JavascriptExecutor getJSExecutor(){
        JavascriptExecutor js = (JavascriptExecutor) driver;
        return js;
    }

    public void jsClick(WebElement element){
        getJSExecutor().executeScript("arguments[0].click();", element);
    }


    public byte[] takeScreenshot(String fileName){
        //it accepts array of byte in cucumber for the screenshot
        TakesScreenshot ts = (TakesScreenshot) driver;
        byte[] picByte = ts.getScreenshotAs(OutputType.BYTES);
        File sourceFile = ts.getScreenshotAs(OutputType.FILE);

        try {
            FileUtils.copyFile(sourceFile,
                    new File(Constants.SCREENSHOT_FILEPATH +
                            fileName+" "+
                            getTimeStamp("yyyy-MM-dd-HH-mm-ss")+".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return picByte;
    }

    public String getTimeStamp(String pattern){
        //this method will return the timestamp which we will add in ss method
        Date date = new Date();
        //12-01-1992-21-32-34
        //yyyy-mm-dd-hh-mm-ss
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }


}
