package com.iteye.weimingtom.snowbook.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iteye.weimingtom.jkanji.R;
import com.nhaarman.listviewanimations.itemmanipulation.ExpandableListItemAdapter;

public class ListViewAnimationsMyExpandableListItemAdapter extends ExpandableListItemAdapter<Integer> {

    private final Context mContext;
    private final LruCache<Integer, Bitmap> mMemoryCache;

    /**
     * Creates a new ExpandableListItemAdapter with the specified list, or an empty list if
     * items == null.
     */
    public ListViewAnimationsMyExpandableListItemAdapter(final Context context, final List<Integer> items) {
        super(context, R.layout.lva__expandablelistitem_card, R.id.activity_expandablelistitem_card_title, R.id.activity_expandablelistitem_card_content, items);
        mContext = context;

        final int cacheSize = (int) (Runtime.getRuntime().maxMemory() / 1024);
        mMemoryCache = new LruCache<Integer, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(final Integer key, final Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
    }

    @Override
    public View getTitleView(final int position, final View convertView, final ViewGroup parent) {
        TextView tv = (TextView) convertView;
        if (tv == null) {
            tv = new TextView(mContext);
        }
        tv.setText(mContext.getString(R.string.lva__expandablelistitemadapter, getItem(position)));
        return tv;
    }

    @Override
    public View getContentView(final int position, final View convertView, final ViewGroup parent) {
        ImageView imageView = (ImageView) convertView;
        if (imageView == null) {
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        int imageResId;
        switch (getItem(position) % 5) {
            case 0:
                imageResId = R.drawable.bitmapfun_empty_photo;
                break;
            case 1:
                imageResId = R.drawable.bitmapfun_empty_photo;
                break;
            case 2:
                imageResId = R.drawable.bitmapfun_empty_photo;
                break;
            case 3:
                imageResId = R.drawable.bitmapfun_empty_photo;
                break;
            default:
                imageResId = R.drawable.bitmapfun_empty_photo;
        }

        Bitmap bitmap = getBitmapFromMemCache(imageResId);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), imageResId);
            addBitmapToMemoryCache(imageResId, bitmap);
        }
        imageView.setImageBitmap(bitmap);

        return imageView;
    }

    private void addBitmapToMemoryCache(final int key, final Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(final int key) {
        return mMemoryCache.get(key);
    }
}
