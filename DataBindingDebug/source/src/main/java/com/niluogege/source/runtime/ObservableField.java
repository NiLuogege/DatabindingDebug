/*
 * Copyright (C) 2015 The Android Open Source Project
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

import androidx.annotation.Nullable;
import androidx.databinding.Observable;

import java.io.Serializable;

/**
 * An object wrapper to make it observable.
 * <p>
 * Observable field classes may be used instead of creating an Observable object. It can also
 * create a calculated field, depending on other fields:
 * <pre><code>public class MyDataObject {
 *     private Context context;
 *     public final ObservableField&lt;String&gt; first = new ObservableField&lt;String&gt;();
 *     public final ObservableField&lt;String&gt; last = new ObservableField&lt;String&gt;();
 *     public final ObservableField&lt;String&gt; display =
 *         new ObservableField&lt;String&gt;(first, last) {
 *             &#64;Override
 *             public String get() {
 *                 return context.getResources().getString(R.string.name, first.get, last.get());
 *             }
 *         };
 *     public final ObservableInt age = new ObservableInt();
 * }</code></pre>
 * Fields of this type should be declared final because bindings only detect changes in the
 * field's value, not of the field itself.
 *
 * @param <T> The type parameter for the actual object.
 * @see ObservableParcelable
 */
public class ObservableField<T> extends BaseObservableField implements Serializable {
    static final long serialVersionUID = 1L;
    private T mValue;

    /**
     * Wraps the given object and creates an observable object
     *
     * @param value The value to be wrapped as an observable.
     */
    public ObservableField(T value) {
        mValue = value;
    }

    /**
     * Creates an empty observable object
     */
    public ObservableField() {
    }

    /**
     * Creates an ObservableField that depends on {@code dependencies}. Typically,
     * ObservableFields are passed as dependencies. When any dependency
     * notifies changes, this ObservableField also notifies a change.
     *
     * @param dependencies The Observables that this ObservableField depends on.
     */
    public ObservableField(Observable... dependencies) {
        super(dependencies);
    }

    /**
     * @return the stored value.
     */
    @Nullable
    public T get() {
        return mValue;
    }

    /**
     * Set the stored value.
     *
     * @param value The new value
     */
    public void set(T value) {
        if (value != mValue) {
            mValue = value;
            notifyChange();
        }
    }
}
