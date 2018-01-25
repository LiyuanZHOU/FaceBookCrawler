package scr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class GetUsersDriver {
	// static String testFile =
	// "/home/zho038/workspace/TwitterCrawler/data/SemEval2018/test_semeval2018task2_text/us_test.text";
	// static String outFile =
	// "/home/zho038/workspace/TwitterCrawler/data/SemEval2018/test_semeval2018task2_text/test_userList.txt";
	static String testFile = "/home/zho038/workspace/TwitterCrawler/data/SemEval2018/dev/us_trial.text";
	static String outFile = "/home/zho038/workspace/TwitterCrawler/data/SemEval2018/dev/dev_userList.txt";
	static BufferedWriter out;
	static HashSet<String> users;

	static WebDriver driver;
	static WebDriverWait wait;

	static int reservePoint = -1;

	public static void main(String[] args) throws IOException, InterruptedException {

		testFile = args[0];
		if (args.length > 1)
			reservePoint = Integer.valueOf(args[1]);
		outFile = testFile.substring(0, testFile.lastIndexOf(".")) + "userList.txt";
		FileWriter Predictedstream = new FileWriter(outFile, true);
		out = new BufferedWriter(Predictedstream);

		login();

		users = new HashSet<String>();

		BufferedReader br = new BufferedReader(new FileReader(testFile));
		String line = "";
		int id = 0;
		int found = 0;
		while ((line = br.readLine()) != null) {
			if (id < reservePoint) {
				id++;
				continue;
			}

			System.out.println(id);
			System.out.println("line: " + line);
			System.out.println(outFile);

			LocalQuery query = new LocalQuery(line);
			String author = searchAuthor(query, id);

			id++;

			out.write(line + "\t");
			out.flush();

			if (author != "") {
				users.add(author);
				found++;
				System.out.println("found: " + found);
				out.write(author.substring(1, author.length() - 1) + "\r\n");
				out.flush();
			}

			// System.out.println("found: " + found);
			// out.write(author.substring(1, author.length() - 1) + "\r\n");
			// out.flush();
			// TODO: for debugging
			// if (id > 10)
			// break;

		}
		System.out.println("number of users: " + users.size());
		out.close();
		br.close();
	}

	private static void login() throws InterruptedException {
		System.setProperty("webdriver.chrome.driver",
				"C:\\Users\\zho038\\workspace\\FaceBookCrawler\\lib\\chromedriver_win32\\chromedriver.exe");
		driver = new ChromeDriver();

		driver.get("https://twitter.com/");
		wait = new WebDriverWait(driver, 30);

		WebElement LogInButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
				"#doc > div.StreamsTopBar-container.StreamsTopBar-container--withStreamHero.StreamsTopBar-container--withTallHeader > div > div.StreamsHero.StreamsHero--tall > div.StreamsHero-buttonContainer > a.EdgeButton.EdgeButton--transparent.EdgeButton--medium.StreamsLogin.js-login")));
		LogInButton.click();

		String inputXpath = "//input[@name='session[username_or_email]']";
		WebElement account = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(inputXpath)));
		account.sendKeys("annjouno@gmail.com");

		String pswXpath = "//input[@name='session[password]']";
		WebElement psw = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(pswXpath)));
		psw.sendKeys("Nicta1234");

		String submit = "#login-dialog-dialog > div.LoginDialog-content.modal-content > div.LoginDialog-body.modal-body > div.LoginDialog-form > form > input.EdgeButton.EdgeButton--primary.EdgeButton--medium.submit.js-submit";
		WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(submit)));
		submitButton.click();

		TimeUnit.SECONDS.sleep(5);

	}

	private static String searchAuthor(LocalQuery query, int id) throws InterruptedException, IOException {
		if (query.query == "")
			return "";

		System.out.println(query.queryString);

		String inputXpath = "//input[@class='search-input']";
		WebElement inputField = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(inputXpath)));
		inputField.clear();
		try {
			inputField.sendKeys(query.queryString);
		} catch (Exception e) {
			return "";
		}

		String buttontXpath = "//button[@type='submit']";
		WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(buttontXpath)));
		button.click();

		WebElement results = null;
		while (true) {
			TimeUnit.SECONDS.sleep(2);
			try {
				results = driver.findElement(By.id("stream-items-id"));
				break;
			} catch (Exception e) {
				System.out.println("wait for 2s to retry");
			}
		}

		String userId = "";
		HashSet<String> userCount = new HashSet<String>();
		List<WebElement> userIDNodes;
		try {
			userIDNodes = results.findElements(By.xpath(
					"//a[@class='account-group js-account-group js-action-profile js-user-profile-link js-nav']"));
		} catch (Exception e) {
			return "";
		}
		for (WebElement element : userIDNodes) {
			String href;
			while (true) {
				TimeUnit.SECONDS.sleep(2);
				try {
					href = element.getAttribute("href");
					break;
				} catch (Exception e) {
					System.out.println("wait for 2s to retry");
				}
			}
			userId = href.substring(href.lastIndexOf('/'));
			// System.out.println("userID: " + userId);
			if (userId != "") {
				userCount.add(userId);
			}
		}

		if (userCount.size() > 0) {
			String returnValue = userCount.iterator().next().toString();
			System.out.println("USER ID IS :::::::::::::::::::::::::" + returnValue);
			return returnValue;
		} else
			return "";
	}
}