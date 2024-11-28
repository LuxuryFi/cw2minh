package com.example.coursework;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import androidx.annotation.NonNull;

import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import com.google.android.material.navigation.NavigationView;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.ByteArrayOutputStream;
import java.io.File;
import androidx.core.content.ContextCompat;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;

public class AddYogaCourseActivity extends AppCompatActivity {

    private EditText dayOfWeekEditText;
    private EditText timeEditText;
    private EditText capacityEditText;
    private EditText durationEditText;
    private EditText priceEditText;
    private EditText classTypeEditText;
    private EditText descriptionEditText;
    private EditText teacherEditText;

    private EditText imageText;

    private Button saveButton;
    private Button backButton;
    private YogaCourseDBHelper dbHelper;
    ImageCapture imageCapture;
    Executor executor = Executors.newSingleThreadExecutor();


    final int REQUEST_CODE_PERMISSIONS = 1001;
    final String[] REQUIRED_PERMISSIONS = new String[]{
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO"
    };

    ImageView imageView;
    EditText inputPictureUri;
    PreviewView previewView;

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
        }
        startCamera();

        imageView = findViewById(R.id.imageView);
        previewView = findViewById(R.id.previewView);
        inputPictureUri = findViewById(R.id.textImage);
        Button btn =findViewById(R.id.buttonAction);
        btn.setOnClickListener(view -> {
            final String[] items = {"Take photo", "Choose from gallery", "View picture from an URI"};
            AlertDialog.Builder dlg = new AlertDialog.Builder(this);
            dlg.setItems(items, (diaglog, item) -> {
                if(items[item].equals("Take photo")) {
                    Toast.makeText(this, "Take photo", Toast.LENGTH_LONG);
                    takePhoto();
                } else if (items[item].equals("Choose from gallery")) {
                    Toast.makeText(this, "Choose from gallery", Toast.LENGTH_LONG);
                    selectPictureFromGallery();

                } else if (items[item].equals("View picture from an URI")) {
                    Toast.makeText(this, "View picture from an URI", Toast.LENGTH_LONG);
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
        saveButton.setOnClickListener(v -> saveYogaCourse());

        // Initialize Back button
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());  // Close the activity without saving
    }

    private void saveYogaCourse() {
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

        // Call AsyncTask to add the course to the database
        new AddYogaCourseTask().execute(newCourse);
    }

    private class AddYogaCourseTask extends AsyncTask<YogaCourse, Void, Void> {

        @Override
        protected Void doInBackground(YogaCourse... yogaCourses) {
            dbHelper.addYogaCourse(yogaCourses[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Show success message
            Toast.makeText(AddYogaCourseActivity.this, "Yoga class added successfully!", Toast.LENGTH_SHORT).show();

            // Send result back to MainActivity to reload the data
            Intent resultIntent = new Intent();
            setResult(RESULT_OK, resultIntent);
            finish();  // Close AddYogaCourseActivity and return to MainActivity
        }
    }

    private void selectPictureFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
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
            file = new File(filepath);
            file.createNewFile();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0,bos);
            byte[] bitmapdata = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return file;
        }
    }


    private void takePhoto() {
        long timestamp = System.currentTimeMillis();
        File savedFile = new File(getApplicationContext().getFilesDir(), String.valueOf(timestamp));
        ImageCapture.OutputFileOptions option2 = new ImageCapture.OutputFileOptions.Builder(
                savedFile
        ).build();
        imageCapture.takePicture(option2, executor,
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri selectingImage = outputFileResults.getSavedUri();
                        runOnUiThread(() -> {
                            try {
                                imageView.setImageBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), selectingImage));
                                inputPictureUri.setText(selectingImage.getPath());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {

                    }
                });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderListenableFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderListenableFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderListenableFuture.get();
                CameraSelector cameraSelector =  CameraSelector.DEFAULT_BACK_CAMERA;
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();
                try {
                    cameraProvider.unbindAll();
                    cameraProvider.bindToLifecycle(
                            this, cameraSelector, preview, imageCapture);
                } catch (Exception ex) {
                    Log.e("Test", "Usse casese ");
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    boolean allPermissionsGranted()
    {
        for (String permission: REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;

    }
}
