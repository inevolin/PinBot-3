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
import model.pinterestobjects.Board;
import model.pinterestobjects.PinterestObject;

/**
 *
 * @author healzer
 */
public class AbstractBoardAdapter implements JsonDeserializer<Board> {

    @Override
    public Board deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

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
        Board b = gson.fromJson(jsonObject, typeOfT);

        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("name")).findAny().isPresent()) {
            b.setName(jsonObject.get("name").isJsonNull() ? null : (jsonObject.get("name").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("url_name")).findAny().isPresent()) {
            b.setUrlName(jsonObject.get("url_name").isJsonNull() ? null : (jsonObject.get("url_name").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("board_id")).findAny().isPresent()) {
            b.setBoardId(jsonObject.get("board_id").isJsonNull() ? null : (jsonObject.get("board_id").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("user_id")).findAny().isPresent()) {
            b.setUserId(jsonObject.get("user_id").isJsonNull() ? null : (jsonObject.get("user_id").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("username")).findAny().isPresent()) {
            b.setUsername(jsonObject.get("username").isJsonNull() ? null : (jsonObject.get("username").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("followed_by_me")).findAny().isPresent()) {
            b.setFollowedByMe(jsonObject.get("followed_by_me").isJsonNull() ? true : (jsonObject.get("followed_by_me").getAsBoolean()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("attempts")).findAny().isPresent()) {
            b.setAttempts(jsonObject.get("attempts").isJsonNull() ? 0 : (jsonObject.get("attempts").getAsInt()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("pins_count")).findAny().isPresent()) {
            b.setPinsCount(jsonObject.get("pins_count").isJsonNull() ? 0 : (jsonObject.get("pins_count").getAsInt()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("description")).findAny().isPresent()) {
            b.setDescription(jsonObject.get("description").isJsonNull() ? null : (jsonObject.get("description").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("category")).findAny().isPresent()) {
            b.setCategory(jsonObject.get("category").isJsonNull() ? null : (jsonObject.get("category").getAsString()));
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
