package net.eforcesoftware.brothersdk.printprocess;

import android.content.Context;

import com.brother.ptouch.sdk.BluetoothPreference;
import com.brother.ptouch.sdk.PrinterInfo;
import com.brother.ptouch.sdk.PrinterStatus;

import java.util.EventListener;

public class MWBluetoothPrinterPreference extends BasePrint {
    private final static int COMMAND_SEND = 0;
    private final static int COMMAND_RECEIVE = 1;
    private final Context context;
    private BluetoothPreference btPref;
    private int commandType = 0;
    private PrinterPreListener listener = null;

    public MWBluetoothPrinterPreference(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * Updating the printer settings The results are reported in listener
     *
     * @param btPref
     */
    public void updatePrinterSetting(BluetoothPreference btPref) {
        mCancel = false;
        this.commandType = COMMAND_SEND;
        this.btPref = btPref;
        PrinterPrefThread pref = new PrinterPrefThread();
        pref.start();
    }

    /**
     * Getting the printer settings
     *
     * @param listener
     */
    public void getPrinterSetting(PrinterPreListener listener) {
        if (listener == null) {
            return;
        }
        mCancel = false;
        this.listener = listener;

        this.commandType = COMMAND_RECEIVE;
        PrinterPrefThread pref = new PrinterPrefThread();
        pref.start();
    }

    @Override
    protected void doPrint() {
    }

    public interface PrinterPreListener extends EventListener {
        void finish(PrinterStatus status, BluetoothPreference btPre);
    }

    private class PrinterPrefThread extends Thread {
        @Override
        public void run() {

            // set info. for printing
            setPrinterInfo();

            mPrintResult = new PrinterStatus();
            if (!mCancel) {
                if (commandType == COMMAND_SEND) {
                    mPrintResult = mPrinter.updateBluetoothPreference(btPref);

                } else if (commandType == COMMAND_RECEIVE) {
                    btPref = new BluetoothPreference();
                    mPrintResult = mPrinter.getBluetoothPreference(btPref);
                }

                if (listener != null) {
                    listener.finish(mPrintResult, btPref);
                }
            } else {
                mPrintResult.errorCode = PrinterInfo.ErrorCode.ERROR_CANCEL;
            }
        }
    }

}
