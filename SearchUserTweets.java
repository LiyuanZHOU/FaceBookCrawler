package scr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class SearchUserTweets {
	static String userListFile = "data\\SemEval2018\\dev_userList.txt";
	static BufferedWriter out;
	static HashMap<String, Integer> emojiMap;
	static Twitter twitter;

	public static void main(String[] args) throws IOException, InterruptedException {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey("o1niBmY9RuujOXeXhBTyTmeey");
		cb.setOAuthConsumerSecret("V48xDHjRpdPLceJjhP3bVRQpMxpOdhSeaJ7pgwTQGtUPWtpVpB");
		cb.setOAuthAccessToken("805666609293467648-EqaEPmEAv93hAROTVSPafpqmj2ACQ1P");
		cb.setOAuthAccessTokenSecret("AQP1apXtKJUFQ9roA6XAA1FA2iKZqryeMRVTb7AMG2gZb");

		twitter = new TwitterFactory(cb.build()).getInstance();

		emojiMap = SearchTwitter.InitialEmojiMap();
		BufferedReader br = new BufferedReader(new FileReader(userListFile));
		String line = "";
		while ((line = br.readLine()) != null) {
			if (!StringUtil.isBlank(line)) {
				ArrayList<String> tweets = searchAPI(line);
				saveTweets(tweets, line);
			}
			// TODO: For debugging
			break;
		}

		br.close();
	}

	private static void saveTweets(ArrayList<String> tweets, String line) throws IOException {
		String outFile = "data\\SemEval2018\\dev\\" + line + ".txt";
		FileWriter Predictedstream = new FileWriter(outFile, true);
		BufferedWriter out = new BufferedWriter(Predictedstream);

		for (String tweet : tweets) {
			out.write(tweet);
			out.write("\r\n");
			out.flush();
		}
		out.close();
	}

	private static ArrayList<String> searchAPI(String line) {

		ArrayList<String> retrieved = new ArrayList<String>();

		int pageno = 1;
		List<Status> statuses = new ArrayList<>();

		System.out.println("line : " + line);

		while (true) {
			try {
				int size = statuses.size();
				Paging page = new Paging(pageno++, 100);
				statuses.addAll(twitter.getUserTimeline(line.substring(1), page));
				if (statuses.size() == size)
					break;
			} catch (TwitterException e) {
				e.printStackTrace();
				System.out.println("Could not find user:" + line);
			}
		}

		for (Status status : statuses) {
			String text = status.getText();
			System.out.println(text);
		}

		return retrieved;
	}

	private static ArrayList<String> search(String line) throws InterruptedException {
		Document doc = null;

		Element results = doc.getElementById("stream-items-id");
		Elements list = null;

		try {
			list = results.getElementsByTag("li");
		} catch (java.lang.NullPointerException e) {
			return null;
		}

		System.out.println("Number of results: " + list.size());

		ArrayList<String> retrieved = new ArrayList<String>();
		for (Element result : list) {
			Elements paragraph = result.getElementsByTag("p");
			String text = paragraph.text().trim();

			if (!StringUtil.isBlank(text)) {
				System.out.println("Retrieved: " + text);
			}

			HashSet<String> emojiSet = new HashSet<String>();
			String label = "";
			Elements emojis = result.getElementsByTag("img");
			if (emojis.size() > 0) {
				for (Element emoji : emojis) {
					label = emoji.attr("title");
					if (label != "") {
						emojiSet.add(label);
					}
				}
			}

			if (emojiSet.size() == 1) {
				System.out.println("Emoji is :::::::::::::::::::::::::" + emojiSet.toString());
				String key = emojiSet.iterator().next();
				if (emojiMap.containsKey(key)) {
					int number = emojiMap.get(key);
					retrieved.add(text + "\t" + "__label__" + number);
				}
			}
		}

		return retrieved;
	}
}
