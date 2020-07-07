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

import com.android.tools.build.jetifier.core.TypeRewriter
import com.android.tools.build.jetifier.core.config.Config
import com.android.tools.build.jetifier.core.config.ConfigParser
import com.android.tools.build.jetifier.core.type.JavaType

class LibTypes(val useAndroidX: Boolean) {
    val bindingPackage = if (useAndroidX) {
        "androidx.databinding"
    } else {
        "android.databinding"
    }

    private val typeRewriter by lazy(LazyThreadSafetyMode.NONE) {
        val config =
            ConfigParser.loadDefaultConfig()
                ?: throw IllegalStateException("Cannot load AndroidX conversion file.")
        TypeRewriter(config = config, useFallback = true)
    }

    val viewStubProxy by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.databinding.ViewStubProxy")
    }


    val observable by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.databinding.Observable")
    }

    val observableList by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.databinding.ObservableList")
    }

    val observableMap by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.databinding.ObservableMap")
    }

    val liveData by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.arch.lifecycle.LiveData")
    }

    val mutableLiveData by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.arch.lifecycle.MutableLiveData")
    }

    val dataBindingComponent by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.databinding.DataBindingComponent")
    }

    val dataBinderMapper by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.databinding.DataBinderMapper")
    }

    val observableFields by lazy(LazyThreadSafetyMode.NONE) {
        arrayOf(
                "android.databinding.ObservableBoolean",
                "android.databinding.ObservableByte",
                "android.databinding.ObservableChar",
                "android.databinding.ObservableShort",
                "android.databinding.ObservableInt",
                "android.databinding.ObservableLong",
                "android.databinding.ObservableFloat",
                "android.databinding.ObservableDouble",
                "android.databinding.ObservableField",
                "android.databinding.ObservableParcelable").map { convert(it) }
    }

    val viewDataBinding by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.databinding.ViewDataBinding")
    }

    val listClassNames by lazy(LazyThreadSafetyMode.NONE) {
        arrayOf("java.util.List",
                "android.util.SparseArray",
                "android.util.SparseBooleanArray",
                "android.util.SparseIntArray",
                "android.util.SparseLongArray",
                "android.util.LongSparseArray",
                "android.support.v4.util.LongSparseArray").map { convert(it) }
    }

    val inverseBindingListener by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.databinding.InverseBindingListener")
    }

    val propertyChangedInverseListener by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.databinding.ViewDataBinding.PropertyChangedInverseListener")
    }

    val bindable by lazy(LazyThreadSafetyMode.NONE) { convert("android.databinding.Bindable") }

    val bindingAdapter by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.databinding.BindingAdapter")
    }

    val dataBindingUtil by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.databinding.DataBindingUtil")
    }

    val nonNull by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.support.annotation.NonNull")
    }

    val nullable by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.support.annotation.Nullable")
    }

    val lifecycleOwner by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.arch.lifecycle.LifecycleOwner")
    }

    val bindableClass = when {
        useAndroidX -> androidx.databinding.Bindable::class.java
        else -> android.databinding.Bindable::class.java
    }

    //获取 BindingAdapter 注解的 class对象
    val bindingAdapterClass = when {
        useAndroidX -> androidx.databinding.BindingAdapter::class.java
        else -> android.databinding.BindingAdapter::class.java
    }

    val bindingMethodsClass = when {
        useAndroidX -> androidx.databinding.BindingMethods::class.java
        else -> android.databinding.BindingMethods::class.java
    }

    val bindingConversionClass = when {
        useAndroidX -> androidx.databinding.BindingConversion::class.java
        else -> android.databinding.BindingConversion::class.java
    }

    val inverseBindingAdapterClass = when {
        useAndroidX -> androidx.databinding.InverseBindingAdapter::class.java
        else -> android.databinding.InverseBindingAdapter::class.java
    }

    val inverseBindingMethodsClass = when {
        useAndroidX -> androidx.databinding.InverseBindingMethods::class.java
        else -> android.databinding.InverseBindingMethods::class.java
    }

    val inverseMethodClass = when {
        useAndroidX -> androidx.databinding.InverseMethod::class.java
        else -> android.databinding.InverseMethod::class.java
    }

    val untaggableClass = when {
        useAndroidX -> androidx.databinding.Untaggable::class.java
        else -> android.databinding.Untaggable::class.java
    }

    val appCompatResources by lazy(LazyThreadSafetyMode.NONE) {
        convert("android.support.v7.content.res.AppCompatResources")
    }

    /**
     * Convert from support to androidX
     */
    fun convert(inp: String): String {
        if (!useAndroidX) {
            return inp
        }
        hackConvert(inp)?.let {
            return it
        }
        val javaType = JavaType.fromDotVersion(inp)
        return typeRewriter.rewriteType(javaType)?.toDotNotation() ?: inp
    }

    // TODO remove once we have all rules in jettifier
    private fun hackConvert(inp: String): String? {
        val match = PREFIX_REPLACEMENTS.entries.firstOrNull {
            inp.startsWith(it.key)
        } ?: return null
        return match.value + inp.substring(match.key.length)
    }

    companion object {
        // needed until we can update jettifier w/ arch and data binding
        private val PREFIX_REPLACEMENTS = mapOf(
                "android.databinding." to "androidx.databinding.",
                "android.arch.lifecycle." to "androidx.lifecycle.",
                "android.arch.core." to "androidx.arch.core.",
                "android.arch.core.executor." to "androidx.executor.",
                "android.arch.paging." to "androidx.paging.",
                "android.arch.persistence.room." to "androidx.room.",
                "android.arch.persistence." to "androidx.sqlite."
        )
    }
}