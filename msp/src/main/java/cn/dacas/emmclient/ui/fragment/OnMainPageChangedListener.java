package cn.dacas.emmclient.ui.fragment;

import cn.dacas.emmclient.model.MamAppInfoModel;

/**
 * Created by lenovo on 2015-12-9.
 */
public interface OnMainPageChangedListener {

        public void onLeftPage(int idx,MamAppInfoModel model);
        public void onRightPage(int idx,MamAppInfoModel model);
}
