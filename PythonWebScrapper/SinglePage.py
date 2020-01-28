#C:\dennis\tools\python-3.7.2.post1-embed-amd64\python.exe a.py
# C:\dennis\tools\python-3.7.2.post1-embed-amd64\python.exe -m pip install pip install requests

# fruits = ["apple", "banana", "cherry"]
# for x in fruits:
  # print(x)

import urllib.request

contents = urllib.request.urlopen( "https://stackoverflow.com/").read()
print (contents)