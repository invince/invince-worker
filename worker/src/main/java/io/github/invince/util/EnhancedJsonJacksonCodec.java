package io.github.invince.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.experimental.UtilityClass;
import org.redisson.client.codec.BaseCodec;
import org.redisson.codec.JsonJacksonCodec;

/**
 * Default redisson JsonJacksonCodec + JavaTimeModule
 */
@UtilityClass
public class EnhancedJsonJacksonCodec {

    public BaseCodec get(){
        ObjectMapper om = new ObjectMapper();
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        om.registerModule(new JavaTimeModule());//make java.time.api works
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new JsonJacksonCodec(om);
    }
}
