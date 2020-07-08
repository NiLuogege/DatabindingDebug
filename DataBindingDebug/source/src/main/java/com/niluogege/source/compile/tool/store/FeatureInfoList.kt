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

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import java.io.File

/**
 * Data class that holds the list of package ids for features
 */
data class FeatureInfoList(
        @SerializedName("packages")
        val packages : Set<String>) {
    companion object {
        private val GSON = GsonBuilder()
                .disableHtmlEscaping()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setPrettyPrinting().create()
        @JvmStatic
        fun fromFile(file : File) : FeatureInfoList {
            if (!file.exists()) {
                return FeatureInfoList(emptySet())
            }
            return file.reader(Charsets.UTF_16).use {
                GSON.fromJson(it, FeatureInfoList::class.java)
            }
        }
    }

    fun serialize(file: File) {
        if (file.exists()) {
            file.delete()
        }
        file.writer(Charsets.UTF_16).use {
            GSON.toJson(this, it)
        }
    }
}
