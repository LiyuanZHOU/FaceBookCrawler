package scr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GetUserTweets {
	static String testFile = "data\\SemEval2018\\us_trial.text";
	static String outFile = "data\\SemEval2018\\dev_userList.txt";
	static BufferedWriter out;
	static HashSet<String> users;

	public static void main(String[] args) throws IOException, InterruptedException {

		users = new HashSet<String>();

		FileWriter Predictedstream = new FileWriter(outFile, true);
		out = new BufferedWriter(Predictedstream);

		BufferedReader br = new BufferedReader(new FileReader(testFile));
		String line = "";
		int id = 0;
		int found = 0;
		while ((line = br.readLine()) != null) {
			System.out.println(id);
			System.out.println("line: " + line);

			Query query = new Query(line);

			query.author = searchAuthor(query, id);

			id++;

			if (query.author != "") {
				users.add(query.author);
				found++;
				System.out.println("found: " + found);
			}
			// TODO: for debugging
			// if (id > 10)
			// break;

		}
		System.out.println("number of users: " + users.size());
		for (String user : users)
			out.write(user.substring(1, user.length() - 1) + "\r\n");

		out.close();
		br.close();
	}

	private static String searchAuthor(Query query, int id) throws InterruptedException, IOException {

		// System.out.println("search term: " + query.queryString);

		if (query.query == "")
			return "";

		Document doc = null;
		try {
			doc = Jsoup.connect("https://twitter.com/search?q=" + query.query).get();
		} catch (IOException e) {
			// e.printStackTrace();
			System.out.println("Network Connection Error, waite for 1 second to retry....");
			TimeUnit.SECONDS.sleep(1);
			return searchAuthor(query, id);
		}
		Element results = doc.getElementById("stream-items-id");
		Elements list = null;
		try {
			list = results.getElementsByTag("li");
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
			//
			// if (!StringUtil.isBlank(text)) {
			// System.out.println("Retrieved: " + text);
			// }
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
