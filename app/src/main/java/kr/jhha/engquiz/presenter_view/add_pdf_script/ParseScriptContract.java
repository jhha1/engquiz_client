package kr.jhha.engquiz.presenter_view.add_pdf_script;

import java.util.List;

/**
 * Created by thyone on 2017-03-15.
 */

public class ParseScriptContract {

    interface View {
        void showMsg( int what, String arg );
        void showErrorDialog(int what );

        void showQuizFolderSelectDialog( List<String> quizFolderList  );
        void showNeedMakeQuizFolderDialog();
        void showNewQuizFolderTitleInputDialog();

        void showAddScriptConfirmDialog( String filename, Float fileSize);
        void showAddScriptSuccessDialog( String quizFolderName );

        void showLoadingDialog();
        void closeLoadingDialog();

        void showCurrentDirectoryPath( String path );
        void showFileListInDirectory( List<String> fileList );
    }

    interface ActionsListener {
        void initDirectoryLocationAndAvailableFiles();
        void onFileListItemClick( int position );

        void scriptSelected();
        void quizFolderSelected( String quizFolderName );
        void newQuizFolderTitleInputted(String quizFolderName);

        void addScript( String pdfFilename);
    }
}