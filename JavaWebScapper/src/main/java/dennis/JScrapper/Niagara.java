package dennis.JScrapper;

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

	static final String[] allHeaders= {"address","Owner Name","Address 1","Address 2","Unit Name","Unit Number","City/State/Zip","Maps","Total","Total Land","County Taxable (Niagara)","Town Taxable","School Taxable","Village Taxable","Equalization Rate","Full Market Value","Maps","Total","Total Land","County Taxable (Niagara)","Town Taxable","School Taxable","Village Taxable","Equalization Rate","Full Market Value","Maps","Total","Total Land","County Taxable (Niagara)","Town Taxable","School Taxable","Village Taxable","Equalization Rate","Full Market Value","Year Built","House Style","Total SQFT *","1st Story SQFT *","2nd Story SQFT *","1/2 Story SQFT *","3/4 Story SQFT *","Add'l Story SQFT *","Finished Attic SQFT *","Finished Basement SQFT *","Finished Rec Room SQFT *","Finished Over Garage SQFT *","Number of Stories","Overall Condition","Exterior Wall Material","Bedrooms","Baths","Kitchens","Basement Type","Central Air","Heat Type","Fuel Type","Fireplaces","Garage(s)","Garage(s) SQFT","Type","Use","Ownership Code","Zoning","Road Type","Water Supply","Utilities","School District","Neighborhood Code","Sale Date","Sale Price","Useable Sale","Arms Length","Prior Owner Name","Deed Book","Deed Page","Deed Date","Year Built","House Style","Total SQFT *","1st Story SQFT *","2nd Story SQFT *","1/2 Story SQFT *","3/4 Story SQFT *","Add'l Story SQFT *","Finished Attic SQFT *","Finished Basement SQFT *","Finished Rec Room SQFT *","Finished Over Garage SQFT *","Number of Stories","Overall Condition","Exterior Wall Material","Bedrooms","Baths","Kitchens","Basement Type","Central Air","Heat Type","Fuel Type","Fireplaces","Garage(s)","Garage(s) SQFT","Type","Use","Ownership Code","Zoning","Road Type","Water Supply","Utilities","School District","Neighborhood Code","Sale Date","Sale Price","Useable Sale","Arms Length","Prior Owner Name","Deed Book","Deed Page","Deed Date","Type","Use","Ownership Code","Zoning","Road Type","Water Supply","Utilities","School District","Neighborhood Code","Sale Date","Sale Price","Useable Sale","Arms Length","Prior Owner Name","Deed Book","Deed Page","Deed Date"};
	static int skipStreetCount = 0;
	static int maxStreetRecordCount = 200;
	static boolean addedHeader = false;

	static int recordStreetCount = 0;
	static int recordCount = 0;
	static String min = "150000";
	static String max = "550000";
	
	static  String dataFile = "data.txt";
	static final String mainURL = "https://niagarafalls.oarsystem.com/assessment/main.asp?swis=291100&dbg=&opt=&swis=&sbl=&parcel9=&debug=";
	static final String propertyBaseUrl = "https://niagarafalls.oarsystem.com/assessment/r1parc.asp";

//	String urlStreetLookup = "https://niagarafalls.oarsystem.com/assessment/pcllist.asp?swis=291100&sbl=&address1=&address2="
//			+ searchString + "&owner_name=&page=2";

	static final String urlStreetLookup = "https://niagarafalls.oarsystem.com/assessment/pcllist.asp?swis=291100&sbl=&address1=&address2={0}&owner_name=&page={1}";

	public static void main(String[] args) throws Exception {

		// Document doc = Jsoup.connect(mainURL).get();
		dataFile=System.getProperty("user.dir").replace("\"", "\\")+"\\"+dataFile;
		System.out.println("dataFile=" + dataFile);
		

		Response res = Jsoup.connect(mainURL).timeout(0).method(Method.GET).execute();

		Document doc = res.parse();

		Elements el = doc.getElementsByTag("option");

		List<String> streets = new ArrayList<String>();

		for (int i = 0; i < el.size(); i++) {
			streets.add(el.get(i).attr("value"));
		}

		for (int i = skipStreetCount; i < streets.size(); i++) {

			lookUpStreet(streets.get(i), res.cookies());
			recordStreetCount++;
			System.out.println("Street Count = " + i + " of " + streets.size());

			if (recordStreetCount > maxStreetRecordCount) {
				// break;
			}
		}

		System.out.println("Complete. recordCount=" + recordCount);

	}

	static void lookUpStreet(String searchString, Map<String, String> cookies) {

		int page = 1;
		int recordCountOnEachPage = 100;

		while (recordCountOnEachPage > 99) {

			try {
				System.out.println("about to fetch for page " + page);
				String urlStreetPage = java.text.MessageFormat.format(urlStreetLookup, searchString, page);
				page++;

				Response res = Jsoup.connect(urlStreetPage).timeout(0).method(Method.GET).execute();

				Document docListings = res.parse();

				//System.out.println(docListings.html());

				Elements el = docListings.getElementsByAttributeValueContaining("id","lnk");
				recordCountOnEachPage = el.size();

				List<String> properties = new ArrayList<String>();

				for (int i = 0; i < el.size(); i++) {
					if(el.get(i).html().contains("1007")) {
						properties.add(el.get(i).attr("onclick").replace("\"", "").trim());
					}
					
				}

				for (int i = 0; i < properties.size(); i++) {
					String propUrl = (propertyBaseUrl
							+ properties.get(i).substring(properties.get(i).indexOf("?"))).trim();
					Document docProp = Jsoup.connect(propUrl).timeout(0).method(Method.GET).execute().parse();
					// System.out.println(docProp.html());

					extractValues(docProp);

				}

			} catch (Exception e) {
				recordCountOnEachPage=0;// to prevent looping infinitely.
				e.printStackTrace();
			}

		} // end of while

	}

	static void extractValues(Document docProp) {
		StringBuffer sbHeader = new StringBuffer();
		StringBuffer sbValues = new StringBuffer();

		String address = docProp.getElementsByClass("auto-style1").get(0).html();

		address = address.substring(9, address.indexOf("br") - 1);

		Elements headings = docProp.getElementsByClass("headings");

		if (!addedHeader) {
			sbHeader.append("address\t");
		}
		sbValues.append(address + "\t");

		for (int i = 0; i < headings.size(); i++) {
			if (headings.get(i).text().equals("Owner Information")) {

				Element e = (Element) headings.get(i).parentNode().parentNode();

				Elements headers = e.getElementsByTag("tr").get(1).getElementsByTag("td");
				Elements values = e.getElementsByTag("tr").get(2).getElementsByTag("td");

				if (!addedHeader) {
					for (int k = 0; k < headers.size(); k++) {
						sbHeader.append(headers.get(k).text() + "\t");
					}
				}

				for (int k = 0; k < values.size(); k++) {
					sbValues.append(values.get(k).text() + "\t");
				}

			}
		}

		Elements tbodyTags = docProp.getElementsByTag("tbody");

		Map<String, String> data = new HashMap<String, String>();

		for (int i = 0; i < tbodyTags.size(); i++) {
			Elements trTags = tbodyTags.get(i).getElementsByTag("tr");
			for (int j = 0; j < trTags.size(); j++) {
				Elements tdTags = trTags.get(j).getElementsByTag("td");
				if (tdTags.size() == 2) {
					// for (int k = 0; k < tdTags.size(); k++) {}

					if (tdTags.get(1).getElementsByTag("input").size() == 0) {
						Element header=tdTags.get(0);
						if(header.getElementsByTag("table").size()==0){
						if (!addedHeader) {
	
								sbHeader.append( header.text()+ "\t");
							
							
							}
						
						sbValues.append(tdTags.get(1).html() + "\t");
						
						}

					}

					// data.put(tdTags.get(0).html(), tdTags.get(1).html());
					// System.out.println(docProp.html());
				}
			}
		}
		if (!addedHeader) {
			writeToFile(sbHeader.toString());
			addedHeader = true;
		}
		writeToFile(sbValues.toString());
		System.out.println("RecordCount = " + recordCount);
		recordCount++;
	}

	static void writeToFile(String data) {
		try {
			Path filePath = Paths.get(dataFile);
			if (!filePath.toFile().exists()) {
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
