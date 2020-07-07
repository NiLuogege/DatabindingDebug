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

package android.databinding.tool.writer

import android.databinding.tool.ext.capitalizeUS
import android.databinding.tool.ext.mapEach
import android.databinding.tool.ext.parseXmlResourceReference
import android.databinding.tool.ext.stripNonJava
import android.databinding.tool.store.GenClassInfoLog
import android.databinding.tool.store.ResourceBundle
import android.databinding.tool.store.ResourceBundle.BindingTargetBundle
import android.databinding.tool.store.ResourceBundle.LayoutFileBundle

class BaseLayoutModel(val variations: List<LayoutFileBundle>) {
    private val COMPARE_FIELD_NAME = Comparator<BindingTargetBundle> { first, second ->
        val fieldName1 = fieldName(first)
        val fieldName2 = fieldName(second)
        fieldName1.compareTo(fieldName2)
    }
    private val scopedNames = mutableMapOf<JavaScope, MutableMap<Any, String>>()
    private val usedNames = mutableMapOf<JavaScope, MutableSet<String>>()

    val variables: List<ResourceBundle.VariableDeclaration>
    val sortedTargets: List<BindingTargetBundle>
    // TODO can these be modified?
    val bindingClassPackage: String = variations[0].bindingClassPackage
    val bindingClassName: String = variations[0].bindingClassName
    val modulePackage: String = variations[0].modulePackage
    val baseFileName: String = variations[0].fileName
    val importsByAlias = variations.flatMap { it.imports }.associate {
        Pair(it.name, it.type)
    }

    init {
        val mergedVars = arrayListOf<ResourceBundle.VariableDeclaration>()
        val mergedBindingTargets = arrayListOf<BindingTargetBundle>()
        val bindingTargetTags = mutableSetOf<String>()
        val bindingTargetIds = mutableSetOf<String>()
        val variableNames = mutableSetOf<String>()
        variations.forEach { variation ->
            variation.variables.forEach { variable ->
                if (!variableNames.contains(variable.name)) {
                    mergedVars.add(variable)
                    variableNames.add(variable.name)
                }
            }
            variation.bindingTargetBundles.forEach { target ->
                val add = if (target.id != null) {
                    !bindingTargetIds.contains(target.id)
                } else if (target.tag != null) {
                    !bindingTargetTags.contains(target.tag)
                } else {
                    // TODO can happen?
                    false
                }
                if (add) {
                    target.id?.let { bindingTargetIds.add(it) }
                    target.tag?.let { bindingTargetTags.add(it) }
                    mergedBindingTargets.add(target)
                }
            }
        }
        sortedTargets = mergedBindingTargets.sortedWith(COMPARE_FIELD_NAME)
        variables = mergedVars
    }

    /**
     * Returns a pair of two lists:
     * <ol>
     *   <li>Layout folder names in which [target] is present in this layout.
     *   <li>Layout folder names in which [target] is absent from this layout.
     * </ol>
     * If the second list is non-empty, the view for [target] may not be available at runtime depending on the configuration.
     */
    fun layoutConfigurationMembership(
        target: BindingTargetBundle
    ): Pair<List<String>, List<String>> {
        return variations
            .partition {
                it.bindingTargetBundles.any { otherTarget ->
                    otherTarget.id == target.id && otherTarget.isUsed
                }
            }
            .mapEach { it.map(LayoutFileBundle::getDirectory).sorted() }
    }

    private fun getScopeNames(scope: JavaScope): MutableMap<Any, String> {
        return scopedNames.getOrPut(scope) {
            mutableMapOf()
        }
    }

    private fun getUsedNames(scope: JavaScope): MutableSet<String> {
        return usedNames.getOrPut(scope) { mutableSetOf() }
    }

    fun fieldName(target: BindingTargetBundle): String {
        return getScopeNames(JavaScope.FIELD).getOrPut(target) {
            val name: String = if (target.id == null) {
                "m${readableName(target)}"
            } else {
                readableName(target)
            }
            getUniqueName(JavaScope.FIELD, name)
        }
    }

    fun fieldName(variable: ResourceBundle.VariableDeclaration): String {
        return getScopeNames(JavaScope.FIELD).getOrPut(variable) {
            getUniqueName(JavaScope.FIELD, "m${readableName(variable)}")
        }
    }

    fun setterName(variable: ResourceBundle.VariableDeclaration): String {
        return getScopeNames(JavaScope.SETTER).getOrPut(variable) {
            "set${readableName(variable)}"
        }
    }

    fun getterName(variable: ResourceBundle.VariableDeclaration): String {
        return getScopeNames(JavaScope.GETTER).getOrPut(variable) {
            "get${readableName(variable)}"
        }
    }

    private fun readableName(target: BindingTargetBundle): String {
        return if (target.id == null) {
            "boundView" + indexFromTag(target.tag)
        } else {
            target.id.parseXmlResourceReference().name.stripNonJava()
        }
    }

    private fun readableName(variable: ResourceBundle.VariableDeclaration) : String {
        return variable.name.stripNonJava().capitalizeUS()
    }

    private fun indexFromTag(tag: String): kotlin.Int {
        val startIndex = if (tag.startsWith("binding_")) {
            "binding_".length
        } else {
            tag.lastIndexOf('_') + 1
        }
        return Integer.parseInt(tag.substring(startIndex))
    }

    private fun getUniqueName(scope: JavaScope, base: String): String {
        val usedNames = getUsedNames(scope)
        var candidate = base
        var i = 0
        while (usedNames.contains(candidate)) {
            i++
            candidate = base + i
        }
        usedNames.add(candidate)
        return candidate
    }

    enum class JavaScope {
        FIELD,
        GETTER,
        SETTER
    }

    fun generateImplInfo(): Set<GenClassInfoLog.GenClassImpl> {
        return variations.map{
            GenClassInfoLog.GenClassImpl.from(it)
        }.toSet()
    }
}
