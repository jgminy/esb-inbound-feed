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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.carbon.inbound.feedep.FeedConstant;
import org.wso2.carbon.inbound.feedep.FeedRegistryHandler;
import org.xml.sax.SAXException;

/**
 * FeedRssRetriever uses to RSS feeds from given Backend
 */
public class FeedAtomRetriever extends FeedGenericRetriever implements FeedRetriever {
	private static final Log log = LogFactory.getLog(FeedAtomRetriever.class);

	private String name;
	private long scanInterval;
	private long lastRanTime;
	private String feedURL;
	private String feedType;
	private DateFormat feedDateFormat = null;

	private FeedRegistryHandler feedRegistryHandler;

	public FeedAtomRetriever(long scanInterval, String feedURL, String feedType,
			FeedRegistryHandler feedRegistryHandler, String name, String feedDateFormat) {
		this.name = name;

		this.feedURL = feedURL;
		this.feedType = feedType;

		this.scanInterval = scanInterval;

		this.feedRegistryHandler = feedRegistryHandler;

		if (!StringUtils.isEmpty(feedDateFormat)) {
			this.feedDateFormat = new SimpleDateFormat(feedDateFormat, Locale.ENGLISH);
		} else {
			this.feedDateFormat = new SimpleDateFormat(FeedConstant.RSS_FEED_DATE_FORMAT, Locale.ENGLISH);
		}

	}

	public String execute() {
		log.debug("Execute: " + this.name);
		String in = null;
		long currentTime = (new Date()).getTime();

		if (((this.lastRanTime + this.scanInterval) <= currentTime)) {
			this.lastRanTime = currentTime;
			log.debug("LastRanTime: " + this.lastRanTime);
			in = consume();
		} else {
			log.debug("Skip cycle since concurrent rate is higher than the scan interval: " + this.name);
		}
		log.debug("End: " + this.name);
		return in;
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

			NodeList nodeList = document.getElementsByTagName("entry");
			String lastUpdate = null;

			if (nodeList != null) {
				lastUpdate = nodeList.item(0).getOwnerDocument().getElementsByTagName("updated").item(0)
						.getTextContent();
				log.debug("lastPubDate: " + lastUpdate);

				if (lastUpdate != null) {
					return getFeedString(document, lastUpdate, this.feedRegistryHandler, this.feedDateFormat,
							this.name);
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