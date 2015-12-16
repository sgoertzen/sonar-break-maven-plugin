package com.sgoertzen.sonarbreak.logic;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgoertzen.sonarbreak.model.ConditionStatus;
import com.sgoertzen.sonarbreak.model.QualityGateCondition;
import com.sgoertzen.sonarbreak.model.QualityGateResult;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sgoertzen on 12/15/15.
 */
public class QualityGateResultDeserializer extends JsonDeserializer {
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        QualityGateResult result = new QualityGateResult();

        JsonNode root= jsonParser.readValueAsTree();
        result.setId(getText(root, "id"));
        result.setKey(getText(root, "key"));
        result.setName(getText(root, "name"));
        result.setVersion(getText(root, "version"));

        JsonNode msr = root.get("msr");
        if (msr != null)
        {
            // TODO: Don't use an iterator as we just want the first item.  Clean up.
            Iterator<JsonNode> elements = msr.elements();
            while(elements.hasNext()){
                String qualityGateJson = elements.next().get("data").asText();
                //System.out.println(qualityGateJson);

                ObjectMapper map2 = new ObjectMapper();
                //map2.readValue(qualityGateJson, )
                JsonNode jsonNode = map2.readTree(qualityGateJson);
                String level = getText(jsonNode, "level");
                result.setStatus(ConditionStatus.forValue(level));

                List<QualityGateCondition> conditions = map2.convertValue(jsonNode.get("conditions"), new TypeReference<List<QualityGateCondition>>() {});
                result.setConditions(conditions);
                break;
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
