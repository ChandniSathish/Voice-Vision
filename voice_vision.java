
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Start extends AppCompatActivity {
    Timer timer;
    private final int REQ_CODE_SPEECH_OUTPUT = 143;
    private static final String LOG_TAG = Start.class.getSimpleName();
    TextToSpeech t;
    TextView showVoiceText;
    protected void onStart()
    {
        super.onStart();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        showVoiceText  = (TextView)findViewById(R.id.textView2);
        t=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() 
{
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    t.setLanguage(Locale.UK);
                    String toSpeak = "Welcome to Voice Vision ...Press Volume Down Button to Give your Instructions";
                    Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                    t.setPitch((float) 1.0);
                    t.setSpeechRate((float) 1.0);
                    t.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Toast.makeText(this, "Volume Down Pressed", Toast.LENGTH_SHORT).show();
            String toSpeak = "Hi , Speak Now ";
            Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
            t.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            ///Delay function

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do something after 100ms
                    btnToOpenMic();
                }
            }, 1500);

            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void btnToOpenMic()
    {
        Inten intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Hiii Speak Now...");

        try{
            startActivityForResult(intent,REQ_CODE_SPEECH_OUTPUT);

        }catch (ActivityNotFoundException tim){

            //just put a toast if google mic is not opened

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode)
        {
            case REQ_CODE_SPEECH_OUTPUT :{
                if(resultCode == RESULT_OK && data != null){

                    ArrayList<String> voiceInText  = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    showVoiceText.setText(voiceInText.get(0));
                    ///Opening Camera
                    String text = voiceInText.get(0);

                    if(text.contains("open camera") || text.contains("Image capture") )
                    {
                        Log.d(LOG_TAG, "Button clicked!");
                        Intent intent = new Intent(this,MainActivity.class);
                        startActivity(intent);

                    }
                    ///End of Opening Camera

                }
                break;
            } } } }




//IMAGE CAPTURE USING EDGE DETECTION AND OCR:

package com.example.dynamsoft.scandocument;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.dynamsoft.camerasdk.view.DcsImageGalleryView;
import com.dynamsoft.camerasdk.view.DcsImageGalleryViewListener;
import com.googlecode.tesseract.android.TessBaseAPI;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.dynamsoft.camerasdk.exception.DcsCameraNotAuthorizedException;
import com.dynamsoft.camerasdk.exception.DcsException;
import com.dynamsoft.camerasdk.exception.DcsValueNotValidException;
import com.dynamsoft.camerasdk.exception.DcsValueOutOfRangeException;
import com.dynamsoft.camerasdk.io.DcsPNGEncodeParameter;
import com.dynamsoft.camerasdk.model.DcsDocument;
import com.dynamsoft.camerasdk.model.DcsImage;
import com.dynamsoft.camerasdk.view.DcsVideoView;
import com.dynamsoft.camerasdk.view.DcsVideoViewListener;
import com.dynamsoft.camerasdk.view.DcsView;
import com.dynamsoft.camerasdk.view.DcsViewListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
public class MainActivity extends AppCompatActivity implements DcsViewListener {
    private DcsView dcsView;
    private TextView tvTitle;
    private TextView tvShow;
    boolean flag = FALSE;
    public Uri photoURI;
    public String mCurrentPhotoPath;
    private static final int CAMERA_OK = 10;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private TessBaseAPI tessBaseAPI;
    Bitmap bitmap;
    private static final String LOG_TAG=MainActivity.class.getSimpleName();

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    TextToSpeech t1,t2;
    String result;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		try {
           DcsView.setLicense(getApplicationContext(),"your license number");
        } catch (DcsValueNotValidException e) {
            e.printStackTrace();
        }       
        tvTitle = findViewById(R.id.tv_title_id);
        tvShow = findViewById(R.id.tv_show_id);
        dcsView = findViewById(R.id.dcsview_id);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
            }
        }, 3000);
        dcsView.setCurrentView(DcsView.DVE_VIDEOVIEW);
        dcsView.setListener(this);
        try {
      dcsView.getVideoView().setMode(DcsView.DME_DOCUMENT);
        } catch (DcsValueOutOfRangeException e) {
            e.printStackTrace();
        }
dcsView.getVideoView().setNextViewAfterCancel(DcsView.DVE_IMAGEGALLERYVIEW);
        dcsView.getVideoView().setNextViewAfterCapture(dcsView.DVE_IMAGEGALLERYVIEW);

        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS) {
                    t1.setLanguage(Locale.UK);
                    String toSpeak = "Camera opened successfully ... ";
                    Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                    t1.setPitch((float) 1.0);
                    t1.setSpeechRate((float) 1.0);
                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        tvShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dcsView.setCurrentView(DcsView.DVE_VIDEOVIEW);
            }
        });
        dcsView.getVideoView().setListener(new DcsVideoViewListener() {
            @Override
            public boolean onPreCapture(DcsVideoView dcsVideoView) {
                return true;
            }

            @Override
            public void onCaptureFailure(DcsVideoView dcsVideoView, DcsException e) {

            }

            @Override
            public void onPostCapture(DcsVideoView dcsVideoView, DcsImage dcsImage)
            {
                bitmap = dcsImage.getImage();
                int height = dcsImage.getHeight();
                int width = dcsImage.getWidth();
                String hw = " " + height + " " + width + " ";
                saveImage(bitmap);


                Toast.makeText(getApplicationContext(), hw, Toast.LENGTH_SHORT).show();
                result = getText(bitmap);
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelTapped(DcsVideoView dcsVideoView) {
                flag = FALSE;

            }

            @Override
            public void onCaptureTapped(DcsVideoView dcsVideoView) {

                //dcsVideoView.captureImage();
            }

            @Override
            public void onDocumentDetected(final DcsVideoView dcsVideoView, DcsDocument dcsDocument)
            {

                if (!flag)
                {
                    int boundary[] = dcsDocument.getDocumentBoundary();
                String bounds = "  ";
                for (int i = 0; i < boundary.length; i++) {
                    int n = boundary[i];
                    bounds = bounds + " " + n;
                }
                Toast.makeText(getApplicationContext(), bounds, Toast.LENGTH_SHORT).show();
                int height1 = boundary[4] - boundary[0];
                int height2 = boundary[6] - boundary[2];
                int width1 = boundary[0] - boundary[2];
                int width2 = boundary[4] - boundary[6];
                String hw = "Height = " + height1 + " " + "Width = " + width2;
                Toast.makeText(getApplicationContext(), hw, Toast.LENGTH_SHORT).show();
          if((height1 >= 100 && height1 <= 200)||(width2 >= 100 && width2 <= 200))
           {
               t2=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                   @Override
                   public void onInit(int status) {
                       if (status == TextToSpeech.SUCCESS) {
                           t2.setLanguage(Locale.UK);
                           String toSpeak = "Edges not covered...Lift the phone";
                           //Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
                           t2.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                       }
                   }
               });

           }
           else if ((height1 >= 700 && height1 <= 800)||(width1 >= 700 && width1 <= 800))
           {

            t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        t1.setLanguage(Locale.UK);
                        String toSpeak = "Document Detected successfully...Keep the phone Steady ";
                        flag = TRUE;
                        //Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_SHORT).show();
                        t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            });
             }
            }
            else
                {
                    //dcsVideoView.stopPreview();

                }
            }
        });
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

            Intent intent = new Intent(this , Main3Activity.class);

            Bundle b = new Bundle();

            //Inserts a String value into the mapping of this Bundle
            b.putString("result", result);
            intent.putExtras(b);
            startActivity(intent);
            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Intent intent = new Intent(this , Main2Activity.class);
            Bundle b = new Bundle();
            //Inserts a String value into the mapping of this Bundle
            b.putString("result", result);
            intent.putExtras(b);
            startActivity(intent);
            return true;
        }
        else {
            return super.onKeyUp(keyCode, event);
        }
    }
    private void saveImage(Bitmap finalBitmap) {
        File myDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        myDir.mkdirs();

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fname = "VoiceVision"+ timeStamp +".jpg";

        File file = new File(myDir, fname);
        if (file.exists()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(dcsView.getCurrentView() == DcsView.DVE_VIDEOVIEW){
            try {
                dcsView.getVideoView().preview();
            } catch (DcsCameraNotAuthorizedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        requestPermissions();
        if(dcsView.getCurrentView() == DcsView.DVE_VIDEOVIEW){
            try {
                dcsView.getVideoView().preview();
            } catch (DcsCameraNotAuthorizedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        dcsView.getVideoView().stopPreview();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dcsView.getVideoView().destroyCamera();
    }

    @Override
    public void onCurrentViewChanged(DcsView dcsView, int lastView, int currentView) {

        if(currentView == DcsView.DVE_IMAGEGALLERYVIEW){
            tvShow.setVisibility(View.VISIBLE);
            tvTitle.setVisibility(View.VISIBLE);
            t2=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if(status == TextToSpeech.SUCCESS) {
                        t2.setLanguage(Locale.UK);
                        String toSpeak = "Press Volume Down Button to run OCR";
                        Toast.makeText(getApplicationContext(), toSpeak,Toast.LENGTH_SHORT).show();
                        t2.setPitch((float) 1.0);
                        t2.setSpeechRate((float) 1.0);
                        t2.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            });
        }else{
            tvShow.setVisibility(View.GONE);
            tvTitle.setVisibility(View.GONE);
        }
    }

    private void requestPermissions(){
        if (Build.VERSION.SDK_INT>22){
            try {
                if (ContextCompat.checkSelfPermission(MainActivity.this,"android.permission.WRITE_EXTERNAL_STORAGE")!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
                }
                if (ContextCompat.checkSelfPermission(MainActivity.this,android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{android.Manifest.permission.CAMERA},CAMERA_OK);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            // do nothing
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            DcsView.setLicense(getApplicationContext(),"483AEC5578C7F3EF8AE656DFA929919B5DF7D701");
        } catch (DcsValueNotValidException e) {
            e.printStackTrace();
        }   
    }
    private String getText(Bitmap bitmap)
    {
        Log.d(LOG_TAG, "inside gettext");
        try {
            tessBaseAPI= new TessBaseAPI();

        }
        catch (Exception e)
        {
            Log.e(LOG_TAG,e.getMessage());

        }
        String datapath = getExternalFilesDir("/").getPath();
        Log.d(LOG_TAG,datapath);


        tessBaseAPI.init(datapath,"eng");
        tessBaseAPI.setImage(bitmap);
        String retStr = "No Result";
        try
        {
            retStr = tessBaseAPI.getUTF8Text();
        }
        catch (Exception e)
        {
            Log.e(LOG_TAG,e.getMessage());
        }
        tessBaseAPI.end();
        return retStr;
    }




//TEXT TO SPEECH: 
package com.example.dynamsoft.scandocument;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import static java.lang.Boolean.TRUE;
public class Main3Activity extends AppCompatActivity {
    Bundle b;
    int status;
    MediaPlayer mp;
    String res1;
    TextToSpeech t1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        b = getIntent().getExtras();
        TextView result = (TextView) findViewById(R.id.textView3);
        result.setText(b.getCharSequence("result"));

        res1 = b.getCharSequence("result").toString();

        result.setMovementMethod(new ScrollingMovementMethod());

        t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    t1.setLanguage(Locale.UK);
                    String toSpeak = "Press Volume down button to read text...";
                    t1.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == android.view.KeyEvent.KEYCODE_VOLUME_DOWN) {
            t1 = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        t1.setLanguage(Locale.UK);

                        t1.speak(res1, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }
            });

            return true;
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        t1.shutdown();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        t1.shutdown(); }}



