/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.inbound.feedep.retriever;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.carbon.inbound.feedep.FeedRegistryHandler;
import org.xml.sax.SAXException;

/**
 * FeedRssRetriever uses to RSS feeds from given Backend
 */
public class FeedRssRetriever extends FeedGenericRetriever implements FeedRetriever {
	static final Log log = LogFactory.getLog(FeedRssRetriever.class);

	public FeedRssRetriever(String name, long scanInterval, String feedURL, String feedType, DateFormat feedDateFormat,
			FeedRegistryHandler feedRegistryHandler) {
		super(name, scanInterval, feedURL, feedType, feedDateFormat, feedRegistryHandler);
	}

	@Override
	protected String consume() {
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

			NodeList nodeList = document.getElementsByTagName("lastBuildDate");
			for (int i = 0; i < nodeList.getLength(); i++) {
				log.trace("nodeList[" + i + "]: " + nodeList.item(i).getNodeName() + " : "
						+ nodeList.item(i).getTextContent());
			}

			String lastBuildDate = null;

			if (nodeList != null) {
				lastBuildDate = nodeList.item(0).getTextContent();
				log.debug("lastBuildDate: " + lastBuildDate);

				if (lastBuildDate != null) {
					return getFeedString(document, lastBuildDate);
				}
			}
		} catch (ParseException e) {
			log.error("Error while parse date ", e);
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
