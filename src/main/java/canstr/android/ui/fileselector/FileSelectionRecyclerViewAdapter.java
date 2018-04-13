/*
 * Copyright (c) 2017. Sipke Vriend
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of canstr nor the names of its contributors may be used
 *     to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package canstr.android.ui.fileselector;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import canstr.android.ui.fileselector.FileSelection.FileSelectionContent;

/**
 * {@link RecyclerView.Adapter} that can display a {@link FileSelectionContent} and makes a call to the
 * specified {@link EditableFileSelectorFragment.OnFileSelectionListener}.
 */
public class FileSelectionRecyclerViewAdapter extends RecyclerView.Adapter<FileSelectionRecyclerViewAdapter.ViewHolder> {

    private SparseBooleanArray selectedItems;
    private List<FileSelectionContent> mValues;
    private final EditableFileSelectorFragment.OnFileSelectionListener mListener;

    public FileSelectionRecyclerViewAdapter(List<FileSelectionContent> items, EditableFileSelectorFragment.OnFileSelectionListener listener) {
        mValues = items;
        mListener = listener;
        selectedItems = new SparseBooleanArray();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_fileitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        holder.mContentView.setText(mValues.get(position).content);
        holder.mSizeView.setText(String.valueOf(mValues.get(position).size));
        holder.itemView.setActivated(selectedItems.get(position, false));


        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that a single item has been selected and
                // the guidance file effectively has been chosen.
                if (!mMultiSelectActive) {
                    if (toggleSingleSelection(position)) {
                        if ((mListener != null)) {
                            mListener.onItemSelection(holder.mItem);
                        }
                    }
                } else {
                    toggleSelection(position);
                }
            }
        });

        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mListener != null) {
                    mListener.onPlaybackSelection(holder.mItem);
                }
                // Pass on control to some other view.
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public FileSelectionContent getItem(int position) {
        FileSelectionContent value = null;
        if ((position >= 0) && (mValues.size() > 0)) {
            return mValues.get(position);
        }
        return value;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public final TextView mSizeView;
        public FileSelectionContent mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.content);
            mSizeView = (TextView) view.findViewById(R.id.file_size);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }

    public boolean toggleSelection(int pos) {
        boolean selected = false;
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        }
        else {
            selected = true;
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
        return selected;
    }

    private boolean toggleSingleSelection(int pos) {
        boolean selected = false;
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
            notifyItemChanged(pos);
        } else {
            selected = true;
            selectedItems.clear();
            selectedItems.put(pos, true);
            notifyDataSetChanged();
        }
        return selected;
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    private List<FileSelectionContent> purgeSelectedValues() {
        List<Integer> items = getSelectedItems();
        List<FileSelectionContent> values = new ArrayList<FileSelectionContent>();
        int count = items.size();
        for (int i = 0; i < count; i++) {
            int position = items.get(i);
            FileSelectionContent value = mValues.get(position);
            values.add(value);
        }
        for (int i = 0; i < count; i++) {
            FileSelectionContent value = values.get(i);
            mValues.remove(value);
        }
        notifyDataSetChanged();
        return values;
    }

    /**
     * Removes the items that currently selected
     */
    public void removeSelected() {
        List<FileSelectionContent> purged = purgeSelectedValues();
        mListener.onItemsRemoved(purged);
        mMultiSelectActive = false;
    }

    /**
     * Get the top most selected item.
     * @return
     */
    public FileSelectionContent getSelected() {
        List<Integer> items = getSelectedItems();
        FileSelectionContent content = null;
        if (items.size() > 0) {
            int position = items.get(0);
            content = mValues.get(position);
        }
        return content;
    }

    public void rename(FileSelectionContent item, String newName) {
        int count = mValues.size();
        for (int i = 0; i < count; i++){
            FileSelectionContent value = mValues.get(i);
            if (value == item) {
                value.rename(newName);
                notifyItemChanged(i);
                mListener.onItemRename(item, newName);
                break;
            }
        }
    }

    public void setSingleSelectionMode() {
        mMultiSelectActive = false;
    }

    public void setMultiSelectionMode() {
        clearSelections();
        mMultiSelectActive = true;
    }

    public boolean isSingleSelectMode() {
        return (mMultiSelectActive == false);
    }

    private boolean mMultiSelectActive = false;
}
