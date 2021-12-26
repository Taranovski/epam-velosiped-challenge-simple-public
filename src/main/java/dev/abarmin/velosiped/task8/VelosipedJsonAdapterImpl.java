package dev.abarmin.velosiped.task8;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class VelosipedJsonAdapterImpl implements VelosipedJsonAdapter {
    @Override
    public <T> T parse(String json, Class<T> targetClass) {
        return createRequestFromJsonBody(json, targetClass);
    }

    @Override
    public String writeAsJson(Object instance) {
        return createResponseStringJsonFromObject(instance);
    }

    private String createResponseStringJsonFromObject(Object object) {
        Map<String, Object> stringObjectHashMap = convertObjectIntoMap(object);

        return generateJsonString(stringObjectHashMap);
    }

    private String generateJsonString(Map<String, Object> stringObjectHashMap) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("{");
        boolean shouldAddComma = false;
        for (Map.Entry<String, Object> stringObjectEntry : stringObjectHashMap.entrySet()) {
            if (shouldAddComma) {
                stringBuilder.append(",");
            } else {
                shouldAddComma = true;
            }
            stringBuilder.append('"');
            stringBuilder.append(stringObjectEntry.getKey());
            stringBuilder.append('"');
            stringBuilder.append(':');
            Object value = stringObjectEntry.getValue();
            if (value instanceof Integer) {
                stringBuilder.append(value);
            } else {
                stringBuilder.append('"');
                stringBuilder.append(value);
                stringBuilder.append('"');
            }
        }
        stringBuilder.append("}");

        String toString = stringBuilder.toString();
        return toString;
    }

    private Map<String, Object> convertObjectIntoMap(Object object) {
        try {
            Map<String, Object> stringObjectHashMap = new HashMap<>();
            Class<?> aClass = object.getClass();
            Method[] declaredMethods = aClass.getDeclaredMethods();
            for (Method method : declaredMethods) {
                String name = method.getName();
                if (name.startsWith("get")) {
                    String get = name.replace("get", "");
                    String replace = get.replace("" + get.charAt(0), ("" + get.charAt(0)).toLowerCase());

                    stringObjectHashMap.put(replace, method.invoke(object));
                }
            }
            return stringObjectHashMap;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T createRequestFromJsonBody(String stringJsonBody, Class<T> aClass) {
        Map<String, Object> stringObjectHashMap = getStringObjectMap(stringJsonBody);

        return createObjectAndSetFields(aClass, stringObjectHashMap);
    }

    private <T> T createObjectAndSetFields(Class<T> aClass, Map<String, Object> stringObjectHashMap) {
        try {
            Constructor<T> declaredConstructor = aClass.getDeclaredConstructor();
            T t = declaredConstructor.newInstance();

            for (Map.Entry<String, Object> stringObjectEntry : stringObjectHashMap.entrySet()) {
                String key = stringObjectEntry.getKey();
                Object value = stringObjectEntry.getValue();

                String setterName = "set" + key.replace("" + key.charAt(0), ("" + key.charAt(0)).toUpperCase());

                Method[] declaredMethods = aClass.getDeclaredMethods();
                for (Method method : declaredMethods) {
                    if (setterName.equals(method.getName())) {
                        Class<?>[] parameterTypes = method.getParameterTypes();
                        Class<?> parameterType = parameterTypes[0];
                        if (parameterType == String.class) {
                            method.invoke(t, String.valueOf(value));
                        }
                        if (parameterType == int.class) {
                            method.invoke(t, Integer.parseInt(String.valueOf(value).trim()));
                        }
                    }
                }

            }
            return t;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> getStringObjectMap(String stringJsonBody) {
        Map<String, Object> stringObjectHashMap = new HashMap<>();

        Map<Character, List<Character>> tokenBoundaryToNext = new HashMap<>();
        tokenBoundaryToNext.put('{', asList('"'));
        tokenBoundaryToNext.put('"', asList('"', ':', ','));
        tokenBoundaryToNext.put(':', asList(',', '{', '}'));
        tokenBoundaryToNext.put(',', asList('"'));

        List<Character> expecting = asList('{');
//            Map<String, String> tokenTypeToNext = new HashMap<>();
//            tokenTypeToNext.put("fieldName", "fieldContentLiteral");
//            tokenTypeToNext.put("fieldContentLiteral", "fieldName");
//            String tokenType = "fieldName";

        boolean shouldSkipWhitespace = true;
        boolean startedString = false;
        String fieldName = null;
        String fieldValue = null;

        StringBuilder currentLiteral = new StringBuilder();
        for (char c : stringJsonBody.toCharArray()) {
            if (expecting.contains(c)) {
                if (c == '{') {
                    //do nothing
                }
                if (c == '"') {
                    if (startedString) {
                        startedString = false;
                        shouldSkipWhitespace = true;
                    } else {
                        startedString = true;
                        shouldSkipWhitespace = false;
                    }
                }
                if (c == ':') {
                    fieldName = currentLiteral.toString();
                    currentLiteral = new StringBuilder();
                }
                if (c == ',') {
                    fieldValue = currentLiteral.toString();
                    currentLiteral = new StringBuilder();
                    stringObjectHashMap.put(fieldName, fieldValue);
                }
                if (c == '}') {
                    fieldValue = currentLiteral.toString();
                    currentLiteral = new StringBuilder();
                    stringObjectHashMap.put(fieldName, fieldValue);
                }
                expecting = tokenBoundaryToNext.get(c);
            } else {
                if (shouldSkipWhitespace) {
                    if (c == ' ' || c == '\n' || c == '\r') {
                        //do nothing
                    } else {
                        currentLiteral.append(c);
                    }
                } else {
                    currentLiteral.append(c);
                }
            }
        }
        return stringObjectHashMap;
    }

}
