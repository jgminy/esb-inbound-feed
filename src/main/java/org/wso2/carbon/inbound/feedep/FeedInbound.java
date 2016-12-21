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

	private String feedURL;
	private String feedType;
	private String feedDateFormat = FeedConstant.RSS_FEED_DATE_FORMAT;

	private FeedRetriever feedRetriever = null;
	private final FeedRegistryHandler registryHandler = new FeedRegistryHandler();

	public FeedInbound(Properties properties, String name, SynapseEnvironment synapseEnvironment, long scanInterval,
			String injectingSeq, String onErrorSeq, boolean coordination, boolean sequential) {
		super(properties, name, synapseEnvironment, scanInterval, injectingSeq, onErrorSeq, coordination, sequential);
		log.info("Initialize Feed polling consumer: " + this.name);

		this.feedURL = getInboundProperties().getProperty(FeedConstant.FEED_URL);
		this.feedType = getInboundProperties().getProperty(FeedConstant.FEED_TYPE);
		if (!StringUtils.isEmpty(getInboundProperties().getProperty(FeedConstant.FEED_TIME_FORMAT))) {
			this.feedDateFormat = getInboundProperties().getProperty(FeedConstant.FEED_TIME_FORMAT);
		}

		log.info("feedURL        : " + this.feedURL);
		log.info("feedType       : " + this.feedType);
		log.info("feedDateFormat : " + this.feedDateFormat);

		if (FeedConstant.FEED_TYPE_RSS.equalsIgnoreCase(this.feedType)) {
			this.feedRetriever = new FeedRssRetriever(scanInterval, this.feedURL, this.feedType, this.registryHandler,
					this.name, this.feedDateFormat);
		}

		if (FeedConstant.FEED_TYPE_ATOM.equalsIgnoreCase(this.feedType)) {
			this.feedRetriever = new FeedAtomRetriever(scanInterval, this.feedURL, this.feedType, this.registryHandler,
					this.name, this.feedDateFormat);
		}

		if (this.feedRetriever == null) {
			this.feedRetriever = new FeedTextRetriever(scanInterval, this.feedURL, this.feedType, this.registryHandler,
					this.name, this.feedDateFormat);
		}

		log.info("Feed polling consumer Initialized.");
	}

	public void destroy() {
		this.registryHandler.deleteResourceFromRegistry(this.name);
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