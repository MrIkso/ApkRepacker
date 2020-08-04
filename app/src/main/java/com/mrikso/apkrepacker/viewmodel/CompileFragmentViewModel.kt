package com.mrikso.apkrepacker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class CompileFragmentViewModel(application: Application) : AndroidViewModel(application)
{
    private val mLogMutableLiveData = MutableLiveData<String>()

}