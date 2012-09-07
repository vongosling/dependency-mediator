package org.apache.maven.plugins;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.WriterStreamConsumer;

/**
 * @author von.gosling
 * @goal detect
 */
public class ConflictDetectorMojo extends AbstractMojo {

	/**
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	private String targetDir;

	/**
	 * @parameter expression="${project.artifactId}"
	 * @required
	 */
	private String artifactId;

	private Map<String, JarEntryRecord> jarEntryRecords = new TreeMap<String, JarEntryRecord>();

	private DuplicateClassContainer duplicateContainer = new DuplicateClassContainer();

	/**
	 * @parameter expression="${conflict.skip}"
	 */
	private boolean skip = false;

	/**
	 * @parameter expression="${basedir}"
	 * @required
	 * */
	protected File basedir;

	/**
	 * @parameter expression="${conflict.debug}"
	 */
	private boolean debug = false;

	/**
	 * @parameter expression="${conflict.details}"
	 */
	private boolean printDetails = false;

	/**
	 * @parameter expression="${project.build.finalName}"
	 * @required=true
	 */
	private String finalName;

	/**
	 * 
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		if (skip) {
			getLog().info(artifactId + " conflict checking skipped");
			return;
		}
		checkProjectType();
		File dependencyFolder = new File(new File(new File(this.targetDir),
				this.finalName), "WEB-INF/lib");

		if (!dependencyFolder.exists()) {
			throw new MojoExecutionException(dependencyFolder
					+ " not exits, execute mvn install first");
		}
		File[] jarFiles = getJarFiles(dependencyFolder);
		if (jarFiles == null || jarFiles.length == 0) {
			throw new MojoExecutionException(dependencyFolder
					+ " has no jar files");
		}
		try {
			checkJarFiles(jarFiles);
		} catch (IOException e) {
			String message = "Failed to check jar duplicate for "
					+ this.artifactId;
			getLog().error(message, e);
			throw new MojoExecutionException(message, e);
		}
		printResult();
	}

	/**
	 * @throws MojoExecutionException
	 * 
	 */
	private void checkProjectType() throws MojoExecutionException {
		Model model = this.loadPom();
		String packagingType = model.getPackaging();
		if (!"war".equalsIgnoreCase(packagingType)) {
			throw new MojoExecutionException(
					"only can be used to check war project");
		}
	}

	/**
	 * 
	 * @return
	 * @throws MojoExecutionException
	 */
	public String getTestDependencyArtifactIds() throws MojoExecutionException {
		StringBuilder result = new StringBuilder();
		Model model = loadPom();
		List<Dependency> dependencies = model.getDependencies();
		for (Dependency dep : dependencies) {
			if ("test".equalsIgnoreCase(dep.getScope())) {
				result.append(dep.getArtifactId()).append(",");
			}
		}
		return result.toString().isEmpty() ? "" : result.toString().substring(
				0, result.length() - 1);
	}

	/**
	 * 
	 * @return
	 * @throws MojoExecutionException
	 */
	public Model loadPom() throws MojoExecutionException {
		File pom = new File(this.basedir, "pom.xml");
		Model model = null;
		MavenXpp3Reader pomReader = new MavenXpp3Reader();
		try {
			model = pomReader.read(new FileReader(pom));
		} catch (Exception e) {
			throw new MojoExecutionException("Error to read pom: "
					+ e.getMessage(), e);
		}
		if (model == null) {
			throw new MojoExecutionException("Failed to read pom: "
					+ pom.getAbsolutePath());
		}
		return model;
	}

	/**
	 * @param jarFiles
	 * @throws IOException
	 */
	private void checkJarFiles(File[] jarFiles) throws IOException {
		for (File file : jarFiles) {
			JarFile jarFile = new JarFile(file);
			String fileName = file.getName();
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.toString();
				if (!name.endsWith(".class")) {
					continue;
				}
				JarEntryRecord record = new JarEntryRecord(fileName, name);
				JarEntryRecord duplicate = this.jarEntryRecords.get(name);
				if (duplicate == null) {
					this.jarEntryRecords.put(name, record);
				} else {
					duplicateContainer.add(record).add(duplicate);
				}
			}
		}
	}

	/**
	 * 
	 */
	private void printResult() {
		Set<String> jarFileNames = new HashSet<String>();
		if (this.duplicateContainer.isEmpty()) {
			getLog().info("#################################################");
			getLog().info(
					"#################################################\n\n\n");
			getLog().info(this.artifactId + " has no class conflicts");
			getLog().info(
					"\n\n\n#################################################");
			getLog().info("#################################################");
		} else {
			getLog().info("==================================");
			StringBuilder result = new StringBuilder(
					"\nduplicate class file list: ");
			for (Map.Entry<String, Set<String>> e : this.duplicateContainer.duplicateEntries
					.entrySet()) {
				result.append(e.getKey().replace("/	", ".")).append("\n");
				for (String value : e.getValue()) {
					result.append("\t").append(value).append("\n");
					jarFileNames.add(value);
				}
				result.append("\n");
			}
			if (this.printDetails) {
				getLog().warn(result.toString());
			}
			StringBuilder jarFileResult = new StringBuilder(
					"\nJar files related: \n");
			for (String f : jarFileNames) {
				jarFileResult.append("\t").append(f).append("\n");
			}
			getLog().warn(jarFileResult.toString());
			getLog().info("==================================");
			System.exit(1);
		}
	}

	private static class DuplicateClassContainer {
		final private Map<String, Set<String>> duplicateEntries = new HashMap<String, Set<String>>();

		DuplicateClassContainer add(JarEntryRecord newRecord) {
			Set<String> entry = this.duplicateEntries.get(newRecord.entryName);
			if (entry == null) {
				entry = new HashSet<String>();
				this.duplicateEntries.put(newRecord.entryName, entry);
			}
			entry.add(newRecord.jarFileName);
			return this;
		}

		boolean isEmpty() {
			return this.duplicateEntries.isEmpty();
		}
	}

	/**
	 * @param dependencyFolder
	 * @return
	 */
	private File[] getJarFiles(File dependencyFolder) {
		File[] jarFiles = dependencyFolder.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.trim().toLowerCase().endsWith("jar");
			}
		});
		if (jarFiles == null || jarFiles.length == 0) {
			getLog().info("No dependencies for " + this.artifactId);
			System.exit(0);
		}
		return jarFiles;
	}

	private static final class JarEntryRecord {
		final String jarFileName;

		final String entryName;

		private JarEntryRecord(String jarFileName, String entryName) {
			super();
			this.jarFileName = jarFileName;
			this.entryName = entryName;
		}

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this, true);
		}

		@Override
		public boolean equals(Object obj) {

			return EqualsBuilder.reflectionEquals(this, obj);

		}
	}

	/**
	 * @return
	 */
	public File getDependencyOutputFolder() {
		File targetFolder = new File(this.targetDir);
		File dependencyFolder = new File(targetFolder, "dependency");
		return dependencyFolder;
	}

	/**
	 * @throws MojoFailureException
	 * @throws MojoExecutionException
	 */
	public void copyDependencies(File dependencyFolder)
			throws MojoFailureException, MojoExecutionException {
		getLog().info("start to copy dependencies");
		Commandline cl = new Commandline();
		cl.setExecutable("mvn");
		cl.createArg().setValue("clean");
		cl.createArg().setValue("dependency:copy-dependencies");
		cl.createArg().setValue(
				"-DoutputDirectory=" + dependencyFolder.getAbsolutePath());
		cl.createArg().setValue("-Dsilent=true");
		String excludedArtifactIds = this.getTestDependencyArtifactIds();
		if (!excludedArtifactIds.isEmpty()) {
			cl.createArg().setValue(
					"-DexcludeArtifactIds=" + excludedArtifactIds);
			if (debug) {
				getLog().info(
						"====excluded artifact ids: " + excludedArtifactIds);
			}
		} else {
			if (debug) {
				getLog().info("====No excluded artifact ids");
			}
		}
		WriterStreamConsumer systemOut = new WriterStreamConsumer(
				new OutputStreamWriter(System.out));
		int result = -1;
		try {
			result = CommandLineUtils.executeCommandLine(cl, systemOut,
					systemOut);
		} catch (CommandLineException e) {
			String message = "Failed to execute command: " + cl.toString();
			throw new MojoFailureException(message);
		}
		if (result != 0) {
			getLog().error("failed to copy dependencies");
			System.exit(result);
		}
	}

}
