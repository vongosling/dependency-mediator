package org.apache.component.dependency.plugins;

import java.io.File;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.WriterStreamConsumer;

public class MavenCommandLineMojo extends AbstractMojo {
    /**
     * Project base directory.
     */
    @Parameter(required = true, defaultValue = "${basedir}")
    protected File basedir;
    /**
     * Target directory for class files.
     */
    @Parameter(required = true, defaultValue = "${project.build.directory}")
    private String targetDir;
    /**
     * Project artifact id.
     */
    @Parameter(required = true, defaultValue = "${project.artifactId}")
    private String artifactId;
    /**
     * Project final name.
     */
    @Parameter(required = true, defaultValue = "${project.build.finalName}")
    private String finalName;

    public String getTestDependencyArtifactIds() throws MojoExecutionException {
        StringBuilder result = new StringBuilder();
        Model model = buildProjectModel();
        List<Dependency> dependencies = model.getDependencies();
        for (Dependency dep : dependencies) {
            if ("test".equalsIgnoreCase(dep.getScope())) {
                result.append(dep.getArtifactId()).append(",");
            }
        }
        return result.toString().isEmpty() ? "" : result.toString().substring(0,
                result.length() - 1);
    }

    public Model buildProjectModel() throws MojoExecutionException {
        File pom = new File(this.basedir, "pom.xml");
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model = null;
        try {
            model = pomReader.read(new FileReader(pom));
        } catch (Exception e) {
            throw new MojoExecutionException("Error to load pom: " + e.getMessage(), e);
        }
        if (model == null) {
            throw new MojoExecutionException("Failed to load pom: " + pom.getAbsolutePath());
        }
        return model;
    }

    public void copyDependenciesTest(File dependencyFolder) throws MojoFailureException,
            MojoExecutionException {
        getLog().info("Start to copy dependencies");
        Commandline cl = new Commandline();
        cl.setExecutable("mvn");
        cl.createArg().setValue("clean");
        cl.createArg().setValue("dependency:copy-dependencies");
        cl.createArg().setValue("-DoutputDirectory=" + dependencyFolder.getAbsolutePath());
        cl.createArg().setValue("-Dsilent=true");
        String excludedArtifactIds = this.getTestDependencyArtifactIds();
        if (!excludedArtifactIds.isEmpty()) {
            cl.createArg().setValue("-DexcludeArtifactIds=" + excludedArtifactIds);
            getLog().info("====Excluded artifact ids: " + excludedArtifactIds);
        } else {
            getLog().info("====No excluded artifact ids");
        }
        WriterStreamConsumer systemOut = new WriterStreamConsumer(
                new OutputStreamWriter(System.out));
        int result = -1;
        try {
            result = CommandLineUtils.executeCommandLine(cl, systemOut, systemOut);
        } catch (CommandLineException e) {
            String message = "Failed to execute command: " + cl.toString();
            throw new MojoFailureException(message);
        }
        if (result != 0) {
            getLog().error("Failed to copy dependencies");
            System.exit(result);
        }
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // TODO Auto-generated method stub

    }

}
