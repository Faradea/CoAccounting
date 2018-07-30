package com.macgavrina.co_accounting.rxjava

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

public class LoginInputObserver {

    public companion object LoginInputObserver {

        fun getTextWatcherObservable(editText: EditText): Observable<String> {

            val publishSubject:PublishSubject<String> = PublishSubject.create()

            editText.addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                }

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

                }

                override fun afterTextChanged(editable: Editable) {
                    publishSubject.onNext(editable.toString())
                }
            })

            return publishSubject
        }
    }
}