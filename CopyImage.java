package scr;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CopyImage {

	public static void main(String[] args) throws Exception {

		System.setProperty("webdriver.chrome.driver",
				"C:\\Users\\zho038\\workspace\\FaceBookCrawler\\lib\\chromedriver_win32\\chromedriver.exe");
		WebDriver driver = new ChromeDriver();

		driver.get(
				"https://detail.tmall.com/item.htm?spm=a1z10.3-b-s.w4011-15061586411.92.17dfb6045fLZMj&id=532740213188&rn=4600b4086cc894639fc6563ba1c78155&abbucket=20&skuId=3591398987221");

		WebDriverWait wait = new WebDriverWait(driver, 30);

		List<WebElement> images = driver.findElements(By.tagName("img"));

		System.out.println(images.size());

		for (WebElement image : images) {
			String src = image.getAttribute("src");
			if (src != null && src.contains(".")) {
				String extension = src.substring(src.lastIndexOf('.'));
				System.out.println("extenstion: " + extension);
				System.out.println(src);
				// try (InputStream in = new URL(src).openStream()) {
				// Files.copy(in, Paths.get("C:/File/To/Save/To/image.jpg"));
				// }
			}
		}
		// System.out.println("");
	}

}
