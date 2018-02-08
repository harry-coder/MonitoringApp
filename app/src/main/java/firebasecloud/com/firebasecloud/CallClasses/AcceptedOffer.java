package firebasecloud.com.firebasecloud.CallClasses;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;

import java.text.ParseException;

import firebasecloud.com.firebasecloud.FirebaseMessangingService;
import firebasecloud.com.firebasecloud.MainActivity;
import firebasecloud.com.firebasecloud.R;
import firebasecloud.com.firebasecloud.TaskFragments.NewTasks;
import firebasecloud.com.firebasecloud.databinding.ActivityAcceptedOfferBinding;

public class AcceptedOffer extends AppCompatActivity {
    ActivityAcceptedOfferBinding binding;
    private MediaPlayer mp;
    TextView tv_title, tv_incentive, tv_expireDate;
    ObjectAnimator rotate;
    private static final int SWIPE_MIN_DISTANCE = 120;
    float imageViewPosition;

    float x1, x2, callingXPos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                + WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                +WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |

                +WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);


        binding = DataBindingUtil.setContentView(this, R.layout.activity_accepted_offer);

        imageViewPosition = binding.imCall.getX();
        tv_title = findViewById(R.id.tv_newOffer);
        tv_incentive = findViewById(R.id.tv_rate);
        tv_expireDate = findViewById(R.id.tv_expire);

        try {
            tv_title.setText(FirebaseMessangingService.messageObject.getString("title"));
            tv_incentive.setText("₹" + FirebaseMessangingService.messageObject.getString("incentive") + "/-");
            tv_expireDate.setText(NewTasks.getDate(FirebaseMessangingService.messageObject.getString("taskExpires"), false));


        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }


        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        mp = MediaPlayer.create(this, defaultSoundUri);
        mp.start();


        vibrateAnimation(binding.imCall);
        binding.imCall.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    x1 = event.getX();
                    binding.imCall.animate().alpha(0);

                    animateAccept();
                    animateReject();


                }
                if (event.getAction() == MotionEvent.ACTION_UP) {

                    binding.imCall.animate().alpha(1);


                    deanimateAccept();
                    deanimateReject();
                    binding.imPick.setVisibility(View.INVISIBLE);
                    binding.imCancel.setVisibility(View.INVISIBLE);

                    x2 = event.getX();

                    float deltaX = x2 - x1;

                    if (Math.abs(deltaX) > SWIPE_MIN_DISTANCE) {
                        // Left to Right swipe action
                        if (x2 > x1) {


                            rotate.end();

                            Intent intent = new Intent(AcceptedOffer.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();

                            //  Toast.makeText(AcceptedOffer.this, "Left to Right swipe [Next]", Toast.LENGTH_SHORT).show();
                        }

                        // Right to left swipe action
                        else {
                            rotate.end();
//                            rotate.cancel();
                            finish();
                        }

                    } else {

                    }

                }

                return true;
            }

        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mp.stop();
        mp.release();

    }

    public void vibrateAnimation(final ImageView view) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                rotate = ObjectAnimator.ofFloat(view, "rotation", 0f, 20f, 0f, -20f, 0f); // rotate o degree then 20 degree and so on for one loop of rotation.

                rotate.setRepeatCount(200);


                rotate.setDuration(250); // animation play time 100 ms
                rotate.start();
            }
        });


    }

    private void deanimateReject() {
        binding.imCancel.animate().translationX(imageViewPosition).setDuration(600).setInterpolator(new OvershootInterpolator());

    }

    private void deanimateAccept() {
        binding.imPick.animate().translationX(imageViewPosition).setDuration(600).setInterpolator(new OvershootInterpolator());

    }

    private void animateReject() {
        binding.imCancel.setVisibility(View.VISIBLE);

        binding.imCancel.animate().translationX(-250).setDuration(600).setInterpolator(new OvershootInterpolator());

    }

    private void animateAccept() {
        binding.imPick.setVisibility(View.VISIBLE);

        binding.imPick.animate().translationX(250).setDuration(600).setInterpolator(new OvershootInterpolator());

    }


}
