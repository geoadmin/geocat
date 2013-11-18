package org.fao.geonet.geocat.kernel.reusable;

import jeeves.server.context.ServiceContext;

import com.google.common.collect.Multimap;

public class SendEmailParameter
{
    public final ServiceContext           context;
    public final String                   msg;
    public final Multimap<Integer/* ownerid */, Integer/* metadataid */> emailInfo;
    public final String                   baseURL;
    public final String                   msgHeader;
    public final String                   subject;
    public final boolean                  testing;

    public SendEmailParameter(ServiceContext context, String msg,Multimap<Integer/* ownerid */, Integer/* metadataid */> emailInfo,
            String baseURL, String msgHeader, String subject, boolean testing)
    {
        this.context = context;
        this.msg = msg;
        this.emailInfo = emailInfo;
        this.baseURL = baseURL;
        this.msgHeader = msgHeader;
        this.subject = subject;
        this.testing = testing;
    }
}