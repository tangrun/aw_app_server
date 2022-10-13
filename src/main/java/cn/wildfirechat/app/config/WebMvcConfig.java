package cn.wildfirechat.app.config;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.CollectionFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.format.FormatterRegistry;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.annotation.Annotation;
import java.text.SimpleDateFormat;
import java.util.*;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new GenericConverter() {

            @Override
            public Set<ConvertiblePair> getConvertibleTypes() {
                return Collections.singleton(new ConvertiblePair(String.class, Date.class));
            }

            @Override
            public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
                if (StringUtils.isBlank((CharSequence) source))
                    return null;

                {
                    DateTimeFormat dateTimeFormat = targetType.getAnnotation(DateTimeFormat.class);
                    if (dateTimeFormat != null) {
                        try {
                            return new SimpleDateFormat(dateTimeFormat.pattern()).parse((String) source);
                        } catch (Exception e) {
                        }
                    }
                }

                try {
                    long l = Long.parseLong((String) source);
                    return new Date(l);
                } catch (Exception e) {
                }

                return null;
            }
        });

        registry.addConverter(new GenericConverter() {
            @Override
            public Set<ConvertiblePair> getConvertibleTypes() {
                return Collections.singleton(new ConvertiblePair(String.class, Collection.class));
            }

            @Override
            public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
                Class<?> collectType = targetType.getType();

                TypeDescriptor elementDesc = targetType.getElementTypeDescriptor();
                Class<?> elementType = elementDesc != null ? elementDesc.getType() : null;

                if (source instanceof String) {
                    //优先json
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        JavaType type = objectMapper.getTypeFactory().constructCollectionType((Class<? extends Collection>) targetType.getType(), elementType);
                        Object value = objectMapper.readValue((String) source, type);
                        return value;
                    } catch (Exception e) {
                        String trim = ((String) source).trim();
                        if (trim.startsWith("[") && trim.endsWith("]")) {
                            throw new IllegalArgumentException(e);
                        }
                    }
                    //默认
                    String[] fields = StringUtils.split((String) source, ",");
                    Collection<Object> target = CollectionFactory.createCollection(collectType, elementType, fields.length);
                    if (elementType == null) {
                        for (String field : fields) {
                            target.add(field.trim());
                        }
                    } else {
                        for (String field : fields) {
                            Object targetElement = ((ConversionService) registry).convert(field.trim(), sourceType, elementDesc);
                            target.add(targetElement);
                        }
                    }
                    return target;
                }
                return CollectionFactory.createCollection(collectType, elementType, 0);
            }
        });
    }
}
