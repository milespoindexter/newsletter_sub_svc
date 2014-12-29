package com.cn.dsa.toolkit;

import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Properties;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import com.cn.dsa.newsletter.NewsletterItem;

import com.cn.dsa.common.ServiceClient;
import com.cn.dsa.common.VelocityMgr;
import com.cn.dsa.common.ParseUtils;

public class ToolkitClient {

    private static String LS = System.getProperties().getProperty("line.separator");
    //Velocity template
    private static String SUB_TEMP = "subscriptionUpdate.vm";

    //For Staging:
    private static final String KEY = "tk.key";
    private static final String SECRET = "tk.secret";
    private static final String URL = "tk.url";
    
    private static Logger log = Logger.getLogger(ToolkitClient.class.getName());
    private static Properties props = null;

    private ServiceClient svcClient = new ServiceClient();
    private static VelocityMgr velocityMgr = new VelocityMgr("toolkit");
    
    private String generateSubscriptionUpdateBody(String email, NewsletterItem newsltr) {
        /*
        $email,$application,$form",$siteCode,$ip,
        $referer,$sourceCode,$url,$newsletterId,$subscribe
        */
        Map<String,String> context = new HashMap<String,String>();
        
        context.put("email", email);

        context.put("newsletterId", Long.toString(newsltr.getId()));
        context.put("application", newsltr.getApplication());
        context.put("form", newsltr.getForm());
        context.put("siteCode", newsltr.getSiteCode());
        context.put("ip", newsltr.getIP());
        context.put("referer", newsltr.getReferer());
        context.put("sourceCode", newsltr.getSourceCode());
        context.put("url", newsltr.getUrl());
        String action = newsltr.getAction();
        if(action.equals(NewsletterItem.TK_SUBSCRIBE)) {
            context.put("subscribe", "true");
        }
        else {
            context.put("subscribe", "false");
        }

        //render template
        return velocityMgr.mergeWithMap(SUB_TEMP, context);

    }

    public String updateSubscription(String email, NewsletterItem newsltr, boolean subscribe) {
        if(props == null) {
            loadProperties();
        }

        String tkUrl = props.getProperty(URL,"");
        String tkKey = props.getProperty(KEY,"");
        String tkSecret = props.getProperty(SECRET,"");

        String apiUrl = tkUrl+"/newsletter/entries";
        String response = "";

        Map<String,String> headers = new HashMap<String,String>();
        String authHeader = OAuthUtils.getPlaintextOauthHeader(tkKey, tkSecret);
        
        //headers.put("Accept", "application/json");    
        headers.put("Accept", "application/xml;ver=1.0");
        headers.put("Content-Type", "application/xml");
        headers.put("Authorization", authHeader);
        
        String body = generateSubscriptionUpdateBody(email, newsltr);
        log.info("toolkit request: "+body);

        if(body != null && body.length() > 0) {
            try {
                response = svcClient.doPostRequest(apiUrl, body, headers);
                //parse response
                String entryId = parseResponse(response);
                if(entryId != null) {
                    newsltr.setEntryId(entryId);
                    newsltr.setStatus("unsubscribed");
                }
            }
            catch(Exception e) {
                response = e.getMessage();
                log.warning("Problems with Toolkit API: "+response);
            }    
        }
        else {
            response = "Problems processing velocity template: "+SUB_TEMP;
        }

        return response;
    }

    private String parseResponse(String response) {
        //<entry id="12345"/>
        String id = null;
        try {
            NodeList nodes = ParseUtils.parseResponse("/*", response);
            if(nodes != null) {
                log.info(nodes.getLength()+" nodes in response");
                for(int i = 0; i < nodes.getLength(); i++) {
                    Node node = nodes.item(i);
                    if(node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element)node;
                        String name = element.getTagName();
                        if(name.equals("entry")) {
                            //get attribute
                            id = element.getAttribute("id");
                        }  
                    }
                }     
            }
               
        }
        catch(Exception ex) {
            log.warning("could not parse toolkit response: "+response);
        }
        
        return id;
    }
    

    private void loadProperties() {
        log.info("ToolkitClient loading properties . . .");
        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("cn.properties");
            props = new Properties();
            props.load(is);
        }
        catch(IOException ie) {
            log.log(Level.SEVERE, "Could not load ToolkitClient properties: ", ie);
        }
        catch(Exception e) {
            log.log(Level.SEVERE, "problems loading ToolkitClient properties: ", e);
        }
    }
    
    
    public static void main(String[] args) {
        ToolkitClient tkClient = new ToolkitClient();
        
    }
    
}




