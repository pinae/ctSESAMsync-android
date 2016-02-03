package de.pinyto.ctSESAMsync;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

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
    private View popupAnchor;
    private String path = "";
    private String requestPayload = "";
    private int responseType = SyncServerRequest.SYNC_RESPONSE;
    private View certExists;

    public SyncServerRequest(Context contentContext, Messenger replyTo,
                             View popupAnchor, View certExists) {
        this.contentContext = contentContext;
        this.replyMessenger = replyTo;
        this.popupAnchor = popupAnchor;
        this.certExists = certExists;
    }

    @Override
    protected String doInBackground(String... params) {
        this.path = params[0];
        this.requestPayload = params[1];
        this.responseType = Integer.parseInt(params[2]);
        SyncServerConnection connection = new SyncServerConnection(this.contentContext);
        return connection.makeRequest(this.path, this.requestPayload, this.responseType);
    }

    @Override
    protected void onPostExecute(String result) {
        try {
            JSONObject resultObject = new JSONObject(result);
            if (resultObject.has("error") && this.popupAnchor != null &&
                resultObject.getString("error").equals("certificate error")) {
                new CertificateLoadingRequest(
                    this.contentContext,
                    this.popupAnchor,
                    this.replyMessenger,
                    this.certExists).execute(
                        this.path,
                        this.requestPayload,
                        Integer.toString(this.responseType));
                return;
            }
            Message resp = Message.obtain(null, resultObject.getInt("responseType"));
            Bundle bResp = new Bundle();
            bResp.putString("respData", resultObject.getString("response"));
            resp.setData(bResp);
            replyMessenger.send(resp);
        } catch (RemoteException | JSONException e) {
            e.printStackTrace();
        }
    }
}
