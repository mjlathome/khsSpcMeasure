package com.khs.spcmeasure.dao;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.khs.spcmeasure.DBAdapter;
import com.khs.spcmeasure.entity.Product;

/**
 * Created by Mark on 22/03/2015.
 */
public class ProductDao {

    private static final String TAG = "ProductDao";

    private Context mContext;
    private DBAdapter db;

    // constructor
    public ProductDao(Context context) {
        super();

        Log.d(TAG, "constructor");

        this.mContext = context;

        // instantiate db helper
        db = new DBAdapter(mContext);
    }

    // extracts the product
    public Product getProduct(long prodId) {
        Log.d(TAG, "getProduct: prodId = " + prodId);

        Product prod = null;
        Cursor cProd = null;

        try {
            db.open();

            // get product
            cProd = db.getProduct(prodId);
            prod = db.cursorToProduct(cProd);

        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            if (cProd != null) {
                cProd.close();
            }
            db.close();
        }

        Log.d(TAG, "getProduct: prod = " + prod);

        return prod;
    }

}