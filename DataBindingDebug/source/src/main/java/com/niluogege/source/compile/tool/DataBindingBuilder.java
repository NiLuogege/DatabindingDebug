/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.databinding.tool;

import android.databinding.tool.processing.Scope;
import android.databinding.tool.processing.ScopedException;
import android.databinding.tool.util.L;
import android.databinding.tool.util.Preconditions;
import android.databinding.tool.writer.JavaFileWriter;

import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * This class is used by Android Gradle plugin.
 */
@SuppressWarnings("unused")
public class DataBindingBuilder {
    Versions mVersions;
    private static final List<String> EXCLUDE_PATTERNS =
            Arrays.asList("android/databinding/layouts/*.*",
                    "androidx/databinding/layouts/*.*");
    public static final String PROCESSOR_NAME =
            "android.databinding.annotationprocessor.ProcessDataBinding";

    public static final String ARTIFACT_BASE_CLASSES_DIR_FROM_LIBS = "dependent-lib-base-classes";
    public static final String INCREMENTAL_BIN_AAR_DIR = "bin-files";
    public static final String INCREMENTAL_BINDING_CLASSES_LIST_DIR = "binding-class-list";
    public static final String DATA_BINDING_ROOT_FOLDER_IN_AAR = "data-binding";
    public static final String DATA_BINDING_CLASS_LOG_ROOT_FOLDER_IN_AAR =
            "data-binding-base-class-log";
    public static final String BR_FILE_EXT = "-br.bin";
    public static final String LAYOUT_INFO_FILE_EXT = "-layoutinfo.bin";
    private static final String SETTER_STORE_FILE_EXT = "-setter_store.bin";
    private static final String SETTER_STORE_JSON_FILE_EXT = "-setter_store.json";
    public static final List<String> RESOURCE_FILE_EXTENSIONS =
            ImmutableList.of(BR_FILE_EXT, LAYOUT_INFO_FILE_EXT, SETTER_STORE_FILE_EXT,
                    SETTER_STORE_JSON_FILE_EXT);
    public static final String BINDING_CLASS_LIST_SUFFIX = "-binding_classes.json";
    public static final String FEATURE_PACKAGE_LIST_FILE_NAME = "all_features.json";
    public static final String FEATURE_BR_OFFSET_FILE_NAME = "feature_data.json";

    public String getCompilerVersion() {
        return getVersions().compiler;
    }

    public String getBaseLibraryVersion(String compilerVersion) {
        return getVersions().baseLibrary;
    }

    public String getLibraryVersion(String compilerVersion) {
        return getVersions().extensions;
    }

    public String getBaseAdaptersVersion(String compilerVersion) {
        return getVersions().extensions;
    }

    public void setPrintMachineReadableOutput(boolean machineReadableOutput) {
        ScopedException.encodeOutput(machineReadableOutput);
    }

    public boolean getPrintMachineReadableOutput() {
        return ScopedException.isEncodeOutput();
    }

    public void setDebugLogEnabled(boolean enableDebugLogs) {
        L.setDebugLog(enableDebugLogs);
    }

    public boolean isDebugLogEnabled() {
        return L.isDebugEnabled();
    }

    private Versions getVersions() {
        if (mVersions != null) {
            return mVersions;
        }
        try {
            Properties props = new Properties();
            InputStream stream = getClass().getResourceAsStream("/data_binding_version_info.properties");
            try {
                props.load(stream);
                mVersions = new Versions(props);
            } finally {
                IOUtils.closeQuietly(stream);
            }
        } catch (IOException exception) {
            L.e(exception, "Cannot read data binding version");
        }
        return mVersions;
    }

    /**
     * Returns the list of classes that should be excluded from the packaging task.
     *
     * @param layoutXmlProcessor The LayoutXmlProcessor for this variant
     * @param generatedClassListFile The location of the File into which data binding compiler wrote
     *                               list of generated classes
     * @param dataBindingCompilerBuildFolder the build folder for the data binding compiler
     * @return The list of classes to exclude. They are already in JNI format.
     */
    public List<String> getJarExcludeList(LayoutXmlProcessor layoutXmlProcessor,
            File generatedClassListFile, File dataBindingCompilerBuildFolder) {
        List<String> excludes = new ArrayList<>();
        String infoClassAsFile = layoutXmlProcessor.getInfoClassFullName().replace('.', '/');
        excludes.add(infoClassAsFile + ".class");
        excludes.addAll(EXCLUDE_PATTERNS);
        excludes.add(layoutXmlProcessor.getPackage().replace('.', '/') + "/BR.*");
        for (String pkg : getBRFilePackages(dataBindingCompilerBuildFolder)) {
            excludes.add(pkg.replace('.', '/') + "/BR.*");
        }
        if (generatedClassListFile != null) {
            List<String> generatedClasses = readGeneratedClasses(generatedClassListFile);
            for (String klass : generatedClasses) {
                excludes.add(klass.replace('.', '/') + ".class");
            }
        }
        Scope.assertNoError();
        return excludes;
    }

    private static List<String> getBRFilePackages(File dependencyArtifactsDir) {
        List<String> packages = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(
                dependencyArtifactsDir.toPath())) {
            for (Path path : directoryStream) {
                String filename = path.getFileName().toString();
                if (filename.endsWith("-br.bin")) {
                    packages.add(filename.substring(0, filename.indexOf('-')));
                }
            }
        } catch (IOException e) {
            L.e(e, "Error reading contents of %s directory", dependencyArtifactsDir);
        }
        return packages;
    }

    private List<String> readGeneratedClasses(File generatedClassListFile) {
        Preconditions.checkNotNull(generatedClassListFile,
                "Data binding exclude generated task is not configured properly");
        Preconditions.check(generatedClassListFile.exists(),
                "Generated class list does not exist %s", generatedClassListFile.getAbsolutePath());
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(generatedClassListFile);
            return IOUtils.readLines(fis);
        } catch (FileNotFoundException e) {
            L.e(e, "Unable to read generated class list from %s",
                    generatedClassListFile.getAbsoluteFile());
        } catch (IOException e) {
            L.e(e, "Unexpected exception while reading %s",
                    generatedClassListFile.getAbsoluteFile());
        } finally {
            IOUtils.closeQuietly(fis);
        }
        L.e("Could not read data binding generated class list");
        return null;
    }

    public JavaFileWriter createJavaFileWriter(File outFolder) {
        return new GradleFileWriter(outFolder.getAbsolutePath());
    }

    public static class GradleFileWriter extends JavaFileWriter {

        private final String outputBase;

        public GradleFileWriter(String outputBase) {
            this.outputBase = outputBase;
        }

        @Override
        public void writeToFile(String canonicalName, String contents) {
            File f = toFile(canonicalName);
            //noinspection ResultOfMethodCallIgnored
            f.getParentFile().mkdirs();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                IOUtils.write(contents, fos);
            } catch (IOException e) {
                L.e(e, "cannot write file " + f.getAbsolutePath());
            } finally {
                IOUtils.closeQuietly(fos);
            }
        }

        private File toFile(String canonicalName) {
            String asPath = canonicalName.replace('.', File.separatorChar);
            return new File(outputBase + File.separatorChar + asPath + ".java");
        }

        @Override
        public void deleteFile(String canonicalName) {
            FileUtils.deleteQuietly(toFile(canonicalName));
        }
    }

    private static class Versions {
        final String compilerCommon;
        final String compiler;
        final String baseLibrary;
        final String extensions;

        public Versions(Properties properties) {
            compilerCommon = properties.getProperty("compilerCommon");
            compiler = properties.getProperty("compiler");
            baseLibrary = properties.getProperty("baseLibrary");
            extensions = properties.getProperty("extensions");
            Preconditions.checkNotNull(compilerCommon, "cannot read compiler common version");
            Preconditions.checkNotNull(compiler, "cannot read compiler version");
            Preconditions.checkNotNull(baseLibrary, "cannot read baseLibrary version");
            Preconditions.checkNotNull(extensions, "cannot read extensions version");
        }
    }
}
