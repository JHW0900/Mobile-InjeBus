package com.jhw0900.moblie_injebus.data.service;

import com.jhw0900.moblie_injebus.data.common.ApiClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthenticationService {
    private ApiService apiService;

    public AuthenticationService(){
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public interface LoginCallback {
        void onSuccess();
        void onFailure();
    }

    public interface GetTimeCallBack {
        void onSuccess(List<HashMap<String, String>> timeList);
        void onFailure();
    }

    public interface GetLineCallBack {
        void onSuccess(Map<String, String> lineSet);
        void onFailure();
    }

    public interface GetBusCodeCallBack {
        void onSuccess(String lineSet);
        void onFailure();
    }
    public interface BookBusCallBack {
        void onSuccess();
        void onFailure();
    }

    public interface GetRecentInfoCallBack {
        void onSuccess(Map<String, String> res);
        void onFailure();
    }


    public void login(String id, String password, LoginCallback callback) {
        Call<HashMap<String, String>> loginCall = apiService.login(id, password);
        loginCall.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                if (response.isSuccessful()) {
                    Map<String, String> resData = (HashMap<String, String>) response.body();

                    if(resData.get("status").equals("success")){
                        System.out.println("Login Successful!");
                        callback.onSuccess();
                    } else {
                        System.out.println("Login Failed.");
                        System.out.println(resData.get("message"));
                        callback.onFailure();
                    }

                } else {
                    System.out.println("Login Failed.");
                    callback.onFailure();
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                t.printStackTrace();
                callback.onFailure();
            }
        });
    }

    public void getLine(String dateCode, GetLineCallBack callback) {
        Call<HashMap<String, String>> getLineCall = apiService.getLine(dateCode);
        getLineCall.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                if (response.isSuccessful()) {
                    Map<String, String> resData = (HashMap<String, String>) response.body();

                    String jsonResponse = resData.get("lineList");

                    Map<String, String> lineDict = new HashMap<>();

                    // Mock response parsing (assuming JSON object for simplicity)
                    // Replace specific substrings
                    String lineStr = jsonResponse.replace("양산 - 물금", "양산-물금")
                            .replace("양산 - 북정", "양산-북정")
                            .replace("영도/부산역", "부산역");

                    // Remove unwanted characters and whitespace
                    String parseStr = lineStr.replaceAll("[^0-9가-힣\\-]", " ").replace("노선선택", "").trim();

                    // Remove multiple spaces
                    parseStr = parseStr.replaceAll(" +", " ");
                    String[] parseList = parseStr.split(" ");

                    // Mock status (assuming status is part of the response)
                    lineDict.put("status", resData.get("status")); // Replace with actual status extraction

                    // Parse the list and create line dictionary
                    for (int i = 0; i < parseList.length - 1; i += 2) {
                        String key = parseList[i];
                        String val = parseList[i + 1];
                        lineDict.put(key, val);
                    }

                    callback.onSuccess(lineDict);
                } else {
                    System.out.println("Login Failed.");
                    callback.onFailure();
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                t.printStackTrace();
                callback.onFailure();
            }
        });
    }

    public void getTime(Map<String, String> params, GetTimeCallBack callback){
        Call<HashMap<String, String>> getTimeCal =
                apiService.getTime(params.get("lineCode"), params.get("dateCode"));
        getTimeCal.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                if (response.isSuccessful()) {
                    Map<String, String> resData = (HashMap<String, String>) response.body();

                    if(resData.get("status").equals("success")){
                        String jsonResponse = resData.get("list");

                        List<HashMap<String, String>> timeList = new ArrayList<>();

                        // Replace specific substrings
                        String timeStr = jsonResponse.replace("양산 - 물금", "양산-물금")
                                .replace("양산 - 북정", "양산-북정")
                                .replace("영도/부산역", "부산역");

                        // Remove unwanted characters and whitespace
                        String parseStr = timeStr.replaceAll("[^0-9가-힣\\-]", " ").replace("노선선택", "").trim();

                        // Remove multiple spaces
                        parseStr = parseStr.replaceAll(" +", " ");
                        String[] parseList = parseStr.split(" ");

                        // Parse the list and create time dictionary
                        for (int i = 0; i < parseList.length - 5; i += 6) {
                            HashMap<String, String> timeDict = new HashMap<>();
                            timeDict.put("busCode", parseList[i + 1]);
                            timeDict.put("busType", parseList[i + 2]);
                            timeDict.put("busTime", parseList[i + 3] + ":" + parseList[i + 4]);

                            timeList.add(timeDict);
                        }

                        callback.onSuccess(timeList);
                    } else {
                        System.out.println("Request Failed.");
                        System.out.println(resData.get("message"));
                        callback.onFailure();
                    }

                } else {
                    System.out.println("Login Failed.");
                    callback.onFailure();
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                t.printStackTrace();
                callback.onFailure();
            }
        });
    }

    public void getBusCode(String lineCode, String timeCode, GetBusCodeCallBack callback) {
        Call<ResponseBody> getLineCall = apiService.getBusCode(lineCode, timeCode);
        getLineCall.enqueue(new Callback<ResponseBody>() {

            private String parseBusCode(String func) {
                Pattern pattern = Pattern.compile("[^0-9]");
                Matcher matcher = pattern.matcher(func);
                String result = matcher.replaceAll(" ").trim().replaceAll(" +", " ");
                String[] parts = result.split(" ");
                return parts.length > 0 ? parts[0] : null;
            }

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    String busCode = null;
                    ResponseBody resData = response.body();
                    try {
                        String html = resData.string();

                        try {
                            Document soup = Jsoup.parse(html);
                            Elements seats = soup.select(".ui-grid-d > div > a");

                            for (Element seat : seats) {
                                if ("X".equals(seat.text())) {
                                    continue;
                                }

                                String func = seat.attr("onclick");
                                busCode = parseBusCode(func);
                                break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    callback.onSuccess(busCode);
                } else {
                    System.out.println("Login Failed.");
                    callback.onFailure();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                callback.onFailure();
            }
        });
    }

    public void bookBus(String busCode, int seatNum, BookBusCallBack callback) {
        String oriCode = busCode;

        Call<HashMap<String, String>> getLineCall = apiService.bookBus(busCode, seatNum, oriCode);
        getLineCall.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                if (response.isSuccessful()) {
                    HashMap<String, String> resData = response.body();

                    String status = resData.get("status");
                    String msg = resData.get("message");

                    System.out.println("[" + status + "]" + seatNum + "번 좌석" + msg);

                    if("예약은 차량출발 1시간 전까지 가능합니다.".equals(msg)) callback.onFailure();

                    if("success".equals(status)){
                        System.out.println(seatNum + "번 좌석 예약 완료");
                        callback.onSuccess();
                    }
                    else if("이미 선택된 좌석입니다.".equals(msg)){
                        if((seatNum + 1) > 44) callback.onFailure();
                        bookBus(busCode, seatNum + 1, callback);
                    }
                    else callback.onFailure();

                    callback.onSuccess();
                } else {
                    System.out.println("Login Failed.");
                    callback.onFailure();
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                t.printStackTrace();
                callback.onFailure();
            }
        });
    }

    public void getRecentInfo(GetRecentInfoCallBack callback){
        Call<ResponseBody> apiCall = apiService.getRecentInfo();

        apiCall.enqueue(new Callback<ResponseBody>() {
            private String parseOnclickText(String onclickText) {
                String cleanText = onclickText.replaceAll("[^0-9NY]", " ").trim();
                cleanText = cleanText.replaceAll(" +", " ");
                return cleanText;
            }
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    ResponseBody resData = response.body();
                    try {
                        Map<String, String> busInfo = new HashMap<>();
                        busInfo.put("status", "fail");

                        String html = resData.string();

                        Document soup = Jsoup.parse(html);
                        String rootSelector = "#extends > div > ul > li";
                        String infoSelector = rootSelector + " > a";

                        Elements infoList = soup.select(infoSelector);

                        for (int i = 0; i < 1; i++) {
                            if(infoList.isEmpty()) break;

                            Element elInfo = infoList.get(i);
                            Element elCode = infoList.get(i + 1);

//                            "info_line" -> "노선 : 창원"
//                            "info_cancel" -> "취소가능 : (06-17 07:00까지)"
//                            "cancel_code" -> "2380704"
//                            "cancel_type" -> "N"
//                            "info_date" -> "06-17 (월) 08:00"
//                            "info_bus" -> "차량 : 1호차 - 9번"

                            Elements pInfoList = elInfo.getElementsByTag("p");
                            busInfo.put("info_date", elInfo.getElementsByTag("h2").text().replace("\u00a0", " "));
                            busInfo.put("info_line", pInfoList.get(0).text());
                            busInfo.put("info_bus", pInfoList.get(1).text());
                            busInfo.put("info_cancel", pInfoList.get(2).text());

                            busInfo.put("busNumber", busInfo.get("info_bus").split(" - ")[0].split(" : ")[1]);
                            busInfo.put("seatNumber", busInfo.get("info_bus").split(" - ")[1]);

                            busInfo.put("busDate", busInfo.get("info_date").split(" ")[0]
                                    .replace("-", "월 ") + "일");
                            busInfo.put("boardBus", busInfo.get("info_date").split(" ")[2]);
                            busInfo.put("cancelTime", busInfo.get("info_cancel").split(" : ")[1]
                                    .replace("(", "").replace(")", ""));

                            int boardHour = Integer.valueOf(busInfo.get("boardBus").split(":")[0]);
                            if(boardHour > 12){
                                busInfo.put("sRegion", "인제대");
                                busInfo.put("eRegion", busInfo.get("info_line").split(" : ")[1]);
                            } else {
                                busInfo.put("sRegion", busInfo.get("info_line").split(" : ")[1]);
                                busInfo.put("eRegion", "인제대");
                            }

                            String onclickText = elCode.attr("onclick");
                            String cancelInfo = parseOnclickText(onclickText);
                            String[] cancelInfoArray = cancelInfo.split(" ");

                            busInfo.put("cancel_code", cancelInfoArray[0]);
                            busInfo.put("cancel_type", cancelInfoArray[1]);
                            busInfo.put("status", "success");
                        }

                        callback.onSuccess(busInfo);
                    } catch (IOException e) {
                        callback.onFailure();
                        throw new RuntimeException(e);
                    }
                } else {
                    callback.onFailure();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }
}

