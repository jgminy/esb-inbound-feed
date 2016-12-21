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

public class FeedConstant {
	public static final String FEED_URL = "feed.url";
	public static final String FEED_TYPE = "feed.type";
	public static final String FEED_TIME_FORMAT = "feed.timeformat";
	public static final String FEED_FORMAT = "TEXT";
	public static final String FEED_TYPE_RSS = "RSS";
	public static final String FEED_TYPE_ATOM = "Atom";

	public static final String RSS_FEED_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";

	public static final String REGISTRY_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss Z";
	public static final String REGISTRY_FEEDEP_PATH_PREFIX = "repository/components/org.wso2.carbon.inbound.feedep/";
	public static final String REGISTRY_FEEDEP_UPDATE_DATE_PROP = "last.updated";

	public static final String CONTENT_TYPE_APPLICATION_XML = "application/xml";
}
