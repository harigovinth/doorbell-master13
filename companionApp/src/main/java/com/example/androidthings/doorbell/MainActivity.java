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
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Locale;

//import com.firebase.client.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    //private DoorbellEntryAdapter mAdapter;
    //private FirebaseStorage mFirebaseStorage;

    ArrayList<String> arraylistText = new ArrayList<>();

    String text = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button btnTTS = findViewById(R.id.btnTTS);
        Button btnSummary = findViewById(R.id.btnSummary);
        Button btnTTSStop = findViewById(R.id.btnTTSStop);

        TextView textView1 = findViewById(R.id.textHeading1);
        TextView textView2 = findViewById(R.id.textHeading2);

        Typeface tftf = Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/OpenDyslexic-Bold.ttf");

        textView1.setTypeface(tftf);
        textView2.setTypeface(tftf);

        final TextToSpeech tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                if(i == TextToSpeech.SUCCESS)
                {
                    Toast.makeText(MainActivity.this, "TTS Initialization Success "+i,Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "TTS Initialization Failed "+i, Toast.LENGTH_SHORT).show();
                }

            }
        });

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference databaseReference = firebaseDatabase.getReference("logs/OCRIMAGE");

        Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/OpenDyslexic3-Regular.ttf");


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

        btnSummary.setTypeface(tf);
        btnTTS.setTypeface(tf);
        btnTTSStop.setTypeface(tf);

        btnTTS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Toast.makeText(MainActivity.this, "Text: "+text, Toast.LENGTH_SHORT).show();

                tts.stop();

                if(text.equals(""))
                {
                    tts.setLanguage(Locale.ENGLISH);
                    int status = tts.speak("Full Text..",TextToSpeech.QUEUE_FLUSH,null,null);

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

        btnSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tts.stop();

                if(text.equals(""))
                {
                    tts.setLanguage(Locale.ENGLISH);
                    int status = tts.speak("Summarized Text..",TextToSpeech.QUEUE_FLUSH,null,null);

                    if(status == TextToSpeech.ERROR)
                    {
                        Toast.makeText(MainActivity.this, "Error in talking the content .. 3", Toast.LENGTH_SHORT).show();
                    }

                }
                else
                {
                    tts.setLanguage(Locale.ENGLISH);
                    int status = tts.speak(arraylistText.toString(), TextToSpeech.QUEUE_FLUSH, null, null);

                    if(status == TextToSpeech.ERROR)
                    {
                        Toast.makeText(MainActivity.this, "Error in talking the content .. 4", Toast.LENGTH_SHORT).show();
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

        //final TextView textView1 = findViewById(R.id.text_Summaarized);

        final Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/OpenDyslexic-Italic.ttf");

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

                                   keywordAnalysing(text);


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



    }

/*
    public void keywordAnalysing(String str) {



        TextView textView = findViewById(R.id.text_Summaarized);

        String textString = "";
        //int i = 0; //starting index..
        int flag = 0;
        int flag1;
        int flagCheck = 0;
        int count = 0;

        // parent loop
        for (int i = 0; i < str.length(); i++) // the loop for overall counting of characters right from the top to the end of the string..
        {

            //System.out.println(str.charAt(i));
            //When there is no space or '.' or ',' the word is included as a consideration for keyword possibility..
            if ((!Character.isWhitespace(str.charAt(i))) && (str.charAt(i) != '.') && (str.charAt(i) != ',')) {
                count++;
                textString += str.charAt(i);
                flag1 = 0;
                //i++;
            } else { // once a character from the above is encountered, consider the word.
                flag1 = 1;
            }

            if ((flag1 == 1) && (Character.isWhitespace(str.charAt(i)))) {
                flag = 0;
            }

            if ((flag1 == 0) && (str.charAt(i) == '.')) {
                flag = 1;
            }

            if (flag == 0 && flag1 == 1) {
                if (count > 2) { //A minimum of three words has to be be there for a keyword.

                    flagCheck = 0;

                    for(int j=0;j<arraylistText.size();j++)
                    {
                        String text = arraylistText.get(j);
                        if(text.equalsIgnoreCase(textString) || text.contains(textString))
                        {
                            Log.i("FLAG CHECK: ",".."+j);

                            flagCheck = 1;
                            break;
                        }
                    }

                    if (count == 3 && flagCheck == 0) {


                        if (checkFullWordCaps(textString)) {
                            arraylistText.add(textString);
                            textString = "";
                            count = 0;
                        } else {
                            textString = "";
                            count = 0;
                        }

                    } else if (count > 3) {
                        //Perform the keyword analysis here...

                        if (checkFirstLetCaps(textString)) {
                            arraylistText.add(textString);
                            textString = "";
                            count = 0;
                        } else if (checkFullWordCaps(textString)) {
                            arraylistText.add(textString);
                            textString = "";
                            count = 0;
                        } else if (checkWordInQuotes(textString)) {
                            arraylistText.add(textString);
                            textString = "";
                            count = 0;
                        } else if (checkHypensInWord(textString)) {
                            arraylistText.add(textString);
                            textString = "";
                            count = 0;
                        } else if (count > 7) {
                            arraylistText.add(textString);
                            textString = "";
                            count = 0;
                        } else {
                            textString = "";
                            count = 0;
                        }

                    }

                } else {
                    textString = "";
                    count = 0;
                }
            }
        }

        Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/OpenDyslexic-Italic.ttf");
        textView.setTypeface(tf);
        textView.setText(arraylistText.toString());

        //System.out.println("The keywords in the given line are: " + arraylistText);

    }

    public boolean checkFirstLetCaps(String capCheckString) {

        return Character.isUpperCase(capCheckString.charAt(0));
    }

    public boolean checkFullWordCaps(String capCheckFullString) {

        //int i = 1;
        int flag = 0;
        char[] charArray = capCheckFullString.toCharArray();

        for (int i = 0; i < charArray.length; i++) {
            if (!Character.isUpperCase(charArray[i])) {
                return false;
            }
        }

        return true;
    }

    public boolean checkWordInQuotes(String quoteCheckString) {

        return (quoteCheckString.startsWith("\"") && quoteCheckString.endsWith("\""));
    }

    public boolean checkHypensInWord(String hyphenCheckString) {

        return hyphenCheckString.contains("-");
    }*/

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
