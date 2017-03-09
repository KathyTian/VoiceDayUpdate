package com.iflytek.voicedemo;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import com.emokit.sdk.util.SDKAppInit;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.LexiconListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.cloud.util.ContactManager;
import com.iflytek.cloud.util.ContactManager.ContactListener;
import com.iflytek.speech.setting.IatSettings;
import com.iflytek.speech.util.ApkInstaller;
import com.iflytek.speech.util.FucUtil;
import com.iflytek.speech.util.JsonParser;
import com.iflytek.sunflower.FlowerCollector;

public class IatDemo extends Activity implements OnClickListener {
	private static String TAG = IatDemo.class.getSimpleName();
	// 语音听写对象
	private SpeechRecognizer mIat;
	// 语音听写UI
	private RecognizerDialog mIatDialog;
	// 用HashMap存储听写结果
	private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

	private EditText mResultText;
	private Toast mToast;
	private SharedPreferences mSharedPreferences;
	private SharedPreferences msharedPre;  //自己创建的sharedPreferences
	// 引擎类型
	private String mEngineType = SpeechConstant.TYPE_CLOUD;
	// 语记安装助手类
	ApkInstaller mInstaller;
	private MySQLiteHelper myHelper;
	private TextView tv1;
	String resultText="";
	String resultKeyWord="";
    private ProgressDialog pdialog;
// sentimental analysis
//Handler mainhandler = new Handler() {
//	@Override
//	public void handleMessage(Message msg) {
//		switch (msg.what) {
//			case 1901://(String) msg.obj,为所第三方api获得的结果
//				tv1.setTextColor(Color.RED);
//				tv1.setText(resultKeyWord);
//                mResultText.setText(resultText);
//                mResultText.setSelection(mResultText.length());
//				break;
//			default:
//				break;
//		}
//	};
//};
	@SuppressLint("ShowToast")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.iatdemo);
		SDKAppInit.createInstance(this);

		myHelper = new MySQLiteHelper(this,"my.db",null,1);
		msharedPre=getSharedPreferences("LoadParameter", Context.MODE_PRIVATE);
		String flag=msharedPre.getString("isFirst", "false");
		if(flag.equals("false"))//第一次加载
		{
			insertDataInNegation(myHelper);
			insertDataInNegative(myHelper);
			insertDataInPositive(myHelper);
			SharedPreferences.Editor editor= msharedPre.edit();
			editor.putString("isFirst", "true");
			editor.commit();
		}
		tv1=(TextView)findViewById(R.id.tv1);
		initLayout();
		// 初始化识别无UI识别对象
		// 使用SpeechRecognizer对象，可根据回调消息自定义界面；
		mIat = SpeechRecognizer.createRecognizer(IatDemo.this, mInitListener);
		
		// 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
		// 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
		mIatDialog = new RecognizerDialog(IatDemo.this, mInitListener);

		mSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME,
				Activity.MODE_PRIVATE);
		mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
		mResultText = ((EditText) findViewById(R.id.iat_text));
		mInstaller = new ApkInstaller(IatDemo.this);
	}
//*******************************sentimental analysis******************************************
	/* Insert data in negation_table
	*/
	public void insertDataInNegation(MySQLiteHelper myHelper){
		SQLiteDatabase db = myHelper.getWritableDatabase();

		ContentValues values=new ContentValues();
		values.put("id", 1);
		values.put("value", "not");
		values.put("flag", "y");
		db.insert("negation_table", null, values);
		values.clear();

		values.put("id", 2);
		values.put("value", "no");
		values.put("flag", "y");
		db.insert("negation_table", null, values);
		values.clear();

		values.put("id", 3);
		values.put("value", "hardly");
		values.put("flag", "y");
		db.insert("negation_table", null, values);
		values.clear();

		values.put("id", 4);
		values.put("value", "scarcely");
		values.put("flag", "y");
		db.insert("negation_table", null, values);
		values.clear();

		values.put("id", 5);
		values.put("value", "seldom");
		values.put("flag", "y");
		db.insert("negation_table", null, values);
		values.clear();

		values.put("id", 6);
		values.put("value", "don't");
		values.put("flag", "y");
		db.insert("negation_table", null, values);
		values.clear();

		values.put("id", 7);
		values.put("value", "doesn't");
		values.put("flag", "y");
		db.insert("negation_table", null, values);
		values.clear();


		values.put("id", 8);
		values.put("value", "didn't");
		values.put("flag", "y");
		db.insert("negation_table", null, values);
		values.clear();

		values.put("id", 9);
		values.put("value", "haven't");
		values.put("flag", "y");
		db.insert("negation_table", null, values);
		values.clear();

		db.close();

	}

	/**
	 * Insert data in positive_table
	 */
	public void insertDataInPositive(MySQLiteHelper myHelper){
		SQLiteDatabase db = myHelper.getWritableDatabase();

		/*db.execSQL("create table if not exists positive_table("
				+ "id integer,"
				+ "value varchar primary key,"
				+ "flag integer)");*/

		ContentValues values=new ContentValues();
		values.put("id", 1);
		values.put("value", "relaxed");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 2);
		values.put("value", "happy");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 3);
		values.put("value", "bright");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 4);
		values.put("value", "free");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 5);
		values.put("value", "comfortable");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 6);
		values.put("value", "pleased");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 7);
		values.put("value", "good");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 8);
		values.put("value", "fine");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 9);
		values.put("value", "easy");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 10);
		values.put("value", "wonderful");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 11);
		values.put("value", "frisky");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();


		values.put("id", 12);
		values.put("value", "excited");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 13);
		values.put("value", "thrilled");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 14);
		values.put("value", "fascinated");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 15);
		values.put("value", "enthusiastic");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 16);
		values.put("value", "inspired");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 17);
		values.put("value", "warm");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 18);
		values.put("value", "lucky");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 19);
		values.put("value", "great");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 20);
		values.put("value", "optimistic");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 21);
		values.put("value", "reliable");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();


		values.put("id", 22);
		values.put("value", "confident");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 23);
		values.put("value", "amazing");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 24);
		values.put("value", "satisfied");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 25);
		values.put("value", "satisfying");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 26);
		values.put("value", "interested");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 27);
		values.put("value", "interesting");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 28);
		values.put("value", "joyous");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 29);
		values.put("value", "joy");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 30);
		values.put("value", "attractive");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 31);
		values.put("value", "love");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 32);
		values.put("value", "loved");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 33);
		values.put("value", "tender");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 34);
		values.put("value", "earnest");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 35);
		values.put("value", "brave");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 36);
		values.put("value", "comforted");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 37);
		values.put("value", "thankful");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 38);
		values.put("value", "sunny");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 39);
		values.put("value", "merry");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 40);
		values.put("value", "jubilant");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 41);
		values.put("value", "alive");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 42);
		values.put("value", "playful");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 43);
		values.put("value", "cheerful");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 44);
		values.put("value", "glad");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		values.put("id", 45);
		values.put("value", "liberated");
		values.put("flag", "0");
		db.insert("positive_table", null, values);
		values.clear();

		db.close();
	}


	/**
	 * 向数据库的negative_table表插入数据。
	 */
	public void insertDataInNegative(MySQLiteHelper myHelper){
		SQLiteDatabase db = myHelper.getWritableDatabase();

		/*db.execSQL("create table if not exists negative_table("
				+ "id integer,"
				+ "value varchar primary key,"
				+ "flag integer)");*/

		//插入数据
		ContentValues values=new ContentValues();

		//否定词表
		values.put("id", 1);
		values.put("value", "sad");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 2);
		values.put("value", "dull");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 3);
		values.put("value", "hurt");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 4);
		values.put("value", "afraid");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 5);
		values.put("value", "anxious");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 6);
		values.put("value", "bored");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 7);
		values.put("value", "worried");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 8);
		values.put("value", "angry");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 9);
		values.put("value", "unhappy");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 10);
		values.put("value", "lonely");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 11);
		values.put("value", "confused");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 12);
		values.put("value", "helpless");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 13);
		values.put("value", "alone");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 14);
		values.put("value", "annoyed");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 15);
		values.put("value", "dissatisfied");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 16);
		values.put("value", "unpleasant");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 17);
		values.put("value", "miserable");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 18);
		values.put("value", "desperate");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 19);
		values.put("value", "terrible");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 20);
		values.put("value", "bad");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 21);
		values.put("value", "hostile");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 22);
		values.put("value", "discouraged");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 23);
		values.put("value", "useless");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 24);
		values.put("value", "perplexed");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 25);
		values.put("value", "ashamed");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 26);
		values.put("value", "hesitant");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 27);
		values.put("value", "vulnerable");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 28);
		values.put("value", "shy");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 29);
		values.put("value", "offensive");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 30);
		values.put("value", "bitter");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 31);
		values.put("value", "resentful");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 32);
		values.put("value", "uneasy");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 33);
		values.put("value", "infuriated");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 34);
		values.put("value", "tragic");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 35);
		values.put("value", "provoked");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 36);
		values.put("value", "boiling");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 37);
		values.put("value", "misgiving");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 38);
		values.put("value", "sulky");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 39);
		values.put("value", "disgusting");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();

		values.put("id", 40);
		values.put("value", "awful");
		values.put("flag", 1);
		db.insert("negative_table", null, values);
		values.clear();
		db.close();
	}


	/**
	 * Query data in negation_table
	 */
	public String queryNegationData(MySQLiteHelper myHelper, String text){
		String result = "";
		SQLiteDatabase db = myHelper.getReadableDatabase();
		Cursor cursor = db.query("negation_table",null,null,null,null,null,"id asc");
		int valueIndex1 = cursor.getColumnIndex("value");
		int flagIndex1 = cursor.getColumnIndex("flag");


		String[] splitArray=text.split("[ ]");
		for(int i=0;i<splitArray.length;i++){

			for(cursor.moveToFirst();!(cursor.isAfterLast());cursor.moveToNext()){
				if(cursor.getString(valueIndex1).equalsIgnoreCase(splitArray[i])){
//				result = result +cursor1.getString(valueIndex1)+"\t\t";
					result = result +cursor.getString(flagIndex1)+i+" ";
				}
			}

		}

		cursor.close();

		db.close();

		return result;

	}
	/**
	 * query data in negative_table
	 */
	public String queryNegativeData(MySQLiteHelper myHelper, String text){
		String result = "";
		SQLiteDatabase db = myHelper.getReadableDatabase();
		Cursor cursor = db.query("negative_table",null,null,null,null,null,"id asc");
		int valueIndex = cursor.getColumnIndex("value");
		int flagIndex = cursor.getColumnIndex("flag");

		String[] splitArray=text.split("[ ]");

		for(int i=0;i<splitArray.length;i++){

			for(cursor.moveToFirst();!(cursor.isAfterLast());cursor.moveToNext()){
				if(cursor.getString(valueIndex).equalsIgnoreCase(splitArray[i])){
//				result = result +cursor1.getString(valueIndex)+"\t\t";
					result = result +cursor.getInt(flagIndex)+i+" ";
				}
			}

		}

		cursor.close();

		db.close();
		return result;

	}

	/**
	 * query data in positive_table
	 */
	public String queryPositiveData(MySQLiteHelper myHelper, String text){
		String result = "";
		SQLiteDatabase db = myHelper.getReadableDatabase();
		Cursor cursor = db.query("positive_table",null,null,null,null,null,"id asc");
		int valueIndex = cursor.getColumnIndex("value");
		int flagIndex = cursor.getColumnIndex("flag");

		String[] splitArray=text.split("[ ]");

		for(int i=0;i<splitArray.length;i++){

			for(cursor.moveToFirst();!(cursor.isAfterLast());cursor.moveToNext()){
				if(cursor.getString(valueIndex).equalsIgnoreCase(splitArray[i])){
//				result = result +cursor1.getString(valueIndex)+"\t\t";
					result = result +cursor.getInt(flagIndex)+i+" ";
				}
			}

		}

		cursor.close();

		db.close();
		return result;

	}
	//*****************************sentimental analysis****************************************
	/**
	 * 初始化Layout。
	 */
	private void initLayout() {
		findViewById(R.id.iat_recognize).setOnClickListener(IatDemo.this);
//		findViewById(R.id.iat_recognize_stream).setOnClickListener(IatDemo.this);
//		findViewById(R.id.iat_upload_contacts).setOnClickListener(IatDemo.this);
//		findViewById(R.id.iat_upload_userwords).setOnClickListener(IatDemo.this);
//		findViewById(R.id.iat_stop).setOnClickListener(IatDemo.this);
//		findViewById(R.id.iat_cancel).setOnClickListener(IatDemo.this);
		findViewById(R.id.image_iat_set).setOnClickListener(IatDemo.this);
		// 选择云端or本地
		RadioGroup group = (RadioGroup) findViewById(R.id.radioGroup);
//		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//
//			@Override
//			public void onCheckedChanged(RadioGroup group, int checkedId) {
//				switch (checkedId) {
//				case R.id.iatRadioCloud:
//					mEngineType = SpeechConstant.TYPE_CLOUD;
//					findViewById(R.id.iat_upload_contacts).setEnabled(true);
//					findViewById(R.id.iat_upload_userwords).setEnabled(true);
//					break;
//				case R.id.iatRadioLocal:
//					mEngineType = SpeechConstant.TYPE_LOCAL;
//					findViewById(R.id.iat_upload_contacts).setEnabled(false);
//					findViewById(R.id.iat_upload_userwords).setEnabled(false);
//					/**
//					 * 选择本地听写 判断是否安装语记,未安装则跳转到提示安装页面
//					 */
//					if (!SpeechUtility.getUtility().checkServiceInstalled()) {
//						mInstaller.install();
//					} else {
//						String result = FucUtil.checkLocalResource();
//						if (!TextUtils.isEmpty(result)) {
//							showTip(result);
//						}
//					}
//					break;
//				case R.id.iatRadioMix:
//					mEngineType = SpeechConstant.TYPE_MIX;
//					findViewById(R.id.iat_upload_contacts).setEnabled(false);
//					findViewById(R.id.iat_upload_userwords).setEnabled(false);
//					/**
//					 * 选择本地听写 判断是否安装语记,未安装则跳转到提示安装页面
//					 */
//					if (!SpeechUtility.getUtility().checkServiceInstalled()) {
//						mInstaller.install();
//					} else {
//						String result = FucUtil.checkLocalResource();
//						if (!TextUtils.isEmpty(result)) {
//							showTip(result);
//						}
//					}
//					break;
//				default:
//					break;
//				}
//			}
//		});
	}

	int ret = 0; // 函数调用返回值

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		// 进入参数设置页面
		case R.id.image_iat_set:
			Intent intents = new Intent(IatDemo.this, IatSettings.class);
			startActivity(intents);
			break;
		// 开始听写
		// 如何判断一次听写结束：OnResult isLast=true 或者 onError
		case R.id.iat_recognize:
			// 移动数据分析，收集开始听写事件
			FlowerCollector.onEvent(IatDemo.this, "iat_recognize");
			
			mResultText.setText(null);// 清空显示内容
			mIatResults.clear();
			// 设置参数
			setParam();
			boolean isShowDialog = mSharedPreferences.getBoolean(
					getString(R.string.pref_key_iat_show), true);
			if (isShowDialog) {
				// 显示听写对话框
                mIatDialog.setListener(mRecognizerDialogListener);
                mIatDialog.show();
                mIatDialog.hide();//隐藏
                pdialog=new ProgressDialog(IatDemo.this);
                pdialog.setMessage("Please speak to the microphone!");
                pdialog.setCancelable(true);
                pdialog.show();
			} else {
				// 不显示听写对话框
				ret = mIat.startListening(mRecognizerListener);
				if (ret != ErrorCode.SUCCESS) {
					showTip("听写失败,错误码：" + ret);
				} else {
					showTip(getString(R.string.text_begin));
				}
			}
			break;

		default:
			break;
		}
	}

	/**
	 * 初始化监听器。
	 */
	private InitListener mInitListener = new InitListener() {

		@Override
		public void onInit(int code) {
			Log.d(TAG, "SpeechRecognizer init() code = " + code);
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败，错误码：" + code);
			}
		}
	};

	/**
	 * 上传联系人/词表监听器。
	 */


	/**
	 * 听写监听器。
	 */
	private RecognizerListener mRecognizerListener = new RecognizerListener() {

		@Override
		public void onBeginOfSpeech() {
			// 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
			showTip("开始说话");
		}

		@Override
		public void onError(SpeechError error) {
			// Tips：
			// 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
			// 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
			showTip(error.getPlainDescription(true));
		}

		@Override
		public void onEndOfSpeech() {
			// 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
			showTip("结束说话");
		}

		@Override
		public void onResult(RecognizerResult results, boolean isLast) {
			Log.d(TAG, results.getResultString());


			if (isLast) {
				// TODO 最后的结果
			}
            printResult(results);
		}

		@Override
		public void onVolumeChanged(int volume, byte[] data) {
			showTip("当前正在说话，音量大小：" + volume);
			Log.d(TAG, "返回音频数据："+data.length);
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			//	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			//		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			//		Log.d(TAG, "session id =" + sid);
			//	}
		}
	};

    private void printResult(RecognizerResult results) {
        pdialog.dismiss();
//		rdialog.dismiss();
        String text = JsonParser.parseIatResult(results.getResultString());
        System.out.println("text:"+text);
        String sn = null;
        MySQLiteHelper myHelper;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        mResultText.setText(text);
        mResultText.setSelection(text.length());
        resultText=resultBuffer.toString();
        myHelper = new MySQLiteHelper(this,"my.db",null,1);
        String result = "";
        String negationResult= queryNegationData(myHelper,resultBuffer.toString());
        String positiveResult=queryPositiveData(myHelper,resultBuffer.toString());
        String negativeResult = queryNegativeData(myHelper,resultBuffer.toString());
        result = negationResult+negativeResult+positiveResult;  //""
        String[] splitResultArray=result.split("[ ]");
//		String[] splitResultArray=result.split(" ");
        String orderedResult="";//排序后的结果如y011
        String processedResult=""; //去掉y之后待数个数比较的结果
        int count0=0,count1=0;//数0和1的个数
        String keywordResult="";//word bag算法最终结果
        try{
            //用冒泡算法对分割后的结果排序
            for(int i=0;i<splitResultArray.length;i++)
            {

                for(int j=i;j<splitResultArray.length;j++)
                {
                    if(Integer.parseInt(splitResultArray[i].substring(1))>Integer.parseInt(splitResultArray[j].substring(1))){

                        String temp = splitResultArray[i];
                        splitResultArray[i]=splitResultArray[j];
                        splitResultArray[j]=temp;

                    }


                }

            }
            //排序后提取flag值，这样就ordered了
            for(int i=0;i<splitResultArray.length;i++){

                orderedResult=orderedResult+splitResultArray[i].substring(0,1);
            }

            for(int i=0;i<orderedResult.length();i++){


                if(orderedResult.charAt(i)=='y'&&orderedResult.charAt(i+1)!='\0')
                {
                    if(orderedResult.charAt(i+1)=='1')
                    {
                        processedResult=processedResult+"0";
                        i++;
                    }
                    else if(orderedResult.charAt(i+1)=='0')
                    {
                        processedResult=processedResult+"1";
                        i++;
                    }
                }
                else{

                    processedResult=processedResult+String.valueOf(orderedResult.charAt(i));
                }

            }

            for(int i=0;i<processedResult.length();i++){

                if(processedResult.charAt(i)=='0')
                {count0++;}
                else if(processedResult.charAt(i)=='1')
                {count1++;}
            }

            if((count0==0)&&(count1>0)){
                keywordResult="Negative";
                tv1.setTextColor(Color.BLUE);

            }
            else if((count1==0)&&(count0>0)){
                keywordResult="Positive";
                tv1.setTextColor(Color.RED);

            }
            else{
                keywordResult="Neutral";
                tv1.setTextColor(Color.GREEN);

            }

            resultKeyWord=keywordResult;
            tv1.setText(resultKeyWord);

        }catch(Exception e)
        {
            resultKeyWord="Neutral";
            tv1.setText(resultKeyWord);
            System.out.println("exception e:"+e);
        }
    }


    /**
	 * 听写UI监听器
	 */
	private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
		public void onResult(RecognizerResult results, boolean isLast) {
            if(isLast==true) return;
			printResult(results);
		}

		/**
		 * 识别回调错误.
		 */
		public void onError(SpeechError error) {
			showTip(error.getPlainDescription(true));
		}

	};

	/**
	 * 获取联系人监听器。
	 */


	private void showTip(final String str) {
		mToast.setText(str);
		mToast.show();
	}

	/**
	 * 参数设置
	 * 
	 * @param
	 * @return
	 */
	public void setParam() {
		// 清空参数
		mIat.setParameter(SpeechConstant.PARAMS, null);

		// 设置听写引擎
		mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
		// 设置返回结果格式
		mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
//		String lag = mSharedPreferences.getString("iat_language_preference",
//				"mandarin");
//		if (lag.equals("en_us")) {
//			// 设置语言
//			mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
//		} else {
//			// 设置语言
//			mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
//			// 设置语言区域
//			mIat.setParameter(SpeechConstant.ACCENT, lag);
//		}

		// 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
		mIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "4000"));
		
		// 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
		mIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));
		
		// 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
		mIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "1"));
		
		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mIat.setParameter(SpeechConstant.AUDIO_FORMAT,"wav");
		mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/iat.wav");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 退出时释放连接
		mIat.cancel();
		mIat.destroy();
	}

	@Override
	protected void onResume() {
		// 开放统计 移动数据统计分析
		FlowerCollector.onResume(IatDemo.this);
		FlowerCollector.onPageStart(TAG);
		super.onResume();
	}

	@Override
	protected void onPause() {
		// 开放统计 移动数据统计分析
		FlowerCollector.onPageEnd(TAG);
		FlowerCollector.onPause(IatDemo.this);
		super.onPause();
	}
}
