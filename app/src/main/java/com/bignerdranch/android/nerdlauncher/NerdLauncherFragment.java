package com.bignerdranch.android.nerdlauncher;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NerdLauncherFragment extends Fragment
{
    private static final String TAG = "NERD_LAUNCHER_FRAGMENT";

    private RecyclerView mRecyclerView;

    private class ActivityHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private ResolveInfo mResolveInfo;
        private TextView mNameTextView;
        private ImageView mIcon;

        public ActivityHolder(View itemView)
        {
            super(itemView);

            mNameTextView = itemView.findViewById(R.id.app_name);
            mIcon = itemView.findViewById(R.id.app_icon);

            itemView.setOnClickListener(this);
        }

        public void bindActivity(ResolveInfo resolveInfo)
        {
            mResolveInfo = resolveInfo;

            PackageManager pm = getActivity().getPackageManager();
            String appName = mResolveInfo.loadLabel(pm).toString();
            Drawable appIcon = mResolveInfo.loadIcon(pm);

            mNameTextView.setText(appName);
            mIcon.setImageDrawable(appIcon);
        }

        @Override
        public void onClick(View view)
        {
            ActivityInfo activityInfo = mResolveInfo.activityInfo;

            Intent i = new Intent(Intent.ACTION_MAIN)
                    .setClassName(activityInfo.applicationInfo.packageName, activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(i);
        }
    }

    private class ActivityAdapter extends RecyclerView.Adapter<ActivityHolder>
    {
        private final List<ResolveInfo> mActivities;

        public ActivityAdapter(List<ResolveInfo> activities)
        {
            mActivities = activities;
        }

        @Override
        public ActivityHolder onCreateViewHolder(ViewGroup parent, int viewType)
        {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());

            View view = layoutInflater
                    .inflate(R.layout.list_item_app, parent, false);

            return new ActivityHolder(view);
        }

        @Override
        public void onBindViewHolder(ActivityHolder holder, int position)
        {
            ResolveInfo resolveInfo = mActivities.get(position);
            holder.bindActivity(resolveInfo);
        }

        @Override public int getItemCount()
        {
            return mActivities.size();
        }
    }

    public static Fragment newInstance()
    {
        return new NerdLauncherFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_nerd_launcher, container, false);

        mRecyclerView = v.findViewById(R.id.app_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setUpAdapter();
        return v;
    }

    private void setUpAdapter()
    {
        Intent startupIntent = new Intent(Intent.ACTION_MAIN);
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(startupIntent, 0);

        Collections.sort(activities, new Comparator<ResolveInfo>() {
            public int compare(ResolveInfo a, ResolveInfo b)
            {
                PackageManager pm = getActivity().getPackageManager();
                return String.CASE_INSENSITIVE_ORDER
                        .compare(a.loadLabel(pm).toString(), b.loadLabel(pm).toString());
            }
        });

        Log.i(TAG, "Found " + activities.size() + " activities.");

        mRecyclerView.setAdapter(new ActivityAdapter(activities));
    }
}
