package org.apache.component.dependency.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.codehaus.plexus.util.ReaderFactory;

/**
 * @author Von Gosling
 */
public class ProjectStub extends MavenProjectStub {

    /**
     * Default constructor
     */
    public ProjectStub() {
        MavenXpp3Reader pomReader = new MavenXpp3Reader();
        Model model;
        try {
            model = pomReader.read(ReaderFactory.newXmlReader(new File(getBasedir(), "pom.xml")));
            setModel(model);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Artifact artifact = new ArtifactStub();
        artifact.setArtifactId(model.getArtifactId());
        artifact.setGroupId(model.getGroupId());
        artifact.setVersion(model.getVersion());
        setArtifact(artifact);

        setGroupId(model.getGroupId());
        setArtifactId(model.getArtifactId());
        setVersion(model.getVersion());
        setName(model.getName());
        setUrl(model.getUrl());
        setPackaging(model.getPackaging());

        Build build = new Build();
        build.setFinalName(model.getArtifactId());
        build.setDirectory(getBasedir() + "/target");
        build.setSourceDirectory(getBasedir() + "/src/main/java");
        build.setOutputDirectory(getBasedir() + "/target/classes");
        build.setTestSourceDirectory(getBasedir() + "/src/test/java");
        build.setTestOutputDirectory(getBasedir() + "/target/test-classes");
        setBuild(build);

        List<String> compileSourceRoots = new ArrayList<String>();
        compileSourceRoots.add(getBasedir() + "/src/main/java");
        setCompileSourceRoots(compileSourceRoots);

        List<String> testCompileSourceRoots = new ArrayList<String>();
        testCompileSourceRoots.add(getBasedir() + "/src/test/java");
        setTestCompileSourceRoots(testCompileSourceRoots);
    }

    /** {@inheritDoc} */
    public File getBasedir() {
        return new File(super.getBasedir() + "/src/test/resources/unit/");
    }

}
