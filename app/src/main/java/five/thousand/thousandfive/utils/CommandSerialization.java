package five.thousand.thousandfive.utils;

import android.util.Log;

import com.esotericsoftware.jsonbeans.Json;
import com.esotericsoftware.kryonet.JsonSerialization;

import java.lang.reflect.Field;

public class CommandSerialization extends JsonSerialization {
    private Json parentJson;
    public CommandSerialization() {
        super();

        parentJson = (Json) reflectObject(reflectField(JsonSerialization.class, "json"), this);

        try {
            reflectField(Json.class, "typeName").set(parentJson, "command");
        } catch (IllegalAccessException e) {
            Log.e("CommandSerialization", "Couldn't set json key to 'command' instead of 'class'"); //Shouldn't happen without all builds failing
        }
    }

    public void register(Class cls) {
        parentJson.addClassTag(cls.getSimpleName().toLowerCase(), cls);
    }

    private Object reflectObject(Field field, Object obj) {
        try {
            return field.get(obj);
        } catch (IllegalAccessException e) {
            Log.e("CommandSerialization", "Couldn't extract field " + field.getName() + " from object of " + obj.getClass().getSimpleName());
            return null; // Yes, crash the app with a NullPointerException
        }
    }
    private Field reflectField(Class cls, String fieldName) {
        Field field;
        try {
            field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            Log.e("CommandSerialization", "Couldn't reflect field " + fieldName + " out of class " + cls.getSimpleName());
            return null; // Yes, crash the app with a NullPointerException
        }
    }
}