package com.thnky.ai;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thnky.domain.ChallengeType;

/**
 * Builds the generator's structured-output schema, one shape per challenge
 * type instead of a single schema where every type's fields are all present
 * and merely nullable. In testing, a model asked for a "lines" challenge
 * against a schema that still had a nullable "options" field would sometimes
 * fill "options" in anyway and leave "lines" empty. A schema that never
 * declares "options" for that type makes that structurally impossible
 * instead of just discouraged in the prompt.
 */
public final class GeneratedChallengeSchema {

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    private GeneratedChallengeSchema() {
    }

    public static JsonNode forType(ChallengeType type) {
        ObjectNode properties = NODES.objectNode();
        List<String> required = new ArrayList<>();

        addString(properties, required, "hook");
        addString(properties, required, "title");
        addString(properties, required, "desc");
        addHints(properties, required);
        addString(properties, required, "good");
        addString(properties, required, "improve");
        addString(properties, required, "insight");

        switch (type) {
            case CHOICE -> {
                addStringArray(properties, required, "options");
                addInteger(properties, required, "answer");
            }
            case LINES -> {
                addStringArray(properties, required, "lines");
                addString(properties, required, "file");
                addInteger(properties, required, "answer");
            }
            case CODE -> addString(properties, required, "starter");
            case TEXT -> {
                // hook/title/desc/hints/good/improve/insight already cover it.
            }
        }

        ObjectNode schema = NODES.objectNode();
        schema.put("type", "object");
        schema.set("properties", properties);
        ArrayNode requiredNode = NODES.arrayNode();
        required.forEach(requiredNode::add);
        schema.set("required", requiredNode);
        schema.put("additionalProperties", false);
        return schema;
    }

    private static void addString(ObjectNode properties, List<String> required, String name) {
        properties.set(name, NODES.objectNode().put("type", "string"));
        required.add(name);
    }

    private static void addInteger(ObjectNode properties, List<String> required, String name) {
        properties.set(name, NODES.objectNode().put("type", "integer"));
        required.add(name);
    }

    private static void addStringArray(ObjectNode properties, List<String> required, String name) {
        ObjectNode arraySchema = NODES.objectNode();
        arraySchema.put("type", "array");
        arraySchema.set("items", NODES.objectNode().put("type", "string"));
        properties.set(name, arraySchema);
        required.add(name);
    }

    private static void addHints(ObjectNode properties, List<String> required) {
        ObjectNode hints = NODES.objectNode();
        hints.put("type", "array");
        hints.set("items", NODES.objectNode().put("type", "string"));
        hints.put("minItems", 3);
        hints.put("maxItems", 3);
        properties.set("hints", hints);
        required.add("hints");
    }
}
