package com.thnky.ai;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Structured-output schema for a shape-sequence puzzle. {@code rotationStep}
 * and {@code visibleSteps} are constrained with {@code enum} so the model
 * cannot pick a value that would make the four answer tiles collide (see
 * {@code RotationSequenceSvg}'s class doc) — validated again on the Java
 * side regardless, since strict mode is not a guarantee.
 */
public final class ShapeSequenceSchema {

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    private ShapeSequenceSchema() {
    }

    public static JsonNode build() {
        ObjectNode properties = NODES.objectNode();
        properties.set("hook", string());
        properties.set("title", string());
        properties.set("desc", string());
        properties.set("rotationStep", intEnum(60, 90));
        properties.set("startSolid", NODES.objectNode().put("type", "boolean"));
        properties.set("visibleSteps", intEnum(2, 3));
        properties.set("hints", hints());
        properties.set("good", string());
        properties.set("improve", string());
        properties.set("insight", string());

        ObjectNode schema = NODES.objectNode();
        schema.put("type", "object");
        schema.set("properties", properties);
        ArrayNode required = NODES.arrayNode();
        List.of("hook", "title", "desc", "rotationStep", "startSolid", "visibleSteps",
                "hints", "good", "improve", "insight").forEach(required::add);
        schema.set("required", required);
        schema.put("additionalProperties", false);
        return schema;
    }

    private static ObjectNode string() {
        return NODES.objectNode().put("type", "string");
    }

    private static ObjectNode intEnum(int a, int b) {
        ObjectNode node = NODES.objectNode();
        node.put("type", "integer");
        ArrayNode values = NODES.arrayNode();
        values.add(a);
        values.add(b);
        node.set("enum", values);
        return node;
    }

    private static ObjectNode hints() {
        ObjectNode node = NODES.objectNode();
        node.put("type", "array");
        node.set("items", string());
        node.put("minItems", 3);
        node.put("maxItems", 3);
        return node;
    }
}
