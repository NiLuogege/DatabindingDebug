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
import javax.lang.model.element.Element

/**
 * Compat for InverseBindingMethod
 */
data class InverseBindingMethodsCompat(val methods: List<InverseBindingMethodCompat>) {
    companion object {
        @JvmStatic
        fun create(element: Element): InverseBindingMethodsCompat {
            val support = element.getAnnotation(
                    android.databinding.InverseBindingMethods::class.java)
            if (support != null) {
                return create(support)
            }

            val androidX = element.getAnnotation(
                    androidx.databinding.InverseBindingMethods::class.java)
            if (androidX != null) {
                return create(androidX)
            }
            throw IllegalArgumentException(
                    "$element does not have InverseBindingMethods annotation")
        }

        fun create(annotation: android.databinding.InverseBindingMethods)
                : InverseBindingMethodsCompat {
            return InverseBindingMethodsCompat(annotation.value.map {
                InverseBindingMethodCompat(
                        type = safeType { it.type.java.canonicalName },
                        attribute = it.attribute,
                        event = it.event,
                        method = it.method
                )
            })
        }

        fun create(annotation: androidx.databinding.InverseBindingMethods)
                : InverseBindingMethodsCompat {
            return InverseBindingMethodsCompat(annotation.value.map {
                InverseBindingMethodCompat(
                        type = safeType { it.type.java.canonicalName },
                        attribute = it.attribute,
                        event = it.event,
                        method = it.method
                )
            })
        }
    }

    data class InverseBindingMethodCompat(
            val type: String,
            val attribute: String,
            val event: String,
            val method: String)
}