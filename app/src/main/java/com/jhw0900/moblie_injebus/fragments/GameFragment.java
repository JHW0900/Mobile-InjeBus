package com.jhw0900.moblie_injebus.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jhw0900.moblie_injebus.ListActivity;
import com.jhw0900.moblie_injebus.R;

public class GameFragment extends Fragment implements View.OnTouchListener {
    private int clickCount = 0;
    private Handler handler;
    private Runnable gameEndRunnable;
    ImageView catImg;
    TextView clickCounter;
    TextView exitTrigger;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.loading_layout, container, false);

        exitTrigger = view.findViewById(R.id.textView2);
        clickCounter = view.findViewById(R.id.clickCounter);
        catImg = view.findViewById(R.id.catImg);

        exitTrigger.setOnClickListener(v -> {
            ((ListActivity) getActivity()).onGameEnd();
        });

        // Set up a handler to end the game after a certain time
        handler = new Handler();
        gameEndRunnable = () -> {
            // End game logic
            if (getActivity() instanceof ListActivity) {
                ((ListActivity) getActivity()).onGameEnd();
            }
        };
        handler.postDelayed(gameEndRunnable, 1000 * 120); // Game ends after 30 seconds

        view.setOnTouchListener(this);

        return view;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                clickCount++;
                clickCounter.setText(String.valueOf(clickCount));

                catImg.setImageResource(R.drawable.popcat_01);
                return true;

            case MotionEvent.ACTION_UP:
                catImg.setImageResource(R.drawable.popcat_02);
                return true;
        }
        return false;
    }

    public void setExitVisiblity(boolean isVisible){
        if(!isVisible)
            exitTrigger.setVisibility(View.GONE);
        else
            exitTrigger.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove callbacks when the fragment is destroyed
        handler.removeCallbacks(gameEndRunnable);
    }
}
