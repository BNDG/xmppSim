package com.hjq.demo.chat.activity;


import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.SPUtils;
import com.hjq.demo.R;
import com.hjq.demo.app.AppActivity;
import com.hjq.demo.utils.CheckUtil;

import com.bndg.smack.constant.SmartConstants;

/**
 * @author r
 * @date 2024/9/20
 * @description 配置
 */

public class ConfigActivity extends AppActivity implements RadioGroup.OnCheckedChangeListener {
    private EditText etDomain;
    private EditText etHost;
    private EditText etPort;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_config;
    }

    @Override
    protected void initView() {
        findViewById(R.id.btn_xabber).setOnClickListener(v -> {
            etDomain.setText("xabber.org");
        });
        findViewById(R.id.btn_tigase).setOnClickListener(v -> {
            etDomain.setText("");
        });
    }

    @Override
    protected void initData() {
        String oldDomain = SPUtils.getInstance(SmartConstants.SP_NAME).getString(SmartConstants.CONSTANT_DOMAIN, com.bndg.smack.BuildConfig.xmppDomain);
        String oldHost = SPUtils.getInstance(SmartConstants.SP_NAME).getString(SmartConstants.CONSTANT_HOST, com.bndg.smack.BuildConfig.xmppHostAddress);
        int oldPort = SPUtils.getInstance(SmartConstants.SP_NAME).getInt(SmartConstants.CONSTANT_PORT, com.bndg.smack.BuildConfig.xmppPort);
        etDomain = findViewById(R.id.et_domain);
        etHost = findViewById(R.id.et_host);
        etPort = findViewById(R.id.et_port);
        etDomain.setText(oldDomain);
        etHost.setText(oldHost);
        etPort.setText(oldPort + "");
        findViewById(R.id.btn_config).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String domain = etDomain.getText().toString();
                String host = etHost.getText().toString();
                String port = etPort.getText().toString();
                if (TextUtils.isEmpty(domain) || TextUtils.isEmpty(host) || TextUtils.isEmpty(port)) {
                    toast("IP或端口号不能为空!");
                    return;
                }
                if (!RegexUtils.isMatch(CheckUtil.REGEX_URL2, domain)) {
                    toast("IP地址格式不正确!");
                    return;
                }
                SPUtils.getInstance(SmartConstants.SP_NAME).put(SmartConstants.CONSTANT_DOMAIN, domain);
                SPUtils.getInstance(SmartConstants.SP_NAME).put(SmartConstants.CONSTANT_HOST, host);
                SPUtils.getInstance(SmartConstants.SP_NAME).put(SmartConstants.CONSTANT_PORT, Integer.parseInt(port));
                toast("配置成功!");
                // todo 通知进行disconnect?
                finish();
            }
        });
        etDomain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                etHost.setText(s.toString());
            }
        });
        RadioGroup radioGroup = findViewById(R.id.rg_tls);
        String sec = SPUtils.getInstance(SmartConstants.SP_NAME).getString(SmartConstants.SECURITY_MODE);
        if (TextUtils.isEmpty(sec)) {
            radioGroup.check(R.id.rb_ifpossible);
        } else {
            if (SmartConstants.SECURITY_MODE_REQUIRED.equals(sec)) {
                radioGroup.check(R.id.rb_required);
            } else if (SmartConstants.SECURITY_MODE_DISABLED.equals(sec)) {
                radioGroup.check(R.id.rb_disabled);
            } else {
                radioGroup.check(R.id.rb_ifpossible);
            }
        }
        radioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // 是否需要重启
        if (checkedId == R.id.rb_disabled) {
            SPUtils.getInstance(SmartConstants.SP_NAME).put(SmartConstants.SECURITY_MODE, SmartConstants.SECURITY_MODE_DISABLED);
        } else if (checkedId == R.id.rb_required) {
            SPUtils.getInstance(SmartConstants.SP_NAME).put(SmartConstants.SECURITY_MODE, SmartConstants.SECURITY_MODE_REQUIRED);
        } else if (checkedId == R.id.rb_ifpossible) {
            SPUtils.getInstance(SmartConstants.SP_NAME).put(SmartConstants.SECURITY_MODE, SmartConstants.SECURITY_MODE);
        }
    }


}
