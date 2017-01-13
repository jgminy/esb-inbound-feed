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

package org.wso2.carbon.inbound.feedep;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.carbon.inbound.endpoint.protocol.generic.GenericPollingConsumer;
import org.wso2.carbon.inbound.feedep.retriever.FeedAtomRetriever;
import org.wso2.carbon.inbound.feedep.retriever.FeedRetriever;
import org.wso2.carbon.inbound.feedep.retriever.FeedRssRetriever;
import org.wso2.carbon.inbound.feedep.retriever.FeedTextRetriever;
import org.xml.sax.SAXException;

public class FeedInbound extends GenericPollingConsumer {
	private static final Log log = LogFactory.getLog(FeedInbound.class);

	private FeedRetriever feedRetriever = null;
	private final FeedRegistryHandler registryHandler = new FeedRegistryHandler();

	public FeedInbound(Properties properties, String name, SynapseEnvironment synapseEnvironment, long scanInterval,
			String injectingSeq, String onErrorSeq, boolean coordination, boolean sequential) {
		super(properties, name, synapseEnvironment, scanInterval, injectingSeq, onErrorSeq, coordination, sequential);
		log.info("Initialize Feed polling consumer: " + this.name);

		String epRepositoryConfig;
		String feedURL;
		String feedType;
		String feedDateFormatText = FeedConstant.RSS_FEED_DATE_FORMAT;

		if (!StringUtils.isEmpty(getInboundProperties().getProperty(FeedConstant.EP_REPOSITORY_CONFIG))) {

			epRepositoryConfig = getInboundProperties().getProperty(FeedConstant.EP_REPOSITORY_CONFIG);
			log.info("epRepositoryConfig : " + epRepositoryConfig);

			try {

				SynapseXPath xPath = new SynapseXPath(FeedConstant.GET_PROTERTY_FUNCTION_PREFIX + epRepositoryConfig
						+ FeedConstant.GET_PROTERTY_FUNCTION_SUFFIX);
				String epRepositoryConfigContent = xPath.stringValueOf(synapseEnvironment.createMessageContext());
				log.trace("epRepositoryConfigContent: " + epRepositoryConfigContent);

				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document document = dBuilder
						.parse(new ByteArrayInputStream(epRepositoryConfigContent.getBytes("UTF-8")));
				document.getDocumentElement().normalize();

				log.trace("root: " + document.getDocumentElement());

				NodeList nodeList;
				nodeList = document.getElementsByTagName(FeedConstant.FEED_URL);
				feedURL = nodeList.item(0).getTextContent();
				nodeList = document.getElementsByTagName(FeedConstant.FEED_TYPE);
				feedType = nodeList.item(0).getTextContent();
				nodeList = document.getElementsByTagName(FeedConstant.FEED_TIME_FORMAT);
				if (nodeList.getLength() > 0) {
					feedDateFormatText = nodeList.item(0).getTextContent();
				}

			} catch (ParserConfigurationException e) {
				throw new IllegalArgumentException("error configuring from registry", e);
			} catch (SAXException e) {
				throw new IllegalArgumentException("error configuring from registry", e);
			} catch (IOException e) {
				throw new IllegalArgumentException("error configuring from registry", e);
			} catch (JaxenException e) {
				throw new IllegalArgumentException("error configuring from registry", e);
			}

		} else {
			feedURL = getInboundProperties().getProperty(FeedConstant.FEED_URL);
			feedType = getInboundProperties().getProperty(FeedConstant.FEED_TYPE);
			if (!StringUtils.isEmpty(getInboundProperties().getProperty(FeedConstant.FEED_TIME_FORMAT))) {
				feedDateFormatText = getInboundProperties().getProperty(FeedConstant.FEED_TIME_FORMAT);
			}
		}

		DateFormat feedDateFormat = new SimpleDateFormat(feedDateFormatText, Locale.ENGLISH);

		log.info("feedURL            : " + feedURL);
		log.info("feedType           : " + feedType);
		log.info("feedDateFormat     : " + feedDateFormatText);

		if (FeedConstant.FEED_TYPE_RSS.equalsIgnoreCase(feedType)) {
			this.feedRetriever = new FeedRssRetriever(this.name, scanInterval, feedURL, feedType, feedDateFormat,
					this.registryHandler);
		}

		if (FeedConstant.FEED_TYPE_ATOM.equalsIgnoreCase(feedType)) {
			this.feedRetriever = new FeedAtomRetriever(this.name, scanInterval, feedURL, feedType, feedDateFormat,
					this.registryHandler);
		}

		if (this.feedRetriever == null) {
			this.feedRetriever = new FeedTextRetriever(this.name, scanInterval, feedURL, feedType, feedDateFormat,
					this.registryHandler);
		}

		log.info("Feed polling consumer Initialized.");
	}

	public void destroy() {
		// this.registryHandler.deleteResourceFromRegistry(this.name);
		log.info("Destroy invoked.");
	}

	public Object poll() {
		String out = this.feedRetriever.execute();
		if (out != null) {
			this.injectMessage(out, FeedConstant.CONTENT_TYPE_APPLICATION_XML);
		}
		return null;
	}
}