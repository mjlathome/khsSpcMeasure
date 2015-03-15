package com.khs.spcmeasure.library;

import android.widget.CursorAdapter;

/**
 * @author Mark
 *
 */
public class CursorAdapterUtils {

    // get position for provided cursor id
    public static Integer getPosForId(CursorAdapter adapter, Long id) {
        Integer pos = null;

        if (adapter != null && id != null) {
            for (pos = 0; pos < adapter.getCount(); pos++) {
                if (adapter.getItemId(pos) == id) {
                    break;
                }
            }
        }

        return pos;
    }
}
