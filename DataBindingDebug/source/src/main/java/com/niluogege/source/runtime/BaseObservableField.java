package com.niluogege.source.runtime;

import androidx.databinding.Observable;

/**
 * A base class for ObservableField and its primitive siblings that captures the ability to
 * add dependencies.
 */
abstract class BaseObservableField extends BaseObservable {
    public BaseObservableField() {
    }

    public BaseObservableField(Observable... dependencies) {
        if (dependencies != null && dependencies.length != 0) {
            DependencyCallback callback = new DependencyCallback();

            for (int i = 0; i < dependencies.length; i++) {
                dependencies[i].addOnPropertyChangedCallback(callback);
            }
        }
    }

    class DependencyCallback extends OnPropertyChangedCallback {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            notifyChange();
        }
    }
}
