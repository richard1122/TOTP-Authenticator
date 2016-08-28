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

import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.hlyue.totpauthenticator.R;
import com.hlyue.totpauthenticator.models.AuthInstance;
import com.hlyue.totpauthenticator.models.AuthUtils;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AuthListAdapter extends RecyclerView.Adapter<AuthListAdapter.AuthItemVH> implements Closeable, ValueEventListener {
    private List<AuthInstance> mList;
    private DatabaseReference mReference;

    public AuthListAdapter() {
    }

    public void init(final DatabaseReference reference) {
        mReference = reference;
        mReference.addValueEventListener(this);
    }

    @Override
    public AuthItemVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AuthItemVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.auth_instance_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final AuthItemVH holder, int position) {
        AuthInstance authInstance = mList.get(position);
        holder.issuer.setText(authInstance.issuer);
        holder.timer.setText(String.format(Locale.US, "%06d", AuthUtils.calculateTOTP(authInstance)));
        holder.path.setText(authInstance.path);
        holder.itemView.setOnClickListener(v -> {
            ClipboardManager manager = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            manager.setPrimaryClip(ClipData.newPlainText("totp", holder.timer.getText()));
            Toast.makeText(v.getContext(), "two factor authentication code copied", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public void close() throws IOException {
        if (mReference != null) mReference.removeEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        mList = Lists.newArrayList(Collections2.transform(dataSnapshot.getValue(new GenericTypeIndicator<Map<String, Map<String, String>>>() {
        }).values(), AuthInstance::getInstance));
        notifyDataSetChanged();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }

    static class AuthItemVH extends RecyclerView.ViewHolder {
        TextView issuer, timer, path;

        AuthItemVH(View itemView) {
            super(itemView);
            this.issuer = (TextView) itemView.findViewById(R.id.auth_item_issuer);
            this.timer = (TextView) itemView.findViewById(R.id.auth_item_timer);
            this.path = (TextView) itemView.findViewById(R.id.auth_item_path);
        }
    }
}
