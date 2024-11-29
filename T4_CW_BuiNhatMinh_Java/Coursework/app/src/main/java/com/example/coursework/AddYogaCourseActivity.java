package com.example.coursework;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddYogaCourseActivity extends AppCompatActivity {

    private EditText dayOfWeekEditText, timeEditText, capacityEditText, durationEditText, priceEditText,
            classTypeEditText, descriptionEditText, teacherEditText, imageText;
    private Button saveButton, backButton;
    private YogaCourseDBHelper dbHelper;
    private ImageCapture imageCapture;
    private Executor executor = Executors.newSingleThreadExecutor();

    private static final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final String[] REQUIRED_PERMISSIONS = new String[] {
            "android.permission.CAMERA", "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"
    };

    private ImageView imageView;
    private EditText inputPictureUri;
    private PreviewView previewView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_yoga_course);

        dbHelper = new YogaCourseDBHelper(this);

        // Initialize EditText views
        dayOfWeekEditText = findViewById(R.id.dayOfWeekEditText);
        timeEditText = findViewById(R.id.timeEditText);
        capacityEditText = findViewById(R.id.capacityEditText);
        durationEditText = findViewById(R.id.durationEditText);
        priceEditText = findViewById(R.id.priceEditText);
        classTypeEditText = findViewById(R.id.classTypeEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        teacherEditText = findViewById(R.id.teacherEditText);
        imageText = findViewById(R.id.textImage);

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        } else {
            startCamera();
        }

        imageView = findViewById(R.id.imageView);
        previewView = findViewById(R.id.previewView);
        inputPictureUri = findViewById(R.id.textImage);
        Button btn = findViewById(R.id.buttonAction);

        btn.setOnClickListener(view -> {
            final String[] items = {"Take photo", "Choose from gallery", "View picture from an URI"};
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setItems(items, (diaglog, item) -> {
                if (items[item].equals("Take photo")) {
                    Toast.makeText(this, "Take photo", Toast.LENGTH_LONG).show();
                    takePhoto();
                } else if (items[item].equals("Choose from gallery")) {
                    Toast.makeText(this, "Choose from gallery", Toast.LENGTH_LONG).show();
                    selectPictureFromGallery();
                } else if (items[item].equals("View picture from an URI")) {
                    Toast.makeText(this, "View picture from an URI", Toast.LENGTH_LONG).show();
                    String uri = inputPictureUri.getText().toString();
                    File file = new File(uri);
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    imageView.setImageBitmap(bitmap);
                    diaglog.dismiss();
                }
            });

            dlg.show();
        });

        // Initialize Save button
        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> {
            try {
                saveYogaCourse();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        // Initialize Back button
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());  // Close the activity without saving
    }

    private void saveYogaCourse() throws JSONException {
        String dayOfWeek = dayOfWeekEditText.getText().toString();
        String time = timeEditText.getText().toString();
        String capacityStr = capacityEditText.getText().toString();
        String durationStr = durationEditText.getText().toString();
        String priceStr = priceEditText.getText().toString();
        String classType = classTypeEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String teacher = teacherEditText.getText().toString();
        String image = imageText.getText().toString();

        if (dayOfWeek.isEmpty() || time.isEmpty() || capacityStr.isEmpty() || durationStr.isEmpty() ||
                priceStr.isEmpty() || classType.isEmpty() || description.isEmpty() || teacher.isEmpty()) {
            Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject jsonPayload = new JSONObject();
        try {
            jsonPayload.put("userId", "gw001249268");
            jsonPayload.put("dayOfWeek", "3123123123");
            jsonPayload.put("time", "123123");
            jsonPayload.put("capacity", 12312);
            jsonPayload.put("duration", 3121);
            jsonPayload.put("price", 312);
            jsonPayload.put("classType", "121212");
            jsonPayload.put("description", "1312");
            jsonPayload.put("teacher", "12312");
            jsonPayload.put("image", "1212");
        } catch (JSONException e) {
            e.printStackTrace();
        }

// Print out the JSON for debugging
        Log.e("TAG", "Sending JSON: " + jsonPayload.toString());

// Retrofit setup
        ApiInterface apiInterface = RetrofitClient.getRetrofitInstance().create(ApiInterface.class);

// Call API with the JSON payload
        Call<Upload> call = apiInterface.getUserInformation(jsonPayload);
        call.enqueue(new Callback<Upload>() {
            @Override
            public void onResponse(Call<Upload> call, Response<Upload> response) {
                if (response.isSuccessful()) {
                    Log.e("TAG", "Upload successful");
                } else {
                    Log.e("TAG", "Upload failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Upload> call, Throwable t) {
                Log.e("TAG", "Upload failed: " + t.getMessage());
            }
        });
        int capacity = 0;
        int duration = 0;
        double price = 0.0;

        try {
            capacity = Integer.parseInt(capacityStr);
            duration = Integer.parseInt(durationStr);
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format. Please enter valid values.", Toast.LENGTH_SHORT).show();
            return;
        }

        YogaCourse newCourse = new YogaCourse(0, dayOfWeek, time, capacity, duration, price, classType, description, teacher, image);

        // Using ExecutorService to perform the task in background
        executor.execute(() -> {
            dbHelper.addYogaCourse(newCourse);  // Add the course to the database

            runOnUiThread(() -> {
                // Show success message
                Toast.makeText(AddYogaCourseActivity.this, "Yoga class added successfully!", Toast.LENGTH_SHORT).show();

                // Send result back to MainActivity to reload the data
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();  // Close AddYogaCourseActivity and return to MainActivity
            });
        });
    }

    private void selectPictureFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        launchActivity.launch(intent);
    }

    ActivityResultLauncher<Intent> launchActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Uri selectedImage = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                        imageView.setImageBitmap(bitmap);
                        long timestamp = System.currentTimeMillis();
                        File file = bitmapToFile(bitmap, getApplicationContext().getFilesDir().getAbsolutePath() + File.pathSeparator + String.valueOf(timestamp));
                        inputPictureUri.setText(file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
    );

    private File bitmapToFile(Bitmap bitmap, String filepath) {
        File file = null;
        try {
            file = new File(filepath + ".png");  // Append .png extension
            file.createNewFile();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapData = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void startCamera() {
        // Initialize CameraX (only for devices with CAMERA permission granted)
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                imageCapture = new ImageCapture.Builder().build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        // Implement photo capturing functionality here.
    }
}
