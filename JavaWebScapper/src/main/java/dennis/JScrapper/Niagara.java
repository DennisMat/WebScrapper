package dennis.JScrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Niagara {

	public static void main(String[] args) throws Exception {
		String min = "150000";
		String max = "550000";
		String mainURL = "https://niagarafalls.oarsystem.com/assessment/main.asp?swis=291100&dbg=&opt=&swis=&sbl=&parcel9=&debug=";
		// Document doc = Jsoup.connect(mainURL).get();

		Response res = Jsoup.connect(mainURL).method(Method.GET).execute();

		Document doc = res.parse();

		Elements el = doc.getElementsByTag("option");

		List<String> streets = new ArrayList<String>();

		for (int i = 0; i < el.size(); i++) {
			streets.add(el.get(i).attr("value"));
		}

		for (int i = 0; i < streets.size(); i++) {
			// parseCondoPage(urls.get(i));
			lookUpStreet(streets.get(i), res.cookies());
		}

	}

	static void lookUpStreet(String searchString, Map<String, String> cookies) {
		try {

//    		searchString=searchString.replace(" ", "+");
//    		  Response res = Jsoup.connect("https://niagarafalls.oarsystem.com/assessment/pcllist.asp?swis=291100")
//					 .data("debug", "bdebug")
//			         .data("streetlookup", "yes")
//			         .data("address2", searchString).cookies(cookies).method(Method.POST).execute();
//			
//    		Document docListings = res.parse();

			String urlStreetLookup = "https://niagarafalls.oarsystem.com/assessment/pcllist.asp?swis=291100&sbl=&address1=&address2="
					+ searchString + "&owner_name=&page=2";

			Response res = Jsoup.connect(urlStreetLookup).method(Method.GET).execute();

			Document docListings = res.parse();

			// System.out.println(docListings.html());

			Elements el = docListings.getElementsByClass("link");

			List<String> onClickattValues = new ArrayList<String>();

			for (int i = 0; i < el.size(); i++) {
				onClickattValues.add(el.get(i).attr("onclick").replace("\"", "").trim());

			}

			String propertyBaseUrl = "https://niagarafalls.oarsystem.com/assessment/r1parc.asp";

			for (int i = 0; i < onClickattValues.size(); i++) {
				String propUrl = (propertyBaseUrl
						+ onClickattValues.get(i).substring(onClickattValues.get(i).indexOf("?"))).trim();
				Document docProp = Jsoup.connect(propUrl).method(Method.GET).execute().parse();
				// System.out.println(docProp.html());

				Elements tbodyTags = docProp.getElementsByTag("tbody");

				getValue(tbodyTags);

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static void getValue(Elements tbodyTags) {
		Map<String, String> data = new HashMap<String, String>();

		for (int i = 0; i < tbodyTags.size(); i++) {
			Elements trTags = tbodyTags.get(i).getElementsByTag("tr");
			for (int j = 0; j < trTags.size(); j++) {
				Elements tdTags = trTags.get(j).getElementsByTag("td");
				if (tdTags.size() == 2) {
					for (int k = 0; k < tdTags.size(); k++) {

					}
					data.put(tdTags.get(0).html(), tdTags.get(1).html());
					// System.out.println(docProp.html());

				}

			}

		}

		int a = 3;
	}

	static int getInt(String raw) {
		int parsedOutput = 0;
		try {
			String numStr = raw.replace("$", "").replace(",", "");
			parsedOutput = Integer.parseInt(numStr);
		} catch (NumberFormatException e) {
		}
		return parsedOutput;
	}

}
