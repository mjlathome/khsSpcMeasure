package com.khs.spcmeasure;

import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.entity.Feature;
import com.khs.spcmeasure.entity.Limits;
import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.entity.Measurement;

import com.khs.spcmeasure.entity.SimpleCode;
import com.khs.spcmeasure.library.CollectStatus;
import com.khs.spcmeasure.library.DateTimeUtils;
import com.khs.spcmeasure.library.LimitType;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Date;


// handles Sqlite interaction
// TODO handle entity specific Table and Field values in Dao
// TODO entity creation within Dao
public class DBAdapter {
	
	private static final String TAG = "DbAdapter";
	
	// All Static variables
    // Database Version
    static final int DATABASE_VERSION = 14;
 
    // Database Name
    public static final String DATABASE_NAME = "spcMeasure";
 
    // table names
    public static final String TABLE_PRODUCT 		= "product";
    public static final String TABLE_FEATURE 		= "feature";
    public static final String TABLE_LIMITS   		= "limits";
    public static final String TABLE_PIECE   		= "piece";
    public static final String TABLE_MEASUREMENT    = "measurement";
    public static final String TABLE_SIMPLE_CODE    = "simpleCode";
	
    // column names
    public static final String KEY_ROWID = "_id";
    public static final String KEY_NAME = "name";
    public static final String KEY_ACTIVE = "active";
    public static final String KEY_CUSTOMER = "customer";
    public static final String KEY_PROGRAM = "program";
    public static final String KEY_PROD_ID = "prodId";
    public static final String KEY_FEAT_ID = "featId";
    public static final String KEY_SUB_GRP_ID = "sgId";
    public static final String KEY_PIECE_ID = "pieceId";
    public static final String KEY_PIECE_NUM = "pieceNum";    
    public static final String KEY_LIMIT_REV = "limitRev";
    public static final String KEY_LIMIT_TYPE = "limitType";
    public static final String KEY_UPPER = "upper";
    public static final String KEY_LOWER = "lower";
    public static final String KEY_CP = "cp";
    public static final String KEY_CPK = "cpk";
    public static final String KEY_COLLECT_DATETIME = "collectDt";
    public static final String KEY_COLLECT_STATUS = "collectStatus";
    public static final String KEY_OPERATOR = "operator";
    public static final String KEY_LOT = "lot";
    public static final String KEY_VALUE = "value";
    public static final String KEY_RANGE = "range";
    public static final String KEY_CAUSE = "cause";
    public static final String KEY_IN_CONTROL = "inControl";
    public static final String KEY_IN_ENG_LIM = "inEngLim";
    public static final String KEY_TYPE = "type";
    public static final String KEY_CODE = "code";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_INT_CODE = "intCode";

    // sql table creates
    static final String CREATE_TABLE_PRODUCT = "CREATE TABLE " + TABLE_PRODUCT + "(" + 
			KEY_ROWID + " INTEGER PRIMARY KEY UNIQUE NOT NULL," +
			KEY_NAME + " TEXT," +
			KEY_ACTIVE + " INTEGER," +
			KEY_CUSTOMER + " TEXT," +
			KEY_PROGRAM + " TEXT" + ")";
	
    static final String CREATE_TABLE_FEATURE = "CREATE TABLE " + TABLE_FEATURE + "(" + 
			KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT," +    		
			KEY_PROD_ID + " INTEGER KEY NOT NULL," +
			KEY_FEAT_ID + " INTEGER KEY NOT NULL," +
			KEY_NAME + " TEXT," +
			KEY_ACTIVE + " INTEGER," +
			KEY_LIMIT_REV + " INTEGER," +
            KEY_CP + " REAL," +
            KEY_CPK + " REAL" + ")";

    static final String CREATE_TABLE_LIMITS = "CREATE TABLE " + TABLE_LIMITS + "(" + 
			KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			KEY_PROD_ID + " INTEGER KEY NOT NULL," +
			KEY_FEAT_ID + " INTEGER KEY NOT NULL," +
			KEY_LIMIT_REV + " INTEGER KEY NOT NULL," +
			KEY_LIMIT_TYPE + " TEXT KEY NOT NULL," +
			KEY_UPPER + " REAL," +
			KEY_LOWER + " REAL" + ")";
    
    static final String CREATE_TABLE_PIECE = "CREATE TABLE " + TABLE_PIECE + "(" + 
			KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			KEY_PROD_ID + " INTEGER KEY NOT NULL," +
			KEY_SUB_GRP_ID + " INTEGER KEY," +
			KEY_PIECE_NUM + " INTEGER KEY NOT NULL," +
			KEY_COLLECT_DATETIME + " TEXT," +
			KEY_OPERATOR + " TEXT," +
			KEY_LOT + " TEXT," +
			KEY_COLLECT_STATUS + " TEXT KEY NOT NULL" + ")"; 

    static final String CREATE_TABLE_MEASUREMENT = "CREATE TABLE " + TABLE_MEASUREMENT + "(" + 
			KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			KEY_PIECE_ID + " INTEGER KEY NOT NULL," +
			KEY_PROD_ID + " INTEGER KEY NOT NULL," +
			KEY_FEAT_ID + " INTEGER KEY NOT NULL," +
			KEY_COLLECT_DATETIME + " TEXT," +
			KEY_OPERATOR + " TEXT," +
			KEY_VALUE + " REAL," +
            KEY_RANGE + " REAL," +
            KEY_CAUSE + " INTEGER," +
			KEY_LIMIT_REV + " INTEGER," +
			KEY_IN_CONTROL + " INTEGER," +
			KEY_IN_ENG_LIM + " INTEGER" + ")";

    static final String CREATE_TABLE_SIMPLE_CODE = "CREATE TABLE " + TABLE_SIMPLE_CODE + "(" +
            KEY_ROWID + " INTEGER PRIMARY KEY UNIQUE NOT NULL," +
            KEY_TYPE + " TEXT KEY NOT NULL," +
            KEY_CODE + " TEXT KEY NOT NULL," +
            KEY_DESCRIPTION + " TEXT," +
            KEY_INT_CODE + " TEXT KEY," +
            KEY_ACTIVE + " INTEGER" + ")";

    // member variables
    final Context context;
    
    DatabaseHelper DBHelper;
    SQLiteDatabase db;
    
    // constructor
    public DBAdapter(Context ctx) {
    	this.context = ctx;
    	DBHelper = new DatabaseHelper(this.context);
    }
    
    private static class DatabaseHelper extends SQLiteOpenHelper {
    	
    	public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

    	// creates all SQLite tables
		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "onCreate");
			try {
				// create tables
				db.execSQL(CREATE_TABLE_PRODUCT);
				db.execSQL(CREATE_TABLE_FEATURE);
				db.execSQL(CREATE_TABLE_LIMITS);
				db.execSQL(CREATE_TABLE_PIECE);
				db.execSQL(CREATE_TABLE_MEASUREMENT);
                db.execSQL(CREATE_TABLE_SIMPLE_CODE);
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}

		// deletes and re-creates all SQLite tables
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "onUpgrade: drop all");
			try {
				// drop older tables if they exist
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEATURE);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIMITS);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_PIECE);
				db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEASUREMENT);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_SIMPLE_CODE);

				// create tables again
				onCreate(db);							
			} catch(SQLException e) {
				e.printStackTrace();
			}
		}			
    }  // DatabaseHelper
    
    // open the database
    public DBAdapter open() throws SQLException {
    	db = DBHelper.getWritableDatabase();
    	return this;
    }
    
    // close the database
    public void close() {
    	DBHelper.close();
    }

    //region Product
    // create new Product
	public long createProduct(Product product) {
		Log.d(TAG, "createProduct: Id = " + String.valueOf(product.getId()));
		// insert row
		return db.insert(TABLE_PRODUCT, null, productToValues(product));
	};
    	
	// get all Products
	public Cursor getAllProducts() {
		// select all query
		String selectQuery = "SELECT * FROM " + TABLE_PRODUCT + " ORDER BY " + KEY_NAME;
		
		return db.rawQuery(selectQuery, null);		
	};	
	
	// get single Product
	public Cursor getProduct(long rowId) {
		Cursor c = db.query(TABLE_PRODUCT,  
				new String[] {KEY_ROWID, KEY_NAME, KEY_ACTIVE, KEY_CUSTOMER, KEY_PROGRAM},
				KEY_ROWID + "=" + rowId, 
				null, null, null, null, null);
		
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	};	
	
	// get Products count
	public int getProductsCount() {
		return getAllProducts().getCount();
	};	
		
	// update single Product
	public boolean updateProduct(Product product) {	
		Log.d(TAG, "updateProduct: Id = " + String.valueOf(product.getId()));
		return db.update(TABLE_PRODUCT, productToValues(product), KEY_ROWID + "=" + product.getId(), null) > 0; 
	};	
	
	// delete single Product
	public boolean deleteProduct(long rowId) {
		
		// delete all associated Features
		Cursor cFeat = getAllFeatures(rowId);		
		if (cFeat.moveToFirst()) {
			do {				
				// delete Feature
				deleteFeature(cFeat.getInt(cFeat.getColumnIndex(KEY_ROWID)));
				
			} while(cFeat.moveToNext());
		}
        cFeat.close();

		// delete all associated Pieces
		Cursor cPiece = getAllPieces(rowId);		
		if (cPiece.moveToFirst()) {
			do {				
				// delete Piece
				deletePiece(cPiece.getInt(cPiece.getColumnIndex(KEY_ROWID)));
				
			} while(cPiece.moveToNext());
		}		
		cPiece.close();

		// delete the product
		return db.delete(TABLE_PRODUCT, KEY_ROWID + "=" + rowId, null) > 0; 
	};	
	
	// convert cursor to product
	public Product cursorToProduct(Cursor c) {
		Product product = new Product(
				c.getInt(c.getColumnIndex(KEY_ROWID)),
				c.getString(c.getColumnIndex(KEY_NAME)),
				Boolean.parseBoolean(c.getString(c.getColumnIndex(KEY_ACTIVE))),
				c.getString(c.getColumnIndex(KEY_CUSTOMER)),
				c.getString(c.getColumnIndex(KEY_PROGRAM)));
		
		// return product
		return product;			
	}	

	// convert product to content values
	private ContentValues productToValues(Product product) {
		ContentValues values = new ContentValues();
		
		values.put(KEY_ROWID, product.getId());
		values.put(KEY_NAME, product.getName());
		values.put(KEY_ACTIVE, product.isActive());
		values.put(KEY_CUSTOMER, product.getCustomer());
		values.put(KEY_PROGRAM, product.getProgram());	
		
		return values;
	}
    //endregion

    //region Feature
    // create new Feature
	public long createFeature(Feature feature) {
		Log.d(TAG, "createFeature: p; f = " + String.valueOf(feature.getProdId()) + "; " + String.valueOf(feature.getFeatId()));
		// insert row
		return db.insert(TABLE_FEATURE, null, featureToValues(feature));
	};
    	
	// get all Features by prodId
	public Cursor getAllFeatures(long prodId) {
		// select all query
		String selectQuery = "SELECT * FROM " + TABLE_FEATURE + " WHERE " + KEY_PROD_ID + " = " + Long.toString(prodId);		
		return db.rawQuery(selectQuery, null);		
	};	

	// get single Feature by rowId
	public Cursor getFeature(long rowId) {
		Cursor c = db.query(TABLE_FEATURE,  
				new String[] {KEY_ROWID, KEY_PROD_ID, KEY_FEAT_ID, KEY_NAME, KEY_ACTIVE, KEY_LIMIT_REV, KEY_CP, KEY_CPK},
				KEY_ROWID + "=" + rowId, 
				null, null, null, null, null);
		
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	};
	
	// get single Feature by prodId and featId
	public Cursor getFeature(long prodId, long featId) {
		Cursor c = db.query(TABLE_FEATURE,  
				new String[] {KEY_ROWID, KEY_PROD_ID, KEY_FEAT_ID, KEY_NAME, KEY_ACTIVE, KEY_LIMIT_REV, KEY_CP, KEY_CPK},
				KEY_PROD_ID + "=" + prodId + " AND " + KEY_FEAT_ID + "=" + featId, 
				null, null, null, null, null);
		
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	};
	
	// update single Feature
	public boolean updateFeature(Feature feature) {	
		Log.d(TAG, "updFest:  RowId; Prod Id = " + String.valueOf(feature.getId()) + "; " + String.valueOf(feature.getProdId()));

        // TODO remove later as was always failing, due to null id value, which then caused new rows to be created during import
		// return db.update(TABLE_FEATURE, featureToValues(feature), KEY_ROWID + "=" + feature.getId(), null) > 0;

        return db.update(TABLE_FEATURE, featureToValues(feature),
                KEY_PROD_ID + "=" + feature.getProdId() + " AND " +
                KEY_FEAT_ID + "=" + feature.getFeatId(), null) > 0;
	};	
	
	// delete single Feature
	public boolean deleteFeature(long rowId) {
				
		// extract the Feature
		Cursor cFeat = getFeature(rowId);
		if (cFeat.moveToFirst()) {			
			// delete all associated Limits
			Cursor cLim = getAllLimits(
					cFeat.getInt(cFeat.getColumnIndex(KEY_PROD_ID)), 
					cFeat.getInt(cFeat.getColumnIndex(KEY_FEAT_ID)));		
			if (cLim.moveToFirst()) {
				do {				
					deleteLimit(cLim.getInt(cLim.getColumnIndex(KEY_ROWID)));				
				} while(cLim.moveToNext());
			}
            cLim.close();
		}
        cFeat.close();
			
		// delete the feature
		return db.delete(TABLE_FEATURE, KEY_ROWID + "=" + rowId, null) > 0; 
	};		
		
	// convert cursor to feature
	public Feature cursorToFeature(Cursor c) {
		Feature feature = new Feature(
				c.getLong(c.getColumnIndex(KEY_ROWID)),
				c.getLong(c.getColumnIndex(KEY_PROD_ID)),
				c.getLong(c.getColumnIndex(KEY_FEAT_ID)),
				c.getString(c.getColumnIndex(KEY_NAME)),
				Boolean.parseBoolean(c.getString(c.getColumnIndex(KEY_ACTIVE))),
				c.getLong(c.getColumnIndex(KEY_LIMIT_REV)),
                c.getDouble(c.getColumnIndex(KEY_CP)),
                c.getDouble(c.getColumnIndex(KEY_CPK)));
		
		return feature;			
	}	
	
	// convert feature to content values
	private ContentValues featureToValues(Feature feature) {
		ContentValues values = new ContentValues();
		
		// don't output id if not known 
		if(feature.getId() != null) {
			Log.d(TAG, "featToVal: id = " + String.valueOf(feature.getId()));
			values.put(KEY_ROWID, feature.getId());	
		}	
		
		values.put(KEY_PROD_ID, feature.getProdId());
		values.put(KEY_FEAT_ID, feature.getFeatId());
		values.put(KEY_NAME, feature.getName());
		values.put(KEY_ACTIVE, feature.isActive());
		values.put(KEY_LIMIT_REV, feature.getLimitRev());
        values.put(KEY_CP, feature.getCp());
        values.put(KEY_CPK, feature.getCpk());
		
		return values;
	}
    //endregion

    //region Limit
    // create new limit
	public long createLimit(Limits limit) {
		Log.d(TAG, "createLimit: p; f; r; t = " +
				String.valueOf(limit.getProdId()) 	+ "; " + 
				String.valueOf(limit.getFeatId()) 		+ "; " +
				String.valueOf(limit.getLimitRev()	+ "; " +
				limit.getLimitType().getValue()) );
		// insert row
		return db.insert(TABLE_LIMITS, null, limitToValues(limit));
	};
    	
	// get all Limits by prodId by featId
	public Cursor getAllLimits(long prodId, long featId) {
		// select all query
		String selectQuery = "SELECT * FROM " + TABLE_LIMITS + " WHERE " + 
				KEY_PROD_ID + " = " + Long.toString(prodId) + " AND " +
				KEY_FEAT_ID + " = " + Long.toString(featId);		
		return db.rawQuery(selectQuery, null);		
	};

    // get all Limits by prodId by featId by revision
    public Cursor getAllLimits(long prodId, long featId, long rev) {
        // select all query
        String selectQuery = "SELECT * FROM " + TABLE_LIMITS + " WHERE " +
                KEY_PROD_ID + " = " + Long.toString(prodId) + " AND " +
                KEY_FEAT_ID + " = " + Long.toString(featId) + " AND " +
                KEY_LIMIT_REV + " = " + Long.toString(rev);
        return db.rawQuery(selectQuery, null);
    };

	// get single Limit by prodId and featId
	public Cursor getLimit(long prodId, long featId, long rev, LimitType limitType) {
		Log.d(TAG, "getLimit - prodId: " + prodId + "; featId = " + featId + "; rev = " + rev + "; type = " + limitType.getValue());
		
		Cursor c = db.query(TABLE_LIMITS,  
				new String[] {KEY_ROWID, KEY_PROD_ID, KEY_FEAT_ID, KEY_LIMIT_REV, KEY_LIMIT_TYPE, KEY_UPPER, KEY_LOWER},
				KEY_PROD_ID	 	+ "=" + prodId + " AND " + 
				KEY_FEAT_ID 	+ "=" + featId + " AND " + 
				KEY_LIMIT_REV	+ "=" + rev    + " AND " +
				KEY_LIMIT_TYPE  + "= '" + limitType.getValue() + "'", 
				null, null, null, null, null);
		
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	};
	
	// update single Limit
	public boolean updateLimit(Limits limit) {	
		Log.d(TAG, "updateLimit: prodId; featId; rev; type = " +
				String.valueOf(limit.getProdId()) 	+ "; " + 
				String.valueOf(limit.getFeatId()) 		+ "; " +
				String.valueOf(limit.getLimitRev()	+ "; " +
				limit.getLimitType().getValue()) );		

		Log.d(TAG, "updateLimit: SQL = " + KEY_PROD_ID + "=" + limit.getProdId() + " AND " +
				KEY_FEAT_ID + "=" + limit.getFeatId() + " AND " +
				KEY_LIMIT_REV + "=" + limit.getLimitRev() + " AND " +
				KEY_LIMIT_TYPE + "='" + limit.getLimitType().getValue() + "'");
		
		return db.update(TABLE_LIMITS, limitToValues(limit), 
				KEY_PROD_ID + "=" + limit.getProdId() + " AND " +
				KEY_FEAT_ID + "=" + limit.getFeatId() + " AND " +
				KEY_LIMIT_REV + "=" + limit.getLimitRev() + " AND " +
				KEY_LIMIT_TYPE + "='" + limit.getLimitType().getValue() + "'", null) > 0;
	};	
	
	// delete single Limit
	public boolean deleteLimit(long rowId) {
		return db.delete(TABLE_LIMITS, KEY_ROWID + "=" + rowId, null) > 0; 
	};		
	
	// convert cursor to limit
	public Limits cursorToLimit(Cursor c) {
		Limits limit = new Limits(
				c.getLong(c.getColumnIndex(KEY_ROWID)),
				c.getLong(c.getColumnIndex(KEY_PROD_ID)),
				c.getLong(c.getColumnIndex(KEY_FEAT_ID)),
				c.getLong(c.getColumnIndex(KEY_LIMIT_REV)),
				LimitType.fromValue(c.getString(c.getColumnIndex(KEY_LIMIT_TYPE))),
				c.getDouble(c.getColumnIndex(KEY_UPPER)),
				c.getDouble(c.getColumnIndex(KEY_LOWER)));
		
		return limit;			
	}		
	
	// convert limit to content values
	private ContentValues limitToValues(Limits limit) {
		ContentValues values = new ContentValues();

		Log.d(TAG, "limitToValues: Limit Id, prodId; featId; rev; type = " +
				String.valueOf(limit.getId()) + "; " +
				String.valueOf(limit.getProdId()) 	+ "; " + 
				String.valueOf(limit.getFeatId()) 		+ "; " +
				String.valueOf(limit.getLimitRev()	+ "; " +
		        limit.getLimitType().getValue()) );		
		
		// don't output id if not known 
		if(limit.getId() != null) {
			Log.d(TAG, "limitToValues: output rowid" + String.valueOf(limit.getId()));
			values.put(KEY_ROWID, limit.getId());	
		}		
		values.put(KEY_PROD_ID, limit.getProdId());
		values.put(KEY_FEAT_ID, limit.getFeatId());
		values.put(KEY_LIMIT_REV, limit.getLimitRev());		
		values.put(KEY_LIMIT_TYPE, limit.getLimitType().getValue());
		values.put(KEY_UPPER, limit.getUpper());
		values.put(KEY_LOWER, limit.getLower());
				
		return values;
	}
    //endregion

    //region Piece
    // create new piece
	public long createPiece(Piece piece) {
		Log.d(TAG, "createPiece: prodId; sgId; pieceNum; collDT = " +
				String.valueOf(piece.getProdId()) 	+ "; " + 
				String.valueOf(piece.getSgId()) 		+ "; " +
				String.valueOf(piece.getPieceNum())	+ "; " +
				String.valueOf(piece.getCollectDt()));
				
		// insert row
		return db.insert(TABLE_PIECE, null, pieceToValues(piece));
	};

	// get all pieces by prodId
	public Cursor getAllPieces(long prodId) {
		// select all query
		String selectQuery = "SELECT * FROM " + TABLE_PIECE + " WHERE " + 
				KEY_PROD_ID + " = " + Long.toString(prodId) + 
				" ORDER BY datetime(" + KEY_COLLECT_DATETIME + ") DESC";		
		return db.rawQuery(selectQuery, null);		
	};

	// get all pieces by collect status
	// TODO is export all required?
	public Cursor getAllPieces(CollectStatus collStat) {
		// select all query
		String selectQuery = "SELECT * FROM " + TABLE_PIECE + " WHERE " + 
				KEY_COLLECT_STATUS + " = '" + collStat.getValue() +				
				"' ORDER BY datetime(" + KEY_COLLECT_DATETIME + ") DESC";		
		return db.rawQuery(selectQuery, null);		
	};	
		
	// get all pieces by prodId by collect status
	public Cursor getAllPieces(long prodId, CollectStatus collStat) {
		// select all query
		String selectQuery = "SELECT * FROM " + TABLE_PIECE + " WHERE " + 
				KEY_PROD_ID + " = " + Long.toString(prodId) + " AND " +
				KEY_COLLECT_STATUS + " = '" + collStat.getValue() +				
				"' ORDER BY datetime(" + KEY_COLLECT_DATETIME + ") DESC";		
		return db.rawQuery(selectQuery, null);		
	};	
	
	// get single piece by prodId, sgId and pieceNum
	public Cursor getPiece(long prodId, long sgId, long pieceNum) {
		Cursor c = db.query(TABLE_PIECE,  
				new String[] {KEY_ROWID, KEY_PROD_ID, KEY_SUB_GRP_ID, KEY_PIECE_NUM, 
					KEY_COLLECT_DATETIME, KEY_OPERATOR, KEY_LOT, KEY_COLLECT_STATUS},
				KEY_PROD_ID	 	+ "=" + prodId + " AND " + 
				KEY_SUB_GRP_ID 	+ "=" + sgId + " AND " + 
				KEY_PIECE_NUM	+ "=" + pieceNum, 
				null, null, null, null, null);
		
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	};

	// get single piece by rowId (i.e. pieceId)
	public Cursor getPiece(long rowId) {
		Cursor c = db.query(TABLE_PIECE,  
				new String[] {KEY_ROWID, KEY_PROD_ID, KEY_SUB_GRP_ID, KEY_PIECE_NUM, 
					KEY_COLLECT_DATETIME, KEY_OPERATOR, KEY_LOT, KEY_COLLECT_STATUS},
				KEY_ROWID	 	+ "=" + rowId, 
				null, null, null, null, null);
		
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	};

    // get previous Pieces by prodId and collect date
    public Cursor getPrevPieces(long prodId, Date collDate) {
        // select all query
        String selectQuery = "SELECT * FROM " + TABLE_PIECE + " WHERE " +
                KEY_PROD_ID + " = " + Long.toString(prodId) + " AND " +
                "datetime(" + KEY_COLLECT_DATETIME + ") < datetime('" + DateTimeUtils.getDateTimeStr(collDate) + "')" +
                " ORDER BY datetime(" + KEY_COLLECT_DATETIME + ") DESC";

        Log.d(TAG, "getPrevPieces: query = " + selectQuery);

        return db.rawQuery(selectQuery, null);
    };

	// update single piece
	public boolean updatePiece(Piece piece) {	
		Log.d(TAG, "updatePiece: prodId; sgId; pieceNum; collDT = " +
				String.valueOf(piece.getProdId()) 	+ "; " + 
				String.valueOf(piece.getSgId()) 		+ "; " +
				String.valueOf(piece.getPieceNum())	+ "; " +
				String.valueOf(piece.getCollectDt()));
		
		return db.update(TABLE_PIECE, pieceToValues(piece), 
				KEY_ROWID + "=" + piece.getId(), null) > 0; 
	};	
	
	// delete single piece
	public boolean deletePiece(long rowId) {
		
		// delete all associated Measurements
		Cursor cMeas = getAllMeasurements(rowId);		
		if (cMeas.moveToFirst()) {
			do {				
				deleteMeasurement(cMeas.getInt(cMeas.getColumnIndex(KEY_ROWID)));				
			} while(cMeas.moveToNext());
		}
        cMeas.close();
		
		return db.delete(TABLE_PIECE, KEY_ROWID + "=" + rowId, null) > 0; 
	};		
	
	// convert cursor to piece
	public Piece cursorToPiece(Cursor c) {

		Piece piece = new Piece(
				c.getLong(c.getColumnIndex(KEY_ROWID)),
				c.getLong(c.getColumnIndex(KEY_PROD_ID)),
				c.getLong(c.getColumnIndex(KEY_SUB_GRP_ID)),
				c.getLong(c.getColumnIndex(KEY_PIECE_NUM)),
				DateTimeUtils.getDate(c.getString(c.getColumnIndex(KEY_COLLECT_DATETIME))),
				c.getString(c.getColumnIndex(KEY_OPERATOR)),
				c.getString(c.getColumnIndex(KEY_LOT)),
				CollectStatus.fromValue(c.getString(c.getColumnIndex(KEY_COLLECT_STATUS))));							
		
		Log.d(TAG, "cursorToPiece, piece id = " + piece.getId());
		Log.d(TAG, "cursorToPiece, piece st = " + piece.getStatus());
		
		return piece;			
	}		
	
	// convert piece to content values
	private ContentValues pieceToValues(Piece piece) {
		ContentValues values = new ContentValues();

		// don't output id if not known 
		if(piece.getId() != null) {
			Log.d(TAG, "pieceToValues: output rowid" + String.valueOf(piece.getId()));
			values.put(KEY_ROWID, piece.getId());	
		}		
				
		values.put(KEY_PROD_ID, piece.getProdId());
		
		// don't output sgId if not known 
		if(piece.getSgId() != null) {
			Log.d(TAG, "pieceToValues: output sgId" + String.valueOf(piece.getSgId()));
			values.put(KEY_SUB_GRP_ID, piece.getSgId());	
		}		
				
		values.put(KEY_PIECE_NUM, piece.getPieceNum());
		values.put(KEY_COLLECT_DATETIME, DateTimeUtils.getDateTimeStr(piece.getCollectDt()));
		values.put(KEY_OPERATOR, piece.getOperator());
		values.put(KEY_LOT, piece.getLot());
		values.put(KEY_COLLECT_STATUS, piece.getStatus().getValue());
				
		return values;
	}
    //endregion

    //region Measurement
    // create new measurement
	public long createMeasurement(Measurement meas) {
		Log.d(TAG, "createMeas pieceId; featId; value = " + 
				String.valueOf(meas.getPieceId()) 	+ "; " + 
				String.valueOf(meas.getFeatId()) 	+ "; " +
				String.valueOf(meas.getValue()));
				
		// insert row
		return db.insert(TABLE_MEASUREMENT, null, measurementToValues(meas));
	};
    	
	// get all measurements by pieceId
	public Cursor getAllMeasurements(long pieceId) {
		// select all query
		String selectQuery = "SELECT * FROM " + TABLE_MEASUREMENT + " WHERE " + 
				KEY_PIECE_ID + " = " + Long.toString(pieceId) + 
				" ORDER BY datetime(" + KEY_COLLECT_DATETIME + ")";		
		return db.rawQuery(selectQuery, null);		
	};

	// get single measurement by pieceId, prodId and featId
	public Cursor getMeasurement(long pieceId, long prodId, long featId) {
		Cursor c = db.query(TABLE_MEASUREMENT,  
				new String[] {KEY_ROWID, KEY_PIECE_ID, KEY_PROD_ID, KEY_FEAT_ID, 
					KEY_COLLECT_DATETIME, KEY_OPERATOR, KEY_VALUE, KEY_RANGE, KEY_CAUSE, KEY_LIMIT_REV, KEY_IN_CONTROL, KEY_IN_ENG_LIM},
				KEY_PIECE_ID	+ "=" + pieceId	+ " AND " +	
				KEY_PROD_ID	 	+ "=" + prodId 	+ " AND " + 
				KEY_FEAT_ID 	+ "=" + featId, 
				null, null, null, null, null);
		
		if (c != null) {
			c.moveToFirst();
		}
		
		return c;
	};

	// update single measurement
	public boolean updateMeasurement(Measurement meas) {	
		Log.d(TAG, "updateMeas pieceId = " + meas.getPieceId() +
                "; featId = " + meas.getFeatId() +
                "; value = " + meas.getValue() +
                "; range = " + meas.getRange() +
                "; inCtrl = " + meas.isInControl() +
                "; cause = " + meas.getCause());

		return db.update(TABLE_MEASUREMENT, measurementToValues(meas), 
				KEY_ROWID + "=" + meas.getId(), null) > 0; 
	};	
	
	// delete single measurement
	public boolean deleteMeasurement(long rowId) {
		return db.delete(TABLE_MEASUREMENT, KEY_ROWID + "=" + rowId, null) > 0; 
	};		
	
	// TODO move to entity class (along with others)
	// convert cursor to measurement
	public Measurement cursorToMeasurement(Cursor c) {

		Measurement meas = new Measurement(
				c.getLong(c.getColumnIndex(KEY_ROWID)),
				c.getLong(c.getColumnIndex(KEY_PIECE_ID)),
				c.getLong(c.getColumnIndex(KEY_PROD_ID)),
				c.getLong(c.getColumnIndex(KEY_FEAT_ID)),
				DateTimeUtils.getDate(c.getString(c.getColumnIndex(KEY_COLLECT_DATETIME))),
				c.getString(c.getColumnIndex(KEY_OPERATOR)),
				c.getDouble(c.getColumnIndex(KEY_VALUE)),
                c.getDouble(c.getColumnIndex(KEY_RANGE)),
                c.getLong(c.getColumnIndex(KEY_CAUSE)),
				c.getLong(c.getColumnIndex(KEY_LIMIT_REV)),
				intToBool(c.getInt(c.getColumnIndex(KEY_IN_CONTROL))),
				intToBool(c.getInt(c.getColumnIndex(KEY_IN_ENG_LIM))));							
		
		return meas;			
	}		

	// TODO move to entity class (along with others)	
	// convert measurement to content values
	private ContentValues measurementToValues(Measurement meas) {
		ContentValues values = new ContentValues();

		// don't output id if not known 
		if(meas.getId() != null) {
			Log.d(TAG, "measToVal: id" + String.valueOf(meas.getId()));
			values.put(KEY_ROWID, meas.getId());	
		}		
		
		Log.d(TAG, "meaToVal - InCtrl = " + meas.isInControl() + "; cause = " + meas.getCause());
		
		values.put(KEY_PIECE_ID, meas.getPieceId());
		values.put(KEY_PROD_ID, meas.getProdId());
		values.put(KEY_FEAT_ID, meas.getFeatId());
		values.put(KEY_COLLECT_DATETIME, DateTimeUtils.getDateTimeStr(meas.getCollectDt()));
		values.put(KEY_OPERATOR, meas.getOperator());
		values.put(KEY_VALUE, meas.getValue());
        values.put(KEY_RANGE, meas.getRange());
        values.put(KEY_CAUSE, meas.getCause());
		values.put(KEY_LIMIT_REV, meas.getLimitRev());
		values.put(KEY_IN_CONTROL, boolToInt(meas.isInControl()));
		values.put(KEY_IN_ENG_LIM, boolToInt(meas.isInEngLim()));
								
		return values;
	}
    //endregion

    //region SimpleCode
    // create new Simple Code
    public long createSimpleCode(SimpleCode code) {
        Log.d(TAG, "createSimpleCode: Id = " + String.valueOf(code.getId()));
        // insert row
        return db.insert(TABLE_SIMPLE_CODE, null, simpleCodeToValues(code));
    };

    // get all Simple Code for type
    public Cursor getAllSimpleCode(String type) {
        // select query
        String selectQuery = "SELECT * FROM " + TABLE_SIMPLE_CODE + " WHERE " + KEY_TYPE + " = '" + type + "' ORDER BY " + KEY_CODE;

        return db.rawQuery(selectQuery, null);
    };

    // get single Simple Code
    public Cursor getSimpleCode(long rowId) {
        Cursor c = db.query(TABLE_SIMPLE_CODE,
                new String[] {KEY_ROWID, KEY_TYPE, KEY_CODE, KEY_DESCRIPTION, KEY_INT_CODE, KEY_ACTIVE},
                KEY_ROWID + "=" + rowId,
                null, null, null, null, null);

        if (c != null) {
            c.moveToFirst();
        }

        return c;
    };

    // get single Simple Code by Internal Code
    public Cursor getSimpleCodeByTypeIntCode(String type, String intCode) {
        Cursor c = db.query(TABLE_SIMPLE_CODE,
                new String[] {KEY_ROWID, KEY_TYPE, KEY_CODE, KEY_DESCRIPTION, KEY_INT_CODE, KEY_ACTIVE},
                KEY_TYPE + "='" + type + "' AND " + KEY_INT_CODE + "='" + intCode + "'",
                null, null, null, null, null);

        if (c != null) {
            c.moveToFirst();
        }

        return c;
    };

    // get Simple Code count
    public int getSimpleCodeCount(String type) {
        return getAllSimpleCode(type).getCount();
    };

    // update single Simple Code
    public boolean updateSimpleCode(SimpleCode code) {
        Log.d(TAG, "updateSimpleCode: Id = " + String.valueOf(code.getId()));
        return db.update(TABLE_SIMPLE_CODE, simpleCodeToValues(code), KEY_ROWID + "=" + code.getId(), null) > 0;
    };

    // delete single Simple Code
    public boolean deleteSimpleCode(long rowId) {

        return db.delete(TABLE_SIMPLE_CODE, KEY_ROWID + "=" + rowId, null) > 0;
    };

    // convert cursor to Simple Code
    public SimpleCode cursorToSimpleCode(Cursor c) {
        SimpleCode code = new SimpleCode(
                c.getInt(c.getColumnIndex(KEY_ROWID)),
                c.getString(c.getColumnIndex(KEY_TYPE)),
                c.getString(c.getColumnIndex(KEY_CODE)),
                c.getString(c.getColumnIndex(KEY_DESCRIPTION)),
                c.getString(c.getColumnIndex(KEY_INT_CODE)),
                Boolean.parseBoolean(c.getString(c.getColumnIndex(KEY_ACTIVE))));

        // return code
        return code;
    }

    // convert simple code to content values
    private ContentValues simpleCodeToValues(SimpleCode code) {
        ContentValues values = new ContentValues();

        values.put(KEY_ROWID, code.getId());
        values.put(KEY_TYPE, code.getType());
        values.put(KEY_CODE, code.getCode());
        values.put(KEY_DESCRIPTION, code.getDescription());
        values.put(KEY_INT_CODE, code.getIntCode());
        values.put(KEY_ACTIVE, code.isActive());

        return values;
    }
    //endregion

	// convert boolean to int
	public static int boolToInt(boolean boolVal) {
		return (boolVal)? 1 : 0;		
	}

	// convert int to boolean
	public static boolean intToBool(int intVal) {
		return (intVal == 1)? true : false;		
	}

}
