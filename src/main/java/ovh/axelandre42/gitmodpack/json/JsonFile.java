package net.axelandre42.gitmodpack.json;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonWriter;

public abstract class JsonFile<T> {
    private File jsonFile;
    
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    public JsonFile(File jsonFile) {
        this.jsonFile = jsonFile;
    }

    protected abstract T loadJson(JsonElement rootElement);
    
    protected abstract JsonElement saveJson(T object);
    
    public T load() throws JsonIOException, JsonSyntaxException, FileNotFoundException {
        JsonElement element = new JsonParser().parse(GSON.newJsonReader(new FileReader(jsonFile)));
        
        return loadJson(element);
    }
    
    public void save(T object) throws JsonIOException, IOException {
        JsonWriter writer = GSON.newJsonWriter(new FileWriter(jsonFile));
        GSON.toJson(saveJson(object), writer);
        writer.flush();
    }
}
