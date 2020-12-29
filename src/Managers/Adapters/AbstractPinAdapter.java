/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Managers.Adapters;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import java.lang.reflect.Type;
import java.util.Map.Entry;
import model.pinterestobjects.Pin;
import model.pinterestobjects.PinterestObject;

/**
 *
 * @author healzer
 */
public class AbstractPinAdapter implements JsonDeserializer<Pin> {

    @Override
    public Pin deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();

        for (Entry<String, JsonElement> kv : jsonObject.entrySet()) {
            JsonElement je = kv.getValue();
            if (je.isJsonPrimitive()) {
                JsonPrimitive jp = je.getAsJsonPrimitive();
                if (jp.isString()) {
                    if (jp.getAsString().length() > 200) {
                        JsonPrimitive newJp = new JsonPrimitive(jp.getAsString().substring(0, 200));
                        kv.setValue(newJp);
                    }
                }
            }
        }

        Gson gson = new Gson();
        Pin b = gson.fromJson(jsonObject, typeOfT);

        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("hash_url")).findAny().isPresent()) {
            b.setHashUrl(jsonObject.get("hash_url").isJsonNull() ? null : (jsonObject.get("hash_url").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("pin_url")).findAny().isPresent()) {
            b.setPinUrl(jsonObject.get("pin_url").isJsonNull() ? null : (jsonObject.get("pin_url").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("description")).findAny().isPresent()) {
            b.setDescription(jsonObject.get("description").isJsonNull() ? null : (jsonObject.get("description").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("source_url")).findAny().isPresent()) {
            b.setSourceUrl(jsonObject.get("source_url").isJsonNull() ? null : (jsonObject.get("source_url").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("pin_id")).findAny().isPresent()) {
            b.setPinId(jsonObject.get("pin_id").isJsonNull() ? null : (jsonObject.get("pin_id").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("username")).findAny().isPresent()) {
            b.setUsername(jsonObject.get("username").isJsonNull() ? null : (jsonObject.get("username").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("board_name__e_x_t")).findAny().isPresent()) {
            b.setBoardName_EXT(jsonObject.get("board_name__e_x_t").isJsonNull() ? null : (jsonObject.get("board_name__e_x_t").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("board_id__e_x_t")).findAny().isPresent()) {
            b.setBoardId_EXT(jsonObject.get("board_id__e_x_t").isJsonNull() ? null : (jsonObject.get("board_id__e_x_t").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("attempts")).findAny().isPresent()) {
            b.setAttempts(jsonObject.get("attempts").isJsonNull() ? null : (jsonObject.get("attempts").getAsInt()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("time_before_next_pin")).findAny().isPresent()) {
            b.setTimeBeforeNextPin(jsonObject.get("time_before_next_pin").isJsonNull() ? null : (jsonObject.get("time_before_next_pin").getAsLong()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("liked_by_me")).findAny().isPresent()) {
            b.setLikedByMe(jsonObject.get("liked_by_me").isJsonNull() ? true : (jsonObject.get("liked_by_me").getAsBoolean()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("destination_board_id")).findAny().isPresent()) {
            b.setDestinationBoardId(jsonObject.get("destination_board_id").isJsonNull() ? null : (jsonObject.get("destination_board_id").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("id")).findAny().isPresent()) {
            b.setId(jsonObject.get("id").isJsonNull() ? null : (jsonObject.get("id").getAsLong()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("timespan")).findAny().isPresent()) {
            b.setTimespan(jsonObject.get("timespan").isJsonNull() ? null : (jsonObject.get("timespan").getAsLong()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("resource")).findAny().isPresent()) {
            b.setResource(jsonObject.get("resource").isJsonNull() ? null : PinterestObject.PinterestObjectResources.valueOf((jsonObject.get("resource").getAsString())));
        }

        return b;

    }

}
