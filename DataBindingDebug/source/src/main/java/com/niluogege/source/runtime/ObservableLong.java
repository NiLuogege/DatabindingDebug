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
 * An observable class that holds a primitive long.
 * <p>
 * Observable field classes may be used instead of creating an Observable object. It can also
 * create a calculated field, depending on other fields:
 * <pre><code>public class MyDataObject {
 *     public final ObservableLong friendCount = new ObservableLong();
 *     public final ObservableLong familyCount = new ObservableLong();
 *     public final ObservableLong knownCount = new ObservableLong(friendCount, familyCount) {
 *         &#64;Override
 *         public long get() { return friendCount.get() + familyCount.get(); }
 *     };
 * }</code></pre>
 * Fields of this type should be declared final because bindings only detect changes in the
 * field's value, not of the field itself.
 * <p>
 * This class is parcelable and serializable but callbacks are ignored when the object is
 * parcelled / serialized. Unless you add custom callbacks, this will not be an issue because
 * data binding framework always re-registers callbacks when the view is bound. A parceled
 * ObservableLong will lose its dependencies.
 */
public class ObservableLong extends BaseObservableField implements Parcelable, Serializable {
    static final long serialVersionUID = 1L;
    private long mValue;

    /**
     * Creates an ObservableLong with the given initial value.
     *
     * @param value the initial value for the ObservableLong
     */
    public ObservableLong(long value) {
        mValue = value;
    }

    /**
     * Creates an ObservableLong with the initial value of <code>0L</code>.
     */
    public ObservableLong() {
    }

    /**
     * Creates an ObservableLong that depends on {@code dependencies}. Typically,
     * {@link ObservableField}s are passed as dependencies. When any dependency
     * notifies changes, this ObservableLong also notifies a change.
     *
     * @param dependencies The Observables that this ObservableLong depends on.
     */
    public ObservableLong(Observable... dependencies) {
        super(dependencies);
    }

    /**
     * @return the stored value.
     */
    public long get() {
        return mValue;
    }

    /**
     * Set the stored value.
     *
     * @param value The new value
     */
    public void set(long value) {
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
        dest.writeLong(mValue);
    }

    public static final Creator<ObservableLong> CREATOR
            = new Creator<ObservableLong>() {

        @Override
        public ObservableLong createFromParcel(Parcel source) {
            return new ObservableLong(source.readLong());
        }

        @Override
        public ObservableLong[] newArray(int size) {
            return new ObservableLong[size];
        }
    };
}
