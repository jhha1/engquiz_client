package kr.jhha.engquiz.presenter_view.scripts.regular;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import kr.jhha.engquiz.R;
import kr.jhha.engquiz.model.local.ScriptRepository;
import kr.jhha.engquiz.presenter_view.MyToolbar;
import kr.jhha.engquiz.util.StringHelper;
import kr.jhha.engquiz.util.ui.MyDialog;

import static kr.jhha.engquiz.presenter_view.FragmentHandler.EFRAGMENT.ADD_SCRIPT_FROM_OTHER_LOCATION;
import static kr.jhha.engquiz.presenter_view.scripts.regular.RegularScriptsPresenter.DEFAUT_FILE_UPDOWN_SIZE;


/**
 * Created by Junyoung on 2016-06-23.
 */

public class AddScriptOtherDirectoryFragment extends Fragment implements AddScriptOtherDirectoryContract.View {
    private AddScriptOtherDirectoryContract.ActionsListener mActionListener;

    // 현재 파일 위치 출력 뷰
    private TextView mFileLocationView = null;
    // 파일 리스트 뷰
    private ListView mItemListView = null;

    private MyToolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initToolbar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_add_script, container, false);

        // presenter에 공용 클래스 변수들이 사용되므로, 뷰가 새로 그려질때마다 presenter를 새로 생성하도록 한다.
        mActionListener = new AddScriptOtherDirectoryPresenter(getActivity(), this, ScriptRepository.getInstance());


        // 현재 폴더 위치 출력할 폴더 위치 칸
        mFileLocationView = (TextView) view.findViewById(R.id.add_script_file_location);
        // 리스트 뷰: 디렉토리와 파일 하이락키를 출력
        mItemListView = (ListView) view.findViewById(R.id.add_script_filebrower);
        mItemListView.clearChoices(); // 기존에 선택했던 것 초기화

        // 파일 리스트 클릭 이벤트 연결
        mItemListView.setOnItemClickListener(mOnItemClickListener);
        // 추가할 스크립트 선택 후, OK 버튼 클릭 이벤트.
        Button mOKButton = (Button) view.findViewById(R.id.add_script_btn_ok);
        mOKButton.setOnClickListener(mClickListener);

        // 리스트에 출력할 데이터 초기화
        mActionListener.initDirectoryLocationAndAvailableFiles();

        view.bringToFront(); // 리스트가 길어질 경우 가장 위로 스크롤.
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 리스트에 출력할 데이터 초기화
        mActionListener.initDirectoryLocationAndAvailableFiles();

        mToolbar.updateToolBar(ADD_SCRIPT_FROM_OTHER_LOCATION);
    }

    // 추가할 스크립트 선택 리스트 뷰: 리스트의 row 선택 이벤트.
    // 상위 or 하위 디렉토리 선택시, 디렉토리 다시 그리기
    AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            // String strText = (String) parent.getItemAtPosition(position);  // toInt TextView's Text.
            mActionListener.onFileListItemClick(position);
        }
    };

    // 스크립트 추가 버튼 클릭.
    //  -> 스크립트 추가 확인 다이알로그를 띄운다.
    Button.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.add_script_btn_ok:
                    mActionListener.scriptSelected();
                    break;
            }
        }
    };

    @Override
    public void showCurrentDirectoryPath(String path) {
        mFileLocationView.setText(
                getString(R.string.add_pdf_script__cur_folder_path)
                + path);
    }

    @Override
    public void showFileListInDirectory(List<String> fileList) {
        // 디렉토리 위치변경에 의한 UI 리프레시
        //  ->  mAdapter.notifyDataSetChanged(); 가 안먹혀서 이렇게 함.
        //      adapter내부 데이터가 변경 될때 mAdapter.notifyDataSetChanged()가 먹힌다고 함.
        //      ArrayAdapter는 내부데이터 변경이 인지 않된다는건데 잘 이해는 안감.
        int drawable = R.layout.content_script__add_other_dir__listview_item;
        ArrayAdapter mAdapter = new ArrayAdapter<String>(getActivity(), drawable, fileList);
        // 파일 리스트 아답터 연결
        mItemListView.setAdapter(mAdapter);
    }

    @Override
    public void showMsg(int what, String arg) {
        String msg = null;
        switch (what) {
            case 1:
                msg = "[" + arg + "] " + getString(R.string.add_pdf_script__can_open_dir);
                break;
        }
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showErrorDialog(int msgID) {
        showErrorDialog(getString(msgID));
    }

    public void showErrorDialog(String msg){
        MyDialog dialog = new MyDialog(getActivity());
        dialog.setMessage( msg );
        dialog.setPositiveButton();
        dialog.showUp();
    }

    @Override
    public void showAddScriptConfirmDialog(final String filename, Float fileSize) {
        // 스크립트 추가 다이알로그 띄우기
        // 파일 사이즈가 이상할경우, UI에는 0.4MB로 표시 (업로드 + 다운로드)
        fileSize = (fileSize <= 0) ? DEFAUT_FILE_UPDOWN_SIZE : fileSize;
        String dialogMsg = (StringHelper.isNull(filename) ? "스크립트" : filename)
                + "\n\n스크립트 분석을 위한 서버로의 파일 업/다운로드 과정에서  "
                + fileSize + "MB의 데이터가 소모될 수 있으니, "
                + "WIFI 환경에서 이용하시길 권장드려요."
                + "\n\n스크립트를 추가하시겠어요?";

        final MyDialog dialog = new MyDialog(getActivity());
        dialog.setTitle(getString(R.string.add_pdf_script__add_script));
        dialog.setMessage( dialogMsg );
        dialog.setPositiveButton( new View.OnClickListener() {
            public void onClick(View arg0) {
                mActionListener.addScript(filename);
                dialog.dismiss();
            }});
        dialog.setNegativeButton();
        dialog.showUp();
    }

    // 다이알로그
    // 동글뱅이 로딩 중(스크립트 추가중.. ) 다이알로그. 서버통신때씀
    private ProgressDialog mDialogLoadingSpinner = null;
    @Override
    public void showLoadingDialog() {
        mDialogLoadingSpinner = MyDialog.createLoadingDialog(getActivity());
    }

    @Override
    public void closeLoadingDialog() {
        mDialogLoadingSpinner.dismiss(); // 로딩스피너 다이알로그 닫기
    }

    @Override
    public void showAddScriptSuccessDialog() {
        MyDialog dialog = new MyDialog(getActivity());
        dialog.setMessage(getString(R.string.add_pdf_script__succ_dialog_title));
        dialog.setPositiveButton();
        dialog.showUp();
    }


    /*
       Action Bar
      */
    private void initToolbar() {
        // 액션 바 보이기
        mToolbar = MyToolbar.getInstance();
        setHasOptionsMenu(true);
    }

    // 메뉴버튼이 처음 눌러졌을 때 실행되는 콜백메서드
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // 화면에 보여질때 마다 호출됨
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        mToolbar.updateToolBarOptionMenu(ADD_SCRIPT_FROM_OTHER_LOCATION, menu);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_bar__help_webview:
                //mActionListener.helpBtnClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
