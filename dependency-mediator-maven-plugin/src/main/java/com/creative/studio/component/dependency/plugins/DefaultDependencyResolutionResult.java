package com.creative.studio.component.dependency.plugins;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;

/**
 * @author <a href="mailto:fengjia10@gmail.com">Von Gosling</a>
 */
public class DefaultDependencyResolutionResult implements DependencyResolutionResult {

    private final Map<String, Artifact>       resolvedDependenciesByName;
    private final Map<String, List<Artifact>> conflictDependencyArtifact;

    public DefaultDependencyResolutionResult(final Map<String, Artifact> resolvedDependenciesByName,
                                             final Map<String, List<Artifact>> conflictDependencyArtifact) {
        super();
        this.resolvedDependenciesByName = Collections.unmodifiableMap(resolvedDependenciesByName);
        this.conflictDependencyArtifact = Collections.unmodifiableMap(conflictDependencyArtifact);
    }

    /**
     * @return the resolvedDependenciesByName
     */
    public Map<String, Artifact> getResolvedDependenciesByName() {
        return this.resolvedDependenciesByName;
    }

    /**
     * @return the conflictDependencyArtifact
     */
    public Map<String, List<Artifact>> getConflictDependencyArtifact() {
        return this.conflictDependencyArtifact;
    }

}
