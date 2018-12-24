package com.shawn;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.jar.JarArchiver;

import java.io.File;
import java.util.Dictionary;

/**
 * Created by lxf on 2018/11/10.
 */
@Mojo(name = "sourcejar", defaultPhase = LifecyclePhase.PACKAGE, requiresProject = true, threadSafe = true,
        requiresDependencyResolution = ResolutionScope.RUNTIME)
public class SourceJarMojo extends AbstractMojo {
    /**
     * Directory containing the classes and resource files that should be packaged into the JAR.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private File classesDirectory;
    @Parameter(defaultValue = "${project.build.sourceDirectory}", required = true)
    private File sourceDirectory;

    @Parameter(defaultValue = "${project.build.directory}", required = true)
    private File directory;

    /**
     * Directory containing the generated JAR.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private File outputDirectory;

    /**
     * Name of the generated JAR.
     */
    @Parameter(defaultValue = "${project.build.finalName}", readonly = true)
    private String finalName;
    /**
     * The Jar archiver.
     */
    @Component(role = Archiver.class, hint = "jar")
    private JarArchiver jarArchiver;

    @Parameter(property = "maven.jar.forceCreation", defaultValue = "false")
    private boolean forceCreation;
    /**
     * List of files to include. Specified as fileset patterns which are relative to the input directory whose contents
     * is being packaged into the JAR.
     */
    @Parameter
    private String[] includes;

    /**
     * List of files to exclude. Specified as fileset patterns which are relative to the input directory whose contents
     * is being packaged into the JAR.
     */
    @Parameter
    private String[] excludes;

    /**
     * The {@link {MavenProject}.
     */
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * The {@link MavenSession}.
     */
    @Parameter(defaultValue = "${session}", readonly = true, required = true)
    private MavenSession session;

    /**
     * The archive configuration to use. See <a href="http://maven.apache.org/shared/maven-archiver/index.html">Maven
     * Archiver Reference</a>.
     */
    @Parameter
    private MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    public void execute() throws MojoExecutionException, MojoFailureException {
        File jarFile = createArchive();
        getProject().getArtifact().setFile(jarFile);
    }

    public static void main(String[] args) {

        MavenArchiver archiver = new MavenArchiver();
    }


    public File createArchive()
            throws MojoExecutionException {
        System.out.println(directory.getAbsolutePath());
        System.out.println(finalName);
        File jarFile = getJarFile(directory, finalName);

        MavenArchiver archiver = new MavenArchiver();

        archiver.setArchiver(jarArchiver);

        archiver.setOutputFile(jarFile);
        archive.setForced(forceCreation);
        try {
            File contentDirectory = getClassesDirectory();
            if (!contentDirectory.exists()) {
                getLog().warn("JAR will be empty - no content was marked for inclusion!");
            } else {
                Build build = project.getBuild();
                System.out.println(directory.getAbsolutePath());
                System.out.println(sourceDirectory.getAbsolutePath());
                System.out.println(outputDirectory.getAbsolutePath());
                archiver.getArchiver().addDirectory(outputDirectory, getIncludes(), getExcludes());
                archiver.getArchiver().addDirectory(sourceDirectory, getIncludes(), getExcludes());
            }
            archiver.createArchive(session, project, archive);
            return jarFile;
        } catch (Exception e) {
            // TODO: improve error handling
            throw new MojoExecutionException("Error assembling JAR", e);
        }
    }

    /**
     * Returns the Jar file to generate, based on an optional classifier.
     *
     * @param basedir         the output directory
     * @param resultFinalName the name of the ear file
     * @return the file to generate
     */
    protected File getJarFile(File basedir, String resultFinalName) {
        if (basedir == null) {
            throw new IllegalArgumentException("basedir is not allowed to be null")           ;
        }
        if (resultFinalName == null) {
            throw new IllegalArgumentException("finalName is not allowed to be null");
        }

        StringBuilder fileName = new StringBuilder(resultFinalName);
        fileName.append( ".jar" );

        return new File(basedir, fileName.toString());
    }

    public File getClassesDirectory() {
        return classesDirectory;
    }

    public String[] getIncludes() {
        return includes;
    }

    public void setIncludes(String[] includes) {
        this.includes = includes;
    }

    public String[] getExcludes() {
        return excludes;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }

    public MavenProject getProject() {
        return project;
    }

    public File getDirectory() {
        return directory;
    }
}
