package com.hjq.demo.chat.activity;

import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.hjq.demo.R;
import com.hjq.demo.chat.adapter.MyFragmentStateAdapter;
import com.hjq.demo.chat.fragment.FilesListFragment;
import com.hjq.demo.ui.adapter.TabAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * author : Android 轮子哥
 * github : https://github.com/getActivity/AndroidProject
 * time   : 2018/10/18
 * desc   : 文件管理界面
 */
public final class FileManagementActivity extends ChatBaseActivity implements TabAdapter.OnTabListener {


    private ViewPager2 qaViewPage2;
    private RecyclerView mTabView;
    private TabAdapter mTabAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.file_management_activity;
    }

    @Override
    protected void initView() {
        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(getString(R.string.file_management));
        qaViewPage2 = findViewById(R.id.vp2);
        // 创建 Fragment 列表
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(FilesListFragment.newInstance(true));
        fragmentList.add(FilesListFragment.newInstance(false));
        // 设置适配器
        MyFragmentStateAdapter adapter = new MyFragmentStateAdapter(this, fragmentList);
        qaViewPage2.setAdapter(adapter);
        qaViewPage2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (mTabAdapter == null) {
                    return;
                }
                mTabAdapter.setSelectedPosition(position);
            }
        });
        qaViewPage2.setOffscreenPageLimit(2);
        mTabView = findViewById(R.id.rv_home_tab);
        // 关联 ViewPager2 和 TabLayout
        // 绑定 TabLayout 和 ViewPager2
        mTabAdapter = new TabAdapter(this);
        mTabView.setAdapter(mTabAdapter);
    }

    @Override
    protected void initData() {
        String[] arrStr = {getString(R.string.received), getString(R.string.sent)};
        mTabAdapter.addItem(arrStr[0]);
        mTabAdapter.addItem(arrStr[1]);
        mTabAdapter.setOnTabListener(this);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void initListener() {

    }

    @Override
    public boolean onTabSelected(RecyclerView recyclerView, int position) {
        qaViewPage2.setCurrentItem(position);
        return true;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        qaViewPage2.setAdapter(null);
        mTabAdapter.setOnTabListener(null);
    }
}