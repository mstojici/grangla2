package com.boardgame.miljac.grangla.gameplay;

import static com.boardgame.miljac.grangla.gameplay.Table.normalizeIndex;
import static com.boardgame.miljac.grangla.music.MusicHelpers.oneMoreInTheEnd;
import static com.boardgame.miljac.grangla.music.MusicHelpers.onlyTrumpetEnd;
import static com.boardgame.miljac.grangla.music.MusicHelpers.turnSomeInstrumentOn;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.boardgame.miljac.grangla.R;
import com.boardgame.miljac.grangla.gameUI.EndDialog;
import com.boardgame.miljac.grangla.gameUI.ResultBarAnimation;
import com.boardgame.miljac.grangla.gameUI.TableView;
import com.boardgame.miljac.grangla.gameUI.WaitYourTurnTimerAnimation;
import com.boardgame.miljac.grangla.high_scores.HighScoresHelper;
import com.boardgame.miljac.grangla.high_scores.Score;

import java.util.List;
import java.util.concurrent.TimeUnit;

class UIRefresh implements Runnable {
    private final GamePlayActivity gamePlayActivity;

    private final boolean updateResult;
    private final boolean resetSequenceDirections;
    private final Table table;
    private final List movesO;
    private final List movesX;
    private final int player1Image;
    private final int player2Image;
    private final TableView tableView;

    public UIRefresh(GamePlayActivity wiFiGamePlayActivity, boolean updateResult, boolean resetSequenceDirections) {
        this.gamePlayActivity = wiFiGamePlayActivity;
        this.updateResult = updateResult;
        this.resetSequenceDirections = resetSequenceDirections;

        this.table = gamePlayActivity.getTable();
        this.movesO = gamePlayActivity.getMovesO();
        this.movesX = gamePlayActivity.getMovesX();
        this.player1Image = gamePlayActivity.getPlayer1Image();
        this.player2Image = gamePlayActivity.getPlayer2Image();
        this.tableView = gamePlayActivity.getTableView();
    }

    private void updateTableViewFields() {

        for (int i = 0; i < TableConfig.TABLE_SIZE; i++) {
            for (int j = 0; j < TableConfig.TABLE_SIZE; j++) {
                if (table.getAndMoveRock(i, j) == State.circle) {
                    if ((movesO.size() >= (TableConfig.MAX_PIECES - 1)) &&
                            (new Coordinates(i, j).equals(movesO.get(TableConfig.MAX_PIECES - 2)))) {
                        tableView.changePinColor(i, j, player1Image, 0.32f);
                    } else if ((movesO.size() >= (TableConfig.MAX_PIECES - 2)) &&
                            (new Coordinates(i, j).equals(movesO.get(TableConfig.MAX_PIECES - 3)))) {
                        tableView.changePinColor(i, j, player1Image, 0.6f);
                    } else {
                        tableView.changePinColor(i, j, player1Image, 1f);
                    }
                }
                if (table.getAndMoveRock(i, j) == State.cross) {
                    if ((movesX.size() >= (TableConfig.MAX_PIECES - 1)) &&
                            (new Coordinates(i, j).equals(movesX.get(TableConfig.MAX_PIECES - 2)))) {
                        tableView.changePinColor(i, j, player2Image, 0.32f);
                    } else if ((movesX.size() >= (TableConfig.MAX_PIECES - 2)) &&
                            (new Coordinates(i, j)).equals(movesX.get(TableConfig.MAX_PIECES - 3))) {
                        tableView.changePinColor(i, j, player2Image, 0.6f);
                    } else {
                        tableView.changePinColor(i, j, player2Image, 1f);
                    }

                }
                if (table.getAndMoveRock(i, j) == State.empty) {
                    tableView.changePinColor(i, j, R.drawable.pin41, 1f, gamePlayActivity.sequenceDirections[normalizeIndex(i)][normalizeIndex(j)]);

                    movesO.remove(new Coordinates(i, j));
                    movesX.remove(new Coordinates(i, j));
                }

                if (table.getAndMoveRock(i, j) == State.rock) {
                    tableView.changePinColor(i, j, R.drawable.pin20, 1f);
                }
            }
        }
    }

    private void checkIfScored() {
        if (updateResult) {
            gamePlayActivity.sequenceDirections = new int[6][6];
        }

        if ((gamePlayActivity.lastMoveO != null) && (table != null) && (movesO != null)) {
            double score = 0;
            score = table.getScore(gamePlayActivity.lastMoveO.x, gamePlayActivity.lastMoveO.y, gamePlayActivity.lastEventTime, gamePlayActivity.sequenceDirections);
            if (updateResult)
                gamePlayActivity.result += score * TableConfig.RESULT_FACTOR;
            if (score == 0) {
                if (movesO.size() >= TableConfig.MAX_PIECES) {
                    Coordinates c = (Coordinates) movesO.remove(TableConfig.MAX_PIECES - 1);
                    table.emptyIfPossible(c.x, c.y);
                    tableView.removeImediately(c.x, c.y);
                    if (resetSequenceDirections) {
                        gamePlayActivity.sequenceDirections[c.x][c.y] = -2;
                    }
                    if (updateResult) {
                        gamePlayActivity.result -= 3 * TableConfig.RESULT_FACTOR;
                    }
                }
            } else {
                gamePlayActivity.lastEventTime = System.currentTimeMillis();
                turnSomeInstrumentOn();
            }
            gamePlayActivity.lastMoveO = null;
        }

        if ((gamePlayActivity.lastMoveX != null) && (table != null) && (movesX != null)) {
            double score = 0;
            score = table.getScore(gamePlayActivity.lastMoveX.x, gamePlayActivity.lastMoveX.y, gamePlayActivity.lastEventTime, gamePlayActivity.sequenceDirections);
            if (updateResult) {
                gamePlayActivity.result -= score * TableConfig.RESULT_FACTOR;
            }
            if (score == 0) {
                if (movesX.size() >= TableConfig.MAX_PIECES) {
                    Coordinates c = (Coordinates) movesX.remove(TableConfig.MAX_PIECES - 1);
                    table.emptyIfPossible(c.x, c.y);
                    tableView.removeImediately(c.x, c.y);
                    if (resetSequenceDirections) {
                        gamePlayActivity.sequenceDirections[c.x][c.y] = -2;
                    }
                    if (updateResult) {
                        gamePlayActivity.result += 3 * TableConfig.RESULT_FACTOR;
                    }
                }
            } else {
                gamePlayActivity.lastEventTime = System.currentTimeMillis();
                turnSomeInstrumentOn();
            }
            gamePlayActivity.lastMoveX = null;
        }
    }

    private void checkIfGameEnded() {
        long gameEndTime = System.currentTimeMillis() - gamePlayActivity.gameStartTime;

        if ((updateResult && ((gamePlayActivity.result <= 0) ||
                (gamePlayActivity.result >= 100))) ||
                gamePlayActivity.gameEndSignal) {

            gamePlayActivity.gameDone = true;

            onlyTrumpetEnd();

            updateTableViewFields();
            checkIfScored();
            refreshResultBar();

            gamePlayActivity.musicPlayer.setEndSong();

            gamePlayActivity.endDialog = new EndDialog(gamePlayActivity, R.style.EndDialog);
            EndDialog endDialog = gamePlayActivity.endDialog;
            endDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            endDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            endDialog.setCanceledOnTouchOutside(false);

            endDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                endDialog.getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }

            if (gamePlayActivity.result <= (resetSequenceDirections ? 50 : 0)) {
                if (player2Image == R.drawable.pin39) {
                    endDialog.setContentView(gamePlayActivity.getLayoutInflater().inflate(R.layout.end_dialog_eye, null));
                }
                if (player2Image == R.drawable.pin40) {
                    endDialog.setContentView(gamePlayActivity.getLayoutInflater().inflate(R.layout.end_dialog_button, null));
                }
                if (player2Image == R.drawable.pin42) {
                    endDialog.setContentView(gamePlayActivity.getLayoutInflater().inflate(R.layout.end_dialog_clover, null));
                }
                if (player2Image == R.drawable.pin43) {
                    endDialog.setContentView(gamePlayActivity.getLayoutInflater().inflate(R.layout.end_dialog_star, null));
                }
            } else {
                if (player1Image == R.drawable.pin39) {
                    endDialog.setContentView(gamePlayActivity.getLayoutInflater().inflate(R.layout.end_dialog_eye, null));
                }
                if (player1Image == R.drawable.pin40) {
                    endDialog.setContentView(gamePlayActivity.getLayoutInflater().inflate(R.layout.end_dialog_button, null));
                }
                if (player1Image == R.drawable.pin42) {
                    endDialog.setContentView(gamePlayActivity.getLayoutInflater().inflate(R.layout.end_dialog_clover, null));
                }
                if (player1Image == R.drawable.pin43) {
                    endDialog.setContentView(gamePlayActivity.getLayoutInflater().inflate(R.layout.end_dialog_star, null));
                }
            }

            Handler handlerAnimation1 = new Handler();
            handlerAnimation1.postDelayed(new Runnable() {
                public void run() {
                    oneMoreInTheEnd();
                    endDialog.show();
                }
            }, 1000);

            Handler handlerAnimation2 = new Handler();
            handlerAnimation2.postDelayed(new Runnable() {
                public void run() {
                    endDialog.dismiss();

                    if ((gamePlayActivity.levelString == null) || !gamePlayActivity.getScoresTask.isHighScore()) {
                        gamePlayActivity.finish();
                    }
                }
            }, 7000);

            Handler handlerIsHighScore = new Handler();
            handlerIsHighScore.postDelayed(new Runnable() {
                public void run() {
                    if (gamePlayActivity.levelString == null) return;
                    gamePlayActivity.getScoresTask = new HighScoresHelper.HttpGetScoresAsyncTask(gamePlayActivity.levelString, (long) (Math.signum(gamePlayActivity.result)) * gameEndTime);
                    gamePlayActivity.getScoresTask.execute("");
                }
            }, 0);

            Handler handlerSendHighScore = new Handler();
            handlerSendHighScore.postDelayed(new Runnable() {
                public void run() {
                    if (gamePlayActivity.levelString == null) return;
                    if (!gamePlayActivity.getScoresTask.isHighScore()) {
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(gamePlayActivity);
                    builder.setTitle("High Score");
                    builder.setInverseBackgroundForced(true);
                    builder.setMessage("Please enter your name:");
                    final EditText input = new EditText(gamePlayActivity);
                    input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                    input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(25)});
                    builder.setView(input);
                    builder.setCancelable(false);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = input.getText().toString();
                            new HighScoresHelper.HttpPostScoreAsyncTask(new Score(gamePlayActivity.levelString, (long) (Math.signum(gamePlayActivity.result)) * gameEndTime, name)).execute("");
                            gamePlayActivity.finish();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            gamePlayActivity.finish();
                        }
                    });

                    builder.show();

                }

            }, 3000);
        }
    }

    public void refreshResultBar() {
        long millis = (System.currentTimeMillis() - gamePlayActivity.gameStartTime);
        gamePlayActivity.textClock.setText(String.format("%d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
        if (gamePlayActivity.result < 50) {
            gamePlayActivity.textClock.setTextColor(ColorUtils.blendARGB(Color.WHITE, gamePlayActivity.player2Color, (float) (50 - gamePlayActivity.result) / 50));
        } else {
            gamePlayActivity.textClock.setTextColor(ColorUtils.blendARGB(Color.WHITE, gamePlayActivity.player1Color, (float) (gamePlayActivity.result - 50) / 50));
        }

        if (!gamePlayActivity.firstTimeAnimatedProgress)
            if (gamePlayActivity.gameDone) {
                gamePlayActivity.resultBar.clearAnimation();
                if (gamePlayActivity.result > 50) {
                    gamePlayActivity.animResult = new ResultBarAnimation(gamePlayActivity.resultBar2, gamePlayActivity.resultBar, (float) 100);
                } else {
                    gamePlayActivity.animResult = new ResultBarAnimation(gamePlayActivity.resultBar2, gamePlayActivity.resultBar, (float) 0);
                }
                gamePlayActivity.animResult.setDuration(100);
                gamePlayActivity.resultBar.startAnimation(gamePlayActivity.animResult);
            } else {
                if ((gamePlayActivity.animResult.hasEnded()) && (gamePlayActivity.resultBar2.getProgress() != (int) gamePlayActivity.result)) {
                    gamePlayActivity.animResult = new ResultBarAnimation(gamePlayActivity.resultBar2, gamePlayActivity.resultBar, (float) gamePlayActivity.result);
                    gamePlayActivity.animResult.setDuration(400);
                    gamePlayActivity.resultBar.startAnimation(gamePlayActivity.animResult);
                }
            }

        if (gamePlayActivity.firstTimeAnimatedProgress) {
            gamePlayActivity.animResult = new ResultBarAnimation(gamePlayActivity.resultBar, gamePlayActivity.resultBar2, (float) gamePlayActivity.result);
            gamePlayActivity.animResult.setDuration(100);
            gamePlayActivity.resultBar.startAnimation(gamePlayActivity.animResult);

            gamePlayActivity.firstTimeAnimatedProgress = false;
        }
    }

    private void refreshWaitingTimeValues() {
        if (!(gamePlayActivity.currentTime < gamePlayActivity.waitingMomentCircle)) {
            gamePlayActivity.allowCircle = true;
        }

        if (!(gamePlayActivity.currentTime < gamePlayActivity.waitingMomentCross)) {
            gamePlayActivity.allowCross = true;
        }

        if (gamePlayActivity.startCircleTime) {
            WaitYourTurnTimerAnimation anim = new WaitYourTurnTimerAnimation(gamePlayActivity.circleBar,
                    gamePlayActivity.player1Color & 0xC5FFFFFF,
                    gamePlayActivity.player1ColorDesaturated & 0x80FFFFFF);
            anim.setDuration(gamePlayActivity.waitingMomentCircle - gamePlayActivity.currentTime);
            gamePlayActivity.circleBar.startAnimation(anim);
            gamePlayActivity.startCircleTime = false;
        }

        if (gamePlayActivity.startCrossTime) {
            WaitYourTurnTimerAnimation anim2 = new WaitYourTurnTimerAnimation(gamePlayActivity.crossBar,
                    gamePlayActivity.player2Color & 0xC5FFFFFF,
                    gamePlayActivity.player2ColorDesaturated & 0x80FFFFFF);
            anim2.setDuration(gamePlayActivity.waitingMomentCross - gamePlayActivity.currentTime);
            gamePlayActivity.crossBar.startAnimation(anim2);
            gamePlayActivity.startCrossTime = false;
        }

        if (updateResult) {
            gamePlayActivity.waitingTimeCircle = TableConfig.MAX_WAITING_TIME - (TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME) / 50 * Math.abs(50 - (int) gamePlayActivity.result);
            gamePlayActivity.waitingTimeCircle -= TableConfig.MIN_WAITING_TIME;
            gamePlayActivity.waitingTimeCircle = (long) ((double) gamePlayActivity.waitingTimeCircle / (1 + (double) (gamePlayActivity.currentTime - gamePlayActivity.gameStartTime) / (double) TableConfig.HALF_LIFE));
            gamePlayActivity.waitingTimeCircle += TableConfig.MIN_WAITING_TIME;

            gamePlayActivity.waitingTimeCross = TableConfig.MAX_WAITING_TIME - (TableConfig.MAX_WAITING_TIME - TableConfig.MIN_WAITING_TIME) / 50 * Math.abs(50 - (int) gamePlayActivity.result);
            gamePlayActivity.waitingTimeCross -= TableConfig.MIN_WAITING_TIME;
            gamePlayActivity.waitingTimeCross = (long) ((double) gamePlayActivity.waitingTimeCross / (1 + (double) (gamePlayActivity.currentTime - gamePlayActivity.gameStartTime) / (double) TableConfig.HALF_LIFE));
            gamePlayActivity.waitingTimeCross += TableConfig.MIN_WAITING_TIME;
        }
    }

    //music measure and speed are changed according to waitingTime, that is game speed
    private void refreshMusicPlayerValues() {
        if (!gamePlayActivity.musicPlayer.isEndSong()) {
            gamePlayActivity.musicPlayer.setNoteDuration((long) (TableConfig.NOTE_DURATION_FACTOR * gamePlayActivity.waitingTimeCross));
        }
    }


    public void run() {
        synchronized (gamePlayActivity) {
            gamePlayActivity.currentTime = System.currentTimeMillis();

            checkIfGameEnded();
            updateTableViewFields();
            checkIfScored();
            refreshResultBar();
            refreshWaitingTimeValues();
            refreshMusicPlayerValues();
        }
    }
}
