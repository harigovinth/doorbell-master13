/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androidthings.doorbell;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Locale;

//import com.firebase.client.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    //private DoorbellEntryAdapter mAdapter;
    //private FirebaseStorage mFirebaseStorage;

    String text = "";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button btnTTS = findViewById(R.id.btnTTS);
        Button btnTTSStop = findViewById(R.id.btnTTSStop);

        final TextToSpeech tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                if(i == TextToSpeech.SUCCESS)
                {
                    Toast.makeText(MainActivity.this, "TTS Initialization Success "+i,Toast.LENGTH_SHORT);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "TTS Initialization Failed "+i, Toast.LENGTH_SHORT).show();
                }

            }
        });

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("logs/OCRIMAGE");


        final ImageView imageView = findViewById(R.id.imageViewOCRIMAGE);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if (snapshot.getValue() != null) {

                    String imageURL = snapshot.child("image").getValue().toString();

                    imageURL = imageURL.replaceAll("\\]","");
                    imageURL = imageURL.replaceAll("\\[","");


                    Picasso.with(MainActivity.this)
                            .load(imageURL)
                            .into(new Target() {
                                @Override
                                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                                    /* Save the bitmap or do something with it here */

                                    // Set it in the ImageView
                                    imageView.setImageBitmap(bitmap);

                                    ocrTextRecognization(bitmap);

                                }

                                @Override
                                public void onBitmapFailed(Drawable errorDrawable) {

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Log.v("Error at onCancelled", databaseError.toString());

            }
        });

        btnTTS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(MainActivity.this, "Text: "+text, Toast.LENGTH_SHORT).show();

                if(text.equals(""))
                {
                    tts.setLanguage(Locale.ENGLISH);
                    int status = tts.speak("Hello WOrld",TextToSpeech.QUEUE_FLUSH,null,null);

                    if(status == TextToSpeech.ERROR)
                    {
                        Toast.makeText(MainActivity.this, "Error in talking the content .. 1", Toast.LENGTH_SHORT).show();
                    }

                }
                else
                {
                    tts.setLanguage(Locale.ENGLISH);
                    int status = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);

                    if(status == TextToSpeech.ERROR)
                    {
                        Toast.makeText(MainActivity.this, "Error in talking the content .. 2", Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });

        btnTTSStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tts.stop();

            }
        });


    }

    public void ocrTextRecognization(Bitmap bitmap)
    {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setWidth(480)   // 480x360 is typically sufficient for
                .setHeight(360)  // image recognition
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .build();

        FirebaseVisionDocumentTextRecognizer detectorDoc = FirebaseVision.getInstance()
                .getCloudDocumentTextRecognizer();

        final TextView textView = findViewById(R.id.textOCR);

        final Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/OpenDyslexic3-Regular.ttf");

        Log.d("TEXT OCR", "TEXT CHECK - 1");

        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                // Task completed successfully
                                // ...

                                for (FirebaseVisionText.TextBlock block: firebaseVisionText.getTextBlocks()) {

                                    text = block.getText();

                                    textView.setTypeface(tf);

                                    textView.setText(text);



                                    for (FirebaseVisionText.Line line: block.getLines()) {

                                        //text = line.getText();
                                      //  textView.setText(text);

                                        for (FirebaseVisionText.Element element: line.getElements()) {

                                            //text = element.getText();
                                            //textView.setText(text);

                                        }
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });


        detectorDoc.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() {
                    @Override
                    public void onSuccess(FirebaseVisionDocumentText result) {
                        // Task completed successfully
                        // ...

                        //String text;

                        for(FirebaseVisionDocumentText.Block block : result.getBlocks())
                        {


                            //text = block.getText();
                            //textView.setText(text);

                            /*for (FirebaseVisionDocumentText.Paragraph paragraph : block.getParagraphs())
                            {
                                text = paragraph.getText();
                                //textView.setText(text);

                              *//*for (FirebaseVisionDocumentText.Word word : paragraph.getWords())
                                {
                                    //text = word.getText();
                                    //textView.setText(text);

                                   *//**//*for (FirebaseVisionDocumentText.Symbol symbol : word.getSymbols())
                                        text = symbol.getText();
                                    textView.setText(text);*//**//*

                                }*//*
                            }*/
                        }



                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Task failed with an exception
                        // ...
                    }
                });

    }

    @Override
    public void onStart() {
        super.onStart();

       /* // Initialize Firebase listeners in adapter
        mAdapter.startListening();
        // Make sure new events are visible
        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount());
            }
        });*/
    }

    @Override
    public void onStop() {
        super.onStop();

        // Tear down Firebase listeners in adapter
        //mAdapter.stopListening();
    }
}
