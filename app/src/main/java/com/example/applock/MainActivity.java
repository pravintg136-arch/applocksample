package com.example.applock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private boolean isUnlocked = false;
    private LinearLayout lockScreen;
    private LinearLayout mainContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lockScreen  = findViewById(R.id.lockScreen);
        mainContent = findViewById(R.id.mainContent);

        Button btnFingerprint = findViewById(R.id.btnFingerprint);
        btnFingerprint.setOnClickListener(v -> checkBiometricSupport());

        Button btnLock = findViewById(R.id.btnLock);
        btnLock.setOnClickListener(v -> lockApp());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isUnlocked) {
            showLockScreen();
            checkBiometricSupport();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isUnlocked = false;
    }

    private void checkBiometricSupport() {
        BiometricManager bm = BiometricManager.from(this);
        int result = bm.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG |
            BiometricManager.Authenticators.DEVICE_CREDENTIAL);

        if (result == BiometricManager.BIOMETRIC_SUCCESS) {
            showBiometricPrompt();
        } else if (result == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
            Toast.makeText(this, "No fingerprint enrolled. Set one up in Settings.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_SECURITY_SETTINGS));
        } else {
            Toast.makeText(this, "Biometric not available on this device.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);

        BiometricPrompt prompt = new BiometricPrompt(this, executor,
            new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult r) {
                    super.onAuthenticationSucceeded(r);
                    unlockApp();
                }
                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                }
                @Override
                public void onAuthenticationError(int code, CharSequence msg) {
                    super.onAuthenticationError(code, msg);
                    Toast.makeText(MainActivity.this, "Error: " + msg, Toast.LENGTH_SHORT).show();
                }
            });

        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
            .setTitle("App Lock")
            .setSubtitle("Verify your identity to continue")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG |
                BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build();

        prompt.authenticate(info);
    }

    private void unlockApp() {
        isUnlocked = true;
        lockScreen.setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);
    }

    private void lockApp() {
        isUnlocked = false;
        showLockScreen();
        checkBiometricSupport();
    }

    private void showLockScreen() {
        lockScreen.setVisibility(View.VISIBLE);
        mainContent.setVisibility(View.GONE);
    }
}
