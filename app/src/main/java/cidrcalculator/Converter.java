
package cidrcalculator;

import android.app.Activity;
import android.content.Intent;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;


import static cidrcalculator.R.id.keyboardview;

public class Converter extends Activity {

    private static final String TAG = Converter.class.getSimpleName();
    private static final boolean debug = false;

	public static final String EXTRA_IP="IP";
	
	private String currentIP;
    private EditText ipAddress;
	private EditText ipBinary;
	private EditText ipHex;
	Animation pulseAnim = null;

	public static final int RETURNIP_MENUID = Menu.FIRST;

    CustomKeyboard mCustomKeyboard;

    @Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		if (debug) Log.d(TAG,"onCreate()");

		pulseAnim = AnimationUtils.loadAnimation( this, R.anim.pulse );

		setContentView(R.layout.converter);



        RelativeLayout layout = (RelativeLayout) findViewById(R.id.converter_outer_layout);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        KeyboardView keyboard = new KeyboardView(this, null);
        keyboard.setId(keyboardview);
        keyboard.setLayoutParams(params);
        keyboard.setFocusable(true);
        keyboard.setFocusableInTouchMode(true);
        keyboard.setVisibility(View.GONE);
        layout.addView(keyboard);

        mCustomKeyboard= new CustomKeyboard(this, keyboardview, R.xml.hexkbd );
        mCustomKeyboard.registerEditText(R.id.converter_ipaddress);
        mCustomKeyboard.registerEditText(R.id.ipbinary);
        mCustomKeyboard.registerEditText(R.id.iphex);

        ipAddress = (EditText) findViewById(R.id.converter_ipaddress);
        ipBinary = (EditText) findViewById(R.id.ipbinary);
        ipHex = (EditText) findViewById(R.id.iphex);


        currentIP = icicle != null ? icicle.getString(Converter.EXTRA_IP) : null;
		if (currentIP == null) {
		    Bundle extras = getIntent().getExtras();            
		    currentIP = extras != null ? extras.getString(Converter.EXTRA_IP) : null;
		}
		if (debug) Log.d(TAG,"onCreate: currentIP="+currentIP);
		
		if ((currentIP!=null) && (currentIP.length()>0)) {
			ipAddress.setText(currentIP);
			convertDecimal();
		}

		Button convertDecButton = (Button) findViewById(R.id.convertdec);
		convertDecButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				// Don't allow the user to enter a blank name, we need
				// something useful to show in the list
				if(ipAddress.getText().toString().trim().length() == 0) {
					Toast.makeText(Converter.this, R.string.err_bad_ip,
							Toast.LENGTH_SHORT).show();
					return;
				}
				convertDecimal();
				ipBinary.startAnimation(pulseAnim);
				ipHex.startAnimation(pulseAnim);
			}
		});
		Button convertBinButton = (Button) findViewById(R.id.convertbin);
		convertBinButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				// Don't allow the user to enter a blank name, we need
				// something useful to show in the list
				if(ipBinary.getText().toString().trim().length() == 0) {
					Toast.makeText(Converter.this, R.string.err_bad_ip,
							Toast.LENGTH_SHORT).show();
					return;
				}
				convertBinary();
				ipAddress.startAnimation(pulseAnim);
				ipHex.startAnimation(pulseAnim);
			}
		});
		Button convertHexButton = (Button) findViewById(R.id.converthex);
		convertHexButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				// Don't allow the user to enter a blank name, we need
				// something useful to show in the list
				if(ipHex.getText().toString().trim().length() == 0) {
					Toast.makeText(Converter.this, R.string.err_bad_ip,
							Toast.LENGTH_SHORT).show();
					return;
				}
				convertHex();
				ipAddress.startAnimation(pulseAnim);
				ipBinary.startAnimation(pulseAnim);
			}
		});
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);
     
    	menu.add(Menu.NONE, RETURNIP_MENUID , Menu.NONE, R.string.return_ip)
    		.setIcon(android.R.drawable.ic_menu_revert);
    	return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
    	switch (item.getItemId()) {
    	case RETURNIP_MENUID:
    		if (currentIP!=null) {
    			Intent i=new Intent();
    			i.putExtra(EXTRA_IP, currentIP);
    			setResult(RESULT_OK, i);
    		} else {
    			setResult(RESULT_CANCELED);
    		}
    		finish();
    		return true;
    	}
    	return false;
    }

	private void convertDecimal() {
		String decimalIP=ipAddress.getText().toString().trim();
		if (debug) Log.d(TAG,"convertDecimal: decimalIP="+decimalIP);

		int ip32bit;
		try {
			ip32bit = CIDRCalculator.stringIPtoInt(decimalIP);
		} catch (Exception e) {
			Toast.makeText(Converter.this, R.string.err_bad_ip,
					Toast.LENGTH_SHORT).show();
			return;
		}
		currentIP=decimalIP;
		String ip=convertIPIntDec2StringBinary(ip32bit);
		ipBinary.setText(ip);
		String hexIP=convertIPIntDec2StringHex(ip32bit);
		ipHex.setText(hexIP);
	}
	
	private void convertBinary() {
        String currentBinary = ipBinary.getText().toString().trim();
		if (debug) Log.d(TAG,"convertBinary: currentBinary="+ currentBinary);
		
		if (currentBinary.length() < 32) {
			Toast.makeText(Converter.this, R.string.err_bad_ip,
					Toast.LENGTH_SHORT).show();
			if (debug) Log.d(TAG,"convertToBinary: less than 32");
			return;
		}
		try {
			String octet1b = currentBinary.substring(0, 8);
			if (debug) Log.d(TAG,"convertToBinary: octet1b="+octet1b);
			String octet2b = currentBinary.substring(9, 17);
			if (debug) Log.d(TAG,"convertToBinary: octet2b="+octet2b);
			String octet3b = currentBinary.substring(18, 26);
			if (debug) Log.d(TAG,"convertToBinary: octet3b="+octet3b);
			String octet4b = currentBinary.substring(27, 35);
			if (debug) Log.d(TAG,"convertToBinary: octet4b="+octet4b);

			long octet1i = Integer.parseInt(octet1b,2);
			long octet2i = Integer.parseInt(octet2b,2);
			long octet3i = Integer.parseInt(octet3b,2);
			long octet4i = Integer.parseInt(octet4b,2);
			currentIP=octet1i +"."+
				octet2i +"."+
				octet3i +"."+
				octet4i;
			ipAddress.setText(currentIP);
			int ip32bit;
			ip32bit = CIDRCalculator.stringIPtoInt(currentIP);
			String hexIP=convertIPIntDec2StringHex(ip32bit);
			ipHex.setText(hexIP);
		} catch (NumberFormatException e) {
			Toast.makeText(Converter.this, R.string.err_bad_ip,
					Toast.LENGTH_SHORT).show();
			if (debug) Log.d(TAG,"convertToBinary: numberFormatException");
		} catch (StringIndexOutOfBoundsException e) {
			Toast.makeText(Converter.this, R.string.err_bad_ip,
					Toast.LENGTH_SHORT).show();
			if (debug) Log.d(TAG,"convertToBinary: StringIndexOutOfBoundsException");
		} catch (Exception e) {
			Toast.makeText(Converter.this, R.string.err_bad_ip,
					Toast.LENGTH_SHORT).show();
			if (debug) Log.d(TAG,"convertToBinary: Exception");
		}

	}
	
	private void convertHex() {
		String hexIP=ipHex.getText().toString().trim();
		if (debug) Log.d(TAG,"convertHex: hexIP="+hexIP);

		if (hexIP.length() < 11) {
			Toast.makeText(Converter.this, R.string.err_bad_ip,
					Toast.LENGTH_SHORT).show();
			if (debug) Log.d(TAG,"convertHex: less than 11");
			return;
		}
		try {
			String octet1b = hexIP.substring(0,2);
			if (debug) Log.d(TAG,"convertHex: octet1b="+octet1b);
			String octet2b = hexIP.substring(3,5);
			if (debug) Log.d(TAG,"convertHex: octet2b="+octet2b);
			String octet3b = hexIP.substring(6,8);
			if (debug) Log.d(TAG,"convertHex: octet3b="+octet3b);
			String octet4b = hexIP.substring(9,11);
			if (debug) Log.d(TAG,"convertHex: octet4b="+octet4b);

			long octet1i = Integer.parseInt(octet1b,16);
			long octet2i = Integer.parseInt(octet2b,16);
			long octet3i = Integer.parseInt(octet3b,16);
			long octet4i = Integer.parseInt(octet4b,16);
			currentIP=octet1i +"."+
				octet2i +"."+
				octet3i +"."+
				octet4i;
			ipAddress.setText(currentIP);
			int ip32bit;
			ip32bit = CIDRCalculator.stringIPtoInt(currentIP);
			String binaryIP=convertIPIntDec2StringBinary(ip32bit);
			ipBinary.setText(binaryIP);
		} catch (NumberFormatException e) {
			Toast.makeText(Converter.this, R.string.err_bad_ip,
					Toast.LENGTH_SHORT).show();
			if (debug) Log.d(TAG,"convertHex: numberFormatException");
		} catch (StringIndexOutOfBoundsException e) {
			Toast.makeText(Converter.this, R.string.err_bad_ip,
					Toast.LENGTH_SHORT).show();
			if (debug) Log.d(TAG,"convertHex: StringIndexOutOfBoundsException");
		} catch (Exception e) {
			Toast.makeText(Converter.this, R.string.err_bad_ip,
					Toast.LENGTH_SHORT).show();
			if (debug) Log.d(TAG,"convertHex: Exception");
		}

	}
	
	public static String convertIPIntDec2StringBinary(int intIP) {
		if (debug) Log.d(TAG,"convertIPIntDec2StringBinary("+intIP+")");
		String stringIP;
		stringIP = Integer.toBinaryString(intIP);
		int length=stringIP.length();
		if (length<32) {
			int prependZeros=32-length;
			for (int i=0; i<prependZeros; i++) {
				stringIP="0"+stringIP;
			}
		}
		String octet1 = stringIP.substring(0,8);
		String octet2 = stringIP.substring(8,16);
		String octet3 = stringIP.substring(16,24);
		String octet4 = stringIP.substring(24,32);
		stringIP = octet1 +"."+octet2+"."+octet3+"."+octet4;
		return stringIP;
	}

	public static String convertIPIntDec2StringHex(int intIP) {
		if (debug) Log.d(TAG,"convertIPIntDec2StringBinary("+intIP+")");
		String stringIP;
		stringIP = Integer.toHexString(intIP);
		int length=stringIP.length();
		if (length<8) {
			int prependZeros=8-length;
			for (int i=0; i<prependZeros; i++) {
				stringIP="0"+stringIP;
			}
		}
		String octet1 = stringIP.substring(0,2);
		String octet2 = stringIP.substring(2,4);
		String octet3 = stringIP.substring(4,6);
		String octet4 = stringIP.substring(6,8);
		stringIP = octet1 +"."+octet2+"."+octet3+"."+octet4;
		return stringIP;
	}
}
