package scr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.naming.AuthenticationException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.Page;
import com.restfb.types.Page.Engagement;

public class GetFacebookInfo {

	static String appId = "157504918205645";
	static String appSecret = "1f402c39cc0219582783bd7d5753709f";
	static String readListFile = "C:\\Users\\zho038\\workspace\\FaceBookCrawler\\data\\Wollongong.txt";
	static String saveFile = "C:\\Users\\zho038\\workspace\\FaceBookCrawler\\data\\Restaurants.csv";
	static char seperator = ';';

	public static String getExtendedAccessToken(String accessToken)
			throws AuthenticationException, InterruptedException {
		String token = "";

		System.setProperty("webdriver.chrome.driver",
				"C:\\Users\\zho038\\workspace\\FaceBookCrawler\\lib\\chromedriver_win32\\chromedriver.exe");
		WebDriver driver = new ChromeDriver();
		driver.get("https://developers.facebook.com/tools/explorer?method=GET&path=&version=v2.11");

		WebDriverWait wait = new WebDriverWait(driver, 30);

		TimeUnit.SECONDS.sleep(5);
		WebElement getButton = null;
		getButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
				"body > div._li._4xit > div._53vp > div > div > div > div._3a8w._4-u3 > div > div._1c2l.uiPopover._6a._6b > a")));
		getButton.click();
		return token;
	}

	@SuppressWarnings({ "resource", "deprecation" })
	public static void main(String[] args)
			throws FileNotFoundException, IOException, InterruptedException, AuthenticationException {
		int i = 0;
		String accessToken = "157504918205645|_EoqX1V7hc3aVoI_CAFphRn9H80";

		FacebookClient fbClient = null;

		// while (tokenValid == false) {
		// try {
		fbClient = new DefaultFacebookClient(accessToken);
		// tokenValid = true;
		// } catch (com.restfb.exception.FacebookException e) {
		// System.out.print("Access Token Expired, requiring a new one...");
		// accessToken = getExtendedAccessToken("");
		// }
		// }

		BufferedWriter out = null;
		FileWriter fstream = new FileWriter(saveFile, true);
		out = new BufferedWriter(fstream);

		BufferedReader br = new BufferedReader(new FileReader(readListFile));

		String line;
		line = br.readLine();
		while (line != null) {
			String searchTerm = line;
			// search?type=place&q=cafe&center=40.7304,-73.9921&distance=1000&fields=name,checkins,picture
			Connection<Page> results = null;
			try {
				results = fbClient.fetchConnection("search", Page.class, Parameter.with("q", searchTerm),
						Parameter.with("type", "page"),
						// Parameter.with("center", "-35.2425, 149.0733"),
						// Parameter.with("distance", "100"),
						Parameter.with("fields",
								"name,id,about,category,username,engagement,country_page_likes,company_overview,website,emails,contact_address,food_styles,general_info,is_chain,phone,place_type,rating_count,single_line_address"));
				TimeUnit.SECONDS.sleep(5);
			} catch (com.restfb.exception.FacebookNetworkException e) {
				e.printStackTrace();
				System.out.println("Network Connection Error, waite for 1 minute to retry....");
				TimeUnit.SECONDS.sleep(1);
				// TimeUnit.MINUTES.sleep(1);
				continue;
			} // catch (com.restfb.exception.FacebookOAuthException e) {
				// System.out.println(" Error validating access token: Session
				// has
				// expired, get new one....");
				// accessToken = getExtendedAccessToken("");
				// fbClient = new DefaultFacebookClient(accessToken);
				// continue;
				// } // catch "too frequent operation exception here, wait 30
				// minutes"

			line = br.readLine();

			// for(List<Page> pages : results){
			// for (Page page : pages){
			if (results != null) {
				List<Page> pages = results.getData();

				if (!pages.isEmpty()) {
					Page page = pages.get(0);
					i++;
					Engagement engage = page.getEngagement();

					System.out.println(i + ". " + page.getName());
					System.out.println("\t ID: " + page.getId());
					System.out.println("\t email: " + page.getEmails());
					System.out.println("\t about: " + page.getAbout());
					System.out.println("\t category: " + page.getCategory());
					System.out.println("\t Phone: " + page.getPhone());
					System.out.println("\t placeType: " + page.getPlaceType());
					System.out.println("\t website: " + page.getWebsite());
					System.out.println("\t likes: " + engage.getCountStringWithLike());
					System.out.println("\t foodStyle: " + page.getFoodStyles());
					System.out.println("\t address: " + page.getSingleLineAddress());
					System.out.println("\t userName: " + page.getUsername());

					out.write("\n");
					out.write(page.getName() + seperator);
					out.write(page.getPhone() + seperator);
					out.write("" + seperator);// for mobile
					out.write("" + seperator);// for fax
					for (int j = 0; j < 3; j++) {
						if (j < page.getEmails().size())
							out.write(page.getEmails().get(j) + seperator);
						else
							out.write("" + seperator);
					}
					out.write(page.getWebsite() + seperator);
					out.write("https://www.facebook.com/" + page.getId() + seperator);
					out.write("" + seperator);// for twitter
					out.write(page.getUsername() + seperator);
					out.write(page.getSingleLineAddress() + seperator);
				}
			}
		}
		out.close();
	}
}
