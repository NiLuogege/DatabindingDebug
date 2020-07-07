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

import androidx.databinding.Bindable
import java.lang.reflect.Field
import java.lang.reflect.Method
import javax.lang.model.element.Element
import android.databinding.Bindable as LegacyBindable

/**
 * Wrapper class when accessing Bindable annotation that handles both androidx and support namespaces
 */
class BindableCompat(val dependencies : Array<String>) {
    companion object {
        @JvmStatic
        fun extractFrom(element : Element) : BindableCompat? {
            return extractSupport(element) ?: extractAndroidX(element)
        }

        @JvmStatic
        fun extractFrom(method : Method) : BindableCompat? {
            return extractSupport(method) ?: extractAndroidX(method)
        }

        @JvmStatic
        fun extractFrom(field : Field) : BindableCompat? {
            return extractSupport(field) ?: extractAndroidX(field)
        }

        private fun extractAndroidX(element: Element): BindableCompat? {
            return element.getAnnotation(Bindable::class.java)?.toCompat()
        }

        private fun extractSupport(element: Element): BindableCompat? {
            return element.getAnnotation(LegacyBindable::class.java)?.toCompat()
        }

        private fun extractAndroidX(method: Method): BindableCompat? {
            return method.getAnnotation(Bindable::class.java)?.toCompat()
        }

        private fun extractSupport(method : Method): BindableCompat? {
            return method.getAnnotation(LegacyBindable::class.java)?.toCompat()
        }

        private fun extractAndroidX(field: Field): BindableCompat? {
            return field.getAnnotation(Bindable::class.java)?.toCompat()
        }

        private fun extractSupport(field: Field): BindableCompat? {
            return field.getAnnotation(LegacyBindable::class.java)?.toCompat()
        }

        private fun LegacyBindable.toCompat() = BindableCompat(value)

        private fun Bindable.toCompat() = BindableCompat(value)
    }
}