package firebasecloud.com.firebasecloud.CallClasses;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.OvershootInterpolator;

import firebasecloud.com.firebasecloud.MainActivity;
import firebasecloud.com.firebasecloud.R;
import firebasecloud.com.firebasecloud.databinding.ActivityAcceptedOfferBinding;

public class AcceptedOffer extends AppCompatActivity {
    ActivityAcceptedOfferBinding binding;
    private Vibrator vib;
    private MediaPlayer mp;
    private static final int SWIPE_MIN_DISTANCE = 120;

    float x1, x2, callingXPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);


        binding = DataBindingUtil.setContentView(this, R.layout.activity_accepted_offer);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        mp = MediaPlayer.create(this, defaultSoundUri);
        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vib != null) {
            //vib.vibrate(500);

            callingXPos = getCallingButtonX();

        }
        mp.start();


        binding.imCall.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    x1 = event.getX();
                    binding.imCall.setVisibility(View.INVISIBLE);

                    binding.imPick.setVisibility(View.VISIBLE);
                    binding.imCancel.setVisibility(View.VISIBLE);


                    /*ObjectAnimator animator = ObjectAnimator.ofFloat(binding.imPick, "translationX", 250f);

                    animator.setDuration(5000);
                    animator.setRepeatMode(ValueAnimator.RESTART);
                    animator.start();

                    ObjectAnimator.ofFloat(binding.imCancel, "translationX", -250f).setDuration(1000).start();
                    */

                    //    binding.imPick.animate().translationX(callingXPos).setDuration(1000);
                    //  binding.imCancel.animate().translationX(callingXPos).setDuration(1000);


                    binding.imPick.animate().translationX(250).setDuration(800).setInterpolator(new OvershootInterpolator());
                    binding.imCancel.animate().translationX(-250).setDuration(800).setInterpolator(new OvershootInterpolator());

//                    binding.imPick.getAnimation().start();
                    //                  binding.imCancel.getAnimation().start();



                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    binding.imCall.setVisibility(View.VISIBLE);
                    // binding.imPick.setVisibility(View.INVISIBLE);
                    // binding.imCancel.setVisibility(View.INVISIBLE);


                    binding.imPick.animate().translationX(callingXPos).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            binding.imCall.setVisibility(View.VISIBLE);
                        }
                    }).start();
                    binding.imCancel.animate().translationX(callingXPos).setDuration(1000).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            binding.imCall.setVisibility(View.VISIBLE);
                        }
                    }).start();

                    binding.imPick.setVisibility(View.INVISIBLE);
                    binding.imCancel.setVisibility(View.INVISIBLE);

                    x2 = event.getX();
                    float deltaX = x2 - x1;

                    if (Math.abs(deltaX) > SWIPE_MIN_DISTANCE) {
                        // Left to Right swipe action
                        if (x2 > x1) {


                            Intent intent = new Intent(AcceptedOffer.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                            //  Toast.makeText(AcceptedOffer.this, "Left to Right swipe [Next]", Toast.LENGTH_SHORT).show();
                        }

                        // Right to left swipe action
                        else {
                            finish();
                        }

                    } else {
                        // consider as something else - a screen tap for example
                        //         Toast.makeText(AcceptedOffer.this, "Swipe right to Accept ", Toast.LENGTH_SHORT).show();

                    }
                }


                return true;
            }
        });

    }

    public float getCallingButtonX() {
        callingXPos = binding.imCall.getX();
        return callingXPos;
    }

    @Override
    protected void onPause() {
        super.onPause();

        mp.stop();
    }
}
