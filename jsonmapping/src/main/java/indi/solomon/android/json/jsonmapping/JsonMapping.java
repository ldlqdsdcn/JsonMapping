/*
 * This file is copyrighted Solomon.liu all
 * Solomon , 2015
 */
package indi.solomon.android.json.jsonmapping;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Solomon.liu on 2015-07-13 18:09.
 * Version 1.0
 */
public class JsonMapping {
    private static final String TAG = "JsonUtil";
    private Map<String, JSONObject> cache = new HashMap<String, JSONObject>();


    public <T> T fromJson(JSONObject jsonObject, Class<T> classOfT) {
        cache.clear();
        T obj = null;
        try {
            obj = classOfT.newInstance();
        } catch (InstantiationException e) {
            throw new ObjectInstanceException(e);
        } catch (IllegalAccessException e) {
            throw new ObjectInstanceException(e);
        }

        Field[] fields = classOfT.getDeclaredFields();

        for (Field field : fields) {

            JsonElement jsonElement = field.getAnnotation(JsonElement.class);
            if (jsonElement == null) {
                continue;
            }
            Log.d(TAG, "------>" + field.getName());
            Object value = readObjectValue(jsonObject, field.getType(), jsonElement);
            Log.d(TAG, field.getName() + "=" + value);
            field.setAccessible(true);
            try {
                field.set(obj, value);
            } catch (IllegalAccessException e) {
                throw new ObjectInstanceException("set value to object Error", e);
            }
        }
        return obj;
    }

    /**
     *
     */
    private Object readDeepValue(JSONObject jsonObject, Class fieldClass, JsonElement jsonElement) {
        String[] names = jsonElement.name().split(".");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.length; i++) {

            for (int j = 0; j <= i && j < names.length - 1; j++) {
                if (j > 0) {
                    sb.append(".");
                }
                sb.append(names[j]);

                String nameKey = sb.toString();
                JSONObject cacheObject = cache.get(nameKey);
                if (cacheObject == null) {
                    if (!cacheObject.has(names[i])) {
                        return null;
                    }
                    cacheObject = cacheObject.optJSONObject(names[i]);
                    cache.put(nameKey, cacheObject);
                }
                if (cacheObject == null) {
                    return null;
                } else {
                    cache.put(nameKey, cacheObject);
                }

            }
            JSONObject finalJsonObject = cache.get(sb.toString());
            Object value = readObjectValue(finalJsonObject, fieldClass, jsonElement);
            Log.d("deep_value", jsonElement.name() + "=" + value);
            return value;
        }
        return null;
    }

    /**
     * Read value from JSONArray
     *
     * @param jsonArray
     * @param componentClass
     * @param position
     * @return
     */
    private Object readArrayValue(JSONArray jsonArray, Class componentClass, int position) {
        Object value = null;
        if (Integer.class.equals(componentClass)) {
            int defaultValue = 0;
            value = jsonArray.optInt(position, defaultValue);
        } else if (Long.class.equals(componentClass)) {
            long defaultValue = 0;
            value = jsonArray.optLong(position, defaultValue);
        } else if (Double.class.equals(componentClass)) {
            double defaultValue = 0;
            value = jsonArray.optDouble(position, defaultValue);
        } else if (Boolean.class.equals(componentClass)) {
            boolean defaultValue = false;
            value = jsonArray.optBoolean(position, defaultValue);
        } else if (componentClass.getAnnotation(JsonClass.class) != null) {
            JSONObject childElement = jsonArray.optJSONObject(position);
            if (childElement != null)
                value = fromJson(childElement, componentClass);
        } else if (componentClass.isArray()) {

            JSONArray jarray = jsonArray.optJSONArray(position);
            if (jarray != null) {
                value = Array.newInstance(componentClass.getComponentType(), jarray.length());
                for (int i = 0; i < jarray.length(); i++) {
                    Object v = readArrayValue(jarray, componentClass.getComponentType(), i);
                    Array.set(value, i, v);
                }

            }

        } else if (componentClass.equals(List.class)) {
            JSONArray jarray = jsonArray.optJSONArray(position);
            if (jarray != null) {
                List list = new ArrayList();
                for (int i = 0; i < jarray.length(); i++) {
                    Object v = readArrayValue(jarray, componentClass.getComponentType(), i);
                    list.add(v);
                }

                value = list;
            }
        }
        return value;
    }

    /**
     * Read value from JSONObject
     *
     * @param jsonObject
     * @param fieldClass
     * @param jsonElement
     * @return
     */
    private Object readObjectValue(JSONObject jsonObject, Class fieldClass, JsonElement jsonElement) {
        Object value = null;

        /**
         * like equipment.name
         *      user.role.name
         */

        if (jsonElement.name().indexOf(".") != -1) {
            Log.d(TAG,"deep_value=" + jsonElement.name() + " read deep value");
            value = readDeepValue(jsonObject, fieldClass, jsonElement);
            if (value == null) {
                String defaultValueDefine = jsonElement.defaultValue();
                int defaultValue = 0;
                if (Number.class.isAssignableFrom(fieldClass)) {
                    if (!"".equals(defaultValue)) {
                        value = Integer.valueOf(defaultValueDefine);
                    } else {
                        value = 0;
                    }
                } else if (String.class.equals(fieldClass)) {
                    value = defaultValueDefine;
                } else if (Boolean.class.equals(fieldClass)) {
                    if (!"".equals(defaultValue)) {
                        value = Boolean.valueOf(defaultValueDefine);
                    } else {
                        value = false;
                    }
                } else {
                    value = null;
                }
            }

        } else if (Integer.class.equals(fieldClass)) {
            String defaultValueDefine = jsonElement.defaultValue();
            int defaultValue = 0;
            if (!"".equals(defaultValueDefine)) {
                defaultValue = Integer.valueOf(defaultValueDefine);
            }
            value = jsonObject.optInt(jsonElement.name(), defaultValue);
        } else if (Long.class.equals(fieldClass)) {
            String defaultValueDefine = jsonElement.defaultValue();
            long defaultValue = 0;
            if (!"".equals(defaultValueDefine)) {
                defaultValue = Long.valueOf(defaultValueDefine);
            }
            value = jsonObject.optLong(jsonElement.name(), defaultValue);
        } else if (Double.class.equals(fieldClass)) {
            String defaultValueDefine = jsonElement.defaultValue();
            double defaultValue = 0;
            if (!"".equals(defaultValueDefine)) {
                defaultValue = Double.valueOf(defaultValueDefine);
            }
            value = jsonObject.optDouble(jsonElement.name(), defaultValue);
        } else if (Boolean.class.equals(fieldClass)) {
            String defaultValueDefine = jsonElement.defaultValue();
            boolean defaultValue = false;
            if (!"".equals(defaultValueDefine)) {
                defaultValue = Boolean.valueOf(defaultValueDefine);
            }
            value = jsonObject.optBoolean(jsonElement.name(), defaultValue);
        } else if (String.class.equals(fieldClass)) {
            String defaultValueDefine = jsonElement.defaultValue();
            value = jsonObject.optString(jsonElement.name(), defaultValueDefine);
            if ("null".equals(value)) {
                value = defaultValueDefine;
            }
        } else if (fieldClass.getAnnotation(JsonClass.class) != null) {
            JSONObject childElement = jsonObject.optJSONObject(jsonElement.name());
            if (childElement != null)
                value = fromJson(childElement, fieldClass);
        } else if (fieldClass.isArray()) {

            JSONArray jsonArray = jsonObject.optJSONArray(jsonElement.name());
            if (jsonArray != null) {
                value = Array.newInstance(fieldClass.getComponentType(), jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {
                    Object v = readArrayValue(jsonArray, fieldClass.getComponentType(), i);
                    Array.set(value, i, v);
                }

            }

        } else if (fieldClass.equals(List.class)) {
            JSONArray jsonArray = jsonObject.optJSONArray(jsonElement.name());
            if (jsonArray != null) {
                List list = new ArrayList();
                for (int i = 0; i < jsonArray.length(); i++) {
                    Object v = readArrayValue(jsonArray, fieldClass.getComponentType(), i);
                    list.add(v);
                }

                value = list;
            }
        }
        return value;
    }

    public static class ObjectInstanceException extends RuntimeException {
        public ObjectInstanceException(String message) {
            super(message);
        }

        public ObjectInstanceException(String message, Throwable throwable) {
            super(message, throwable);
        }

        public ObjectInstanceException(Throwable throwable) {
            super(throwable);
        }
    }
}
