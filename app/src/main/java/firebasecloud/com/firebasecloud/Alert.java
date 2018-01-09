package firebasecloud.com.firebasecloud;

import android.content.Context;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Harpreet on 26/12/2017.
 */

public class Alert {

    public static SweetAlertDialog showAlertDialog(String msg, Context context) {

        SweetAlertDialog dialog = new SweetAlertDialog(context);
        dialog.setTitleText(msg)
                .show();


        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

}
