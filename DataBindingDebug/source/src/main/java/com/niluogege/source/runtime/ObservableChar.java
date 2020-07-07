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
 * An observable class that holds a primitive char.
 * <p>
 * Observable field classes may be used instead of creating an Observable object. It can also
 * create a calculated field, depending on other fields:
 * <pre><code>public class MyDataObject {
 *     public final ObservableChar firstInitial = new ObservableChar();
 *     public final ObservableChar firstInitialCapitalized = new ObservableChar(firstInitial)
 *         &#64;Override
 *         public char get() { return Character.toUpperCase(firstInitial.get()); }
 *     };
 * }</code></pre>
 * Fields of this type should be declared final because bindings only detect changes in the
 * field's value, not of the field itself.
 * <p>
 * This class is parcelable and serializable but callbacks are ignored when the object is
 * parcelled / serialized. Unless you add custom callbacks, this will not be an issue because
 * data binding framework always re-registers callbacks when the view is bound. A parceled
 * ObservableChar will lose its dependencies.
 */
public class ObservableChar extends BaseObservableField implements Parcelable, Serializable {
    static final long serialVersionUID = 1L;
    private char mValue;

    /**
     * Creates an ObservableChar with the given initial value.
     *
     * @param value the initial value for the ObservableChar
     */
    public ObservableChar(char value) {
        mValue = value;
    }

    /**
     * Creates an ObservableChar with the initial value of <code>0</code>.
     */
    public ObservableChar() {
    }

    /**
     * Creates an ObservableChar that depends on {@code dependencies}. Typically,
     * {@link ObservableField}s are passed as dependencies. When any dependency
     * notifies changes, this ObservableChar also notifies a change.
     *
     * @param dependencies The Observables that this ObservableChar depends on.
     */
    public ObservableChar(Observable... dependencies) {
        super(dependencies);
    }

    /**
     * @return the stored value.
     */
    public char get() {
        return mValue;
    }

    /**
     * Set the stored value.
     *
     * @param value The new value
     */
    public void set(char value) {
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
        dest.writeInt(mValue);
    }

    public static final Creator<ObservableChar> CREATOR
            = new Creator<ObservableChar>() {

        @Override
        public ObservableChar createFromParcel(Parcel source) {
            return new ObservableChar((char) source.readInt());
        }

        @Override
        public ObservableChar[] newArray(int size) {
            return new ObservableChar[size];
        }
    };
}
