package de.pinyto.ctSESAMsync;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class SyncService extends Service {
    static final int REQUEST_SYNC = 1;
    static final int SEND_UPDATE = 2;

    public SyncService() {
    }

    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_SYNC:
                    new SyncServerRequest(getBaseContext(), msg.replyTo).execute(
                            "/ajax/read.php",
                            "",
                            Integer.toString(SyncServerRequest.SYNC_RESPONSE));
                    break;
                case SEND_UPDATE:
                    String updatedData = msg.getData().getString("updatedData");
                    new SyncServerRequest(getBaseContext(), msg.replyTo).execute(
                            "/ajax/write.php",
                            "data="+updatedData,
                            Integer.toString(SyncServerRequest.SEND_UPDATE_RESPONSE));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
