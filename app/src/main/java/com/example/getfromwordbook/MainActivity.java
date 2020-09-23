package com.example.getfromwordbook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG="MyWordsTag";
    private ContentResolver resolver;
    public static List<Activity> activityList = new LinkedList();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.caidan,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.all:{
                refreshWordsList();
                break;
            }
            case R.id.search:{
                SearchDialog();
                break;
            }
            case R.id.add:{
                InsertDialog();
                break;
            }
            case R.id.help:{
                Intent intent = new Intent(MainActivity.this,Help.class);
                startActivity(intent);
                break;
            }
            case R.id.exit:{
                exit();
            }
        }
        return false;
    }
    public void exit(){

        for(Activity act:activityList){

            act.finish();

        }

        System.exit(0);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activityList.add(this);
        resolver = this.getContentResolver();

        ListView list = (ListView) findViewById(R.id.list);
        registerForContextMenu(list);


        setWordsListView(getAllWord());
    }



    private void setWordsListView(ArrayList<Map<String, String>> items){
        SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.item,
                new String[]{Words.Word._ID,Words.Word.COLUMN_NAME_WORD, Words.Word.COLUMN_NAME_MEANING, Words.Word.COLUMN_NAME_SAMPLE},
                new int[]{R.id.textId,R.id.textViewWord, R.id.textViewMeaning, R.id.textViewSample});

        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
    }

    //得到全部单词的ArrayList
    public ArrayList<Map<String,String>> getAllWord(){
        Cursor cursor = resolver.query(Words.Word.CONTENT_URI,
                new String[] { Words.Word._ID, Words.Word.COLUMN_NAME_WORD,Words.Word.COLUMN_NAME_MEANING,Words.Word.COLUMN_NAME_SAMPLE},
                null, null, null);
        if (cursor == null){
            Toast.makeText(MainActivity.this,"没有找到记录",Toast.LENGTH_LONG).show();
        }
        return ConvertCursorToWordList(cursor);
    }

    //cursor转ArrayList
    public ArrayList<Map<String,String>> ConvertCursorToWordList(Cursor cursor){
        ArrayList<Map<String,String>> list = new ArrayList<Map<String, String>>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Map<String,String> map = new HashMap<String,String>();
            System.out.println("dsl:"+cursor.getString(cursor.getColumnIndex("_id"))+ "word:"+cursor.getString(cursor.getColumnIndex("word")));
            map.put(Words.Word._ID, cursor.getString(cursor.getColumnIndex("_id")));
            map.put(Words.Word.COLUMN_NAME_WORD, cursor.getString(cursor.getColumnIndex("word")));
            map.put(Words.Word.COLUMN_NAME_MEANING, cursor.getString(cursor.getColumnIndex("meaning")));
            map.put(Words.Word.COLUMN_NAME_SAMPLE, cursor.getString(cursor.getColumnIndex("sample")));
            list.add(map);
        }


        return list;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.contextmenu_wordslistview, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        TextView textId = null;
        TextView textWord = null;
        TextView textMeaning = null;
        TextView textSample = null;
        AdapterView.AdapterContextMenuInfo info = null;
        View itemView = null;
        switch (item.getItemId()) {
            case R.id.delete:
                //删除单词
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                itemView = info.targetView;
                textId = (TextView) itemView.findViewById(R.id.textId);
                if (textId != null) {
                    String strId = textId.getText().toString();
                    DeleteDialog(strId);
                }

                break;
            case R.id.update:
                //修改单词
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                itemView = info.targetView;
                textId = (TextView) itemView.findViewById(R.id.textId);
                if (textId != null) {
                    String strId = textId.getText().toString();
                    Words.WordDescription word=  getSingleWord(strId);
                    String strWord = word.getWord();
                    String strMeaning = word.getMeaning();
                    String strSample = word.getSample();
                    UpdateDialog(strId,strWord, strMeaning, strSample);
                    break;
                }
        }
                return true;

    }
    private void DeleteDialog(final String strId) {
        new AlertDialog.Builder(this).setTitle("删除单词")
                .setMessage("是否真的删除单词?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //既可以使用Sql语句删除，也可以使用使用delete方法删除
                        //删除
                        Uri uri = Uri.parse(Words.Word.CONTENT_URI_STRING + "/" + strId);
                        int result = resolver.delete(uri, null, null);
                        refreshWordsList();
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).create().show();
    }

    //修改对话框
    private void UpdateDialog(final String strId, final String strWord, final String strMeaning, final String strSample) {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.insert, null);
        ((EditText) tableLayout.findViewById(R.id.txtWord)).setText(strWord);
        ((EditText) tableLayout.findViewById(R.id.txtMeaning)).setText(strMeaning);
        ((EditText) tableLayout.findViewById(R.id.txtSample)).setText(strSample);
        new AlertDialog.Builder(this).setTitle("修改单词")//标题
                .setView(tableLayout)//设置视图
                // 确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strNewWord = ((EditText) tableLayout.findViewById(R.id.txtWord)).getText().toString();

                        String strNewMeaning = ((EditText) tableLayout.findViewById(R.id.txtMeaning)).getText().toString();
                        String strNewSample = ((EditText) tableLayout.findViewById(R.id.txtSample)).getText().toString();
                        //既可以使用Sql语句更新，也可以使用使用update方法更新

                        //更新
                        ContentValues values = new ContentValues();

                        values.put(Words.Word.COLUMN_NAME_WORD, strNewWord);
                        values.put(Words.Word.COLUMN_NAME_MEANING, strNewMeaning);
                        values.put(Words.Word.COLUMN_NAME_SAMPLE, strNewSample);

                        Uri uri = Uri.parse(Words.Word.CONTENT_URI_STRING + "/" + strId);
                        int result = resolver.update(uri, values, null, null);
                        //单词已经更新，更新显示列表
                        refreshWordsList();
                    }

                })
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).create()//创建对话框
                .show();//显示对话框

    }

    //新增对话框
    public void InsertDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater().inflate(R.layout.insert, null);
        new AlertDialog.Builder(this)
                .setTitle("新增单词")//标题
                .setView(tableLayout)//设置视图
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String strWord = ((EditText) tableLayout.findViewById(R.id.txtWord)).getText().toString();
                        String strMeaning = ((EditText) tableLayout.findViewById(R.id.txtMeaning)).getText().toString();
                        String strSample = ((EditText) tableLayout.findViewById(R.id.txtSample)).getText().toString();

                        Log.v("test",strWord+":"+strMeaning+":"+strSample);
                        //既可以使用Sql语句插入，也可以使用使用insert方法插入
                        // InsertUserSql(strWord, strMeaning, strSample);
                        ContentValues values = new ContentValues();

                        values.put(Words.Word.COLUMN_NAME_WORD, strWord);
                        values.put(Words.Word.COLUMN_NAME_MEANING, strMeaning);
                        values.put(Words.Word.COLUMN_NAME_SAMPLE, strSample);

                        Uri newUri = resolver.insert(Words.Word.CONTENT_URI, values);

                        //单词已经插入到数据库，更新显示列表
                        refreshWordsList();
                    }
                })
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {           }
                })
                .create()//创建对话框
                .show();//显示对话框
    }


    //查找对话框
    private void SearchDialog() {
        final TableLayout tableLayout = (TableLayout) getLayoutInflater()
                .inflate(R.layout.searchterm, null);
        new AlertDialog.Builder(this)
                .setTitle("查找单词")//标题
                .setView(tableLayout)//设置视图
                //确定按钮及其动作
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String txtSearchWord = ((EditText) tableLayout.findViewById(R.id.searchWord))
                                .getText().toString();

                        //单词已经插入到数据库，更新显示列表
                        refreshWordsList(txtSearchWord);
                    }
                })
                //取消按钮及其动作
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create()//创建对话框
                .show();//显示对话框
    }
    //获得单个单词
    public Words.WordDescription getSingleWord(String id){
        Words.WordDescription result = new Words.WordDescription();
        Uri uri = Uri.parse(Words.Word.CONTENT_URI_STRING + "/" + id);
        Cursor cursor = resolver.query(Words.Word.CONTENT_URI,
                new String[] { Words.Word._ID, Words.Word.COLUMN_NAME_WORD, Words.Word.COLUMN_NAME_MEANING,Words.Word.COLUMN_NAME_SAMPLE},
                null, null, null);
        if(cursor.moveToFirst()){
            result.setWord(cursor.getString(cursor.getColumnIndex("word")));
            result.setMeaning(cursor.getString(cursor.getColumnIndex("meaning")));
            result.setSample(cursor.getString(cursor.getColumnIndex("sample")));
            System.out.println("result: "+result.getWord());
            return result;
        }
        return null;
    }

    //查找
    public ArrayList<Map<String, String>> SearchUseSql(String strWordSearch) {

        Cursor cursor = resolver.query(Words.Word.CONTENT_URI,
                new String[]{Words.Word._ID, Words.Word.COLUMN_NAME_WORD, Words.Word.COLUMN_NAME_MEANING, Words.Word.COLUMN_NAME_SAMPLE},
                Words.Word.COLUMN_NAME_WORD+" like ?", new String[]{"%"+strWordSearch+"%"}, null);


        return ConvertCursorToWordList(cursor);
    }
    //更新列表
    public void refreshWordsList(){
        ArrayList<Map<String,String>> items = getAllWord();
        SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.item,
                new String[]{Words.Word._ID,Words.Word.COLUMN_NAME_WORD, Words.Word.COLUMN_NAME_MEANING, Words.Word.COLUMN_NAME_SAMPLE},
                new int[]{R.id.textId,R.id.textViewWord, R.id.textViewMeaning, R.id.textViewSample});

        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
    }

    //更新单词列表，从数据库中找到同strWord向匹配的单词，然后在列表中显示出来
    public void refreshWordsList(String strWord) {
            ArrayList<Map<String, String>> items = SearchUseSql(strWord);
        SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.item,
                new String[]{Words.Word._ID,Words.Word.COLUMN_NAME_WORD, Words.Word.COLUMN_NAME_MEANING, Words.Word.COLUMN_NAME_SAMPLE},
                new int[]{R.id.textId,R.id.textViewWord, R.id.textViewMeaning, R.id.textViewSample});

        ListView list = (ListView) findViewById(R.id.list);
        list.setAdapter(adapter);
    }
}
