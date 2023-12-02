package eu.rg_engineering.simplemusicplayer.TwonkyServer;

import static android.os.Looper.getMainLooper;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pUpnpServiceRequest;
import android.util.Log;

public class discoverServer {

    private String TAG = "discoverServer";

    private Context mContext;

    public discoverServer(Context context){
        mContext = context;
    }


    public void start(){
        Log.d(TAG, "start discovery ");

        //Initialisiere das WifiP2pManager-Objekt:
        WifiP2pManager wifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel channel = wifiP2pManager.initialize(mContext, getMainLooper(), null);

        //Erstelle eine Instanz von WifiP2pUpnpServiceRequest:
        WifiP2pUpnpServiceRequest upnpServiceRequest = WifiP2pUpnpServiceRequest.newInstance();


        //Registriere einen Broadcast-Empfänger, um die Ergebnisse des UPnP-Dienst-Scans zu empfangen:
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(intent.getAction())) {
                    int discoveryState = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);

                    if (discoveryState == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {
                        // Der Dienst-Scan hat begonnen
                        Log.d(TAG, "Der Dienst-Scan hat begonnen ");
                    } else if (discoveryState == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
                        // Der Dienst-Scan wurde gestoppt
                        Log.d(TAG, "Der Dienst-Scan wurde gestoppt ");
                    }
                }
            }
        };


        //Starte den Dienst-Scan und füge WifiP2pUpnpServiceRequest hinzu:
        wifiP2pManager.addServiceRequest(channel, upnpServiceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // Service-Anfrage erfolgreich hinzugefügt, starte den Dienst-Scan
                wifiP2pManager.discoverServices(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Dienst-Scan wurde gestartet
                        Log.d(TAG, "Der Dienst-Scan wurde gestarte ");
                    }

                    @Override
                    public void onFailure(int reason) {
                        // Fehler beim Starten des Dienst-Scans
                        Log.e(TAG, "Fehler beim Starten des Dienst-Scans ");
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                // Fehler beim Hinzufügen der Service-Anfrage
                Log.e(TAG, "Fehler beim Hinzufügen der Service-Anfrage ");
            }
        });


    }

}
