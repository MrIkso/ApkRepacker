package com.mrikso.apkrepacker.fragment.dialogs.bottomsheet;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ReplaceInFileDialogViewModel extends AndroidViewModel {

    private MutableLiveData<String> mSearchEditData = new MutableLiveData<>();
    private MutableLiveData<String> mReplaceEditData = new MutableLiveData<>();

    public ReplaceInFileDialogViewModel(@NonNull Application application) {
        super(application);
    }

    public void setSearchEditData(String data) {
        mSearchEditData.postValue(data);
    }

    public void setReplaceEditData(String data) {
        mReplaceEditData.postValue(data);
    }

    public LiveData<String> getSearchEditData() {
        return mSearchEditData;
    }

    public LiveData<String> getReplaceEditData() {
        return mReplaceEditData;
    }

}
