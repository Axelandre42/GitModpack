package ovh.axelandre42.gitmodpack.utils;

import java.util.HashSet;

import org.eclipse.jgit.lib.ProgressMonitor;

public class ProgressMonitorSet extends HashSet<ProgressMonitor> implements ProgressMonitor {

    private static final long serialVersionUID = 2968777473637479151L;

    @Override
    public void start(int totalTasks) {
	this.forEach(m -> m.start(totalTasks));
    }

    @Override
    public void beginTask(String title, int totalWork) {
	this.forEach(m -> m.beginTask(title, totalWork));
    }

    @Override
    public void update(int completed) {
	this.forEach(m -> m.update(completed));
    }

    @Override
    public void endTask() {
	this.forEach(m -> m.endTask());
    }

    @Override
    public boolean isCancelled() {
	return this.stream().anyMatch(m -> m.isCancelled());
    }

}
