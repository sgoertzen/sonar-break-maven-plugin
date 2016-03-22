package com.sgoertzen.sonarbreak.qualitygate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgoertzen.sonarbreak.qualitygate.ConditionStatus;
import com.sgoertzen.sonarbreak.qualitygate.Condition;
import com.sgoertzen.sonarbreak.qualitygate.Result;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Custom JSON deserializer for a Quality Gate Result.
 */
public class ResultDeserializer extends JsonDeserializer {
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        Result result = new Result();

        JsonNode root = jsonParser.readValueAsTree();
        result.setId(getText(root, "id"));
        result.setKey(getText(root, "key"));
        result.setName(getText(root, "name"));
        result.setVersion(getText(root, "version"));

        JsonNode dateNode = root.get("date");
        if (dateNode == null) {
            throw new IOException("Node named \"date\" not found in JSON response.");
        }
        String dateString = dateNode.asText();
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");
        result.setDatetime(formatter.parseDateTime(dateString));


        JsonNode msr = root.get("msr");
        if (msr == null) {
            throw new IOException("Node named \"msr\" not found in JSON response.");
        }
        Iterator<JsonNode> elements = msr.elements();
        if (elements.hasNext()) {
            String qualityGateJson = elements.next().get("data").asText();

            ObjectMapper map2 = new ObjectMapper();
            JsonNode jsonNode = map2.readTree(qualityGateJson);
            String level = getText(jsonNode, "level");
            result.setStatus(ConditionStatus.forValue(level));

            List<Condition> conditions = map2.convertValue(jsonNode.get("conditions"), new TypeReference<List<Condition>>() {
            });
            result.setConditions(conditions);
        }
        return result;
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
