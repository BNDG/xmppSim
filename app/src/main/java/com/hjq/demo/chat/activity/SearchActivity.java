package com.hjq.demo.chat.activity;

import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.hjq.demo.R;
import com.hjq.demo.chat.adapter.SearchNewsAdapter;

import butterknife.BindView;

/**
 * 搜一搜
 *
 * @author zhou
 */
public class SearchActivity extends ChatBaseActivity {

    @BindView(R.id.et_search)
    EditText mSearchEt;

    @BindView(R.id.lv_search_news)
    ListView mSearchNewsLv;

    SearchNewsAdapter mSearchNewsAdapter;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_search;
    }

    public void initView() {


        getHotSearchHistoryList();

        mSearchEt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(SearchActivity.this, SearchContentActivity.class));
            }
        });
    }

    @Override
    protected void initData() {

    }

    /**
     * 获取搜索热词
     */
    private void getHotSearchHistoryList() {
    }

    @Override
    public void initListener() {

    }
}
