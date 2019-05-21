package com.sgoertzen.sonarbreak.qualitygate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


import java.io.IOException;

public class CEResponseDeserialiser extends JsonDeserializer {
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        CEResponse response = null;
        JsonNode root = jsonParser.readValueAsTree();

        JsonNode current = root.get("current");
        if(null != current) {
            response = new CEResponse();
            String dateString = current.get("executedAt").asText();
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");
            response.setAnalysisTime(formatter.parseDateTime(dateString));
            response.setComponentKey(getText(current, "componentKey"));
        }
        return response;
    }


    /**
     * Get the text from child node with the given name.  Returns null if child node does not exist.
     *
     * @param node         Node to search
     * @param propertyName Name of the child node
     * @return The text value of the child node
     */
    private String getText(JsonNode node, String propertyName) {
        JsonNode childNode = node.get(propertyName);
        if (childNode == null) {
            return null;
        }
        return childNode.asText();
    }

}

