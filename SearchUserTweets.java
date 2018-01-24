package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class SearchUserTweets {
	static String userListFile = "/home/zho038/workspace/TwitterCrawler/data/SemEval2018/debug_userList.txt";
	static Twitter twitter;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey("o1niBmY9RuujOXeXhBTyTmeey");
		cb.setOAuthConsumerSecret("V48xDHjRpdPLceJjhP3bVRQpMxpOdhSeaJ7pgwTQGtUPWtpVpB");
		cb.setOAuthAccessToken("805666609293467648-EqaEPmEAv93hAROTVSPafpqmj2ACQ1P");
		cb.setOAuthAccessTokenSecret("AQP1apXtKJUFQ9roA6XAA1FA2iKZqryeMRVTb7AMG2gZb");

		twitter = new TwitterFactory(cb.build()).getInstance();

		BufferedReader br = new BufferedReader(new FileReader(userListFile));
		String line = "";
		
		HashSet<String> userSet = new HashSet<String>();
		while ((line = br.readLine()) != null) {
			userSet.add(line.substring(1));
		}
		br.close();
		
		int number = 0;
		for (String user: userSet){
			if (number < -1) {
				number ++;
				continue;
			}
			String outFile = "data/SemEval2018/debug/" + user + ".txt";
			FileWriter Predictedstream = new FileWriter(outFile, true);
			BufferedWriter out = new BufferedWriter(Predictedstream);
			System.out.println("number: " + number);
			System.out.println("user : " + user);
			searchAPI(user, out);
			number++;
			out.close();
			
			if (number >10) break;
		}
		
		System.out.println("Done!");
	}

	private static void searchAPI(String user, BufferedWriter outStream) throws IOException {

		int pageno = 1;
		List<Status> statuses = new ArrayList<>();

		while (true) {
			try {
				int size = statuses.size();
				Paging page = new Paging(pageno++, 100);
				statuses.addAll(twitter.getUserTimeline(user, page));
				System.out.println(size + " tweets found");
				if (statuses.size() == size)
					break;
			} catch (TwitterException e) {
				e.printStackTrace();
				System.out.println("Could not find user:" + user);
			}
		}

		System.out.println("write to file...");
		for (Status status : statuses) {
			long id = status.getId();
			outStream.write(String.valueOf(id));
			outStream.write("\r\n");
			outStream.flush();
//			System.out.println(id);
		}
	}
}