package com.iteye.weimingtom.snowbook.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.iteye.weimingtom.appmesh.dictionary.Node;
import com.iteye.weimingtom.appmesh.dictionary.Record;
import com.iteye.weimingtom.jkanji.R;
import com.nhaarman.listviewanimations.itemmanipulation.ExpandableListItemAdapter;

public class ExpandListAdapter extends ExpandableListItemAdapter<Node> {

    private final Context mContext;
    private final LruCache<Integer, Bitmap> mMemoryCache;
    private LayoutInflater mInflater;

    /**
     * Creates a new ExpandableListItemAdapter with the specified list, or an empty list if
     * items == null.
     */
    public ExpandListAdapter(final Context context, final List<Node> items) {
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
        
        this.mInflater = LayoutInflater.from(context);
    }

    /*
    @Override
    public View getTitleView(final int position, final View convertView, final ViewGroup parent) {
        TextView tv = (TextView) convertView;
        if (tv == null) {
            tv = new TextView(mContext);
        }
        Record record = getItem(position).getValue();
        if (record != null) {
        	tv.setText(record.getSound() + " -- " + record.getRomaji());
        } else {
        	tv.setText("empty record");
        }
        return tv;
    }
    */

    /*
    @Override
    public View getContentView(final int position, final View convertView, final ViewGroup parent) {
        ImageView imageView = (ImageView) convertView;
        if (imageView == null) {
            imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }

        int imageResId;
        switch (getItem(position).getId() % 5) { //FIXME:
            case 0:
                imageResId = R.drawable.lva__img_nature1;
                break;
            case 1:
                imageResId = R.drawable.lva__img_nature2;
                break;
            case 2:
                imageResId = R.drawable.lva__img_nature3;
                break;
            case 3:
                imageResId = R.drawable.lva__img_nature4;
                break;
            default:
                imageResId = R.drawable.lva__img_nature5;
        }

        Bitmap bitmap = getBitmapFromMemCache(imageResId);
        if (bitmap == null) {
            bitmap = BitmapFactory.decodeResource(mContext.getResources(), imageResId);
            addBitmapToMemoryCache(imageResId, bitmap);
        }
        imageView.setImageBitmap(bitmap);

        return imageView;
    }
    */
    
    @Override
    public View getTitleView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.snowbook_expand_title, null);
            holder = new ViewHolder();
            holder.textViewRomaji = (TextView) convertView.findViewById(R.id.textViewRomaji);
            holder.textViewSound = (TextView) convertView.findViewById(R.id.textViewSound);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
    	
        Record record = getItem(position).getValue();
        if (record != null) {
        	holder.textViewRomaji.setText(replaceNum(record.getRomaji()));
        	holder.textViewSound.setText(record.getSound());
        } else {
        	holder.textViewRomaji.setText("");
        	holder.textViewSound.setText("");
        }
        return convertView;
    }
    
    @Override
    public View getContentView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.snowbook_expand_content, null);
            holder = new ViewHolder();
            holder.textViewKanji = (TextView) convertView.findViewById(R.id.textViewKanji);
            holder.textViewAccent = (TextView) convertView.findViewById(R.id.textViewAccent);
            holder.textViewPos = (TextView) convertView.findViewById(R.id.textViewPos);
            holder.textViewMean = (TextView) convertView.findViewById(R.id.textViewMean);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        Record record = getItem(position).getValue();
        if (record != null) {
        	holder.textViewKanji.setText(replaceText(record.getKanji()));
            holder.textViewAccent.setText(replaceText(record.getAccent()));
            holder.textViewPos.setText(replaceText(record.getPos()));
            holder.textViewMean.setText(record.getMean());
        } else {
        	holder.textViewKanji.setText("");
            holder.textViewAccent.setText("");
            holder.textViewPos.setText("");
            holder.textViewMean.setText("");
        }

        return convertView;
    }

    private String replaceNum(String str) {
    	if (str == null) {
    		return null;
    	}
    	return str.replaceAll("\\d", "");
    }
    
    private String replaceText(String str) {
    	if (str == null) {
    		return null;
    	}
    	return str.replace("|", "、");
    }
    
    private static class ViewHolder {
    	TextView textViewRomaji;
    	TextView textViewSound;
    	
    	TextView textViewKanji;
    	TextView textViewAccent;
    	TextView textViewPos;
    	TextView textViewMean;
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
