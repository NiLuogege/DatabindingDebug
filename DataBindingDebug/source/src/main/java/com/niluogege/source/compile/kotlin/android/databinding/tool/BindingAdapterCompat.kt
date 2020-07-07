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
 * Wrapper for BindingAdapter annotation
 */
class BindingAdapterCompat(val attributes : Array<String>, val requireAll : Boolean) {
    companion object {
        @JvmStatic
        fun create(element : Element) : BindingAdapterCompat {
            val support = element.getAnnotation(android.databinding.BindingAdapter::class.java)
            if (support != null) {
                return BindingAdapterCompat(
                        attributes = support.value,
                        requireAll = support.requireAll
                )
            }
            val androidX = element.getAnnotation(androidx.databinding.BindingAdapter::class.java)
            if (androidX != null) {
                return BindingAdapterCompat(
                        attributes = androidX.value,
                        requireAll = androidX.requireAll
                )
            }
            throw IllegalArgumentException("$element does not have BindingAdapter annotation")
        }
    }
}