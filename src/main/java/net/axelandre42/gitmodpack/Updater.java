package net.axelandre42.gitmodpack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefComparator;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import net.axelandre42.gitmodpack.json.ManifestJsonFile;
import net.axelandre42.gitmodpack.json.MetadataJsonFile;
import net.axelandre42.gitmodpack.model.Manifest;
import net.axelandre42.gitmodpack.model.Metadata;

public class Updater {

    private static final String DEFAULT_REPOSITORY_NAME = "repository";

    private static Logger logger = LogManager.getLogger();

    private File instanceDir;
    private File repositoryDir;
    private File metadataFile;
    private URL remoteUrl;

    private MetadataJsonFile metaJson;

    private Metadata metadata;
    private Manifest manifest;

    public Updater(File instanceDir, URL remoteUrl, File metadataFile) {
        this.instanceDir = instanceDir;
        this.remoteUrl = remoteUrl;
        this.metadataFile = metadataFile;
    }

    private void createMetadata() {
        this.metadata = new Metadata();
        this.metadata.repositoryName = DEFAULT_REPOSITORY_NAME;

        metaJson = new MetadataJsonFile(metadataFile);

        try {
            this.metadataFile.createNewFile();
            metaJson.save(metadata);
        } catch (JsonIOException | IOException e) {
            throw new RuntimeException("An error occured during metadata creation!", e);
        }
    }

    private void loadMetadata() {
        metaJson = new MetadataJsonFile(metadataFile);

        try {
            metadata = metaJson.load();
        } catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
            throw new RuntimeException("An error occured during metadata loading!", e);
        }
    }

    private void cloneRepository() {
        try {
            Git.cloneRepository().setURI(remoteUrl.toString()).setDirectory(this.repositoryDir).call();
        } catch (GitAPIException e) {
            throw new RuntimeException("An error occured during repository cloning!", e);
        }
    }

    private void pullRepository() {
        try {
            Git.open(repositoryDir).pull().call();
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException("An error occured during repository pulling!", e);
        }
    }

    private boolean checkTags() {
        List<Ref> tags;
        try {
            tags = Git.open(repositoryDir).tagList().call();
        } catch (GitAPIException | IOException e) {
            throw new RuntimeException("An error occured during tag checking!", e);
        }

        tags.sort(RefComparator.INSTANCE);
        tags.forEach(t -> logger.info("Tag found: " + t.getName()));
        Ref lastTag = tags.get(tags.size() - 1);

        if (lastTag.getName() != metadata.currentTag) {
            metadata.currentTag = lastTag.getName();

            try {
                metaJson.save(metadata);
            } catch (JsonIOException | IOException e) {
                throw new RuntimeException("An error occured during metadata save!", e);
            }

            return false;
        }

        return true;
    }

    public boolean check() {
        logger.info("Starting version check.");
        if (this.metadataFile.exists()) {
            logger.info("Found metadata file: " + this.metadataFile);
            this.loadMetadata();
        } else {
            logger.info("Creating metadata file: " + this.metadataFile);
            this.createMetadata();
        }

        this.repositoryDir = new File(this.instanceDir, this.metadata.repositoryName);

        if (this.repositoryDir.exists()) {
            logger.info("Pulling repository.");
            this.pullRepository();
        } else {
            logger.info("Cloning repository.");
            this.cloneRepository();
        }

        logger.info("Checking tags.");
        boolean hasUpdates = !this.checkTags();

        logger.info("Version check finished.");
        return hasUpdates;
    }

    private void loadManifest() {
        ManifestJsonFile manifestLoader = new ManifestJsonFile(new File(repositoryDir, "manifest.json"));

        try {
            manifest = manifestLoader.load();
        } catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
            throw new RuntimeException("An error occured during manifest parsing!", e);
        }
    }

    private String getFileNameFromUrlOrPath(String from) {
        try {
            URL url = new URL(from);
            return new File(url.getPath()).getName();
        } catch (MalformedURLException e) {
            File file = new File(repositoryDir, from);
            return file.getName();
        }
    }

    private void downloadOrMoveFromUrlOrPath(String from, String to) {
        InputStream in;
        try {
            URL url = new URL(from);
            in = url.openStream();
        } catch (MalformedURLException e) {
            File file = new File(repositoryDir, from);
            try {
                in = new FileInputStream(file);
            } catch (FileNotFoundException e1) {
                throw new RuntimeException("An error occured during download initialisation! (File source)", e);
            }
        } catch (IOException e) {
            throw new RuntimeException("An error occured during download initialisation! (URL source)", e);
        }

        File dest = new File(instanceDir, to + File.separator + this.getFileNameFromUrlOrPath(from));
        File destDir = new File(instanceDir, to);
        OutputStream out;
        try {
            destDir.mkdirs();
            dest.createNewFile();
            out = new FileOutputStream(dest);
        } catch (IOException e) {
            throw new RuntimeException("An error occured during download initialisation! (Destination)", e);
        }

        try {
            int len;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.close();
        } catch (IOException e) {
            throw new RuntimeException("An error occured during download!", e);
        }
    }

    private void updateDifferences() {
        List<String> newInst = new ArrayList<String>(metadata.installed);
        metadata.installed.forEach(i -> {
            if (!manifest.rules.stream()
                    .anyMatch(r -> r.to + File.separator + this.getFileNameFromUrlOrPath(r.from) == i)) {
                new File(instanceDir, i).delete();
                newInst.remove(i);
            }
        });
        manifest.rules.forEach(r -> {
            if (!metadata.installed.stream()
                    .anyMatch(i -> r.to + File.separator + this.getFileNameFromUrlOrPath(r.from) == i)) {
                this.downloadOrMoveFromUrlOrPath(r.from, r.to.replace('/', File.separatorChar));
                newInst.add(r.to + File.separator + this.getFileNameFromUrlOrPath(r.from));
            }
        });

        metadata.installed = newInst;

        try {
            metaJson.save(metadata);
        } catch (JsonIOException | IOException e) {
            throw new RuntimeException("An error occured during metadata save!", e);
        }
    }

    public void update() {
        logger.info("Loading manifest.");
        this.loadManifest();

        logger.info("Updating.");
        this.updateDifferences();
    }
}
