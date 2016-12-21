package org.wso2.carbon.inbound.feedep.retriever;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.w3c.dom.Document;
import org.wso2.carbon.inbound.feedep.FeedRegistryHandler;
import org.xml.sax.SAXException;

public class FeedTextRetriever extends FeedGenericRetriever implements FeedRetriever {
	private static final Log log = LogFactory.getLog(FeedTextRetriever.class);

	private String feedURL;
	private String feedType;

	public FeedTextRetriever(long scanInterval, String feedURL, String feedType,
			FeedRegistryHandler feedRegistryHandler, String name, String feedDateFormat) {

		this.feedURL = feedURL;
		this.feedType = feedType;

	}

	public String execute() {
		return consume();
	}

	private String consume() {
		InputStream in = null;
		try {
			in = new URL(feedURL).openStream();

			if (in == null) {
				log.error("Please check host address or feed type: " + this.feedType + " " + this.feedURL);
				throw new SynapseException(
						"Please check host address or feed type: " + this.feedType + " " + this.feedURL);
			}

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document document = dBuilder.parse(in);
			document.getDocumentElement().normalize();

			log.trace("root: " + document.getDocumentElement());

			return documentToString(document);
			
		} catch (MalformedURLException e) {
			log.error("Given url doesn't have feed ", e);
		} catch (IOException e) {
			log.error("Error while read feed ", e);
		} catch (ParserConfigurationException e) {
			log.error("Error while parsing feed ", e);
		} catch (SAXException e) {
			log.error("Error while parsing feed ", e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				log.error("Error while closing input feed input stream ", e);
			}
		}
		return null;
	}
}
