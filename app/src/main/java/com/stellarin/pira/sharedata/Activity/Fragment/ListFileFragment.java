package com.stellarin.pira.sharedata.Activity.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.stellarin.pira.sharedata.Activity.Adapter.GridAdapter;
import com.stellarin.pira.sharedata.R;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ListFileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ListFileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListFileFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    GridView gridView;
    MediaPlayer mediaPlayer;
    List<String> files = new ArrayList<String>();
    List<String> fileextension  = new ArrayList<String>();
    ArrayList<String> song = new ArrayList<String>();

    // TODO: Rename and change types and number of parameters
    public static ListFileFragment newInstance() {
        ListFileFragment fragment = new ListFileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ListFileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_file, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_BACK) {
                    FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                    fragmentTransaction.add(R.id.content, MainFragment.newInstance(), MainFragment.class.getName());
                    fragmentTransaction.addToBackStack(MainFragment.class.getName());
                    fragmentTransaction.commit();
                    return true;
                }
                return false;
            }
        });
        try {
            getZipFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator +"test.zip");
        } catch (IOException e) {
            e.printStackTrace();
        }
        gridView = (GridView) view.findViewById(R.id.gridView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

               /* Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + parent.getAdapter().getItem(position));
                intent.setDataAndType(Uri.fromFile(file), "audio/*");
                intent.setPackage("com.google.android.music");
                startActivity(intent);*/
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + parent.getAdapter().getItem(position)), "image/*");
                startActivity(intent);
            }
        });
        GridAdapter gridAdapter = new GridAdapter(getActivity(),R.layout.item_grid,files,fileextension);
        gridView.setAdapter(gridAdapter);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }
    public void getZipFile(String path) throws IOException {
        ZipFile zipFile = new ZipFile(path);

        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String entryName = entry.getName();
            if(FilenameUtils.getExtension(entryName).equals("mp3"))
                song.add(entryName);
            fileextension.add(FilenameUtils.getExtension(entryName));
            files.add(entryName);
        }
    }

}
