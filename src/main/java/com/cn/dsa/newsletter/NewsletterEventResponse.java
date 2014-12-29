package com.cn.dsa.newsletter;

import javax.xml.bind.annotation.*;

//@XmlRootElement(name = "newsletterResponse")
@XmlRootElement(name = "newsletterEventResponse")
@XmlType(propOrder={"success", "responseMsg"})
@XmlAccessorType(XmlAccessType.PROPERTY)
public class NewsletterEventResponse extends NewsletterEvent {
    
    private boolean success;
    
    @XmlElement
    public boolean getSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }

    private String responseMsg;
    
    @XmlElement
    public String getResponseMsg() {
        return responseMsg;
    }
    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }
    
    

}