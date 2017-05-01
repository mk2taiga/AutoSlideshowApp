package jp.techacademy.taiga.miyazaki.autoslideshowapp;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.os.Bundle;
import android.widget.Button;
import android.os.Handler;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    int fieldIndex;
    Long id;
    Uri imageUri;
    Cursor cursor = null;
    ContentResolver resolver;

    Timer mTimer;
    Handler mHandler = new Handler();

    ImageView imageView;
    Button upButton;
    Button backButton;
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        upButton = (Button) findViewById(R.id.button1);
        upButton.setOnClickListener(this);
        backButton = (Button) findViewById(R.id.button2);
        backButton.setOnClickListener(this);
        startButton = (Button) findViewById(R.id.button3);
        startButton.setOnClickListener(this);

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {
        // 画像の情報を取得する
        resolver = getContentResolver();
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );

        if (cursor.moveToFirst()) {
            cursor.moveToFirst();
            getDisplay();
        } else {
            cursor.close();
            upButton.setEnabled(false);
            backButton.setEnabled(false);
            startButton.setEnabled(false);
        }
    }

    private void getDisplay() {
        fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        id = cursor.getLong(fieldIndex);
        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageURI(imageUri);
    }

    //ボタンを押された時のメソッド
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button1) {
            if (cursor.isLast()) {
                cursor.moveToFirst();
            } else {
                cursor.moveToNext();
            }
            getDisplay();
        }else if (v.getId() == R.id.button2) {
            if (cursor.isFirst()) {
                cursor.moveToLast();
            } else {
                cursor.moveToPrevious();
            }
            getDisplay();
        }else if (v.getId() == R.id.button3) {
            slideShow();
        }
    }

    public void slideShow() {
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    if (cursor.isLast()) {
                        cursor.moveToFirst();
                    } else {
                        cursor.moveToNext();
                    }

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            getDisplay();
                        }
                    });
                }
            },2000,2000);

            upButton.setEnabled(false);
            backButton.setEnabled(false);
            startButton.setText("停止");
        } else if (mTimer != null) {
            mTimer.cancel();
            upButton.setEnabled(true);
            backButton.setEnabled(true);
            startButton.setText("再生");
            mTimer = null;
        }

    }

}
