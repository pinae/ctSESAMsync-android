package de.pinyto.passwordsettingssync;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class can establish https connections and make requests.
 */
public class SyncServerRequest extends AsyncTask<String, Void, String> {

    static final int SYNC_RESPONSE = 1;
    static final int SEND_UPDATE_RESPONSE = 2;

    final Context contentContext;
    private Messenger replyMessenger;

    public SyncServerRequest(Context contentContext, Messenger replyTo) {
        this.contentContext = contentContext;
        this.replyMessenger = replyTo;
    }

    @Override
    protected String doInBackground(String... params) {
        String path = params[0];
        String request = params[1];
        int responseType = Integer.parseInt(params[2]);
        SyncServerConnection connection = new SyncServerConnection(this.contentContext);
        return connection.makeRequest(path, request, responseType);
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject resultObject = new JSONObject(result);
            Message resp = Message.obtain(null, resultObject.getInt("responseType"));
            Bundle bResp = new Bundle();
            bResp.putString("respData", resultObject.getString("response"));
            resp.setData(bResp);
            replyMessenger.send(resp);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            Log.d("Request error", "Could not unpack response.");
            e.printStackTrace();
        }
    }
}
