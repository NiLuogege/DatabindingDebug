/*
 * Copyright (C) 2014 The Android Open Source Project
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

package android.databinding.tool.ext

public fun List<String>.joinToCamelCase(): String = when(size) {
    0 -> throw IllegalArgumentException("invalid section size, cannot be zero")
    1 -> this[0].toCamelCase()
    else -> this.joinToString("", transform = String::toCamelCase)
}

public fun List<String>.joinToCamelCaseAsVar(): String = when(size) {
    0 -> throw IllegalArgumentException("invalid section size, cannot be zero")
    1 -> this[0].toCamelCaseAsVar()
    else -> get(0).toCamelCaseAsVar() + drop(1).joinToCamelCase()
}

fun <T, R> Pair<T, T>.mapEach(body: (T) -> R): Pair<R, R> = body(first) to body(second)
