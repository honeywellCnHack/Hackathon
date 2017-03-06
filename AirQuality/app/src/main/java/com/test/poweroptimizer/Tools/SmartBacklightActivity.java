package com.test.poweroptimizer.Tools;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.test.poweroptimizer.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SmartBacklightActivity extends AppCompatActivity {

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {

        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            return false;
        }
    };
    private ListView mProfilelistView;
    private TextView mNotificationView;
    private LayoutInflater mInflater;
    private boolean mVisible;
    ProfileListAdapter mAdapter;
    Cursor profileListCur = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_smart_backlight);

        mInflater = LayoutInflater.from(this);
        mVisible = true;
        mProfilelistView = (ListView) findViewById(R.id.profileList);
        mNotificationView = (TextView) findViewById(R.id.notification_text);
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            if (profileListCur != null)
                profileListCur.close();

            profileListCur = getContentResolver().query(BacklightConst.CONTENT_URI, BacklightConst.PROJECTION, null, null,
                    null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mAdapter = new ProfileListAdapter(this);

        initUI();
    }

    private void initUI () {
        if (mAdapter.getCount() == 0) {
            mProfilelistView.setVisibility(View.INVISIBLE);
            mNotificationView.setVisibility(View.VISIBLE);
            mNotificationView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent();
                    ComponentName componentName = new ComponentName(SmartBacklightActivity.this.getPackageName(),
                            SmartBacklightActivity.this.getPackageName() + ".Tools.BacklightPreferences");
                    intent.setComponent(componentName);
                    startActivity(intent);
                }
            });
        } else {
            mProfilelistView.setAdapter(mAdapter);
            mProfilelistView.setVisibility(View.VISIBLE);
            mNotificationView.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (profileListCur != null)
            profileListCur.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_smartbacklight, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_add_new) {
            Intent intent = new Intent();
            ComponentName componentName = new ComponentName(this.getPackageName(),
                    this.getPackageName() + ".Tools.BacklightPreferences");
            intent.setComponent(componentName);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class ProfileListAdapter extends BaseAdapter {
        private int mCount = 0;

        public ProfileListAdapter(Context context) {
            super();

            if (profileListCur != null)
                mCount = profileListCur.getCount();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            String str = "Profile name";
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.profile_list_item,
                        null);
            }

            profileListCur.moveToPosition(position);
            final TextView firstLine = (TextView) convertView.findViewById(R.id.first_line);
            firstLine.setText(profileListCur.getString(BacklightConst.INDEX_PROFILE_NAME));

            final TextView secondLine = (TextView) convertView.findViewById(R.id.second_line);
            String packagename = profileListCur.getString(BacklightConst.INDEX_PN);
            int level = profileListCur.getInt(BacklightConst.INDEX_BACKLIGHT_LEVEL);
            int timeout = profileListCur.getInt(BacklightConst.INDEX_BACKLIGHT_TIME);
            String status = profileListCur.getString(BacklightConst.INDEX_STATUS);

            secondLine.setText(packagename + "  " + level+"%"+ "  " + timeout +"s");
            secondLine.setHorizontallyScrolling(true);

            final TextView index = (TextView) convertView.findViewById(R.id.profile_index);
            index.setText("" + (position+1)+".");

            Switch onoff = (Switch) convertView.findViewById(R.id.profile_switch);
            onoff.setTag(packagename);
            onoff.setChecked("on".equals(status));

            convertView.setTag(packagename);

            onoff.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Cursor cursor = getContentResolver().query(BacklightConst.CONTENT_URI, BacklightConst.PROJECTION, BacklightConst.STRING_PN+"=?",
                            new String[]{(String)v.getTag()}, null);

                    if (cursor != null) {
                        cursor.moveToFirst();
                        String status = ((Switch)v).isChecked()?"on":"off";
                        ContentValues cv = new ContentValues();
                        // replace package with classname
                        cv.put(BacklightConst.STRING_PROFILE_NAME, cursor.getString(BacklightConst.INDEX_PROFILE_NAME));
                        cv.put(BacklightConst.STRING_PN, cursor.getString(BacklightConst.INDEX_PN));
                        cv.put(BacklightConst.STRING_BACKLIGHT_LEVEL, cursor.getInt(BacklightConst.INDEX_BACKLIGHT_LEVEL));
                        cv.put(BacklightConst.STRING_STATUS, status);
                        cv.put(BacklightConst.STRING_BACKLIGHT_TIME, cursor.getInt(BacklightConst.INDEX_BACKLIGHT_TIME));

                        getContentResolver().update(BacklightConst.CONTENT_URI, cv, BacklightConst.STRING_PN+"=?",
                                new String[]{(String)v.getTag()});
                    }
                }
            });

            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                public String selectedPackage = null;
                public boolean onLongClick(View arg0) {
                    selectedPackage = (String)arg0.getTag();
                    AlertDialog.Builder builder = new AlertDialog.Builder(SmartBacklightActivity.this);
                    builder.setMessage(R.string.dialog_profile_delete_prompt);
                    builder.setTitle(R.string.dialog_profile_delete_title);
                    builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            getContentResolver().delete(BacklightConst.CONTENT_URI,
                                    BacklightConst.STRING_PN + "='" + selectedPackage + "'", null);

                            try {
                                if (profileListCur != null)
                                    profileListCur.close();

                                profileListCur = getContentResolver().query(BacklightConst.CONTENT_URI, BacklightConst.PROJECTION, null, null,
                                        null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            mAdapter = new ProfileListAdapter(SmartBacklightActivity.this);
                            initUI();
                        }
                    });
                    builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    builder.create().show();

                    return true;
                }
            });

            return convertView;
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

    }
}
