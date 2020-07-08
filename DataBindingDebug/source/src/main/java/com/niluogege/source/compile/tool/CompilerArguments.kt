/*
 * Copyright (C) 2016 The Android Open Source Project
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

package android.databinding.tool

import android.databinding.tool.util.Preconditions
import com.google.common.collect.Sets
import java.io.File
import java.util.TreeSet

@Suppress("unused")
/**
 * This class is used to pass information from the build system into the data binding compiler.
 * It can serialize itself to a given list of annotation processor options and read itself
 * from there.
 */
data class CompilerArguments constructor(
    // whether incremental annotation processing is requested
    val incremental: Boolean,

    val artifactType: Type,
    val modulePackage: String,
    val minApi: Int,

    // the SDK directory
    val sdkDir: File,

    // the directory containing artifacts from library dependencies
    val dependencyArtifactsDir: File,

    // output of the process layouts task
    val layoutInfoDir: File,

    // log file created by GenBaseClassesTask which is used to generate implementations in the data
    // binding annotation processor
    val classLogDir: File,

    // set when compiling a base feature, includes the package ids of all features
    val baseFeatureInfoDir: File?,

    // set when compiling a feature, includes the features id offset as well as the BR files it
    // is responsible to generate
    val featureInfoDir: File?,

    // the folder where generational class files are exported, only set in library builds
    val aarOutDir: File?,

    // the file into which data binding will output the list of classes that should be stripped in
    // the packaging phase
    val exportClassListOutFile: File?,

    val enableDebugLogs: Boolean,
    val printEncodedErrorLogs: Boolean,
    val isTestVariant: Boolean,
    val isEnabledForTests: Boolean,
    val isEnableV2: Boolean,
    // comma separated list of package names for direct dependencies that are directly accessible in this compilation.
    // only passed by bazel to be able to distinguish which mappers in the classpath can be accessed
    // in generated code. Gradle removes such classes from classpath hence they are not necessary.
    val directDependencyPackages : String? = null
) {
    init {
        Preconditions.check(
            !(incremental && !isEnableV2),
            "Incremental annotation processing is not supported by data binding V1"
        )
        Preconditions.check(
            artifactType != Type.FEATURE || featureInfoDir != null,
            "Must provide a feature info folder while compiling a non-base feature module"
        )
        Preconditions.check(
            artifactType != Type.LIBRARY || isTestVariant || aarOutDir != null,
            "Must specify bundle folder (aar out folder) for library projects"
        )
        Preconditions.check(
            artifactType != Type.LIBRARY || isTestVariant || exportClassListOutFile != null,
            "Must provide a folder to export generated class list"
        )
    }

    val isApp: Boolean
        get() = artifactType == Type.APPLICATION

    val isLibrary: Boolean
        get() = artifactType == Type.LIBRARY

    val isFeature: Boolean
        get() = artifactType == Type.FEATURE

    // returns null if not specified
    fun parseDirectDependencyPackages() : TreeSet<String>? {
        return directDependencyPackages?.let {
            val encoded = if (it.startsWith('[') && it.endsWith(']')) {
                it.subSequence(1, it.length - 1)
            } else {
                it
            }
            encoded.splitToSequence(',').toCollection(TreeSet())
        }
    }

    /**
     * Creates a copy of the arguments but sets the version to v1 and package to the given package.
     * This is used when we need to run a compatibility compilation for v1 dependencies.
     */
    fun copyAsV1(modulePackage: String): CompilerArguments {
        val argMap = toMap().toMutableMap()

        // Incremental annotation processing is not supported by data binding V1
        argMap[PARAM_INCREMENTAL] = booleanToString(false)
        argMap[PARAM_MODULE_PACKAGE] = modulePackage
        argMap[PARAM_ENABLE_V2] = booleanToString(false)

        return readFromOptions(argMap)
    }

    fun toMap(): Map<String, String> {
        val args = HashMap<String, String>()
        args[PARAM_INCREMENTAL] = booleanToString(incremental)
        args[PARAM_ARTIFACT_TYPE] = artifactType.name
        args[PARAM_MODULE_PACKAGE] = modulePackage
        args[PARAM_MIN_API] = minApi.toString()
        args[PARAM_SDK_DIR] = sdkDir.path
        args[PARAM_DEPENDENCY_ARTIFACTS_DIR] = dependencyArtifactsDir.path
        args[PARAM_LAYOUT_INFO_DIR] = layoutInfoDir.path
        args[PARAM_CLASS_LOG_DIR] = classLogDir.path
        baseFeatureInfoDir?.let { args[PARAM_BASE_FEATURE_INFO_DIR] = it.path }
        featureInfoDir?.let { args[PARAM_FEATURE_INFO_DIR] = it.path }
        aarOutDir?.let { args[PARAM_AAR_OUT_DIR] = it.path }
        exportClassListOutFile?.let { args[PARAM_EXPORT_CLASS_LIST_OUT_FILE] = it.path }
        args[PARAM_ENABLE_DEBUG_LOGS] = booleanToString(enableDebugLogs)
        args[PARAM_PRINT_ENCODED_ERROR_LOGS] = booleanToString(printEncodedErrorLogs)
        args[PARAM_IS_TEST_VARIANT] = booleanToString(isTestVariant)
        args[PARAM_ENABLE_FOR_TESTS] = booleanToString(isEnabledForTests)
        args[PARAM_ENABLE_V2] = booleanToString(isEnableV2)
        if (directDependencyPackages != null) {
            args[PARAM_DIRECT_DEPENDENCY_PKGS] = directDependencyPackages
        }
        return args
    }

    enum class Type {
        APPLICATION,
        LIBRARY,
        FEATURE
    }

    companion object {
        /**
         * legacy arguments are used by blaze, will be removed once blaze is updated.
         * it cannot happen in lock step.
         */
        private const val PREFIX = "android.databinding."
        const val PARAM_INCREMENTAL = PREFIX + "incremental" // Needs to be public
        private const val PARAM_ARTIFACT_TYPE = PREFIX + "artifactType"
        private const val PARAM_MODULE_PACKAGE = PREFIX + "modulePackage"
        private const val PARAM_MIN_API = PREFIX + "minApi"
        private const val PARAM_SDK_DIR = PREFIX + "sdkDir"
        private const val PARAM_DEPENDENCY_ARTIFACTS_DIR = PREFIX + "dependencyArtifactsDir"
        private const val LEGACY_PARAM_DEPENDENCY_ARTIFACTS_DIR = PREFIX + "bindingBuildFolder"
        private const val PARAM_LAYOUT_INFO_DIR = PREFIX + "layoutInfoDir"
        private const val LEGACY_PARAM_LAYOUT_INFO_DIR = PREFIX + "xmlOutDir"
        private const val PARAM_CLASS_LOG_DIR = PREFIX + "classLogDir"
        private const val PARAM_BASE_FEATURE_INFO_DIR = PREFIX + "baseFeatureInfoDir"
        private const val PARAM_FEATURE_INFO_DIR = PREFIX + "featureInfoDir"
        private const val PARAM_AAR_OUT_DIR = PREFIX + "aarOutDir"
        private const val LEGACY_PARAM_AAR_OUT_DIR = PREFIX + "generationalFileOutDir"
        private const val PARAM_EXPORT_CLASS_LIST_OUT_FILE = PREFIX + "exportClassListOutFile"
        private const val LEGACY_PARAM_EXPORT_CLASS_LIST_OUT_FILE = PREFIX + "exportClassListTo"
        private const val PARAM_ENABLE_DEBUG_LOGS = PREFIX + "enableDebugLogs"
        private const val PARAM_PRINT_ENCODED_ERROR_LOGS = PREFIX + "printEncodedErrorLogs"
        private const val PARAM_IS_TEST_VARIANT = PREFIX + "isTestVariant"
        private const val PARAM_ENABLE_FOR_TESTS = PREFIX + "enableForTests"
        private const val PARAM_ENABLE_V2 = PREFIX + "enableV2"
        // this is only passed by blaze to distinguish which mappers are accessible in code.
        // It is not needed in gradle since gradle removes them from classpath unless they are
        // accessible
        // it looks like [pkg1, pkg2]. Java does not distinguish between empty string vs null (absent) so we are
        // using [] as a wrapper around to easily distinguish between unspecified vs empty list
        private const val PARAM_DIRECT_DEPENDENCY_PKGS = PREFIX + "directDependencyPkgs"

        @JvmField
        val ALL_PARAMS: Set<String> = Sets.newHashSet(
            PARAM_INCREMENTAL,
            PARAM_ARTIFACT_TYPE,
            PARAM_MODULE_PACKAGE,
            PARAM_MIN_API,
            PARAM_SDK_DIR,
            PARAM_DEPENDENCY_ARTIFACTS_DIR,
            PARAM_LAYOUT_INFO_DIR,
            PARAM_CLASS_LOG_DIR,
            PARAM_BASE_FEATURE_INFO_DIR,
            PARAM_FEATURE_INFO_DIR,
            PARAM_AAR_OUT_DIR,
            PARAM_EXPORT_CLASS_LIST_OUT_FILE,
            PARAM_ENABLE_DEBUG_LOGS,
            PARAM_PRINT_ENCODED_ERROR_LOGS,
            PARAM_IS_TEST_VARIANT,
            PARAM_ENABLE_FOR_TESTS,
            PARAM_ENABLE_V2,
            PARAM_DIRECT_DEPENDENCY_PKGS
        )

        @JvmStatic
        fun readFromOptions(options: Map<String, String>): CompilerArguments {
            return CompilerArguments(
                incremental = stringToBoolean(options[PARAM_INCREMENTAL]),
                artifactType = Type.valueOf(options[PARAM_ARTIFACT_TYPE]!!),
                modulePackage = options[PARAM_MODULE_PACKAGE]!!,
                minApi = Integer.parseInt(options[PARAM_MIN_API]!!),
                sdkDir = File(options[PARAM_SDK_DIR]!!),
                dependencyArtifactsDir = File(
                        (options[PARAM_DEPENDENCY_ARTIFACTS_DIR] ?:
                        options[LEGACY_PARAM_DEPENDENCY_ARTIFACTS_DIR])!!),
                layoutInfoDir = File(
                        (options[PARAM_LAYOUT_INFO_DIR] ?:
                        options[LEGACY_PARAM_LAYOUT_INFO_DIR])!!),
                // class log dir is not passed by bazel but we don't want early failure
                classLogDir = File(options[PARAM_CLASS_LOG_DIR] ?: ""),
                baseFeatureInfoDir = options[PARAM_BASE_FEATURE_INFO_DIR]?.let { File(it) },
                featureInfoDir = options[PARAM_FEATURE_INFO_DIR]?.let { File(it) },
                aarOutDir = (options[PARAM_AAR_OUT_DIR] ?: options[LEGACY_PARAM_AAR_OUT_DIR])?.let {
                    File(it)
                },
                exportClassListOutFile = (options[PARAM_EXPORT_CLASS_LIST_OUT_FILE] ?:
                        options[LEGACY_PARAM_EXPORT_CLASS_LIST_OUT_FILE])?.let {
                    File(it)
                },
                enableDebugLogs = stringToBoolean(options[PARAM_ENABLE_DEBUG_LOGS]),
                printEncodedErrorLogs =
                stringToBoolean(options[PARAM_PRINT_ENCODED_ERROR_LOGS]),
                isTestVariant = stringToBoolean(options[PARAM_IS_TEST_VARIANT]),
                isEnabledForTests = stringToBoolean(options[PARAM_ENABLE_FOR_TESTS]),
                isEnableV2 = stringToBoolean(options[PARAM_ENABLE_V2]),
                // if specified, rely on it even if it is empty
                directDependencyPackages = options[PARAM_DIRECT_DEPENDENCY_PKGS]
            )
        }

        private fun booleanToString(boolValue: Boolean): String {
            return if (boolValue) "1" else "0"
        }

        private fun stringToBoolean(boolValue: String?): Boolean {
            return boolValue?.trim() == "1"
        }
    }
}
