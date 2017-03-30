/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.my.game.wesport.data;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;

/**
 * API Contract for the Games app.
 */
public final class GameContract {

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.my.game.wesport";
    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.games/games/ is a valid path for
     * looking at game data. content://com.example.android.games/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_GAMES = "games";

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private GameContract() {

    }

    /**
     * Inner class that defines constant values for the games database table.
     * Each entry in the table represents a single game.
     */
    public static final class GameEntry implements BaseColumns {

        /**
         * The content URI to access the game data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_GAMES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of games.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GAMES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single game.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GAMES;

        /**
         * Name of database table for games
         */
        public final static String TABLE_NAME = "games";

        /**
         * Unique ID number for the game (only for use in the database table).
         * <p>
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * GameModel Nam
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_GAME_NAME = "gamename";

        /**
         * GameModel Description
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_GAME_DESC = "name";

        /**
         * GameModel startdate
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_START_DATE = "startdate";

        /**
         * GameModel starttime
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_START_TIME = "starttime";

        /**
         * GameModel endtime
         * <p>
         * Type: TEXT
         */
        public final static String COLUMN_END_TIME = "endtime";

        /**
         * skill for the game.
         * <p>
         * Type: INTEGER
         */

        public final static String COLUMN_GAME_SKILL = "skill";

        public final static String COLUMN_GAME_NOTES = "notes";


        /**
         * Storing Username and Location for the GameModel.
         */
        public final static String COLUMN_USER_NAME = "username";

        public final static String COLUMN_GAME_ADDRESS = "address";

        /**
         * Possible values for the Skilllevel for the GameModel.
         */
        public static final int SKILL_ROOKIES = 0;
        public static final int SKILL_VET = 1;
        public static final int SKILL_PRO = 2;


        /**
         * Returns whether or not the given skill is valid
         */
        public static boolean isValidskill(int skill) {
            return skill == SKILL_ROOKIES || skill == SKILL_VET || skill == SKILL_PRO;
        }

        public static String getUserName(Context context) {
            //Get Username from sharedpreferences
            SharedPreferences prefUser = PreferenceManager.getDefaultSharedPreferences(context);
            return prefUser.getString("displayName", "user");
        }

    }


}
