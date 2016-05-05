package com.hlyue.totpauthenticator.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hlyue.totpauthenticator.R;
import com.hlyue.totpauthenticator.models.AuthInstance;
import com.hlyue.totpauthenticator.models.AuthUtils;

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class AuthListAdapter extends RecyclerView.Adapter<AuthListAdapter.AuthItemVH> {
    RealmResults<AuthInstance> mAuthInstances;

    public AuthListAdapter() {
        init();
        Realm.getDefaultInstance().addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                init();
            }
        });
    }

    private void init() {
        Realm.getDefaultInstance().where(AuthInstance.class).findAllAsync().addChangeListener(new RealmChangeListener<RealmResults<AuthInstance>>() {
            @Override
            public void onChange(RealmResults<AuthInstance> element) {
                mAuthInstances = element;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public AuthItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AuthItemVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.auth_instance_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final AuthItemVH holder, int position) {
        AuthInstance authInstance = mAuthInstances.get(position);
        holder.issuer.setText(authInstance.getIssuer());
        holder.timer.setText(String.format(Locale.US, "%06d", AuthUtils.calculateTOTP(authInstance)));
        holder.path.setText(authInstance.getPath());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager manager = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setPrimaryClip(ClipData.newPlainText("totp", holder.timer.getText()));
                Toast.makeText(v.getContext(), "two factor authentication code copied", Toast.LENGTH_SHORT).show();
            }
        });
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
