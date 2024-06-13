package com.jhw0900.moblie_injebus;

import static android.app.PendingIntent.getActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.jhw0900.moblie_injebus.data.service.AuthenticationService;

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
    AuthenticationService authService;

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
        beginData = new HashMap<>();
        endData = new HashMap<>();

        authService = new AuthenticationService();

        ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new SchedulePagerAdapter(getSupportFragmentManager()));

        Spinner regionSpinner = findViewById(R.id.regionSpinner);
        Spinner seatSpinner = findViewById(R.id.seatSpinner);

        initRegionSpinner(regionSpinner);
        initSeatSpinner(seatSpinner);
    }

    private void initSeatSpinner(Spinner spinner){
        List<String> seats = new ArrayList<>();
        seats.add("좌석 선택");
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

                ViewPager viewPager = findViewById(R.id.viewPager);
                viewPager.setAdapter(new SchedulePagerAdapter(getSupportFragmentManager()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private static class SchedulePagerAdapter extends FragmentPagerAdapter {

        private static final String[] DAYS = {"월요일", "화요일", "수요일", "목요일", "금요일"};

        public SchedulePagerAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return ScheduleFragment.newInstance(DAYS[position], selRegion);
        }

        @Override
        public int getCount() {
            return DAYS.length;
        }
    }

    public void onClick(View v){
        Map<String, String> reqData = new HashMap<>();
        reqData.put("lineCode", "8");
        reqData.put("begin_line_sun", "free");
        reqData.put("end_line_sun", "free");
        reqData.put("begin_line_sat", "free");
        reqData.put("end_line_sat", "free");

        String []days = {"mon", "tue", "wed", "thu", "fri"};
        for(String day : days){
            reqData.put("begin_line_"+day, "08:00");
            reqData.put("end_line_"+day, "17:20");
        }

        int seatNum = 5;
        Map<String, Object> dateSet = getCurDate();
        setTimeData(reqData);

        AtomicBoolean shouldContinue = new AtomicBoolean(true);
        AtomicBoolean shouldSkip = new AtomicBoolean(false);

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
                                Toast.makeText(ListActivity.this, "요청하신 작업이 완료되었습니다.", Toast.LENGTH_LONG);
                                shouldContinue.set(false);
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

}
