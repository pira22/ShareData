package com.stellarin.pira.sharedata.Activity;

import android.app.Activity;
import android.app.NotificationManager;
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
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.stellarin.pira.sharedata.Activity.Fragment.ListFileFragment;
import com.stellarin.pira.sharedata.Activity.Fragment.MainFragment;
import com.stellarin.pira.sharedata.Activity.Fragment.ReceiveFragment;
import com.stellarin.pira.sharedata.Activity.Utils.FileUtils;
import com.stellarin.pira.sharedata.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpResponse;
import fi.iki.elonen.NanoHTTPD;

public class StartServer extends AppCompatActivity implements MainFragment.OnFragmentInteractionListener,
        ReceiveFragment.OnFragmentInteractionListener,ListFileFragment.OnFragmentInteractionListener {
    File currentRootDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    private static final int PORT = 8080;
    private MyHTTPD server;
    private Handler handler = new Handler();
    private static final int FILE_SELECT_CODE = 0, FILE_CODE = 105;
    String formatedIpAddress;
    String scanContent;
    String namefile;
    int id = 1;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotifyManager;
    byte[] buffer = new byte[1024];
    ArrayList<File> files = new ArrayList<File>();
    private String[] mAllowedContentTypes = new String[]{
            RequestParams.APPLICATION_OCTET_STREAM,
            "image/jpeg",
            "image/png",
            "image/gif",
            "application/zip"
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_server);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.content, MainFragment.newInstance(), MainFragment.class.getName());
        fragmentTransaction.addToBackStack(MainFragment.class.getName());
        fragmentTransaction.commit();
    }

    @Override
    protected void onResume() {
        super.onResume();


        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
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
    protected void onPause() {
        super.onPause();
        if (server != null)
            server.stop();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    private class MyHTTPD extends NanoHTTPD {
        public MyHTTPD() throws IOException {
            super(PORT);
        }

        @Override
        public Response serve(IHTTPSession session) {
            final StringBuilder buf = new StringBuilder();
            String filename = namefile;
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

    public void fileChooser() {
        Intent i = new Intent(getBaseContext(), FilePickerActivity.class);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
        i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
        i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.EXTRA_MODE);
        i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
        startActivityForResult(i, FILE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanningResult != null) {
            scanContent = scanningResult.getContents();
            Log.d("scan result",scanContent+"aakakakak");
            if (scanContent!=null) {
                String scanFormat = scanningResult.getFormatName();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content, ReceiveFragment.newInstance(scanContent));
                fragmentTransaction.commit();
            }

        }
       /* switch (requestCode) {
            case FILE_CODE:
                if (resultCode == Activity.RESULT_OK) {
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
                                    files.add(new File(path));
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
        }*/


    }


    public InputStream createZipByteArray(ArrayList<File> files) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {

            ZipOutputStream fos = new ZipOutputStream(byteArrayOutputStream);
            BufferedOutputStream buff = new BufferedOutputStream(fos);
            fos.setMethod(ZipOutputStream.DEFLATED);
            fos.setLevel(9);
            for (int i = 0; i < files.size(); i++) {
                FileInputStream in = new FileInputStream(files.get(i));
                BufferedInputStream buffi = new BufferedInputStream(in, 1024);
                ZipEntry ze = new ZipEntry(files.get(i).getName());
                fos.putNextEntry(ze);
                int len;
                while ((len = buffi.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.closeEntry();
                buffi.close();
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

    public void getZipFile(String path) throws IOException {
        ZipFile zipFile = new ZipFile(path);

        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String entryName = entry.getName();
            InputStream stream = zipFile.getInputStream(entry);
        }
    }


}

