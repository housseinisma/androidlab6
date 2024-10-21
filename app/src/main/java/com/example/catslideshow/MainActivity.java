package com.example.catslideshow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the ImageView and ProgressBar in the layout
        imageView = findViewById(R.id.catImageView);
        progressBar = findViewById(R.id.progressBar);

        // Start the AsyncTask to download and display cat images
        new CatImages().execute();
    }

    // AsyncTask class to handle downloading and displaying cat images
    private class CatImages extends AsyncTask<String, Integer, String> {
        private Bitmap catBitmap;
        private String imageName;

        @Override
        protected String doInBackground(String... strings) {
            while (true) {
                try {
                    // Fetch random cat image JSON from API
                    URL url = new URL("https://cataas.com/cat?json=true");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    StringBuilder jsonBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonBuilder.append(line);
                    }

                    // Parse JSON response
                    JSONObject jsonObject = new JSONObject(jsonBuilder.toString());
                    imageName = jsonObject.getString("id") + ".png";
                    String imageUrl = "https://cataas.com/cat/" + jsonObject.getString("id");

                    // Check if image exists locally
                    File file = new File(getFilesDir(), imageName);
                    if (file.exists()) {
                        catBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    } else {
                        // Download the image
                        URL catImageUrl = new URL(imageUrl);
                        HttpURLConnection imgConnection = (HttpURLConnection) catImageUrl.openConnection();
                        InputStream imgInputStream = new BufferedInputStream(imgConnection.getInputStream());
                        catBitmap = BitmapFactory.decodeStream(imgInputStream);

                        // Save the image locally
                        FileOutputStream outputStream = new FileOutputStream(file);
                        catBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                        outputStream.close();
                    }

                    // Progress update for viewing duration
                    for (int i = 0; i < 100; i++) {
                        publishProgress(i);
                        Thread.sleep(30); // Adjust to set the image display duration
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // Update progress bar and ImageView on the UI thread
            progressBar.setProgress(values[0]);
            if (values[0] == 0 && catBitmap != null) {
                imageView.setImageBitmap(catBitmap);
            }
        }
    }
}
