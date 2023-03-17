package com.project.agroworld.taskmanager.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.project.agroworld.db.FarmerDAO;
import com.project.agroworld.db.FarmerDatabase;
import com.project.agroworld.db.FarmerModel;

import java.util.List;

public class DatabaseRepository {
    private FarmerDAO dao;
    private LiveData<List<FarmerModel>> farmerRoutines;
    private LiveData<Integer> maxIdCount;

    public DatabaseRepository(Application application) {
        FarmerDatabase database = FarmerDatabase.getInstance(application);
        dao = database.taskDao();
        farmerRoutines = dao.getFarmerRoutines();
        maxIdCount = dao.getMaxCount();
    }

    public void insert(FarmerModel model) {
        new InsertRoutineAsyncTask(dao).execute(model);
    }
    public void update(FarmerModel model) {
        new UpdateRoutineAsyncTask(dao).execute(model);
    }

    public void deleteAllCourses() {
        new DeleteAllCoursesAsyncTask(dao).execute();
    }

    public void delete(FarmerModel model) {
        new DeleteRoutineAsyncTask(dao).execute(model);
    }
    public LiveData<List<FarmerModel>> getFarmerRoutines() {
        return farmerRoutines;
    }

    public LiveData<Integer> getMaxIdCount() {return maxIdCount;}
    private static class InsertRoutineAsyncTask extends AsyncTask<FarmerModel, Void, Void> {
        private FarmerDAO dao;

        private InsertRoutineAsyncTask(FarmerDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(FarmerModel... model) {
            // below line is use to insert our modal in dao.
            dao.insert(model[0]);
            return null;
        }
    }

    private static class UpdateRoutineAsyncTask extends AsyncTask<FarmerModel, Void, Void> {
        private FarmerDAO dao;

        private UpdateRoutineAsyncTask(FarmerDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(FarmerModel... models) {
            dao.update(models[0]);
            return null;
        }
    }

    private static class DeleteRoutineAsyncTask extends AsyncTask<FarmerModel, Void, Void> {
        private FarmerDAO dao;

        private DeleteRoutineAsyncTask(FarmerDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(FarmerModel... models) {
            dao.delete(models[0]);
            return null;
        }
    }

    private static class DeleteAllCoursesAsyncTask extends AsyncTask<Void, Void, Void> {
        private FarmerDAO dao;

        private DeleteAllCoursesAsyncTask(FarmerDAO dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // on below line calling method
            // to delete all courses.
            dao.deleteRoutines();
            return null;
        }
    }
}
