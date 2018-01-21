package scr;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SearchTwitter {
	static String testFile = "data\\SemEval2018\\us_trial.text";
	static String labelFile = "data\\SemEval2018\\us_trial.labels";
	static String exactMatch = "data\\SemEval2018\\exactMatch.csv";
	static String predicted = "data\\SemEval2018\\predicted.txt";
	// static BufferedWriter out;
	static BufferedWriter outPredicted;
	static char seperator = ';';

	static HashMap<String, Integer> emojiMap;
	static ArrayList<Integer> labelList;

	static int found = 0;
	static int correct = 0;

	public static void main(String[] args) throws IOException, InterruptedException {

		FileWriter Predictedstream = new FileWriter(predicted, true);
		outPredicted = new BufferedWriter(Predictedstream);

		InitialEmojiMap();
		// TODO: return some statistics from the trainig set
		// TODO: this is for development, need to modify
		LoadLabels();

		BufferedReader br = new BufferedReader(new FileReader(testFile));
		String line = "";
		int id = 0;
		while ((line = br.readLine()) != null) {
			System.out.println(id);
			System.out.println("line: " + line);

			Query query = new Query(line);

			int predicted = searchTweet(query, id);
			outPredicted.write(String.valueOf(predicted));
			outPredicted.write("\r\n");
			outPredicted.flush();

			int TrueLabelNumber = labelList.get(id);
			String TrueLabelText = getKeyByValue(emojiMap, TrueLabelNumber);

			if (predicted == TrueLabelNumber)
				correct += 1;

			System.out.println("Predeicted: " + getKeyByValue(emojiMap, predicted));
			System.out.println("TrueLable: " + TrueLabelText);
			System.out.println("found: " + found);
			System.out.println("correct: " + correct + "\taccuracy: " + correct * 1.0 / (id + 1));

			id++;

			// TODO: for debugging
			// if (id > 10)
			// break;
		}
		br.close();
		outPredicted.close();
	}

	private static int searchTweet(Query query, int id) throws InterruptedException, IOException {

		// System.out.println("search term: " + query.queryString);

		if (query.query == "")
			return 2;

		Document doc = null;
		try {
			doc = Jsoup.connect("https://twitter.com/search?q=" + query.query).get();
		} catch (IOException e) {
			// e.printStackTrace();
			System.out.println("Network Connection Error, waite for 1 second to retry....");
			TimeUnit.SECONDS.sleep(1);
			return searchTweet(query, id);
		}
		Element results = doc.getElementById("stream-items-id");
		Elements list = null;
		try {
			list = results.getElementsByTag("li");
		} catch (java.lang.NullPointerException e) {
			// System.out.println("Could not retrieve results");
			query.shorter();
			return searchTweet(query, id);
			// return 2;
		}

		System.out.println("Number of results: " + list.size());
		if (list.size() <= 3)
			found++;

		int outNumber = 0;
		String writeOut = "";
		TreeMap<String, Integer> tree = new TreeMap<String, Integer>();

		for (Element result : list) {
			Elements paragraph = result.getElementsByTag("p");

			String label = "";

			Elements emojis = result.getElementsByTag("img");
			if (emojis.size() > 0)
				for (Element emoji : emojis) {
					label = emoji.attr("title");
					if (label != "") {
						// System.out.println(label);
						if (tree.containsKey(label))
							tree.put(label, tree.get(label) + 1);
						else
							tree.put(label, 1);
						outNumber++;
						writeOut = label;
					}
				}
			// for (Element tweet : paragraph) {
			// String text = tweet.text();
			// System.out.println(c + ": " + text);
			// c++;
			// }
		}

		// System.out.println(outNumber);

		// Only found one emoji
		if (outNumber == 1) {
			int predictedNumber = 2;
			if (emojiMap.containsKey(writeOut))
				predictedNumber = emojiMap.get(writeOut);
			return predictedNumber;
		} else if (outNumber == 0) {
			// not able to find any emoji, put most popular one
			return 2;
		} else {
			// find multiple emojis,majority vote
			List<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>();
			entryList.addAll(tree.entrySet());
			// sort the collection
			Collections.sort(entryList, byMapValues);

			// TODO: Calculate edit distance, assign to the result with max
			// distance
			// TODO: When distance < threathhold, find exact match
			int index = 2;
			for (Entry<String, Integer> entry : entryList) {
				String key = entry.getKey();
				int value = entry.getValue();
				if (emojiMap.containsKey(key)) {
					index = emojiMap.get(key);
					break;
				}
			}
			return index;
		}
	}

	static Comparator<Map.Entry<String, Integer>> byMapValues = new Comparator<Map.Entry<String, Integer>>() {
		@Override
		public int compare(Map.Entry<String, Integer> left, Map.Entry<String, Integer> right) {
			return -left.getValue().compareTo(right.getValue());
		}
	};

	private static void LoadLabels() throws IOException {
		labelList = new ArrayList<Integer>();
		BufferedReader br = new BufferedReader(new FileReader(labelFile));
		String line = "";
		while ((line = br.readLine()) != null) {
			labelList.add(Integer.valueOf(line));
		}
		System.out.println("label list size: " + labelList.size());
		br.close();
	}

	private static void InitialEmojiMap() {
		emojiMap = new HashMap<String, Integer>();
		emojiMap.put("Heart suit", 0);
		emojiMap.put("Heavy red heart", 0);
		emojiMap.put("Growing heart", 0);
		emojiMap.put("Sparkling heart", 0);
		emojiMap.put("Beating heart", 0);
		emojiMap.put("Heavy heart exclamation mark ornament", 0);

		emojiMap.put("Smiling face with heart-shaped eyes", 1);

		emojiMap.put("Face with tears of joy", 2);
		emojiMap.put("Loudly crying face", 2);
		emojiMap.put("Pensive face", 2);
		emojiMap.put("Loudly crying face", 2);
		emojiMap.put("Rolling on the floor laughing", 2);
		emojiMap.put("Smiling face with open mouth and cold sweat", 2);

		// Could not find exact match
		emojiMap.put("Two hearts", 3);
		emojiMap.put("Fire", 4);

		emojiMap.put("Smiling face with smiling eyes", 5);
		emojiMap.put("Smiling face", 5);
		emojiMap.put("Slightly smiling face", 5);

		emojiMap.put("Smiling face with sunglasses", 6);
		emojiMap.put("Cat face with wry smile", 6);

		emojiMap.put("Sparkles", 7);
		emojiMap.put("Party popper", 7);
		emojiMap.put("Birthday cake", 7);
		emojiMap.put("Dizzy symbol", 7);

		emojiMap.put("Blue heart", 8);
		emojiMap.put("Green heart", 8);

		emojiMap.put("Face throwing a kiss", 9);
		emojiMap.put("Kiss mark", 9);
		emojiMap.put("Kissing face with closed eyes", 9);
		emojiMap.put("Kissing face", 9);

		emojiMap.put("Camera", 10);
		emojiMap.put("Film projector", 10);

		emojiMap.put("Flag of United States", 11);
		emojiMap.put("Sun with rays", 12);
		emojiMap.put("Purple heart", 13);
		emojiMap.put("Winking face", 14);

		emojiMap.put("Hundred points symbol", 15);
		emojiMap.put("Trophy", 15);

		emojiMap.put("Grinning face with smiling eyes", 16);
		emojiMap.put("Grinning face", 16);
		emojiMap.put("Smiling face with open mouth and tightly-closed eyes", 16);

		emojiMap.put("Christmas tree", 17);
		emojiMap.put("Wrapped present", 17);
		emojiMap.put("Confetti ball", 17);
		emojiMap.put("Balloon", 17);
		emojiMap.put("Cocktail glass", 17);

		emojiMap.put("Camera with flash", 18);

		emojiMap.put("Face with stuck-out tongue and winking eye", 19);
		emojiMap.put("Face with stuck-out tongue and tightly-closed eyes", 19);
		emojiMap.put("Face with stuck-out tongue", 19);
		emojiMap.put("Crazy face", 19);
	}

	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

}
