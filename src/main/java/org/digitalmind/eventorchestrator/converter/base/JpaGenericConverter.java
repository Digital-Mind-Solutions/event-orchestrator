package org.digitalmind.eventorchestrator.converter.base;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import lombok.extern.slf4j.Slf4j;
import org.digitalmind.eventorchestrator.converter.exception.JpaMapJsonConverterException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public abstract class JpaGenericConverter<C, T extends C> implements AttributeConverter<C, String> {

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final Map<MapperType, ObjectMapper> objectMapperMap = initializeObjectMapper();

    private static final Map<MapperType, ObjectMapper> initializeObjectMapper() {
        Map<MapperType, ObjectMapper> objectMapperMap = new HashMap<MapperType, ObjectMapper>();
        ObjectMapper objectMapper;

        //SIMPLE
        objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        objectMapperMap.put(MapperType.SIMPLE, objectMapper);

        //TYPE
        objectMapper = new ObjectMapper();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, JsonTypeInfo.As.PROPERTY);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapperMap.put(MapperType.TYPE, objectMapper);

        return objectMapperMap;
    }

    public enum MapperType {
        SIMPLE,
        TYPE;

    }

    private Class<T> type;
    private MapperType mapperType;
    private boolean encryption;

    public JpaGenericConverter(
            MapperType mapperType,
            boolean encryption
    ) {
        this.mapperType = mapperType;
        Type actualTypeArgument = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        if (actualTypeArgument instanceof ParameterizedType) {
            this.type = (Class<T>) ((ParameterizedType) actualTypeArgument).getRawType();
        } else {
            this.type = (Class<T>) actualTypeArgument;
        }
        this.encryption = encryption;
    }

    public Class<T> getType() {
        return this.type;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapperMap.get(mapperType);
    }

    @Override
    public String convertToDatabaseColumn(C attribute) {
        if (attribute == null) return null;
        try {
            return encrypt(getObjectMapper().writeValueAsString(attribute));
        } catch (JsonProcessingException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
            throw new JpaMapJsonConverterException("Error while transforming <" + this.type.getSimpleName() + "> to a text datatable column as json string", ex);
        }
    }

    @Override
    public C convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return getObjectMapper().readValue(decrypt(dbData), getType());
        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException ex) {
            throw new JpaMapJsonConverterException("IO exception while transforming json text column in <" + this.type.getSimpleName() + "> object property", ex);
        }
    }

    private String encrypt(String data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if (!encryption) return data;
        // do some encryption
        Key key = new SecretKeySpec(JpaGenericConverterProperties.getConfig().getKey().getBytes(), JpaGenericConverterProperties.getConfig().getAlgorithm());
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getEncoder().encodeToString(c.doFinal(data.getBytes()));
    }

    private String decrypt(String data) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        if (!encryption) return data;
        // do some decryption
        Key key = new SecretKeySpec(JpaGenericConverterProperties.getConfig().getKey().getBytes(), JpaGenericConverterProperties.getConfig().getAlgorithm());
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);
        return new String(c.doFinal(Base64.getDecoder().decode(data)));
    }

}
