package org.openhab.binding.withings.internal.api;

import org.openhab.binding.withings.internal.model.Attribute;
import org.openhab.binding.withings.internal.model.Category;
import org.openhab.binding.withings.internal.model.MeasureType;


import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class JsonDeserializers {

    public static final class AttributeJsonDeserializer implements JsonDeserializer<Attribute> {
    	 @Override
        public Attribute deserialize(JsonElement jsonElement, Type type,
                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return Attribute.getForType(jsonElement.getAsInt());
        }

    }

    public static final class CategoryJsonDeserializer implements JsonDeserializer<Category> {
    	 @Override
        public Category deserialize(JsonElement jsonElement, Type type,
                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return Category.getForType(jsonElement.getAsInt());
        }

    }

    public static final class MeasureTypeJsonDeserializer implements JsonDeserializer<MeasureType> {
    	 @Override
    	public MeasureType deserialize(JsonElement jsonElement, Type type,
                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return MeasureType.getForType(jsonElement.getAsInt());
        }
    }

}
