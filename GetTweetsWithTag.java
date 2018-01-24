package src;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class GetTweetsWithTag {
	static String tagTweetsList = "data/SemEval2018/tagTweetsList.txt";
	static Twitter twitter;

	static String trainFile = "data/SemEval2018/train/tweet_by_ID_19_12_2017__04_13_22.txt.text";
	static String devFile = "data/SemEval2018/dev/us_trail.text";
	static String testFile = "data/SemEval2018/test_semeval2018task2_text/us_test.text";

	static int totalRetrieved = 0;

	static int reservePoint = -1;

	public static void main(String[] args) throws IOException, InterruptedException {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey("o1niBmY9RuujOXeXhBTyTmeey");
		cb.setOAuthConsumerSecret("V48xDHjRpdPLceJjhP3bVRQpMxpOdhSeaJ7pgwTQGtUPWtpVpB");
		cb.setOAuthAccessToken("805666609293467648-EqaEPmEAv93hAROTVSPafpqmj2ACQ1P");
		cb.setOAuthAccessTokenSecret("AQP1apXtKJUFQ9roA6XAA1FA2iKZqryeMRVTb7AMG2gZb");

		twitter = new TwitterFactory(cb.build()).getInstance();

		FileWriter Predictedstream = new FileWriter(tagTweetsList, true);
		BufferedWriter out = new BufferedWriter(Predictedstream);

		SearchTwitter.InitialEmojiMap();

		BufferedReader br = new BufferedReader(new FileReader(trainFile));
		String line = "";

		int id = 0;
		while ((line = br.readLine()) != null) {
			if (id < reservePoint) {
				id++;
				continue;
			}
			System.out.println(id);
			System.out.println("line: " + line);

			LocalQuery queryLocal = new LocalQuery(line);

			searchTag(queryLocal, out);

			System.out.println("Retrieved " + totalRetrieved + " tweets");
			id++;

			// TODO: for debugging
			if (id > 10)
				break;
		}
		out.close();
		br.close();
		System.out.println("Done!");
	}

	private static void searchTag(LocalQuery queryLocal, BufferedWriter outStream) throws IOException, InterruptedException {
		List<String> tags = queryLocal.tags;

		for (String tag : tags) {
			Query query = new Query(tag);
			int numberOfTweets = 500;
			List<Status> statuses = new ArrayList<>();
			long lastID = Long.MAX_VALUE;
			
			int empty = 0;
			while (statuses.size() < numberOfTweets) {
				if (numberOfTweets - statuses.size() > 100)
					query.setCount(100);
				else
					query.setCount(numberOfTweets - statuses.size());
				try {
					QueryResult result = twitter.search(query);
					statuses = result.getTweets();
					for (Status status : statuses) {
						long id = status.getId();
						outStream.write(String.valueOf(id));
						outStream.write("\r\n");
						outStream.flush();
						if (id < lastID)
							lastID = id;
						// System.out.println(id);
					}
					
					if(statuses.size() == 0) empty ++;
					else System.out.println("Gathered " + statuses.size() + " tweets" + "\n");
					if(statuses.size() % 400 == 0 ) {
			            try {
			            	System.out.println("Hit Twitter API Rate limit, wait for 15 mins to retry.");
			                    Thread.sleep(900000);
			                } catch (InterruptedException e) {
			                    e.printStackTrace();
			                }
			            }
					if(empty >5) break;
					totalRetrieved += statuses.size();

				} catch (TwitterException te) {
					System.out.println("Couldn't connect: " + te);
                    Thread.sleep(900000);
				}
				query.setMaxId(lastID - 1);
			}
		}
	}
}