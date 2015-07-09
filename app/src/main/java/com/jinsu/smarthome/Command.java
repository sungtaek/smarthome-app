package com.jinsu.smarthome;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


/**
 * Created by 이성택 on 2015-06-18.
 */
public class Command  {
    private TextView resultView;
    private Socket socket;
    private Boolean isConnected = false;

    public Command(TextView resultView) {
        this.resultView = resultView;
    }
    
    public enum Action {
        LED_ON      ("Led ON", "led", "on", "불꺼", "꺼"),
        LED_OFF     ("Led OFF", "led", "off", "불켜", "켜"),
        TEMPERATURE ("Temperature", "thermometer", "get", "온도", "지금온도", "현재온도"),
        HUMIDITY    ("Humidity", "hygrometer", "get", "습도", "지금습도", "현재습도"),


        UNKNOWN(null, null, null, null);

        private String name;
        private String target;
        private String command;
        private String[] texts;

        private Action(String name, String target, String command, String... texts) {
            this.name = name;
            this.target = target;
            this.command = command;
            this.texts = texts;
        }

        public static Action search(String text) {
            for(Action action: Action.values()) {
                if(action.texts != null) {
                    for(String t: action.texts) {
                        if(t.equalsIgnoreCase(text))
                            return action;
                    }
                }
            }
            return null;
        }
    }
    
    public void connect(String url) throws URISyntaxException {
        socket = IO.socket(url);

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onConnect();
            }
        });
        socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onDisconnect();
            }
        });
        socket.on("join", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onJoin((JSONObject) args[0]);
            }
        });
        socket.on("result", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                onResult((JSONObject) args[0]);
            }
        });

        socket.connect();
    }

    public void close() {
        socket.off();
        socket.close();
    }

    public JSONObject getAction(String cmd) throws JSONException {
        Action action = null;
        JSONObject actionData = null;

        cmd = cmd.replaceAll("\\s+","");
        Log.d("CMD", "command [" + cmd + "]");

        action = Action.search(cmd);
        if(action != null) {
            actionData = new JSONObject();
            actionData.put("name", action.name);
            actionData.put("source", "user");
            actionData.put("target", action.target);
            actionData.put("command", action.command);
        }

        return actionData;
    }

    public void send(JSONObject action) throws JSONException {
        /*
        RestTask r = new RestTask(this);
        r.execute("http://52.25.241.254/control/sungtaek", "POST", action.toString());
        */
        Log.d("CMD", "Socket emit action ~");
        Log.d("CMD", action.toString());
        socket.emit("action", action);
    }

    private void onDisconnect() {
        Log.i("CMD", "Socket Disconnect!");
        isConnected = false;
    }

    private void onConnect() {
        Log.i("CMD", "Socket Connect!");
        JSONObject account = new JSONObject();
        try {
            account.put("home", "sungtaek");
            account.put("agent", "user");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("CMD", "Socket emit account ~");
        Log.d("CMD", account.toString());
        socket.emit("join", account);

        resultView.post(new Runnable() {
            @Override
            public void run() {
                resultView.setText("Welcome!");
            }
        });
        isConnected = true;
    }

    private void onJoin(JSONObject account) {
        Log.i("CMD", "Join!");
        Log.i("CMD", "<- " + account.toString());
    }

    private void onResult(JSONObject result) {
        Log.i("CMD", "Result!");
        Log.i("CMD", "<- " + result.toString());
        String strResult = null;

        try {
            strResult = (String)result.get("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(strResult != null) {
            final String finalStrResult = strResult;
            resultView.post(new Runnable() {
                @Override
                public void run() {
                    resultView.setText(finalStrResult);
                }
            });
        }
        else {
            resultView.post(new Runnable() {
                @Override
                public void run() {
                    resultView.setText("Error");
                }
            });
        }
    }

    public Boolean isConnected() {
        return isConnected;
    }

}
