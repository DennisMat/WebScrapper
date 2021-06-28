package dennis.JScrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
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
	
	static int maxRecordCount=1;
	
	
	static int recordCount=0;
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
			
			lookUpStreet(streets.get(i), res.cookies());
			recordCount++;
			System.out.println("Record Count = " + recordCount);
			
			if(recordCount>maxRecordCount) {
				break;
			}
		}
		
		System.out.println("Complete");

	}

	static void lookUpStreet(String searchString, Map<String, String> cookies) {
		try {


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

				extractValues(tbodyTags);
				
			

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static   void extractValues(Elements tbodyTags) {
		Map<String, String> data = new HashMap<String, String>();

		StringBuffer sbHeader=new StringBuffer();
		StringBuffer sbValues=new StringBuffer();
		
		for (int i = 0; i < tbodyTags.size(); i++) {
			Elements trTags = tbodyTags.get(i).getElementsByTag("tr");
			for (int j = 0; j < trTags.size(); j++) {
				Elements tdTags = trTags.get(j).getElementsByTag("td");
				if (tdTags.size() == 2) {
					//for (int k = 0; k < tdTags.size(); k++) {}
					sbHeader.append(tdTags.get(0).html());
					sbValues.append(tdTags.get(1).html());
					
					//data.put(tdTags.get(0).html(), tdTags.get(1).html());
					// System.out.println(docProp.html());
				}
			}
		}
		writeToFile(sbHeader.toString());
		writeToFile(sbValues.toString());
		
	}
	
	
	static void writeToFile(String data) {
		try {
			Path filePath = Paths.get("C:\\dennis\\work\\WebScrapper\\JavaWebScapper\\data.txt");
			if(!filePath.toFile().exists()) {
				filePath.toFile().createNewFile();
			}
			List<String> lines = Arrays.asList(data);
			Files.write(filePath, lines, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
