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

import javax.lang.model.element.Element

/**
 * Compat shim for InverseBindingAdapter
 */
data class InverseBindingAdapterCompat(val attribute : String, val event : String) {
    companion object {
        @JvmStatic
        fun create(element : Element) : InverseBindingAdapterCompat {
            val support = element.getAnnotation(
                    android.databinding.InverseBindingAdapter::class.java)
            if (support != null) {
                return InverseBindingAdapterCompat(
                        attribute = support.attribute,
                        event = support.event
                )
            }
            val androidX = element.getAnnotation(
                    androidx.databinding.InverseBindingAdapter::class.java)
            if (androidX != null) {
                return InverseBindingAdapterCompat(
                        attribute = androidX.attribute,
                        event = androidX.event
                )
            }
            throw IllegalArgumentException(
                    "$element does ont have InverseBindingAdapter annotation")
        }
    }
}