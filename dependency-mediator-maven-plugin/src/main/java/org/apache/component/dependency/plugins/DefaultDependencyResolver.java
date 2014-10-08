package org.apache.component.dependency.plugins;

import org.apache.maven.shared.dependency.tree.DependencyNode;

/**
 * An implementation of the {@link DependencyResolver} that uses a
 * {@link DependencyResolverVisitor} to visit the nodes in order to resolve the
 * dependency conflicts.
 * 
 * @author <a href="mailto:fengjia10@gmail.com">Von Gosling</a>
 */
public class DefaultDependencyResolver implements DependencyResolver {

    /**
     * Construct a <code>DefaultDependencyConflictsResolver<code>.
     */
    public DefaultDependencyResolver() {
        super();
    }

    @Override
    public DependencyResolutionResult resolve(final DependencyNode rootNode) {
        DependencyResolverVisitor nodeVisitor = new DependencyResolverVisitor();
        rootNode.accept(nodeVisitor);
        return new DefaultDependencyResolutionResult(nodeVisitor.getResolvedDependenciesByName(),
                nodeVisitor.getConflictDependencyArtifacts());
    }
}
