package cn.qdsc.msp.ui.fragment;

import cn.qdsc.msp.model.MamAppInfoModel;

/**
 * Created by lenovo on 2015-12-9.
 */
public interface OnMainPageChangedListener {

        public void onLeftPage(int idx,MamAppInfoModel model);
        public void onRightPage(int idx,MamAppInfoModel model);
}
