/*
 * Copyright (C) 2019 The Android Open Source Project
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

package android.databinding.tool.util

import java.io.File

/**
 * Represents an absolute path that could be split into two components: the path to a base
 * directory, and the relative path from that base directory. The absolute path is always available
 * (non-null), whereas the base directory and the relative path may not be available (they are
 * nullable).
 */
class RelativizableFile private constructor(
    val baseDir: File?,
    file: File /* Relative if baseDir != null, absolute if baseDir == null */
) {
  val absoluteFile: File
  val relativeFile: File?

  init {
    if (baseDir != null) {
      check(baseDir.isAbsolute) { "${baseDir.path} is not an absolute path" }
      check(!file.isAbsolute) { "${file.path} is not a relative path" }
      absoluteFile = File(baseDir, file.path)
      relativeFile = file
    } else {
      check(file.isAbsolute) { "${file.path} is not an absolute path" }
      absoluteFile = file
      relativeFile = null
    }
  }

  companion object {

    /**
     * Creates a [RelativizableFile] with a base directory and a relative path from the base
     * directory.
     */
    @JvmStatic
    fun fromRelativeFile(baseDir: File, relativeFile: File): RelativizableFile {
      return RelativizableFile(baseDir, relativeFile)
    }

    /**
     * Creates a [RelativizableFile] with a base directory and a relative path from the base
     * directory if the base directory is given and it is a parent of the given absolute path.
     * Otherwise, this method creates a [RelativizableFile] with the given absolute path only.
     */
    @JvmStatic
    fun fromAbsoluteFile(absoluteFile: File, baseDir: File? = null): RelativizableFile {
      check(absoluteFile.isAbsolute) { "${absoluteFile.path} is not an absolute path" }
      baseDir?.let { check(it.isAbsolute) { "${it.path} is not an absolute path" } }

      return if (baseDir != null && absoluteFile.absolutePath.startsWith(baseDir.absolutePath)) {
        fromRelativeFile(baseDir, baseDir.toPath().relativize(absoluteFile.toPath()).toFile())
      } else {
        RelativizableFile(null, absoluteFile)
      }
    }
  }
}