package com.khs.spcmeasure;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.khs.spcmeasure.entity.Piece;
import com.khs.spcmeasure.entity.Product;
import com.khs.spcmeasure.library.AlertUtils;
import com.khs.spcmeasure.library.BarCodeUtils;
import com.khs.spcmeasure.library.CollectStatus;
import com.khs.spcmeasure.library.DateTimeUtils;
import com.khs.spcmeasure.library.ProgressUtils;
import com.khs.spcmeasure.library.SecurityUtils;

import java.security.Security;
import java.util.Date;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

// captures new piece details and launches measurement activity
public class PieceDialogFragment extends DialogFragment implements OnClickListener{

	private Long mProdId;
	private Date mCollectDate;
	private String mCollectDtStr;
	private OnNewPieceListener mListener;
	
	Button btnScan, btnOkay, btnCancel;
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
			throw new ClassCastException(activity.toString() + getString(R.string.text_must_implement_onnewpiecelistener));
		}		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);

		// check security
		if (!SecurityUtils.checkSecurity(getActivity(), true)) {
			dismiss();
		}

		// unpack arguments	
		Bundle args = getArguments();
		if (args.containsKey(DBAdapter.KEY_PROD_ID)) {
			mProdId = args.getLong(DBAdapter.KEY_PROD_ID);
		}
		
		// verify arguments and exit upon error
		if (mProdId == null) {
			AlertUtils.errorDialogShow(getActivity(), getString(R.string.text_mess_prod_id_invalid));
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
        btnScan = (Button) v.findViewById(R.id.btnScan);
		btnOkay = (Button) v.findViewById(R.id.btnPieceOk);
		btnCancel = (Button) v.findViewById(R.id.btnPieceCancel);		
		
		// TODO is this required to make it look more like a dialog?
		Dialog myDialog = getDialog();
		myDialog.setTitle(getString(R.string.title_new_piece));
		
		// extract Product
		// TODO need to handle situation when Prod Id is null.
		DBAdapter db = new DBAdapter(getActivity());
        db.open();
		Cursor c = db.getProduct(mProdId);
		Product p = db.cursorToProduct(c);
        c.close();
		db.close();
		
		// populate dialog widget values
		TextView txtProdName = (TextView) v.findViewById(R.id.txtProdName);
		txtProdName.setText(p.getName());
		
		mCollectDate = new Date();
		mCollectDtStr = DateTimeUtils.getDateTimeStr(mCollectDate);
		TextView txtCollectDt = (TextView) v.findViewById(R.id.txtCollectDt);
		txtCollectDt.setText(mCollectDtStr);

		// populate Operator
		edtOperator.setText(SecurityUtils.getUsername(getActivity()));

		// set button listeners
        btnScan.setOnClickListener(this);
		btnOkay.setOnClickListener(this);
		btnCancel.setOnClickListener(this);		
		
		return v;
	}

	// respond to clicks
	@Override
	public void onClick(View v) {

		switch (v.getId()) {
			case R.id.btnScan:
                // scan
                IntentIntegrator scanIntegrator = new IntentIntegrator(this);
                scanIntegrator.addExtra("PROMPT_MESSAGE", "Scan Serial Number");
                scanIntegrator.addExtra("SCAN_FORMATS", "CODE_39,CODE_128");    // code 3 of 9 and code 128 only
                scanIntegrator.initiateScan();
                break;
            case R.id.btnPieceOk:
				// validate fields
				if (validateFields() == false) {
					return;
				}

				// create progress dialog
				ProgressDialog progDiag = ProgressUtils.progressDialogCreate(getActivity(), getString(R.string.text_creating_piece));

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
						AlertUtils.errorDialogShow(getActivity(), getString(R.string.text_piece_create_failed));
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

				// Toast.makeText(getActivity(), "OKAY", Toast.LENGTH_LONG).show();
				break;
			case R.id.btnPieceCancel:
				dismiss();
				Toast.makeText(getActivity(), getString(R.string.text_piece_create_cancelled), Toast.LENGTH_LONG).show();
				break;
		}
	}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // retrieve scan result
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (scanningResult != null) {
            // we have a result
            String scanContent = scanningResult.getContents();
            String scanFormat = scanningResult.getFormatName();

            // formatTxt.setText("FORMAT: " + scanFormat);

			// extract serial number
			String serial = BarCodeUtils.getSerialNumber(scanContent);

			if (serial != null) {
				edtLot.setText(serial.substring(0, BarCodeUtils.LOT_NUMBER_LENGTH - 1));
            } else {
				AlertUtils.errorDialogShow(getActivity(), "Not a Serial: " + scanContent);
				// TODO remove later as not required
                // Toast toast = Toast.makeText(getActivity(), "Not a Serial: " + scanContent, Toast.LENGTH_LONG);
                // toast.show();
            }
        } else {
			AlertUtils.errorDialogShow(getActivity(), "No scan data received!");
			// TODO remove later as not required
            // Toast toast = Toast.makeText(getActivity(), "No scan data received!", Toast.LENGTH_LONG);
            // toast.show();
        }

    }

	// returns true if all on-screen fields are valid, otherwise false
	private boolean validateFields() {
		
		// validate operator
		if (edtOperator.getText().length() == 0) {
			AlertUtils.errorDialogShow(getActivity(), getString(R.string.text_not_set_operator));
			edtOperator.requestFocus();
			return false;
		}
					
		// validate lot
		if (edtLot.getText().length() != 6) {
			AlertUtils.errorDialogShow(getActivity(), getString(R.string.text_not_set_lot));
			edtLot.requestFocus();
			return false;
		}			
				
		return true;
	}


}
