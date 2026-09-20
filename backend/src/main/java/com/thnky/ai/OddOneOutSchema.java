package com.thnky.ai;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Structured-output schema for an odd-one-out puzzle. Every numeric field
 * uses {@code enum} rather than {@code minimum}/{@code maximum} — the latter
 * was never verified against this model's structured-output mode, while
 * {@code enum} was, so this sticks to what is known to work.
 */
public final class OddOneOutSchema {

    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;
    private static final int TILE_COUNT = 6;

    private OddOneOutSchema() {
    }

    public static JsonNode build() {
        ObjectNode properties = NODES.objectNode();
        properties.set("hook", string());
        properties.set("title", string());
        properties.set("desc", string());
        properties.set("sides", sidesArray());
        properties.set("oddIndex", intEnum(0, 1, 2, 3, 4, 5));
        properties.set("violationDelta", intEnum(-2, -1, 1, 2));
        properties.set("hints", hints());
        properties.set("good", string());
        properties.set("improve", string());
        properties.set("insight", string());

        ObjectNode schema = NODES.objectNode();
        schema.put("type", "object");
        schema.set("properties", properties);
        ArrayNode required = NODES.arrayNode();
        List.of("hook", "title", "desc", "sides", "oddIndex", "violationDelta",
                "hints", "good", "improve", "insight").forEach(required::add);
        schema.set("required", required);
        schema.put("additionalProperties", false);
        return schema;
    }

    private static ObjectNode sidesArray() {
        ObjectNode array = NODES.objectNode();
        array.put("type", "array");
        array.set("items", intEnum(3, 4, 5, 6));
        array.put("minItems", TILE_COUNT);
        array.put("maxItems", TILE_COUNT);
        return array;
    }

    private static ObjectNode string() {
        return NODES.objectNode().put("type", "string");
    }

    private static ObjectNode intEnum(int... values) {
        ObjectNode node = NODES.objectNode();
        node.put("type", "integer");
        ArrayNode values0 = NODES.arrayNode();
        for (int v : values) {
            values0.add(v);
        }
        node.set("enum", values0);
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
