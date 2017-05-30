package pl.edu.pwr.lab2borowiak.lab3sensors;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.os.PersistableBundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class  MainActivity extends AppCompatActivity implements SensorEventListener{

    @BindView(R.id.left) ImageButton leftButton;
    @BindView(R.id.right) ImageButton rightButton;
    @BindView(R.id.memImage) ImageView memImage;
    @BindView(R.id.imageNumber) TextView imageNumberView;

    private static final String FAVOURITES_TABLE = "favourites";
    private static final String IMAGE_NUMBER = "imageNumber";
    int imageNumber = 0;
    private long lastUpdate = 0;
    private float last_y, last_x;
    private static final int SHAKE_THRESHOLD = 500;
    private static final int SHAKE_TIME = 100;

    private List<Integer> pictureTableKwsn;
    private List<Integer> pictureLikeKwsn;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Menu menuBar;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);


        setPictureTableKwsn();
        //setStart();

        leftButton.setOnClickListener(leftClick);
        rightButton.setOnClickListener(rightClick);

        setSensors();

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("APP TITLE");
        Toast.makeText(getApplicationContext(),"create", Toast.LENGTH_SHORT).show();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        this.menuBar = menu;
        setFavouriteIcon(pictureTableKwsn.get(imageNumber));
        Toast.makeText(getApplicationContext(),"menu", Toast.LENGTH_SHORT).show();
        setImage(imageNumber);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntegerArrayList(FAVOURITES_TABLE, (ArrayList<Integer>) pictureLikeKwsn);
        outState.putInt(IMAGE_NUMBER, imageNumber);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null){
            pictureLikeKwsn = savedInstanceState.getIntegerArrayList(FAVOURITES_TABLE);
            imageNumber = savedInstanceState.getInt(IMAGE_NUMBER);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case R.id.action_favorite:
                setFavourite(item);
                return true;
            case R.id.action_info:
                setAlertDialog();
                return true;
            case R.id.action_search:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Podaj numer zdjęcia");
                final EditText input = new EditText(this);
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);
                builder.setPositiveButton("Szukaj", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int photoNumber = Integer.valueOf(input.getText().toString());
                        if(photoNumber > pictureTableKwsn.size())
                            Toast.makeText(getApplicationContext(), "Nieprawidłowy numer zdjęcia", Toast.LENGTH_SHORT).show();
                        else
                            setImage(photoNumber-1);
                    }
                });
                builder.create().show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setFavourite(MenuItem item){
        if(pictureLikeKwsn.contains(pictureTableKwsn.get(imageNumber))){
            pictureLikeKwsn.remove(pictureTableKwsn.indexOf(pictureTableKwsn.get(imageNumber)));
            item.setIcon(R.mipmap.ic_favorite_black);
        }
        else{
            pictureLikeKwsn.add(pictureTableKwsn.get(imageNumber));
            item.setIcon(R.mipmap.ic_favorite);
        }
    }

    private void setFavouriteIcon(int id){
        if(pictureLikeKwsn.contains(id))
            menuBar.findItem(R.id.action_favorite).setIcon(R.mipmap.ic_favorite);
        else
            menuBar.findItem(R.id.action_favorite).setIcon(R.mipmap.ic_favorite_black);

    }

    private void setAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.info));
        builder.setMessage(R.string.about_author);
        builder.create().show();
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;


        float x = event.values[0];
        float y = event.values[1];


        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > SHAKE_TIME) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

            float speedy = Math.abs(y -last_y)/ diffTime * 10000;

            float speedx = Math.abs(x - last_x) / diffTime * 10000;
            if (speedy > SHAKE_THRESHOLD) {
                next();
            }

            if(speedx > SHAKE_THRESHOLD) {
                previous();
            }
            last_x = x;
            last_y = y;

        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }


    private void setPictureTableKwsn(){
        pictureTableKwsn = new ArrayList<>();
        pictureTableKwsn.add(R.drawable.kwasniewski);
        pictureTableKwsn.add(R.drawable.kwasniewskib);
        pictureTableKwsn.add(R.drawable.kwasniewskic);
        pictureTableKwsn.add(R.drawable.kwasniewskid);
        pictureTableKwsn.add(R.drawable.kwasniewskie);

        pictureLikeKwsn = new ArrayList<>();
    }


    private void setStart(){
        memImage.setImageResource(pictureTableKwsn.get(0));
        setImageNumberView(imageNumber, pictureTableKwsn.size());
    }


    View.OnClickListener leftClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            previous();
        }
    };


    View.OnClickListener rightClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            next();
        }
    };


    private void setImageNumberView(int number, int size){
        imageNumberView.setText(number + 1 + " / " + size);
    }

    private void setImage(int number){
        memImage.setImageResource(pictureTableKwsn.get(number));
        setImageNumberView(number, pictureTableKwsn.size());
        setFavouriteIcon(pictureTableKwsn.get(number));
        imageNumber = number;
    }

    private void next(){
        imageNumber = (imageNumber + 1) % pictureTableKwsn.size();
        setImage(imageNumber);
    }

    private void previous(){
        imageNumber = (imageNumber + pictureTableKwsn.size() - 1) % pictureTableKwsn.size();
        setImage(imageNumber);
    }


    private void setSensors(){
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);
    }
}
