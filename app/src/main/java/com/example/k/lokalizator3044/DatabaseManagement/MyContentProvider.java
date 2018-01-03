package com.example.k.lokalizator3044.DatabaseManagement;

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;


public class MyContentProvider extends android.content.ContentProvider {
    private DBHelper mPomocnikBD;

    private static final String IDENTYFIKATOR = "com.example.k.lokalizator3044";
    public static final Uri URI_ZAWARTOSCI = Uri.parse("content://" + IDENTYFIKATOR + "/" + DBHelper.TABLE_NAME);
    private static final int CALA_TABELA = 1;
    private static final int WYBRANY_WIERSZ = 2;
    private static final UriMatcher sDopasowanieUri = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sDopasowanieUri.addURI(IDENTYFIKATOR, DBHelper.TABLE_NAME, CALA_TABELA);
        sDopasowanieUri.addURI(IDENTYFIKATOR, DBHelper.TABLE_NAME + "/#", WYBRANY_WIERSZ);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d("LOKLIZATOR", "Insert to database!");
        int typUri = sDopasowanieUri.match(uri);
        SQLiteDatabase baza = mPomocnikBD.getWritableDatabase();
        long idDodanego = 0;
        switch (typUri) {
            case CALA_TABELA:
                idDodanego = baza.insert(DBHelper.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Nieznane URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(DBHelper.TABLE_NAME + "/" + idDodanego);
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int typUri = sDopasowanieUri.match(uri);
        SQLiteDatabase baza = mPomocnikBD.getWritableDatabase();
        Cursor kursorTel = null;
        switch (typUri) {
            case CALA_TABELA:
                kursorTel = baza.query(false, DBHelper.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder, null, null);
                break;
            case WYBRANY_WIERSZ:
                kursorTel = baza.query(false, DBHelper.TABLE_NAME, projection, dodajIdDoSelekcji(selection, uri), selectionArgs, null, null, sortOrder, null, null);
                break;
            default:
                throw new IllegalArgumentException("Nieznane URI: " + uri);
        }
        kursorTel.setNotificationUri(getContext().getContentResolver(), uri);
        return kursorTel;
    }

    @Override
    public boolean onCreate() {
        mPomocnikBD = new DBHelper(getContext());
        return true;
    }

    private String dodajIdDoSelekcji(String selekcja, Uri uri) {
        if (selekcja != null && !selekcja.equals(""))
            selekcja = selekcja + " and " + DBHelper.ID + "=" + uri.getLastPathSegment();
        else
            selekcja = DBHelper.ID + "=" + uri.getLastPathSegment();
        return selekcja;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int typUri = sDopasowanieUri.match(uri);
        SQLiteDatabase baza = mPomocnikBD.getWritableDatabase();
        int liczbaUsunietych = 0;
        switch (typUri) {
            case CALA_TABELA:
                liczbaUsunietych = baza.delete(DBHelper.TABLE_NAME, selection, selectionArgs);
                break;
            case WYBRANY_WIERSZ:
                liczbaUsunietych = baza.delete(DBHelper.TABLE_NAME, dodajIdDoSelekcji(selection, uri), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Nieznane URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return liczbaUsunietych;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int typUri = sDopasowanieUri.match(uri);
        SQLiteDatabase baza = mPomocnikBD.getWritableDatabase();
        int liczbaZaktualizowanych = 0;
        switch (typUri) {
            case CALA_TABELA:
                liczbaZaktualizowanych = baza.update(DBHelper.TABLE_NAME, values, selection, selectionArgs);
                break;
            case WYBRANY_WIERSZ:
                liczbaZaktualizowanych = baza.update(DBHelper.TABLE_NAME, values, dodajIdDoSelekcji(selection, uri), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Nieznane URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return liczbaZaktualizowanych;
    }
}

