from html.parser import HTMLParser
import urllib.request

url_list = []

class CondoListParser(HTMLParser):
	
	flag_n="n"

	def handle_starttag(self, tag, attrs):
		if tag.lower() == 'a':
			for attr in attrs:
					
					if attr[0]=='class'and attr[1]=="no-decro":
						self.flag_n="y"
					if self.flag_n=="y" and attr[0]=='href':
						url_list.append(attr[1])
						self.flag_n="n"



			
class CondoPageParser(HTMLParser):
	
	flag_gly="n"	
	flag_span="n"
	flag_tax="n"
	flag_taxvalue="n"

	def handle_starttag(self, tag, attrs):
		if tag.lower() == 'i':
			for attr in attrs:
					if attr[0]=='class':
						if(attr[1])==" glyphicon glyphicon-tag":
							self.flag_gly="y"
		if(tag.lower() == 'span' and self.flag_gly=="y"):
			self.flag_span="y"
		if(tag.lower() == 'div' and self.flag_tax=="y"):
			self.flag_taxvalue="y"
					

	# def handle_endtag(self, tag):
		# print("Encountered an end tag :", tag)

	def handle_data(self, data):
		if(self.flag_gly=="y"  and self.flag_span=="y"):
			print("Asking price: " + data)
			self.flag_gly="n"	
			self.flag_span="n"
		if data == 'Taxes':
			self.flag_tax="y"
		if(self.flag_taxvalue=="y"):
			print("taxes: " + data)
			self.flag_tax="n"
			self.flag_taxvalue="n"
			

main_page="https://condos.ca/search?for=sale&search_by=Neighbourhood&buy_min=185000&buy_max=515000&unit_area_min=0&unit_area_max=99999999&beds_min=1&area_ids=590&view=0&user_search=1&sort=days_on_market"
contents = urllib.request.urlopen(main_page).read()
parser = CondoListParser()
parser.feed(contents.decode('utf-8'))


parser = CondoListParser()

for url in url_list:
	contents = urllib.request.urlopen( "https://condos.ca/" + url).read()
	parser = CondoPageParser()
	parser.feed(contents.decode('utf-8'))