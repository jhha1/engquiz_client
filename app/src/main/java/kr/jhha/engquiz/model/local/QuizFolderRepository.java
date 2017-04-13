package kr.jhha.engquiz.model.local;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import kr.jhha.engquiz.model.remote.AsyncNet;
import kr.jhha.engquiz.model.remote.EProtocol;
import kr.jhha.engquiz.model.remote.EProtocol2;
import kr.jhha.engquiz.util.exception.EResultCode;
import kr.jhha.engquiz.model.remote.Request;
import kr.jhha.engquiz.model.remote.Response;

/**
 * Created by thyone on 2017-03-19.
 */

public class QuizFolderRepository {

    public interface GetQuizFolderCallback {
        void onSuccess( List<QuizFolder> quizFolders );
        void onFail(EResultCode resultCode);
    }

    public interface AddQuizFolderCallback {
        void onSuccess( List<QuizFolder> updatedQuizFolders );
        void onFail(EResultCode resultCode);
    }

    public interface DelQuizFolderCallback {
        void onSuccess( List<QuizFolder> updatedQuizFolders );
        void onFail(EResultCode resultCode);
    }

    public interface ChangePlayingQuizFolderCallback {
        void onSuccess( QuizFolder playingQuizFolder );
        void onFail(EResultCode resultCode);
    }

    public interface GetQuizFolderScriptListCallback {
        void onSuccess( List<Integer> scriptIds );
        void onFail(EResultCode resultCode);
    }

    public interface AddScriptIntoQuizFolderCallback {
        void onSuccess( List<Integer> updatedScriptIds );
        void onFail(EResultCode resultCode);
    }

    public interface DelQuizFolderScriptCallback {
        void onSuccess( List<Integer> updatedScriptIds );
        void onFail(EResultCode resultCode);
    }

    private static QuizFolderRepository instance = new QuizFolderRepository();
    private QuizFolderRepository() {}
    public static QuizFolderRepository getInstance() {
        return instance;
    }


    private List<QuizFolder> mQuizFolders = new ArrayList<>();
    private Integer MAX_QUIZ_GROUP_LIST_COUNT = 30;

    public Integer getMAX_QUIZ_GROUP_LIST_COUNT() {
        return MAX_QUIZ_GROUP_LIST_COUNT;
    }

    public Integer getQuizFolderCount(){
        return this.mQuizFolders.size();
    }

    public boolean isExistQuizFolder( String quizFolderName ) {
        Integer quizFolderId = getQuizFolderIdByName( quizFolderName );
        return isExistQuizFolder(quizFolderId);
    }

    public boolean isExistQuizFolder( Integer quizFolderId ) {
        // TODO check 방식. exception thorw? or boolean

        if( quizFolderId < 0 ){
            return false;
        }
        QuizFolder folder = getQuizFolderById( quizFolderId );
        return ! QuizFolder.isNull(folder);
    }

    public Integer getQuizFolderIdByName( String quizFolderName ){
        for( QuizFolder quizFolder: mQuizFolders){
            if( quizFolder.getTitle().equals(quizFolderName)){
                return quizFolder.getId();
            }
        }
        return -1;
    }

    public String getQuizFolderNameById( Integer quizFolderId ){
        for( QuizFolder quizFolder: mQuizFolders){
            if( quizFolder.getId() == quizFolderId ){
                return quizFolder.getTitle();
            }
        }
        return null;
    }

    public QuizFolder getQuizFolderById( Integer quizFolderId ){
        quizFolderId = QuizFolder.checkQuizFolderID(quizFolderId);
        for( QuizFolder folder : mQuizFolders){
            if( folder.getId() == quizFolderId ){
                return folder;
            }
        }
        return null;
    }

    public List<String> getQuizFolderNames(){
        if( mQuizFolders == null || mQuizFolders.isEmpty() ){
            return new LinkedList<>();
        }

        List<String> names = new LinkedList<>();
        for(QuizFolder quizFolder : mQuizFolders){
            names.add( quizFolder.getTitle() );
        }
        return names;
    }

    public void getQuizFolders( final QuizFolderRepository.GetQuizFolderCallback callback ) {

        Log.d("##############","QuizFolderRepo.getQuizFolders()"+((mQuizFolders!=null)?mQuizFolders.toString():null));

        if( null != mQuizFolders && false == mQuizFolders.isEmpty() ){
            callback.onSuccess(mQuizFolders);
            return;
        }

        Request request = new Request( EProtocol2.PID.GetUserQuizFolders);
        Integer userId = UserRepository.getInstance().getUserID();
        request.set(EProtocol.UserID, userId);
        AsyncNet net = new AsyncNet( request, onGetQuizFolders(callback) );
        net.execute();
    }

    private AsyncNet.Callback onGetQuizFolders(final QuizFolderRepository.GetQuizFolderCallback callback ) {
        return new AsyncNet.Callback() {
            @Override
            public void onResponse(Response response) {
                if (response.isSuccess()) {
                    List<String> quizFolders = (List)response.get(EProtocol.QuizFolders);
                    Log.e("AppContent", "onGetQuizFolders(): "+ quizFolders.toString());
                    // save quiz folders
                    List<QuizFolder> deserializedQuizFolders = deserializeAndSaveMemoryQuizFolderAll( quizFolders );
                    callback.onSuccess( deserializedQuizFolders );
                } else {
                    // 서버 응답 에러
                    Log.e("AppContent", "onGetQuizFolders() UnkownERROR : "+ response.getResultCodeString());
                    callback.onFail( response.getResultCode() );
                }
            }
        };
    }

    // 퀴즈폴더 생성한 경우.
    // 서버에 저장 -> 클라에 저장.
    public void addQuizFolder( String quizFolderTitle, List<Integer> scriptIds, final AddQuizFolderCallback callback ){
        Request request = new Request( EProtocol2.PID.AddUserQuizFolder );
        request.set(EProtocol.QuizFolderTitle, quizFolderTitle);
        request.set(EProtocol.ScriptIds, scriptIds);
        AsyncNet net = new AsyncNet( request, onAddQuizFolder(callback) );
        net.execute();
    }

    private AsyncNet.Callback onAddQuizFolder( final AddQuizFolderCallback callback ) {
        return new AsyncNet.Callback() {
            @Override
            public void onResponse(Response response) {
                if (response.isSuccess()) {
                    List<String> quizFolders = (List)response.get(EProtocol.QuizFolders);
                    Log.e("AppContent", "onAddQuizFolder(): "+ quizFolders.toString());
                    // save quiz folders
                    List<QuizFolder> deserializedQuizFolders = deserializeAndSaveMemoryQuizFolderAll( quizFolders );
                    callback.onSuccess( deserializedQuizFolders );
                } else {
                    // 서버 응답 에러
                    Log.e("AppContent", "onGetQuizFolders() UnkownERROR : "+ response.getResultCodeString());
                    callback.onFail( response.getResultCode() );
                }
            }
        };
    }

    public void delQuizFolder( Integer userId, Integer quizfolderId, final DelQuizFolderCallback callback ){
        Request request = new Request( EProtocol2.PID.DelUserQuizFolder );
        request.set(EProtocol.UserID, userId);
        request.set(EProtocol.QuizFolderId, quizfolderId);
        AsyncNet net = new AsyncNet( request, onDelQuizFolder(callback) );
        net.execute();
    }

    private AsyncNet.Callback onDelQuizFolder( final DelQuizFolderCallback callback ) {
        return new AsyncNet.Callback() {
            @Override
            public void onResponse(Response response) {
                if (response.isSuccess()) {
                    List<String> quizFolders = (List)response.get(EProtocol.QuizFolders);
                    Log.e("AppContent", "onDelQuizFolder(): "+ quizFolders.toString());
                    // save summaries.
                    List<QuizFolder> deserializedQuizFolders = deserializeAndSaveMemoryQuizFolderAll( quizFolders );
                    callback.onSuccess(deserializedQuizFolders);
                } else {
                    // 서버 응답 에러
                    Log.e("AppContent", "onGetQuizFolders() UnkownERROR : "+ response.getResultCodeString());
                    callback.onFail( response.getResultCode() );
                }
            }
        };
    }

    private List<QuizFolder> deserializeAndSaveMemoryQuizFolderAll( List<String> quizFolders ){

        List<QuizFolder> deserializedQuizFolders = new ArrayList<>();

        for( String quizFolderString : quizFolders ) {
            QuizFolder obj = new QuizFolder();
            obj.deserialize(quizFolderString);
            deserializedQuizFolders.add(obj);
        }

        // 1. save into memory.
        if( this.mQuizFolders == null )
            this.mQuizFolders = new ArrayList<>();

        this.mQuizFolders.clear();
        for(QuizFolder folder: deserializedQuizFolders){
            this.mQuizFolders.add(folder);
        }
        return deserializedQuizFolders;

        // 2. No save into file.
        //     -> download summaries from a server when a quizfolder menu first clicked.
    }

    /*
        서버 데이터 변경 성공시, 클라 데이터 변경
        클라 데이터 변경 실패시, 앱 재접속 유도해서
        서버로부터 받은 플레잉퀴즈폴더 데이터로 다시 셋팅하게 함
     */
    public void changePlayingQuizFolder( QuizFolder quizfolder,
                                         final ChangePlayingQuizFolderCallback callback )
    {
        Log.i("AppContent", "QuizPlayRepository changePlayingQuizFolder() called.");

        // change folder of Server
        final Integer userId = UserRepository.getInstance().getUserID();
        Request request = new Request( EProtocol2.PID.ChangePlayingQuizFolder );
        request.set(EProtocol.UserID, userId);
        request.set(EProtocol.QuizFolderId, quizfolder.getId());
        AsyncNet net = new AsyncNet( request, onChangePlayingQuizFolder(quizfolder, callback) );
        net.execute();
    }

    private AsyncNet.Callback onChangePlayingQuizFolder(final QuizFolder newQuizFolder,
                                                        final ChangePlayingQuizFolderCallback callback ) {
        return new AsyncNet.Callback() {
            @Override
            public void onResponse(Response response) {
                if (response.isSuccess()) {

                    List<String> quizFolders = (List)response.get(EProtocol.QuizFolders);
                    Log.e("AppContent", "onChangePlayingQuizFolder(): "+ quizFolders.toString());
                    deserializeAndSaveMemoryQuizFolderAll( quizFolders );
                    callback.onSuccess( newQuizFolder );
                } else {
                    // 서버 응답 에러
                    Log.e("AppContent", "onGetQuizFolders() UnkownERROR : "+ response.getResultCodeString());
                    callback.onFail( response.getResultCode() );
                }
            }
        };
    }


    /*
       서버에서 UI순서 소팅된 scriptId list를
        그대로 클라메모리에 저장하므로,
        List index == uiOderNumber
    */
    public String getQuizFolderScriptTitleByUIOrder(int quizFolderId, int uiOrderNumber ){
        if( uiOrderNumber <= 0 ) {
            Log.e("########", "invalid uiOrderNumber. " +
                    "quizFolderId:"+quizFolderId + ", uiOrderNumber:"+uiOrderNumber);
            return new String();
        }

        List<Integer> scriptIds = getQuizFolderScriptIDs(quizFolderId);
        Integer scriptId = scriptIds.get(uiOrderNumber);
        if( scriptId <= 0 ) {
            Log.e("########", "invalid scriptId. " +
                    "quizFolderId:"+quizFolderId + ", uiOrderNumber:"+uiOrderNumber+", scriptId:"+scriptId);
            return new String();
        }

        return ScriptRepository.getInstance().getScriptTitleById(scriptId);
    }

    public List<Integer> getQuizFolderScriptIDs(Integer quizFolderId ) {
        quizFolderId = QuizFolder.checkQuizFolderID(quizFolderId);
        QuizFolder quizFolder = getQuizFolderById( quizFolderId );
        if( QuizFolder.isNull(quizFolder) ){
            Log.e("########", "getQuizFolderScriptIDs() quizFolder is null. quizFolderId:"+quizFolderId);
            return null;
        }
        return quizFolder.getScriptIds();
    }

    public void getQuizFolderScriptList(Integer userId, Integer quizFolderId, final GetQuizFolderScriptListCallback callback ) {
        // TODO script Id + title 같이 넘기자.
        List<Integer> scrpitIds = getQuizFolderScriptIDs(quizFolderId);
        if( scrpitIds != null && ! scrpitIds.isEmpty() ){
            callback.onSuccess(scrpitIds);
            return;
        }

        Request request = new Request( EProtocol2.PID.GetUserQuizFolderDetail);
        request.set(EProtocol.UserID, userId);
        request.set(EProtocol.QuizFolderId, quizFolderId);
        AsyncNet net = new AsyncNet( request, onGetQuizFolderScriptList(callback) );
        net.execute();
    }

    private AsyncNet.Callback onGetQuizFolderScriptList(final GetQuizFolderScriptListCallback callback ) {
        return new AsyncNet.Callback() {
            @Override
            public void onResponse(Response response) {
                if (response.isSuccess()) {
                    final Integer quizFolderId = (Integer)response.get(EProtocol.QuizFolderId);
                    final List<Integer> quizFolderScriptIds = (List)response.get(EProtocol.ScriptIds);
                    Log.e("AppContent", "onGetQuizFolderScriptList(): "+ quizFolderScriptIds.toString());
                    // save quiz folders
                    overwriteQuizFolderScriptIdList( quizFolderId, quizFolderScriptIds );
                    callback.onSuccess(quizFolderScriptIds);
                } else {
                    // 서버 응답 에러
                    Log.e("AppContent", "onGetQuizFolders() UnkownERROR : "+ response.getResultCodeString());
                    callback.onFail( response.getResultCode() );
                }
            }
        };
    }


    public void addScriptIntoQuizFolder(Integer quizFolderId, Integer scriptId, final AddScriptIntoQuizFolderCallback callback ){
        Request request = new Request( EProtocol2.PID.AddUserQuizFolderDetail );
        Integer userId = UserRepository.getInstance().getUserID();
        request.set(EProtocol.UserID, userId);
        request.set(EProtocol.QuizFolderId, quizFolderId);
        request.set(EProtocol.ScriptId, scriptId);
        AsyncNet net = new AsyncNet( request, onAddQuizFolderDetail(quizFolderId, callback) );
        net.execute();
    }

    private AsyncNet.Callback onAddQuizFolderDetail( final Integer quizFolderId, final AddScriptIntoQuizFolderCallback callback ) {
        return new AsyncNet.Callback() {
            @Override
            public void onResponse(Response response) {
                if (response.isSuccess()) {
                    List<Integer> quizFolderScriptIdList = (List)response.get(EProtocol.ScriptIds);
                    Log.e("AppContent", "onAddQuizFolderDetail(): "+ quizFolderScriptIdList.toString());
                    // save script list
                    overwriteQuizFolderScriptIdList( quizFolderId, quizFolderScriptIdList );
                    callback.onSuccess( quizFolderScriptIdList );
                } else {
                    // 서버 응답 에러
                    Log.e("AppContent", "onGetQuizFolders() UnkownERROR : "+ response.getResultCodeString());
                    callback.onFail( response.getResultCode() );
                }
            }
        };
    }

    public Integer getQuizFolderScriptsCount( Integer quizFolderId ){
        List<Integer> scriptIds = getQuizFolderScriptIDs(quizFolderId);
        if( scriptIds == null ){
            Log.e("########", "getQuizFolderScriptsCount() scriptIds is null. quizFolderId:"+quizFolderId);
            return 0;
        }
        return scriptIds.size();
    }

    public void delQuizFolderScript( Integer userId, Integer quizfolderId, Integer scriptId, final DelQuizFolderScriptCallback callback ){
        Request request = new Request( EProtocol2.PID.DelUserQuizFolderDetail );
        request.set(EProtocol.UserID, userId);
        request.set(EProtocol.QuizFolderId, quizfolderId);
        request.set(EProtocol.ScriptId, scriptId);
        AsyncNet net = new AsyncNet( request, onDelScript(callback) );
        net.execute();
    }

    private AsyncNet.Callback onDelScript( final DelQuizFolderScriptCallback callback ) {
        return new AsyncNet.Callback() {
            @Override
            public void onResponse(Response response) {
                if (response.isSuccess()) {
                    final Integer quizFolderId = (Integer)response.get(EProtocol.QuizFolderId);
                    final List<Integer> quizFolderScriptIds = (List)response.get(EProtocol.ScriptIds);
                    Log.e("AppContent", "onDelScript(): "+ quizFolderScriptIds.toString());
                    // save summaries.
                    overwriteQuizFolderScriptIdList( quizFolderId, quizFolderScriptIds );
                    callback.onSuccess(quizFolderScriptIds);
                } else {
                    // 서버 응답 에러
                    Log.e("AppContent", "onGetQuizFolders() UnkownERROR : "+ response.getResultCodeString());
                    callback.onFail( response.getResultCode() );
                }
            }
        };
    }

    // 퀴즈폴더 디테일 (스크립트 리스트 보기/스크립트 추가/스크립트 삭제 시 호출)
    // 스크립트 변동에 따른 UI 재소팅 결과를 저장.
    // 클라에 저장. 서버는 이미 저장됨.
    public void overwriteQuizFolderScriptIdList(Integer quizFolderId, List<Integer> scriptIds )
    {
        for( QuizFolder quizFolder : mQuizFolders ){
            if( quizFolder.getId() == quizFolderId ){
                quizFolder.setScriptIds(scriptIds);
            }
        }
    }
}