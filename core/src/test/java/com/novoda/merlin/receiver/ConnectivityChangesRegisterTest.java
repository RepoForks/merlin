package com.novoda.merlin.receiver;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkRequest;
import android.os.Build;

import com.novoda.merlin.service.AndroidVersion;
import com.novoda.merlin.service.MerlinService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ConnectivityChangesRegisterTest {

    @Mock
    private Context context;
    @Mock
    private ConnectivityManager connectivityManager;
    @Mock
    private AndroidVersion androidVersion;
    @Mock
    private MerlinService merlinService;

    private ConnectivityChangesRegister connectivityChangesRegister;

    @Before
    public void setUp() {
        initMocks(this);

        connectivityChangesRegister = new ConnectivityChangesRegister(context, connectivityManager, androidVersion, merlinService);
    }

    @Test
    public void givenRegisteredBroadcastReceiver_whenBindingForASecondTime_thenOriginalBroadcastReceieverIsRegisteredAgain() {
        ArgumentCaptor<ConnectivityReceiver> broadcastReceiver = givenRegisteredBroadcastReceiver();

        connectivityChangesRegister.register();

        verify(context, times(2)).registerReceiver(eq(broadcastReceiver.getValue()), refEq(new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)));
    }

    @Test
    public void givenRegisteredBroadcastReceiver_whenUnbinding_thenUnregistersOriginallyRegisteredBroadcastReceiver() {
        ArgumentCaptor<ConnectivityReceiver> broadcastReceiver = givenRegisteredBroadcastReceiver();

        connectivityChangesRegister.unregister();

        verify(context).unregisterReceiver(broadcastReceiver.getValue());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Test
    public void givenRegisteredMerlinNetworkCallbacks_whenBindingForASecondTime_thenOriginalNetworkCallbacksIsRegisteredAgain() {
        ArgumentCaptor<ConnectivityCallbacks> merlinNetworkCallback = givenRegisteredMerlinNetworkCallbacks();

        connectivityChangesRegister.register();

        verify(connectivityManager, times(2)).registerNetworkCallback(refEq((new NetworkRequest.Builder()).build()), eq(merlinNetworkCallback.getValue()));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Test
    public void givenRegisteredMerlinNetworkCallback_whenUnbinding_thenUnregistersOriginallyRegisteredNetworkCallbacks() {
        ArgumentCaptor<ConnectivityCallbacks> merlinNetworkCallback = givenRegisteredMerlinNetworkCallbacks();

        connectivityChangesRegister.unregister();

        verify(connectivityManager).unregisterNetworkCallback(merlinNetworkCallback.getValue());
    }

    private ArgumentCaptor<ConnectivityReceiver> givenRegisteredBroadcastReceiver() {
        when(androidVersion.isLollipopOrHigher()).thenReturn(false);
        connectivityChangesRegister.register();
        ArgumentCaptor<ConnectivityReceiver> argumentCaptor = ArgumentCaptor.forClass(ConnectivityReceiver.class);
        verify(context).registerReceiver(argumentCaptor.capture(), refEq(new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)));
        return argumentCaptor;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private ArgumentCaptor<ConnectivityCallbacks> givenRegisteredMerlinNetworkCallbacks() {
        when(androidVersion.isLollipopOrHigher()).thenReturn(true);
        connectivityChangesRegister.register();
        ArgumentCaptor<ConnectivityCallbacks> argumentCaptor = ArgumentCaptor.forClass(ConnectivityCallbacks.class);
        verify(connectivityManager).registerNetworkCallback(refEq((new NetworkRequest.Builder()).build()), argumentCaptor.capture());
        return argumentCaptor;
    }

}
