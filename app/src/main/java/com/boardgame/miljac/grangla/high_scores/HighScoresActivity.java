package com.boardgame.miljac.grangla.high_scores;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.boardgame.miljac.grangla.R;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public class HighScoresActivity extends AppCompatActivity {
    private int currentApiVersion;
    ListView scoreListView;
    ArrayAdapter<Score> scoreListAdapter;

    private ArrayAdapter<Score> scoreArrayAdapter(){
        return new ArrayAdapter<Score>(HighScoresActivity.this, R.layout.list_white_text, R.id.list_content){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view =super.getView(position, convertView, parent);
                TextView textView=(TextView) view.findViewById( R.id.list_content);
                textView.setTextColor(Color.WHITE);
                return view;
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentApiVersion = Build.VERSION.SDK_INT;
        setContentView(R.layout.activity_high_scores);
        scoreListView = (ListView) findViewById(R.id.score_list);
        scoreListAdapter = scoreArrayAdapter();
        scoreListView.setAdapter(scoreListAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        new HttpAsyncTask().execute("https://grangla.com/scores/scores/");
    }

    /* used to fetch the scores */
    private class HttpAsyncTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... urls) {
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;

            try {
                String urlWithParams = "https://grangla.com/scores/scores/";
                URL url = new URL(urlWithParams);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("GET");
                int statusCode = urlConnection.getResponseCode();

                if (statusCode == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);

                    return response;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return "";
        }

        // displays the fetched scores
        @Override
        protected void onPostExecute(String result) {

            try {
                ObjectMapper mapper = new ObjectMapper();
                List<Score> scoreList = new ArrayList<>();
                scoreList = mapper.readValue(result, new TypeReference<ArrayList<Score>>(){});

                scoreListAdapter.clear();
                scoreListAdapter.add(new Score(getResources().getString(R.string.beginner).toUpperCase()));
                scoreListAdapter.addAll(getScoresOfLevel(scoreList, "BEGINNER"));
                scoreListAdapter.add(new Score(""));
                scoreListAdapter.add(new Score(getResources().getString(R.string.intermediate).toUpperCase()));
                scoreListAdapter.addAll(getScoresOfLevel(scoreList, "MID"));
                scoreListAdapter.add(new Score(""));
                scoreListAdapter.add(new Score(getResources().getString(R.string.expert).toUpperCase()));
                scoreListAdapter.addAll(getScoresOfLevel(scoreList, "EXPERT"));
                scoreListAdapter.notifyDataSetChanged();

            } catch (Exception e) {
                scoreListAdapter.clear();
                scoreListAdapter.add(new Score(getResources().getString(R.string.check_internet)));
            }

        }
    }

    private List<Score> getScoresOfLevel(List<Score> scoreList, String level){
        List<Score> scoresOfLevel = scoreList.stream()
                .filter(score -> level.equals(score.getLevel()))
                .sorted((s1, s2) -> {
                    if(Math.signum(s1.getScore()) != Math.signum(s2.getScore())){
                        return Long.compare(s2.getScore(), s1.getScore());
                    } else {
                        return Long.compare(s1.getScore(), s2.getScore());
                    }
                })
                .limit(5)
                .collect(Collectors.toList());

        scoresOfLevel.forEach(s -> {
            s.setTag(String.format("%2d:%02d %16s  %s",
                    Math.abs(s.getScore())/1000l/60l,
                    Math.abs(s.getScore())/1000%60l,
                    s.getName(),
                    s.getScore()>0?
                            getResources().getString(R.string.won) :
                            getResources().getString(R.string.lost)));
        });

        return scoresOfLevel;
    }

    private String convertInputStreamToString(InputStream inputStream)
            throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setUiFlags();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setUiFlags();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUiFlags();
    }

    private void setUiFlags(){
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    public void exit(View view) {
        this.finish();
    }
}