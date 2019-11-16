package ovh.axelandre42.gitmodpack;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class Main {
    
    public static void main(String[] args) throws MalformedURLException {
        Updater updater = new Updater(new File(args[0]), new URL(args[1]), new File(args[0] + File.separator + "gitmeta.json"));
        if (updater.check())
            updater.update();
        
    }

}
