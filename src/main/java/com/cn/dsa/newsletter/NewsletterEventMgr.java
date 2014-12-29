package com.cn.dsa.newsletter;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.GregorianCalendar;
import java.util.Date;
import java.util.ArrayList;

import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.ByteArrayInputStream;

import java.sql.Timestamp;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import javax.xml.parsers.*;
import javax.xml.xpath.*;

import com.cn.dsa.silverpop.common.*;
import com.cn.dsa.silverpop.db.*;

import org.osgi.framework.*;
import org.apache.velocity.VelocityContext;

import com.mongodb.MongoClient;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.BasicDBObject;

import com.cn.dsa.common.ServiceClient;
import com.cn.dsa.db.MongoMgr;
import com.cn.dsa.silverpop.svc.OrgService;
import com.cn.dsa.common.VelocityMgr;
import com.cn.dsa.toolkit.ToolkitClient;

public class NewsletterEventMgr {

    public static String NL = System.getProperty("line.separator");
    private static String MONGO_DB = "newsletters";
    private static String MONGO_COLLECTION = "newsletter_events";
    private static final String ADD_RECIPIENT = "addRecipient.vm";
    private static final String ADD_TO_PROGRAM = "addContactToProgram.vm";
    
    private static Logger logMgr = Logger.getLogger(NewsletterEventMgr.class.getName());

    private static VelocityMgr velocityMgr = new VelocityMgr("silverpop");
    
    private SessionMgr sessionMgr;
    private ToolkitClient toolkitClient;

    private String sessionID = "";

    //private static Properties props = null;
    //private static boolean propsLoaded = false;

    public static String queryMarker = "?"; //use this when hitting ESB Relay Service
    //public static String queryMarker = ";"; //use this when hitting SP API directly
    
    //private OrgMgr orgMgr = OrgMgr.getInstance();
    private OrgService orgSvc;
    private ServiceClient svcClient;

    //mongoDB objects
    private MongoMgr mongoMgr = MongoMgr.getInstance();
    private MongoClient mongoClient;
    private DB mongoSpDb;
    private DBCollection newsltrCollection;

    private String faultStr = "";

    public void setFaultString(String fs) {
        faultStr = fs;
    }
    public String getFaultString() {
        return faultStr;
    }
    
    //constructor
    public NewsletterEventMgr() {
        super();
        //if(!propsLoaded) {
        //    loadProperties();
        //}
        svcClient = new ServiceClient();
        toolkitClient = new ToolkitClient();
        orgSvc = getOrgService();
        loadMongoCollection();
        
    }

    private void loadMongoCollection() {
        mongoClient = mongoMgr.getClient();
        mongoSpDb = mongoClient.getDB(MONGO_DB);
        newsltrCollection = mongoSpDb.getCollection(MONGO_COLLECTION);
    }

    private boolean saveSubscriptionToDb(NewsletterEventResponse newsltr) {
        //create list of BasicDBObjects
        List<BasicDBObject> newsltrList = new ArrayList<BasicDBObject>();
        List<NewsletterItem> nList = newsltr.getNewsletters();
        for(NewsletterItem n : nList) {
            BasicDBObject nltr = new BasicDBObject("id", n.getId())
            .append("action", n.getAction())
            .append("eventCode", n.getEventCode())
            .append("sendEmail", n.isSendEmail())
            .append("status", n.getStatus())
            .append("success", n.getSuccess())
            .append("subscriberId", n.getSubscriberId())
            .append("responseMsg", n.getResponseMsg());
            //add Org code if there
            Org org = n.getOrg();
            if(org != null) {
                nltr.append("orgCode",org.getOrgCode());
            }
            newsltrList.add(nltr);
        }

        //create JSON document from msg
        BasicDBObject newsltrEventDoc = new BasicDBObject("email", newsltr.getEmail())
        .append("newsletters", newsltrList)
        .append("timestamp", newsltr.getTimestamp())
        .append("success", newsltr.getSuccess())
        .append("responseMsg", newsltr.getResponseMsg());
        String name = newsltr.getName();
        if(name != null && name.length() > 0) {
            newsltrEventDoc.append("name", name);
        }
        //add date object
        newsltrEventDoc.append("added", new Date());
        
        if(newsltrCollection == null) {
            loadMongoCollection();
        }
        newsltrCollection.insert(newsltrEventDoc);
        
        return true;
    }


    private OrgService getOrgService() {
        BundleContext ctx = FrameworkUtil.getBundle(NewsletterEventMgr.class).getBundleContext();
        //logMgr.info("Looking for OrgService service in BundleContext . . .");
        
        ServiceReference ref = ctx.getServiceReference("com.cn.dsa.silverpop.svc.OrgService");
        if (ref != null) {
            //logMgr.info("OrgService service found.");
            OrgService orgSvc = (OrgService)ctx.getService(ref);
            return orgSvc;
        }
        
        return null;

    }


    private Org getOrg(long newsltrId) {
        Org org = null;
        //Org org = orgMgr.getOrgForNewsletterId(newsltrId);
        OrgService orgSvc = getOrgService();
        try {
            org = orgSvc.getOrgForNewsletter(newsltrId);
            //return org;
            if(org != null && org.getOrgCode() != null) {
                //logMgr.info( "Org found for newsltr #"+
                //                newsltrId+" by svc: "+org.getOrgCode());
            }
            else {
                //logMgr.info( "NO Org found for newsltr #"+newsltrId);
                org = null;
            }
            
        }
        catch(Exception orgEx) {
            logMgr.severe( "Exception looking for Org: "+
                                newsltrId+", "+orgEx.getMessage());
        }

        return org;

    }

    private void modifyDefaultsForToolkit(NewsletterItem newsltr, boolean subscribe) {
        //action
        String action = newsltr.getAction();
        if(action == null || action.equalsIgnoreCase(NewsletterItem.SUBSCRIBE) || action.equalsIgnoreCase(NewsletterItem.UNSUBSCRIBE) ) {
            if(subscribe) {
                newsltr.setAction(NewsletterItem.TK_SUBSCRIBE);
            }
            else {
                newsltr.setAction(NewsletterItem.TK_UNSUBSCRIBE);
            }
        }
        //event code
        String eventCode = newsltr.getEventCode();
        if(eventCode == null || eventCode.equalsIgnoreCase(NewsletterItem.SUB_CODE) || eventCode.equalsIgnoreCase(NewsletterItem.UNSUB_CODE) ) {
            if(subscribe) {
                newsltr.setEventCode(NewsletterItem.TK_SUB_CODE);
            }
            else {
                newsltr.setEventCode(NewsletterItem.TK_UNSUB_CODE);
            }
        }
        //send email
        newsltr.setSendEmail( new Boolean(false) );
    
    }


    //TODO: This method should be merged into the update method, and removed.
    public NewsletterEventResponse toolkitUpdate(NewsletterEvent newsltrEvt) {
        //create the response object
        NewsletterEventResponse response = new NewsletterEventResponse();
        
        //set the request values
        response.setEmail(newsltrEvt.getEmail());
        //add timestamp if not there
        String ts = newsltrEvt.getTimestamp();
        if(ts == null) {
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());
            newsltrEvt.setTimestamp(timestamp.toString());
        }
        

        response.setTimestamp(newsltrEvt.getTimestamp());
        logMgr.info("newsletter toolkit event: email: "+newsltrEvt.getEmail());

        response.setSuccess(true);
          
        //loop through each newsletter and process
        List<NewsletterItem> newsltrList = newsltrEvt.getNewsletters();
        String email = newsltrEvt.getEmail();

        for(NewsletterItem newsltr: newsltrList) {
            long newsltrId = newsltr.getId();
            String action = newsltr.getAction();
            String responseMsg = "";

            if(action != null && action.equals(NewsletterItem.TK_SUBSCRIBE)) {
                responseMsg = subscribeViaToolkit(email, newsltr);
            }
            else if(action.equals(NewsletterItem.TK_UNSUBSCRIBE)) {
                responseMsg = unsubscribeViaToolkit(email, newsltr);
            }

            //append msgs to overall event object
            if(response.getResponseMsg() != null && response.getResponseMsg().length() > 0) {
                response.setResponseMsg( response.getResponseMsg() +" | "+responseMsg);
            }
            else {
                response.setResponseMsg(responseMsg);
            }
            
            //if any sub fails, set overall success bool to false
            if(!newsltr.getSuccess()) {
                response.setSuccess(false);
            }
            
        }

        response.setNewsletters(newsltrList);
        saveSubscriptionToDb(response);

        return response;
    }



    public NewsletterEventResponse update(NewsletterEvent newsltrEvt) {
        
        //create the response object
        NewsletterEventResponse response = new NewsletterEventResponse();
        
        //set the request values
        response.setEmail(newsltrEvt.getEmail());

        //add timestamp if not there
        String ts = newsltrEvt.getTimestamp();
        if(ts == null) {
            Date date = new Date();
            Timestamp timestamp = new Timestamp(date.getTime());
            newsltrEvt.setTimestamp(timestamp.toString());
        }

        response.setTimestamp(newsltrEvt.getTimestamp());
        response.setName(newsltrEvt.getName());
        logMgr.info("newsletter event: email: "+newsltrEvt.getEmail());

        response.setSuccess(true);
          
        //loop through each newsletter and process
        List<NewsletterItem> newsltrList = newsltrEvt.getNewsletters();
        String email = newsltrEvt.getEmail();

        for(NewsletterItem newsltr: newsltrList) {
            long newsltrId = newsltr.getId();
            //get the Org data for this NewsletterEvent
            Org org = getOrg(newsltrId);
            String action = newsltr.getAction();

            String responseMsg = "";

            if(org == null) {
                responseMsg = "No SilverPop Org found for Newsletter ID: "+newsltrId;
                //logMgr.info(responseMsg);
                newsltr.setSuccess(false);
            }
            else if(action.equals(NewsletterItem.SUBSCRIBE)) {
                //If org was found, add the org to newsletter for later access
                newsltr.setOrg(org);
                responseMsg = subscribe(email, newsltr, org);
                
            }
            else if(action.equals(NewsletterItem.UNSUBSCRIBE)) {
                newsltr.setOrg(org);
                responseMsg = unsubscribe(email, newsltr, org);
            }
            else if(action.equals(NewsletterItem.TK_SUBSCRIBE)) {
                responseMsg = subscribeViaToolkit(email, newsltr);
            }
            else if(action.equals(NewsletterItem.TK_UNSUBSCRIBE)) {
                responseMsg = unsubscribeViaToolkit(email, newsltr);
            }


            //append msgs to overall event object
            if(response.getResponseMsg() != null && response.getResponseMsg().length() > 0) {
                response.setResponseMsg( response.getResponseMsg() +" | "+responseMsg);
            }
            else {
                response.setResponseMsg(responseMsg);
            }
            
            //if any sub fails, set overall success bool to false
            if(!newsltr.getSuccess()) {
                response.setSuccess(false);
            }
            
        }

        response.setNewsletters(newsltrList);
        saveSubscriptionToDb(response);

        return response;
    }

    /**
     * unsubscribe service not implemented yet . . .
    */
    public String unsubscribe(String email, NewsletterItem newsltr, Org org) {
        String responseMsg = "Unsubscribe service not implemented yet.";
        newsltr.setSuccess(true); //set to true for now
        newsltr.setResponseMsg(responseMsg);

        return responseMsg;
    }


    //returns msg with recipient ID if successful, error msg if not
    private String subscribe(String email, NewsletterItem newsltr, Org org) {
        String responseMsg = "";
        long newsltrId = newsltr.getId();

        try {
            //Get welcome program from Org
            Program welcomeProg = null;
            List<Program> programs = org.getPrograms();
            for(Program p : programs) {
                logMgr.info("check: "+p.getNewsletterId()+" = "+newsltrId);
                if(p.getType().equals(Program.WELCOME_EMAIL) && p.getNewsletterId() == newsltrId ) {
                //if(p.getType().equals(Program.WELCOME_EMAIL)) {
                    welcomeProg = p;
                }
            }

            //check status of the welcome email program
            String welcomeEmailStatus = welcomeProg.getWelcomeEmailStatus();

            if( welcomeEmailStatus.equals(Program.WE_STATUS_LIVE) ||
                (welcomeEmailStatus.equals(Program.WE_STATUS_TEST) && email.endsWith("condenast.com")) ) {
                //continue to SilverPop
                SessionMgr sessionMgr = new SessionMgr(org);
                
                //login
                String apiUrl = generateSessionUrl(org, sessionMgr);
                
                //trigger Newsletter welcome Program at SilverPop
                responseMsg = engageSilverPopApi(apiUrl,email,newsltr,welcomeProg);

                //logout
                String logout = sessionMgr.createLogoutBody();
                //logMgr.info("logging out of session: "+apiUrl);
                String response = svcClient.doXmlPostRequest(apiUrl, logout, null);

            }
            else {
                if(welcomeEmailStatus.equals(Program.WE_STATUS_TEST)) {
                   responseMsg = "newsletter subscriber ignored: "+email+
                    ". Only condenast.com emails will be processed during test phase.";
                }
                else {
                    responseMsg = "newsletter subscriber ignored: "+email+
                    ". Newsletter welcome email program is currently disabled.";
                
                }
                //logMgr.info(responseMsg);
                newsltr.setSuccess(false);
            }
        }
        catch(Exception e) {
            logMgr.severe("bombed adding SP recipient: "+e);
            responseMsg = "Exception adding newsletter subscriber: "+e.getMessage();
        }
        
        newsltr.setResponseMsg(responseMsg);
        
        return responseMsg;
        
    }

    //returns toolkit response
    private String subscribeViaToolkit(String email, NewsletterItem newsltr) {
        String responseMsg = "";
        modifyDefaultsForToolkit(newsltr, true);
        
        try {
            responseMsg = toolkitClient.updateSubscription(email, newsltr, true);
            newsltr.setSuccess(true);
        }
        catch(Exception e) {
            logMgr.severe("bombed sending subscribe event to Toolkit API: "+e);
            responseMsg = "Exception sending subscribe event to Toolkit API: "+e.getMessage();
            newsltr.setSuccess(false);
        }
        
        newsltr.setResponseMsg(responseMsg);
        return responseMsg;
        
    }

    //returns toolkit response
    private String unsubscribeViaToolkit(String email, NewsletterItem newsltr) {
        String responseMsg = "";
        modifyDefaultsForToolkit(newsltr, false);
        
        try {
            responseMsg = toolkitClient.updateSubscription(email, newsltr, false);
            newsltr.setSuccess(true);
        }
        catch(Exception e) {
            logMgr.severe("bombed sending unsub event to Toolkit API: "+e);
            responseMsg = "Exception sending unsub event to Toolkit API: "+e.getMessage();
            newsltr.setSuccess(false);
        }
        
        newsltr.setResponseMsg(responseMsg);
        return responseMsg;
        
    }

    private String generateSessionUrl(Org org, SessionMgr sessionMgr) {
        String apiUrl = "";
        String sessionID = sessionMgr.getSessionID();

        if(sessionID != null && sessionID.length() > 0) {
            //logMgr.info("login successful");
            //logMgr.info("session ID: " +sessionID);
            
            //set the URL for AddRecipient request
            apiUrl = org.getApiRelay()+queryMarker+"jsessionid="+sessionID;
        }

        return apiUrl;       
    }


    private String engageSilverPopApi(String apiUrl, String email, NewsletterItem newsltr, Program welcomeProg) throws Exception {
        String responseMsg = "";
        
        //generate request
        String request = createAddRecipientMsgBody(newsltr, email, welcomeProg);

        //logMgr.info("SP add recipient request: "+request);
        String response = svcClient.doXmlPostRequest(apiUrl, request, null);
        boolean success = false;

        String recipientId = parseAddRecipientResponse(response);
        if(!newsltr.isSendEmail()) {
            if(recipientId != null) {
                success = true;
                responseMsg =   "Subscriber ID: "+recipientId+
                                " added to Newsletter ID: "+newsltr.getId()+
                                ". Welcome Email was not sent.";
            }
            else {
                success = false;
                //get Fault String
                responseMsg = "Failed to add subscriber to Newsletter ID: "+
                              newsltr.getId()+". FAULT: "+getFaultString();
            }
        }
        else {
            if(recipientId != null) {
                //now call the program
                String programRequest = createAddContactToProgramMsgBody(recipientId, welcomeProg.getProgramId());
                response = svcClient.doXmlPostRequest(apiUrl, programRequest, null);
                boolean welcomeSent = parseAddContactResponse(response);
                if(welcomeSent) {
                    success = true;
                    responseMsg =   "Subscriber ID: "+recipientId+
                                    " added to Newsletter ID: "+newsltr.getId()+
                                    ". Welcome Email sent.";
                }
                else {
                    success = false;
                    responseMsg =   "Subscriber ID: "+recipientId+
                                    " did not get welcome email for Newsletter: "+
                                    newsltr.getId()+". FAULT: "+
                                    getFaultString();

                }
                
            }
            else {
                success = false;
                //get Fault String
                responseMsg = "Failed to add subscriber to Newsletter ID: "+
                        newsltr.getId()+". FAULT: "+getFaultString();
            
            }
        }
        
        if(recipientId != null) {
            newsltr.setSubscriberId(recipientId);
        }
        newsltr.setSuccess(success);

        return responseMsg;
    }





    /**
     * uses apache velocity to create xml request body
     */
    private String createAddRecipientMsgBody(NewsletterItem newsltr, String email, Program prog) {
        VelocityContext context = new VelocityContext();
        
        context.put("listId", (prog.getListId() > 0 ? Long.toString(prog.getListId()) : ""));
        context.put("email", (email != null ? email : ""));
        context.put("newsletterId", (newsltr.getId() > 0 ? Long.toString(newsltr.getId()) : ""));
        context.put("status", (newsltr.getStatus() != null ? newsltr.getStatus() : ""));
        //context.put("timestamp", (newsltrEvt.getTimestamp() != null ? newsltrEvt.getTimestamp() : ""));
          
        //render template
        return velocityMgr.merge(ADD_RECIPIENT,context);
    }

   /**
     * uses apache velocity to create xml request body
     */
    private String createAddContactToProgramMsgBody(String contactId, long programId) {
        VelocityContext context = new VelocityContext();
        
        context.put("programId", programId);
        context.put("contactId", contactId);
        return velocityMgr.merge(ADD_TO_PROGRAM,context);

    }

    //if this returns string, there is a problem. Otherwise it returns null
    private String findFault(Document doc) {
        String faultStr = null;
        //logMgr.info("Looking for Fault . . .");
        try { 
            //check for problems
            XPath successpath = XPathFactory.newInstance().newXPath();
            XPathExpression xs = successpath.compile("/Envelope/Body/RESULT/SUCCESS/text()");
            Object successObj = xs.evaluate(doc, XPathConstants.STRING);
            if(successObj != null) {
                String successVal = (String)successObj;
                if(successVal.equalsIgnoreCase("false")) {
                    
                    //get fault info
                    XPath fspath = XPathFactory.newInstance().newXPath();
                    XPathExpression fs = fspath.compile("/Envelope/Body/Fault/FaultString/text()");
                    Object fsObj = fs.evaluate(doc, XPathConstants.STRING);
                    if(fsObj != null) {
                        faultStr = (String)fsObj;
                        //logMgr.info("faultString: "+faultStr);
                        setFaultString(faultStr);
                    }
                    
                    //just return empty array . . .
                    logMgr.severe("Fault string: "+getFaultString());
                }
                else { 
                    //success is true, so return null
                }
            }

        }
        catch(Exception e) {
            logMgr.severe("trouble parsing response: "+NL+e.getMessage());
        }

        return faultStr;

    }

    /**
     * TODO: return contact ID if found, else return null
     */
    private String parseAddRecipientResponse(String response) {
        String recipientId = null;
        /*
        //TESTING
        System.out.println("AddRecipient Response:");
        java.util.Scanner s = new java.util.Scanner(response).useDelimiter("\\A");
        System.out.println( s.hasNext() ? s.next() : "" );
        */
        /*
        <Envelope>
            <Body>
                <RESULT>
                    <SUCCESS>TRUE</SUCCESS>
                    <RecipientId>67621536837</RecipientId>
                    <ORGANIZATION_ID>c442db-110deb40bf0-f528764d624db129b32c21fbca0cb8d6</ORGANIZATION_ID>
                </RESULT> 
            </Body>
        </Envelope>
        */
        
        //logMgr.info("parsing AddRecipient response . . .");
        try {
            
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true); 
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(response.getBytes("UTF-8"));
            Document doc = builder.parse(stream);
            //logMgr.info("Document object built from xml");

            //if this returns string, there is a problem. Otherwise it returns null
            String faultStr = findFault(doc);
            
            if(faultStr == null) {  //success is true
                    
                XPath xpath = XPathFactory.newInstance().newXPath();
                XPathExpression rid = xpath.compile("/Envelope/Body/RESULT/RecipientId/text()");
                
                Object ridObj = rid.evaluate(doc, XPathConstants.STRING);
                if(ridObj != null) {
                    recipientId = (String)ridObj;
                    //logMgr.info("recipientID string from xml doc: "+recipientId);
                }
            }
        }
        catch(Exception e) {
            logMgr.severe("trouble parsing AddRecipient response: "+NL+e.getMessage());
        }
        
        return recipientId;
    }

    
    /**
     * TODO: return true if contact added, false if not
     */
    private boolean parseAddContactResponse(String response) {
        boolean success = false;
        /*
        //TESTING
        System.out.println("AddContactToProgram Response:");
        java.util.Scanner s = new java.util.Scanner(response).useDelimiter("\\A");
        System.out.println( s.hasNext() ? s.next() : "" );
        return true;
        */
        /*
        <Envelope>
        <Body>
        ￼   <RESULT>
        <SUCCESS>false</SUCCESS>
        </RESULT>
        <Fault>
        <Request/>
        <FaultCode/>
        <FaultString><![CDATA[The specified program status is Inactive and not accepting new contacts.]]></FaultString>
        <detail>
        <error>
        <errorid>653</errorid>
        <module/>
        <class>SP.API</class>
        <method/>
        </error>
        </detail>
        </Fault>
          </Body>
        </Envelope>
        */
        //logMgr.info("parsing AddContact response . . .");
        try {
            
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true); 
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(response.getBytes("UTF-8"));
            Document doc = builder.parse(stream);
            //logMgr.info("Document object built from xml");

            String faultStr = findFault(doc);
            if(faultStr == null) {  //success is true
                success = true;
            }
        }
        catch(Exception e) {
            logMgr.severe("trouble parsing AddContact response: "+NL+e.getMessage());
        }
        return success;
    }

    /*
    private void loadProperties() {
        logMgr.info("NewsletterEventMgr loading properties . . .");
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("application.properties");
            this.props = new Properties();
            this.props.load(is);
            logMgr.info("NewsletterEventMgr props loaded");
            
        }
        catch(IOException ie) {
            logMgr.log(Level.SEVERE, "Could not load NewsletterEventMgr properties: ", ie);
        }
        catch(Exception e) {
            logMgr.log(Level.SEVERE, "problems loading NewsletterEventMgr properties: ", e);
        }
    }
    */
    
}
