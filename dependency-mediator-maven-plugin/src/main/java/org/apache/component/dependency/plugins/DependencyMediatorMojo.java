package org.apache.component.dependency.plugins;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import org.apache.component.dependency.ComponentContainer;
import org.apache.component.dependency.ComponentEntry;
import org.apache.component.dependency.ComponentFormat;
import org.apache.component.dependency.DependencyMediator;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;

/**
 * @author <a href="mailto:fengjia10@gmail.com">Von Gosling</a>
 * @see <a
 *      href="http://maven.apache.org/developers/mojo-api-specification.html">mojo-api-specification</a>
 */
@Mojo(name = "check", threadSafe = true, requiresDependencyResolution = ResolutionScope.TEST, defaultPhase = LifecyclePhase.VERIFY)
public class DependencyMediatorMojo extends AbstractMojo {
    /**
     * The Maven project.
     */
    @Component
    private MavenProject          project;
    /**
     * The dependency tree builder to use for verbose output.
     */
    @Component
    private DependencyTreeBuilder dependencyTreeBuilder;
    /**
     * Local Repository.
     */
    @Parameter(defaultValue = "${localRepository}", readonly = true, required = true)
    private ArtifactRepository    localRepository;
    /**
     * Skip plugin execution completely.
     */
    @Parameter(property = "skip", defaultValue = "false")
    private boolean               skip;
    /**
     * Whether report class incompatible details
     */
    @Parameter(property = "printIncompatibleDetails", defaultValue = "true")
    private boolean               printIncompatibleDetails;
    /**
     * The scope to filter by when resolving the dependency tree, or
     * <code>null</code> to include dependencies from all scopes. Note that this
     * feature does not currently work due to MNG-3236.
     * 
     * @see <a href="http://jira.codehaus.org/browse/MNG-3236">MNG-3236</a>
     * @since 2.0-alpha-5
     */
    @Parameter(property = "scope")
    private String                scope;
    /**
     * A comma-separated list of artifacts to filter the serialized dependency
     * tree by, or <code>null</code> not to filter the dependency tree. The
     * filter syntax is:
     * 
     * <pre>
     * [groupId]:[artifactId]:[type]:[version]
     * </pre>
     * 
     * where each pattern segment is optional and supports full and partial
     * <code>*</code> wildcards. An empty pattern segment is treated as an
     * implicit wildcard.
     * <p>
     * For example, <code>org.apache.*</code> will match all artifacts whose
     * group id starts with <code>org.apache.</code>, and
     * <code>:::*-SNAPSHOT</code> will match all snapshot artifacts.
     * </p>
     * 
     * @see StrictPatternIncludesArtifactFilter
     * @since 2.0-alpha-6
     */
    @Parameter(property = "includes")
    private String                includes;

    /**
     * A comma-separated list of artifacts to filter from the serialized
     * dependency tree, or <code>null</code> not to filter any artifacts from
     * the dependency tree. The filter syntax is:
     * 
     * <pre>
     * [groupId]:[artifactId]:[type]:[version]
     * </pre>
     * 
     * where each pattern segment is optional and supports full and partial
     * <code>*</code> wildcards. An empty pattern segment is treated as an
     * implicit wildcard.
     * <p>
     * For example, <code>org.apache.*</code> will match all artifacts whose
     * group id starts with <code>org.apache.</code>, and
     * <code>:::*-SNAPSHOT</code> will match all snapshot artifacts.
     * </p>
     * 
     * @see StrictPatternExcludesArtifactFilter
     * @since 2.0-alpha-6
     */
    @Parameter(property = "excludes")
    private String                excludes;
    /**
     * Determines whether or not to abort the build when encountering an error
     * dependency checking.
     */
    @Parameter(defaultValue = "true")
    private boolean               failOnError;

    public boolean isSkip() {
        return skip;
    }

    /**
     * Gets the artifact filter to use when resolving the dependency.
     * 
     * @return the artifact filter
     */
    private ArtifactFilter createResolvingArtifactFilter() {
        // add filters in well known order, least specific to most specific
        ArtifactFilter filter = null;

        // filter scope
        if (scope != null) {
            getLog().debug("+ Resolving dependency tree for scope '" + scope + "'");

            filter = new ScopeArtifactFilter(scope);
        } else {
            filter = null;
        }

        //        FilterArtifacts filters = new FilterArtifacts();
        //
        //        filters.addFilter( new ProjectTransitivityFilter( project.getDependencyArtifacts(), this.excludeTransitive ) );
        //
        //        filters.addFilter( new ScopeFilter( DependencyUtil.cleanToBeTokenizedString( this.includeScope ),
        //                                           DependencyUtil.cleanToBeTokenizedString( this.excludeScope ) ) );
        //
        //        filters.addFilter( new TypeFilter( DependencyUtil.cleanToBeTokenizedString( this.includeTypes ),
        //                                          DependencyUtil.cleanToBeTokenizedString( this.excludeTypes ) ) );
        //
        //        filters.addFilter( new ClassifierFilter( DependencyUtil.cleanToBeTokenizedString( this.includeClassifiers ),
        //                                                DependencyUtil.cleanToBeTokenizedString( this.excludeClassifiers ) ) );
        //
        //        filters.addFilter( new GroupIdFilter( DependencyUtil.cleanToBeTokenizedString( this.includeGroupIds ),
        //                                             DependencyUtil.cleanToBeTokenizedString( this.excludeGroupIds ) ) );
        //
        //        filters.addFilter( new ArtifactIdFilter( DependencyUtil.cleanToBeTokenizedString( this.includeArtifactIds ),
        //                                                DependencyUtil.cleanToBeTokenizedString( this.excludeArtifactIds ) ) );

        return filter;
    }

    //    /**
    //     * Gets the dependency node filter to use when serializing the dependency
    //     * graph.
    //     * 
    //     * @return the dependency node filter, or <code>null</code> if none required
    //     */
    //    private DependencyNodeFilter createDependencyNodeFilter() {
    //        List<DependencyNodeFilter> filters = new ArrayList<DependencyNodeFilter>();
    //
    //        // filter includes
    //        if (includes != null) {
    //            List<String> patterns = Arrays.asList(includes.split(","));
    //
    //            getLog().debug("+ Filtering dependency tree by artifact include patterns: " + patterns);
    //
    //            ArtifactFilter artifactFilter = new StrictPatternIncludesArtifactFilter(patterns);
    //            filters.add(new ArtifactDependencyNodeFilter(artifactFilter));
    //        }
    //
    //        // filter excludes
    //        if (excludes != null) {
    //            List<String> patterns = Arrays.asList(excludes.split(","));
    //
    //            getLog().debug("+ Filtering dependency tree by artifact exclude patterns: " + patterns);
    //
    //            ArtifactFilter artifactFilter = new StrictPatternExcludesArtifactFilter(patterns);
    //            filters.add(new ArtifactDependencyNodeFilter(artifactFilter));
    //        }
    //
    //        return filters.isEmpty() ? null : new AndDependencyNodeFilter(filters);
    //    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (isSkip()) {
            getLog().info(project.getArtifactId() + " skipped compatible checking");
            return;
        }
        doExecute();
    }

    private void processPackage() throws MojoExecutionException {
        try {
            //Limit the transitivity of a dependency, and also to affect the classpath used for various build tasks.
            ArtifactFilter artifactFilter = createResolvingArtifactFilter();

            DependencyResolver dependencyResolver = new DefaultDependencyResolver();
            DependencyNode rootNode = dependencyTreeBuilder.buildDependencyTree(project,
                    localRepository, artifactFilter);

            DependencyResolutionResult drr = dependencyResolver.resolve(rootNode);

            Map<String, List<Artifact>> conflictDependencyArtifact = drr
                    .getConflictDependencyArtifact();
            Map<String, Artifact> results = drr.getResolvedDependenciesByName();
            if (!conflictDependencyArtifact.isEmpty()) {
                Iterator<Entry<String, List<Artifact>>> iter = conflictDependencyArtifact
                        .entrySet().iterator();
                while (iter.hasNext()) {
                    Entry<String, List<Artifact>> conflictEntries = iter.next();
                    StringBuilder sb = new StringBuilder("Founded conflict dependency components:");
                    List<Artifact> conflictArtifacts = conflictEntries.getValue();
                    sb.append(conflictEntries.getKey())
                            .append("\n Resolved version is "
                                    + results.get(conflictEntries.getKey()))
                            .append("\n But found conflict artifact ");
                    for (Artifact at : conflictArtifacts) {
                        sb.append(String.format("%s:%s:%s,", at.getGroupId(), at.getArtifactId(),
                                at.getVersion()));
                    }
                    getLog().warn(sb.subSequence(0, sb.length() - 1));
                }
            }
        } catch (DependencyTreeBuilderException e) {
            throw new MojoExecutionException("Cannot build project dependency ", e);
        }
    }

    private void doExecute() throws MojoExecutionException {
        Model model = project.getModel();
        String packagingType = model.getPackaging();
        if (ComponentFormat.WAR.getValue().equalsIgnoreCase(packagingType)) {
            processWarPackage();
            printResult();
        } else {
            processPackage();
        }
    }

    private void processWarPackage() throws MojoExecutionException {
        File dependencyFolder = new File(new File(new File(project.getBuild().getDirectory()),
                project.getBuild().getFinalName()), "WEB-INF/lib");
        if (!dependencyFolder.exists()) {
            throw new MojoExecutionException(dependencyFolder
                    + " not exits, please execute mvn install first");
        }
        File[] jarFiles = listFiles(dependencyFolder);
        if (jarFiles == null || jarFiles.length == 0) {
            getLog().info("No dependencies for " + project.getArtifactId());
            System.exit(0);
        }
        try {
            processJarFiles(jarFiles);
        } catch (IOException e) {
            String message = "Failed to process  " + project.getArtifactId();
            getLog().error(message, e);
            throw new MojoExecutionException(message, e);
        }

    }

    private void processJarFiles(File[] jarFiles) throws IOException {
        for (File file : jarFiles) {
            DependencyMediator.processJarFile(file, true);
        }
    }

    private void printResult() {
        getLog().info("Output component reactor info......");
        int count = 0;
        for (Entry<String, TreeSet<ComponentEntry>> entry : ComponentContainer.compMaps.entrySet()) {
            if (entry.getValue().size() > 1) {
                count++;
                getLog().warn(
                        String.format("Duplicated component  [%s] was founded in the  path : \n",
                                entry.getKey()));
                for (ComponentEntry jar : entry.getValue()) {
                    getLog().warn(String.format(" \t%s\n", jar.getPathName()));
                }
            }
        }
        if (count == 0) {
            getLog().info("Congratulations,no component conflict or incompatible !");
        }
    }

    private File[] listFiles(File dependencyFolder) {
        return dependencyFolder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return DependencyMediator.JAR_FILE_PATTERN.matcher(name).matches();
            }
        });
    }

}
