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
import java.util.LinkedList;
import java.util.Locale;

//import com.firebase.client.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    //private DoorbellEntryAdapter mAdapter;
    //private FirebaseStorage mFirebaseStorage;



    ArrayList<String> arraylistText = new ArrayList<>();

    String summarizedText = "";

    String text = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button btnTTS = findViewById(R.id.btnTTS);
        Button btnSummary = findViewById(R.id.btnSummary);
        Button btnTTSStop = findViewById(R.id.btnTTSStop);
        //Button btnKeywords = findViewById(R.id.btnKeywords);

        TextView textView1 = findViewById(R.id.textHeading1);
        TextView textView2 = findViewById(R.id.textHeading2);
//        TextView textView3 = findViewById(R.id.textHeading3);

        Typeface tftf = Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/OpenDyslexic-Bold.ttf");

        textView1.setTypeface(tftf);
        textView2.setTypeface(tftf);
       // textView3.setTypeface(tftf);

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
        //btnKeywords.setTypeface(tf);

        /*btnKeywords.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                tts.stop();

                if(text.equals(""))
                {
                    tts.setLanguage(Locale.ENGLISH);

                    int status = tts.speak("KEywords..",TextToSpeech.QUEUE_FLUSH,null,null);

                    if(status == TextToSpeech.ERROR)
                    {
                        Toast.makeText(MainActivity.this, "Error in talking the content .. Keywords", Toast.LENGTH_SHORT).show();
                    }

                }
                else
                {
                    tts.setLanguage(Locale.ENGLISH);
                    Toast.makeText(MainActivity.this,"KEYWORDS.",Toast.LENGTH_SHORT).show();
                    int status = tts.speak(arraylistText.toString(), TextToSpeech.QUEUE_FLUSH, null, null);

                    if(status == TextToSpeech.ERROR)
                    {
                        Toast.makeText(MainActivity.this, "Error in talking the content .. Keywords AL", Toast.LENGTH_SHORT).show();
                    }

                }


            }
        });
*/
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
                        Toast.makeText(MainActivity.this, "Error in talking the content .. Full Text", Toast.LENGTH_SHORT).show();
                    }

                }
                else
                {
                    tts.setLanguage(Locale.ENGLISH);
                    Toast.makeText(MainActivity.this,"FULL TEXT.",Toast.LENGTH_SHORT).show();
                    int status = tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);

                    if(status == TextToSpeech.ERROR)
                    {
                        Toast.makeText(MainActivity.this, "Error in talking the content .. Full Text Text", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MainActivity.this, "Error in talking the content .. Summarized Text", Toast.LENGTH_SHORT).show();
                    }

                }
                else
                {
                    tts.setLanguage(Locale.ENGLISH);
                    Toast.makeText(MainActivity.this,"SUMMARY.",Toast.LENGTH_SHORT).show();
                    int status = tts.speak(summarizedText, TextToSpeech.QUEUE_FLUSH, null, null);

                    if(status == TextToSpeech.ERROR)
                    {
                        Toast.makeText(MainActivity.this, "Error in talking the content .. Summarized Text Text", Toast.LENGTH_SHORT).show();
                    }

                }




            }
        });

        btnTTSStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(MainActivity.this,"STOP.",Toast.LENGTH_SHORT).show();
                tts.stop();

            }
        });


    }

    public void ocrTextRecognization(Bitmap bitmap)
    {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getOnDeviceTextRecognizer();

        FirebaseVisionDocumentTextRecognizer det = FirebaseVision.getInstance().getCloudDocumentTextRecognizer();

        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setWidth(480)   // 480x360 is typically sufficient for
                .setHeight(360)  // image recognition
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .build();

    /*    FirebaseVisionDocumentTextRecognizer detectorDoc = FirebaseVision.getInstance()
                .getCloudDocumentTextRecognizer();
*/
        final TextView textView = findViewById(R.id.textOCR);

        final Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/OpenDyslexic-Italic.ttf");

        Log.d("TEXT OCR", "TEXT CHECK - 1");

        /*Task<FirebaseVisionDocumentText> res = det.processImage(image).addOnSuccessListener(new OnSuccessListener<FirebaseVisionDocumentText>() {
            @Override
            public void onSuccess(FirebaseVisionDocumentText firebaseVisionDocumentText) {

                for (FirebaseVisionDocumentText.Block block : firebaseVisionDocumentText.getBlocks()) {

                    text = block.getText();
                    textView.setTypeface(tf);
                    textView.setText(text);
                    //keywordAnalysing(text);


                    extractiveSummarization(text);

                    Log.i("TEXT API..", "summary 1");

                    //textSummarization.execute();


                }
            }
        });
*/
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
                                    //keywordAnalysing(text);


                                    extractiveSummarization(text);


                                    for(FirebaseVisionText.Line line : block.getLines()) {


                                        Log.i("TEXT API..", "summary 1");

                                        //textSummarization.execute();

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


    }

    public void extractiveSummarization(String textString)
    {

        int len = textString.length();

        System.out.println("Full Text:\n"+textString+"\n Length: "+len);

        TextView textView = findViewById(R.id.text_Summaarized);
        Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/OpenDyslexic-Italic.ttf");

        ArrayList<String> alSentences;
       // String summarizedContext = "";


        System.out.println("Full Text:\n"+textString+"\nTotal Length: "+textString.length());

        alSentences = sentenceConvertion(textString);

        summarizedText = textRanking(alSentences);

        //System.out.println("Summarized Text:\n"+summarizedText+"\nCompressed Length: "+summarizedText.length());

        textView.setTypeface(tf);
        textView.setText(summarizedText);

    }

    public ArrayList<String> sentenceConvertion(String text) {

        //get each sentence..
        //remove unnecessary words..
        //retain the same sentence..
        //unnecessary words are feeded by the user.
        //The Possible unnecessary words could be as follows: 'a','is','has','was','an','for','if','had','have','were','it','the','though',
        //'as','its','it's'.
        String sentence = "";
        int len = text.length();

        LinkedList<String> llWords = new LinkedList<>();
        ArrayList<String> alSentences = new ArrayList<>();

        int i = 0;
        while (i < len) {

       /*     if (text.charAt(i) == '@') {
                checkAT = true;
            }

       */     if ((text.charAt(i) != '.')) {
                sentence += text.charAt(i);
                // i++;
            } else {
                alSentences.add(sentence);
                sentence = "";
            }

            i++;
        }

        return alSentences;

    }

    public String textRanking(ArrayList<String> sentences) {

        int len = sentences.size();

        String sentence = "";

        if (len >= 10) {
            int _0_per = 0;
            int _25_per = len / 4;
            int _50_per = len / 2;
            int _75_per = (3 * len) / 4;
            int _100_per = len - 1;



            sentence += sentences.get(_0_per) + ".\n" + sentences.get(_25_per) + ".\n" + sentences.get(_50_per) + ".\n" + sentences.get(_75_per) + ".\n" + sentences.get(_100_per) + ".";

            System.out.println("Summarized Text:\n" + sentence + "\nLength: " + sentence.length());

        } else if (len < 10 && len > 4) {
            int _0_per = 0;
            int _75_per = (3 * len) / 4;

            sentence += sentences.get(_0_per) + ".\n" + sentences.get(_75_per) + ".";

            System.out.println("Summarized Text:\n" + sentence + "\nLength: " + sentence.length());

        }

        return sentence;

    }

    public void keywordAnalysing(String str) {



        //TextView textView = findViewById(R.id.text_Keywords);

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
        //textView.setTypeface(tf);
        //textView.setText(arraylistText.toString());

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
