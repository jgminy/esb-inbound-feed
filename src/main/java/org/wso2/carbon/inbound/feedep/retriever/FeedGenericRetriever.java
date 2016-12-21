package org.wso2.carbon.inbound.feedep.retriever;

import java.io.StringWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.w3c.dom.Document;
import org.wso2.carbon.inbound.feedep.FeedConstant;
import org.wso2.carbon.inbound.feedep.FeedRegistryHandler;

public abstract class FeedGenericRetriever implements FeedRetriever {
	static final Log log = LogFactory.getLog(FeedGenericRetriever.class);

	protected String documentToString(Document document) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer;
			transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			DOMSource source = new DOMSource(document);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			transformer.transform(source, result);
			return writer.getBuffer().toString();
		} catch (TransformerException e) {
			log.error("Error while transforming feed ", e);
		}
		return null;
	}

	protected String getFeedString(Document document, String lastFeedUpdatedDate,
			FeedRegistryHandler feedRegistryHandler, DateFormat feedDateFormat, String name) throws ParseException {
		log.debug("Start: " + name);

		Date lastFeedUpdated = null;
		try {
			lastFeedUpdated = feedDateFormat.parse(lastFeedUpdatedDate);
		} catch (ParseException e) {
			log.error("Please set correct date format to fix the issue", e);
			throw new SynapseException("Please set correct date format to fix the issue", e);
		}

		DateFormat formatRegistryUpdate = new SimpleDateFormat(FeedConstant.REGISTRY_TIME_FORMAT, Locale.ENGLISH);
		Date lastRegistryUpdated = (feedRegistryHandler.readPropertiesFromRegistry(name,
				FeedConstant.REGISTRY_FEEDEP_UPDATE_DATE_PROP) != null
						? formatRegistryUpdate.parse(feedRegistryHandler.readPropertiesFromRegistry(name,
								FeedConstant.REGISTRY_FEEDEP_UPDATE_DATE_PROP))
						: null);
		log.debug("lastRegistryUpdated: " + lastRegistryUpdated);
		log.debug("lastFeedUpdated: " + lastFeedUpdated);

		if (lastRegistryUpdated == null) {
			log.debug("lastRegistryUpdated is null");
			feedRegistryHandler.writePropertiesToRegistry(name, FeedConstant.REGISTRY_FEEDEP_UPDATE_DATE_PROP,
					formatRegistryUpdate.format(lastFeedUpdated));
			log.info("Feed updated to be injected: " + name);
			return documentToString(document);
		} else if (lastFeedUpdated.after(lastRegistryUpdated)) {
			log.debug("lastFeedUpdated after lastRegistryUpdated");
			feedRegistryHandler.writePropertiesToRegistry(name, FeedConstant.REGISTRY_FEEDEP_UPDATE_DATE_PROP,
					formatRegistryUpdate.format(lastFeedUpdated));
			log.info("Feed updated to be injected: " + name);
			return documentToString(document);
		}
		return null;
	}

}
