package dennis.JScrapper;

import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;

public class Zillow {
	static String zillowFile = "zillow.html";
	static String mainURL = "https://www.zillow.com/niagara-falls-ny/?searchQueryState=%7B%22pagination%22%3A%7B%7D%2C%22mapBounds%22%3A%7B%22west%22%3A-79.09014701843262%2C%22east%22%3A-78.98440361022949%2C%22south%22%3A43.072367645650196%2C%22north%22%3A43.13014849671711%7D%2C%22mapZoom%22%3A13%2C%22regionSelection%22%3A%5B%7B%22regionId%22%3A46749%2C%22regionType%22%3A6%7D%5D%2C%22isMapVisible%22%3Atrue%2C%22filterState%22%3A%7B%22ah%22%3A%7B%22value%22%3Atrue%7D%2C%22sort%22%3A%7B%22value%22%3A%22globalrelevanceex%22%7D%7D%2C%22isListVisible%22%3Atrue%7D";
	static String a = "https://www.zillow.com/homedetails/1128-Centre-Ave-Niagara-Falls-NY-14305/31438415_zpid/";

	static String addressLookupOARSUrl = "https://niagarafalls.oarsystem.com/assessment/pcllist.asp?swis=291100";
	static final String googleMapsBaseURL = "https://www.google.com/maps/place/";
	static int recordCount = 0;
	static WebDriver  driver = null;



	public static void main(String[] args) {
		try {
			Niagara.initValues();
			StringBuffer sb = addHeader();
		 Map<String, Element> uniqueListings = getListingsFromFile();
			for (Map.Entry<String, Element> entry : uniqueListings.entrySet()) {
				sb.append(getListingDetail(entry.getValue()) + "\r\n");

				//clear all hashmap vales.
				for (String h : Niagara.selectHeaders) {
					Niagara.details.put(h,"");
				}
			}
			
			Niagara.writeToFile(sb.toString());
			System.out.println("Complete. recordCount=" + recordCount);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static Map<String, Element> getListingsFromFile() throws IOException {
		zillowFile = System.getProperty("user.dir").replace("\"", "\\") + "\\" + zillowFile;
		String oldlistings= System.getProperty("user.dir").replace("\"", "\\") + "\\oldhtmlfiles";
		
		File input = new File(zillowFile);
		Document doc = Jsoup.parse(input, null);

		Elements listings = doc.getElementsByTag("article");
		Map<String, Element> uniqueListings = pickUniqueListings(listings, new HashMap<String, Element>());
		
		File directoryPath = new File(oldlistings);
		File filesList[] = directoryPath.listFiles();
		for (File file : filesList) {
			Document docOld = Jsoup.parse(file, null);
			Elements listingsOld = docOld.getElementsByTag("article");
			Map<String, Element> uniqueListingsOld = pickUniqueListings(listingsOld, new HashMap<String, Element>());
			for (Map.Entry<String, Element> set :uniqueListingsOld.entrySet()) {
	        	if(uniqueListings.containsKey(set.getKey())) {
	        		uniqueListings.remove(set.getKey());
				}
	        }
		}

		return uniqueListings;
	}
	
	

	static Map<String, Element> pickUniqueListings(Elements listings, Map<String, Element> uniqueListings) {

		for (int i = 0; i < listings.size(); i++) {
			if (listings.get(i).getElementsByClass("list-card-addr").size() > 0) {
				String fullAddress = listings.get(i).getElementsByClass("list-card-addr").get(0).text();
				if (!uniqueListings.containsKey(fullAddress)) {
					uniqueListings.put(fullAddress, listings.get(i));
				}
			}
		}
		return uniqueListings;
	}

	static Map<String, Element> getListingsFromUrls(String[] urls) throws IOException {
		Map<String, Element> uniqueListings = new HashMap<String, Element>();
		for (int i = 0; i < urls.length; i++) {
			Response response = Jsoup.connect(urls[i]).validateTLSCertificates(false).userAgent("Mozilla/5.0")
					.timeout(0).method(Method.GET).execute();
			Document doc = response.parse();
			Elements listings = doc.getElementsByTag("article");
			uniqueListings = pickUniqueListings(listings, uniqueListings);
		}
		return uniqueListings;
	}

	static String getListingDetail(Element listing) throws UnsupportedEncodingException {

		StringBuffer sb = new StringBuffer();
		Element info = listing.getElementsByClass("list-card-info").get(0);
		
		if (info.getElementsByClass("list-card-extra-info").size() > 0) {
			Element listedBy = listing.getElementsByClass("list-card-extra-info").get(0);
			Niagara.details.put("listedBy", listedBy.text());
		}
		

		String days = listing.getElementsByClass("list-card-variable-text list-card-img-overlay").get(0).text();
		if (days.contains("hours")) {
			Niagara.details.put("days", "0");
		} else {
			Niagara.details.put("days", days.substring(0, days.indexOf(" ")));
		}
		if (info.getElementsByClass("list-card-addr").size() > 0) {
			String fullAddress = info.getElementsByClass("list-card-addr").get(0).text();
			Niagara.details.put("address", fullAddress);
			String listedPrice = info.getElementsByClass("list-card-price").get(0).text();
			Niagara.details.put("listedPrice", listedPrice);

			// String label = listing.getElementsByClass("list-card-label").get(0).text();
			String status = info.getElementsByClass("list-card-statusText").get(0).text();
			Niagara.details.put("status", status);

			Niagara.details.put("googleMap",
					googleMapsBaseURL + URLEncoder.encode(fullAddress, StandardCharsets.UTF_8.toString()));
			String zillowLink = info.getElementsByTag("a").get(0).attr("href");
			Niagara.details.put("zillowLink", zillowLink);

			String address = fullAddress.split(",")[0];
			String searchString = address.substring(0, address.lastIndexOf(" "));
			System.out.println(searchString);
			if (searchString.contains("Centre")) {
				searchString = searchString.replace("Centre", "Center");
			}
			Map<String, String> details = lookUpSinglePropertyOnOARSByAddress(searchString, Niagara.details);
			for (int j = 0; j < Niagara.selectHeaders.length; j++) {
				sb.append(details.get(Niagara.selectHeaders[j]) + "\t");
			}

			recordCount++;
			System.out.println("RecordCount = " + recordCount);
		} else {
			System.out.println("");
		}
		return sb.toString();
	}

	static StringBuffer addHeader() {
		StringBuffer sb = new StringBuffer();
		for (int j = 0; j < Niagara.selectHeaders.length; j++) {
			sb.append(Niagara.selectHeadersTitle[j] + "\t");
		}
		sb.append("\n");
		return sb;
	}

	static Map<String, String> lookUpSinglePropertyOnOARSByAddress(String searchString, Map<String, String> details) {

		try {
			Response response = Jsoup.connect(addressLookupOARSUrl).validateTLSCertificates(false)
					.userAgent("Mozilla/5.0").timeout(0).method(Method.POST).data("address1", searchString).execute();

			Document doc = response.parse();
			Elements el = doc.getElementsByAttributeValueContaining("id", "lnk");
			List<String> properties = new ArrayList<String>();
			for (int i = 0; i < el.size(); i++) {
				properties.add(el.get(i).attr("onclick").replace("\"", "").trim());
				break;
			}

			for (int i = 0; i < properties.size(); i++) {
				String propUrl = (Niagara.propertyBaseOARSUrl
						+ properties.get(i).substring(properties.get(i).indexOf("?"))).trim();
				Document docProp = Jsoup.connect(propUrl).timeout(0).validateTLSCertificates(false).method(Method.GET)
						.execute().parse();

				details.put("oarsLink", propUrl);
				// System.out.println(docProp.html());
				details = Niagara.extractValues(docProp, details);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return details;

	}

}
