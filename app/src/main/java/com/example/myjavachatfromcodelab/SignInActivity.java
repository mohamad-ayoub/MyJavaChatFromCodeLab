package com.example.myjavachatfromcodelab;

import static java.util.List.*;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.myjavachatfromcodelab.databinding.ActivitySignInBinding;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignInActivity extends AppCompatActivity {
    final String TAG = "MyChat:" + getClass().getSimpleName();

    private ActivitySignInBinding binding ;
    ;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private ActivityResultLauncher<Intent> signIn = registerForActivityResult(new FirebaseAuthUIActivityResultContract(), new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
        @Override
        public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Log.d(TAG, "Sign in successful!");
                goToMainActivity();
            } else {
                Toast.makeText(SignInActivity.this,
                        "There was an error signing in",
                        Toast.LENGTH_LONG).show();

                IdpResponse response = result.getIdpResponse();
                if (response == null) {
                    Log.w(TAG, "Sign in canceled");
                } else {
                    Log.w(TAG, "Sign in error", response.getError());
                }
                SignInActivity.this.finish();
            }
        }
    });

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // If there is no signed in user, launch FirebaseUI
        // Otherwise head to MainActivity
        if (auth.getCurrentUser() == null) {
            // Sign in with FirebaseUI, see docs for more details:
            // https://firebase.google.com/docs/auth/android/firebaseui
            List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(), new AuthUI.IdpConfig.GoogleBuilder().build());

            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setLogo(R.mipmap.ic_launcher)
                    .setAvailableProviders(providers).build();

            signIn.launch(signInIntent);
        } else {
            goToMainActivity();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        binding=null;
    }
}