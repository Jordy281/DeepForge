package com.amazonaws.mobile.samples.mynotes;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.samples.mynotes.utils.FileUtils;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunctionException;
import com.amazonaws.mobileconnectors.lambdainvoker.LambdaInvokerFactory;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;


import com.amazonaws.http.HttpMethodName;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.apigateway.ApiResponse;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.File;

import com.amazonaws.mobile.samples.mynotes.utils.FileUtils;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.StringUtils;

import com.amazonaws.mobileconnectors.lambdainvoker.LambdaFunction;

import javax.net.ssl.HttpsURLConnection;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

import java.util.HashMap;
import java.util.Map;

public class UploadActivity extends AppCompatActivity {

    private static final String TAG = "UploadActivity";
    private static final int REQUEST_CODE = 6384; // onActivityResult request
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        TextView myAwesomeTextView = (TextView)findViewById(R.id.resultsText);
        ImageView imageView = (ImageView) findViewById(R.id.loading);
        DrawableImageViewTarget imageViewTarget = new DrawableImageViewTarget(imageView);
        Glide.with(getApplicationContext()).load(R.raw.tenor).into(imageViewTarget);


        AWSMobileClient.getInstance().initialize(this).execute();
        showChooser();
    }

    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        /**
        dv = new DrawingView(this);
        setContentView(dv);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);



        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, "CHOOSE YOUR FILE");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
         **/

        Context context = getApplicationContext();

        Intent intent = new Intent(context, com.amazonaws.mobile.samples.mynotes.DoodleActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                /**
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(this, uri);
                            Toast.makeText(UploadActivity.this,
                                    "File Selected: " + path, Toast.LENGTH_LONG).show();


                            uploadWithTransferUtility(path);
                        } catch (Exception e) {
                            Log.e("FileSelectorTstActivity", "File select error", e);
                        }
                    }
                }
                break;
                 **/
                if (resultCode == RESULT_OK) {
                    try {
                        // Get the file path from the URI

                        final String path = Environment.DIRECTORY_PICTURES +"deepforge.png";




                        Toast.makeText(UploadActivity.this,
                                "File Selected: " + path, Toast.LENGTH_LONG).show();
                        uploadWithTransferUtility(path);
                    } catch (Exception e) {
                        Log.e("FileSelectorTstActivity", "File select error", e);
                    }
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadWithTransferUtility(String path2) {
        path = path2;
        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                        .build();
        /*
        String pattern = "^(.+)\\\\/([^\\\\/]+)$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(path);
        Log.e("Pattern", "Pattern: "+pattern);
        Log.e("Pattern", "Path: "+path);
        Log.e("Pattern", "Matches: "+m);


        TransferObserver uploadObserver =
                transferUtility.upload(
                        "Mobile-app/"+m.group(2),
                        new File(path));
         */
        int index = path.lastIndexOf("/");
        final String fileName = path.substring(index + 1);


        TransferObserver uploadObserver =
                transferUtility.upload(
                        "Mobile-app/"+fileName,
                        new File(path));


        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                    RequestQueue MyRequestQueue = Volley.newRequestQueue(getApplicationContext());
                    String url = "https://ppms3t54il.execute-api.us-east-1.amazonaws.com/default/signatureVerification";
                    StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //This code is executed if the server responds, whether or not the response contains data.
                            //The String 'response' contains the server's response.
                            Log.d("Responses",response);
                            Log.d("Responses","WOOOOOOOOO");

                            TextView myAwesomeTextView = (TextView)findViewById(R.id.resultsText);
                            ImageView loading = (ImageView) findViewById(R.id.loading);
                            DrawableImageViewTarget imageViewTarget = new DrawableImageViewTarget(loading);
                            loading.setVisibility(View.GONE);


                            ImageView a1 = (ImageView) findViewById(R.id.a1);
                            a1.setImageResource(R.drawable.a1);
                            a1.setVisibility(View.VISIBLE);

                            File imgFile = new  File(path);

                            if(imgFile.exists()){

                                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                                ImageView a2 = (ImageView) findViewById(R.id.a2);

                                a2.setImageBitmap(myBitmap);
                                a2.setVisibility(View.VISIBLE);


                            }

                            if (response.equals("\"MATCH\"")){

                                //Glide.with(getApplicationContext()).load(R.raw.woooo).into(imageViewTarget);
                                myAwesomeTextView.setText("You are a Fraudster");
                            }else{

                                //Glide.with(getApplicationContext()).load(R.raw.nonono).into(imageViewTarget);
                                myAwesomeTextView.setText("You are NOT a Fraudster");
                            }

                        }
                    }, new Response.ErrorListener() { //Create an error listener to handle errors appropriately.
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //This code is executed if there is an error.
                        }
                    })
                    {
                        protected Map<String, String> getParams() {
                            Map<String, String> MyData = new HashMap<String, String>();
                            MyData.put("key1", fileName);
                            return MyData;
                        }
                    };
                    MyRequestQueue.add(MyStringRequest);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d("YourActivity", "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.

        }

        Log.d("YourActivity", "Bytes Transferrred: " + uploadObserver.getBytesTransferred());
        Log.d("YourActivity", "Bytes Total: " + uploadObserver.getBytesTotal());



    }
}
