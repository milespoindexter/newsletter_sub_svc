Project: CN Newsletter Subscription Service

2014-05-20
Miles Poindexter
selfpropelledcity@gmail.com

mvn exec:java -Dexec.mainClass="com.cn.dsa.toolkit.ToolkitClient"


Service URL:
http://localhost:8040/services/newsletter

Service will accept either XML or JSON formatted requests.
Specify the format of your request using the "Content-Type" HTTP header:
examples:
Content-Type: application/json or Content-Type: application/xml

Response format can be controlled by sending an "Accept" HTTP header:
Default response format is xml. Response will be in JSON if your Content-Type header is json.  Accept header is not required.
examples:
Accept: application/xml or Accept: application/json

SUBSCRIBE/UNSUBSCRIBE SERVICE:
URL: http://localhost:8040/services/newsletter/update

To get a sample of the xml POST body for new subscriptions:
http://localhost:8040/services/newsletter/update/test

To get a sample of the JSON format, hit the same test url with your Accept header set to application/json

Only 1 email is allowed.
Multiple newsletter IDs can be added.
Each newsletter should have an action key set to either "subscribe" or "unsubscribe".
The default action is "subscribe"

EVENT CODES:
N01= newsletter subscribe
N02= newsletter unsubscribe

