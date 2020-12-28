package com;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ObjectToJsonConverter {
    public String convertToJson(Object object) throws JsonSerializationException {
        try {
            checkIfSerializable(object);
            initializeObject(object);
            return getJsonString(object);
        } catch (Exception e) {
            throw new JsonSerializationException(e.getMessage());
        }
    }

    // So far, we have seen how to create custom annotations and how to use them to decorate the Person class.
    // Now, we're going to see how to take advantage of them by using Java's Reflection API.
    // The first step will be to check whether our object is null or not, as well as whether its type has the @JsonSerializable annotation or not:
    private void checkIfSerializable(Object object) {
        if (Objects.isNull(object)) {
            throw new JsonSerializationException("Can't serialize a null object");
        }
        Class<?> clazz = object.getClass();
        if (!clazz.isAnnotationPresent(JsonSerializable.class)) {
            throw new JsonSerializationException("The class " + clazz.getSimpleName() + " is not annotated with JsonSerializable");
        }
    }

    // Then, we look for any method with @Init annotation, and we execute it to initialize our object's fields:
    // The call of method.setAccessible(true) allows us to execute the private initNames() method.
    private void initializeObject(Object object) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class<?> clazz = object.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Init.class)) {
                method.setAccessible(true);
                method.invoke(object);
            }
        }
    }

    // After the initialization, we iterate over our object's fields, retrieve the key and value of JSON elements,
    // and put them in a map. Then, we create the JSON string from the map:
    private String getJsonString(Object object) throws IllegalArgumentException, IllegalAccessException {
        Class<?> clazz = object.getClass();
        Map<String, String> jsonElementsMap = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(JsonElement.class)) {
                jsonElementsMap.put(getKey(field), (String) field.get(object));
            }
        }
        String jsonString = jsonElementsMap.entrySet()
                .stream()
                .map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
                .collect(Collectors.joining(","));
        return "{" + jsonString + "}";
    }

    private String getKey(Field field) {
        String value = field.getAnnotation(JsonElement.class)
                .key();
        return value.isEmpty() ? field.getName() : value;
    }
}