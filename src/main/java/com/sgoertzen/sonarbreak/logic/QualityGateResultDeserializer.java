package com.sgoertzen.sonarbreak.logic;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgoertzen.sonarbreak.model.ConditionStatus;
import com.sgoertzen.sonarbreak.model.QualityGateCondition;
import com.sgoertzen.sonarbreak.model.QualityGateResult;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sgoertzen on 12/15/15.
 */
public class QualityGateResultDeserializer extends JsonDeserializer {
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        QualityGateResult result = new QualityGateResult();

        JsonNode root= jsonParser.readValueAsTree();
        result.setId(getText(root, "id"));
        result.setKey(getText(root, "key"));
        result.setName(getText(root, "name"));
        result.setVersion(getText(root, "version"));

        JsonNode dateNode = root.get("date");
        if (dateNode != null) {
            String dateString = dateNode.asText();
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZZ");
            result.setDatetime(formatter.parseDateTime(dateString));
        }

        JsonNode msr = root.get("msr");
        if (msr != null)
        {
            // TODO: Don't use an iterator as we just want the first item.  Clean up.
            Iterator<JsonNode> elements = msr.elements();
            if(elements.hasNext()){
                String qualityGateJson = elements.next().get("data").asText();

                ObjectMapper map2 = new ObjectMapper();
                JsonNode jsonNode = map2.readTree(qualityGateJson);
                String level = getText(jsonNode, "level");
                result.setStatus(ConditionStatus.forValue(level));

                List<QualityGateCondition> conditions = map2.convertValue(jsonNode.get("conditions"), new TypeReference<List<QualityGateCondition>>() {});
                result.setConditions(conditions);
            }
        }


        return result;
    }

    private String getText(JsonNode node, String propertyName){
        JsonNode childNode = node.get(propertyName);
        if (childNode == null){
            return null;
        }
        return childNode.asText();
    }
}
