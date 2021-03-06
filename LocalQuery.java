package src;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalQuery {
	// int id;
	String line;

	List<String> tags;
	// Queue<String> ats;

	String queryString;
	String query;

	public LocalQuery(String line) {
		// this.tags = new LinkedList<String>();
		// this.ats = new LinkedList<String>();

		this.line = line;
		this.tags = new ArrayList<String>();

		String atUser = "@user";
		Pattern r = Pattern.compile(atUser);
		Matcher atUsers = r.matcher(line);
		line = atUsers.replaceAll("");

		line = line.replaceAll("@ ", "");

		String hashTags = "#\\w+";
		r = Pattern.compile(hashTags);
		Matcher matcher = r.matcher(line);
		while (matcher.find()) {
			this.tags.add(matcher.group());
		}
		//
		// String atPlace = "@\\s+([A-Z]+\\w*[ ]*)*";
		// r = Pattern.compile(atPlace);
		// Matcher atPlaces = r.matcher(line);
		// if (atPlaces.find()) {
		// for (int i = 0; i < atPlaces.groupCount(); i++) {
		// ats.add(atPlaces.group(i).replaceAll(" ", ""));
		// line = atPlaces.replaceAll(atPlaces.group(i).replaceAll(" ", ""));
		// }
		// }

		this.queryString = line;
		try {
			this.query = URLEncoder.encode(queryString, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	void shorter() {
		if (this.queryString.contains(" "))
			this.queryString = this.queryString.substring(0, this.queryString.lastIndexOf(" "));
		else
			this.queryString = "";
		try {
			this.query = URLEncoder.encode(this.queryString, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}