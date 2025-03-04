package cca.ruian_puller.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@Converter(autoApply = true)
public class JSONObjectConverter implements AttributeConverter<JSONObject, String> {

    @Override
    public String convertToDatabaseColumn(JSONObject attribute) {
        return attribute != null ? attribute.toJSONString() : null;
    }

    @Override
    public JSONObject convertToEntityAttribute(String dbData) {
        try {
            return dbData != null ? (JSONObject) new JSONParser().parse(dbData) : null;
        } catch (ParseException e) {
            throw new IllegalArgumentException("Error parsing JSON", e);
        }
    }
}