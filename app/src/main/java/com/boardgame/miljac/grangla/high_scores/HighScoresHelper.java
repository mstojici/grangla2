package com.boardgame.miljac.grangla.high_scores;

import android.os.AsyncTask;

import com.boardgame.miljac.grangla.high_scores.Score;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

/* used to retrieve high scores from gameplay activities */
public class HighScoresHelper {

    public static class HttpGetScoresAsyncTask extends AsyncTask<String, String, String> {
        private String level;
        private long score;
        private boolean highScore = false;

        public HttpGetScoresAsyncTask(String level, long score){
            super();
            this.level = level;
            this.score = score;
        }

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

            return "NO_RESULT";
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            try {
                if (result.equals("NO_RESULT")){
                    return;
                }
                ObjectMapper mapper = new ObjectMapper();
                List<Score> scoreList = new ArrayList<>();
                scoreList = mapper.readValue(result, new TypeReference<ArrayList<Score>>(){});
                Score myScore = new Score(level, score, "");
                scoreList.add(myScore);

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

                highScore = scoresOfLevel.contains(myScore);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public boolean isHighScore() {
            return highScore;
        }
    }

    private static String convertInputStreamToString(InputStream inputStream)
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





    public static class HttpPostScoreAsyncTask extends AsyncTask<String, String, String> {
        private Score score;

        public HttpPostScoreAsyncTask(Score score){
            super();
            this.score = score;
        }

        @Override
        protected String doInBackground(String... urls) {
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;

            try {
                ObjectMapper obj = new ObjectMapper();
                String jsonStr = obj.writeValueAsString(score);
                byte[] postDataBytes = jsonStr.getBytes("UTF-8");
                String urlWithParams = "https://grangla.com/scores/scores/";
                URL url = new URL(urlWithParams);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setRequestProperty("Accept", "application/json");
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
                urlConnection.setDoOutput(true);
                urlConnection.getOutputStream().write(postDataBytes);
                int statusCode = urlConnection.getResponseCode();

                if (statusCode == 200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);

                    return response;
                }

                inputStream = new BufferedInputStream(urlConnection.getInputStream());
                String response = convertInputStreamToString(inputStream);

                return response;

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

            return "";//GET(urls[0]);
        }

    }

}
