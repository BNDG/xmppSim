package com.bndg.smack;

import android.app.Application;
import android.text.TextUtils;


import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.carbons.packet.CarbonExtension;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.omemo.OmemoManager;
import org.jivesoftware.smackx.omemo.OmemoMessage;
import org.jivesoftware.smackx.omemo.element.OmemoElement_VAxolotl;
import org.jivesoftware.smackx.omemo.internal.OmemoDevice;
import org.jivesoftware.smackx.omemo.listener.OmemoMessageListener;
import org.jivesoftware.smackx.omemo.listener.OmemoMucMessageListener;
import org.jivesoftware.smackx.omemo.signal.SignalCachingOmemoStore;
import org.jivesoftware.smackx.omemo.signal.SignalFileBasedOmemoStore;
import org.jivesoftware.smackx.omemo.signal.SignalOmemoService;
import org.jivesoftware.smackx.omemo.trust.OmemoFingerprint;
import org.jivesoftware.smackx.omemo.trust.OmemoTrustCallback;
import org.jivesoftware.smackx.omemo.trust.TrustState;

import java.util.HashMap;

import com.bndg.smack.constant.SmartConstants;
import com.bndg.smack.utils.SmartTrace;
import com.bndg.smack.utils.StorageUtils;

/**
 * @author r
 * @date 2024/12/23
 * @description Brief description of the file content.
 */
public class OmemoHelper {
    private static volatile OmemoHelper instance;
    private HashMap<String, Boolean> encryptMap = new HashMap();

    private OmemoHelper() {
    }

    public static OmemoHelper getInstance() {
        if (instance == null) {
            synchronized (OmemoHelper.class) {
                if (instance == null) {
                    instance = new OmemoHelper();
                }
            }
        }
        return instance;
    }

    public void init(XMPPTCPConnection connection) {
        // 对于每个设备，您都需要一个 OmemoManager。在这个例子中，我们使用 smack-omemo-signal 实现，
        // 因此我们使用 SignalOmemoService 作为 OmemoService。OmemoManager 必须使用 deviceId（现有设备）或 null（如果要生成新设备）进行初始化。
        // OmemoManager 可用于执行与 OMEMO 相关的操作，例如发送消息等。如果您未传递 deviceId，则将使用 defaultDeviceId 的值（如果存在）
        OmemoManager omemoManager = OmemoManager.getInstanceFor(connection);
        omemoManager.setTrustCallback(new OmemoTrustCallback() {
            @Override
            public TrustState getTrust(OmemoDevice device, OmemoFingerprint fingerprint) {
                SmartTrace.w("getTrust " + fingerprint.toString());
                return TrustState.trusted;
            }

            @Override
            public void setTrust(OmemoDevice device, OmemoFingerprint fingerprint, TrustState state) {
                SmartTrace.w("setTrust " + fingerprint.toString());
            }
        });
        omemoManager.addOmemoMessageListener(new OmemoMessageListener() {
            @Override
            public void onOmemoMessageReceived(Stanza stanza, OmemoMessage.Received decryptedMessage) {
                // Do not process if decryptedMessage isKeyTransportMessage i.e. msgBody == null
                if (decryptedMessage.isKeyTransportMessage())
                    return;
                Message msg = (Message) stanza;
                Message build = msg.asBuilder().removeExtension(OmemoElement_VAxolotl.NAME_ENCRYPTED, OmemoElement_VAxolotl.NAMESPACE)
                        .removeExtension(Message.Body.ELEMENT, Message.Body.NAMESPACE)
                        .setBody(decryptedMessage.getBody()).build();
                SmartIMClient.getInstance().processMsg(false, build);
                SmartTrace.d("origin body " + decryptedMessage.getBody());
            }

            @Override
            public void onOmemoCarbonCopyReceived(CarbonExtension.Direction direction, Message carbonCopy, Message wrappingMessage, OmemoMessage.Received decryptedCarbonCopy) {

            }
        });
        omemoManager.addOmemoMucMessageListener(new OmemoMucMessageListener() {
            @Override
            public void onOmemoMucMessageReceived(MultiUserChat muc, Stanza stanza, OmemoMessage.Received decryptedOmemoMessage) {

            }
        });
    }

    public void setUp(Application application) {
        // 注册 OmemoService 将服务注册为单一实例。您可以稍后通过调用 来访问实例。该服务只能注册一次
        SignalOmemoService.acknowledgeLicense();
        SignalOmemoService.setup();
        SignalOmemoService service = (SignalOmemoService) SignalOmemoService.getInstance();
        // 第一步，您必须准备 OmemoStore。您可以使用自己的实现，也可以使用内置的 FileBasedOmemoStore（默认）。
        // 如果您不想使用自己的 store，则 implementation 使用基于文件的 store，因此您必须设置默认路径。
        service.setOmemoStoreBackend(new SignalCachingOmemoStore(new SignalFileBasedOmemoStore(application.getFilesDir())));
    }

    public void publishDevice() {
        // 在登录之后
        OmemoManager omemoManager = OmemoManager.getInstanceFor(SmartIMClient.getInstance().getConnection());
        try {
            omemoManager.purgeDeviceList();
            String string = omemoManager.getOwnFingerprint().toString();
            StorageUtils.getInstance(SmartConstants.SP_NAME).put(SmartCommHelper.getInstance().getAccount() + "_omemo_finger", string);
            // 会打印出我的设备的指纹
            SmartTrace.w("omemoManager: " + string);
        } catch (Exception e) {
            SmartTrace.w("omemoManager: " + e);
        }
        omemoManager.initializeAsync(new OmemoManager.InitializationFinishedCallback() {
            @Override
            public void initializationFinished(OmemoManager manager) {
                SmartTrace.w("omemoManager: initializationFinished");
            }

            @Override
            public void initializationFailed(Exception cause) {
                SmartTrace.w("omemoManager: initializationFailed");
            }
        });
    }

    public String getFingerprint() {
        return StorageUtils.getInstance(SmartConstants.SP_NAME).getString(SmartCommHelper.getInstance().getAccount() + "_omemo_finger");
    }

    public boolean isEnableEncrypt(String conversationId) {
        // 获取是否开启加密 客户端控制还是？
        String key = SmartCommHelper.getInstance().getAccount() + "_omemo_" + conversationId;
        return !TextUtils.isEmpty(StorageUtils.getInstance(SmartConstants.SP_NAME).getString(
                key));
    }

    public void enableEncrypt(String conversationId, boolean enabled) {
        String key = SmartCommHelper.getInstance().getAccount() + "_omemo_" + conversationId;
        if (enabled) {
            StorageUtils.getInstance(SmartConstants.SP_NAME).put(
                    key, "true");
        } else {
            StorageUtils.getInstance(SmartConstants.SP_NAME).remove(
                    key);
        }
    }
}
