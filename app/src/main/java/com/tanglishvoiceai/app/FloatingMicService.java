package com.tanglishvoiceai.app;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.*;
import android.widget.*;
import java.util.ArrayList;

public class FloatingMicService extends Service {
    private static final String CHANNEL_ID = "tanglish_voice_mic";
    private WindowManager windowManager;
    private LinearLayout bubble;
    private WindowManager.LayoutParams windowParams;
    private TextView micText, languageText, resultText;
    private SpeechRecognizer recognizer;
    private boolean listening = false;
    private String language = "ta-IN";

    @Override public void onCreate() {
        super.onCreate();
        createNotification();
        showBubble();
    }

    private void createNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Tanglish Floating Microphone", NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Keeps the floating microphone available while using other apps.");
            nm.createNotificationChannel(channel);
        }
        PendingIntent open = PendingIntent.getActivity(this, 0,
            new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("Tanglish Voice AI")
            .setContentText("Floating mic active • open app to stop")
            .setContentIntent(open)
            .setOngoing(true)
            .build();
        if (Build.VERSION.SDK_INT >= 29) startForeground(31, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        else startForeground(31, notification);
    }

    private void showBubble() {
        if (!android.provider.Settings.canDrawOverlays(this)) { stopSelf(); return; }
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        bubble = new LinearLayout(this);
        bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setPadding(dp(8), dp(8), dp(8), dp(8));
        bubble.setBackground(round(Color.rgb(19,26,45), 20));
        bubble.setElevation(dp(10));
        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        bubble.addView(controls);

        micText = new TextView(this);
        micText.setText("🎙");
        micText.setTextSize(26);
        micText.setGravity(Gravity.CENTER);
        micText.setTextColor(Color.WHITE);
        micText.setContentDescription("Tap to dictate, drag to move");
        micText.setBackground(round(Color.rgb(37,99,235), 18));
        controls.addView(micText, new LinearLayout.LayoutParams(dp(60), dp(58)));

        languageText = new TextView(this);
        languageText.setText("TA");
        languageText.setTextSize(14);
        languageText.setTypeface(null, Typeface.BOLD);
        languageText.setTextColor(Color.WHITE);
        languageText.setGravity(Gravity.CENTER);
        languageText.setContentDescription("Change recognition language");
        LinearLayout.LayoutParams langLayout = new LinearLayout.LayoutParams(dp(43), dp(58));
        langLayout.leftMargin = dp(6);
        controls.addView(languageText, langLayout);
        languageText.setOnClickListener(v -> {
            if (listening) { setResult("Finish speaking first"); return; }
            language = language.equals("ta-IN") ? "en-IN" : "ta-IN";
            languageText.setText(language.equals("ta-IN") ? "TA" : "EN");
            setResult(language.equals("ta-IN") ? "Tamil mode" : "English mode");
        });

        resultText = new TextView(this);
        resultText.setText("Tap mic to speak");
        resultText.setMaxLines(3);
        resultText.setTextSize(12);
        resultText.setTextColor(Color.WHITE);
        resultText.setPadding(dp(4), dp(5), dp(4), 0);
        bubble.addView(resultText, new LinearLayout.LayoutParams(dp(115), -2));

        windowParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT);
        windowParams.gravity = Gravity.TOP | Gravity.START;
        windowParams.x = getResources().getDisplayMetrics().widthPixels - dp(145);
        windowParams.y = getResources().getDisplayMetrics().heightPixels / 3;
        windowManager.addView(bubble, windowParams);

        micText.setOnClickListener(v -> {
            if (listening) { if (recognizer != null) recognizer.stopListening(); }
            else startRecognition();
        });
        micText.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY; private float downX, downY;
            private boolean moved;
            @Override public boolean onTouch(View v, android.view.MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = windowParams.x; initialY = windowParams.y;
                        downX = event.getRawX(); downY = event.getRawY(); moved = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float dx = event.getRawX() - downX;
                        float dy = event.getRawY() - downY;
                        if (Math.abs(dx) > dp(7) || Math.abs(dy) > dp(7)) moved = true;
                        if (moved) {
                            windowParams.x = Math.max(0, initialX + Math.round(dx));
                            windowParams.y = Math.max(0, initialY + Math.round(dy));
                            if (windowManager != null && bubble != null) windowManager.updateViewLayout(bubble, windowParams);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!moved) v.performClick();
                        return true;
                    default: return true;
                }
            }
        });
    }

    private void startRecognition() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            setResult("Speech recognition unavailable. Install/enable Google speech services.");
            return;
        }
        try {
            if (recognizer != null) { recognizer.destroy(); recognizer = null; }
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                @Override public void onReadyForSpeech(Bundle params) { setResult("Listening... speak now"); }
                @Override public void onBeginningOfSpeech() { setResult("Hearing you..."); }
                @Override public void onRmsChanged(float rmsdB) { }
                @Override public void onBufferReceived(byte[] buffer) { }
                @Override public void onEndOfSpeech() { setResult("Converting..."); }
                @Override public void onError(int error) {
                    listening = false; micText.setText("🎙");
                    setResult("Try again (speech error " + error + ")");
                }
                @Override public void onResults(Bundle results) {
                    listening = false; micText.setText("🎙");
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches == null || matches.isEmpty()) { setResult("No words heard"); return; }
                    String spoken = matches.get(0);
                    String output = TanglishConverter.convert(spoken);
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    clipboard.setPrimaryClip(ClipData.newPlainText("Tanglish text", output));
                    setResult("Copied: " + output);
                    Toast.makeText(FloatingMicService.this, "Copied! Paste into your chat", Toast.LENGTH_SHORT).show();
                }
                @Override public void onPartialResults(Bundle partialResults) { }
                @Override public void onEvent(int eventType, Bundle params) { }
            });
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language);
            i.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
            listening = true; micText.setText("🔴");
            setResult("Starting mic...");
            recognizer.startListening(i);
        } catch (Exception e) {
            listening = false; micText.setText("🎙");
            setResult("Speech error: " + e.getClass().getSimpleName());
        }
    }

    private void setResult(String text) { if (resultText != null) resultText.setText(text); }
    private GradientDrawable round(int color, int radiusDp) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radiusDp)); return d;
    }
    private int dp(float n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    @Override public IBinder onBind(Intent intent) { return null; }
    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    @Override public void onDestroy() {
        if (recognizer != null) { recognizer.cancel(); recognizer.destroy(); }
        if (windowManager != null && bubble != null) windowManager.removeView(bubble);
        super.onDestroy();
    }
}
