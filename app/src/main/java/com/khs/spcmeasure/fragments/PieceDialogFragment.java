package com.khs.spcmeasure.fragments;

import java.util.Date;

import com.khs.spcmeasure.DBAdapter;
import com.khs.spcmeasure.R;
import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.fragments.MntSetupFragment.OnSetupSelectedListener;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.CollectStatus;
import com.khs.spcmeasure.library.DateTimeUtils;
import com.khs.spcmeasure.library.ProgressUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PieceDialogFragment extends DialogFragment implements OnClickListener{

	private Long mProdId;
	private Date mCollectDate;
	private String mCollectDtStr;
	private OnNewPieceListener mListener;
	
	Button btnOkay, btnCancel;
	EditText edtLot, edtOperator;

	// container activity must implement this interface
	public interface OnNewPieceListener {
		public void onNewPieceCreated(Long pieceId);
	}
			
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// ensure host Activity implements the OnNewPieceListener interface
		try {
			mListener = (OnNewPieceListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnNewPieceListener");
		}		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		
		// unpack arguments	
		Bundle args = getArguments();
		if (args.containsKey(DBAdapter.KEY_PROD_ID)) {
			mProdId = args.getLong(DBAdapter.KEY_PROD_ID);
		}
		
		// verify arguments and exit upon error
		if (mProdId == null) {
			AlertUtils.errorDialogShow(getActivity(), "Product Id is unknown.  Please try again.");						
			dismiss();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		// inflate custom view
		View v = inflater.inflate(R.layout.piece_dialog_fragment, container, false);
				
		// extract widgets
		edtLot = (EditText) v.findViewById(R.id.edtLot);
		edtOperator = (EditText) v.findViewById(R.id.edtOperator);
		btnOkay = (Button) v.findViewById(R.id.btnPieceOk);
		btnCancel = (Button) v.findViewById(R.id.btnPieceCancel);		
		
		// TODO is this required to make it look more like a dialog?
		Dialog myDialog = getDialog();
		myDialog.setTitle("New Piece");
		
		// extract Product
		// TODO need to handle situation when Prod Id is null.
		DBAdapter db = new DBAdapter(getActivity());
        db.open();
		Cursor c = db.getProduct(mProdId);
		Product p = db.cursorToProduct(c);
		db.close();
		
		// populate dialog widget values
		TextView txtProdName = (TextView) v.findViewById(R.id.txtProdName);
		txtProdName.setText(p.getName());
		
		mCollectDate = new Date();
		mCollectDtStr = DateTimeUtils.getDateTimeStr(mCollectDate);
		TextView txtCollectDt = (TextView) v.findViewById(R.id.txtCollectDt);
		txtCollectDt.setText(mCollectDtStr);
						
		// set button listeners
		btnOkay.setOnClickListener(this);
		btnCancel.setOnClickListener(this);		
		
		return v;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnPieceOk:
			// validate fields
			if (validateFields() == false) {
				return;
			}
			
			// create progress dialog
			ProgressDialog progDiag = ProgressUtils.progressDialogCreate(getActivity(), "Creating Piece...");
			
			// TODO use try catch
			try {
				// show progress dialog
				progDiag.show();
				
				// open the DB
				DBAdapter db = new DBAdapter(getActivity());
				db.open();
				
				// create Piece
				long pieceNum = 1;
				Piece piece =  new Piece(mProdId, pieceNum, mCollectDate, edtOperator.getText().toString(), edtLot.getText().toString(), CollectStatus.OPEN);
				
				// insert Piece into the DB
				long pieceId = db.createPiece(piece);
								
				// close the DB			
				db.close();			
							
				dismiss();
				
				if (pieceId < 0) {
					AlertUtils.errorDialogShow(getActivity(), "New Piece create failed.  Contact administrator.");
				} else {
					// inform the Activity of the new Setup
					mListener.onNewPieceCreated(pieceId);
				}				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				// close progress dialog
				progDiag.dismiss();
			}
				
			Toast.makeText(getActivity(), "OKAY", Toast.LENGTH_LONG).show();
			break;
		case R.id.btnPieceCancel:
			dismiss();
			Toast.makeText(getActivity(), "New Piece cancelled", Toast.LENGTH_LONG).show();
			break;			
		}
	}

	// returns true if all on-screen fields are valid, otherwise false
	private boolean validateFields() {
		
		// validate operator
		if (edtOperator.getText().length() == 0) {
			AlertUtils.errorDialogShow(getActivity(), "Operator not specified.  Please try again.");
			edtOperator.requestFocus();
			return false;
		}
					
		// validate lot
		if (edtLot.getText().length() != 6) {
			AlertUtils.errorDialogShow(getActivity(), "Lot Number is invalid.  Please try again.");				
			edtLot.requestFocus();
			return false;
		}			
				
		return true;
	}
	
}
