package com.ebao.cas.spring;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.WebApplicationInitializer;

public class EbaoWebApplicationInitializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		// TODO Auto-generated method stub
		Properties properties=loadSystemProperties(servletContext);
		register(properties);
		
	}
	
	private void register(Properties properties) {
		System.setProperty("spring.profiles.active", properties.getProperty("spring.profiles.active"));
	}
	
	private Properties loadSystemProperties(ServletContext servletContext) {
		InputStream inputStream = servletContext.getResourceAsStream("/WEB-INF/cas.properties");
	    Properties properties=new Properties();
		if (null != inputStream) {
	      try {
	    	  properties.load(inputStream);
	      } catch (IOException e) {
	    	  Logger.getGlobal().log(Level.SEVERE, "init logging system", e);
	      }finally{
	    	  try {
				inputStream.close();
			} catch (IOException e) {
			}
	      }
	    }
		
		return properties;
	}

}
