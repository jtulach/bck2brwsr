/**
 * Back 2 Browser Bytecode Translator
 * Copyright (C) 2012-2018 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://opensource.org/licenses/GPL-2.0.
 */
package org.apidesign.bck2brwsr.mojo;

import java.io.File;
import java.util.Collection;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apidesign.vm4brwsr.ObfuscationLevel;

/**
 *
 * @author Jaroslav Tulach
 * @since 0.9
 */
@Mojo(name = "aot",
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
    defaultPhase = LifecyclePhase.PACKAGE
)
public class AheadOfTime extends AbstractMojo {
    private static final String GROUPID = "org.apidesign.bck2brwsr";
    
    @Parameter(defaultValue = "${project}")
    private MavenProject prj;
    
    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.jar")
    private File mainJar;

    @Parameter(defaultValue = "${project.build.directory}/${project.build.finalName}.js")
    private File mainJavaScript;
    
    @Parameter
    private String[] exports;
    
    /**
     * Directory where to generate ahead-of-time JavaScript files for
     * required libraries.
     */
    @Parameter(defaultValue = "lib")
    private String classPathPrefix;

    /** Root JavaScript file to generate */
    @Parameter(defaultValue="${project.build.directory}/bck2brwsr.js")
    private File vm;
    
    @Parameter(defaultValue = "true")
    private boolean generateAotLibraries;
    
    @Parameter(defaultValue = "true")
    private boolean ignoreBootClassPath;

    @Component
    private ArtifactResolver artifactResolver;

    @Component
    private ArtifactFactory artifactFactory;

    @Parameter(required=true, readonly=true, property="localRepository")
    private ArtifactRepository localRepository;

    /**
     * The obfuscation level for the generated JavaScript file.
     *
     * @since 0.5
     */
    @Parameter(defaultValue = "NONE")
    private ObfuscationLevel obfuscation;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Set<Artifact> artifacts = prj.getArtifacts();
        boolean foundEmul = false;
        for (Artifact a : artifacts) {
            if (
                GROUPID.equals(a.getGroupId()) &&
                a.getArtifactId() != null &&
                a.getArtifactId().contains("emul")
            ) {
                foundEmul = true;
                break;
            }
        }
        if (!foundEmul) {

            Artifact rt = artifactFactory.createDependencyArtifact(
                GROUPID,
                "emul",
                VersionRange.createFromVersion(UtilBase.findOwnVersion()),
                "jar",
                "rt",
                "runtime"
            );
            try {
                artifactResolver.resolve(rt, prj.getRemoteArtifactRepositories(), localRepository);
            } catch (ArtifactResolutionException | ArtifactNotFoundException ex) {
                throw new MojoExecutionException("Cannot resolve " + rt, ex);
            }
            artifacts.add(rt);

            Artifact bck2brwsrRt = artifactFactory.createDependencyArtifact(
                GROUPID,
                "emul",
                VersionRange.createFromVersion(UtilBase.findOwnVersion()),
                "jar",
                "bck2brwsr",
                "provided"
            );
            try {
                artifactResolver.resolve(bck2brwsrRt, prj.getRemoteArtifactRepositories(), localRepository);
            } catch (ArtifactResolutionException | ArtifactNotFoundException ex) {
                throw new MojoExecutionException("Cannot resolve " + bck2brwsrRt, ex);
            }
            artifacts.add(bck2brwsrRt);
        }
        System.err.println("artifacts: " + artifacts);

        class Work extends AheadOfTimeBase<Artifact> {
            @Override
            protected File mainJavaScript() {
                return mainJavaScript;
            }

            @Override
            protected String classPathPrefix() {
                return classPathPrefix;
            }

            @Override
            protected ObfuscationLevel obfuscation() {
                return obfuscation;
            }

            @Override
            protected String[] exports() {
                return exports;
            }

            @Override
            protected boolean ignoreBootClassPath() {
                return ignoreBootClassPath;
            }

            @Override
            protected boolean generateAotLibraries() {
                return generateAotLibraries;
            }

            @Override
            protected File mainJar() {
                return mainJar;
            }

            @Override
            protected File vm() {
                return vm;
            }

            @Override
            protected Collection<Artifact> artifacts() {
                return artifacts;
            }

            @Override
            protected void logInfo(String msg) {
                getLog().info(msg);
            }

            @Override
            protected Exception failure(String msg, Throwable cause) {
                if (cause != null) {
                    return new MojoFailureException(msg, cause);
                } else {
                    return new MojoExecutionException(msg);
                }
            }

            @Override
            protected File file(Artifact a) {
                return a.getFile();
            }

            @Override
            protected Scope scope(Artifact a) {
                if ("provided".equals(a.getScope())) {
                    return Scope.PROVIDED;
                }
                return Scope.RUNTIME;
            }

            @Override
            protected String classifier(Artifact a) {
                return a.getClassifier();
            }

            @Override
            protected String artifactId(Artifact a) {
                return a.getArtifactId();
            }

            @Override
            protected String groupId(Artifact a) {
                return a.getGroupId();
            }

            @Override
            protected String version(Artifact a) {
                return a.getVersion();
            }
        }
        new Work().work();
    }
}
