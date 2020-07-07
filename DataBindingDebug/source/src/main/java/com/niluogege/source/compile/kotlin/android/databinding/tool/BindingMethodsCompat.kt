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

package android.databinding.tool

import android.databinding.tool.ext.safeType
import android.databinding.tool.util.LoggedErrorException
import javax.lang.model.element.Element

/**
 * compat shim for BindingMethod annotations
 */
data class BindingMethodsCompat(
        val methods: List<BindingMethodCompat>
) {
    companion object {
        @JvmStatic
        fun create(element: Element): BindingMethodsCompat {
            val support = element.getAnnotation(android.databinding.BindingMethods::class.java)
            if (support != null) {
                return create(support)
            }
            val androidX = element.getAnnotation(androidx.databinding.BindingMethods::class.java)
            if (androidX != null) {
                return create(androidX)
            }
            throw IllegalArgumentException("$element does ont have BindingMethods annotation")
        }

        private fun create(annotation: android.databinding.BindingMethods): BindingMethodsCompat {
            return BindingMethodsCompat(annotation.value.map {
                try {
                    BindingMethodCompat(
                            type = safeType { it.type.java.canonicalName },
                            attribute = it.attribute,
                            method = it.method
                    )
                } catch (e: LoggedErrorException) {
                    null
                }
            }.filterNotNull())
        }

        private fun create(annotation: androidx.databinding.BindingMethods): BindingMethodsCompat {
            return BindingMethodsCompat(annotation.value.map {
                try {
                    BindingMethodCompat(
                            type = safeType { it.type.java.canonicalName },
                            attribute = it.attribute,
                            method = it.method
                    )
                } catch (e: LoggedErrorException) {
                    null
                }
            }.filterNotNull())
        }
    }

    data class BindingMethodCompat(
            val type: String,
            val attribute: String,
            val method: String)
}