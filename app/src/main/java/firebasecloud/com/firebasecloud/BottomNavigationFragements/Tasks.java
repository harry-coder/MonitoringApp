package firebasecloud.com.firebasecloud.BottomNavigationFragements;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import firebasecloud.com.firebasecloud.ActiveTaskFragements.Complete;
import firebasecloud.com.firebasecloud.ActiveTaskFragements.OnGoing;
import firebasecloud.com.firebasecloud.ActiveTaskFragements.Pending;
import firebasecloud.com.firebasecloud.R;
import firebasecloud.com.firebasecloud.TaskFragments.AcceptedTasks;
import firebasecloud.com.firebasecloud.TaskFragments.NewTasks;
import firebasecloud.com.firebasecloud.TaskFragments.RejectedTasks;


public class Tasks extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    ViewPager pager;
    private TabLayout tabs;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public Tasks() {
        // Required empty public constructor
    }

    public static Tasks newInstance(String param1, String param2) {
        Tasks fragment = new Tasks();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        pager = view.findViewById(R.id.pager);
        pager.setAdapter(new myAdapter(getActivity().getSupportFragmentManager()));
        //  pager.setPageTransformer(true, new ZoomOutSlideTransformer());
        tabs = view.findViewById(R.id.tablayout);
        tabs.setupWithViewPager(pager);


        return view;
    }

    class myAdapter extends FragmentStatePagerAdapter {


        public myAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return NewTasks.newInstance("", "");

            } else if (position == 1)
                return AcceptedTasks.newInstance("", "");

            else if (position == 2)
                return RejectedTasks.newInstance("", "");

            else return null;


        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {

            if (position == 0) {
                return "New";
            } else if (position == 1) {
                return "Accepted ";
            } else if (position == 2) {
                return "Rejected";
            } else return null;


        }


    }
}
