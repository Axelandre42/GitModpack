package ovh.axelandre42.gitmodpack.json;

import java.io.File;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ovh.axelandre42.gitmodpack.model.Manifest;
import ovh.axelandre42.gitmodpack.model.Rule;

public final class ManifestJsonFile extends JsonFile<Manifest> {

    public ManifestJsonFile(File jsonFile) {
        super(jsonFile);
    }

    @Override
    protected Manifest loadJson(JsonElement rootElement) {
        Manifest manifest = new Manifest();
        
        rootElement.getAsJsonObject().get("rules").getAsJsonArray()
                .forEach(rule -> manifest.rules.add(new Rule(
                rule.getAsJsonObject().get("from").getAsString(),
                rule.getAsJsonObject().get("to").getAsString())));
        
        return manifest;
    }

    @Override
    protected JsonElement saveJson(Manifest object) {
        JsonObject element = new JsonObject();
        JsonArray array = new JsonArray();
        
        object.rules.forEach(rule -> {
            JsonObject jObj = new JsonObject();
            jObj.addProperty("from", rule.from);
            jObj.addProperty("to", rule.to);
            array.add(jObj);
        });
        
        element.add("rules", array);
        return element;
    }

}
