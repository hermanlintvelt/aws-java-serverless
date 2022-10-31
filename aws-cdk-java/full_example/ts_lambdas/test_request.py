import requests
import json

#CoreServicesDEVStack.Outputs3uploaddeletefilehandler = Endpoint URL: https://qkuuz272bh.execute-api.eu-west-1.amazonaws.com/prod/attachments/deleteS3File
#CoreServicesDEVStack.Outputs3uploaddownloadurlhandler = Endpoint URL: https://qkuuz272bh.execute-api.eu-west-1.amazonaws.com/prod/attachments/requestDownloadURL
#CoreServicesDEVStack.Outputs3uploadpresignedurlhandler = Endpoint URL: https://qkuuz272bh.execute-api.eu-west-1.amazonaws.com/prod/attachments/requestPresignedURL

# url = requests.post('https://oocuu0ssi3.execute-api.us-east-1.amazonaws.com/dev/requestDownloadURL', json={"storageIdentifier": "bfd6229a-b25b-4830-8793-591e0eba3ae8.MOV"})
# print(url.text)

url = requests.post('https://qkuuz272bh.execute-api.eu-west-1.amazonaws.com/prod/attachments/requestDownloadURL', json={"storageIdentifier": "bfd6229a-b25b-4830-8793-591e0eba3ae8.MOV"})
print(url.text)