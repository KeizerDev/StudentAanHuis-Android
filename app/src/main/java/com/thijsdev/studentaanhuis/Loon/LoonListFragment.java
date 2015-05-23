package com.thijsdev.studentaanhuis.Loon;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.thijsdev.studentaanhuis.Callback;
import com.thijsdev.studentaanhuis.DividerItemDecoration;
import com.thijsdev.studentaanhuis.FragmentInterface;
import com.thijsdev.studentaanhuis.MainActivity;
import com.thijsdev.studentaanhuis.Prikbord.PrikbordHelper;
import com.thijsdev.studentaanhuis.Prikbord.PrikbordItem;
import com.thijsdev.studentaanhuis.R;

public class LoonListFragment extends Fragment implements FragmentInterface {
    private PrikbordHelper prikbordHelper = new PrikbordHelper();

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MainActivity mainActivity;
    private Toolbar toolbar;

    public LoonAdapter mAdapter;

    private boolean isRefreshing = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loon_list, container, false);

        mainActivity = (MainActivity) view.getContext();
        toolbar = mainActivity.getToolbar();
        toolbar.getMenu().clear();
        toolbar.setTitle(getString(R.string.loon));
        toolbar.inflateMenu(R.menu.menu_loon);
        toolbar.setNavigationIcon(null);

        registerToolbarClick();

        if(mainActivity.getSharedObject("loonAdapter") == null) {
            mAdapter = (LoonAdapter) mainActivity.addSharedObject("loonAdapter", new LoonAdapter(mainActivity));
        }else{
            mAdapter = (LoonAdapter) mainActivity.getSharedObject("loonAdapter");
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.loonList);
        mRecyclerView.setHasFixedSize(false);

        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL_LIST));

        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onStart() {
        mainActivity.setupActionBar();

        if(mAdapter.getItemCount() == 0) {
            updateLoon();
        }

        super.onStart();
    }

    public void updateLoon() {
        if (!isRefreshing) {
            isRefreshing = true;
            Animation a = AnimationUtils.loadAnimation(mainActivity, R.anim.rotate);
            a.setRepeatCount(Animation.INFINITE);
            toolbar.findViewById(R.id.action_refresh).startAnimation(a);

            //TODO: mAdapter.clearItems();
            prikbordHelper.updatePrikbordItems(mainActivity, new Callback() {
                @Override
                public void onTaskCompleted(Object... results) {
                    PrikbordItem pi = (PrikbordItem) results[0];
                    if (!mAdapter.hasItem(pi)) {
                        int noItemsFound = mAdapter.findItem(pi.getBeschikbaar() + 3);
                        if (noItemsFound > -1)
                            mAdapter.removeItem(noItemsFound);

                        mAdapter.addItem(mAdapter.findItem(pi.getBeschikbaar()) + 1, pi);
                    }
                }
            }, new Callback() {
                @Override
                public void onTaskCompleted(Object... results) {
                    PrikbordItem pi = (PrikbordItem) results[0];
                    if (!mAdapter.hasItem(pi)) {
                        int noItemsFound = mAdapter.findItem(pi.getBeschikbaar() + 3);
                        if (noItemsFound > -1)
                            mAdapter.removeItem(noItemsFound);

                        mAdapter.addItem(mAdapter.findItem(pi.getBeschikbaar()) + 1, pi);
                    }
                }
            }, new Callback() {
                @Override
                public void onTaskCompleted(Object... results) {
                    if (toolbar.findViewById(R.id.action_refresh) != null)
                        toolbar.findViewById(R.id.action_refresh).clearAnimation();
                    isRefreshing = false;
                }
            });
        }
    }

    @Override
    public void unload() {
        toolbar.findViewById(R.id.action_refresh).clearAnimation();
        toolbar.removeView(toolbar.findViewById(R.id.action_refresh));
    }

    public void registerToolbarClick() {
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_refresh:
                        updateLoon();
                        return true;
                }

                return false;
            }
        });
    }

    @Override
    public int getDrawerId() {
        return R.id.menu_loon;
    }

    @Override
    public String getTitle() {
        return getResources().getString(R.string.loon);
    }
}
