package org.digitalmind.eventorchestrator.converter;

import lombok.extern.slf4j.Slf4j;
import org.digitalmind.eventorchestrator.converter.base.JpaGenericConverter;

import javax.persistence.Converter;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Converter
public class JpaMapJsonConverter extends JpaGenericConverter<Map<String, Object>, LinkedHashMap<String, Object>> {

    public JpaMapJsonConverter() {
        super(JpaGenericConverter.MapperType.TYPE, false);
    }

}

//public class JpaMapJsonConverter implements AttributeConverter<Map<String, Object>, String> {
//
//    private static final ObjectMapper objectMapper = getObjectMapper();
//
//    private static final ObjectMapper getObjectMapper() {
//        ObjectMapper om = new ObjectMapper();
//        om.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);
//        om.enable(SerializationFeature.INDENT_OUTPUT);
//        return om;
//    }
//
//    @Override
//    public String convertToDatabaseColumn(Map<String, Object> attribute) {
//        try {
//            return objectMapper.writeValueAsString(attribute);
//        } catch (JsonProcessingException ex) {
//            throw new JpaMapJsonConverterException("Error while transforming Map to a text datatable column as json string", ex);
//        }
//    }
//
//    @Override
//    public Map<String, Object> convertToEntityAttribute(String dbData) {
//        try {
//            return objectMapper.readValue(dbData, HashMap.class);
//        } catch (IOException ex) {
//            throw new JpaMapJsonConverterException("IO exception while transforming json text column in Map property", ex);
//        }
//    }
//
//}
