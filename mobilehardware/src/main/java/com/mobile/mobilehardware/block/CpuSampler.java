/*
 * Copyright (C) 2016 MarkZhai (http://zhaiyifan.cn).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mobile.mobilehardware.block;

import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Dumps cpu usage.
 */
public class CpuSampler extends AbstractSampler {

    private static final String TAG = CpuSampler.class.getSimpleName();
    private static final int BUFFER_SIZE = 1000;

    /**
     * TODO: Explain how we define cpu busy in README
     */
    private final int BUSY_TIME;

    private final LinkedHashMap<Long, CpuBean> mCpuInfoEntries = new LinkedHashMap<>();
    private int mPid = 0;
    private long mUserLast = 0;
    private long mSystemLast = 0;
    private long mIdleLast = 0;
    private long mIoWaitLast = 0;
    private long mTotalLast = 0;
    private long mAppCpuTimeLast = 0;

    public CpuSampler(long sampleInterval) {
        super(sampleInterval);
        BUSY_TIME = (int) (mSampleInterval * 1.2f);
    }

    @Override
    public void start() {
        super.start();
        reset();
    }

    /**
     * Get cpu rate information
     *
     * @return string show cpu rate information
     */
    public String getCpuRateInfo() {
        StringBuilder sb = new StringBuilder();
        synchronized (mCpuInfoEntries) {
            for (Map.Entry<Long, CpuBean> entry : mCpuInfoEntries.entrySet()) {
                long time = entry.getKey();
                sb.append(time)
                        .append(' ')
                        .append(entry.getValue())
                        .append("\r\n");

            }
        }
        return sb.toString();
    }

    public JSONArray getCpuRateInfoOfJSON() {
        JSONArray jsonArray = new JSONArray();
        synchronized (mCpuInfoEntries) {
            for (Map.Entry<Long, CpuBean> entry : mCpuInfoEntries.entrySet()) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("time", entry.getKey());
                    CpuBean cpuBean = entry.getValue();
                    jsonObject.put("cpu",cpuBean.getCpu());
                    jsonObject.put("app",cpuBean.getApp());
                    jsonObject.put("user",cpuBean.getUser());
                    jsonObject.put("system",cpuBean.getSystem());
                    jsonObject.put("ioWait",cpuBean.getIoWait());
                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
        return jsonArray;
    }

    public LinkedHashMap<Long, CpuBean> getCpuList() {
        return mCpuInfoEntries;
    }

    public void clearCpuList() {
        mCpuInfoEntries.clear();
    }


    public boolean isCpuBusy(long start, long end) {
        if (end - start > mSampleInterval) {
            long s = start - mSampleInterval;
            long e = start + mSampleInterval;
            long last = 0;
            synchronized (mCpuInfoEntries) {
                for (Map.Entry<Long, CpuBean> entry : mCpuInfoEntries.entrySet()) {
                    long time = entry.getKey();
                    if (s < time && time < e) {
                        if (last != 0 && time - last > BUSY_TIME) {
                            return true;
                        }
                        last = time;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void doSample() {
        BufferedReader cpuReader = null;
        BufferedReader pidReader = null;

        try {
            cpuReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), BUFFER_SIZE);
            String cpuRate = cpuReader.readLine();
            if (cpuRate == null) {
                cpuRate = "";
            }

            if (mPid == 0) {
                mPid = android.os.Process.myPid();
            }
            pidReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + mPid + "/stat")), BUFFER_SIZE);
            String pidCpuRate = pidReader.readLine();
            if (pidCpuRate == null) {
                pidCpuRate = "";
            }

            parse(cpuRate, pidCpuRate);
        } catch (Throwable throwable) {
            Log.e(TAG, "doSample: ", throwable);
        } finally {
            try {
                if (cpuReader != null) {
                    cpuReader.close();
                }
                if (pidReader != null) {
                    pidReader.close();
                }
            } catch (IOException exception) {
                Log.e(TAG, "doSample: ", exception);
            }
        }
    }

    private void reset() {
        mUserLast = 0;
        mSystemLast = 0;
        mIdleLast = 0;
        mIoWaitLast = 0;
        mTotalLast = 0;
        mAppCpuTimeLast = 0;
    }

    private void parse(String cpuRate, String pidCpuRate) {
        String[] cpuInfoArray = cpuRate.split(" ");
        if (cpuInfoArray.length < 9) {
            return;
        }

        long user = Long.parseLong(cpuInfoArray[2]);
        long nice = Long.parseLong(cpuInfoArray[3]);
        long system = Long.parseLong(cpuInfoArray[4]);
        long idle = Long.parseLong(cpuInfoArray[5]);
        long ioWait = Long.parseLong(cpuInfoArray[6]);
        long total = user + nice + system + idle + ioWait
                + Long.parseLong(cpuInfoArray[7])
                + Long.parseLong(cpuInfoArray[8]);

        String[] pidCpuInfoList = pidCpuRate.split(" ");
        if (pidCpuInfoList.length < 17) {
            return;
        }

        long appCpuTime = Long.parseLong(pidCpuInfoList[13])
                + Long.parseLong(pidCpuInfoList[14])
                + Long.parseLong(pidCpuInfoList[15])
                + Long.parseLong(pidCpuInfoList[16]);

        if (mTotalLast != 0) {
            long idleTime = idle - mIdleLast;
            long totalTime = total - mTotalLast;

            CpuBean cpuBean = new CpuBean();
            cpuBean.setCpu((totalTime - idleTime) * 100L / totalTime + "%");
            cpuBean.setApp((appCpuTime - mAppCpuTimeLast) * 100L / totalTime + "%");
            cpuBean.setUser((user - mUserLast) * 100L / totalTime + "%");
            cpuBean.setSystem((system - mSystemLast) * 100L / totalTime + "%");
            cpuBean.setIoWait((ioWait - mIoWaitLast) * 100L / totalTime + "%");

            synchronized (mCpuInfoEntries) {
                mCpuInfoEntries.put(System.currentTimeMillis(), cpuBean);
                if (mCpuInfoEntries.size() > CpuInternals.getInstance().getMaxEntryCount()) {
                    for (Map.Entry<Long, CpuBean> entry : mCpuInfoEntries.entrySet()) {
                        Long key = entry.getKey();
                        mCpuInfoEntries.remove(key);
                        break;
                    }
                }
            }
        }
        mUserLast = user;
        mSystemLast = system;
        mIdleLast = idle;
        mIoWaitLast = ioWait;
        mTotalLast = total;

        mAppCpuTimeLast = appCpuTime;
    }
}