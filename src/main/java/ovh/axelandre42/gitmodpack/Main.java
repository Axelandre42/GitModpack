package ovh.axelandre42.gitmodpack;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.TextProgressMonitor;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import ovh.axelandre42.gitmodpack.utils.LogWriter;
import ovh.axelandre42.gitmodpack.utils.ProgressMonitorSet;

public class Main {

    public static void main(String[] args) {
	Logger logger = LogManager.getLogger();
	OptionParser optParser = new OptionParser();
	optParser.accepts("local-repository", "Git local repository path").withRequiredArg().defaultsTo("repository");
	optParser.accepts("minecraft-root", "Minecraft root path").withRequiredArg().defaultsTo(".");

	OptionSet options = optParser.parse(args);

	RepositoryManager repo = null;

	ProgressMonitorSet monitors = new ProgressMonitorSet();
	monitors.add(new TextProgressMonitor(new LogWriter(logger)));

	try {
	    repo = new RepositoryManager(new URI((String) options.nonOptionArguments().get(0)),
		    new File((String) options.valueOf("minecraft-root"), (String) options.valueOf("local-repository")),
		    monitors);
	} catch (URISyntaxException e) {
	    logger.fatal("Exception thrown during URI parsing. Verify your arguments!");
	    logger.catching(e);
	    System.exit(-1);
	}

	if (!repo.localExists()) {
	    try {
		repo.cloneRepository();
	    } catch (GitAPIException e) {
		logger.fatal("Exception thrown during repository cloning. Check logs!");
		logger.catching(e);
		System.exit(-1);
	    }
	}

    }

}
