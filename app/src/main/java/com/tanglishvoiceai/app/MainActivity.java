package com.tanglishvoiceai.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int AUDIO_REQUEST = 200;
    private TextView status;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(247,249,252));
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(24), dp(40), dp(24), dp(24));
        scroll.addView(panel);

        TextView title = new TextView(this);
        title.setText("Tanglish Voice AI");
        title.setTextSize(29);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(Color.rgb(17,24,39));
        panel.addView(title);

        TextView description = new TextView(this);
        description.setText("Speak Tamil or English from a floating microphone while using your chat apps. Tamil speech is converted into simple Latin-letter Tanglish and copied for pasting.");
        description.setTextColor(Color.rgb(75,85,99));
        description.setTextSize(16);
        description.setPadding(0, dp(12), 0, dp(25));
        panel.addView(description);

        Button start = makeButton("START FLOATING MIC", Color.rgb(37,99,235));
        start.setOnClickListener(v -> startFloatingMic());
        panel.addView(start);
        Button stop = makeButton("STOP FLOATING MIC", Color.rgb(55,65,81));
        stop.setOnClickListener(v -> {
            stopService(new Intent(this, FloatingMicService.class));
            status.setText("Floating microphone stopped.");
        });
        LinearLayout.LayoutParams stopParams = new LinearLayout.LayoutParams(-1, dp(54));
        stopParams.topMargin = dp(12);
        panel.addView(stop, stopParams);

        status = new TextView(this);
        status.setText("1. Tap Start.\n2. Grant microphone and display-over-other-apps access.\n3. Tap Start again if requested.\n4. Open WhatsApp / Instagram and tap the floating mic.\n5. Paste the copied text into your chat.\n\nTap TA/EN on the bubble to switch languages. You can drag the bubble around.");
        status.setTextSize(15);
        status.setTextColor(Color.rgb(55,65,81));
        status.setPadding(0, dp(25), 0, 0);
        panel.addView(status);
        setContentView(scroll);
    }

    private Button makeButton(String text, int color) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setTextColor(Color.WHITE);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(14));
        b.setBackground(bg);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(56));
        b.setLayoutParams(p);
        return b;
    }

    private void startFloatingMic() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_REQUEST);
            status.setText("Allow microphone access, then tap Start Floating Mic again.");
            return;
        }
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            status.setText("Allow display over other apps, return here, and tap Start Floating Mic again.");
            return;
        }
        try {
            Intent intent = new Intent(this, FloatingMicService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent);
            else startService(intent);
            status.setText("Floating mic started. Switch to your chat app. Tap 🎙 and paste the result from your clipboard. Use TA/EN to switch languages.");
        } catch (Exception e) {
            status.setText("Could not start floating mic: " + e.getMessage());
        }
    }
    private int dp(float size) { return Math.round(size * getResources().getDisplayMetrics().density); }
}
