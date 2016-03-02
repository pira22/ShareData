package com.stellarin.pira.sharedata.Activity.Fragment;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.jlmd.animatedcircleloadingview.AnimatedCircleLoadingView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.stellarin.pira.sharedata.Activity.Utils.CustomAnimation;
import com.stellarin.pira.sharedata.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import is.arontibo.library.ElasticDownloadView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ReceiveFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ReceiveFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReceiveFragment extends Fragment {
    File currentRootDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotifyManager;
    byte[] buffer = new byte[1024];
    int id = 1;
    private OnFragmentInteractionListener mListener;
    private String[] mAllowedContentTypes = new String[]{
            "application/zip",
            "image/jpeg",
            "image/jpg"
    };
    CustomAnimation elasticDownloadView;
    private  String  scanString ;
    public static ReceiveFragment newInstance(String scanContent) {
        ReceiveFragment fragment = new ReceiveFragment();
        Bundle args = new Bundle();
        args.putString("codeqr",scanContent);
        fragment.setArguments(args);
        return fragment;
    }

    public ReceiveFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            scanString = getArguments().getString("codeqr");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_receive, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        elasticDownloadView = (CustomAnimation) view.findViewById(R.id.elastic_download_view);
        elasticDownloadView.startIntro();
        Log.d("test", scanString + "");
        DonwloadFile(scanString);
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

    private void DonwloadFile(String url) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new BinaryHttpResponseHandler(mAllowedContentTypes) {
            @Override
            public void onPreProcessResponse(ResponseHandlerInterface instance, HttpResponse response) {
                super.onPreProcessResponse(instance, response);

                        mNotifyManager =
                                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                        mBuilder = new NotificationCompat.Builder(getActivity());
                        mBuilder.setContentTitle("File Download")
                                .setContentText("Download in progress")
                                .setSmallIcon(R.drawable.down);

                     }

                     @Override
                     public void onProgress(long bytesWritten, long totalSize) {
                         super.onProgress(bytesWritten, totalSize);
                         final double percent = (totalSize > 0) ? (bytesWritten * 1.0 / totalSize) * 100 : -1;
                                 elasticDownloadView.setProgress((float) percent);
                                 Log.d("progress", percent + "%");
                                 mBuilder.setProgress(100, (int) percent, false);
                                 mNotifyManager.notify(id, mBuilder.build());
                     }

                     @Override
                     public void onSuccess(int statusCode, Header[] headers, byte[] binaryData) {
                         mBuilder.setContentText("Download complete")
                                 .setProgress(0, 0, true);
                         try {
                             OutputStream f = new FileOutputStream(new File(currentRootDirectory, "test.zip"));
                             Log.d("binary", statusCode + "%");
                             f.write(binaryData);
                             f.flush();
                             f.close();
                             unZipIt(currentRootDirectory.getAbsolutePath() + File.separator + "test.zip", currentRootDirectory.getAbsolutePath());
                         } catch (IOException e) {
                             Log.d("erreur IOException", e.getMessage());
                         }
                         mBuilder.setProgress(0, 0, false);
                         mNotifyManager.notify(id, mBuilder.build());
                         elasticDownloadView.success();
                         final View coordinatorLayoutView = getActivity().findViewById(R.id.snackbarPosition);
                         Snackbar
                                 .make(coordinatorLayoutView, "View File", Snackbar.LENGTH_INDEFINITE)
                                 .setAction("View", new View.OnClickListener() {
                                     @Override
                                     public void onClick(View v) {
                                         FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                                         fragmentTransaction.replace(R.id.content, ListFileFragment.newInstance(), ListFileFragment.class.getName())
                                                 .addToBackStack(ListFileFragment.class.getName())
                                                 .commit();
                                     }
                                 })
                                 .show(); // Don’t forget to show!

                     }

                     @Override
                     public void onPostProcessResponse(ResponseHandlerInterface instance, HttpResponse response) {
                         super.onPostProcessResponse(instance, response);


                     }

                     @Override
                     public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error) {
                         try {
                             if (error != null)
                                 Log.d("erreur", error.getMessage());
                             elasticDownloadView.fail();
                             final View coordinatorLayoutView = getActivity().findViewById(R.id.snackbarPosition);
                             Snackbar
                                     .make(coordinatorLayoutView, error.getMessage(), Snackbar.LENGTH_LONG)
                                     .show(); // Don’t forget to show!
                         } catch (RuntimeException ex) {
                             mBuilder.setContentText("failed download")
                                     // Removes the progress bar
                                     .setProgress(0, 0, false);
                             mNotifyManager.notify(id, mBuilder.build());
                         }
                     }
                 });
    }

    public void unZipIt(String zipFile, String outputFolder) {

        try {
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                System.out.println("file unzip : " + newFile.getAbsoluteFile());
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                Log.d("deeded", ze.getName());
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
