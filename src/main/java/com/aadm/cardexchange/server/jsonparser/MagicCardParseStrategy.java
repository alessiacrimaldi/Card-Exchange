package com.aadm.cardexchange.server.jsonparser;

import com.aadm.cardexchange.shared.models.MagicCard;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class MagicCardParseStrategy implements CardParseStrategy {

    public MagicCard execute(JsonObject json) {
        // fields
        String name = json.has("name") ? json.get("name").getAsString() : "unknown";
        String description = json.has("text") ? json.get("text").getAsString() : "unknown";
        String types = json.has("types") ? json.get("types").getAsString() : "unknown";
        String artist = json.has("artist") ? json.get("artist").getAsString() : "unknown";
        String rarity = json.has("rarity") ? json.get("rarity").getAsString() : "unknown";
        // variants
        List<String> variants = new ArrayList<>();
        String[] variantNames = {"hasFoil", "isAlternative", "isFullArt", "isPromo", "isReprint"};
        for (String variantName : variantNames) {
            if (json.has(variantName) && json.get(variantName).getAsInt() != 0) {
                variants.add(variantName);
            }
        }
        return new MagicCard(name, description, types, artist, rarity, variants.toArray(new String[0]));
    }
}
