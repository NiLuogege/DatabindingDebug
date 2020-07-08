/*
 * Copyright (C) 2018 The Android Open Source Project
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
import android.databinding.tool.store.GenClassInfoLog.GenClass
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOCase
import org.apache.commons.io.filefilter.SuffixFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import java.io.File
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectStreamClass
import java.io.Serializable

/**
 * This class is necessary to handle v1 dependencies.
 * <p>
 * It basically loads the layout intermediates in the old format and converts them into
 * GenClassInfoLog (the new format). This us to generate correct base class code inside gradl
 * task.
 * <p>
 * Unfortunately, the v1 class is ProcessExpressions#Intermediate, which is NOT a dependency on
 * the gradle tool. Luckily, the actual content of the serialized object is just a hashmap where
 * values are xml that can be parsed from compilerCommon. This is why we have the frankenstein
 * deserialization to map it into another class.
 */
class V1CompatLayoutInfoLoader {
    fun load(folder: File): GenClassInfoLog {
        val fileFilter = SuffixFileFilter(
                DataBindingBuilder.LAYOUT_INFO_FILE_EXT,
                IOCase.INSENSITIVE)
        val files = FileUtils.listFiles(folder,
                fileFilter,
                TrueFileFilter.INSTANCE)
        val mapping: Map<String, GenClassInfoLog.GenClass> = files.flatMap {
            // read bundle
            FileUtils.openInputStream(it).use { inputStream ->
                val intermediateCompat =
                        CompatObjectInputStream(inputStream).readObject() as IntermediateV2Compat
                intermediateCompat.mLayoutInfoMap.values.map {
                    ResourceBundle.LayoutFileBundle
                            .fromXML(it.byteInputStream(Charsets.UTF_8))
                }
            }
        }.map { bundle ->
            // convert it into gen class
            Pair(bundle.fileName,
                    GenClass(
                            qName = bundle.fullBindingClass,
                            modulePackage = bundle.modulePackage,
                            variables = bundle.variables.associateBy(
                                    keySelector = { it.name },
                                    valueTransform = { it.type }
                            ),
                            // just ignored because we won't use it.
                            implementations = emptySet()
                    ))
        }.toMap()
        return GenClassInfoLog(mappings = mapping.toMutableMap())
    }

    private class CompatObjectInputStream(`in`: InputStream) : ObjectInputStream(`in`) {
        override fun readClassDescriptor(): ObjectStreamClass {
            val original = super.readClassDescriptor()
            // hack for https://issuetracker.google.com/issues/71057619
            INTERMEDIATE_CLASSES[original.name]?.let {
                return ObjectStreamClass.lookup(it)
            }
            return original
        }
    }

    class IntermediateV2Compat : IntermediateV1Compat(), Serializable {
    }

    open class IntermediateV1Compat : Serializable {
        // name to xml content map
        @JvmField
        internal var mLayoutInfoMap: MutableMap<String, String> = HashMap()
    }

    companion object {
        private val INTERMEDIATE_CLASSES = mapOf(
                "android.databinding.annotationprocessor.ProcessExpressions\$IntermediateV1" to
                        IntermediateV1Compat::class.java,
                "android.databinding.annotationprocessor.ProcessExpressions\$IntermediateV2" to
                        IntermediateV2Compat::class.java)
    }
}