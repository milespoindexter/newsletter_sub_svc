package com.cn.dsa.newsletter;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.sql.Timestamp;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.DefaultValue; 
import javax.ws.rs.core.MediaType;


@Path("/")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class NewsletterService {

    private NewsletterEventMgr newsltrEventMgr = new NewsletterEventMgr();

    @POST
    @Path("/update")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //@Produces(MediaType.APPLICATION_JSON)
    public Response update(NewsletterEvent newsltrEvent) throws Exception {
        NewsletterEventResponse response = newsltrEventMgr.update(newsltrEvent);

        //return response;
        return Response.ok().entity(response).build();
    }

    @POST
    @Path("/tk/update")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //@Produces(MediaType.APPLICATION_JSON)
    public Response toolkitUpdate(NewsletterEvent newsltrEvent) throws Exception {
        NewsletterEventResponse response = newsltrEventMgr.toolkitUpdate(newsltrEvent);

        //return response;
        return Response.ok().entity(response).build();
    }


    @GET
    //@Path("/test")
    @Path("/{a:test|update/test|tk/update/test}")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	//@Produces(MediaType.APPLICATION_JSON)
    public Response testNewsletter() throws Exception {
    	//Map props = new HashMap<String,String>();
    	//props.put("extra","extra props not in original spec.");
        //props.put("notes","user specific notes.");

    	Date today = new Date();
    	Timestamp ts = new Timestamp(today.getTime());
    	
        NewsletterEvent ltr = new NewsletterEvent();
        ltr.setEmail("test@condenast.com");

        List<NewsletterItem> newsltrs = new ArrayList<NewsletterItem>();

        NewsletterItem item1234 = new NewsletterItem(1234);
        item1234.setSuccess(null);
        newsltrs.add(item1234);

        NewsletterItem cancelItem = new NewsletterItem(2468);
        cancelItem.setAction(NewsletterItem.UNSUBSCRIBE);
        cancelItem.setEventCode(NewsletterItem.UNSUB_CODE);
        cancelItem.setSuccess(null);
        newsltrs.add(cancelItem);

        NewsletterItem item9876 = new NewsletterItem(1234);
        item9876.setSuccess(null);
        newsltrs.add(item9876);

        ltr.setNewsletters(newsltrs);

        //ltr.setName("Dieting Daily");
        //ltr.setTimestamp(ts.toString());
        //ltr.setProps(props);

        //return ltr;
        return Response.ok().entity(ltr).build();
    }


    @GET
    @Path("/tk/update/test")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //@Produces(MediaType.APPLICATION_JSON)
    public Response testToolkit() throws Exception {
        Date today = new Date();
        Timestamp ts = new Timestamp(today.getTime());
        
        NewsletterEvent nEvent = new NewsletterEvent();
        nEvent.setEmail("tk_test@condenast.com");
        nEvent.setTimestamp("2014-11-19 14:59:30.252");
        
        List<NewsletterItem> newsltrs = new ArrayList<NewsletterItem>();

        NewsletterItem unsubItem = new NewsletterItem(2468);
        unsubItem.setAction(NewsletterItem.TK_UNSUBSCRIBE);
        unsubItem.setEventCode(NewsletterItem.TK_UNSUB_CODE);
        unsubItem.setApplication("testApp");
        unsubItem.setForm("someWebformName");
        unsubItem.setSiteCode("LKY");
        unsubItem.setIP("10.68.2.4");
        unsubItem.setReferer("http://someAppSite.com/refer.html");
        unsubItem.setSourceCode("mySourceCode123");
        unsubItem.setUrl("http://somesite.com/subscribe.html");
        unsubItem.setSuccess(null);
        newsltrs.add(unsubItem);

        nEvent.setNewsletters(newsltrs);
        return Response.ok().entity(nEvent).build();
    }
}
