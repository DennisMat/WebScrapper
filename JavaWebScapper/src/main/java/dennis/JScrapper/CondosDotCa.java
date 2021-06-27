package dennis.JScrapper;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CondosDotCa {

    public static void main(String[] args) throws Exception {
    	String min="150000";
    	String max="550000";
        String mainURL="https://condos.ca/search?for=sale&search_by=Neighbourhood&buy_min="+min+"&buy_max="+max+"&unit_area_min=0&unit_area_max=99999999&beds_min=0&area_ids=590&view=0&user_search=1&sort=days_on_market";
        Document doc = Jsoup.connect(mainURL).get();
        Elements el=doc.getElementsByClass("no-decro");

        List<String> urls= new ArrayList<String>();
        for(int i=0;i<el.size();i++){

            urls.add(("https://condos.ca/" + el.get(i).attr("href")));
        }

        for(int i=0;i<urls.size();i++){
            parseCondoPage(urls.get(i));
        }


    }

    static void parseCondoPage(String condoUrl ) throws IOException{

      
        Document doc = Jsoup.connect(condoUrl).get();
        
        int asking=0;
        int taxes=0;
        int maint=0;
        int yearlyRent=0;
        int daysMarket=0;
        
        String daysOnMarketSelector="div.post-details-list:nth-child(1) > div:nth-child(2) > div:nth-child(2)";
        String rentCssSelector="#value > div:nth-child(3) > div > div > div:nth-child(1) > div > div:nth-child(2) > div.rental_avg_border.col-xs-6 > span.price_number";
        Elements el=doc.getElementsByClass(" glyphicon glyphicon-tag");

        if(el.size()>0){
            Element p=el.get(0).parent();
            asking=getInt(p.getElementsByTag("span").get(0).html());
        }
        
        Elements details=doc.getElementsByClass("col-xs-4");
        
        for(int i=0;i<details.size();i++){
            String section=details.get(i).child(0).html();
            //System.out.println(section);
            if(section.equalsIgnoreCase("Maint Fees")){
                maint=getInt(details.get(i).child(1).html()) *12;
            }
            if(section.equalsIgnoreCase("Taxes")){
                taxes=getInt(details.get(i).child(1).html());
            }
        }
        String rentStr="0";
        String daysMStr="0";
        try {
            rentStr = doc.select(rentCssSelector).get(0).html();
            daysMStr = doc.select(daysOnMarketSelector).get(0).html();
        } catch (Exception e) {}
        
        daysMarket=getInt(daysMStr);
        yearlyRent=12*getInt(rentStr);
        if ( taxes>0){
            System.out.println("-------------------------");
            System.out.println(condoUrl);
            System.out.println("asking: " + asking);
            System.out.println("Days in the Market: " + daysMarket);
            System.out.println("Yearly Rent: " + yearlyRent);
            System.out.println("ROI on full amount: "   + (yearlyRent-(taxes+maint))*1000/asking +"/10 (yearlyRent-(taxes+maint))/asking");        
        }
        
        
    }
    
    static int getInt(String raw){
        int parsedOutput=0;
        try {
            String numStr=raw.replace("$", "").replace(",", "");        
            parsedOutput= Integer.parseInt(numStr);
        } catch (NumberFormatException e) {
        }
        return parsedOutput;
    }



}
