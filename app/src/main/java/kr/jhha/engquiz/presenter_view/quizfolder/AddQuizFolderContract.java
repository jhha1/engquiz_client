package kr.jhha.engquiz.presenter_view.quizfolder;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import kr.jhha.engquiz.model.local.QuizFolder;

/**
 * Created by thyone on 2017-03-15.
 */

public class AddQuizFolderContract {

    interface View {
        void setAdapter(ArrayAdapter<String> adapter);

        void showEmptyScriptDialog();
        void showQuizFolderTitleDialog();
        void showAddQuizFolderConfirmDialog();

        void onSuccessAddQuizFolder( List<QuizFolder> updatedQuizFolders );
        void onFailAddQuizFolder( String msg );

        void clearUI();
        void returnToQuizFolderFragment();
    }

    interface ActionsListener {
        void initScriptList();

        Integer checkInputtedTitle(String title );
        void scriptsSelected();
        void addQuizFolder( String title, ListView listView);

        void emptyScriptDialogOkButtonClicked();
    }
}