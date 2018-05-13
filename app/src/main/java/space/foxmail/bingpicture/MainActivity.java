package space.foxmail.bingpicture;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.SAXParserFactory;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private ImageView toolbar_image;
    private ImageView content_image;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private Context context=this;
    private FloatingActionButton left;
    private FloatingActionButton location;
    private FloatingActionButton right;
    private FloatingActionButton save;
    private TextView content_text;
    private TextView author_text;
    private TextView date_text;
    private int idx=0;
    private String date;
    private String imageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        toolbar_image = findViewById(R.id.toolbar_image);
        content_image = findViewById(R.id.content_image);
        left=findViewById(R.id.left);
        location=findViewById(R.id.location);
        right=findViewById(R.id.right);
        save=findViewById(R.id.save);
        content_text=findViewById(R.id.content_text);
        author_text=findViewById(R.id.author_text);
        date_text=findViewById(R.id.date_text);
        setSupportActionBar(toolbar);
        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(idx==8){
                    Toast.makeText(context,"没有更前的图片了",Toast.LENGTH_SHORT).show();
                    return;
                }
                idx=idx+1;
                loadUrl("http://cn.bing.com/HPImageArchive.aspx?idx="+idx+"&n=1");
            }
        });
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(idx==0){
                    return;
                }
                idx=0;
                loadUrl("http://cn.bing.com/HPImageArchive.aspx?idx="+idx+"&n=1");
            }
        });
        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(idx==0){
                    Toast.makeText(context,"没有更后的图片了",Toast.LENGTH_SHORT).show();
                    return;
                }
                idx=idx-1;
                loadUrl("http://cn.bing.com/HPImageArchive.aspx?idx="+idx+"&n=1");
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPermission();
            }
        });
        loadUrl("http://cn.bing.com/HPImageArchive.aspx?idx="+idx+"&n=1");
    }
    private void loadImage(final String url, final String copyright,final String date) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                RequestOptions options = new RequestOptions()
                        .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                Glide.with(context)
                        .asBitmap()
                        .load(url)
                        .apply(options)
                        .into(new BitmapImageViewTarget(content_image){
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                super.onResourceReady(resource, transition);
                                Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(@NonNull Palette palette) {
                                        int rgb = palette.getVibrantColor(palette.getMutedColor(0));
                                        collapsingToolbarLayout.setContentScrimColor(rgb);
                                        collapsingToolbarLayout.setStatusBarScrimColor(rgb);
                                        left.setBackgroundTintList(ColorStateList.valueOf(rgb));
                                        location.setBackgroundTintList(ColorStateList.valueOf(rgb));
                                        right.setBackgroundTintList(ColorStateList.valueOf(rgb));
                                        save.setBackgroundTintList(ColorStateList.valueOf(rgb));
                                    }
                                });
                            }
                        });
                Glide.with(context).load(url).into(toolbar_image);
                setText(copyright,date);
            }
        });
    }
    private void loadUrl(String url) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(url)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseData = response.body().string();
                    try {
                        SAXParserFactory factory = SAXParserFactory.newInstance();
                        XMLReader xmlReader = factory.newSAXParser().getXMLReader();
                        ContentHandler handler = new ContentHandler();
                        xmlReader.setContentHandler(handler);
                        xmlReader.parse(new InputSource(new StringReader(responseData)));
                        String url = "http://cn.bing.com" + handler.getUrl()+"_1920x1080.jpg";
                        imageUrl=url;
                        date=handler.getDate();
                        loadImage(url,handler.getCopyright(),handler.getDate());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void setText(String copyright,String date){
        String content[]=copyright.split("©");
        content_text.setText(content[0].substring(0,content[0].length()-1));
        author_text.setText(content[1].substring(1,content[1].length()-1));
        String year=date.substring(0,4);
        String month=date.substring(4,6);
        String day=date.substring(6,8);
        date_text.setText(year+"年"+month+"月"+day+"日");
        collapsingToolbarLayout.setTitle(year+"年"+month+"月"+day+"日");
    }
    private void saveImage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Context context = getApplicationContext();
                    FutureTarget<File> target = Glide.with(context)
                            .asFile()
                            .load(imageUrl)
                            .submit();
                    final File imageFile = target.get();
                    File pictureFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsoluteFile();
                    File appDir = new File(pictureFolder ,"BingPicture");
                    if (!appDir.exists()) {
                        appDir.mkdirs();
                    }
                    String fileName = date+ ".jpg";
                    final File destFile = new File(appDir, fileName);
                    if(destFile.exists()){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "图片已存在", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                    FileInputStream fileInputStream = null;
                    FileOutputStream fileOutputStream = null;
                    try {
                        fileInputStream=new FileInputStream(imageFile);
                        fileOutputStream = new FileOutputStream(destFile);
                        byte[] buffer = new byte[1024];
                        while (fileInputStream.read(buffer) > 0) {
                            fileOutputStream.write(buffer);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                       fileInputStream.close();
                        fileOutputStream.close();
                    }
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.fromFile(destFile)));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "图片已保存至"+destFile.getPath(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void getPermission(){
        List<String> permissionList=new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.
                permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String permissions[]=permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else{
            saveImage();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if(grantResults.length>0){
                    for(int result:grantResults){
                        if(result!=PackageManager.PERMISSION_GRANTED){
                            Toast.makeText(context,"保存图片被拒绝，请同意所有权限",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    saveImage();
                }
            default:
                break;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.setWallpaper:
                setWallpaper();
                break;
            default:
                break;
        }
        return true;
    }
    private void setWallpaper(){
        WallpaperManager manager = WallpaperManager.getInstance(context);
        FutureTarget<Bitmap> bitmapTarget = Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .submit();
        try {
            Bitmap bitmap=bitmapTarget.get();
            manager.setBitmap(bitmap);
            Toast.makeText(context,"图片已设为壁纸",Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}