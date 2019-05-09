package net.eforcesoftware.brothersdk.printprocess;

import android.content.Context;

import com.brother.ptouch.sdk.PrinterInfo;
import com.brother.ptouch.sdk.PrinterInfo.PrinterSettingItem;
import com.brother.ptouch.sdk.PrinterStatus;

import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class PrinterPreference extends BasePrint {
    private final static int COMMAND_SEND = 0;
    private final static int COMMAND_RECEIVE = 1;
    private final Context context;
    private List<PrinterSettingItem> mKey;
    private Map<PrinterSettingItem, String> mSettings;
    private int commandType = 0;
    private PrinterPreListener mListener = null;

    public PrinterPreference(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * Updating the printer settings The results are reported in listener
     *
     * @param btPref
     * @param listener
     */
    public void updatePrinterSetting(Map<PrinterSettingItem, String> settings) {
        mCancel = false;
        this.commandType = COMMAND_SEND;
        this.mSettings = settings;
        PrinterPrefThread pref = new PrinterPrefThread();
        pref.start();
    }

    /**
     * Getting the printer settings
     *
     * @param listener
     */
    public void getPrinterSetting(List<PrinterSettingItem> keys,
                                  PrinterPreListener listener) {
        if (listener == null) {
            return;
        }
        mCancel = false;

        mListener = listener;
        this.mKey = keys;
        this.commandType = COMMAND_RECEIVE;
        PrinterPrefThread pref = new PrinterPrefThread();
        pref.start();
    }

    @Override
    protected void doPrint() {
    }

    public interface PrinterPreListener extends EventListener {
        void finish(PrinterStatus status,
                    Map<PrinterSettingItem, String> settings);
    }

    private class PrinterPrefThread extends Thread {
        @Override
        public void run() {

            // set info. for printing
            setPrinterInfo();

            mPrintResult = new PrinterStatus();

            if (!mCancel) {
                if (commandType == COMMAND_SEND) {
                    mPrintResult = mPrinter.updatePrinterSettings(mSettings);

                } else if (commandType == COMMAND_RECEIVE) {

                    mSettings = new HashMap<PrinterSettingItem, String>();
                    mPrintResult = mPrinter.getPrinterSettings(mKey, mSettings);
                }
            } else {
                mPrintResult.errorCode = PrinterInfo.ErrorCode.ERROR_CANCEL;
            }

            if (mListener != null) {
                mListener.finish(mPrintResult, mSettings);
            }
        }
    }

}
