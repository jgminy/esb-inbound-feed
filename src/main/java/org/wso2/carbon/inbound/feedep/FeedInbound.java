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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.wso2.carbon.inbound.endpoint.protocol.generic.GenericPollingConsumer;
import org.wso2.carbon.inbound.feedep.retriever.FeedAtomRetriever;
import org.wso2.carbon.inbound.feedep.retriever.FeedRetriever;
import org.wso2.carbon.inbound.feedep.retriever.FeedRssRetriever;
import org.wso2.carbon.inbound.feedep.retriever.FeedTextRetriever;

public class FeedInbound extends GenericPollingConsumer {
	private static final Log log = LogFactory.getLog(FeedInbound.class);

	private FeedRetriever feedRetriever = null;
	private final FeedRegistryHandler registryHandler = new FeedRegistryHandler();

	public FeedInbound(Properties properties, String name, SynapseEnvironment synapseEnvironment, long scanInterval,
			String injectingSeq, String onErrorSeq, boolean coordination, boolean sequential) {
		super(properties, name, synapseEnvironment, scanInterval, injectingSeq, onErrorSeq, coordination, sequential);
		log.info("Initialize Feed polling consumer: " + this.name);

		String feedURL;
		String feedType;
		String feedDateFormatText = FeedConstant.RSS_FEED_DATE_FORMAT;

		feedURL = getPropertyValue(FeedConstant.FEED_URL);
		feedType = getPropertyValue(FeedConstant.FEED_TYPE);
		if (!StringUtils.isEmpty(getInboundProperties().getProperty(FeedConstant.FEED_TIME_FORMAT))) {
			feedDateFormatText = getPropertyValue(FeedConstant.FEED_TIME_FORMAT);
		}

		DateFormat feedDateFormat = new SimpleDateFormat(feedDateFormatText, Locale.ENGLISH);

		log.info("feedURL        : " + feedURL);
		log.info("feedType       : " + feedType);
		log.info("feedDateFormat : " + feedDateFormatText);

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

	private String getPropertyValue(String key) {
		String value = getInboundProperties().getProperty(key);
		if (value.startsWith(FeedConstant.GET_PROTERTY_FUNCTION)) {
			value = (String) this.synapseEnvironment.getSynapseConfiguration().getEntry(
					value.trim().substring(FeedConstant.GET_PROTERTY_FUNCTION.length() + 2, value.length() - 2));
		}
		return value;
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