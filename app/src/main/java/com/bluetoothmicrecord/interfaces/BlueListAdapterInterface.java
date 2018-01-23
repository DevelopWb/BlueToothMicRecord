package com.bluetoothmicrecord.interfaces;

import android.widget.TextView;

import com.bluetoothmicrecord.bean.BluetoothDevices;

/**
 * Created by ${王sir} on 2017/9/11.
 * application
 */

public interface BlueListAdapterInterface {

    void OnBlueListIvClickListener(BluetoothDevices bean, TextView matchStatus, TextView connectStatus, TextView bt);

    void blueListItemClickListener();

    void blueListItemLongClickListener(BluetoothDevices bean);

}
