package dennis.JScrapper;

import org.jsoup.Jsoup;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Zillow {
	static String zillowFile = "zillow.html";
	static String mainURL = "https://www.zillow.com/niagara-falls-ny/?searchQueryState=%7B%22pagination%22%3A%7B%7D%2C%22mapBounds%22%3A%7B%22west%22%3A-79.09014701843262%2C%22east%22%3A-78.98440361022949%2C%22south%22%3A43.072367645650196%2C%22north%22%3A43.13014849671711%7D%2C%22mapZoom%22%3A13%2C%22regionSelection%22%3A%5B%7B%22regionId%22%3A46749%2C%22regionType%22%3A6%7D%5D%2C%22isMapVisible%22%3Atrue%2C%22filterState%22%3A%7B%22ah%22%3A%7B%22value%22%3Atrue%7D%2C%22sort%22%3A%7B%22value%22%3A%22globalrelevanceex%22%7D%7D%2C%22isListVisible%22%3Atrue%7D";
	static String a = "https://www.zillow.com/homedetails/1128-Centre-Ave-Niagara-Falls-NY-14305/31438415_zpid/";

	static String addressLookupURL = "https://niagarafalls.oarsystem.com/assessment/pcllist.asp?swis=291100";
	static final String googleMapsBaseURL = "https://www.google.com/maps/place/";
	

	public static void main(String[] args) {

		try {
			Niagara.initValues();
			
	
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < Niagara.selectHeaders.length; j++) {
				sb.append(Niagara.selectHeadersTitle[j]+"\t");
			}
			sb.append("\n");
			
			
			zillowFile = System.getProperty("user.dir").replace("\"", "\\") + "\\" + zillowFile;

			File input = new File(zillowFile);
			Document doc = Jsoup.parse(input, null);

			Elements listings = doc.getElementsByTag("article");

			for (int i = 0; i < listings.size(); i++) {
				Element listing = listings.get(i).getElementsByClass("list-card-info").get(0);
				if (listing.getElementsByClass("list-card-addr").size() > 0) {
					String fullAddress = listing.getElementsByClass("list-card-addr").get(0).text();
					Niagara.details.put("address", fullAddress);
					String listedPrice = listing.getElementsByClass("list-card-price").get(0).text();				
					Niagara.details.put("listedPrice", listedPrice);
					
					String label = listing.getElementsByClass("list-card-label").get(0).text();				
					String status = listing.getElementsByClass("list-card-statusText").get(0).text();				
					Niagara.details.put("status", label + " " + status);
					
					
					
					Niagara.details.put("googleMap", googleMapsBaseURL + URLEncoder.encode(fullAddress, StandardCharsets.UTF_8.toString()));
					String zillowLink = listing.getElementsByTag("a").get(0).attr("href");
					Niagara.details.put("zillowLink", zillowLink);
					String address = fullAddress.split(",")[0];
					String searchString = address.substring(0, address.lastIndexOf(" "));
					System.out.println(searchString);
					Map<String,String> details=lookUpSinglePropertyByaddress(searchString, Niagara.details);
					for (int j = 0; j < Niagara.selectHeaders.length; j++) {
						sb.append(details.get(Niagara.selectHeaders[j])+"\t");
					}
					
					Niagara.writeToFile(sb.toString());
					sb = new StringBuffer();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	static Map<String,String>  lookUpSinglePropertyByaddress(String searchString, Map<String,String> details) {

		try {
			Response response = Jsoup.connect(addressLookupURL).userAgent("Mozilla/5.0").timeout(10 * 1000)
					.method(Method.POST).data("address1", searchString).execute();

			Document doc = response.parse();
			Elements el = doc.getElementsByAttributeValueContaining("id", "lnk");
			List<String> properties = new ArrayList<String>();
			for (int i = 0; i < el.size(); i++) {
				properties.add(el.get(i).attr("onclick").replace("\"", "").trim());
			}

			for (int i = 0; i < properties.size(); i++) {
				String propUrl = (Niagara.propertyBaseUrl + properties.get(i).substring(properties.get(i).indexOf("?")))
						.trim();
				Document docProp = Jsoup.connect(propUrl).timeout(0).method(Method.GET).execute().parse();
				// System.out.println(docProp.html());
				 details=Niagara.extractValues(docProp,details);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return details;

	}

}
