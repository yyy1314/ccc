package com.potato.timetable.ui.login;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bigkoo.pickerview.builder.OptionsPickerBuilder;
import com.bigkoo.pickerview.view.OptionsPickerView;
import com.potato.timetable.R;
import com.potato.timetable.bean.Course;
import com.potato.timetable.colleges.base.College;
import com.potato.timetable.colleges.base.CollegeFactory;
import com.potato.timetable.ui.main.MainActivity;
import com.potato.timetable.util.Config;
import com.potato.timetable.util.KeyStoreUtils;
import com.potato.timetable.util.OkHttpUtils;
import com.potato.timetable.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    private College college;

    private EditText mAccountEt;
    private EditText mPwEt;
    private EditText mRandomCodeEt;
    private ImageView mRandomCodeIv;
    private Button mLoginBtn;
    private ProgressBar mProgressBar;

    private final Handler mHandler = new Handler();

    public static final String EXTRA_UPDATE_TIMETABLE = "update_timetable";
    private static final String KEY_ACCOUNT = "encryption_account";
    private static final String KEY_PWD = "encryption_pwd";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        college = CollegeFactory.createCollege(Config.getCollegeName());

        OkHttpUtils.setFollowRedirects(college.getFollowRedirects());
        setHasOptionsMenu(true);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        init(view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_login, menu);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        new Thread(() -> {
            if (college.isLogin()) {
                final String[] strings = college.getTermOptions();
                mHandler.post(() -> showSelectDialog(strings));
            } else {
                setRandomCodeImg();
            }
        }).start();
    }

    /**
     * ?????????
     */
    private void init(View view) {
        TextView collegeName = view.findViewById(R.id.tv_college_name);
        collegeName.setText(college.getCollegeName());
        mRandomCodeIv = view.findViewById(R.id.iv_random_code);
        mLoginBtn = view.findViewById(R.id.btn_login);
        mProgressBar = view.findViewById(R.id.loading);
        mAccountEt = view.findViewById(R.id.et_account);
        mPwEt = view.findViewById(R.id.et_password);

        mRandomCodeEt = view.findViewById(R.id.et_random_code);
        mRandomCodeEt.setFilters(new InputFilter[]{new InputFilter.LengthFilter(college.getRandomCodeMaxLength()),});
        mLoginBtn.setOnClickListener(view1 -> {
            hideInput();
            String account = mAccountEt.getText().toString();
            String pw = mPwEt.getText().toString();
            String randomCode = mRandomCodeEt.getText().toString();

            if (pw.isEmpty() || account.isEmpty() || randomCode.isEmpty()) {
                Utils.showToast("??????????????????");
            } else {
                setLoading(true);
                login(account, pw, randomCode);
            }

        });

        mRandomCodeIv.setOnClickListener(view12 -> setRandomCodeImg());
        readAccountFromLocal();
    }

    private void saveAccountToLocal(String account, String pwd) {
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("account", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_ACCOUNT, KeyStoreUtils.encrypt(account));
        editor.putString(KEY_PWD, KeyStoreUtils.encrypt(pwd));
        editor.apply();
    }

    private void readAccountFromLocal() {
        SharedPreferences sharedPreferences = Objects.requireNonNull(getContext()).getSharedPreferences("account", Context.MODE_PRIVATE);
        mAccountEt.setText(KeyStoreUtils.decrypt(sharedPreferences.getString(KEY_ACCOUNT, "")));
        mPwEt.setText(KeyStoreUtils.decrypt(sharedPreferences.getString(KEY_PWD, "")));
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Objects.requireNonNull(getActivity(), "login activity can't be null").finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * ?????????????????????
     */
    private void setUpdateResult() {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_UPDATE_TIMETABLE, true);
        Objects.requireNonNull(getActivity()).setResult(Activity.RESULT_OK, intent);
    }

    /**
     * ???????????????????????????
     *
     * @param termOptions ????????????
     */
    private void showSelectDialog(String[] termOptions) {
        if (termOptions == null || termOptions.length == 0) {
            Utils.showToast("????????????????????????");
            return;
        }
        final List<String> items = new ArrayList<>();
        for (String s : termOptions) {
            if (!s.isEmpty())
                items.add(s);
        }
        OptionsPickerView<String> mOptionsPv = new OptionsPickerBuilder(getActivity(), (options1, options2, options3, v) -> {
            setLoading(true);
            getCourses(items.get(options1));
        }).build();

        mOptionsPv.setTitleText("????????????");

        mOptionsPv.setNPicker(items, null, null);
        mOptionsPv.setSelectOptions(0);
        mOptionsPv.show();

    }

    /**
     * ???????????????????????????
     *
     * @param term
     */
    private void getCourses(final String term) {
        new Thread(() -> {
            final List<Course> list = college.getCourses(term);
            final boolean success = list != null;
            if (success && list.size() > 0) {
                Collections.sort(list);//??????????????????????????????
            }
            mHandler.post(() -> {
                setLoading(false);
                if (success) {
                    Utils.showToast(list.size() == 0 ? "?????????????????????" : "????????????");
                    MainActivity.sCourseList = list;
                    setUpdateResult();
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.finish();
                    }
                } else {
                    Utils.showToast("????????????");
                }
            });
        }).start();


    }

    /**
     * ??????????????????????????????
     *
     * @param b
     */
    private void setLoading(boolean b) {

        mLoginBtn.setEnabled(!b);
        if (b) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }

    }

    /**
     * ????????????
     */
    private void hideInput() {
        Activity activity = getActivity();
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            View v = activity.getWindow().peekDecorView();
            if (null != v && imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }

    }

    /**
     * ??????
     *
     * @param account
     * @param pw
     * @param randomCode String ?????????
     */
    private void login(final String account, final String pw, final String randomCode) {
        new Thread(() -> {
            final boolean isLogin = college.login(account, pw, randomCode);
            final String[] termOptions = college.getTermOptions();
            mHandler.post(() -> {
                setLoading(false);
                if (isLogin) {
                    saveAccountToLocal(mAccountEt.getText().toString(), mPwEt.getText().toString());
                    showSelectDialog(termOptions);
                } else {
                    Utils.showToast("????????????????????????????????????????????????");
                    setRandomCodeImg();
                }

            });
        }).start();

    }

    /**
     * ???????????????????????????????????????
     */
    private void setRandomCodeImg() {
        new Thread(() -> {
            final Bitmap bitmap = college.getRandomCodeImg(getContext().getExternalCacheDir().getAbsolutePath());
            mHandler.post(() -> {
                if (getContext() != null) {
                    if (bitmap == null) {
                        mRandomCodeIv.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_error_red_24dp));
                    } else {
                        mRandomCodeIv.setImageBitmap(bitmap);
                    }
                }
            });
        }).start();

    }

    @Override
    public void onDetach() {
        mHandler.removeCallbacksAndMessages(null);
        super.onDetach();
    }
}
