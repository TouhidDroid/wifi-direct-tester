package edu.rit.se.crashavoidance.views;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.rit.se.crashavoidance.R;
import edu.rit.se.wifibuddy.DnsSdService;
import edu.rit.se.wifibuddy.WifiDirectHandler;

/**
 * ListFragment that shows a list of available discovered services
 */
public class AvailableServicesFragment extends Fragment{

    private WiFiDirectHandlerAccessor wifiDirectHandlerAccessor;
    private List<DnsSdService> services = new ArrayList<>();
    private AvailableServicesListViewAdapter servicesListAdapter;
    private ListView deviceList;
    private Toolbar toolbar;
    private static final String TAG = WifiDirectHandler.TAG + "ServicesFragment";

    /**
     * Sets the Layout for the UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_available_services, container, false);
        toolbar = (Toolbar) getActivity().findViewById(R.id.mainToolbar);
        deviceList = (ListView)rootView.findViewById(R.id.device_list);
        prepareResetButton(rootView);
        setServiceList();
        services.clear();
        servicesListAdapter.notifyDataSetChanged();
        Log.d("TIMING", "Discovering started " + (new Date()).getTime());
        registerLocalP2pReceiver();
        getHandler().continuouslyDiscoverServices();
        return rootView;
    }

    private void prepareResetButton(View view){
        Button resetButton = (Button)view.findViewById(R.id.reset_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetServiceDiscovery();

            }
        });
    }

    /**
     * Sets the service list adapter to display available services
     */
    private void setServiceList() {
        servicesListAdapter = new AvailableServicesListViewAdapter((MainActivity) getActivity(), services);
        deviceList.setAdapter(servicesListAdapter);
    }

    /**
     * Onclick Method for the the reset button to clear the services list
     * and start discovering services again
     */
    private void resetServiceDiscovery(){
        // Clear the list, notify the list adapter, and start discovering services again
        Log.i(TAG, "Resetting Service discovery");
        services.clear();
        servicesListAdapter.notifyDataSetChanged();
        getHandler().resetServiceDiscovery();
    }

    private void registerLocalP2pReceiver() {
        Log.i(TAG, "Registering local P2P broadcast receiver");
        WifiDirectReceiver p2pBroadcastReceiver = new WifiDirectReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiDirectHandler.Action.DNS_SD_SERVICE_AVAILABLE);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(p2pBroadcastReceiver, intentFilter);
        Log.i(TAG, "Local P2P broadcast receiver registered");
    }

    /**
     * Receiver for receiving intents from the WifiDirectHandler to update UI
     * when Wi-Fi Direct commands are completed
     */
    public class WifiDirectReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get the intent sent by WifiDirectHandler when a service is found
            if (intent.getAction().equals(WifiDirectHandler.Action.DNS_SD_SERVICE_AVAILABLE)) {
                String serviceKey = intent.getStringExtra(WifiDirectHandler.SERVICE_MAP_KEY);
                DnsSdService service = getHandler().getDnsSdServiceMap().get(serviceKey);
                Log.d("TIMING", "Service Discovered and Accessed " + (new Date()).getTime());
                // Add the service to the UI and update
                servicesListAdapter.addUnique(service);
                // TO-DO Capture an intent that indicates the peer list has changed
                // and see if we need to remove anything from our list
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (toolbar != null) {
            toolbar.setTitle("Service Discovery");
        }
    }

    /**
     * Shortcut for accessing the wifi handler
     */
    private WifiDirectHandler getHandler() {
        return wifiDirectHandlerAccessor.getWifiHandler();
    }

    /**
     * This is called when the Fragment is opened and is attached to MainActivity
     * Sets the ListAdapter for the Service List and initiates the service discovery
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            wifiDirectHandlerAccessor = ((WiFiDirectHandlerAccessor) getActivity());
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement WiFiDirectHandlerAccessor");
        }
    }
}
