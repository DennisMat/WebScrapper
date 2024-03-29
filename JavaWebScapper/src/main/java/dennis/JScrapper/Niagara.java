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

	/* IMPORTANT: contains duplicates */
	static final String[] allHeaders = { "address", "Owner Name", "Address 1", "Address 2", "Unit Name", "Unit Number",
			"City/State/Zip", "Maps", "Total", "Total Land", "County Taxable (Niagara)", "Town Taxable",
			"School Taxable", "Village Taxable", "Equalization Rate", "Full Market Value", "Maps", "Total",
			"Total Land", "County Taxable (Niagara)", "Town Taxable", "School Taxable", "Village Taxable",
			"Equalization Rate", "Full Market Value", "Maps", "Total", "Total Land", "County Taxable (Niagara)",
			"Town Taxable", "School Taxable", "Village Taxable", "Equalization Rate", "Full Market Value", "Year Built",
			"House Style", "Total SQFT *", "1st Story SQFT *", "2nd Story SQFT *", "1/2 Story SQFT *",
			"3/4 Story SQFT *", "Add'l Story SQFT *", "Finished Attic SQFT *", "Finished Basement SQFT *",
			"Finished Rec Room SQFT *", "Finished Over Garage SQFT *", "Number of Stories", "Overall Condition",
			"Exterior Wall Material", "Bedrooms", "Baths", "Kitchens", "Basement Type", "Central Air", "Heat Type",
			"Fuel Type", "Fireplaces", "Garage(s)", "Garage(s) SQFT", "Type", "Use", "Ownership Code", "Zoning",
			"Road Type", "Water Supply", "Utilities", "School District", "Neighborhood Code", "Sale Date", "Sale Price",
			"Useable Sale", "Arms Length", "Prior Owner Name", "Deed Book", "Deed Page", "Deed Date", "Year Built",
			"House Style", "Total SQFT *", "1st Story SQFT *", "2nd Story SQFT *", "1/2 Story SQFT *",
			"3/4 Story SQFT *", "Add'l Story SQFT *", "Finished Attic SQFT *", "Finished Basement SQFT *",
			"Finished Rec Room SQFT *", "Finished Over Garage SQFT *", "Number of Stories", "Overall Condition",
			"Exterior Wall Material", "Bedrooms", "Baths", "Kitchens", "Basement Type", "Central Air", "Heat Type",
			"Fuel Type", "Fireplaces", "Garage(s)", "Garage(s) SQFT", "Type", "Use", "Ownership Code", "Zoning",
			"Road Type", "Water Supply", "Utilities", "School District", "Neighborhood Code", "Sale Date", "Sale Price",
			"Useable Sale", "Arms Length", "Prior Owner Name", "Deed Book", "Deed Page", "Deed Date", "Type", "Use",
			"Ownership Code", "Zoning", "Road Type", "Water Supply", "Utilities", "School District",
			"Neighborhood Code", "Sale Date", "Sale Price", "Useable Sale", "Arms Length", "Prior Owner Name",
			"Deed Book", "Deed Page", "Deed Date" };
	
	static final String[] selectHeaders = 
		{"address", "listedPrice", "status", "Total",  "Sale Date", "Sale Price", "Use", "Bedrooms", 
			"Baths", "Kitchens","days","googleMap","zillowLink","oarsLink",
			"Total SQFT *", "Arms Length",	"Owner Name", 
			"Heat Type", "Fuel Type", "listedBy" };
	static final String[] selectHeadersTitle = 
		{ "Address", "Listed Price", "Status","Tax Assesment", "Last Sale Date", "Last Sale Price", "Family", "Bedrooms", 
			"Baths", "Kitchens", "Days On Market","Google Map","Zillow Link","Oars Link",
			"Total SQFT *", "Arms Length",	"Owner Name", 
			"Heat Type", "Fuel Type", "Listed By" };

	static Map<String,String> details;

	static int skipStreetCount = 0;
	static int maxStreetRecordCount = 200;
	static boolean addedHeader = false;

	static int recordStreetCount = 0;
	
	static String min = "150000";
	static String max = "550000";

	static String dataFile = "data.txt";
	static final String mainURL = "https://niagarafalls.oarsystem.com/assessment/main.asp?swis=291100&dbg=&opt=&swis=&sbl=&parcel9=&debug=";
	static final String propertyBaseOARSUrl = "https://niagarafalls.oarsystem.com/assessment/r1parc.asp";

//	String urlStreetLookup = "https://niagarafalls.oarsystem.com/assessment/pcllist.asp?swis=291100&sbl=&address1=&address2="
//			+ searchString + "&owner_name=&page=2";

	static final String streetLookupOARSUrl = "https://niagarafalls.oarsystem.com/assessment/pcllist.asp?swis=291100&sbl=&address1=&address2={0}&owner_name=&page={1}";

	public static void main2(String[] args) throws Exception {

		initValues();

		Response res = Jsoup.connect(mainURL).validateTLSCertificates(false).timeout(0).method(Method.GET).execute();

		Document doc = res.parse();

		Elements el = doc.getElementsByTag("option");

		List<String> streets = new ArrayList<String>();

		for (int i = 0; i < el.size(); i++) {
			streets.add(el.get(i).attr("value"));
		}

		for (int i = skipStreetCount; i < streets.size(); i++) {

			lookUpStreet(streets.get(i));
			recordStreetCount++;
			System.out.println("Street Count = " + i + " of " + streets.size());

			if (recordStreetCount > maxStreetRecordCount) {
				// break;
			}
		}

		

	}

	static void initValues() {
		details = new HashMap<String, String>();
		for (String h : selectHeaders) {
			details.put(h,"");
		}

		// Document doc = Jsoup.connect(mainURL).get();
		dataFile = System.getProperty("user.dir").replace("\"", "\\") + "\\" + dataFile;
		System.out.println("dataFile=" + dataFile);
	}

	static void lookUpStreet(String searchString) {

		int page = 1;
		int recordCountOnEachPage = 100;

		while (recordCountOnEachPage > 99) {

			try {
				System.out.println("about to fetch for page " + page);
				String urlStreetPage = java.text.MessageFormat.format(streetLookupOARSUrl, searchString, page);
				page++;

				Response res = Jsoup.connect(urlStreetPage).validateTLSCertificates(false).timeout(0).method(Method.GET).execute();

				Document docListings = res.parse();

				// System.out.println(docListings.html());

				Elements el = docListings.getElementsByAttributeValueContaining("id", "lnk");
				recordCountOnEachPage = el.size();

				List<String> properties = new ArrayList<String>();

				for (int i = 0; i < el.size(); i++) {
					// if(el.get(i).html().contains("1007")) {
					properties.add(el.get(i).attr("onclick").replace("\"", "").trim());
					// }

				}

				for (int i = 0; i < properties.size(); i++) {
					String propUrl = (propertyBaseOARSUrl + properties.get(i).substring(properties.get(i).indexOf("?")))
							.trim();
					Document docProp = Jsoup.connect(propUrl).validateTLSCertificates(false).timeout(0).method(Method.GET).execute().parse();
					// System.out.println(docProp.html());

					extractValues(docProp, details);

				}

			} catch (Exception e) {
				recordCountOnEachPage = 0;// to prevent looping infinitely.
				e.printStackTrace();
			}

		} // end of while

	}

	static Map<String,String> extractValues(Document docProp) {
		return extractValues(docProp,null);
		
	}
	static Map<String,String> extractValues(Document docProp, Map<String,String> details) {
		if(details==null) {
			initValues();
		}

		String address = docProp.getElementsByClass("auto-style1").get(0).html();

		address = address.substring(9, address.indexOf("br") - 1);

		Elements headings = docProp.getElementsByClass("headings");

		if (details.containsKey(address)) {
			details.put("address",address);
		}

		for (int i = 0; i < headings.size(); i++) {
			if (headings.get(i).text().equals("Owner Information")) {
				Element e = (Element) headings.get(i).parentNode().parentNode();
				Elements headers = e.getElementsByTag("tr").get(1).getElementsByTag("td");
				Elements values = e.getElementsByTag("tr").get(2).getElementsByTag("td");

				for (int k = 0; k < values.size(); k++) {
					if (details.containsKey(headers.get(k).text())) {
						details.put(headers.get(k).text(),values.get(k).text());
					}
				}
			}
		}

		Elements tbodyTags = docProp.getElementsByTag("tbody");

		for (int i = 0; i < tbodyTags.size(); i++) {
			Elements trTags = tbodyTags.get(i).getElementsByTag("tr");
			for (int j = 0; j < trTags.size(); j++) {
				Elements tdTags = trTags.get(j).getElementsByTag("td");
				if (tdTags.size() == 2) {
					if (tdTags.get(1).getElementsByTag("input").size() == 0) {
						Element header = tdTags.get(0);
						if (header.getElementsByTag("table").size() == 0) {
							if (details.containsKey(header.text())) {
								details.put(header.text(),tdTags.get(1).html());
							}
						}
					}
				}
			}
		}
		
		return details;
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
