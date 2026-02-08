package com.inout.app;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Full-screen dialog for Admin to send feedback to the developer.
 * Uses Formspree API via OkHttp.
 */
public class ContactDevDialog extends DialogFragment {

    private EditText etName, etEmail, etCompany, etContent;
    private MaterialButton btnSend;
    private ImageButton btnClose;
    private ProgressBar progressBar;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set full screen style
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_contact_dev, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.et_dev_name);
        etEmail = view.findViewById(R.id.et_dev_email);
        etCompany = view.findViewById(R.id.et_dev_company);
        etContent = view.findViewById(R.id.et_dev_content);
        btnSend = view.findViewById(R.id.btn_send_feedback);
        btnClose = view.findViewById(R.id.btn_close_dev);
        progressBar = view.findViewById(R.id.progressBarDev);

        btnClose.setOnClickListener(v -> dismiss());

        btnSend.setOnClickListener(v -> validateAndSend());
    }

    private void validateAndSend() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String company = etCompany.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email required");
            return;
        }
        if (TextUtils.isEmpty(content)) {
            etContent.setError("Please enter your message");
            return;
        }

        sendToFormspree(name, email, company, content);
    }

    private void sendToFormspree(String name, String email, String company, String content) {
        progressBar.setVisibility(View.VISIBLE);
        btnSend.setEnabled(false);

        // Build the request body for Formspree
        RequestBody formBody = new FormBody.Builder()
                .add("name", name)
                .add("email", email)
                .add("company", company)
                .add("message", content)
                .build();

        Request request = new Request.Builder()
                .url("https://formspree.io/f/xyzenlao")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnSend.setEnabled(true);
                        Toast.makeText(getContext(), "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Feedback Sent Successfully!", Toast.LENGTH_LONG).show();
                            dismiss();
                        } else {
                            btnSend.setEnabled(true);
                            Toast.makeText(getContext(), "Server error. Please try again later.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }
}