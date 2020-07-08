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

package android.databinding.tool

import android.databinding.tool.processing.Scope
import android.databinding.tool.store.GenClassInfoLog
import android.databinding.tool.store.LayoutInfoInput
import android.databinding.tool.store.LayoutInfoLog
import android.databinding.tool.store.ResourceBundle
import android.databinding.tool.store.ResourceBundle.LayoutFileBundle
import android.databinding.tool.writer.BaseLayoutBinderWriter
import android.databinding.tool.writer.BaseLayoutModel
import android.databinding.tool.writer.JavaFileWriter
import android.databinding.tool.writer.generatedClassInfo
import android.databinding.tool.writer.toJavaFile
import android.databinding.tool.writer.toViewBinder
import com.squareup.javapoet.JavaFile

@Suppress("unused")// used by tools
class BaseDataBinder(val input : LayoutInfoInput) {
    private val resourceBundle : ResourceBundle = ResourceBundle(
            input.packageName, input.args.useAndroidX)
    init {
        input.filesToConsider
                .forEach {
                    it.inputStream().use {
                        val bundle = LayoutFileBundle.fromXML(it)
                        resourceBundle.addLayoutBundle(bundle, true)
                    }
                }
        resourceBundle.addDependencyLayouts(input.existingBindingClasses)
        resourceBundle.validateAndRegisterErrors()
    }
    @Suppress("unused")// used by android gradle plugin
    fun generateAll(writer : JavaFileWriter) {
        input.invalidatedClasses.forEach {
            writer.deleteFile(it)
        }

        val myLog = LayoutInfoLog()
        myLog.addAll(input.unchangedLog)

        val useAndroidX = input.args.useAndroidX
        val libTypes = LibTypes(useAndroidX = useAndroidX)

        val layoutBindings = resourceBundle.allLayoutFileBundlesInSource
            .groupBy(LayoutFileBundle::getFileName)

        layoutBindings.forEach { layoutName, variations ->
            val layoutModel = BaseLayoutModel(variations)

            val javaFile: JavaFile
            val classInfo: GenClassInfoLog.GenClass
            if (variations.first().isBindingData) {
                val binderWriter = BaseLayoutBinderWriter(layoutModel, libTypes)
                javaFile = binderWriter.write()
                classInfo = binderWriter.generateClassInfo()
            } else {
                check(input.args.enableViewBinding) {
                    "View binding is not enabled but found non-data binding layouts: $variations"
                }

                val viewBinder = layoutModel.toViewBinder()
                javaFile = viewBinder.toJavaFile(useLegacyAnnotations = !useAndroidX)
                classInfo = viewBinder.generatedClassInfo()
            }

            writer.writeToFile(javaFile)
            myLog.classInfoLog.addMapping(layoutName, classInfo)

            variations.forEach {
                it.bindingTargetBundles.forEach { bundle ->
                    if (bundle.isBinder) {
                        myLog.addDependency(layoutName, bundle.includedLayout)
                    }
                }
            }
        }
        input.saveLog(myLog)
        // data binding will eat some errors to be able to report them later on. This is a good
        // time to report them after the processing is done.
        Scope.assertNoError()
    }
}
