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

import android.os.Parcel;
import android.os.Parcelable;

import androidx.databinding.Observable;

import java.io.Serializable;

/**
 * An observable class that holds a primitive boolean.
 * <p>
 * Observable field classes may be used instead of creating an Observable object. It can also
 * create a calculated field, depending on other fields:
 * <pre><code>public class MyDataObject {
 *     public final ObservableBoolean isAdult = new ObservableBoolean();
 *     public final ObservableBoolean isChild = new ObservableBoolean(isAdult) {
 *         &#64;Override
 *         public boolean get() { return !isAdult.get(); }
 *     };
 * }</code></pre>
 * Fields of this type should be declared final because bindings only detect changes in the
 * field's value, not of the field itself.
 * <p>
 * This class is parcelable and serializable but callbacks are ignored when the object is
 * parcelled / serialized. Unless you add custom callbacks, this will not be an issue because
 * data binding framework always re-registers callbacks when the view is bound. A parceled
 * ObservableBoolean will lose its dependencies.
 */
public class ObservableBoolean extends BaseObservableField implements Parcelable, Serializable {
    static final long serialVersionUID = 1L;
    private boolean mValue;

    /**
     * Creates an ObservableBoolean with the given initial value.
     *
     * @param value the initial value for the ObservableBoolean
     */
    public ObservableBoolean(boolean value) {
        mValue = value;
    }

    /**
     * Creates an ObservableBoolean with the initial value of <code>false</code>.
     */
    public ObservableBoolean() {
    }

    /**
     * Creates an ObservableBoolean that depends on {@code dependencies}. Typically,
     * {@link ObservableField}s are passed as dependencies. When any dependency
     * notifies changes, this ObservableBoolean also notifies a change.
     *
     * @param dependencies The Observables that this ObservableBoolean depends on.
     */
    public ObservableBoolean(Observable... dependencies) {
        super(dependencies);
    }

    /**
     * @return the stored value.
     */
    public boolean get() {
        return mValue;
    }

    /**
     * Set the stored value.
     * @param value The new value
     */
    public void set(boolean value) {
        if (value != mValue) {
            mValue = value;
            notifyChange();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mValue ? 1 : 0);
    }

    public static final Creator<ObservableBoolean> CREATOR
            = new Creator<ObservableBoolean>() {

        @Override
        public ObservableBoolean createFromParcel(Parcel source) {
            return new ObservableBoolean(source.readInt() == 1);
        }

        @Override
        public ObservableBoolean[] newArray(int size) {
            return new ObservableBoolean[size];
        }
    };
}
