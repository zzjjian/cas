package com.ebao.cas.controller;

import javax.ws.rs.Produces;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ebao.cas.credential.TokenEntity;

@RestController("/ebao")
public class EbaoTicketsResource {
	private static final Logger LOGGER = LoggerFactory.getLogger(EbaoTicketsResource.class);

    @Autowired
    private CentralAuthenticationService cas;
    
    @RequestMapping(value = "/v1/tickets", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Produces({"application/json"}) 
    public final TokenEntity createTicket(@RequestBody final MultiValueMap<String, String> requestBody) {
        try{
            final TicketGrantingTicket tgtId = this.cas.createTicketGrantingTicket(obtainCredential(requestBody));
            return new TokenEntity(tgtId.getId());
        } catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return new TokenEntity(null);
        }
    }
    
    protected Credential obtainCredential(final MultiValueMap<String, String> requestBody) {
        return new UsernamePasswordCredential(requestBody.getFirst("username"), requestBody.getFirst("password"));
    }
    
    @RequestMapping(value = "/v1/json/tickets", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Produces({"application/json"}) 
    public final TokenEntity createTicketForRainbow(@RequestBody UsernamePasswordCredential cre) {
        try{
            final TicketGrantingTicket tgtId = this.cas.createTicketGrantingTicket(cre);
            return new TokenEntity(tgtId.getId());
        } catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return new TokenEntity(null);
        }
    }
}
