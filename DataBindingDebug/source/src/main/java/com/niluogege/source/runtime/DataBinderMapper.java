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

package com.niluogege.source.runtime;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.databinding.DataBindingComponent;

import java.util.Collections;
import java.util.List;

/**
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("WeakerAccess")
public abstract class DataBinderMapper {
    public abstract ViewDataBinding getDataBinder(DataBindingComponent bindingComponent, View view,
                                                  int layoutId);
    public abstract ViewDataBinding getDataBinder(DataBindingComponent bindingComponent,
                                                  View[] view, int layoutId);
    public abstract int getLayoutId(String tag);
    public abstract String convertBrIdToString(int id);
    @NonNull
    public List<DataBinderMapper> collectDependencies() {
        // default implementation for backwards compatibility.
        return Collections.emptyList();
    }
}
