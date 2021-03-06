package kr.jhha.engquiz.util;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import kr.jhha.engquiz.util.exception.EResultCode;
import kr.jhha.engquiz.util.exception.system.MyIllegalStateException;
import kr.jhha.engquiz.util.ui.MyLog;

import static kr.jhha.engquiz.util.AsyncFileHandler.ACTION.MakeDir;
import static kr.jhha.engquiz.util.AsyncFileHandler.ACTION.Write;

/**
 * Created by jhha on 2016-10-14.
 */

public class FileHelper2
{
    public interface MakeDirectoryCallback {
        void onSuccess();
        void onFail();
    }

    public interface ReadFileCallback {
        void onSuccess(byte[] file);
        void onFail();
    }

    public interface WriteFileCallback {
        void onSuccess();
        void onFail();
    }

    private static final FileHelper2 singletonInstance = new FileHelper2();

    public static File mRoot = null;
    public static String mRootAbsolutePath = null;

    public static final String TOP_ROOT_DIR = "/";
    public static final String KaKaoDownloadFolder_AndroidPath = "/KakaoTalkDownload/";
    public static final String AppRoot_AndroidPath = "/EnglishSentenceQuiz/";
    public static final String ParsedFile_AndroidPath = AppRoot_AndroidPath + "Parsed/";
    public static final String UserInfoFolderPath = AppRoot_AndroidPath + "User/";
    public static final String PlayInfoFolderPath = AppRoot_AndroidPath + "Play/";
    public static final String UserInfoFileName = "userInfo.txt";
    public static final String PlayInfoFileName = "playInfo.txt";

    private FileHelper2() {}
    public static FileHelper2 getInstance() {
        return singletonInstance;
    }

    public boolean init()
    {
        // set root
        File root = null;
        String sdcard = Environment.getExternalStorageState();
        if(false == sdcard.equals(Environment.MEDIA_MOUNTED)) {
            // sd카드가 마운트 안되있음
            root = Environment.getRootDirectory();
        } else {
            // sd카드가 마운트되있음
            root = Environment.getExternalStorageDirectory();
        }

        if(root == null) {
            MyLog.e("Root Dir is null");
            return false;
        }

        this.mRoot = root;
        this.mRootAbsolutePath = root.getAbsolutePath();
        return true;
    }



    public boolean isRootDirectory( String dirPath ) {
        return mRootAbsolutePath.equals(dirPath);
    }

    public Boolean isAbsoluteDirectory( String dirPath ) {
        return mRootAbsolutePath.contains(dirPath);
    }

    public boolean isExist(String releativeDirPath, String fileName){
        String absolutePath = getAbsolutePath(releativeDirPath);
        String filePath = absolutePath + fileName;
        File file = new File( filePath );
        return file.exists();
    }

    public String getAbsolutePath(String path)
    {
        if( StringHelper.isNull( path ) ) {
            MyLog.e("File Path is null. path:["+ path +"]");
            return null;
        }
        if( isAbsoluteDirectory( path ) )
            return path;

        String absolutePath = mRootAbsolutePath + path;
        return absolutePath;
    }

    public String getParentDirectoryName( String childDirPath )
    {
        if( StringHelper.isNull( childDirPath ) ) {
            MyLog.e("childDirPath is null");
            return new String();
        }

        File file = new File(childDirPath);
        if( file == null ) {
            MyLog.e("childDirPath - File is null");
            return new String();
        }

        String parentDir = file.getParent();
        if( parentDir == null ) {
            MyLog.e("parentDir is null");
            return new String();
        }

        // 원하는건 물리적 최상위 root경로(/)가 아니라,
        // 가상 vm의 root경로( storage/emulate/0 ) 이다.
        if( TOP_ROOT_DIR.equals(parentDir) )
            return mRootAbsolutePath;

        return getAbsolutePath(parentDir);
    }


    /*
        Read File
     */
    public String readFile(String dir, String name) {
        dir = getAbsolutePath( dir );
        if( StringHelper.isNull( dir ) || StringHelper.isNull( name ) ) {
            MyLog.e("invalid filePath or fileName. dir["+ dir +"], name["+name+"]");
            return StringHelper.EMPTY_STRING;
        }
        String path = dir + name;
        return readFile( path );
    }

    public String readFile( String path )
    {
        MyLog.d("filepath("+ path +")");

        StringBuffer buffer= new StringBuffer();
        File file= new File(path);
        if( ! file.exists() ) {
            MyLog.e("file no exist. path:"+path);
            throw new MyIllegalStateException(EResultCode.NOEXSIT);
        }

        try
        {
            FileReader in = new FileReader(file);
            BufferedReader reader = new BufferedReader(in);

            String str = reader.readLine();
            while (str != null) {
                buffer.append(str);
                str = reader.readLine(); //한 줄씩 읽어오기
            }

            reader.close();
            return buffer.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return StringHelper.EMPTY_STRING;
        }
    }

    /*
        File file = new File( dirpath + filename );

        dirpath 마지막에 / 가 붙여 있던지,
        filename 앞에 / 가 있어야 함.
     */
    public byte[] readBinary( String dirpath, String filename )
    {
        if (StringHelper.isNull(dirpath) || StringHelper.isNull(filename)) {
           MyLog.e("path or name is null. dir[" + dirpath + "], name[" + filename + "]");
            return null;
        }

        FileInputStream fis = null;
        try
        {
            File file = new File( dirpath + filename );
            fis = new FileInputStream( file );
            int fileSize = (int)file.length();
            byte [] b = new byte [fileSize]; //임시로 읽는데 쓰는 공간
            MyLog.d("dir("+ dirpath+"), filename("+filename+"), fileLen("+b.length+")");
            fis.read(b);

            // System.out.println(new String(b)); //필요에 따라 스트링객체로 변환
            return b;
        } catch (Exception e) {
            MyLog.e("Failed readBinary. dir("+ dirpath +") name("+filename+")");
            e.printStackTrace();
        } finally {
            try {
                if( fis != null )
                    fis.close();
            } catch (Exception e) {
                MyLog.e("Failed to close file IO. dir("+ dirpath +") name("+filename+")");
                e.printStackTrace();
            }
        }
        return null;
    }


    public List listFileNames( String dirPath ) {
        File[] files = listFiles( dirPath );
        if( files == null ) {
            MyLog.e("listFiles is null");
            return Collections.EMPTY_LIST;
        }

        List<String> fileNames = new LinkedList<>();
        for( File file : files ) {
            fileNames.add( file.getName() );
        }
        return fileNames;
    }

    public File[] listFiles( String dirPath )
    {
        if( StringHelper.isNull( dirPath ) ) {
            MyLog.e("File Path is null. path:["+ dirPath +"]");
            return null;
        }

        dirPath = getAbsolutePath( dirPath );
        File directory = new File( dirPath );
        if( directory == null ) {
            MyLog.e("Directory is null. path: " + dirPath);
            return null;
        }

        File filelist[] = directory.listFiles();
        if( filelist == null ) {
            MyLog.e("directory is null. path: " + dirPath);
            return null;
        }
        // 디렉토리안에 파일이 없을때,
        // directory.listFiles() 에서 ".txt"라는 파일이 하나 있다고 리턴함. <- 버그인듯.
        // 하여, 빈 디렉토리인지는 아래와 같이 체크.
        if((filelist.length == 1 && ".txt".equals(filelist[0].getName())) ) {
            MyLog.e("directory is empty. path: " + dirPath);
            return null;
        }
        return filelist;
    }

    // 현재 디렉토리 하이락키, 파일 가져오기
    // extension : 해당 확장자가 있는 파일만 가져옴
    public Map loadFileListInDirectory( String dirName, String extension ) {
        File files[] = listFiles(dirName);
        if (files == null) {
            MyLog.e("Directory is null. mFilepath:" + dirName);
            return Collections.emptyMap();
        }

        List items = new ArrayList<String>();    // 리스트에 보여질 파일이름 리스트
        List filepath = new ArrayList<String>(); // 파일이름에 해당하는 실제 파일로케이션

        // 현재 디렉토리가 루트가 아니면, 현재폴더 상위 디렉토리를 뷰 리스트에 삽입
        if (false == isRootDirectory(dirName)) {
            items.add("../"); // 상위 디렉토리로 이동 텍스트
            String parentDir = getParentDirectoryName(dirName);
            filepath.add(parentDir); // 상위 디렉토리 경로 삽입
        }

        // 폴더 내 파일들을 뷰 리스트에 삽입
        for (File file : files) {
            if( ! StringHelper.isNull(extension) ){
                // 디렉토리와 특정 확장자 파일만 표시
                boolean bHasExtention = file.getName().contains(extension);
                boolean bHideFile = (false == file.isDirectory()) && (false == bHasExtention);
                if( bHideFile )
                    continue;
            }

            filepath.add(file.getPath());  // file path = file dir + file name
            String fileName = (file.isDirectory()) ? file.getName() + "/" : file.getName();
            items.add(fileName);
        }

        Map<String, List> listMap = new HashMap<>();
        listMap.put("path", filepath);
        listMap.put("file", items);
        return listMap;
    }

    public List<String> uploadParsedTextFiles() throws IOException
    {
        String path = getAbsolutePath( ParsedFile_AndroidPath );
        File files[] = listFiles( ParsedFile_AndroidPath  );
        if( files == null ) {
            MyLog.e( "uploadParsedTextFiles is failed. Directory is null. path: " + path);
            return null;
        }
        MyLog.d("Dir("+path+"), accessedFileCount("+files.length+")");

        List<String> scriptTextList = new LinkedList<String>();
        for( File file : files ) {
            String scriptText = readFile(ParsedFile_AndroidPath, file.getName());
            if( scriptText.isEmpty() ) {
                MyLog.e("scriptText is empty. file["+path+"/"+file.getName()+"]");
                continue;
            }
            scriptTextList.add( scriptText );
        }
        return scriptTextList;
    }

    /*
        WRITE FILE
     */
    // 파일이 존재하면 덮어씀
    public void overwrite(String dirPath, String fileName, String text, final WriteFileCallback callback)
    {
        boolean bDeleted = deleteFile(dirPath, fileName);
        if( ! bDeleted ) {
            MyLog.e("Failed deleteFile IO. path("+ dirPath +") name("+fileName+")");
            callback.onFail();
        }

        Bundle args = new Bundle();
        args.putString("dirPath", dirPath);
        args.putString("fileName", fileName);
        args.putString("text", text);
        AsyncFileHandler asyncHandler = new AsyncFileHandler(Write, args, onWrite(callback));
        asyncHandler.execute();
    }

    private AsyncFileHandler.Callback onWrite( final WriteFileCallback callback ) {
        return new AsyncFileHandler.Callback() {
            @Override
            public void onResult(Object result) {
                MyLog.e("!!!!!!!!!!!!!!!!!!!!!!!!!!! result. " + result);
                Boolean bOK = (Boolean) ((result!=null)? result : false);
                if( bOK ) {
                    callback.onSuccess();
                } else {
                    callback.onFail();
                }
            }
        };
    }

    public void makeDirectoryIfNotExist( final String directoryPath, final MakeDirectoryCallback callback )
    {
        if( isExist(directoryPath, ""))
            callback.onSuccess();

        Bundle args = new Bundle();
        args.putString("dirPath", directoryPath);
        AsyncFileHandler asyncHandler = new AsyncFileHandler(MakeDir, args, onMakeDir(callback));
        asyncHandler.execute();
    }

    private AsyncFileHandler.Callback onMakeDir( final MakeDirectoryCallback callback ) {
        return new AsyncFileHandler.Callback() {
            @Override
            public void onResult(Object result) {
                MyLog.e("!!!!!!!!!!!!!!!!!!!!!!!!!!! result. " + result);
                Boolean bMadeDirectory = (Boolean) ((result!=null)? result : false);
                if( bMadeDirectory ) {
                    callback.onSuccess();
                } else {
                    callback.onFail();
                }
            }
        };
    }

    public void createFileIfNotExist( final String relativeDirPath, final String fileName, final WriteFileCallback callback )
    {
        if( isExist(relativeDirPath, fileName) )
            callback.onSuccess();

        Bundle args = new Bundle();
        args.putString("dirPath", relativeDirPath);
        args.putString("fileName", fileName);
        args.putString("text", StringHelper.EMPTY_STRING);
        AsyncFileHandler asyncHandler = new AsyncFileHandler(Write, args, onWrite(callback));
        asyncHandler.execute();
    }

    /*
        DELETE File
     */
    public boolean deleteFile(String dirPath, String fileName) {
        String absolutePath = getAbsolutePath(dirPath);
        String filePath = absolutePath + fileName;
        File file = new File( filePath );
        if(file.exists()) {
            Boolean bDeleted = file.delete();
            if(false == bDeleted) {
                MyLog.d("["+filePath+"] " + "delete failed.");
                return false;
            }
        }
        return true;
    }

    /*
       File Size
    */
    // ROUND_FOCTOR : 소수점 n째 자리에서 반올림
    // (ex. ROUND_FOCTOR == 2 <- 둘째자리에서 반올림)
    static final int ROUND_FOCTOR = 1;
    public float getFileMegaSize( String filepath, String filename )
    {
        long bytesize = getFileByteSize(filepath, filename);
        if( bytesize <= 0) {
            MyLog.e("bytesize <= 0");
            return 0;
        }

        float megaSize = ((float)bytesize)/1000000;
        float n = 1f;
        for(int i = 0; i< ROUND_FOCTOR; ++i) {
            n *= 10f;
        }
        return Math.round(megaSize * n) / n;
    }

    public Long getFileByteSize( String filepath, String filename ) {
        if( StringHelper.isNull( filepath ) || StringHelper.isNull( filename ) ) {
            MyLog.e("path or name is null. path["+ filepath +"], name["+filename+"]");
            return 0L;
        }

        File file = new File( filepath + "/" + filename );
        if(file == null) {
            MyLog.e("no exist file. path["+ filepath +"], name["+filename+"]");
            return 0L;
        }
        return file.length();
    }

}

class AsyncFileHandler extends AsyncTask<Void, Void, Object>
{
    public interface Callback<T> {
        void onResult( Object result );
    }

    public enum ACTION {
        MakeDir(1),
        Read(2),
        Write(3);

        private Integer value;
        ACTION( Integer value )
        {
            this.value = value;
        }
        public int toInt() {return value.intValue(); }
    }

    private Bundle args;
    private ACTION action;
    private Callback callback;

    public AsyncFileHandler(ACTION what, Bundle args, Callback callback) {
        super();
        this.action = what;
        this.args = args;
        this.callback = callback;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Void... params) {
        try {
            switch ( this.action ){
                case MakeDir:
                    return makeDir(args.getString("dirPath"));
                case Write:
                    return write(args.getString("dirPath"),
                                    args.getString("fileName"),
                                    args.getString("text"));
                case Read:
                    return read(args.getString("filePath"));

            }
        } catch(Exception e){
            //
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        super.onPostExecute(result);
        this.callback.onResult(result);
    }



    public Boolean makeDir( String dirPath )
    {
        String absoluteDirectoryPath = FileHelper2.getInstance().getAbsolutePath(dirPath);
        MyLog.e("!!!!!!!!!!!!!!!!!!!!!!!!!!! absoluteDirectoryPath. " + absoluteDirectoryPath);
        File file = new File( absoluteDirectoryPath );
        MyLog.e("!!!!!!!!!!!!!!!!!!!!!!!!!!! file. " + file);
        if( file == null )
            return false;

        MyLog.e("!!!!!!!!!!!!!!!!!!!!!!!!!!! indexOf. " + (dirPath.indexOf('\u0000') < 0));
        MyLog.e("!!!!!!!!!!!!!!!!!!!!!!!!!!! mkdir. " + file.mkdir());

        boolean bOK = file.mkdirs();
        MyLog.e("!!!!!!!!!!!!!!!!!!!!!!!!!!! bOK. " + bOK);
        return bOK;
    }

    public Boolean write( String dirPath, String fileName, String text )
    {
        // 파일이 없으면 만든다
        String absolutePath = FileHelper2.getInstance().getAbsolutePath(dirPath);
        String filePath = absolutePath + fileName;
        File file = new File( filePath );
        if( ! file.exists() ) {
            boolean bMade = file.mkdirs();
            if( ! bMade ){
                MyLog.e("Failed to write. Make Directory is failed. dir["+dirPath+"]");
                return false;
            }
        }

        // 파일 쓰기
        try{
            // BufferedWriter 와 FileWriter를 조합하여 사용 (속도 향상)
            BufferedWriter fw = new BufferedWriter(new FileWriter(filePath, true));

            MyLog.d("WRITE TextFile. filePath["+filePath+"] .");
            // 파일안에 문자열 쓰기
            fw.write(text);
            fw.flush();

            // 객체 닫기
            fw.close();

            return true;
        } catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public byte[] read( String filePath )
    {
        FileInputStream fis = null;
        try
        {
            File file = new File( filePath );
            fis = new FileInputStream( file );
            int fileSize = (int)file.length();
            byte [] b = new byte [fileSize]; //임시로 읽는데 쓰는 공간
            MyLog.d("filePath("+ filePath +"), fileLen("+b.length+")");
            fis.read(b);

            // System.out.println(new String(b)); //필요에 따라 스트링객체로 변환
            return b;
        } catch (Exception e) {
            MyLog.e("Failed readBinary. filePath("+filePath+")");
            e.printStackTrace();
        } finally {
            try {
                if( fis != null )
                    fis.close();
            } catch (Exception e) {
                MyLog.e("Failed to close file IO. filePath("+filePath+")");
                e.printStackTrace();
            }
        }
        return null;
    }
}