package com.example.androidthings.doorbell;


import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class SummaryTool extends AppCompatActivity {
	FileInputStream in;
	FileOutputStream out;
	ArrayList<Sentence> sentences, contentSummary;
	ArrayList<Paragraph> paragraphs;
	int noOfSentences, noOfParagraphs;

	double[][] intersectionMatrix;
	LinkedHashMap<Sentence,Double> dictionary;


	public SummaryTool(){
		in = null;
		out = null;
		noOfSentences = 0;
		noOfParagraphs = 0;
	}

	public void init(){
		sentences = new ArrayList<>();
		paragraphs = new ArrayList<>();
		contentSummary = new ArrayList<>();
		dictionary = new LinkedHashMap<>();
		noOfSentences = 0;
		noOfParagraphs = 0;
		try {
	        in = new FileInputStream("Dyslexic.txt");
	        out = new FileOutputStream("output.txt");
    	}catch(FileNotFoundException e){
    		e.printStackTrace();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	}

	/*Gets the sentences from the entire passage*/
	public ArrayList<Sentence> extractSentenceFromContext(String textPara){

        Log.v("Check Points","0");

	    int j=0;

		String checkString = "";

		int i = 0;

        try{

        	while(i<textPara.length())
			{
				j = i;
				while(textPara.charAt(j) != '.')
				{

					checkString += textPara.charAt(j);

					if(textPara.charAt(j) == '\n')
						noOfParagraphs++;

					j++;
					i = j;
				}

				sentences.add(new Sentence(noOfSentences,checkString.trim(),checkString.trim().length(),noOfParagraphs));

				Log.i("SENTENCE: ", sentences.toString());

				noOfSentences++;

				i++;
			}


	    }catch(Exception e){
	    	e.printStackTrace();
	    }

	    return sentences;

	}

	public ArrayList<Paragraph> groupSentencesIntoParagraphs(){
		int paraNum = 0;
		Paragraph paragraph = new Paragraph(0);

		for(int i=0;i<noOfSentences;i++){
			if(sentences.get(i).paragraphNumber == paraNum){
				//continue
			}else{
				paragraphs.add(paragraph);
				paraNum++;
				paragraph = new Paragraph(paraNum);
				
			}
			paragraph.sentences.add(sentences.get(i));
		}

		paragraphs.add(paragraph);

		return paragraphs;
	}

	public double noOfCommonWords(Sentence str1, Sentence str2){
		double commonCount = 0;

		for(String str1Word : str1.value.split("\\s+")){
			for(String str2Word : str2.value.split("\\s+")){
				if(str1Word.compareToIgnoreCase(str2Word) == 0){
					commonCount++;
				}
			}
		}

		return commonCount;
	}

	public void createIntersectionMatrix(){
		intersectionMatrix = new double[noOfSentences][noOfSentences];
		for(int i=0;i<noOfSentences;i++){
			for(int j=0;j<noOfSentences;j++){

				if(i<=j){
					Sentence str1 = sentences.get(i);
					Sentence str2 = sentences.get(j);
					intersectionMatrix[i][j] = noOfCommonWords(str1,str2) / ((double)(str1.noOfWords + str2.noOfWords) /2);
				}else{
					intersectionMatrix[i][j] = intersectionMatrix[j][i];
				}
				
			}
		}
	}

	public void createDictionary(){
		for(int i=0;i<noOfSentences;i++){
			double score = 0;
			for(int j=0;j<noOfSentences;j++){
				score+=intersectionMatrix[i][j];
			}
			dictionary.put(sentences.get(i), score);
			((Sentence)sentences.get(i)).score = score;
		}
	}

	public void createSummary(){

		System.out.println("Creating Summary");

		try{
			for(int j = 0; j < noOfParagraphs; j++){
				int primary_set = (paragraphs.get(j).sentences.size())%5;

				//Sort based on score (importance)
				Collections.sort(paragraphs.get(j).sentences,new SentenceComparator());
				for(int i = 0; i< primary_set; i++){
					contentSummary.add(paragraphs.get(j).sentences.get(i));
					Log.i("SUMMARY..1",contentSummary.toString());
				}

				Log.i("SUMMARY..2",contentSummary.toString());
			}

		}catch (Exception ex)
		{
			Log.i("SUMMARY EXCEPTION .. ",ex.getMessage());
		}

		//To ensure proper ordering
		Collections.sort(contentSummary,new SentenceComparatorForSummary());

	}


	public String printSentences(){

		String context = "";

		for(Sentence sentence : sentences){

			context += sentence.value;

			//System.out.println(sentence.number + " => " + sentence.value + " => " + sentence.stringLength  + " => " + sentence.noOfWords + " => " + sentence.paragraphNumber);
		}

		return context;
	}

	public void printIntersectionMatrix(){

		System.out.println("Intersection Matrix");

		for(int i=0;i<noOfSentences;i++){
			for(int j=0;j<noOfSentences;j++){
				System.out.print(intersectionMatrix[i][j] + "    ");
			}
			System.out.print("\n");
		}
	}

	public void printDicationary(){

		System.out.println("Dictionary");
		  // Get a set of the entries
	      Set set = dictionary.entrySet();
	      // Get an iterator
	      Iterator i = set.iterator();
	      // Display elements
	      while(i.hasNext()) {
	         Map.Entry me = (Map.Entry)i.next();
	         System.out.print(((Sentence)me.getKey()).value + ": ");
	         System.out.println(me.getValue());
	      }
	}

	public String printSummary(){

		System.out.println("Summary");

		String summarizedText = "";

		int len = contentSummary.size();

		Log.i("Lenght of the summary..",""+len);

		int i =0;

		System.out.println("no of paragraphs = "+ noOfParagraphs);
		for(Sentence sentence : contentSummary){
			//System.out.println(sentence.value);

			//Log.v("CONTENT SUMMARY: ", sentence.value);

			try
			{
				if(i == (len-2)%5)
				{
					summarizedText += sentence.value;
					break;
				}

			}catch (Exception ex)
			{

				summarizedText = "Cannot be summarized..";

			}

			i++;

		}

		Log.i("SUMMARIZED TEXT: ", summarizedText);

		return summarizedText;



		//textView.setText(summarizedText);
	}

	double getWordCount(ArrayList<Sentence> sentenceList){
		double wordCount = 0.0;
		for(Sentence sentence:sentenceList){
			wordCount +=(sentence.value.split(" ")).length;
		}
		return wordCount;
	}

	public void printStats(){
		System.out.println("number of words in Context : " + getWordCount(sentences));
		System.out.println("number of words in Summary : " + getWordCount(contentSummary));
		System.out.println("Commpression : " +getWordCount(contentSummary)/ getWordCount(sentences) );
	}

}