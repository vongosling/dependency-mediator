package org.apache.component.dependency.plugins;

import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.Artifact;

/**
 * @author <a href="mailto:fengjia10@gmail.com">Von Gosling</a>s
 */
public interface DependencyResolutionResult {

    Map<String, Artifact> getResolvedDependenciesByName();

    Map<String, List<Artifact>> getConflictDependencyArtifact();
}
