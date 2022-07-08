package com.example.contanct.database.demo;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


public class ContentProviderHelper {

    private AtomicInteger atomicInteger = new AtomicInteger(0);
    private AtomicInteger currentInteger = new AtomicInteger(0);


    ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

    private ContentProviderHelper() {

    }

    private static class Singleton2 {
        private static final ContentProviderHelper INSTANCE = new ContentProviderHelper();
    }

    public static ContentProviderHelper getInstance() {
        return Singleton2.INSTANCE;
    }


    public void insertContact(Context context, List<TsContact> contactManList) {
        atomicInteger.incrementAndGet();

        deleteContact(context);
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                currentInteger.incrementAndGet();
                Log.e("测试代码", "测试代码___单线程___insertContactMen:" + Thread.currentThread().getName());
                insertContactMen(context, contactManList);
                Log.e("测试代码", "测试代码___单线程___insertContactMen end:" + Thread.currentThread().getName());
            }
        });

    }

    public void deleteContact(Context context) {
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {

                delAllContacts(context);

            }
        });


    }

    private void insertContactMen(Context context, List<TsContact> contactManList) {
        Log.e("测试代码", "测试代码______insertContactMen:Thread.name=" + Thread.currentThread().getName());
        if (contactManList == null) {
            return;
        }
        try {
            ArrayList<ContentProviderOperation> ops = new ArrayList<>();
            int rawContactInsertIndex = 0;

            for (TsContact contactMan : contactManList) {
                //添加姓名
                String name = contactMan.getName();
                String numbers = contactMan.getPhoneNumber();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(numbers + "")) {
                    continue;
                }
                rawContactInsertIndex = ops.size();
                //必不可少
                ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, 1 + "")
                        .withValue(RawContacts.ACCOUNT_NAME, "hs")
                        .withYieldAllowed(true).build());
                //添加名字
                ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex)
                        .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(StructuredName.DISPLAY_NAME, name)
                        .withYieldAllowed(true).build());
                //添加号码
                ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference(Data.RAW_CONTACT_ID, rawContactInsertIndex).withValue(Data.MIMETYPE, Phone
                        .CONTENT_ITEM_TYPE).withValue(Phone.NUMBER, numbers).withValue(Phone.TYPE, Phone.TYPE_MOBILE).withValue(Phone.LABEL, "").withYieldAllowed(true).build());
                if (atomicInteger.get() > currentInteger.get()) {
                    Log.e("测试代码", "测试代码______取消");
                    return;
                }


                if (ops.size() >= 400) {
                    try {
                        Log.e("测试代码", "测试代码 context.getContentResolver().applyBatch ops.size() >= 400");
                        context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        ops.clear();
                    }
                } else {

                }
            }


            try {
                Log.e("测试代码", "测试代码 context.getContentResolver().applyBatch");
                context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                ops.clear();
                atomicInteger.set(0);
                currentInteger.set(0);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }finally {

        }
    }


    /**
     * 删除全部联系人
     *
     * @return
     */
    private HashMap<String, Object> delAllContacts(Context context) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ContentProviderOperation op = null;
        Uri uri = null;
        HashMap<String, Object> delResult = new HashMap<String, Object>();
        int num = 0;//删除影响的行数
        context.getContentResolver().delete(Uri.parse(ContactsContract.RawContacts.CONTENT_URI.toString() + "?"
                        + ContactsContract.CALLER_IS_SYNCADAPTER + "=true"),
                ContactsContract.RawContacts._ID + ">0", null);
        //删除Data表的数据
        uri = Uri.parse(Data.CONTENT_URI.toString() + "?" + ContactsContract.CALLER_IS_SYNCADAPTER + "=true");
        op = ContentProviderOperation.newDelete(uri)
                .withSelection(Data.RAW_CONTACT_ID + ">0", null)
                .withYieldAllowed(true)
                .build();
        ops.add(op);
        //删除RawContacts表的数据
        uri = Uri.parse(RawContacts.CONTENT_URI.toString() + "?" + ContactsContract.CALLER_IS_SYNCADAPTER + "=true");
        op = ContentProviderOperation.newDelete(RawContacts.CONTENT_URI)
                .withSelection(RawContacts._ID + ">0", null)
                .withYieldAllowed(true)
                .build();
        ops.add(op);
        //删除Contacts表的数据
        uri = Uri.parse(ContactsContract.Contacts.CONTENT_URI.toString() + "?" + ContactsContract.CALLER_IS_SYNCADAPTER + "=true");
        op = ContentProviderOperation.newDelete(uri)
                .withSelection(ContactsContract.Contacts._ID + ">0", null)
                .withYieldAllowed(true)
                .build();
        ops.add(op);
        //执行批量删除
        try {
            ContentProviderResult[] results = context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
            for (ContentProviderResult result : results) {
                num += result.count;
                Log.i("测试代码", "删除影响的行数：" + result.count);
            }
            delResult.put("result", "1");
            delResult.put("obj", num);
        } catch (Exception e) {
            Log.i("测试代码", e.getMessage());
            delResult.put("result", "-1");
            delResult.put("obj", "删除失败！" + e.getMessage());
        }
        if (delResult.size() == 0) {
            delResult.put("result", "0");
            delResult.put("obj", "无效删除，联系人信息不正确！");
        }
        return delResult;
    }


}

