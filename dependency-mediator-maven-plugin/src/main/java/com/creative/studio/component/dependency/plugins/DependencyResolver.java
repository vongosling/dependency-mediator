package com.creative.studio.component.dependency.plugins;

import org.apache.maven.shared.dependency.tree.DependencyNode;

/**
 * @author <a href="mailto:fengjia10@gmail.com">Von Gosling</a>
 */
public interface DependencyResolver {

    /**
     * Resolve the dependency conflicts from a dependency tree. The root node of
     * the tree is used as entry point of the tree.
     * 
     * @param rootNode the root node of the dependency tree from which to
     *            resolve the dependency conflicts
     * @return the resolved dependency conflicts
     */
    DependencyResolutionResult resolve(DependencyNode rootNode);

}
