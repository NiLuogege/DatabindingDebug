/*
 * Copyright (C) 2017 The Android Open Source Project
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

package android.databinding.tool.store

import android.databinding.tool.DataBindingBuilder
import android.databinding.tool.util.L
import com.google.common.annotations.VisibleForTesting
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.Serializable

/**
 * Parameters for BaseDataBinder class. It also includes the logic to decide which files need to
 * be processes, which ones are invalid etc.
 */
class LayoutInfoInput(val args: Args) {
    private val baseBinderOutFile = File(args.logFolder, LOG_FILE_NAME)
    private val depsLogOutFile = File(args.logFolder, DEPS_LOG_FILE_NAME)
    val baseBinderLog = LayoutInfoLog.fromFile(baseBinderOutFile)
    val packageName = args.packageName

    companion object {
        @VisibleForTesting
        const val LOG_FILE_NAME = "base_builder_log.json"
        const val DEPS_LOG_FILE_NAME = "deps_log.json"
        private val LAYOUT_KEY = "-layout"
        private fun getBareLayoutName(fileName: String): String {
            val index = fileName.indexOf(LAYOUT_KEY)
            return if (index < 0) {
                L.e("unexpected layout file name $fileName")
                fileName
            } else {
                fileName.substring(0, index)
            }
        }
    }

    private val allInfoFiles by lazy(LazyThreadSafetyMode.NONE) {
        FileUtils.listFiles(args.infoFolder, arrayOf("xml"), true).toList()
    }

    private val groupedInfoFiles: Map<String, List<File>> by lazy(LazyThreadSafetyMode.NONE) {
        allInfoFiles.groupBy {
            getBareLayoutName(it.name)
        }
    }

    val deps by lazy(LazyThreadSafetyMode.NONE) {
        val v2 = ResourceBundle.loadClassInfoFromFolder(args.dependencyClassesFolder)
        args.v1ArtifactsFolder?.let {
            val v1 = V1CompatLayoutInfoLoader().load(it)
            v2.addAll(v1)
        }
        v2
    }

    val updatedDeps by lazy(LazyThreadSafetyMode.NONE) {
        val prev = GenClassInfoLog.fromFile(depsLogOutFile)
        val curret = deps
        prev.diff(curret)
    }

    /**
     * Binding classes that are inherited from depenendecies + prev run
     * they were generated in the previous run and are not affected in this run (incremental)
     */
    val existingBindingClasses: GenClassInfoLog by lazy(LazyThreadSafetyMode.NONE) {
        if (args.incremental) {
            // add files from previous step but remove any removed or outOfDate file
            deps.addAll(unchangedLog.classInfoLog)
        }
        deps
    }

    /**
     * These are classes that are either deleted, changed, outOfDate or has been potentially
     * affected by another change (e.g. dependent layout file)
     */
    val invalidatedClasses: Set<String> by lazy(LazyThreadSafetyMode.NONE) {
        invalidOutputs.mapNotNull {
            baseBinderLog.classInfoLog.mappings()[it]?.qName
        }.toSet()
    }

    /**
     * This is the files that needs to be processed and generated.
     */
    val filesToConsider by lazy {
        if (args.incremental) {
            // consider each outOfDate file + their sibling files
            invalidOutputs.flatMap {
                groupedInfoFiles[it] ?: emptyList()
            }.toSet()
        } else {
            groupedInfoFiles.values.flatMap { it }.toSet()
        }
    }

    /**
     * log composed of previous log minus changed / affected files.
     */
    val unchangedLog: LayoutInfoLog by lazy(LazyThreadSafetyMode.NONE) {
        val out = LayoutInfoLog()
        // what if one of these included another file ? :/ need to have an invalidation for that
        // as well :/.
        baseBinderLog.classInfoLog.mappings().forEach { mapping ->
            if (!invalidOutputs.contains(mapping.key)) {
                out.classInfoLog.addMapping(mapping.key, mapping.value)
                baseBinderLog.getDependencies(mapping.key).forEach {
                    out.addDependency(mapping.key, it)
                }
            }
        }
        out
    }

    private val invalidOutputs: Set<String> by lazy(LazyThreadSafetyMode.NONE) {
        val dontCarry = mutableSetOf<String>()
        args.outOfDate.forEach { dontCarry.add(getBareLayoutName(it.name)) }
        args.removed.forEach { dontCarry.add(getBareLayoutName(it.name)) }
        dontCarry.addAll(updatedDeps)
        // recursively invalidate dependencies
        var startCnt = 0
        while (startCnt < dontCarry.size) {
            startCnt = dontCarry.size
            dontCarry.addAll(baseBinderLog.getLayoutsThatDependOn(dontCarry))
        }
        dontCarry
    }

    internal fun saveLog(myLog: LayoutInfoLog) {
        myLog.serialize(baseBinderOutFile)
        FileUtils.forceMkdir(args.artifactFolder)
        val merged = GenClassInfoLog()
        merged.addAll(deps)
        merged.addAll(myLog.classInfoLog)
        merged.serialize(File(args.artifactFolder,
                "${args.packageName}${DataBindingBuilder.BINDING_CLASS_LIST_SUFFIX}"))
    }
    // separate serializable input args so that we can use gradle's worker unit
    data class Args(val outOfDate: List<File>,
                    val removed: List<File>,
                    val infoFolder: File,
                    val dependencyClassesFolder: File,
                    val artifactFolder: File,
                    val logFolder: File,
                    val packageName : String,
                    val incremental: Boolean,
                    val v1ArtifactsFolder : File? = null,
                    val useAndroidX : Boolean,
                    val enableViewBinding: Boolean
    ) : Serializable
}
