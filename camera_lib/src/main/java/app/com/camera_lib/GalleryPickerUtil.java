package app.com.camera_lib;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ankitpanwar on 3/8/18.
 */

public class GalleryPickerUtil {
    public static final int SELECT_FILE = 1;
    public static final int CAPTURE_PHOTO = 2;

    public static void launchGallery(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_FILE);
    }

    public static void launchGallery(Fragment fragment) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        fragment.startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_FILE);
    }

    public static void launchCamera(Activity activity, File photoFile) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            if (null != photoFile) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            }/*
            takePictureIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15);
            takePictureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);*/
            activity.startActivityForResult(takePictureIntent, CAPTURE_PHOTO);
        } else {
            Toast.makeText(activity, "Unable to open camera.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void launchCamera(Fragment fragment, File photoFile) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(fragment.getActivity().getPackageManager()) != null) {
            if (null != photoFile) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            }
            fragment.startActivityForResult(takePictureIntent, CAPTURE_PHOTO);
        } else {
            Toast.makeText(fragment.getActivity(), "Unable to open camera.", Toast.LENGTH_SHORT).show();
        }
    }


    public static String getPath(Uri uri, Activity activity) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        if (null == cursor) {
            return "";
        }
        int columnIndex = -1;
        try {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        } catch (Exception e) {
            e.printStackTrace();
            columnIndex = -1;
        }
        if (columnIndex < 0) {
            return "";
        }
        cursor.moveToFirst();
        String path = cursor.getString(columnIndex);
        return (null == path ? "" : path);
    }

    public static Bitmap getRotatedBitmap(String photoPath, int scaledWidth, int scaledHeight) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 1;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bitmap = decodeFile(options, photoPath);
            if (null == bitmap) {
                return bitmap;
            }
            if (bitmap.getWidth() > scaledWidth && bitmap.getHeight() > scaledHeight) {
                if (scaledHeight <= 0) {
                    float factor = (float) bitmap.getWidth() / (float) scaledWidth;
                    scaledHeight = (int) ((float) bitmap.getHeight() / factor);
                }
                try {
                    bitmap = ThumbnailUtils.extractThumbnail(bitmap, scaledWidth, scaledHeight);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }

            Matrix matrix = new Matrix();

            ExifInterface ei = new ExifInterface(photoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int angle = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;
                default:
                    angle = 0;
                    break;
            }
            matrix.postRotate(angle);
            if (angle != 0) {
                try {
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap decodeFile(BitmapFactory.Options options, String photoPath) {
        try {
            return BitmapFactory.decodeFile(photoPath, options);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            options.inSampleSize *= 2;
            return decodeFile(options, photoPath);
        }
    }

    public static File writeToTempFile(Activity activity, Bitmap bitmap) {
        try {
            File file = File.createTempFile("temp.jpg", null, activity.getCacheDir());

            FileOutputStream out = null;
            try {
                out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
                return file;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File createTempFile() {
        try {
            File file = File.createTempFile(Utils.getCurrentUnixTimestamp()+"temp.jpg", null, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));

            return file;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File createTempFileInSDCard() {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Utils.getCurrentUnixTimestamp()+"temp.jpg");
            if(file.exists()){
                file.delete();
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File writeToSDCardTempFile(Activity activity, Bitmap bitmap) {
        File file = new File(Environment.getExternalStorageDirectory(), "temp.jpg");

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize,
                                   boolean filter) {
        float ratio = Math.min(
                (float) maxImageSize / realImage.getWidth(),
                (float) maxImageSize / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);
        return newBitmap;
    }

}
