package firebasecloud.com.firebasecloud;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import io.paperdb.Paper;

public class IntroActivity extends AppCompatActivity {

    TextView tv_signIn, tv_signUp;
    boolean isUserLoggedOut=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if(Paper.book().exist("isUserLoggedOut")) {
             isUserLoggedOut = Paper.book().read("isUserLoggedOut");
            System.out.println("is "+ isUserLoggedOut);
        }
        if (Paper.book().exist("contact")&&!isUserLoggedOut) {

                setContentView(R.layout.splash_screen);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        startActivity(new Intent(IntroActivity.this, MainActivity.class));
                        overridePendingTransition(R.anim.activity_in, R.anim.avtivity_out);

                        finish();


                    }
                }, 1000);

        }
        else {
            setContentView(R.layout.activity_intro);
            tv_signIn = findViewById(R.id.tv_signin);
            tv_signUp = findViewById(R.id.tv_signup);

            tv_signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(IntroActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });
            tv_signUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(IntroActivity.this, Signup_Activity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
            });

        }



    }
}
