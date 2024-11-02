package com.iffelse.iastro.view.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.iffelse.iastro.utils.Utils;

public class NetworkChangeReceiver extends BroadcastReceiver {
    private static MutableLiveData<Boolean> networkChange = null;

    public NetworkChangeReceiver() {
        networkChange = new MutableLiveData<>();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        networkChange.setValue(Utils.INSTANCE.getConnectivityStatus(context));
    }

    public static LiveData<Boolean> getStatus() {
        return networkChange;
    }

}
