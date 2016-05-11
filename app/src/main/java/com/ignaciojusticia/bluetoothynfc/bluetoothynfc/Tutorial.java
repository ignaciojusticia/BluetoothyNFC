package com.ignaciojusticia.bluetoothynfc.bluetoothynfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

public class Tutorial extends Activity {

    Integer[] imageIDs = {
            R.drawable.uno_opt,
            R.drawable.dos_opt,
            R.drawable.tres_opt,
            R.drawable.cuatro_opt,
            R.drawable.cinco_opt,
            R.drawable.ayuda_opt,
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
                SharedPreferences pref = getSharedPreferences("ActivityPREF", Context.MODE_PRIVATE);
                if(pref.getBoolean("activity_executed", false)){
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    SharedPreferences.Editor ed = pref.edit();
                    ed.putBoolean("activity_executed", true);
                    ed.commit();
                }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial);

        Gallery gallery = (Gallery) findViewById(R.id.gallery);
        gallery.setAdapter(new ImageAdapter(this));
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

            }
        });

    }

    public class ImageAdapter extends BaseAdapter {
        private Context context;
        private int itemBackground;
        public ImageAdapter(Context c)
        {
            context = c;
            // sets a grey background; wraps around the images
            TypedArray a = obtainStyledAttributes(R.styleable.MyGallery);
            itemBackground = a.getResourceId(R.styleable.MyGallery_android_galleryItemBackground, 0);
            a.recycle();
        }
        // returns the number of images
        public int getCount() {
            return imageIDs.length;
        }
        // returns the ID of an item
        public Object getItem(int position) {
            return position;
        }
        // returns the ID of an item
        public long getItemId(int position) {
            return position;
        }
        // returns an ImageView view
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(imageIDs[position]);
           // imageView.setLayoutParams(new Gallery.LayoutParams(100, 100));
            imageView.setBackgroundResource(itemBackground);
            return imageView;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
