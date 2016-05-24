package cn.qdsc.msp.ui.activity.mainframe;

import android.content.Context;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import cn.qdsc.dynamicgrid.BaseDynamicGridAdapter;
import cn.qdsc.msp.R;
import cn.qdsc.msp.model.MamAppInfoModel;

public class AppDynamicGridAdapter extends BaseDynamicGridAdapter {
    private Context mContext;
    public AppDynamicGridAdapter(Context context, List<?> items, int columnCount) {
        super(context, items, columnCount);
        mContext=context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_grid, null);
            holder = new AppViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (AppViewHolder) convertView.getTag();
        }
        if (getItem(position) instanceof MamAppInfoModel)
            holder.build((MamAppInfoModel)getItem(position));
        return convertView;
    }

    private class AppViewHolder {
        private TextView titleText;
        private ImageView image;

        private AppViewHolder(View view) {
            titleText = (TextView) view.findViewById(R.id.item_title);
            image = (ImageView) view.findViewById(R.id.item_img);
        }

        void build(MamAppInfoModel model) {
            titleText.setText(model.appName);
            if (model.isApk()) {
                PackageManager pm = mContext.getPackageManager();
                try {
                    image.setImageDrawable(pm.getApplicationIcon(model.pkgName));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    image.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.apk));
                }
            }
            else if (model.isWeb()) {
//                image.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.container_web));
                DisplayImageOptions options = new DisplayImageOptions.Builder()
                        .showImageOnLoading(R.mipmap.container_web)
                        .showImageForEmptyUri(R.mipmap.ic_empty)
                        .showImageOnFail(R.mipmap.container_web).cacheInMemory(true)
                        .cacheOnDisk(true).considerExifParams(true).build();
                ImageLoader.getInstance().displayImage(model.iconUrl, image,options);
            }
        }
    }
}
