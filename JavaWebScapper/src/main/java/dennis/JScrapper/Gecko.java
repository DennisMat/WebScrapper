package dennis.JScrapper;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class Gecko {
	static String baseXpath = "/html/body/div[1]/div[6]/div[1]/div[1]/div/div/div[3]/div/div/div/div[2]/div[4]/div[6]/ul/li[3]/div/div/div[1]/div";
	static String suffix = "/div/div/div[2]";

	static String pressandHold = "//*[contains(@aria-label,'Human Challenge')]";

	static String listedByXpath1 = baseXpath + "[3]" + suffix;
	static String listedByXpath2 = baseXpath + "[4]" + suffix;

	public static void main(String[] args) throws IOException {

		String a1 = "https://www.zillow.com/homes/Niagara-Falls,-NY_rb/";
		String a2 = "https://www.zillow.com/niagara-falls-ny/2_p/";

		Response response = Jsoup.connect(a1).validateTLSCertificates(false).userAgent("Mozilla/5.0").timeout(0)
				.method(Method.GET).execute();

		Document doc = response.parse();

		System.out.println(doc.html());

	}

	public static WebDriver openBrowser(String url) {

		System.setProperty("webdriver.gecko.driver", "C:\\work\\geckodriver\\geckodriver.exe");

		FirefoxProfile firefoxProfile = new FirefoxProfile();

		// firefoxProfile.setPreference("permissions.default.image", 2);
		firefoxProfile.setPreference("dom.ipc.plugins.enabled.libflashplayer.so", false);
		FirefoxOptions firefoxOptions = new FirefoxOptions();

		firefoxOptions.setProfile(firefoxProfile);

		WebDriver driver = new FirefoxDriver(firefoxOptions);
		return driver;
	}

	static Map<String, String> getDetailsFromZillow(String url, WebDriver driver, Map<String, String> details) {
		driver.get(url);

		WebDriverWait wait = new WebDriverWait(driver, 20);
		// a wait time just to make sure that every thing is loaded

		boolean clickAndHold = true;
		
		Boolean pressButton1 = wait.until(ExpectedConditions.or(
				ExpectedConditions.visibilityOfElementLocated(By.xpath(pressandHold)),
				ExpectedConditions.visibilityOfElementLocated(By.xpath(baseXpath))
			)); 

		WebElement	checkElement = null;
		try {
			checkElement = driver.findElement(By.xpath(baseXpath));
		} catch (Exception e1) {
			
		}
		
		if(checkElement==null){
			
			WebElement pressButton = wait
					.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(pressandHold)));
			System.out.println("press and Hold button encountered");
			Actions actions = new Actions(driver);

			actions.moveToElement(pressButton).clickAndHold();

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			actions.release();
			clickAndHold = false;
		}
			
		
		/*
		while (clickAndHold) {
			try {
				WebElement pressButton = wait
						.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(pressandHold)));
				System.out.println("press and Hold button encountered");
				Actions actions = new Actions(driver);

				actions.moveToElement(pressButton).clickAndHold();

				Thread.sleep(10000);
				actions.release();
				clickAndHold = false;
			} catch (Exception e1) {
				clickAndHold = false;
				System.out.println("base path not found..." + baseXpath);
			}

			try {
				System.out.println("");
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(baseXpath)));
			} catch (Exception e1) {
				System.out.println("base path not found..." + baseXpath);
			}

		}
		
		*/

		String listedBy = "";
		WebElement listedByE = null;
		try {
			listedByE = driver.findElement(By.xpath(listedByXpath1));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println("path not found..." + listedByXpath1);
			// e.printStackTrace();
		}

		if (listedByE == null) {
			try {
				// listedByE =
				// wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(listedByXpath2)));
				listedByE = driver.findElement(By.xpath(listedByXpath2));
				listedBy = listedByE.getText() + listedByE.getText();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("2nd path not found..." + listedByXpath2);
				// e.printStackTrace();
			}
		}

		details.put("listedBy", listedBy);
		System.out.println("listed by " + listedBy);
		System.out.println();

		return details;
	}

	public static String getRequestToken(String url) {
		System.out.println("Generating request_token via selenium");
		WebDriver driver = null;
		String request_token = null;
		try {

			System.setProperty("webdriver.gecko.driver", "C:\\work\\geckodriver\\geckodriver.exe");
			String u = "(//input[@type='text'])[1]";
			String p = "(//input[@type='password'])[1]";
			String b = "//button[1]";

			FirefoxOptions o = new FirefoxOptions();

			driver = new FirefoxDriver(o);
			driver.get(url);

			WebDriverWait wait = new WebDriverWait(driver, 20);

			WebElement userid = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(u)));
			WebElement pwd = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(p)));
			WebElement btn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(b)));

			System.out.println("In first page");

			userid.sendKeys("xxxx");
			pwd.sendKeys("yyyyy");

			btn.click();

			System.out.println("after first button click");
			System.out.println("5 sec wait...");
			Thread.sleep(10000);// only this is working driver.manage().timeouts().implicitlyWait
			WebElement btnContinue = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(b)));
			WebElement pin = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(p)));
			// System.out.println("line3 ="+driver.getPageSource());
			System.out.println("likely in second page");
			pin.sendKeys("zzzz");
			System.out.println("putting pin");
			btnContinue.click();
			System.out.println("another 5 sec wait...");
			Thread.sleep(10000);

			System.out.println("after waiting for 10 sec");
			wait.until(ExpectedConditions.urlContains("127.0.0.1"));
			System.out.println("url is " + driver.getCurrentUrl());
			String param = driver.getCurrentUrl().split("\\?")[1];
			String[] pairs = param.split("&");
			for (int i = 0; i < pairs.length; i++) {
				int idx = pairs[i].indexOf("=");
				String key = URLDecoder.decode(pairs[i].substring(0, idx), "UTF-8");
				String value = URLDecoder.decode(pairs[i].substring(idx + 1), "UTF-8");
				if (key.equals("request_token")) {
					request_token = value;
					System.out.println("request_token  is " + value);
					break;
				}

			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			driver.quit();
		}

		return request_token;
	}

}
