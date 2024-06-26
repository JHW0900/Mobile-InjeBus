package com.jhw0900.moblie_injebus;

import static android.app.PendingIntent.getActivity;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.jhw0900.moblie_injebus.data.service.AuthenticationService;
import com.jhw0900.moblie_injebus.fragments.GameFragment;
import com.jhw0900.moblie_injebus.fragments.ScheduleFragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class ListActivity extends AppCompatActivity {

    private Map<String, Integer> regionMap;
    private Map<Integer,
            Map<String, ArrayList<String>>
            > timeList;
    private static Map<String, String> beginData;
    private static Map<String, String> endData;
    private static String selRegion = "";
    private GameFragment gameFragment;
    AuthenticationService authService;

    private FragmentManager fragmentManager;
    private SchedulePagerAdapter schedulePagerAdapter;
    private ViewPager viewPager;

    Spinner regionSpinner;
    Spinner seatSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.list), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        authService = new AuthenticationService();
        getRecentInfo();

        fragmentManager = getSupportFragmentManager();
        Button fetchDataButton = findViewById(R.id.button2);
        fetchDataButton.setOnClickListener(this::onClick);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();
                } else {
                    finish();
                }
            }
        });

        beginData = new HashMap<>();
        endData = new HashMap<>();

        viewPager = findViewById(R.id.viewPager);
        schedulePagerAdapter = new SchedulePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(schedulePagerAdapter);

        regionSpinner = findViewById(R.id.regionSpinner);
        seatSpinner = findViewById(R.id.seatSpinner);

        initRegionSpinner(regionSpinner);
        initSeatSpinner(seatSpinner);
    }

    private void initSeatSpinner(Spinner spinner){
        List<String> seats = new ArrayList<>();
        seats.add("선호 좌석 선택");
        for(int i = 1; i < 45; i++) seats.add(i + "번");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, seats);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void initRegionSpinner(Spinner spinner){
        List<String> regions = new ArrayList<>();
        regions.add("노선 선택");
        regions.add("동래");
        regions.add("마산");
//        regions.add("양산 - 물금");
//        regions.add("양산 - 북정");
//        regions.add("영도/부산역");
        regions.add("울산");
//        regions.add("장유");
//        regions.add("진해");
        regions.add("창원");
//        regions.add("창원-마산");
        regions.add("하단");
//        regions.add("해운대");

        regionMap = new HashMap<>();
        regionMap.put("노선 선택", -1);
        regionMap.put("동래", 3);
        regionMap.put("마산", 2);
//        regionMap.put("양산 - 물금", 18);
//        regionMap.put("양산 - 북정", 11);
//        regionMap.put("영도/부산역", 7);
        regionMap.put("울산", 12);
//        regionMap.put("장유", 10);
//        regionMap.put("진해", 9);
        regionMap.put("창원", 8);
//        regionMap.put("창원-마산", 21);
        regionMap.put("하단", 4);
//        regionMap.put("해운대", 5);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, regions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedRegion = (String) parent.getItemAtPosition(position);
                int value = regionMap.get(selectedRegion);

                selRegion = selectedRegion;
                viewPager.setAdapter(schedulePagerAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private class SchedulePagerAdapter extends FragmentPagerAdapter {

        private final String[] DAYS = {"월요일", "화요일", "수요일", "목요일", "금요일"};
        private final List<ScheduleFragment> fragments;

        public SchedulePagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            fragments = new ArrayList<>();
            for (String day : DAYS) {
                fragments.add(ScheduleFragment.newInstance(day, selRegion));
            }
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return DAYS.length;
        }

        public ScheduleFragment getFragment(int position) {
            return fragments.get(position);
        }
    }

    public void onClick(View v){
        Map<String, String> reqData = new HashMap<>();
        String regionData = regionSpinner.getSelectedItem().toString();
        int lineCode = regionMap.get(regionData);

        reqData.put("lineCode", String.valueOf(lineCode));
        reqData.put("begin_line_sun", "free");
        reqData.put("end_line_sun", "free");
        reqData.put("begin_line_sat", "free");
        reqData.put("end_line_sat", "free");

        String []days = {"mon", "tue", "wed", "thu", "fri"};
        for(int i = 0; i < days.length; i++){
            String sTime = getSpinnerValueForDay(i, true);
            String eTime = getSpinnerValueForDay(i, false);

            sTime = (sTime == null || sTime.equals("등교시간")) ? "free" : sTime;
            eTime = (eTime == null || eTime.equals("하교시간")) ? "free" : eTime;

            reqData.put("begin_line_"+days[i], sTime);
            reqData.put("end_line_"+days[i], eTime);
        }

        int seatNum;
        String spinnerData = seatSpinner.getSelectedItem().toString();
        if(!"선호 좌석 선택".equals(spinnerData)){
            seatNum = Integer.valueOf(spinnerData.replace("번", ""));
        } else {
            seatNum = 1;
        }

        Map<String, Object> dateSet = getCurDate();
        setTimeData(reqData);

        AtomicBoolean shouldContinue = new AtomicBoolean(true);
        AtomicBoolean shouldSkip = new AtomicBoolean(false);
        //*
        showGameFragment();
        //progressDialog = ProgressDialog.show(ListActivity.this, "Loading", "Please wait...", true);

        new Thread(() -> {
            while (shouldContinue.get()) {
                if (shouldSkip.getAndSet(false)) {
                    // Reset shouldSkip and skip to the next iteration
                    continue;
                }

                LocalDate curDate;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    curDate = (LocalDate) dateSet.get("curDate");
                } else {
                    curDate = null;
                }
                String curDay = (String) dateSet.get("curDay");
                String dateCode = (String) dateSet.get("dateCode");
                authService.getLine(dateCode, new AuthenticationService.GetLineCallBack() {
                    @Override
                    public void onSuccess(Map<String, String> lineSet) {
                        runOnUiThread(() -> {

                            if ("error".equals(lineSet.get("status"))) {
                                shouldContinue.set(false);
                                onCompletion();
                            } else if ("free".equals(beginData.get(curDay)) || lineSet.get("lineCode") == null) {
                                // Other conditions
                                String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

                                // Calculate next date
                                LocalDate nextDate = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    nextDate = curDate.plusDays(1);
                                }

                                // Get the day of the week
                                String curDay = null; // To match Python's weekday() output
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    curDay = days[nextDate.getDayOfWeek().getValue() % 7];
                                }

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    dateSet.put("dateCode", nextDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
                                }

                                dateSet.put("curDate", nextDate);
                                dateSet.put("curDay", curDay);

                                shouldSkip.set(true);
                            }

                            Map<String, String> reqData = new HashMap<>();
                            reqData.put("lineCode", "8");
                            reqData.put("dateCode", dateCode);
                            authService.getTime(reqData, new AuthenticationService.GetTimeCallBack() {
                                @Override
                                public void onSuccess(List<HashMap<String, String>> timeList) {
                                    runOnUiThread(() -> {

                                        String cBeginTime = beginData.get(curDay);
                                        String cEndTime = endData.get(curDay);

                                        Log.d("BOOK_TIME", "===========================");
                                        Log.d("BOOK_TIME", "****" + dateCode + "****");
                                        for(Map<String, String> m : timeList){
                                            String busType = m.get("busType");
                                            String busTime = m.get("busTime");

                                            if(busType.equals("등교") && busTime.equals(cBeginTime)){
                                                Log.d("BOOK_TIME", "등교: ");
                                                authService.getBusCode(reqData.get("lineCode"), m.get("busCode"), new AuthenticationService.GetBusCodeCallBack() {
                                                    @Override
                                                    public void onSuccess(String busCode) {
                                                        runOnUiThread(() -> {
                                                            Log.d("getBusCode", "Success");
                                                            authService.bookBus(busCode, seatNum, new AuthenticationService.BookBusCallBack() {
                                                                @Override
                                                                public void onSuccess() {
                                                                    runOnUiThread(() -> {
                                                                        Log.d("getBusCode", "Success");
                                                                    });
                                                                }

                                                                @Override
                                                                public void onFailure() {
                                                                    runOnUiThread(() -> {
                                                                        Log.d("getBusCode", "FAILED");
                                                                    });
                                                                }
                                                            });
                                                        });
                                                    }

                                                    @Override
                                                    public void onFailure() {
                                                        runOnUiThread(() -> {});
                                                    }
                                                });

                                                Log.d("BOOK_TIME", "탑승 시간:" + busTime);
                                            }
                                            else if(busType.equals("하교") && busTime.equals(cEndTime)){
                                                Log.d("BOOK_TIME", "하교: ");
//                                                String busCode = getBusCode(reqData.get("lineCode"), m.get("busCode"));
                                                authService.getBusCode(reqData.get("lineCode"), m.get("busCode"), new AuthenticationService.GetBusCodeCallBack() {
                                                    @Override
                                                    public void onSuccess(String busCode) {
                                                        runOnUiThread(() -> {
                                                            Log.d("getBusCode", "Success");
                                                            authService.bookBus(busCode, seatNum, new AuthenticationService.BookBusCallBack() {
                                                                @Override
                                                                public void onSuccess() {
                                                                    runOnUiThread(() -> {
                                                                        Log.d("getBusCode", "Success");
                                                                    });
                                                                }

                                                                @Override
                                                                public void onFailure() {
                                                                    runOnUiThread(() -> {
                                                                        Log.d("getBusCode", "FAILED");
                                                                    });
                                                                }
                                                            });
                                                        });
                                                    }

                                                    @Override
                                                    public void onFailure() {
                                                        runOnUiThread(() -> {});
                                                    }
                                                });
                                            }
                                        }
                                        Log.d("BOOK_TIME", "===========================");
                                    });
                                }

                                @Override
                                public void onFailure() {
                                    runOnUiThread(() -> {});
                                }
                            });
                        });
                    }

                    @Override
                    public void onFailure() {
                        runOnUiThread(() -> {});
                    }
                });

                // Sleep or wait for a short period before the next iteration
                try {
                    Thread.sleep(1000); // Adjust the sleep time as needed
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        // */
    }

    private void onCompletion() {
        runOnUiThread(() -> {
            getRecentInfo();
            gameFragment.setExitVisiblity(true);

//            if (progressDialog != null && progressDialog.isShowing()) {
//                progressDialog.dismiss();
//            }

            Toast.makeText(ListActivity.this, "요청하신 예약이 완료되었습니다!", Toast.LENGTH_LONG).show();
        });
    }

    public static Map<String, Object> getCurDate() {
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        LocalDate curDate = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            curDate = LocalDate.now();
        }
        String curDay = null; // To match Python's weekday() output
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            curDay = days[curDate.getDayOfWeek().getValue() % 7];
        }

        Map<String, Object> dateSet = new HashMap<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dateSet.put("dateCode", curDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        }
        dateSet.put("curDate", curDate);
        dateSet.put("curDay", curDay.toString());



        return dateSet;
    }

    public void setTimeData(Map<String, String> map){
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

        for(String day : days){
            beginData.put(day, (String) map.get("begin_line_" + day.toLowerCase()));
            endData.put(day, (String) map.get("end_line_" + day.toLowerCase()));
        }
    }

    public void getRecentInfo(){
        authService.getRecentInfo(new AuthenticationService.GetRecentInfoCallBack(){

            @Override
            public void onSuccess(Map<String, String> res) {
                runOnUiThread(() -> {
                    int []idList = {
                            R.id.nextBus, R.id.busNumber, R.id.sRegion, R.id.eRegion, R.id.seatNumber,
                            R.id.boardBus, R.id.seatTitle, R.id.boardTitle, R.id.cancelTitle,
                            R.id.cancelTime };

                    Map<Integer, String> busInfoMap = new HashMap<>();
                    busInfoMap.put(R.id.nextBus, "busDate");
                    busInfoMap.put(R.id.busNumber, "busNumber");
                    busInfoMap.put(R.id.sRegion, "sRegion");
                    busInfoMap.put(R.id.eRegion, "eRegion");
                    busInfoMap.put(R.id.seatNumber, "seatNumber");
                    busInfoMap.put(R.id.boardBus, "boardBus");
                    busInfoMap.put(R.id.cancelTime, "cancelTime");

                    if("success".equals(res.get("status"))){
                        ((LinearLayout) findViewById(R.id.listLayout)).setPadding(64, 64, 64, 64);
                        ((TextView) findViewById(R.id.textView)).setVisibility(View.GONE);
                        for (int j : idList){
                            ((TextView) findViewById(j)).setVisibility(View.VISIBLE);
                        }
                        ((ImageView) findViewById(R.id.directionIco)).setVisibility(View.VISIBLE);
                        for(int id : busInfoMap.keySet()){
                            if(id == R.id.nextBus)
                                ((TextView) findViewById(id)).setText("다음 버스: " + res.get(busInfoMap.get(id)));
                            else
                                ((TextView) findViewById(id)).setText(res.get(busInfoMap.get(id)));
                        }
                    } else {
                        ((LinearLayout) findViewById(R.id.listLayout)).setPadding(16, 250, 16, 250);
                        ((TextView) findViewById(R.id.textView)).setVisibility(View.VISIBLE);
                        for (int j : idList) {
                            ((TextView) findViewById(j)).setVisibility(View.GONE);
                        }
                        ((ImageView) findViewById(R.id.directionIco)).setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onFailure() {runOnUiThread(() -> {
                System.out.println("Failed");
            });}
        });
    }

    private void showGameFragment() {
        gameFragment = new GameFragment();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, gameFragment);
        transaction.commit();

        findViewById(R.id.fragmentContainer).setVisibility(View.VISIBLE);
        findViewById(R.id.cardView).setVisibility(View.GONE);
    }

    private void hideGameFragment() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
        if (fragment != null) {
            transaction.remove(fragment);
            transaction.commit();
            fragmentManager.popBackStack();
        }

        // Ensure the fragment container is hidden
        findViewById(R.id.fragmentContainer).setVisibility(View.GONE);
        findViewById(R.id.cardView).setVisibility(View.VISIBLE);
    }

    public void onGameEnd() {
        hideGameFragment();
    }

    public String getSelRegion(){ return selRegion; }

    public String getSpinnerValueForDay(int position, boolean isStart) {
        ScheduleFragment fragment = schedulePagerAdapter.getFragment(position);
        if(isStart) return fragment.getStartSpinnerValue();
        else return fragment.getEndSpinnerValue();
    }
}
