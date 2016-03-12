/**
 * 
 */
package com.khs.spcmeasure.old;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.khs.spcmeasure.entity.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mark
 *
 */
public class DatabaseHandler extends SQLiteOpenHelper{

	// All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 2;
 
    // Database Name
    private static final String DATABASE_NAME = "spcMeasure";
 
    // table names
    private static final String TABLE_PRODUCT = "product";
    private static final String TABLE_FEATURE = "feature";
    private static final String TABLE_LIMITS = "limits";
	
    // column names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_ACTIVE = "active";
    private static final String KEY_CUSTOMER = "customer";
    private static final String KEY_PROGRAM = "program";
    private static final String KEY_LIMIT_ID = "limitId";
    private static final String KEY_TYPE = "type";
    private static final String KEY_UPPER = "upper";
    private static final String KEY_LOWER = "lower";
    
    // constructor
    public DatabaseHandler(Context context) {
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    // create tables    
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_PRODUCT_TABLE ="CREATE TABLE " + TABLE_PRODUCT + "(" + 
				KEY_ID + " INTEGER PRIMARY KEY UNIQUE," +
				KEY_NAME + " TEXT," +
				KEY_ACTIVE + " INTEGER," +
				KEY_CUSTOMER + " TEXT," +
				KEY_PROGRAM + " TEXT" + ")";
		
		String CREATE_FEATURE_TABLE ="CREATE TABLE " + TABLE_FEATURE + "(" + 
				KEY_ID + " INTEGER PRIMARY KEY UNIQUE," +
				KEY_NAME + " TEXT," +
				KEY_ACTIVE + " INTEGER," +
				KEY_LIMIT_ID + " INTEGER" + ")";

		String CREATE_LIMIT_TABLE ="CREATE TABLE " + TABLE_LIMITS + "(" + 
				KEY_ID + " INTEGER PRIMARY KEY," +
				KEY_TYPE + " TEXT," +
				KEY_UPPER + " REAL," +
				KEY_LOWER + " REAL" + ")";
	
		db.execSQL(CREATE_PRODUCT_TABLE);
		db.execSQL(CREATE_FEATURE_TABLE);
		db.execSQL(CREATE_LIMIT_TABLE);
	}

	// upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// drop older table if it exists
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEATURE);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIMITS);
		
		// create tables again
		onCreate(db);		
	}
	
	// create new product
	public void createProduct(Product product) {
		SQLiteDatabase db = this.getWritableDatabase();
				
		// insert row
		db.insert(TABLE_PRODUCT, null, productToValues(product));
		
		Log.d("DEBUG addProduct","");
		
		db.close();
	};
	
	// get single product
	public Product getProduct(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		
		Cursor c = db.query(TABLE_PRODUCT,  
				new String[] {KEY_ID, KEY_NAME, KEY_ACTIVE, KEY_CUSTOMER, KEY_PROGRAM},
				KEY_ID + "=?",
				new String[] {String.valueOf(id)}, null, null, null, null);
		
		if (c != null) {
			c.moveToFirst();
		}
		
		return cursorToProduct(c);
	};
	
	// get all products
	public List<Product> getAllProducts() {
		List<Product> productList = new ArrayList<Product>();
		
		// select all query
		String selectQuery = "SELECT * FROM " + TABLE_PRODUCT;
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, null);
		
		// looping through all rows and adding to list
		if (c.moveToFirst()) {
			do {				
				// add product to the list
				productList.add(cursorToProduct(c));
				
			} while(c.moveToNext());
		}
		
		// return product list
		return productList;
		
	};
	
	// get products count
	public int getProductsCount() {
		// select all query
		String countQuery = "SELECT * FROM " + TABLE_PRODUCT;
		
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		
		int count = cursor.getCount();
		
		cursor.close();
		
		// return count
		return count;		
	};
	
	// update single product
	public int updateProduct(Product product) {
		SQLiteDatabase db = this.getWritableDatabase();
				
		// update row
		return db.update(TABLE_PRODUCT, productToValues(product), KEY_ID + " = ?",
				new String[] {String.valueOf(product.getId())});		
	};
	
	// delete single product
	public void deleteProduct(Product product) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_PRODUCT, KEY_ID + " = ? ", 
				new String[] {String.valueOf(product.getId())});
		db.close();
	};
	
	private Product cursorToProduct(Cursor c) {
		Product product = new Product(
				c.getInt(c.getColumnIndex(KEY_ID)),
				c.getString(c.getColumnIndex(KEY_NAME)),
				Boolean.parseBoolean(c.getString(c.getColumnIndex(KEY_ACTIVE))),
				c.getString(c.getColumnIndex(KEY_CUSTOMER)),
				c.getString(c.getColumnIndex(KEY_PROGRAM)));
		
		// return product
		return product;			
	}
	
	private ContentValues productToValues(Product product) {
		ContentValues values = new ContentValues();
		
		values.put(KEY_ID, product.getId());
		values.put(KEY_NAME, product.getName());
		values.put(KEY_ACTIVE, product.isActive());
		values.put(KEY_CUSTOMER, product.getCustomer());
		values.put(KEY_PROGRAM, product.getProgram());	
		
		return values;
	}
	
}
