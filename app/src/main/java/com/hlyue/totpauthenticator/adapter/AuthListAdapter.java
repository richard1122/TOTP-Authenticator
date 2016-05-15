package com.hlyue.totpauthenticator.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.hlyue.totpauthenticator.MyApplication;
import com.hlyue.totpauthenticator.R;
import com.hlyue.totpauthenticator.models.AuthInstance;
import com.hlyue.totpauthenticator.models.AuthUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AuthListAdapter extends RecyclerView.Adapter<AuthListAdapter.AuthItemVH> implements Closeable, SharedPreferences.OnSharedPreferenceChangeListener {
    private final List<AuthInstance> mList = new ArrayList<>();

    public AuthListAdapter() {
        MyApplication.getTotpPreference().registerOnSharedPreferenceChangeListener(this);
        readList();
    }

    private void readList() {
        for (String key : MyApplication.getTotpPreference().getAll().keySet()) {
            mList.add(AuthInstance.getInstance(key));
        }
        notifyDataSetChanged();
    }

    @Override
    public AuthItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AuthItemVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.auth_instance_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final AuthItemVH holder, int position) {
        AuthInstance authInstance = mList.get(position);
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
        return mList.size();
    }

    @Override
    public void close() throws IOException {
        MyApplication.getTotpPreference().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mList.clear();
        readList();
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
