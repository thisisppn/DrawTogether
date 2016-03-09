package com.example.thisisppn.drawtogether;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.socketio.client.IO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.HashMap;

public class MainDrawingView extends View {
    private Paint paint = new Paint();
    private Path path = new Path();
    private Socket mSocket;
    private HashMap<String, Path> pathMap = new HashMap<String, Path>();

    public MainDrawingView(Context context, AttributeSet attrs) {

        super(context, attrs);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5f);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);

        try {
            mSocket = IO.socket("http://192.168.0.7:3000");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        mSocket.on("connected", onConnectEvent);
        mSocket.on("touch", onTouchEvent);
        mSocket.on("move", onMoveEvent);
        mSocket.connect();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        for (String key : pathMap.keySet()) {
            Path sockPath = pathMap.get(key);
            canvas.drawPath(sockPath, paint);
        }
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Get the coordinates of the touch event.
        float eventX = event.getX();
        float eventY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Set a new starting point
                path.moveTo(eventX, eventY);
//                mSocket.emit("touch", "{"+eventX+", "+eventY+"}");
                mSocket.emit("touch", eventX, eventY);
                return true;
            case MotionEvent.ACTION_MOVE:
                // Connect the points
                path.lineTo(eventX, eventY);
                mSocket.emit("move", eventX, eventY);
                break;
            default:
                return false;
        }

        // Makes our view repaint and call onDraw
        invalidate();
        return true;
    }


    public Emitter.Listener onConnectEvent = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    JSONObject data = (JSONObject) args[0];
                    String id = null;
                    String ids = null;

                    try {
                        id = data.getString("id");
                        ids = data.getString("ids");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    System.out.println(ids);

                    if (ids != null) {
                        JSONArray connectedIds = null;
                        try {
                            connectedIds = new JSONArray(ids);
                            for (int i = 0; i < connectedIds.length(); i++) {
                                try {
                                    String connectedId = connectedIds.getString(i);
                                    Log.v("Already connected", connectedId);
                                    pathMap.put(connectedId, new Path());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }


                    Path tempPath = new Path();
                    pathMap.put(id, tempPath);
                    Log.v("Connection", id + " connected");
//                    for (String name: pathMap.keySet()){
//
//                        String key =name.toString();
//                        String value = pathMap.get(name).toString();
//                        System.out.println(key + " " + value);
//                    }
                    invalidate();
                }
            });
        }
    };

    public Emitter.Listener onTouchEvent = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Code for the UiThread
                    JSONObject data = (JSONObject) args[0];
                    String x = null;
                    String y = null;
                    String id = null;
                    try {
                        x = data.getString("touchX");
                        y = data.getString("touchY");
                        id = data.getString("id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.v("Touch", x + ", " + y);
                    Log.v("Touch from id", id);
                    float touchX = Float.parseFloat(x);
                    float touchY = Float.parseFloat(y);
                    Path sockPath = pathMap.get(id);
                    sockPath.moveTo(touchX, touchY);
//                    path.moveTo(touchX + 200, touchY + 200);
                    invalidate();
                }
            });
        }
    };

    public Emitter.Listener onMoveEvent = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            ((Activity) getContext()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String x = null;
                    String y = null;
                    String id = null;
                    try {
                        x = data.getString("touchX");
                        y = data.getString("touchY");
                        id = data.getString("id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Log.v("Move", x + ", " + y);
                    float touchX = Float.parseFloat(x);
                    float touchY = Float.parseFloat(y);
                    Path sockPath = pathMap.get(id);
                    System.out.println(sockPath);
                    sockPath.lineTo(touchX, touchY);
//                    path.lineTo(touchX+200, touchY+200);
                    invalidate();
                }
            });
        }
    };


}