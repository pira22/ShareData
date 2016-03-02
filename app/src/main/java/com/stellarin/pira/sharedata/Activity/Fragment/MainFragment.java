package com.stellarin.pira.sharedata.Activity.Fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.stellarin.pira.sharedata.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import fi.iki.elonen.NanoHTTPD;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment implements View.OnClickListener{

    private static final int FILE_CODE = 105;
    private RelativeLayout send;
    private RelativeLayout receive;
    private ImageView qrcodeimg;
    private OnFragmentInteractionListener mListener;
    private static final int PORT = 8080;
    private MyHTTPD server;
    String formatedIpAddress;
    byte[] buffer = new byte[1024];
    ArrayList<File> files = new ArrayList<File>();
    private Handler handler = new Handler();
    ByteArrayOutputStream byteArrayOutputStream;
    Dialog openDialog ;
    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    public MainFragment() {
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
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        send = (RelativeLayout) view.   findViewById(R.id.send);
        receive = (RelativeLayout) view.findViewById(R.id.receive);
        qrcodeimg = (ImageView) view.findViewById(R.id.qrcodeimg);
        send.setOnClickListener(this);
        receive.setOnClickListener(this);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if( i == KeyEvent.KEYCODE_BACK )
                {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            send.setVisibility(View.VISIBLE);
                            receive.setVisibility(View.VISIBLE);
                            qrcodeimg.setVisibility(View.GONE);
                            try {
                                byteArrayOutputStream.reset();
                                files.clear();
                            }catch (NullPointerException e){

                            }

                        }
                    });
                    return true;
                }
                return false;
            }
        });
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

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(getActivity().WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));


        try {
            server = new MyHTTPD();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (server != null)
            server.stop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send:
                    fileChooser();
                break;
            case R.id.receive:
                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.initiateScan();
                break;
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


    public void fileChooser() {
        Intent i = new Intent(getActivity(), FilePickerActivity.class);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
        startActivityForResult(i, FILE_CODE);
    }

    public void setQrcode(String path) throws WriterException {
        MultiFormatWriter writer = new MultiFormatWriter();
        String finaldata = Uri.encode(path, "utf-8");
        BitMatrix bm = writer.encode(path, BarcodeFormat.QR_CODE, 800, 800);
        final Bitmap ImageBitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < 800; i++) {
            for (int j = 0; j < 800; j++) {
                ImageBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
            }
        }
        if (ImageBitmap != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    send.setVisibility(View.GONE);
                    receive.setVisibility(View.GONE);
                    qrcodeimg.setImageBitmap(ImageBitmap);
                    qrcodeimg.setVisibility(View.VISIBLE);
                    openDialog.dismiss();
                }
            });
        } else {
            Toast.makeText(getActivity(), "erreur vide",
                    Toast.LENGTH_SHORT).show();
        }
    }


    public InputStream createZipByteArray(ArrayList<File> files) throws IOException {
         byteArrayOutputStream = new ByteArrayOutputStream();
        try {

            ZipOutputStream fos = new ZipOutputStream(byteArrayOutputStream);
            fos.setMethod(ZipOutputStream.DEFLATED);
            fos.setLevel(9);
            for (int i = 0; i < files.size(); i++) {
                FileInputStream in = new FileInputStream(files.get(i));
                BufferedInputStream buffi = new BufferedInputStream(in, 1024);
                ZipEntry ze = new ZipEntry(files.get(i).getName());
                Log.d("files names",ze.getName());
                fos.putNextEntry(ze);
                int len;
                while ((len = buffi.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.closeEntry();
                buffi.close();
                in.close();

            }
            //remember close it
            fos.close();
            System.out.println("Done");

        } catch (IOException ex) {
            Log.d("erreur zip method", ex.getMessage());
        }

        InputStream is = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        return is;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String path;

        switch (requestCode) {
            case FILE_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    openDialog = new Dialog(getActivity());
                    openDialog.setContentView(R.layout.customdialog_layout);
                    openDialog.setTitle("Loading...");
                    openDialog.show();
                    if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
                        // For JellyBean and above
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            ClipData clip = data.getClipData();

                            if (clip != null) {
                                for (int i = 0; i < clip.getItemCount(); i++) {
                                    Uri uri = clip.getItemAt(i).getUri();
                                    Log.d("item_uri", uri.getPath());
                                    path = uri.getPath();
                                    files.add(new File(path));
                                }
                                try {
                                    createZipByteArray(files);
                                    server.start();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    setQrcode("http://" + formatedIpAddress + ":" + PORT);
                                } catch (WriterException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            ArrayList<String> paths = data.getStringArrayListExtra
                                    (FilePickerActivity.EXTRA_PATHS);

                            if (paths != null) {
                                for (String uripath : paths) {
                                    Uri uri = Uri.parse(uripath);
                                    Log.d("item_uri", uri.getPath().toString());
                                    files.add(new File(uri.getPath()));
                                }
                                try {

                                    createZipByteArray(files);
                                    server.start();
                                } catch (IOException e) {
                                    Log.d("erreur zip", e.getMessage());
                                }
                                try {
                                    setQrcode("http://" + formatedIpAddress + ":" + PORT);
                                } catch (WriterException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } else {
                        Uri uri = data.getData();
                        Log.d("item_uri", uri.getPath().toString());
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class MyHTTPD extends NanoHTTPD {
        public MyHTTPD() throws IOException {
            super(PORT);
        }

        @Override
        public Response serve(IHTTPSession session) {
            final StringBuilder buf = new StringBuilder();
            Map<String, String> parms = session.getParms();
            for (Map.Entry<String, String> kv : parms.entrySet())
                buf.append(kv.getKey() + " : " + kv.getValue() + "\n");



            Response response = null;

            try {
                response = new Response(Response.Status.OK, "application/zip", createZipByteArray(files));
            } catch (IOException e) {
                e.printStackTrace();
            }

            response.addHeader("Content-Disposition: attachment; filename=", "test.zip");
            return response;
        }

    }
}
