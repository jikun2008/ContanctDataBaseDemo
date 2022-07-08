package com.example.contanct.database.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btTest = findViewById(R.id.btTest);
        getPermissions();
        btTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<TsContact> contactList = new ArrayList<>();
                for (int i = 0; i < 1000; i++) {
                    TsContact contact = new TsContact(i + "jikun", i + "");
                    contactList.add(contact);
                }


                //测试多次调用
                for (int i = 0; i < 10; i++) {
                    ContentProviderHelper.getInstance().insertContact(getApplicationContext(), contactList);
                }


            }
        });

        Button btTest1 = findViewById(R.id.btTest1);
        btTest1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("测试代码", "测试代码testDeleteContacts");
                ContentProviderHelper.getInstance().deleteContact(getApplicationContext());
                Log.e("测试代码", "测试代码testDeleteContacts over");
            }
        });
    }

    private void getPermissions() {
        XXPermissions.with(this)
                // 申请多个权限
                .permission(Permission.READ_CONTACTS)
                .permission(Permission.WRITE_CONTACTS)
                // 设置权限请求拦截器（局部设置）
                //.interceptor(new PermissionInterceptor())
                // 设置不触发错误检测机制（局部设置）
                //.unchecked()
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all) {
                            toast("获取联系人权限成功");
                        } else {
                            toast("获取部分权限成功，但部分权限未正常授予");
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            toast("被永久拒绝授权，请手动授予录音和日历权限");
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(getApplicationContext(), permissions);
                        } else {
                            toast("获取录音和日历权限失败");
                        }
                    }
                });
    }

    private void toast(String info) {
        Toast.makeText(getApplicationContext(), info, Toast.LENGTH_SHORT).show();
    }
}