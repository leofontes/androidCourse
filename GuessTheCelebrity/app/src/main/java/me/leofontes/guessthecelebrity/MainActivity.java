package me.leofontes.guessthecelebrity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> celebURLs = new ArrayList<String>();
    ArrayList<String> celebNames = new ArrayList<String>();
    int chosenCeleb = 0;
    int locationOfCorrectAnswer;
    String[] answers = new String[4];
    ImageView imageView;

    Button button0;
    Button button1;
    Button button2;
    Button button3;

    TextView correctTxt;
    TextView wrongTxt;
    int correctCount = 0;
    int wrongCount = 0;

    public void celebChosen(View view) {
        if(view.getTag().toString().equals(Integer.toString(locationOfCorrectAnswer))) {
            Snackbar.make(view, "Correct!", Snackbar.LENGTH_LONG).show();
            correctCount++;
            correctTxt.setText(correctCount + " Correct");
        } else {
            Snackbar.make(view, "Wrong! It was " + celebNames.get(chosenCeleb), Snackbar.LENGTH_LONG).show();
            wrongCount++;
            wrongTxt.setText(wrongCount + " Wrong");
        }

        createNewQuestion();
    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream inputStream = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);

                return myBitmap;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while(data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = (ImageView) findViewById(R.id.imageView);
        button0 = (Button) findViewById(R.id.buttonOne);
        button1 = (Button) findViewById(R.id.buttonTwo);
        button2 = (Button) findViewById(R.id.buttonThree);
        button3 = (Button) findViewById(R.id.buttonFour);

        correctTxt = (TextView) findViewById(R.id.correctTextView);
        wrongTxt = (TextView) findViewById(R.id.wrongTextView);

        DownloadTask task = new DownloadTask();
        String result = null;

        try {
            result = task.execute("http://www.kbono.com.ve/apps/celebrities.html").get();

            String[] splitResult = result.split("<div class=\"sidebarContainer\">");

            Pattern p = Pattern.compile("img src=\"(.*?)\"");
            Matcher m = p.matcher(splitResult[0]);

            while(m.find()) {
                celebURLs.add(m.group(1));
            }

            p = Pattern.compile("alt=\"(.*?)\"");
            m = p.matcher(splitResult[0]);

            while(m.find()) {
                celebNames.add(m.group(1));
            }

            createNewQuestion();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void createNewQuestion() {
        Random rand = new Random();
        chosenCeleb = rand.nextInt(celebURLs.size());

        ImageDownloader imageTask = new ImageDownloader();
        Bitmap celebImage;

        try {
            celebImage = imageTask.execute(celebURLs.get(chosenCeleb)).get();
            imageView.setImageBitmap(celebImage);

            locationOfCorrectAnswer = rand.nextInt(4);
            int incorrectAnswerLocation;

            for(int i = 0; i < 4; i++) {
                if(i == locationOfCorrectAnswer) {
                    answers[i] = celebNames.get(chosenCeleb);
                } else {
                    incorrectAnswerLocation = rand.nextInt(celebURLs.size());

                    while(incorrectAnswerLocation == chosenCeleb) {
                        incorrectAnswerLocation = rand.nextInt(celebURLs.size());
                    }

                    answers[i] = celebNames.get(incorrectAnswerLocation);
                }
            }

            button0.setText(answers[0]);
            button1.setText(answers[1]);
            button2.setText(answers[2]);
            button3.setText(answers[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}