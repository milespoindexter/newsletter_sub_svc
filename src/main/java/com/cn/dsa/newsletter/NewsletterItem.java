package com.cn.dsa.newsletter;

import javax.xml.bind.annotation.*;
import com.cn.dsa.silverpop.common.Org;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlRootElement(name = "newsletter")
@XmlType(propOrder={    "id",
                        "action",
                        "eventCode",
                        "sendEmail",
                        "status",
                        "success",
                        "subscriberId",
                        "responseMsg",
                        "application",
                        "form",
                        "siteCode",
                        "IP",
                        "referer",
                        "sourceCode",
                        "url",
                        "entryId"
                    })

public class NewsletterItem {
    //Actions
    public static final String SUBSCRIBE = "subscribe";
    public static final String UNSUBSCRIBE = "unsubscribe";
    public static final String TK_SUBSCRIBE = "toolkit_subscribe";
    public static final String TK_UNSUBSCRIBE = "toolkit_unsubscribe";
    //Event Codes
    public static final String SUB_CODE = "N01";
    public static final String UNSUB_CODE = "N02";
    public static final String TK_SUB_CODE = "TK01";
    public static final String TK_UNSUB_CODE = "TK02";

    private long id;
    private String action = SUBSCRIBE;  //default
    private String eventCode = SUB_CODE;  //default
    private Boolean sendEmail = true;  //default
    private String status;
    private Boolean success = false;
    private String subscriberId;
    private String responseMsg;

    //track Org data for DB record
    private Org org = null;

    //constructors
    public NewsletterItem() {
        super();
    }
    public NewsletterItem(long id) {
        this.id = id;
    }
    
    @XmlAttribute
    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    @XmlAttribute
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
    
    @XmlAttribute
    public String getEventCode() {
        return eventCode;
    }
    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    @XmlAttribute
    public Boolean isSendEmail() {
        return sendEmail;
    }
    public void setSendEmail(Boolean sendEmail) {
        this.sendEmail = sendEmail;
    }
    
    @XmlElement
    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }
    

    @XmlElement
    public Boolean getSuccess() {
        return success;
    }
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    
    @XmlElement
    public String getSubscriberId() {
        return subscriberId;
    }
    public void setSubscriberId(String subscriberId) {
        this.subscriberId = subscriberId;
    }
    

    @XmlElement
    public String getResponseMsg() {
        return responseMsg;
    }
    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }
    
    @XmlTransient
    public Org getOrg() {
        return org;
    }
    public void setOrg(Org org) {
        this.org = org;
    }

    /********* TOOLKIT UPDATE ITEMS *************/
    //$application,$form",$siteCode,$ip,$referer,$sourceCode,$url,$subscribe
    private String application = null;
    private String form = null;
    private String siteCode = null;
    private String ip = null;
    private String referer = null;
    private String sourceCode = null;
    private String url = null;
    private String entryId = null;

    @XmlElement
    public String getApplication() {
        return application;
    }
    public void setApplication(String application) {
        this.application = application;
    }
    @XmlElement
    public String getForm() {
        return form;
    }
    public void setForm(String form) {
        this.form = form;
    }
    @XmlElement
    public String getSiteCode() {
        return siteCode;
    }
    public void setSiteCode(String siteCode) {
        this.siteCode = siteCode;
    }
    @XmlElement
    public String getIP() {
        return ip;
    }
    public void setIP(String ip) {
        this.ip = ip;
    }
    @XmlElement
    public String getReferer() {
        return referer;
    }
    public void setReferer(String referer) {
        this.referer = referer;
    }
    @XmlElement
    public String getSourceCode() {
        return sourceCode;
    }
    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }
    @XmlElement
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    @XmlElement
    public String getEntryId() {
        return entryId;
    }
    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }
   
}