package cn.dacas.emmclient.ui.qdlayout;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.dacas.emmclient.R;
import cn.dacas.emmclient.util.QDLog;


/**
 * Created by yetwish on 2015-05-11
 */

public class SearchView extends LinearLayout implements View.OnClickListener {

    /**
     * �����
     */
    private EditText etInput;

    /**
     * ɾ����
     */
    private ImageView ivDelete;

    /**
     * ���ذ�ť
     */
//    private Button btnBack;

    /**
     * �����Ķ���
     */
    private Context mContext;

    /**
     * �����б�
     */
//    private ListView lvTips;

    /**
     * ��ʾadapter ���Ƽ�adapter��
     */
    private ArrayAdapter<String> mHintAdapter;

    /**
     * �Զ���ȫadapter ֻ��ʾ����
     */
    private ArrayAdapter<String> mAutoCompleteAdapter;

    /**
     * �����ص��ӿ�
     */
    private SearchViewListener mListener;

    /**
     * ���������ص��ӿ�
     *
     * @param listener ������
     */
    public void setSearchViewListener(SearchViewListener listener) {
        mListener = listener;
    }

    public SearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.search_layout, this);
        initViews();
    }

    private void initViews() {
        etInput = (EditText) findViewById(R.id.search_et_input);
        ivDelete = (ImageView) findViewById(R.id.search_iv_delete);
        ivDelete.setVisibility(View.GONE);
//        btnBack = (Button) findViewById(R.id.search_btn_back);
//        lvTips = (ListView) findViewById(R.id.search_lv_tips);
//
//        lvTips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                //set edit text
//                String text = lvTips.getAdapter().getItem(i).toString();
//                etInput.setText(text);
//                etInput.setSelection(text.length());
//                //hint list view gone and result list view show
//                lvTips.setVisibility(View.GONE);
//                notifyStartSearching(text);
//            }
//        });

        ivDelete.setOnClickListener(this);
//        btnBack.setOnClickListener(this);

        etInput.addTextChangedListener(new EditChangedListener());
        etInput.setOnClickListener(this);
        etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    lvTips.setVisibility(GONE);
                    QDLog.i("SearchView", "initViews========== IME_ACTION_SEARCH");
                    notifyStartSearching(etInput.getText().toString());
                }
                return true;
            }
        });
        InputMethodManager imm = ( InputMethodManager ) mContext.getSystemService(Context.INPUT_METHOD_SERVICE );
        if ( imm.isActive( ) ) {
            imm.hideSoftInputFromWindow( etInput.getApplicationWindowToken( ) , 0 );
        }
//        InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(etInput.getWindowToken(),0);
//        etInput.clearFocus();
    }

    /**
     * ֪ͨ������ ������������
     * @param text
     */
    private void notifyStartSearching(String text){
        if (mListener != null) {
            mListener.onSearch(etInput.getText().toString());
        }
//        //���������
//        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * �������Ѱ���ʾ adapter
     */
    public void setTipsHintAdapter(ArrayAdapter<String> adapter) {
        this.mHintAdapter = adapter;
//        if (lvTips.getAdapter() == null) {
//            lvTips.setAdapter(mHintAdapter);
//        }
    }

    /**
     * �����Զ���ȫadapter
     */
    public void setAutoCompleteAdapter(ArrayAdapter<String> adapter) {
        this.mAutoCompleteAdapter = adapter;
    }

    private class EditChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            if (!"".equals(charSequence.toString())) {
                ivDelete.setVisibility(VISIBLE);
//                lvTips.setVisibility(VISIBLE);
//                if (mAutoCompleteAdapter != null && lvTips.getAdapter() != mAutoCompleteAdapter) {
//                    lvTips.setAdapter(mAutoCompleteAdapter);
//                }
                //����autoComplete����
                if (mListener != null) {
                    mListener.onRefreshAutoComplete(charSequence + "");
                }
            } else {
                ivDelete.setVisibility(GONE);
//                if (mHintAdapter != null) {
//                    lvTips.setAdapter(mHintAdapter);
//                }
//                lvTips.setVisibility(GONE);
            }

        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_et_input:
//                lvTips.setVisibility(VISIBLE);
                break;
            case R.id.search_iv_delete:
                etInput.setText("");
                ivDelete.setVisibility(GONE);
                break;
//            case R.id.search_btn_back:
//                ((Activity) mContext).finish();
//                break;
        }
    }

    /**
     * search view�ص�����
     */
    public interface SearchViewListener {

        /**
         * �����Զ���ȫ����
         *
         * @param text ���벹ȫ����ı�
         */
        void onRefreshAutoComplete(String text);

        /**
         * ��ʼ����
         *
         * @param text �����������ı�
         */
        void onSearch(String text);

//        /**
//         * ��ʾ�б�����ʱ�ص����� (��ʾ/�Զ���ȫ)
//         */
//        void onTipsItemClick(String text);
    }

}


//
//import android.app.Activity;
//import android.content.Context;
//import android.text.Editable;
//import android.text.TextWatcher;
//import android.util.AttributeSet;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.inputmethod.EditorInfo;
//import android.widget.AdapterView;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ListView;
//import android.widget.TextView;
//
//import com.yetwish.customsearchdemo.R;
//import com.yetwish.customsearchdemo.activity.adapter.SearchAdapter;
//import com.yetwish.customsearchdemo.activity.model.Bean;
//
///**
// * Created by yetwish on 2015-05-11
// */
//
//public class SearchView extends LinearLayout implements View.OnClickListener {
//
//    private EditText etInput;
//    private ImageView ivDelete;
//    private Button btnBack;
//    private Context mContext;
//
//    private ListView lvTips;
//    private SearchAdapter mHintAdapter;
//    private SearchAdapter mAutoCompleteAdapter;
//
//    private SearchViewListener mListener;
//
//    public void setSearchViewListener(SearchViewListener listener) {
//        mListener = listener;
//    }
//
//    public SearchView(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        mContext = context;
//        LayoutInflater.from(context).inflate(R.layout.search_layout, this);
//        initViews();
//    }
//
//    private void initViews() {
//        etInput = (EditText) findViewById(R.id.search_et_input);
//        ivDelete = (ImageView) findViewById(R.id.search_iv_delete);
//        btnBack = (Button) findViewById(R.id.search_btn_back);
//        lvTips = (ListView) findViewById(R.id.search_lv_tips);
//
//        lvTips.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                //set edit text
//                Bean currentBean = (Bean) lvTips.getAdapter().getItem(i);
//                etInput.setText(currentBean.getTitle());
//                //hint list view gone and result list view show
//                lvTips.setVisibility(View.GONE);
//                if (mListener != null) {
//                    mListener.onTipsItemClick();
//                }
//            }
//        });
//
//        ivDelete.setOnClickListener(this);
//        btnBack.setOnClickListener(this);
//
//        etInput.addTextChangedListener(new EditChangedListener());
//        etInput.setOnClickListener(this);
//        etInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
//                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                    if (mListener != null) {
//                        mListener.onSearch(etInput.getText().toString());
//                    }
//                }
//                return true;
//            }
//        });
//    }
//
//    /**
//     * 设置热搜版提�? adapter
//     */
//    public void setTipsHintAdapter(SearchAdapter adapter) {
//        this.mHintAdapter = adapter;
//        if (lvTips.getAdapter() == null) {
//            lvTips.setAdapter(mHintAdapter);
//        }
//    }
//
//    /**
//     * 设置自动补全adapter
//     */
//    public void setAutoCompleteAdapter(SearchAdapter adapter) {
//        this.mAutoCompleteAdapter = adapter;
//    }
//
//    private class EditChangedListener implements TextWatcher {
//        @Override
//        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//
//        }
//
//        @Override
//        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
//            if (!"".equals(charSequence + "")) {
//                ivDelete.setVisibility(VISIBLE);
//                lvTips.setVisibility(VISIBLE);
//                if (mAutoCompleteAdapter != null && lvTips.getAdapter() != mAutoCompleteAdapter) {
//                    lvTips.setAdapter(mAutoCompleteAdapter);
//                } else {
//                    //更新autoComplete数据
//                    if (mListener != null) {
//                        mListener.onRefreshAutoComplete(charSequence + "");
//                    }
//                }
//            } else {
//                ivDelete.setVisibility(GONE);
//                if (mHintAdapter != null) {
//                    lvTips.setAdapter(mHintAdapter);
//                }
//                lvTips.setVisibility(GONE);
//            }
//
//        }
//
//        @Override
//        public void afterTextChanged(Editable editable) {
//
//        }
//    }
//
//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.search_et_input:
//                lvTips.setVisibility(VISIBLE);
//                break;
//            case R.id.search_iv_delete:
//                etInput.setText("");
//                ivDelete.setVisibility(GONE);
//                break;
//            case R.id.search_btn_back:
//                ((Activity) mContext).finish();
//                break;
//        }
//    }
//
//    /**
//     * search view回调方法
//     */
//    public interface SearchViewListener {
//
//        void onRefreshAutoComplete(String text);
//
//        void onSearch(String text);
//
//        void onTipsItemClick();
//    }
//
//}
