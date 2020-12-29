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
import model.pinterestobjects.Pinner;
import model.pinterestobjects.PinterestObject;

/**
 *
 * @author healzer
 */
public class AbstractPinnerAdapter implements JsonDeserializer<Pinner> {

    @Override
    public Pinner deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

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
        Pinner b = gson.fromJson(jsonObject, typeOfT);

        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("username")).findAny().isPresent()) {
            b.setUsername(jsonObject.get("username").isJsonNull() ? null : (jsonObject.get("username").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("base_username")).findAny().isPresent()) {
            b.setBaseUsername(jsonObject.get("base_username").isJsonNull() ? null : (jsonObject.get("base_username").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("pins_count")).findAny().isPresent()) {
            b.setPinsCount(jsonObject.get("pins_count").isJsonNull() ? null : (jsonObject.get("pins_count").getAsInt()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("followers_count")).findAny().isPresent()) {
            b.setFollowersCount(jsonObject.get("followers_count").isJsonNull() ? null : (jsonObject.get("followers_count").getAsInt()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("following_count")).findAny().isPresent()) {
            b.setFollowingCount(jsonObject.get("following_count").isJsonNull() ? null : (jsonObject.get("following_count").getAsInt()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("boards_count")).findAny().isPresent()) {
            b.setBoardsCount(jsonObject.get("boards_count").isJsonNull() ? null : (jsonObject.get("boards_count").getAsInt()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("attempts")).findAny().isPresent()) {
            b.setAttempts(jsonObject.get("attempts").isJsonNull() ? null : (jsonObject.get("attempts").getAsInt()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("pinner_id")).findAny().isPresent()) {
            b.setPinnerId(jsonObject.get("pinner_id").isJsonNull() ? null : (jsonObject.get("pinner_id").getAsString()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("followed_by_me")).findAny().isPresent()) {
            b.setFollowedByMe(jsonObject.get("followed_by_me").isJsonNull() ? null : (jsonObject.get("followed_by_me").getAsBoolean()));
        }
        if (jsonObject.entrySet().stream().filter(o -> o.getKey().equalsIgnoreCase("time_follow")).findAny().isPresent()) {
            b.setTimeFollow(jsonObject.get("time_follow").isJsonNull() ? null : (jsonObject.get("time_follow").getAsLong()));
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
