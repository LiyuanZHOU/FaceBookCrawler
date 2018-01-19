package scr;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CrawTripAdvisor {
	public static void main(String[] args) throws IOException, InterruptedException {

		boolean loop = true;
		System.setProperty("webdriver.chrome.driver",
				"C:\\Users\\zho038\\workspace\\FaceBookCrawler\\lib\\chromedriver_win32\\chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		// Melbourne
		// driver.get("https://www.tripadvisor.com.au/Restaurants-g255100-Melbourne_Victoria.html");
		// Gold Coast
		driver.get("https://www.tripadvisor.com.au/Restaurants-g255347-Bendigo_Greater_Bendigo_Victoria.html");

		WebDriverWait wait = new WebDriverWait(driver, 30);

		// System.out.println("first check point");

		int pageNumber = 1;

		while (loop) {
			WebElement page = null;
			// Wait here to get this page loaded
			TimeUnit.SECONDS.sleep(5);
			WebElement nextButton = null;
			if (pageNumber == 1) {
				nextButton = wait.until(ExpectedConditions
						.elementToBeClickable(By.cssSelector("#EATERY_LIST_CONTENTS > div.deckTools.btm > div > a")));
				page = driver.findElement(By.id("EATERY_SEARCH_RESULTS"));

			} else {
				nextButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
						"#EATERY_LIST_CONTENTS > div.deckTools.btm > div > a.nav.next.rndBtn.ui_button.primary.taLnk")));
				page = driver.findElement(By.id("EATERY_OVERVIEW"));
			}

			List<WebElement> list = page.findElements(By.className("title"));

			// System.out.println(list.get(0).text());
			// System.out.println(list.size());

			for (WebElement e : list) {
				System.out.println(e.getText());
			}
			// Elements nextList = page.getElementsByAttributeValue("class",
			// "nav next rndBtn ui_button primary taLnk");
			// Element next = nextList.get(0);
			try {
				// System.out.println("wait for debugging...");
				// WebElement nextButton = wait.until(ExpectedConditions
				// .elementToBeClickable(By.cssSelector("#EATERY_LIST_CONTENTS >
				// div.deckTools.btm > div > a")));
				nextButton.click();
				pageNumber++;
			} catch (Exception e) {
				loop = false;
			}
		}

		driver.close();
		driver.quit();

	}

}
