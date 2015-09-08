package com.hlyue.totpauthenticator.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hlyue.totpauthenticator.R;
import com.hlyue.totpauthenticator.models.AuthInstance;
import com.hlyue.totpauthenticator.models.AuthUtils;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

/**
 * Created by v-linyhe on 9/6/2015.
 */
public class AuthListAdapter extends RecyclerView.Adapter<AuthListAdapter.AuthItemVH> {
    RealmResults<AuthInstance> mAuthInstances;

    public AuthListAdapter() {
        mAuthInstances = Realm.getDefaultInstance().allObjects(AuthInstance.class);
        Realm.getDefaultInstance().addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public AuthItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AuthItemVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.auth_instance_item, parent, false));
    }

    @Override
    public void onBindViewHolder(AuthItemVH holder, int position) {
        AuthInstance authInstance = mAuthInstances.get(position);
        holder.issuer.setText(authInstance.getIssuer());
        holder.timer.setText(String.format("%06d", AuthUtils.calculateTOTP(authInstance)));
        holder.path.setText(authInstance.getPath());
    }

    @Override
    public int getItemCount() {
        return mAuthInstances.size();
    }

    static class AuthItemVH extends RecyclerView.ViewHolder {
        TextView issuer, timer, path;

        public AuthItemVH(View itemView) {
            super(itemView);
            this.issuer = (TextView) itemView.findViewById(R.id.auth_item_issuer);
            this.timer = (TextView) itemView.findViewById(R.id.auth_item_timer);
            this.path = (TextView) itemView.findViewById(R.id.auth_item_path);
        }
    }
}
