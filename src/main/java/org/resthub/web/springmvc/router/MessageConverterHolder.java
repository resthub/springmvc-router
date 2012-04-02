package org.resthub.web.springmvc.router;

import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Source;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.http.converter.xml.XmlAwareFormHttpMessageConverter;

public class MessageConverterHolder {
    
    private List<HttpMessageConverter<?>> messageConverters;

    public MessageConverterHolder() {
        this.messageConverters = new ArrayList<HttpMessageConverter<?>>();
        
        
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter();
        stringHttpMessageConverter.setWriteAcceptCharset(false); // See SPR-7316

        this.messageConverters = new ArrayList<HttpMessageConverter<?>>();
        this.messageConverters.add(new ByteArrayHttpMessageConverter());
        this.messageConverters.add(stringHttpMessageConverter);
        this.messageConverters.add(new SourceHttpMessageConverter<Source>());
        this.messageConverters.add(new XmlAwareFormHttpMessageConverter());
    }
    
    	/**
	 * Provide the converters to use in argument resolvers and return value 
	 * handlers that support reading and/or writing to the body of the 
	 * request and response.
	 */
	public void setMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
		this.messageConverters = messageConverters;
	}

	/**
	 * Return the configured message body converters.
	 */
	public List<HttpMessageConverter<?>> getMessageConverters() {
		return messageConverters;
	}
    
}
