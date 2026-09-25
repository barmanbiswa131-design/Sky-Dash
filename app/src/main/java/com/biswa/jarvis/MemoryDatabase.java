package com.biswa.jarvis;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MemoryDatabase extends SQLiteOpenHelper {
    private static final String DB_NAME="personal_ai_memory.db";
    private static final int VERSION=1;

    public MemoryDatabase(Context c){ super(c,DB_NAME,null,VERSION); }

    @Override public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE memories(id INTEGER PRIMARY KEY AUTOINCREMENT, category TEXT NOT NULL, key_name TEXT NOT NULL UNIQUE, value TEXT NOT NULL, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE conversations(id INTEGER PRIMARY KEY AUTOINCREMENT, user_text TEXT NOT NULL, ai_text TEXT NOT NULL, created_at INTEGER NOT NULL)");
        db.execSQL("CREATE INDEX idx_mem_category ON memories(category)");
        db.execSQL("CREATE INDEX idx_conv_time ON conversations(created_at)");
    }

    @Override public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){ }

    public synchronized void remember(String category,String key,String value){
        long now=System.currentTimeMillis();
        SQLiteDatabase db=getWritableDatabase();
        android.content.ContentValues v=new android.content.ContentValues();
        v.put("category",category); v.put("key_name",key); v.put("value",value); v.put("created_at",now); v.put("updated_at",now);
        db.insertWithOnConflict("memories",null,v,SQLiteDatabase.CONFLICT_REPLACE);
    }

    public synchronized String recall(String key){
        Cursor c=getReadableDatabase().query("memories",new String[]{"value"},"key_name=?",new String[]{key},null,null,null,"1");
        try{return c.moveToFirst()?c.getString(0):null;}finally{c.close();}
    }

    public synchronized String recentMemories(){
        Cursor c=getReadableDatabase().query("memories",new String[]{"category","key_name","value"},"1",null,null,null,"updated_at DESC","20");
        StringBuilder s=new StringBuilder();
        try{while(c.moveToNext())s.append(c.getString(0)).append(": ").append(c.getString(1)).append(" = ").append(c.getString(2)).append("\n");}
        finally{c.close();}
        return s.toString().trim();
    }

    public synchronized void addConversation(String user,String ai){
        android.content.ContentValues v=new android.content.ContentValues();
        v.put("user_text",user); v.put("ai_text",ai); v.put("created_at",System.currentTimeMillis());
        getWritableDatabase().insert("conversations",null,v);
        getWritableDatabase().execSQL("DELETE FROM conversations WHERE id NOT IN (SELECT id FROM conversations ORDER BY created_at DESC LIMIT 100)");
    }

    public synchronized String recentConversation(int limit){
        Cursor c=getReadableDatabase().rawQuery("SELECT user_text,ai_text FROM conversations ORDER BY created_at DESC LIMIT ?",new String[]{String.valueOf(limit)});
        StringBuilder s=new StringBuilder(); java.util.ArrayList<String> rows=new java.util.ArrayList<>();
        try{while(c.moveToNext())rows.add("USER: "+c.getString(0)+"\nAI: "+c.getString(1));}
        finally{c.close();}
        for(int i=rows.size()-1;i>=0;i--)s.append(rows.get(i)).append("\n");
        return s.toString().trim();
    }

    public synchronized void clearAll(){ getWritableDatabase().execSQL("DELETE FROM memories"); getWritableDatabase().execSQL("DELETE FROM conversations"); }
    public synchronized void closeDb(){ close(); }
}