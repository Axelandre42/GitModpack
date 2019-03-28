package net.axelandre42.gitmodpack.json;

import java.io.File;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.axelandre42.gitmodpack.model.Metadata;

public class MetadataJsonFile extends JsonFile<Metadata> {

    public MetadataJsonFile(File jsonFile) {
        super(jsonFile);
    }

    @Override
    protected Metadata loadJson(JsonElement rootElement) {
        Metadata metadata = new Metadata();
        JsonObject obj = rootElement.getAsJsonObject();
        
        metadata.currentTag = obj.get("currentTag").getAsString();
        metadata.repositoryName = obj.get("repositoryName").getAsString();
        
        obj.get("installed").getAsJsonArray().forEach(i -> metadata.installed.add(i.getAsString()));
        
        return metadata;
    }

    @Override
    protected JsonElement saveJson(Metadata object) {
        JsonObject element = new JsonObject();
        
        element.addProperty("currentTag", object.currentTag);
        element.addProperty("repositoryName", object.repositoryName);
        
        JsonArray array = new JsonArray();
        object.installed.forEach(i -> array.add(i));
        element.add("installed", array);
        
        return element;
    }

}
