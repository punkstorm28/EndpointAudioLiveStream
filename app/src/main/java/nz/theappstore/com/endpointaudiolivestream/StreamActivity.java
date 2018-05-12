package nz.theappstore.com.endpointaudiolivestream;

import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

public class StreamActivity extends AppCompatActivity {

    Button streamButton;
    private boolean isPlaying;
    private MediaPlayer mediaPlayer;
    private ProgressDialog progressDialog;
    private boolean initialStage = true;
    private static final String STREAMING_ENDPOINT = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream);
        initializeViews();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        progressDialog = new ProgressDialog(this);
    }

    private void initializeViews() {
        streamButton = findViewById(R.id.stream_button);
        streamButton.setOnClickListener((view) -> {
            if (!isPlaying) {
                streamButton.setText("Pause Streaming");

                if (initialStage) {
                    new Player().execute("http://192.168.1.9:8083/listen");
                } else {
                    if (!mediaPlayer.isPlaying())
                        mediaPlayer.start();
                }

                isPlaying = true;

            } else {
                streamButton.setText("Launch Streaming");

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }

                isPlaying = false;
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    class Player extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... strings) {
            Boolean prepared = false;

            try {
                mediaPlayer.setDataSource(strings[0]);
                mediaPlayer.setOnCompletionListener(mediaPlayer -> {
                    initialStage = true;
                    isPlaying = false;
                    streamButton.setText("Launch Streaming");
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                });

                mediaPlayer.prepare();
                prepared = true;

            } catch (Exception e) {
                e.printStackTrace();
                prepared = false;
            }

            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (progressDialog.isShowing()) {
                progressDialog.cancel();
            }

            mediaPlayer.start();
            initialStage = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Buffering...");
            progressDialog.show();
        }
    }
}
