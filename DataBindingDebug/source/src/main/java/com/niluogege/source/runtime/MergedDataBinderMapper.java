/*
 * Copyright (C) 2017 The Android Open Source Project
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


import android.util.Log;
import android.view.View;

import androidx.databinding.DataBindingComponent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A data binding mapper that merges other mappers.
 */
@SuppressWarnings("unused")
public class MergedDataBinderMapper extends DataBinderMapper {
    private static final String TAG = "MergedDataBinderMapper";
    // we keep set of existing classes so that addMapper can avoid re-adding same class.
    // usually not necessary as list lookup might be sufficient but if the project has 100+
    // modules, it might matter, hence we have a fast lookup as well.
    private Set<Class<? extends DataBinderMapper>> mExistingMappers = new HashSet<>();
    private List<DataBinderMapper> mMappers = new CopyOnWriteArrayList<>();
    /**
     * List of features that have binding mappers. We try to load those classes lazily when we
     * cannot find a binding.
     */
    private List<String> mFeatureBindingMappers = new CopyOnWriteArrayList<>();

    /**
     * Adds the provided mapper to the list of mappers unless an instance of it already exists
     * in the list. MergedDataBinderMapper will also call
     * {@link DataBinderMapper#collectDependencies()} on the provided
     * mapper.
     *
     * @param mapper The new DataBinderMapper to add to the list of mappers.
     */
    @SuppressWarnings("WeakerAccess")
    public void addMapper(DataBinderMapper mapper) {
        Class<? extends DataBinderMapper> mapperClass = mapper.getClass();
        if (mExistingMappers.add(mapperClass)) {
            mMappers.add(mapper);
            final List<DataBinderMapper> dependencies = mapper.collectDependencies();
            for(DataBinderMapper dependency : dependencies) {
                addMapper(dependency);
            }
        }
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    protected void addMapper(String featureMapper) {
        mFeatureBindingMappers.add(featureMapper + ".DataBinderMapperImpl");
    }

    @Override
    public ViewDataBinding getDataBinder(DataBindingComponent bindingComponent, View view,
                                         int layoutId) {
        for(DataBinderMapper mapper : mMappers) {
            ViewDataBinding result = mapper.getDataBinder(bindingComponent, view, layoutId);
            if (result != null) {
                return result;
            }
        }
        if (loadFeatures()) {
            return getDataBinder(bindingComponent, view, layoutId);
        }
        return null;
    }

    @Override
    public ViewDataBinding getDataBinder(DataBindingComponent bindingComponent, View[] view,
                                         int layoutId) {
        for(DataBinderMapper mapper : mMappers) {
            ViewDataBinding result = mapper.getDataBinder(bindingComponent, view, layoutId);
            if (result != null) {
                return result;
            }
        }
        if (loadFeatures()) {
            return getDataBinder(bindingComponent, view, layoutId);
        }
        return null;
    }

    @Override
    public int getLayoutId(String tag) {
        for(DataBinderMapper mapper : mMappers) {
            int result = mapper.getLayoutId(tag);
            if (result != 0) {
                return result;
            }
        }
        if (loadFeatures()) {
            return getLayoutId(tag);
        }
        return 0;
    }

    @Override
    public String convertBrIdToString(int id) {
        for(DataBinderMapper mapper : mMappers) {
            String result = mapper.convertBrIdToString(id);
            if (result != null) {
                return result;
            }
        }
        if (loadFeatures()) {
            return convertBrIdToString(id);
        }
        return null;
    }

    /**
     * @return true if we load a new mapper
     */
    private boolean loadFeatures() {
        boolean found = false;
        for (String mapper : mFeatureBindingMappers) {
            try {
                final Class<?> aClass = Class.forName(mapper);
                if (DataBinderMapper.class.isAssignableFrom(aClass)) {
                    addMapper((DataBinderMapper) aClass.newInstance());
                    mFeatureBindingMappers.remove(mapper);
                    found = true;
                }
            } catch (ClassNotFoundException ignored) {
            } catch (IllegalAccessException exception) {
                Log.e(TAG, "unable to add feature mapper for " + mapper, exception);
            } catch (InstantiationException exception) {
                Log.e(TAG, "unable to add feature mapper for " + mapper, exception);
            }
        }
        return found;
    }
}
