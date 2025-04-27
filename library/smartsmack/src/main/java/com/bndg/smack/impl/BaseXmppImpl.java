package com.bndg.smack.impl;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * author : r
 * time   : 2024/6/8 2:06 PM
 * desc   :
 */
class BaseXmppImpl {
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    public void addDisposable(Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    public void removeDisposable(Disposable disposable) {
        compositeDisposable.remove(disposable);
    }

    public void clearDisposable() {
        compositeDisposable.clear();
    }
}
