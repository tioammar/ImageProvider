package com.jude.library.imageprovider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.jude.library.imageprovider.corpimage.CropImageIntentBuilder;
import com.jude.library.imageprovider.net.NetImageSearchActivity;
import com.jude.library.imageprovider.net.utils.ImageLoader;

import java.io.File;
import java.io.IOException;

/**
 * Created by Mr.Jude on 2015/3/15.
 */
public class ImageProvider {

    private Activity act;

    private OnImageSelectListener mListener;

    private static final int REQUEST_CAMERA = 12580;
    private static final int REQUEST_ALBUM = 12581;
    private static final int REQUEST_NET = 12582;
    private static final int REQUEST_CORP = 12583;

    private File dir;
    private File tempImage;

    public static String[] mRecommendList = {
            "拥抱","梦幻","爱情","唯美","汪星人","美好","风景","孤独","插画"
    };

    public static void setNetRecommendList(String[] list){
        mRecommendList = list;
    }

    public ImageProvider(Activity act){
        this.act = act;
        Utils.initialize(act.getApplication(), "imageLog");
        dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        dir.mkdir();
    }

    public void getImageFromAlbum(OnImageSelectListener mListener){
        this.mListener = mListener;
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        act.startActivityForResult(intent, REQUEST_ALBUM);
    }

    public void getImageFromCamera(OnImageSelectListener mListener){
        this.mListener = mListener;
        tempImage =  createTempImageFile();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(tempImage));
        act.startActivityForResult(intent, REQUEST_CAMERA);
    }

    public void getImageFromNet(OnImageSelectListener mListener){
        this.mListener = mListener;
        Intent intent = new Intent(act,NetImageSearchActivity.class);
        act.startActivityForResult(intent, REQUEST_NET);
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent data){
        if (resultCode != act.RESULT_OK) return ;
        if (mListener == null) return;
        switch (requestCode){
            case REQUEST_CAMERA:
                mListener.onImageSelect();
                mListener.onImageLoaded(Uri.fromFile(tempImage));
                break;
            case REQUEST_ALBUM:
                mListener.onImageSelect();
                mListener.onImageLoaded(Uri.parse(ImageUriUtils.getPath(act, data.getData())));
                break;
            case REQUEST_NET:
                mListener.onImageSelect();
                String url = data.getStringExtra("data");
                ImageLoader.getInstance().image(url, new ImageLoader.ImageCallback() {
                    @Override
                    public void success(Bitmap bitmap) {
                        File temp = createTempImageFile();
                        Utils.BitmapSave(bitmap, temp.getPath());
                        mListener.onImageLoaded(Uri.fromFile(temp));
                    }

                    @Override
                    public void error() {
                        mListener.onError();
                    }
                });
                break;
            case REQUEST_CORP:
                mListener.onImageSelect();
                mListener.onImageLoaded(Uri.fromFile(tempImage));
                break;
        }
    }

    public void corpImage(Uri uri,int width,int height,OnImageSelectListener listener){
        this.mListener = listener;
        tempImage = createTempImageFile();
        CropImageIntentBuilder cropImage = new CropImageIntentBuilder(width, height,width, height, Uri.fromFile(tempImage));
        cropImage.setSourceImage(uri);
        act.startActivityForResult(cropImage.getIntent(act), REQUEST_CORP);
    }

    private File createTempImageFile(){
        File file = new File(dir,System.currentTimeMillis()+".jpg");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static Bitmap readImageWithSize(Uri uri, int outWidth, int outHeight){
        return Utils.readBitmapAutoSize(uri.getPath(), outWidth, outHeight);
    }
}
