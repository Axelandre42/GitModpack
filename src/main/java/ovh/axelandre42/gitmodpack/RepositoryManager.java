package ovh.axelandre42.gitmodpack;

import java.io.File;
import java.net.URI;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.NullProgressMonitor;
import org.eclipse.jgit.lib.ProgressMonitor;

public class RepositoryManager {
    private URI remote;
    private File local;
    private ProgressMonitor monitor;

    public RepositoryManager(URI remote, File local) {
	this(remote, local, NullProgressMonitor.INSTANCE);
    }

    public RepositoryManager(URI remote, File local, ProgressMonitor monitor) {
	this.remote = remote;
	this.local = local;
	this.monitor = monitor;
    }

    public boolean localExists() {
	return local.exists();
    }

    public void cloneRepository() throws InvalidRemoteException, TransportException, GitAPIException {
	Git.cloneRepository().setProgressMonitor(monitor).setDirectory(local).setURI(remote.toString()).call();
    }

}
