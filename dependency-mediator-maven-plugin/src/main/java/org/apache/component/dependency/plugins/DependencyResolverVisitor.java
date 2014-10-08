package org.apache.component.dependency.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor;

/**
 * An implementation of the {@link DependencyNodeVisitor} that resolves
 * dependency conflicts of the nodes it is visiting.
 * 
 * @author <a href="mailto:fengjia10@gmail.com">Von Gosling</a>
 */
public class DependencyResolverVisitor implements DependencyNodeVisitor {
    /** Qualified artifact name to artifact. */
    protected final Map<String, Artifact>       resolvedDependenciesByName;
    protected final Map<String, List<Artifact>> conflictDependencyArtifacts;

    public DependencyResolverVisitor() {
        super();
        this.resolvedDependenciesByName = new TreeMap<String, Artifact>();
        this.conflictDependencyArtifacts = new TreeMap<String, List<Artifact>>();
    }

    @Override
    public boolean endVisit(final DependencyNode node) {
        return true;
    }

    /**
     * Returns the qualified name for an Artifact.
     */
    private String getQualifiedName(Artifact artifact) {
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getType();
    }

    @Override
    public boolean visit(final DependencyNode node) {
        final Artifact at = node.getArtifact();
        final int state = node.getState();
        final String key = getQualifiedName(at);
        switch (state) {
            case DependencyNode.INCLUDED:
                resolvedDependenciesByName.put(key, at);
                break;
            case DependencyNode.OMITTED_FOR_CONFLICT:
                DefaultArtifactVersion dav1 = new DefaultArtifactVersion(node.getArtifact()
                        .getVersion());
                DefaultArtifactVersion dav2 = new DefaultArtifactVersion(node.getRelatedArtifact()
                        .getVersion());
                if (isIncompatible(dav1, dav2)) {
                    if (conflictDependencyArtifacts.containsKey(key)) {
                        conflictDependencyArtifacts.get(key).add(at);
                        conflictDependencyArtifacts.get(key).add(node.getRelatedArtifact());
                    } else {
                        List<Artifact> ats = new ArrayList<Artifact>();
                        ats.add(at);
                        if (!key.equals(getQualifiedName(node.getRelatedArtifact()))) {
                            ats.add(node.getRelatedArtifact());
                        }
                        conflictDependencyArtifacts.put(key, ats);
                    }
                    //isSpecifiedExplicitly(node.getParent(), node.getRelatedArtifact());
                }
            case DependencyNode.OMITTED_FOR_CYCLE:
            case DependencyNode.OMITTED_FOR_DUPLICATE:
            default:
                break;
        }
        return true;
    }

    private boolean isIncompatible(DefaultArtifactVersion dav1, DefaultArtifactVersion dav2) {
        if (dav1.getMajorVersion() > dav2.getMajorVersion()
                || (dav1.getMajorVersion() == dav2.getMajorVersion() && dav1.getMinorVersion() > dav2
                        .getMinorVersion())) {
            return true;
        }
        return false;
    }

    /**
     * @return the resolvedDependenciesByName
     */
    public Map<String, Artifact> getResolvedDependenciesByName() {
        return resolvedDependenciesByName;
    }

    /**
     * @return the conflictDependencyArtifacts
     */
    public Map<String, List<Artifact>> getConflictDependencyArtifacts() {
        return conflictDependencyArtifacts;
    }

    //    private boolean isSpecifiedExplicitly(final DependencyNode node,
    //                                          final Artifact dependencyArtifact) {
    //        for (final DependencyNode child : node.getChildren()) {
    //            if (child.getArtifact().equals(dependencyArtifact)) {
    //                return true;
    //            }
    //        }
    //        return node.getParent() == null ? false : isSpecifiedExplicitly(node.getParent(),
    //                dependencyArtifact);
    //    }

}
