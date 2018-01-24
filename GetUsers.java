package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GetUsers {
//	static String testFile = "/home/zho038/workspace/TwitterCrawler/data/SemEval2018/test_semeval2018task2_text/us_test.text";
//	static String outFile = "/home/zho038/workspace/TwitterCrawler/data/SemEval2018/test_semeval2018task2_text/test_userList.txt";
	static String testFile = "/home/zho038/workspace/TwitterCrawler/data/SemEval2018/dev/us_trial.text";
	static String outFile = "/home/zho038/workspace/TwitterCrawler/data/SemEval2018/dev/dev_userList.txt";
	static BufferedWriter out;
	static HashSet<String> users;
	
	static int reservePoint = -1;

	public static void main(String[] args) throws IOException, InterruptedException {
		
		testFile = args[0];
		if (args.length > 1)
		reservePoint = Integer.valueOf(args[1]);
		
		outFile = testFile.substring(0, testFile.lastIndexOf(".")) + "userList.txt";


		users = new HashSet<String>();

		FileWriter Predictedstream = new FileWriter(outFile, true);
		out = new BufferedWriter(Predictedstream);

		BufferedReader br = new BufferedReader(new FileReader(testFile));
		String line = "";
		int id = 0;
		int found = 0;
		while ((line = br.readLine()) != null) {
			if (id < reservePoint){
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
			// TODO: for debugging
			// if (id > 10)
			// break;

		}
		System.out.println("number of users: " + users.size());
//		for (String user : users)
//			out.write(user.substring(1, user.length() - 1) + "\r\n");

		out.close();
		br.close();
	}

	private static String searchAuthor(LocalQuery query, int id) throws InterruptedException, IOException {

		// System.out.println("search term: " + query.queryString);

		if (query.query == "")
			return "";

		Document doc = null;
		
		int tmp = 0;
		System.out.println(query.query);
		
		while(tmp < 500){
			try {
				 Response response = Jsoup.connect("https://twitter.com/search?q=" + query.query).execute();
				 doc = response.parse();
//				 final File f = new File("/home/zho038/Desktop/filename.html");
//			        FileUtils.writeStringToFile(f, doc.outerHtml(), "UTF-8");
				 break;
			} catch (Exception e) {
				// e.printStackTrace();
				System.out.println("Network Connection Error, waite for 1 second to retry....");
				TimeUnit.SECONDS.sleep(1);
				tmp ++;
			}
		}
		
		Element results = doc.getElementById("stream-items-id");
		Elements list = null;
		try {
			list = results.getElementsByTag("li");
//			System.out.println("list size: "+ list.size());
		} catch (java.lang.NullPointerException e) {
			query.shorter();
			return searchAuthor(query, id);
		}

		System.out.println("Number of results: " + list.size());

		String userId = "";
		HashSet<String> userCount = new HashSet<String>();
		for (Element result : list) {
			Elements paragraph = result.getElementsByTag("p");
			String text = paragraph.text().trim();
			
			 if (!StringUtil.isBlank(text)) {
//			 System.out.println("Retrieved: " + text);
			 }
			Elements userIDNodes = result.getElementsByAttribute("data-user-id");
			userId = userIDNodes.attr("href");
			if (userId != "") {
				userCount.add(userId);
			}
		}
		if (userCount.size() == 1) {
			System.out.println("USER ID IS :::::::::::::::::::::::::" + userCount.toString());
			return userCount.toString();
		} else
			return "";
	}
}