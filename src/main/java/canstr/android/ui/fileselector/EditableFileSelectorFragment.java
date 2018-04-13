package canstr.android.ui.fileselector;

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
/**
 *
 * TODO: Add a default text if no files found or if file permisisons are not set so no files
 * are listed in this fragment. Just so user knows what the story is.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import canstr.android.ui.fileselector.FileSelection.FileSelectionContent;

/**
 * A fragment representing a list of files in a folder.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFileSelectionListener}
 * interface.
 */
public class EditableFileSelectorFragment extends Fragment implements RecyclerView.OnItemTouchListener,
        View.OnClickListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 1;
    private OnFileSelectionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EditableFileSelectorFragment() {
    }

    @SuppressWarnings("unused")
    public static EditableFileSelectorFragment newInstance(int columnCount) {
        EditableFileSelectorFragment fragment = new EditableFileSelectorFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gestureDetector =
                new GestureDetectorCompat(this.getContext(), new RecyclerViewDemoOnGestureListener());
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fileselection, container, false);

        setHasOptionsMenu(true);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            mAdapter = new FileSelectionRecyclerViewAdapter(FileSelection.ITEMS, mListener);
            recyclerView.setAdapter(mAdapter);
            recyclerView.addOnItemTouchListener(this);
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fileselection_actions, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        MenuItem itemDelete = menu.findItem(R.id.menu_delete);
        MenuItem itemRename = menu.findItem(R.id.menu_rename);
        MenuItem itemEdit = menu.findItem(R.id.menu_edit);
        MenuItem itemFileSelection = menu.findItem(R.id.menu_fileselection);

        // mAdapter is created in oncreateview, so no null check needed.
        if (mAdapter.isSingleSelectMode()) {
            itemEdit.setVisible(true);
            itemFileSelection.setVisible(false);
        } else {
            itemEdit.setVisible(false);
            itemFileSelection.setVisible(true);
            int count = mAdapter.getSelectedItemCount();
            if (count == 0) {
                itemDelete.setVisible(false);
                itemRename.setVisible(false);
            } else if (count == 1) {
                itemDelete.setVisible(true);
                itemRename.setVisible(true);
            } else if (count > 1) {
                itemDelete.setVisible(true);
                itemRename.setVisible(false);
            }
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Adapter is created in onCreateView, not need to check for null.
        int count = mAdapter.getSelectedItemCount();
        int id = item.getItemId();
        if (id == R.id.menu_fileselection) {
            setSelectMode();
        } else if (id == R.id.menu_edit) {
            setEditMode();
        } else if (id == R.id.menu_delete){
            if (count == 0) {
                Toast.makeText(this.getActivity(), R.string.fileselection_select_single_item, Toast.LENGTH_LONG).show();
            } else {
                mAdapter.removeSelected();
                setSelectMode();
            }
        } else if (id == R.id.menu_rename) {
            if (count == 1) {
                FileSelectionContent content = mAdapter.getSelected();
                updateFileNameDialog(content);
            } else {
                Toast.makeText(this.getActivity(), R.string.fileselection_select_an_item_to_delete, Toast.LENGTH_LONG).show();
            }
        } else {
                setSelectMode();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFileSelectionListener) {
            mListener = (OnFileSelectionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public FileSelectionRecyclerViewAdapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void onClick(View view) {
        // If an item is click invalide the options menu as likely the action icons need to
        // change and the menu adjusted.
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean b) {

    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }


    private class RecyclerViewDemoOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        // We are not using this at the moment, but it could be useful in future to detect long
        // press on the fragment, so leave this code in place for now.
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            onClick(view);
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            int idx = recyclerView.getChildPosition(view);
            super.onLongPress(e);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFileSelectionListener {
        void onItemSelection(FileSelectionContent item);
        void onItemsRemoved(List<FileSelectionContent> items);
        void onItemRename(FileSelectionContent item, String newName);
        void onPlaybackSelection(FileSelectionContent item);
    }

    private void setEditMode() {
        mAdapter.setMultiSelectionMode();
        getActivity().invalidateOptionsMenu();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.select_and_edit);
    }

    private void setSelectMode() {
        mAdapter.clearSelections();
        mAdapter.setSingleSelectionMode();
        getActivity().invalidateOptionsMenu();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setTitle(R.string.select_file);
    }

    private void updateFileNameDialog(FileSelection.FileSelectionContent content) {
        if (content != null) {
            final FileSelectionContent intcontent = content;
            final EditText txt = new EditText(this.getContext());
            final File originalFile = content.file;
            txt.setText(originalFile.getName());
            new AlertDialog.Builder(this.getContext())
                    .setTitle(R.string.dialog_rename_file)
                    .setView(txt)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String newName = txt.getText().toString();
                            String originalName = originalFile.getName().toString();
                            int comparison = newName.compareTo(originalName);
                            if (comparison != 0) {
                                mAdapter.rename(intcontent, newName);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    })

                    .show();
        }
    }


    private FileSelectionRecyclerViewAdapter mAdapter;
    private RecyclerView recyclerView;
    private GestureDetectorCompat gestureDetector;
}
