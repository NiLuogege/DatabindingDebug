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
 * An observable class that holds a primitive double. It can also
 * create a calculated field, depending on other fields:
 * <p>
 * Observable field classes may be used instead of creating an Observable object:
 * <pre><code>public class MyDataObject {
 *     public final ObservableDouble temperatureC = new ObservableDouble();
 *     public final ObservableDouble temperatureF = new ObservableDouble(temperatureC) {
 *         &#64;Override
 *         public double get() { return (temperatureC.get() * 9 / 5) + 32; }
 *     };
 * }</code></pre>
 * Fields of this type should be declared final because bindings only detect changes in the
 * field's value, not of the field itself.
 * <p>
 * This class is parcelable and serializable but callbacks are ignored when the object is
 * parcelled / serialized. Unless you add custom callbacks, this will not be an issue because
 * data binding framework always re-registers callbacks when the view is bound. A parceled
 * ObservableByte will lose its dependencies.
 */
public class ObservableDouble extends BaseObservableField implements Parcelable, Serializable {
    static final long serialVersionUID = 1L;
    private double mValue;

    /**
     * Creates an ObservableDouble with the given initial value.
     *
     * @param value the initial value for the ObservableDouble
     */
    public ObservableDouble(double value) {
        mValue = value;
    }

    /**
     * Creates an ObservableDouble with the initial value of <code>0</code>.
     */
    public ObservableDouble() {
    }

    /**
     * Creates an ObservableDouble that depends on {@code dependencies}. Typically,
     * {@link ObservableField}s are passed as dependencies. When any dependency
     * notifies changes, this ObservableDouble also notifies a change.
     *
     * @param dependencies The Observables that this ObservableDouble depends on.
     */
    public ObservableDouble(Observable... dependencies) {
        super(dependencies);
    }

    /**
     * @return the stored value.
     */
    public double get() {
        return mValue;
    }

    /**
     * Set the stored value.
     *
     * @param value The new value
     */
    public void set(double value) {
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
        dest.writeDouble(mValue);
    }

    public static final Creator<ObservableDouble> CREATOR
            = new Creator<ObservableDouble>() {

        @Override
        public ObservableDouble createFromParcel(Parcel source) {
            return new ObservableDouble(source.readDouble());
        }

        @Override
        public ObservableDouble[] newArray(int size) {
            return new ObservableDouble[size];
        }
    };
}
