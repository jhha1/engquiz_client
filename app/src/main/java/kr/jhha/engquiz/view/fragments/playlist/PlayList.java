package kr.jhha.engquiz.view.fragments.playlist;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import kr.jhha.engquiz.R;
import kr.jhha.engquiz.model.Const;
import kr.jhha.engquiz.view.MainActivity;
import kr.jhha.engquiz.view.fragments.PlayQuizFragment;

/**
 * Created by jhha on 2016-12-16.
 */

public class PlayList extends Fragment
{
    private final String mTITLE = "Play List";

    // 내 퀴즈 리스트
    private ListView mItemListView = null;
    // 내 퀴즈 리스트 중, 유저가 선택한 것.
    private int mSelectedItemIndex = -1;
    // 리스트 아이템 선택시, 보여지는 레이아웃
    private LinearLayout mItemOptionLayout;

    // Activity를 통해, 타 fragment에 이벤트를 던지기 위한 (fragment간 직접통신 안됨. activity를 통해야함)
    // 연결자 인터페이스.
    public interface OnPlayListButtonClickListener {
        public void onPlayListBtnClicked(View v );
    }
    private OnPlayListButtonClickListener mOnPlayListBtnClickListener;
    private AlertDialog.Builder mDeleteItemDialog = null;

    public PlayList() {}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        initDialog();
        super.onCreate(savedInstanceState);
    }

    private void initDialog()
    {
        mDeleteItemDialog = new AlertDialog.Builder( getActivity() );
        mDeleteItemDialog.setTitle("내 퀴즈 삭제");
        mDeleteItemDialog.setPositiveButton( "OK", mDeleteItemDialogListner_ClickBtnOk);
        mDeleteItemDialog.setNegativeButton( "CanCel", mDeleteItemDialogListner_ClickBtnCancel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.content_playlist, null);

        // 1. 리스트 옵션 설정
        // 옵션 레이아웃. 아이템 선택 여부에 따라 보여졌다 안보여졌다 해야함.
        mItemOptionLayout = (LinearLayout) view.findViewById(R.id.playlist_selected_item_option);
        // 옵션 레이아웃 내 버튼 클릭 이벤트 핸들러 정의
        view.findViewById(R.id.playlist_show_detail_btn).setOnClickListener(mClickListener);
        view.findViewById(R.id.playlist_set_for_play_btn).setOnClickListener(mClickListener);

        // 2. 플레이 리스트뷰
        ListView itemListView = (ListView) view.findViewById(R.id.playlistview);
        // 아답터 연결
        itemListView.setAdapter( PlayListAdapter.getInstance() );
        // 클릭 이벤트 핸들러 정의: 내 퀴즈 디테일 보기
        itemListView.setOnItemClickListener(mListItemClickListener);
        // 롱 클릭 이벤트 핸들러 정의: 내 퀴즈 삭제
        itemListView.setOnItemLongClickListener(mListItemLongClickListener);
        // 데이터 변경에 대한 ui 리프레시 요청
        PlayListAdapter.getInstance().notifyDataSetChanged();

        // 3. 아이템 선택시 나오는 옵션 창이 열려있으면 닫기
        hideOptions();

        view.bringToFront(); // 리스트가 길어질 경우 가장 위로 스크롤.
        return view;
    }

    @Override
    public void onResume() {
        // 툴바에 현 프래그먼트 제목 출력
        ((MainActivity)getActivity()).setActionBarTitle( mTITLE );
        super.onResume();
    }

    // 플레이 리스트 클릭 이벤트 리스너
    private AdapterView.OnItemClickListener mListItemClickListener
                                        = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView parent, View v, int position, long id) {

            mSelectedItemIndex = position;

            // get item
            PlayListItem item = (PlayListItem) parent.getItemAtPosition(position) ;
            String titleStr = item.getTitle() ;
            String descStr = item.getDesc() ;
            Drawable iconDrawable = item.getIcon() ;

            // 새 커스텀 퀴즈 리스트 만들기
            if( Const.Text_New.equals(titleStr) )
                ((MainActivity)getActivity()).changeViewFragment( Const.View.NEW_CUSTOM_QUIZ );  // activity에게 리스트추가 화면으로 전환요청.
            else // 옵션 선택 버튼 띄우기
                showOptions();
        }
    };

    // 플레이 리스트 롱클릭 이벤트 리스너 : 퀴즈 삭제 다이알로그 보이기
    private AdapterView.OnItemLongClickListener mListItemLongClickListener
            = new AdapterView.OnItemLongClickListener()
    {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id)
        {
            mSelectedItemIndex = position;
            PlayListItem item = (PlayListItem) parent.getItemAtPosition(position) ;
            // 아이템 삭제 확인 다이알로그 띄우기
            String msg = item.getTitle() + " 을 삭제하시겠습니까?";
            mDeleteItemDialog.setMessage( msg );
            mDeleteItemDialog.show();
            return true;
        }
    };

    // 퀴즈 삭제 다이알로그 OK 클릭 이벤트
    DialogInterface.OnClickListener mDeleteItemDialogListner_ClickBtnOk = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface d, int which) {
            // 내 퀴즈 삭제
            Boolean bOK = deletePlayList();
            if( bOK  )
                addPlayListAfterProcess();
        }
    };

    // 퀴즈 삭제 다이알로그 버튼 클릭 이벤트 CANCEL
    DialogInterface.OnClickListener mDeleteItemDialogListner_ClickBtnCancel = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface d, int which) {
            // 다이알로그 닫기
            d.dismiss();
        }
    };


    // 리스트 아이템 옵션 버튼 클릭 리스너
    private Button.OnClickListener mClickListener = new View.OnClickListener() {
        public void onClick(View v) {
        switch(v.getId()){
            case R.id.playlist_set_for_play_btn:
                // 플레이용 퀴즈로 설정
                boolean bOK = PlayQuizFragment.setQuizList( mSelectedItemIndex );
                String msg = (bOK)? "게임플레이용 퀴즈가 변경되었습니다.":"게임플레이용 퀴즈 변경에 실패했습니다.";
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
                break;

            case R.id.playlist_show_detail_btn:
                // 디테일 보기로 화면 전환
                ((MainActivity)getActivity()).changeViewFragment( Const.View.DETAILQUIZ );
                // 옵션 창 닫기
                hideOptions();
                break;
        }
        }
    };

    private void showOptions( )
    {
        mItemOptionLayout.setVisibility(View.VISIBLE);
    }

    private void hideOptions()
    {
        mItemOptionLayout.setVisibility(View.INVISIBLE);
    }

    private Boolean deletePlayList()
    {
        if( true == isEmptyPlayList() ) {
            return false;
        }

        boolean bDeleted = PlayListAdapter.getInstance().deleteItem( mSelectedItemIndex );
        if( false == bDeleted ) {
            Log.e("Tag", "Failed Deleting customPlaylist. Idx:" + mSelectedItemIndex);
            return false;
        }

        Toast.makeText(getActivity(), "내 퀴즈가 삭제되었습니다", Toast.LENGTH_SHORT).show();
        return true;
    }

    private boolean isEmptyPlayList()
    {
        int playListCount = PlayListAdapter.getInstance().getCount();
        return (playListCount <= 0);
    }

    private void addPlayListAfterProcess()
    {
        // mAdapter.notifyDataSetChanged();
        PlayListAdapter.getInstance().notifyDataSetChanged(); // playlist
    }

    // Activity와 통신을 위해, ShowList를 activity에 연결.
    // .. 하나의 앱은, 하나의 activity를 가진다. 하나의 activity에서 여러 화면전환을 컨트롤.
    // .. 전환되는 각 화면들은 프래그먼트로 만들어야 함.
    // .. Activity <-> fragment간 통신을 위해 아래와 같이, activity에게 showlist 프래그먼트를 연결.
    @Override
    public void onAttach( Context context ) {
        super.onAttach( context );
        this.mOnPlayListBtnClickListener = (OnPlayListButtonClickListener)context;
    }
}
