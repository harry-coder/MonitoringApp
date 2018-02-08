package firebasecloud.com.firebasecloud;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import io.paperdb.Paper;

/**
 * Created by Harpreet on 15/11/2017.
 */

public class FcmInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();


        String tokenRefereshed= FirebaseInstanceId.getInstance().getToken();

        if (tokenRefereshed != null) {
            Paper.book().write("token",tokenRefereshed);
        }

    }
}
